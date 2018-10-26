/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.grubix.simulator.traffic;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * A decorating traffic generator that avoids to generate new traffic
 * in the end phase of the simulation.
 * @author dmeister
 *
 */
public class CoolDownTrafficGenerator extends TrafficGenerator implements Configurable{

	/**
	 * child traffic generator which traffic is cut off in the
	 * end phase of the simulation.
	 */
	@ShoXParameter(description = "child traffic generator", required = true)
	private TrafficGenerator generator;
	
	/**
	 * seconds to cut off at the end.
	 */
	@ShoXParameter(defaultValue = "10")
	private double secondsToCutOff;
	
	/**
	 * Constructor.
	 */
	private CoolDownTrafficGenerator() {
	}
	
	/**
	 * @param configuration configuration
	 * @throws ConfigurationException throws if the generation configuration fails
	 */
	@Override
	public void initConfiguration(Configuration configuration)
			throws ConfigurationException {
		super.initConfiguration(configuration);
		generator.initConfiguration(configuration);
	}
	
	@Override
	protected SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes) {
		return generator.generateTraffic(allNodes);
	}

	/**
	 * returns the delay to the next traffic generation phase.
	 * @see br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator#getDelayToNextQuery()
	 */
	@Override
	public double getDelayToNextQuery() {
		double currentTime = SimulationManager.getInstance().getCurrentTime();
		double cutOffTime = getConfig().getSimulationTime()
		- getConfig().getSimulationSteps(secondsToCutOff);
		
		if (currentTime < cutOffTime) {
			return super.getDelayToNextQuery();
		}
		return TrafficGenerator.NO_TRAFFIC_ANYMORE;
	}

}
