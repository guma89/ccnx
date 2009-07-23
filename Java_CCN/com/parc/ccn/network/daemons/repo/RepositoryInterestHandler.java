package com.parc.ccn.network.daemons.repo;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import com.parc.ccn.Library;
import com.parc.ccn.config.SystemConfiguration;
import com.parc.ccn.data.ContentName;
import com.parc.ccn.data.ContentObject;
import com.parc.ccn.data.content.LinkReference;
import com.parc.ccn.data.query.CCNFilterListener;
import com.parc.ccn.data.query.Interest;
import com.parc.ccn.library.CCNLibrary;
import com.parc.ccn.library.CCNNameEnumerator;
import com.parc.ccn.library.profiles.CommandMarkers;
import com.parc.ccn.network.daemons.repo.Repository.NameEnumerationResponse;

/**
 * 
 * @author rasmusse
 *
 */

public class RepositoryInterestHandler implements CCNFilterListener {
	private RepositoryDaemon _daemon;
	private CCNLibrary _library;
	
	public RepositoryInterestHandler(RepositoryDaemon daemon) {
		_daemon = daemon;
		_library = daemon.getLibrary();
	}

	public int handleInterests(ArrayList<Interest> interests) {
		for (Interest interest : interests) {
			try {
				if (SystemConfiguration.getLogging("repo"))
					Library.logger().finer("Saw interest: " + interest.name());
				if (interest.name().contains(CommandMarkers.REPO_START_WRITE)) {
					startReadProcess(interest);
				} else if(interest.name().contains(CCNNameEnumerator.NEMARKER)){
					nameEnumeratorResponse(interest);
				} else if(interest.name().contains(CommandMarkers.REPO_GET_HEADER)){
					getHeader(interest);
				}
				
				ContentObject content = _daemon.getRepository().getContent(interest);
				if (content != null) {
					Library.logger().finest("Satisfying interest: " + interest + " with content " + content.name());
					_library.put(content);
				} else {
					Library.logger().fine("Unsatisfied interest: " + interest);
				}
			} catch (Exception e) {
				Library.logStackTrace(Level.WARNING, e);
				e.printStackTrace();
			}
		}
		return interests.size();
	}
	
	private void startReadProcess(Interest interest) throws XMLStreamException {
		for (RepositoryDataListener listener : _daemon.getDataListeners()) {
			if (listener.getOrigInterest().equals(interest)) {
				Library.logger().info("Write request " + interest.name() + " is a duplicate, ignoring");
				return;
			}
		}
		
		/*
		 * For now we need to wait until all current sessions are complete before a namespace
		 * change which will reset the filters is allowed. So for now, we just don't allow any
		 * new sessions to start until a pending namespace change is complete to allow there to
		 * be space for this to actually happen. In theory we should probably figure out a way
		 * to allow new sessions that are within the new namespace to start but figuring out all
		 * the locking/startup issues surrounding this is complex so for now we just don't allow it.
		 */
		if (_daemon.getPendingNameSpaceState()) {
			Library.logger().info("Discarding write request " + interest.name() + " due to pending namespace change");
			return;
		}
		
		ContentName listeningName = new ContentName(interest.name().count() - 2, interest.name().components());
		try {
			Library.logger().info("Processing write request for " + listeningName);
			Integer count = interest.nameComponentCount();
			if (count != null && count > listeningName.count())
				count = null;
			Interest readInterest = Interest.constructInterest(listeningName, _daemon.getExcludes(), null, count);
			RepositoryDataListener listener = _daemon.addListener(interest, readInterest);
			_daemon.getWriter().put(interest.name(), _daemon.getRepository().getRepoInfo(null), null, null,
					_daemon.getFreshness());
			_library.expressInterest(readInterest, listener);
		} catch (Exception e) {
			Library.logStackTrace(Level.WARNING, e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Since the headers are currently sent out last, and we don't want to send out
	 * unnecessary interests early in a stream because this kills performance, the client
	 * side repo code will specifically ask for a header. This returns it.
	 * 
	 * @param interest
	 * @throws XMLStreamException
	 */
	private void getHeader(Interest interest) throws XMLStreamException {
		ContentName listeningName = new ContentName(interest.name().count() - 2, interest.name().components());
		for (RepositoryDataListener listener : _daemon.getDataListeners()) {
			if (listener.getOrigInterest().name().equals(listeningName)) {		
				try {
					Interest readInterest = Interest.constructInterest(listener.getVersionedName(), _daemon.getExcludes(), null, 
							listener.getVersionedName().count());
					readInterest.additionalNameComponents(1);
					_library.expressInterest(readInterest, listener);
				} catch (IOException e) {
					Library.logStackTrace(Level.WARNING, e);
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	public void nameEnumeratorResponse(Interest interest) {
		//the name enumerator marker won't be at the end if the interest is a followup (created with .last())
		//else if(Arrays.equals(marker, CCNNameEnumerator.NEMARKER)){
		//System.out.println("handling interest: "+interest.name().toString());
		//ContentName prefixName = interest.name().cut(CCNNameEnumerator.NEMARKER);
		
		NameEnumerationResponse ner = _daemon.getRepository().getNamesWithPrefix(interest);
		if(ner!=null && ner.prefix!=null){
			try{
				
				//the following 6 lines are to be deleted after Collections are refactored
				LinkReference[] temp = new LinkReference[ner.names.size()];
				for(int x = 0; x < ner.names.size(); x++)
					temp[x] = new LinkReference(ner.names.get(x));
				
				
				_library.put(ner.prefix, temp);
				
				//CCNEncodableCollectionData ecd = new CCNEncodableCollectionData(collectionName, cd);
				//ecd.save();
				//System.out.println("saved ecd.  name: "+ecd.getName());
			}
			catch(IOException e){
				
			}
			catch(SignatureException e) {
				Library.logStackTrace(Level.WARNING, e);
				e.printStackTrace();
			}
		}
	}
}
