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

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.BitrateAdaptationPolicy;
import br.ufla.dcc.grubix.simulator.node.MACLayer;

/**
 * This class implements a bitrate adaptation policy to be used for 802.11
 * called ARF and is extended to act as adaptive ARF too (AARF).
 * 
 * @author Dirk Held
 */
public class AARFRateAdaptation extends BitrateAdaptationPolicy {

	/** use no ARF and keep bitrate static. */
	public static final int NO_ARF = 0; 
	/** use ARF to adjust the bitrate. */
	public static final int ARF    = 1;
	/** use adaptive ARF to adjust the bitrate. */
	public static final int AARF   = 2;
	
	/** the current mode of the rate adaptation (none,ARF,AARF). */
	private int mode;
	
	/** The MAC that is associated with this adaptation policy. */
	private MACLayer mac;
	
	/** Constant by which the PHY timings bps value must be divided to get Mbps. */
	private double mBit;
	
	/** 
	 * If this rate adaption level is > 0, raLevel packets where transmitted 
	 * successfully. If it is < 0, raLevel packets could't be transmitted in succession.
	 */
	private int raLevel;
	/** if raLevel drops below raDownLevel, the bitrate is lowerd. */
	private int raDownLevel;
	/** if raLevel rises beyond raUpLevel, the bitrate is increased. */
	private int raUpLevel;
	/** the initial value for raUpLevel for AARF without multiplicative increase. */ 
	private int raUpInit;
	/** the multiplicator for AARF (for example 2) to increase raUpLevel by. */
	private int raUpMult;
	/** if true, the bitrate was just risen. If a packet is lost then raUpMult is applied to raUpLevel. */ 
	private boolean raUpTry;
	/** time of the last packet retry. */
	private double lastRetry;
	/** time to wait after the last retry, to again try to rise the bitrate. */
	private double raTimeout;
	
	/**
	 * default constructor.
	 * 
	 * @param mac               the mac layer, this object belongs to.
	 * @param initialBitrateIdx the initial index into the bitrate array.
	 * @param maxBitrateIdx     the largest possible index into the bitrate array.
	 * @param mode              the mode as NO_ARF, ARF or AARF.
	 * @param downLevel         the number of failures to tolerate before reducing the bitrate.
	 * @param upLevel           the number of good transmissions, before increasing the bitrate.
	 * @param upMult            the multiplicator for the upLevel.
	 * @param timeout           time to wait after a successfull transmission, to reset the bitrate.
	 */
	public AARFRateAdaptation(MACLayer mac, int initialBitrateIdx, int maxBitrateIdx, int mode, 
			                  int downLevel, int upLevel, int upMult, double timeout) {
		super(initialBitrateIdx, maxBitrateIdx);
		
		this.mac = mac;
		this.mBit = 1000000 / Configuration.getInstance().getSimulationSteps(1);
		this.mode = mode;
		raDownLevel = downLevel;
		raUpLevel   = upLevel;
		raUpInit    = upLevel;
		raUpMult    = upMult;
		raTimeout   = timeout;
		
		raLevel   = 0;
		raUpTry   = false;
		lastRetry = 0.0;
	}

	/**
	 * implements the clone method to duplicate rate adaptation policies
	 * from the default rate adaption policy of the MAC for example.
	 * @return a clone of the object.
	 */
	public Object clone() {
		AARFRateAdaptation obj = (AARFRateAdaptation) super.clone();
		
		obj.mode = mode;
		obj.raDownLevel = raDownLevel;
		obj.raUpLevel   = raUpLevel;
		obj.raUpInit    = raUpLevel;
		obj.raUpMult    = raUpMult;
		obj.raTimeout   = raTimeout;
		
		obj.raLevel   = 0;
		obj.raUpTry   = false;
		obj.lastRetry = 0.0;
		
		return obj;
	}
	
	/** a packet was transmitted succesfully, thus try to rise the bitrate. */
	@Override
	public void processSuccess() {
		if (mode > NO_ARF) {
			raUpTry = false; // if there was a bitrate increase, accept it.
			
			if (raLevel <= 0) {
				raLevel = 1;
			} else {
				raLevel++;
				
				if (raLevel > raUpLevel) {
					raUpTry = true;
					raLevel = 0;
					
					if (bitrateIdx < maxBitrateIdx) {
						bitrateIdx++;
						double bitrate = MACLayer.getTimings().getBPS(bitrateIdx) / this.mBit;
						SimulationManager.logStatistic(this.mac.getNode().getId(), LayerType.MAC, "time", "Bitrate", 
								Double.toString(SimulationManager.getInstance().getCurrentTime()), 
								Double.toString(bitrate));
					}
				}
			}
		}
	}
	
	/**
	 * standard method to process a failed transmission.
	 * @param now the time when the error happened to retrigger the timeout.
	 */
	public void processFailure(double now) {
		if (mode > NO_ARF) {
			lastRetry = now; // memorize the time of this retry.
			
			if (raLevel > 0) {
				raLevel = -1;
			} else {
				raLevel--;
			}
			
			if (raUpTry || (raLevel < raDownLevel)) {
				if (bitrateIdx > 0) {
					bitrateIdx--;
					double bitrate = MACLayer.getTimings().getBPS(bitrateIdx) / this.mBit;
					SimulationManager.logStatistic(this.mac.getNode().getId(), LayerType.MAC, "time", "Bitrate", 
							Double.toString(SimulationManager.getInstance().getCurrentTime()), 
							Double.toString(bitrate));
				} else {
					raLevel = 0;
				}

				if (mode == AARF) {
					if (raUpTry) {
						raUpLevel = raUpInit;
					}
					raUpLevel *= raUpMult;
					
					if (raUpLevel > 50) {
						raUpLevel = 50; 
					}
				}
			}
			raUpTry = false;
		}
	}
	
	/**
	 * If the last resend is too long ago (timeout), the policy is resetted.
	 * 
	 * @param now the current time.
	 */
	public void reset(double now) {
		double t = now - lastRetry; // time passed since the last retry.

		if ((raLevel < 0) && (t > raTimeout)) {
			raLevel = 0;
		}
	}
}
