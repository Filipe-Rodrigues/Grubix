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

package br.ufla.dcc.grubix.simulator.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.debug.compactlogging.ShoxLogger;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.node.LogLinkLayer;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayer;
import br.ufla.dcc.grubix.simulator.node.SimulationObserver;
import br.ufla.dcc.grubix.simulator.node.energy.EnergyManager;
import br.ufla.dcc.grubix.simulator.node.user.os.OperatingSystemLayer;
import br.ufla.dcc.grubix.xml.ConfigurableFactory;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ConfigurationParameter;
import br.ufla.dcc.grubix.xml.PositionHandler;


/**
 * The node generator for the SIMULATION. Called once during start up.
 * 
 * @author Andreas Kumlehn
 */
public class NodeGenerator {

	/**
	 * Logger of the class NodeGenerator.
	 */
	private static final Logger LOGGER = Logger.getLogger(NodeGenerator.class);

	/**
	 * Empty Constructor to avoid public instanciation.
	 */
	private NodeGenerator() {
	}

	/**
	 * Static method to generate nodes from a provided CONFIGURATION file.
	 * 
	 * @param allNodes
	 *            Map to fill with nodes.
	 * @param configuration
	 *            The configuration.
	 * @throws ConfigurationException might be thrown.
	 */
	@SuppressWarnings("unchecked")
	public static void generateNodes(Configuration configuration, SortedMap<NodeId, Node> allNodes) 
		throws ConfigurationException {
		StartPositionGenerator posGenerator = configuration.getPositionGenerator();
		Map<Integer, Position> positions = configuration.getStartPositions();
		String positionsFile = configuration.getStartPositionsFile();

		if (positions == null) {
			// positions will be generated and inserted in the following loop
			configuration.setStartPositions(new HashMap<Integer, Position>());
			if ((posGenerator == null) || (positionsFile == null)) {
				// there are missing values in the configuration
				LOGGER.fatal("Either positions, a position file or a generator and a target file must be specified.");
				throw new ConfigurationException(
						"Either positions, a position file or a generator and a target file must be specified.");
			}
			if (posGenerator != null) {
				posGenerator.initConfiguration(configuration);
			}
		}

		///ConfigurableFactory<Node> nodeFactory = createNodeFactory(configuration);
		Map<String,ConfigurableFactory<Node>> nodeFactories = new HashMap<String,ConfigurableFactory<Node>>();
		
		Map<String, Integer> nameAndAmmount = configuration.getNameAndAmmount();
		
		Set<String> keySet = nameAndAmmount.keySet();
		int j = 0;
		for (String name : keySet) {
			ConfigurableFactory<Node> nodeFactory = createNodeFactory(configuration, j);
			nodeFactories.put(name,nodeFactory);
			j++;
		}
		
		int k = 0;
		for (String name : keySet) {
			Integer ammount = nameAndAmmount.get(name);
			for (int i = 0; i < ammount; i++) {
				Node nextNode = nodeFactories.get(name).newInstance();
				nextNode.setNodeName(name);
				if(configuration.getListIsMobile().size() > 0){
					nextNode.setIsMobile(configuration.getListIsMobile().get(k));/**Jesimar*/		
				}
				
				Position realPos;
				if (positions == null) {
					// we obviously have to generate positions
					realPos = posGenerator.newPosition(nextNode);
					
					// we store the generated value in Configuration, so later we
					// can just flush it to XML
					configuration.getStartPositions().put(nextNode.getId().asInt(), realPos);
				} else {
					// positions were stored in the configuration (embedded or
					// externally)
					realPos = positions.get(nextNode.getId().asInt());
				}
				nextNode.setPosition(realPos);
				
				allNodes.put(nextNode.getId(), nextNode);
				LOGGER.debug("Created node number " + nextNode.getId());
			}
			k++;
		}
		
		if (positions == null && !configuration.getStartPositionsFile().equals("")) {
			LOGGER.debug("Writing node positions into position file.");
			PositionHandler posHandler = new PositionHandler(configuration.getStartPositionsFile(), 
					configuration.getStartPositions());
			posHandler.writeToXML();
			LOGGER.debug("Writing node positions done.");
		}


		if (configuration.getTrafficGenerator() != null) {
			configuration.getTrafficGenerator().initConfiguration(configuration);
		}
		if (configuration.getMovementManager() != null) {
			configuration.getMovementManager().initSimulation(allNodes.values());
		}
		for (Node node : allNodes.values()) {
			node.initConfiguration(configuration);
		}

		LOGGER.info("Nodes generated.");
	}

	/**
	 * @param configuration
	 * @return
	 * @throws ConfigurationException
	 */
	@SuppressWarnings("unchecked")
	private static ConfigurableFactory createNodeFactory(Configuration configuration, int index) throws ConfigurationException {
		
		ConfigurableFactory airModuleFactory = configuration.getAirModuleFactory();
		ArrayList<ConfigurableFactory<PhysicalLayer>> physicalFactory = configuration.getPhysicalLayerFactory();
		ArrayList<ConfigurableFactory<MACLayer>> macFactory = configuration.getMacLayerFactory();
		ArrayList<ConfigurableFactory<LogLinkLayer>> logLinkFactory = configuration.getLogLinkLayerFactory();
		ArrayList<ConfigurableFactory<NetworkLayer>> networkFactory = configuration.getNetworkLayerFactory();
		ArrayList<ConfigurableFactory<OperatingSystemLayer>> operatingSystemFactory = configuration.getOperatingSystemLayerFactory();
		ArrayList<ConfigurableFactory<ApplicationLayer>> applicationFactory = configuration.getApplicationLayerFactory();
		
		ArrayList<ConfigurableFactory<EnergyManager>> energyManagerFactory = configuration.getEnergyManagerLayerFactory();
		
		// pre generate global obersers
		SimulationObserver[] globalObservers = new SimulationObserver[configuration.getObserverFactory().length];
		for (int i = 0; i < globalObservers.length; i++) {
			globalObservers[i] = configuration.getObserverFactory()[i].newInstance();
		}
		
		ConfigurableFactory<Node> nodeFactory = (ConfigurableFactory<Node>) 
			ConfigurationParameter.getFactory(Node.class);
		for (int i = 0; i < globalObservers.length; i++) {
			nodeFactory.putConfiguration("observerList", globalObservers[i]);
		}
		
		nodeFactory.putConfiguration("energyManager", energyManagerFactory.get(index));
		nodeFactory.putConfiguration("layerList", airModuleFactory);
		nodeFactory.putConfiguration("layerList", physicalFactory.get(index));
		nodeFactory.putConfiguration("layerList", macFactory.get(index));		
		nodeFactory.putConfiguration("layerList", logLinkFactory.get(index));		
		nodeFactory.putConfiguration("layerList", networkFactory.get(index));
		nodeFactory.putConfiguration("layerList", operatingSystemFactory.get(index));
		nodeFactory.putConfiguration("layerList", applicationFactory.get(index));
		return nodeFactory;
	}

	/**
	 * Method to log the generation of nodes.
	 * 
	 * @param writer
	 *            Writer to log into.
	 */
	public static void log(Configuration configuration, ShoxLogger writer) {
		Class<? extends Layer> physical = configuration.getPhysicalLayerFactory().get(0).getInstanceClass();
		Class<? extends Layer> mac = configuration.getMacLayerFactory().get(0).getInstanceClass();
		Class<? extends Layer> logLink = configuration.getLogLinkLayerFactory().get(0).getInstanceClass();
		Class<? extends Layer> network = configuration.getNetworkLayerFactory().get(0).getInstanceClass();
		Class<? extends Layer> application = configuration.getApplicationLayerFactory().get(0).getInstanceClass();
		
		
		
		writer.logNodeSetup(configuration.getNodeCount(), physical, mac, logLink, network, application);

	}

}
