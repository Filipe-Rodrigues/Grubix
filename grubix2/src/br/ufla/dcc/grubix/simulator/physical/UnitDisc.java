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

package br.ufla.dcc.grubix.simulator.physical;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/** 
 * The UnitDisc is a deterministic model with a constant transmission delay.
 * The model therefore needs a transmission limit and a constant for the delay.
 * A packet reaches a receiver if the euclidean distance between positions of
 * sender and receiver is <= transmissionlimit.
 * 
 * @author Andreas Kumlehn
 */
public class UnitDisc extends PhysicalModel {

	
	@ShoXParameter(description = "maximum distance for a sending node, who can reach this node.")
	private static double reachableDistance;
		
	@ShoXParameter(description = "maximum distance for a sending node, who can interfere with this node.")
	private double interferenceDistance;
	
	/**Jesimar*/
	public static double getRadiusCommuntion(){
		return reachableDistance;
	}
	
	/**
	 * Constructor of the class UnitDisc.
	 */
	public UnitDisc() {
	}

	/**
	 * use the unit disc model, to calculate a reachability object for the
	 * supplied sender and receiver and a given signal strength.
     * @param receiver       the Position of the receiver
     * @param sender         the Position of the sender
     * @param signalStrength the used signal strength
     * @return the resulting attenuation and whether the receiver is reachable at all 
	 */
	@Override
	public final Reachability apply(Node receiver, Node sender, double signalStrength) {
			
		Reachability reachability = new Reachability();
		
		reachability.setAttenuation(0.0);
		reachability.setSignalStrength(signalStrength);
		reachability.setPositions(receiver.getPosition(), sender.getPosition());

		reachability.setReachable(reachability.getSquaredDistance() <= Math.pow(reachableDistance,2));
		reachability.setInterfering(reachability.getSquaredDistance() <= Math.pow(interferenceDistance,2));
		reachability.setAttenuation(0.0);
		reachability.setSsAtReceiver(signalStrength);
		
		return reachability;
	}

	/* (non-Javadoc)
	 * @see br.ufla.dcc.grubix.simulator.physical.PhysicalModel#initConfiguration(br.ufla.dcc.grubix.simulator.kernel.Configuration)
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);

	}

	/**
	 * @return the reachableDistance
	 */
	public final double getReachableDistance() {
		return reachableDistance;
	}

	/**
	 * @return the interferenceDistance
	 */
	public final double getInterferenceDistance() {
		return interferenceDistance;
	}
	
	public void setReachableDistance(double reachableDistance) {
		this.reachableDistance = reachableDistance;
	}

	public void setInterferenceDistance(double interferenceDistance) {
		this.interferenceDistance = interferenceDistance;
	}
}
