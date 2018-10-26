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
 * Like the super class, but with an additional double.
 * 
 * @author Dirk Held
 */
public class CrossLayerDoubleResult extends CrossLayerResult {

	/** an additional double value. */
	private double doubleVal;
	
	/**
	 * class to describe a return value object containing an additional double value.
	 * @param result    the error code.
	 * @param doubleVal an optional double value.
	 */
	public CrossLayerDoubleResult(int result, double doubleVal) {
		super(result);
		
		this.doubleVal = doubleVal;
	}

	/** @return the doubleVal. */
	public final double getDoubleVal() {
		return doubleVal;
	}

	/** @param doubleVal the doubleVal to set. */
	public final void setDoubleVal(double doubleVal) {
		this.doubleVal = doubleVal;
	}
}
