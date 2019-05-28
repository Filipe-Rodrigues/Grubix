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

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import br.ufla.dcc.grubix.debug.compactlogging.ShoxLogger;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.movement.FromFileStartPositions;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * Simple startup class for the whole SIMULATION. Initializes all needed stuff
 * like: - LOGGER CONFIGURATION - Configuration.readConfig - all objects needed
 * for SimulationManager
 * 
 * @author Andreas Kumlehn
 */
public final class Simulator {

	/**
	 * Logger of the class Shox.
	 */
	private static final Logger LOGGER = Logger.getLogger(Simulator.class);

	/**
	 * Main method of the whole SIMULATION.
	 * 
	 * @param args
	 *            Console arguments. The XML config file name is expected as
	 *            args[0].
	 */
	public static void main(String[] args) {
		try {
			String log4jFile = "log4j.properties";
			if (args.length >= 4) {
				System.out.println("Using logging " + args[3]);
				log4jFile = args[3];
			}
			PropertyConfigurator.configure(log4jFile);
			LOGGER.info("Network Simulator is configuring...");

			if (args.length == 0) {
				LOGGER.fatal("No configuration file was specified.");
				return;
			}

			Configuration configuration = Configuration.readConfig(args[0]);
			
			ShoxLogger writer = null;

			if (configuration.isLogging()) {
				try {
					writer = configuration.getHistoryLogger();

					writer.initLogging(configuration.getNameHistoryLogfile(),
							configuration.getNameStatisticsLogfile(),
							configuration.getSimulatorId());

					writer.startConfigurationPart();
					configuration.logConfig(writer);
				} catch (IOException e) {
					LOGGER.warn("Setup of XML Writer failed! Quitting!");
					return;
				}

			}

			BackboneConfigurationManager.startup(Boolean.parseBoolean(args[2]));
			
			// create empty map to fill later because NodeGenerator needs a
			// SimulationManager reference to be able
			// to create AirModules for Nodes.
			SortedMap<NodeId, Node> allNodes = getNodes(configuration.getPositionGenerator() instanceof FromFileStartPositions);
			// get SimulationManager

			SimulationManager sim = new SimulationManager(writer, allNodes);
			NodeGenerator.generateNodes(configuration, allNodes);
			if (configuration.isLogging() && (writer != null)) {
				NodeGenerator.log(configuration, writer);

				// log all starting positions
				writer.logNodePlacement(allNodes.values());

				writer.endConfigurationPart();
			}
			LOGGER.info("Network Simulator is starting the SIMULATION...");
			try {
				sim.runSimulation();
			} catch (SimulationFailedException e) {
				LOGGER.fatal("Simulation failed", e);
				e.printStackTrace();
			}
			if (configuration.isLogging() && (writer != null)) {
				writer.finishLogging();
			}
			
			long seed = Configuration.getInstance().getRandomGenerator()
					.getSeed();

			LOGGER.info("Network Simulator is ending the SIMULATION. Good Bye! (seed: "
					+ seed + ")");
			
			// Salva a configuração dos backbones. ÚTIL APENAS AO PROJETO EXMac!!!!
			BackboneConfigurationManager.close(Boolean.parseBoolean(args[1]));
			
			//System.err.println("SIMULAÇÃO TERMINADA COM SUCESSO!");
		} catch (ConfigurationException e) {
			LOGGER.error("Network Simlator has no valid configuration", e);
			e.printStackTrace();
		}
	}
	
	private static SortedMap<NodeId, Node> getNodes(boolean loadedFromFile) {
		return new TreeMap<NodeId, Node>();
	}
	
	private static void writeNodesToFile() {
		
	}
}
