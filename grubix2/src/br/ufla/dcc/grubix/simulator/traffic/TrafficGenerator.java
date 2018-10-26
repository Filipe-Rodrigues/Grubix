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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;
import br.ufla.dcc.grubix.xml.TimeIntervalReader;
import br.ufla.dcc.grubix.xml.XMLWriter;



/**
 * Super-class of all classes which generate application level network traffic.
 * 
 * @author jlsx
 */
public abstract class TrafficGenerator implements Configurable {

	/**
	 * Magic value subclasses can return in getDelayToNextQuery() to avoid generating
	 * traffic anytime in the future. 
	 */
	public static final double NO_TRAFFIC_ANYMORE = -1.0;
	
	/**
	 * The name of the file containing the trace.
	 */
	@ShoXParameter(description = "The name of the file containing the trace", defaultValue = "")
	protected File traceFileName;

	/**
	 * Use either read or write to access the trace.
	 */
	@ShoXParameter(description = "Use either read or write to access the trace.")
	protected String traceFileMode;

	/**
	 * A value between 0 and 1 defining the current amount of traffic generated
	 * by this application. 0 means (almost) no traffic, 1 means permanent
	 * traffic.
	 */
	protected double trafficIntensity = 0.5;

	/**
	 * Logger of the class TrafficGenerator.
	 */
	private static final Logger LOGGER = Logger.getLogger(TrafficGenerator.class);

	/** Identifier (content of param's name attribute for read mode. */
	private static final String READ_MODE = "read";

	/** Identifier (content of param's name attribute for write mode. */
	private static final String WRITE_MODE = "write";

	/**
	 * Allows to write directly into the trace file. Only set if traceFileMode =
	 * WRITE_MODE.
	 */
	protected XMLWriter traceWriter;

	/**
	 * Allows to read directly from the trace file. Only set if traceFileMode =
	 * READ_MODE.
	 */
	protected TimeIntervalReader traceReader;

	/**
	 * Stores the traffic generated during the last round (i.e. invocation of
	 * generateTraffic.
	 */
	protected Collection<TrafficGeneration> currentTraffic;

	/** The number of packet types which is supported by the application. */
	protected static int packetTypeCount;
	
	/**
	 * Stores the current simulation time as obtained from the simulation
	 * manager.
	 */
	private double currentTime;
	
	/**
	 * configuration.
	 */
	private Configuration config;
	
	/**
	 * Called at the beginning of simulation to initialize traffic generator.
	 * This is useful since while the constructor is executed, the parameters
	 * are not yet set.
	 * 
	 * @param configuration configuration of the current simulation run
	 * @throws ConfigurationException throws if the configuration is invalid
	 */
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		this.config = configuration;
		if (traceFileMode == null) {
			return;
		}
		if (traceFileName != null
			&& !traceFileName.getName().equals("")
			&& traceFileMode != null) {
			try {
				if (traceFileMode.equals(WRITE_MODE)) {
					traceWriter = new XMLWriter(traceFileName.getAbsolutePath());
					traceWriter.openTag("trace");
				} else if (traceFileMode.equals(READ_MODE)) {
					traceReader = new TimeIntervalReader(traceFileName.getAbsolutePath(), 
							configuration.getTrafficTimeInterval());
				} else {
					throw new ConfigurationException("invalid trace mode " + traceFileMode);
				}
			} catch (IOException e) {
				throw new ConfigurationException("IO Exception", e);
			}
		} else {
			LOGGER.debug("Trace file name and mode is null. No trace file can be read or written.");
		}
	}

	/**
	 * Invoked by the simulation manager to notify the traffic generator that
	 * the simulation ended. This enables the traffic generator to perform some
	 * cleanup at the end.
	 */
	public void endSimulation() {
		if (traceWriter != null) {
			try {
				traceWriter.closeTag("trace");
			} catch (Exception e) {
				LOGGER.error("Could not append traffic trace file. " + e);
				return;
			}
			traceWriter.close();
			LOGGER.info("Traffic trace file successully created.");
		}
	}

	/**
	 * Generates traffic for all nodes contained in the specified collection for
	 * the coming trafficTimeInterval. If traceFileMode is "write", the
	 * generated traffic is logged.
	 * 
	 * @see br.ufla.dcc.grubix.simulator.kernel.Configuration#trafficTimeInterval
	 * @param allNodes
	 *            Collection of nodes for which new traffic is to be generated
	 *            (usually all nodes in the network)
	 * @param currentTime
	 *            The current simulation time when this method is invoked
	 * @return Sorted collection of TrafficGeneration events specifying the new
	 *         traffic per node. The collection is expected to be sorted in
	 *         ascending simulation time order.
	 */
	public SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes, double currentTime) {
		this.currentTime = currentTime;
		SortedSet<TrafficGeneration> tgc = this.generateTraffic(allNodes);
		this.appendTraceFile(tgc);
		return tgc;
	}
	
	/**
	 * The TrafficGenEnvelope of SimulationManager calls this method to determine
	 * when to check for new traffic by the TrafficGenerator.
	 *  
	 * @return NO_TRAFFIC_ANYMORE if TrafficGenEnvelope should not check again,
	 * 		   a value greater or equal to zero is a valid delay in simulation steps,
	 * 		   all other (negative) values are invalid. 
	 */
	public double getDelayToNextQuery() {
		return Configuration.getInstance().getTrafficTimeInterval();
	}

	/**
	 * Internally invoked by {@link #generateTraffic(Collection, double)} to
	 * delegate the real traffic generation to the particular sub-class.
	 * 
	 * @param allNodes
	 *            allNodes Collection of nodes for which new traffic is to be
	 *            generated
	 * @return Sorted collection of TrafficGeneration events specifying the new
	 *         traffic per node. The collection is expected to be sorted in
	 *         ascending simulation time order.
	 */
	protected abstract SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes);

	/**
	 * @inheritDoc
	 */
	public void init() throws ConfigurationException {
		// do nothing
	}

	/**
	 * Sets the number of packet types which is supported by the application.
	 * 
	 * @param ptc
	 *            The number of supported packet types.
	 */
	public final static void setPacketTypeCount(int ptc) {
		packetTypeCount = ptc;
	}

	/**
	 * Appends log entries for the generated traffic specified in
	 * <code>tgc</code>.
	 * 
	 * @param tgc
	 *            A collection of TrafficGeneration events which are to be
	 *            logged
	 * @return True, if operation was successful, false otherwise
	 */
	private boolean appendTraceFile(Collection<TrafficGeneration> tgc) {
		if ((traceWriter != null) && (tgc != null)) {
			try {
				for (TrafficGeneration tgn : tgc) {
					HashMap<String, String> atts = new HashMap<String, String>();
					atts.put("time", Double.toString(this.currentTime
							+ tgn.getDelay()));
					atts.put("src", Integer.toString(tgn.getSource().asInt()));
					atts.put("dest", Integer.toString(tgn.getRecipient()
							.asInt()));
					atts.put("type", Integer.toString(tgn.getPacketType()));
					traceWriter.writeEmptyTag("traffic", atts);
				}
			} catch (Exception e) {
				LOGGER.error("Could not append traffic trace file. " + e);
				return false;
			}
		}
		return true;
	}

	/**
	 * This method sets a new traffic intensity value. This is a value between 0
	 * and 1 indicating the current amount of traffic in the network. 0 means no
	 * traffic, 1 means permanent traffic (possibly overloading the network).
	 * Note that the exact semantics of these values depend on the network
	 * designer. It is just meant as an easy means to test the network design
	 * under different loads.
	 * 
	 * @param intensity
	 *            The new traffic intensity value
	 */
	public void setTrafficIntensity(double intensity) {
		this.trafficIntensity = intensity;
	}

	/**
	 * @return the config
	 */
	public Configuration getConfig() {
		return config;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "[" + getClass().getSimpleName() + "]";
	}
}
