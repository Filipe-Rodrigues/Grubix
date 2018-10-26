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

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;
import static java.lang.Math.*;

/**
 * The VariableDisc Model is a simple signal propagation model based on the 
 * free space propatation model, where a packet reaches the receiver with 
 * signal strength s(rx) = s(tx) / d² (d is the euclidean distance between
 * sender and receiver). If s(rx) < threshold t, the receiver is considered
 * to be not reachable by the sender. 
 * @author jlsx
 *
 */
public class VariableDisc extends PhysicalModel {

	@ShoXParameter(description = "minimum rssi for a sending node, who can reach this node.")
	private double reachableThreshold;
	
	@ShoXParameter(description = "minimum rssi for a sending node, who can interfere with this node.")
	private double interferenceThreshold;


	/**
	 * Constructor of the class VariableDisc.
	 */
	public VariableDisc() {
	}
	
	/**
	 * use the variable disc model, to calculate a reachability object for the
	 * supplied sender and receiver and a given signal strength.
	 * @see br.ufla.dcc.grubix.simulator.physical.PhysicalModel#apply(
	 * 			br.ufla.dcc.grubix.simulator.Position, br.ufla.dcc.grubix.simulator.Position,
	 * 			double)
     * @param receiver       the Position of the receiver
     * @param sender         the Position of the sender
     * @param signalStrength the used signal strength
     * @return the resulting attenuation and whether the receiver is reachable at all 
	 */
	@Override
	public final Reachability apply(Node re, Node se, double signalStrength) {
		
		/// BUGADAO ARRUMAR
		Position receiver = new Position(0,0); Position sender = new Position(0,0);
		Reachability reachability = new Reachability();
		double rssi, attenuation;

		reachability.setSignalStrength(signalStrength);
		reachability.setPositions(receiver, sender);
		attenuation = 1 / pow(reachability.getDistance(), 2);
		reachability.setAttenuation(attenuation);

		rssi = attenuation * signalStrength;
		
		reachability.setReachable(rssi >= reachableThreshold);
		reachability.setInterfering(rssi >= interferenceThreshold);
		reachability.setSsAtReceiver(rssi);

		return reachability;
	}

	/* (non-Javadoc)
	 * @see br.ufla.dcc.grubix.simulator.physical.PhysicalModel#initConfiguration(br.ufla.dcc.grubix.simulator.kernel.Configuration)
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		
		if (interferenceThreshold > reachableThreshold) {
			throw new ConfigurationException("interferenceThreshold must be <= reachableThreshold.");
		}
	}

	/**
	 * @return the reachableThreshold
	 */
	public final double getReachableThreshold() {
		return reachableThreshold;
	}

	/**
	 * @return the interferenceThreshold
	 */
	public final double getInterferenceThreshold() {
		return interferenceThreshold;
	}
}
