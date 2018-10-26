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

package br.ufla.dcc.grubix.simulator.node.user;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.PhysicalTimingParameters;

/**
 * this class collects the timing parameters needed for the IEEE 802.11b,g MAC/Phy.
 * These Values are converted to simulation steps !!!!!
 *  
 * @author Dirk Held
 */
public class IEEE_802_11_TimingParameters extends PhysicalTimingParameters {
	/** contains to the simulationsteps converted value of SIFS. */
	private double sifs;
	/** contains to the simulationsteps converted value of DIFS. */
	private double pifs;
	/** contains to the simulationsteps converted value of PIFS. */
	private double difs;
	/** contains to the simulationSteps converted value of SlotTime. */
	private double slotTime;
	
	/** define the time of a long preamble. */
	private double longPreamble;
	/** define the time of a short preamble @ 2 MBit. */
	private double shortPreamble;
	/** define the time of a short preamble @ 54 MBit. */
	private double ofdmPreamble;
	/** contains the precomputed duration of a wlan header PLCP-Preamble/header in simulation steps. */
	private double syncDuration;
	/** contains the to us converted value of the syncDuration. */
	private int syncLength;

	/**
	 * default constructor to initialize the timing parameters.
	 * 
	 * Use isB==false, isLongPreamble==false for 802.11g only.
	 * @param isB			 is true, if 802.11b is requested.
	 * @param isLongPreamble is true, if a long preamble is requested.
	 */
	public IEEE_802_11_TimingParameters(boolean isB, boolean isLongPreamble) {
		Configuration configuration = Configuration.getInstance();
		
		if (isB) {
			slotTime = configuration.getSimulationSteps(0.000020);
		} else {
			slotTime = configuration.getSimulationSteps(0.000009);
		}

		sifs = configuration.getSimulationSteps(0.000010);
		pifs = sifs + slotTime;
		difs = sifs + 2.0 * slotTime;
	
		int longPre  = 192;
		int shortPre =  96;
		int ofdmPre  =  26;
		
		longPreamble  = configuration.getSimulationSteps(0.000192);
		shortPreamble = configuration.getSimulationSteps(0.000096);
		ofdmPreamble  = configuration.getSimulationSteps(0.000026); // 16 + 4 + 6
		
		int count = 4;

		if (!isB) {
			count = 8;
			if (isLongPreamble) {
				count = 12;
			}
		}
		
		bps = new double[count];
		maxBitrateIDX = count - 1;

		double mbit = 1000000.0 / configuration.getSimulationSteps(1.0);
		
		if (isB) {
			bps[0] =       mbit; bps[1] =  2 * mbit;
			bps[2] = 5.5 * mbit; bps[3] = 11 * mbit;
			this.maxBitrateIDX = 3;
				
			if (isLongPreamble) {
				syncDuration = longPreamble;
				syncLength   = longPre;
			} else {
				syncDuration = shortPreamble;
				syncLength   = shortPre;
			}
		} else {
			if (isLongPreamble) { // compatibility mode for b and g devices
				bps[0] =      mbit;	bps[ 1] =  2 * mbit; bps[ 2] = 5.5 * mbit;
				bps[3] =  6 * mbit;	bps[ 4] =  9 * mbit; bps[ 5] = 11  * mbit;
				bps[6] = 12 * mbit;	bps[ 7] = 18 * mbit; bps[ 8] = 24  * mbit;
				bps[9] = 36 * mbit;	bps[10] = 48 * mbit; bps[11] = 54  * mbit;

				syncDuration = longPreamble;
				syncLength   = longPre;
				this.maxBitrateIDX = 11;
			} else {
				bps[0] =  6 * mbit; bps[1] =  9 * mbit; bps[2] = 12 * mbit;	
				bps[3] = 18 * mbit; bps[4] = 24 * mbit; bps[5] = 36 * mbit;	
				bps[6] = 48 * mbit; bps[7] = 54 * mbit;

				syncDuration = ofdmPreamble;
				syncLength   = ofdmPre;
				this.maxBitrateIDX = 7;
			}
		}
	}

	/** @return the difs. */
	public final double getDifs() {
		return difs;
	}

	/** @return the pifs. */
	public final double getPifs() {
		return pifs;
	}

	/** @return the sifs. */
	public final double getSifs() {
		return sifs;
	}

	/** @return the slotTime. */
	public final double getSlotTime() {
		return slotTime;
	}

	/** @return the longPreamble. */
	public final double getLongPreamble() {
		return longPreamble;
	}

	/** @return the ofdmPreamble. */
	public final double getOfdmPreamble() {
		return ofdmPreamble;
	}

	/** @return the shortPreamble. */
	public final double getShortPreamble() {
		return shortPreamble;
	}

	/** @return the syncDuration in simulation steps. */
	public final double getSyncDuration() {
		return syncDuration;
	}

	/** @return the syncLength in us. */
	public final int getSyncLength() {
		return syncLength;
	}
}
