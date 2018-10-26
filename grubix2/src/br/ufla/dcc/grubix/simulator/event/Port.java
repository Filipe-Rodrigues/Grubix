/********************************************************************************
This file is part of ShoX.
It is created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net and
the Orcos developers as defined unter http://orcos.cs.upb.de

********************************************************************************/
package br.ufla.dcc.grubix.simulator.event;

/**
 * Port of an OperatingSystemPacket.
 * This class should be immutable and can be shared between instances.
 * 
 * @author dmeister
 *
 */
public class Port {
	/**
	 * port number.
	 */
	private final int number;

	/**
	 * Constructor of the port.
	 * @param number a positive port number
	 */
	public Port(int number) {
		super();
		if (number <= 0) {
			throw new IllegalArgumentException("port number must be greater 0");
		}
		this.number = number;
	}

	/**
	 * returns the port number.
	 * @return port number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * calculates the hash code.
	 * @return a hash code
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return number;
	}

	/**
	 * tests if an object is equal to his.
	 * Compares the number attribute
	 * @param obj an object to test
	 * @return true if both objects are equal, otherwise false
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Port)) {
			return false;			
		}
		final Port other = (Port) obj;
		if (number != other.number) {
			return false;			
		}
		return true;
	}

	/**
	 * returns a human readable representation of the port.
	 * @return description of the port
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Port=" + number + "]";	
	}
}
