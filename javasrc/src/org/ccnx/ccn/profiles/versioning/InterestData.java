/*
 * Part of the CCNx Java Library.
 *
 * Copyright (C) 2011 Palo Alto Research Center, Inc.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation. 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.ccnx.ccn.profiles.versioning;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Exclude;
import org.ccnx.ccn.protocol.ExcludeAny;
import org.ccnx.ccn.protocol.ExcludeComponent;
import org.ccnx.ccn.protocol.Interest;


/**
 * Stores state about a specific Interest on the wire.  This class does not
 * do any network transactions, it only stores state about a specific interest
 * and will generate a new Interest message based on its current start, stop,
 * and exclusion list.
 */
public class InterestData implements Comparable<InterestData> {
	
	/**
	 * An Interest with unbounded timespan
	 * @param basename
	 */
	public InterestData(ContentName basename) {
		this(basename, VersionNumber.getMinimumVersion(), VersionNumber.getMaximumVersion());
	}
	
	/**
	 * An Interest with only a lower bound
	 * @param basename
	 * @param startTime
	 */
	public InterestData(ContentName basename, VersionNumber startTime) {
		this(basename, startTime, VersionNumber.getMaximumVersion());
	}
	
	/**
	 * @param startTime minimum version to include.
	 * @param stopTime maximum version to include.
	 */
	public InterestData(ContentName basename, VersionNumber startTime, VersionNumber stopTime) {
		_name = basename;
		
		setStartTime(startTime);
		setStopTime(stopTime);
	}


	public synchronized int size() {
		return _excludedVersions.size();
	}
	
	/**
	 * Order by startTime using UNSIGNED COMPARISON
	 */
	@Override
	public int compareTo(InterestData other) {
		return _startTime.compareTo(other._startTime);
	}

	/**
	 * Dont do this while in a sorted set, as the sort order will break.
	 * Start time is the minimum version to include.
	 * in milliseconds (not binarytime)
	 * 
	 @param startTime minimum version to include (milliseconds).  The earliest time is NO_START_TIME.
	 *                  if a startTime < NO_START_TIME is given (e.g. 0), NO_START_TIME is used.
	 */
	public synchronized void setStartTime(VersionNumber startTime) {
		if( VersionNumber.getMinimumVersion().after(startTime) )
			startTime = VersionNumber.getMinimumVersion();

		_startTime = startTime;
		_dirty = true;
	}

	/**
	 * stopTime is the maximum version to include.
	 * use NO_STOP_TIME for infinity. If a greater value (unsigned comparison) is
	 * given, NO_STOP_TIME is used.
	 * in milliseconds (not binarytime)
	 */
	public synchronized void setStopTime(VersionNumber stopTime) {
		if( VersionNumber.getMaximumVersion().before(stopTime) )
			stopTime = VersionNumber.getMaximumVersion();

		_stopTime = stopTime;
		_dirty = true;
	}

	/**
	 * Returns false if too many excludes in this Interest
	 * @param version
	 * @return
	 */
	public synchronized boolean addExclude(VersionNumber version) {
		if( _excludedVersions.size() >= VersioningInterestManager.MAX_FILL ) {
			if( Log.isLoggable(Log.FAC_ENCODING, Level.FINE) )
				Log.fine(Log.FAC_ENCODING, "addExclude full, not adding {0}", version);
			return false;
		}
		addExcludeUnbounded(version);
		return true;
	}

	public synchronized Interest buildInterest() {
		if( !_dirty )
			return _interest;

		ArrayList<Exclude.Element> components = new ArrayList<Exclude.Element>();

		if( VersionNumber.getMinimumVersion().before(_startTime) ) {
			VersionNumber startTimeMinusOne = _startTime.addAndReturn(-1);
			byte [] startTimeMinusOneComponent = _startTime.getBytes();

			components.add(new ExcludeAny());
			components.add(new ExcludeComponent(startTimeMinusOneComponent));
			
			if( Log.isLoggable(Log.FAC_ENCODING, Level.FINEST) )
				Log.finest(Log.FAC_ENCODING, "Exclusion: start version {0}", startTimeMinusOne);
		}

		// Now add the specific exclusions
		
		ExcludeComponent lastComponentExcluded = null;
		if( !_excludedVersions.isEmpty() ) {
			// TreeSet is sorted, so this is in right order for the exclusion filter
			Iterator<VersionNumber> i = _excludedVersions.iterator();
			while( i.hasNext() ) {
				VersionNumber elem = i.next();
				lastComponentExcluded = new ExcludeComponent(elem.getBytes());
				components.add(lastComponentExcluded);
			}
		}

		// Now exclude everything after stop time
		ExcludeComponent exStop = null;
		VersionNumber stopTimePlusOne = _stopTime.addAndReturn(1);
		byte [] stopTimePlusOneComponent = stopTimePlusOne.getBytes();
		exStop = new ExcludeComponent(stopTimePlusOneComponent);
		
		// It could happen that our stop time is exactly equal to the version of an
		// exclusion we already made.  if that's the case, don't add a duplicate
		if( null != lastComponentExcluded && ! lastComponentExcluded.equals(exStop) )
			components.add(exStop);

		components.add(new ExcludeAny());
		
		if( Log.isLoggable(Log.FAC_ENCODING, Level.FINEST) )
			Log.finest(Log.FAC_ENCODING, "Exclusion: stop  version {0}", exStop.toString());
		
		Exclude exclude;
		
		try {
			exclude = new Exclude(components);
		} catch(InvalidParameterException ipe) {
			ipe.printStackTrace();
			Log.severe(Log.FAC_ENCODING, "Parameters: " + components.toString());
			throw ipe;
		}
		
		if( Log.isLoggable(Log.FAC_ENCODING, Level.FINEST) )
			Log.finest(Log.FAC_ENCODING, "Exclusion: {0}", exclude.toString());
		
		Interest interest = Interest.last(			
				_name, 
				exclude, 
				(Integer) _name.count(), 
				(Integer) 2, // dont want anything beyond version/segment
				(Integer) 2, // version, segment
				null // publisher
		);

		// recompute density too.  This is the only place
		// where we set _dirty to false
		getDensity();
		
		_dirty = false;
		_interest = interest;
		return _interest;
	}

	/**
	 * return the last interest built.
	 */
	public Interest getLastInterest() {
		return _interest;
	}
	
	/**
	 * Is #version contained in [startTime, stopTime]?
	 * Uses UNSIGNED COMPARISON
	 * @param version
	 * @return
	 */
	public synchronized boolean contains(VersionNumber version) {
		if( _startTime.compareTo(version) <= 0 &&
			_stopTime.compareTo(version) >= 0 )
			return true;
		return false;
	}
	
	public String toString() {
		return String.format("InterestData(%s, %s, %s, %d)", _name, _startTime, _stopTime, _excludedVersions.size());
	}
	
	public String dumpContents() {
		StringBuilder sb = new StringBuilder();
		for( VersionNumber vn : _excludedVersions ) {
			sb.append(vn.printAsVersionComponent());
			sb.append(", ");
		}
		return sb.toString();
	}
	
	/**
	 * Split this object to the left, transfering #count elements
	 */
	public InterestData splitLeft(int count) {
		// create a pristine object
		InterestData left = new InterestData(_name, _startTime, _stopTime);
		if( count > 0 )
			transferLeft(left, count);		
		return left;
	}
	
	/**
	 * Split this object to the right, transfering #count elements
	 */
	public InterestData splitRight(int count) {
		// create a pristine object
		InterestData right = new InterestData(_name, _startTime, _stopTime);
		
		if( count > 0 )
			transferRight(right, count);
		
		return right;
	}
	
	/**
	 * transfer #count items from head of exclusion list to #left.  Caller
	 * has verified that #count items will fit in #left.
	 * @param left
	 * @param count
	 */
	public void transferLeft(InterestData left, int count) {
		if( count <= 0 )
			return;
		
		// walk from the left
		Iterator<VersionNumber> iter = _excludedVersions.iterator();
		
		// this is a redundant condition
		VersionNumber lastversion = null;
		while( count-- > 0 && iter.hasNext() ) {
			VersionNumber vn = iter.next();
			lastversion = vn;
			left.addExcludeUnbounded(lastversion);
			iter.remove();
		}
		
		// now fixup the start and stop times
		left.setStopTime(lastversion);
		
		// add 1 tick
		this.setStartTime(lastversion.addAndReturn(1));
		
		if( Log.isLoggable(Log.FAC_ENCODING, Level.FINE) ) 
			Log.fine(Log.FAC_ENCODING, String.format("TransferLeft to %s from %s", left.toString(), this.toString()));
	}
	
	/**
	 * transfer #count items from tail of exclusion list to #right.  Caller
	 * has verified that #count items will fit in #right.
	 * @param right
	 * @param count
	 */
	public void transferRight(InterestData right, int count) {
		if( count <= 0 )
			return;

		// walk from the right
		Iterator<VersionNumber> iter = _excludedVersions.descendingIterator();
		
		// this is a redundant condition
		VersionNumber lastversion = null;
		while( count-- > 0 && iter.hasNext() ) {
			VersionNumber vn = iter.next();
			lastversion = vn;
			right.addExcludeUnbounded(lastversion);
			iter.remove();
		}
		
		// now fixup the start and stop times
		right.setStartTime(lastversion);
		
		// subtract 1 tick
		this.setStopTime(lastversion.addAndReturn(-1));
		
		if( Log.isLoggable(Log.FAC_ENCODING, Level.FINE) ) 
			Log.fine(Log.FAC_ENCODING, String.format("TransferRight from %s to %s", this.toString(), right.toString()));
	}
	
	public synchronized VersionNumber getStartVersion() {
		return _startTime;
	}
	
	public synchronized VersionNumber getStopVersion() {
		return _stopTime;
	}
	
	/**
	 * @return stopTime - startTime + 1
	 */
	public synchronized long getWidth() {
		return _stopTime.getAsMillis() - _startTime.getAsMillis() + 1;
	}
	
	public synchronized double getDensity() {
		if( ! _dirty )
			return _density;
		
		_density = (double) size() / getWidth();
		return _density;
	}
	
	/**
	 * Sanity check that all the excluded versions fall between
	 * [start, stop] inclusive, using unsigned comparison.
	 * @return
	 */
	public synchronized boolean validate() {
		for(VersionNumber vn : _excludedVersions) {
			if( ! contains(vn) )
				return false;
		}
		return true;
	}
	
	// ===========================
	private final TreeSet<VersionNumber> _excludedVersions = new TreeSet<VersionNumber>();
	private final ContentName _name;
	private VersionNumber _startTime;
	private VersionNumber _stopTime;
	private boolean _dirty = true;
	private Interest _interest = null;
	private double _density;
	
	/**
	 * Used internally.  Sometimes we want to intentionally overflow
	 * @param version
	 */
	private void addExcludeUnbounded(VersionNumber version) {
		if( Log.isLoggable(Log.FAC_ENCODING, Level.FINER) ) 
			Log.finer(Log.FAC_ENCODING, String.format("addExcludeUnbounded %s", version.toString()));

		_excludedVersions.add(version);
		_dirty = true;
	}
	
	// ===================================
	// Inner classes
	
//	// public for testing
//	public static class TimeElement implements Comparable<TimeElement> {
//		public final CCNTime version;
//		public final byte [] versionComponent;
//
//		public TimeElement(CCNTime version) {
//			this.version = version;
//			this.versionComponent = VersioningProfile.timeToVersionComponent(version);
//		}
//
//		// So it is sortable
//		@Override
//		public int compareTo(TimeElement other) {
//			return version.compareTo(other.version);
//		}	
//	}
}
