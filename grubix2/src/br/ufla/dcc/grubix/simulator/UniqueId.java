/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator;

/** 
 * Abstract superclass for all types of IDs used during the SIMULATION.
 * The numbering of ids has to be implemented in the subclasses.
 * 
 * @author Andreas Kumlehn
 */
public abstract class UniqueId implements Comparable<UniqueId> {
	
	/**
	 * The actual ID of one instance.
	 */
	private int id;
	
	/**
	 * Constructor of the class UniqueID.
	 * @param i int value of the new ID.
	 */
	protected UniqueId(int i) {
		this.id = i;
	}

	/**
	 * @return  returns the ID as int value.
	 */
	public final int asInt() {
		return id;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * @return Idvalue as String.
	 */
	public final String toString() {
		return String.valueOf(this.id);
	}
	
	/**
	 * Implements the Comparable<UniqueId> interface.
	 * @param uid Another UniqueId object to compare with
	 * @return -1, 0, 1, if this object has a smaller, equal, greater id, respectively
	 */
	public int compareTo(UniqueId uid) {
		if (this.getClass() != uid.getClass()) {
			throw new ClassCastException();
		}
		if (this.id < uid.id) {
			return -1;
		} else if (this.id == uid.id) {
			return 0;
		} 
		return 1;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
	    if (this == other) {
	       return true;
	    }
	    if (other == null) {
	       return false;
	    }
	    if (other.getClass() != getClass()) {
	      return false;
	    }
	    if (this.id != ((UniqueId) other).id) {
	      return false;  
	    }
	    return true;
	}
	
	/**
	 * @return A unique hash code for this id.
	 */
	@Override
	public int hashCode() {
		return this.id;
	}
}

