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

package br.ufla.dcc.grubix.simulator.event;

/**
 * common superclass to store the result of a (failed) transmission.
 * 
 * @author Dirk Held
 */
public class CrossLayerResult {

	/** this indicates a failed operation, which may succeed, if retried with different parameters. */
	public static final int RETRY   = -1;
	/** use to indicate a failed operation. */
	public static final int FAIL    = 0;
	/** used to indicate a successful operation. */
	public static final int SUCCESS = 1;
	
	/** the basic form of a result, an integer. */
	private int val;
	
	/**
	 * the default constructor of this class.
	 * @param result the value of the transmission result.
	 */
	public CrossLayerResult(int result) {
		val = result;
	}

	/** @return the error code. */ 
	public final int getVal() {
		return val;
	}

	/**
	 * method to change the error code.
	 * @param result the new error code.
	 */
	public final void setVal(int result) {
		val = result;
	}
}
