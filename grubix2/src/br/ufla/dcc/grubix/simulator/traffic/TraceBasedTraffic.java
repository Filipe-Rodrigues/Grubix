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

package br.ufla.dcc.grubix.simulator.traffic;

import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;



/**
 * This traffic generator generates traffic according to a traffic trace file. The latter has
 * to comply with conf/traffictrace.xsd and stores traffic generated previously by some other
 * traffic generator.
 * @author jlsx
 */
public class TraceBasedTraffic extends TrafficGenerator {
	
	/**
	 * Logger of the class TrafficGenerator.
	 */
	private static final Logger LOGGER = Logger.getLogger(TraceBasedTraffic.class.getName());

	/** The number of times that {@link #generateTraffic(Collection)} was already called plus 1. */
	private int round = 1;

	/**
	 * This constructor is only used internally by getInstance(). The necessary
	 * parameters to initialize this class are passed through {@link #setConfiguration(java.util.HashMap)}.
	 */
	public TraceBasedTraffic() {
	}
	
	/**
	 * Called at the beginning of simulation to initialize traffic generator. This is useful
	 * since while the constructor is executed, the parameters are not yet set.
	 */
	public final void initConfiguration() {
		if (traceReader == null) {
			LOGGER.fatal("Trace file reader could not be set up properly. Configuration must be wrong.");
			System.exit(1);
		}
		Thread trace = new Thread(traceReader);
		trace.start();		
	}

	/**
	 * Invoked by the simulation manager to notify the traffic generator that the simulation
	 * ended. This enables the traffic generator to perform some cleanup at the end.
	 */
	public final void endSimulation() {
		super.endSimulation();
		if (traceReader != null) {
			// Shut down trace reader, should not be necessary since reader will notice EOF
			LOGGER.info("Traffic trace reader shut down.");
		}		
	}

	/**
	 * Generates trace based traffic for all nodes contained in the specified collection 
	 * for the coming trafficTimeInterval.
	 * @see br.ufla.dcc.grubix.simulator.kernel.Configuration#trafficTimeInterval
	 * @param allNodes Collection of nodes for which new traffic is to be generated (usually
	 * all nodes in the network)
	 * @return Sorted collection of TrafficGeneration events specifying the new traffic per node.
	 * The collection is expected to be sorted in ascending simulation time order.
	 */
	protected SortedSet<TrafficGeneration> generateTraffic(Collection<Node> allNodes) {
		TreeSet<TrafficGeneration> ts = new TreeSet<TrafficGeneration>();
		Vector<HashMap<String, String>> nt = traceReader.getNewTraces(); 
		for (HashMap<String, String> m : nt) {
			NodeId src = NodeId.get(new Integer(m.get("src")));
			NodeId dest = NodeId.get(new Integer(m.get("dest")));
			double delay = Double.parseDouble(m.get("time")) - this.round
				* Configuration.getInstance().getTrafficTimeInterval();
			int packetType = Integer.parseInt(m.get("type"));
			TrafficGeneration tg = new TrafficGeneration(src, dest, delay, packetType);
			ts.add(tg);
		}
		traceReader.interrupt();
		this.round++;
		return ts;
	}
}
