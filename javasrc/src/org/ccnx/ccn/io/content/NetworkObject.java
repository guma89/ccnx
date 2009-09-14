/**
 * Part of the CCNx Java Library.
 *
 * Copyright (C) 2008, 2009 Palo Alto Research Center, Inc.
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

package org.ccnx.ccn.io.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.NullOutputStream;



/**
 * A NetworkObject provides support for storing an object in a network based backing store.
 * It provides support for loading the object from the network, tracking if the object's data
 * has been changed, to determine whether it needs to be saved or not and saving the object.
 * 
 * It can have 3 states:
 * - available: refers to whether it has data (either set by caller or updated from network)
 * 		(potentiallyDirty refers to whether it has been saved since last set; it might not 
 * 		 actually be dirty if saved to same value as previous)
 * - stored: saved to network or updated from network and not since saved
 * 
 * It can be:
 * - not available (no data assigned, not saved or read, basically not ready)
 * - available but not stored (assigned locally, but not yet stored anywhere; this
 * 				means that storage-related metadata is unavailable even though data can be read
 * 				back out), or assigned locally since last stored
 * 		- if assigned locally but unchanged, it will not be rewritten and last stored
 * 			metadata 
 * - available and stored (stored by caller, or read from network)
 * 
 * Subclasses can vary as to whether they think null is valid data for an object -- i.e. 
 * whether assigning the object's value to null makes it available or not. The default behavior
 * is to not treat a null assignment as a value -- i.e. not available.
 */
public abstract class NetworkObject<E> {

	public static final String DEFAULT_CHECKSUM_ALGORITHM = "MD5"; // Care about speed, not collision-resistance.

	protected Class<E> _type;
	protected E _data;
	protected boolean _isDirty = false;
	protected boolean _isPotentiallyDirty = false;
	protected byte [] _lastSaved; // save digest of serialized item, so can tell if updated outside
								  // of setData
	protected boolean _available = false; // false until first time data is set or updated

	public NetworkObject(Class<E> type) {
		_type = type;
	}
	
	public NetworkObject(Class<E> type, E data) {
		this(type);
		setData(data); // marks data as available if non-null
	}

	protected E factory() throws IOException {
		E newE;
		try {
			newE = _type.newInstance();
		} catch (InstantiationException e) {
			Log.warning("Cannot wrap class " + _type.getName() + " -- impossible to construct instances!");
			throw new IOException("Cannot wrap class " + _type.getName() + " -- impossible to construct instances!");
		} catch (IllegalAccessException e) {
			Log.warning("Cannot wrap class " + _type.getName() + " -- cannot access default constructor!");
			throw new IOException("Cannot wrap class " + _type.getName() + " -- cannot access default constructor!");
		}
		return newE;
	}

	public void update(InputStream input) throws IOException, XMLStreamException {

		E newData = readObjectImpl(input);

		synchronized(this) {
			if (!_available) {
				Log.info("Update -- first initialization.");
			}

			_data = newData;
			_available = true;
			setDirty(false);
			_lastSaved = digestContent();
		}
	}
	
	/**
	 * Have we read any data yet? Only valid at beginning; doesn't tell
	 * you if update has gone through.
	 * @return false if data has not been set or updated from the network yet
	 */
	public synchronized boolean available() {
		return _available; // do we need to return a copy of a primitive type?
	}
	
	public synchronized void setData(E data) { 
		if (null != _data) {
			if (!_data.equals(data)) {
				_data = data;
				setDirty(true);
				setAvailable(data != null);
			}
			// else -- setting to same value, not dirty, do nothing
		} else {
			if (data != null) {
				_data = data;
				setDirty(true);
				setAvailable(true);				
			}
			// else -- setting from null to null, do nothing
		}
	}

	/**
	 * @param available
	 */
	protected synchronized void setAvailable(boolean available) {
		_available = available;
	}

	/**
	 * Subclasses should expose methods to update _data,
	 * but possibly not _data itself. Ideally any dangerous operation
	 * (like giving access to some variable that could be changed) will
	 * mark the object as _isDirty.
	 * @return Returns the data. Whether null data is allowed or not is
	 *   determined by the subclass, which can override available() (by
	 *   default, data cannot be null).
	 * @throws ContentNotReadyException if the object has not finished retrieving data/having data set
	 */
	protected synchronized E data() throws ContentNotReadyException, ContentGoneException { 

		if (!available()) {
			throw new ContentNotReadyException("No data yet saved or retrieved!");
		}
		// Mark that we've given out access to the internal data, so we know someone might
		// have changed it.
		_isPotentiallyDirty = true;
		// return a pointer to the current data. No guarantee that this will continue
		// to be what we think our data unless caller holds read lock.
		return _data; 
	}
	
	/**
	 * Base behavior -- always write when asked.
	 * @param output
	 * @throws IOException
	 */
	public synchronized void save(OutputStream output) throws IOException {
		if (null == _data) {
			throw new InvalidObjectException("No data to save!");
		}
		internalWriteObject(output);
	}

	/**
	 * Write only if necessary.
	 * @param output
	 * @throws IOException
	 */
	public synchronized void saveIfDirty(OutputStream output) throws IOException, XMLStreamException {

		if (available() && isDirty()) {
			save(output);
		}
	}
	
	protected byte [] digestContent() throws IOException {
		try {
			// Otherwise, might have been written when we weren't looking (someone accessed
			// data and then changed it).
			DigestOutputStream dos = new DigestOutputStream(new NullOutputStream(), MessageDigest.getInstance(DEFAULT_CHECKSUM_ALGORITHM));
			writeObjectImpl(dos);
			dos.flush();
			dos.close();
			byte [] currentValue = dos.getMessageDigest().digest();
			return currentValue;
		} catch (NoSuchAlgorithmException e) {
			Log.warning("No pre-configured algorithm {0} available -- configuration error!", DEFAULT_CHECKSUM_ALGORITHM);
			throw new RuntimeException("No pre-configured algorithm " + DEFAULT_CHECKSUM_ALGORITHM + " available -- configuration error!");
		} catch (XMLStreamException e) {
			Log.warning("Encoding exception determining whether an object is dirty: {0}", e);
			throw new IOException("Encoding exception determining whether an object is dirty: " + e);
		}
	}

	protected synchronized boolean isDirty() throws IOException {

		if (_isDirty) {
			return _isDirty;
		} else if (_lastSaved == null) {
			if (_data == null)
				return false;
			return true;
		}
		if (_isPotentiallyDirty) {
			byte [] currentValue = digestContent();

			if (Arrays.equals(currentValue, _lastSaved)) {
				Log.info("Last saved value for object still current.");
				_isDirty = false;
			} else {
				Log.info("Last saved value for object not current -- object changed.");
				_isDirty = true;
			}
		} else {
			// We've never set the data, nor given out access to it. It can't be dirty.
			Log.finer("NetworkObject: data cannot be dirty.");
			_isDirty = false;
		}

		return _isDirty; 
	}
	
	/**
	 * True if the content was either read from the network or was saved locally.
	 * @return
	 */
	public synchronized boolean isSaved() throws IOException {
		return available() && !isDirty();
	}

	/**
	 * @param dirty
	 */
	protected synchronized void setDirty(boolean dirty) { 
		_isDirty = dirty; 
		if (!_isDirty) {
			_isPotentiallyDirty = false; // just read or written
		}
	}

	protected synchronized void internalWriteObject(OutputStream output) throws IOException {
		try {
			DigestOutputStream dos = new DigestOutputStream(output, MessageDigest.getInstance(DEFAULT_CHECKSUM_ALGORITHM));
			writeObjectImpl(dos);
			dos.flush(); // do not close dos, as it will close the output, allow caller to close
			_lastSaved = dos.getMessageDigest().digest();
			setDirty(false);
			
		} catch (NoSuchAlgorithmException e) {
			Log.warning("No pre-configured algorithm {0} available -- configuration error!", DEFAULT_CHECKSUM_ALGORITHM);
			throw new RuntimeException("No pre-configured algorithm " + DEFAULT_CHECKSUM_ALGORITHM + " available -- configuration error!");
		} catch (XMLStreamException e) {
			Log.warning("Encoding exception determining whether an object is dirty: {0}", e);
			// TODO when move to 1.6, use nested exceptions
			throw new IOException("Encoding exception determining whether an object is dirty: " + e);
		}
	}
	
	/**
	 * Subclasses override. This implements the actual object write. No flush or close necessary.
	 * @param output
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	protected abstract void writeObjectImpl(OutputStream output) throws IOException, XMLStreamException;

	/**
	 * Subclasses override. This implements the actual object read from stream, returning
	 * the new object.
	 * @throws ClassNotFoundException 
	 */
	protected abstract E readObjectImpl(InputStream input) throws IOException, XMLStreamException;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_data == null) ? 0 : _data.hashCode());
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkObject<?> other = (NetworkObject<?>) obj;
		if (_type == null) {
			if (other._type != null)
				return false;
		} else if (!_type.equals(other._type))
			return false;
		if (_data == null) {
			if (other._data != null)
				return false;
		} else if (!_data.equals(other._data))
			return false;
		return true;
	}

	public boolean contentEquals(Object obj) {
		if (getClass() != obj.getClass())
			return false;
		NetworkObject<?> other = (NetworkObject<?>) obj;
		if (_data == null) {
			if (other._data != null) {
				return false;
			} else {
				return true;
			}
		}
		return _data.equals(other._data);
	}
	
	@Override
	public String toString() { return (null == _data) ? null : _data.toString(); }

}