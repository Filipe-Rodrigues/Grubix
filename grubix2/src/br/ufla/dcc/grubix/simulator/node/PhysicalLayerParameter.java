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

package br.ufla.dcc.grubix.simulator.node;


/**
 * Class to define the needed parameters for the physical layer.
 * 
 * @author Dirk Held
 */
public class PhysicalLayerParameter {
	/** the current used signal strength. */
	protected final double signalStrength;
	/** the smallest supported signal strength. */
	protected final double minSignalStrength;
	/** the largest supported signal strength. */
	protected final double maxSignalStrength;
	/** the number of supported channels. */
	protected final int channelCount;
	/** the smallest used frequency (centered). */
	protected final double minFrequency;
	/** the largest used frequency (centered). */
	protected final double maxFrequency;
	
	/** @return the current number of channels. */
	public int getChannelCount() {
		return channelCount;
	}

	/** @return the largest used frequency. */
	public double getMaxFrequency() {
		return maxFrequency;
	}

	/** @return the largest supported signal strength. */
	public double getMaxSignalStrength() {
		return maxSignalStrength;
	}

	/** @return the smallest supported frequency. */
	public double getMinFrequency() {
		return minFrequency;
	}

	/** @return the smallest supported signal strength. */
	public double getMinSignalStrength() {
		return minSignalStrength;
	}

	/** @return the current used signal strength. */
	public double getSignalStrength() {
		return signalStrength;
	}

	/**
	 * Default constructor for the class PhysicalLayerParameter.
	 * 
	 * @param signalStrength    the current used, or the new signal strength.
	 * @param minSignalStrength the minimal possible signal strength.
	 * @param maxSignalStrength the maximal possible signal strength.
	 * @param channelCount      the number of supported channels. 
	 * @param minFrequency      the minimal used frequency (centered).
	 * @param maxFrequency      the minimal used frequency (centered).
	 */
	public PhysicalLayerParameter(final double signalStrength, final double minSignalStrength, 
								  final double maxSignalStrength, final int channelCount, 
								  final double minFrequency, final double maxFrequency) {
		super();
		this.signalStrength = signalStrength;
		this.minSignalStrength = minSignalStrength;
		this.maxSignalStrength = maxSignalStrength;
		this.channelCount = channelCount;
		this.minFrequency = minFrequency;
		this.maxFrequency = maxFrequency;
	}
}
