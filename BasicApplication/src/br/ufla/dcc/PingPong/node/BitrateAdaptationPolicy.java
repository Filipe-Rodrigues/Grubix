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

package br.ufla.dcc.PingPong.node;

public class BitrateAdaptationPolicy implements Cloneable {
	
	/** The index into the MAC bitrate array to start with. */
	protected int bitrateIdx;
	/** The highest index into the MAC bitrate array. */
	protected int maxBitrateIdx;
	
	/**
	 * Default constructor.
	 * 
	 * @param initialBitrateIdx the initial bitrate.
	 * @param maxBitrateIdx the maximum usable bitrate.
	 */
	public BitrateAdaptationPolicy(int initialBitrateIdx, int maxBitrateIdx) {
		this.bitrateIdx = initialBitrateIdx;
		this.maxBitrateIdx = maxBitrateIdx;
	}

	/**
	 * implements the clone method to duplicate rate adaptation policies
	 * from the default rate adaption policy of the MAC for example.
	 * @return a clone of the object.
	 */
	@Override
	public Object clone() {
		BitrateAdaptationPolicy obj;
		try {
			obj = (BitrateAdaptationPolicy) super.clone();
			obj.bitrateIdx    = bitrateIdx;
			obj.maxBitrateIdx = maxBitrateIdx;
			
			return obj;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/** nothing to do here for the dummy implementation. */
	public void processSuccess() {
		
	}
	
	/** 
	 * nothing to do here for the dummy implementation.
	 * @param now time, when the failure happened. 
	 */
	public void processFailure(double now) {
		
	}
	
	/** 
	 * nothing to do here for the dummy implementation.
	 * @param now time, when the failure happened. 
	 */
	public void reset(double now) {
		
	}
	
	/** @return the current bitrate index. */
	public final int getBitrateIdx() {
		return this.bitrateIdx;
	}

	/**
	 * method to manualy set the bitrate to a new value.
	 * @param bitrateIdx the new bitrate.
	 */
	public void setBitrateIdx(int bitrateIdx) {
		this.bitrateIdx = bitrateIdx;
	}

	/** @return the maxBitrateIdx. */
	public final int getMaxBitrateIdx() {
		return maxBitrateIdx;
	}
}
