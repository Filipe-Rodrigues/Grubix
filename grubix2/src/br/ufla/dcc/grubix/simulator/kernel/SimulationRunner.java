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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import br.ufla.dcc.grubix.debug.compactlogging.ShoxLogger;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.node.Node;



/**
 * This class is used by the ShoX GUI to run a simulation as a thread. The GUI has to
 * run the simulation concurrently, since otherwise, the GUI would be blocked during
 * simulation and could not be updated.
 * @author jlsx
 */
public class SimulationRunner extends Thread {

	/** The frame where the simulation progress is displayed. */
	private final JFrame progressFrame;
	
	/** The text area which displays the simulation progress messages. */
	private final JTextArea simMessageArea;
	
	/** The progress bar indicating the current simulation progress. */
	private final JProgressBar simProgress;
	
	/** The real-world start time of the simulation. */
	private long simulationStartRealTime;
	
	/**
	 * This button can be pressed after the simulation to proceed with Monitor/Statistics.
	 */
	private final JButton simEndButton;
	
	/**
	 * This button can be pressed to cancel a running simulation.
	 */
	private final JButton simCancelButton;
	
	/** 
	 * Reference to the actual simulation manager that is executing the simulation.
	 */
	private SimulationManager sim;
	
	/**
	 * the full name of the configuration file.
	 */
	private final String configurationFile;

	/**
	 * Constructs a runner thread.
	 * @param progressFrame The frame that displays the simulation progress
	 * @param simMessageArea The area where simulation events can be displayed
	 * @param simProgress The progress bar
	 * @param simEndButton The button used to proceed after the simulation is finished
	 * @param simCancelButton The button used to cancel a running simulation
	 * @param configurationFile The name of the temporary configuration file, which will be
	 *                                         used for the simulation.
	 */
	public SimulationRunner(JFrame progressFrame, JTextArea simMessageArea, JProgressBar simProgress,
			JButton simEndButton, JButton simCancelButton, String configurationFile) {
		this.progressFrame = progressFrame;
		this.simMessageArea = simMessageArea;
		this.simProgress = simProgress;
		this.simEndButton = simEndButton;
		this.simCancelButton = simCancelButton;
		this.configurationFile = configurationFile;
	}
	
	/**
	 * Called to start the thread.
	 */
	@Override
	public void run() {
		try {
			Configuration configuration = Configuration.readConfig(configurationFile);
			ShoxLogger writer = null;
			if (configuration.isLogging()) {
				try {
					
					writer = configuration.getHistoryLogger();
					
					writer.initLogging(configuration.getNameHistoryLogfile(), 
										configuration.getNameStatisticsLogfile(), configuration.getSimulatorId());
					writer.startConfigurationPart();
					configuration.logConfig(writer);
				} catch (IOException e) {
					this.simMessageArea.append("Setup of ShoXLogger failed! Quitting!\n");
					return;
				}
			}
			
			//create empty map to fill later because NodeGenerator needs a SimulationManager reference to be able
			//to create AirModules for Nodes.
			SortedMap<NodeId, Node> allNodes = new TreeMap<NodeId, Node>();
			//get SimulationManager
			this.sim = new SimulationManager(writer, allNodes);
			this.sim.setRunner(this);
			NodeGenerator.generateNodes(configuration, allNodes);
			if (configuration.isLogging() && (writer != null)) {
				NodeGenerator.log(configuration, writer);
				//log all starting positions
				writer.logNodePlacement(allNodes.values());
				writer.endConfigurationPart();
			}
			
			this.simProgress.setValue(0);
			this.simulationStartRealTime = System.currentTimeMillis();
			this.simMessageArea.append("Network Simulator is starting the SIMULATION...\n");
			this.sim.runSimulation();
			if (configuration.isLogging() && (writer != null)) {
				writer.finishLogging();
			}
			
			this.simMessageArea.append("Network Simulator is ending the SIMULATION. Good Bye!\n");
		} catch (Exception e) {
			long seed = Configuration.getInstance().getRandomGenerator().getSeed();
			this.simMessageArea.append("An exception occured during simulation: " + e + "seed: " + seed + "\n");
			this.simMessageArea.append("Network Simulator is aborting the SIMULATION. Good Bye!\n");
			e.printStackTrace();
		}
		this.simProgress.setValue(100);
		this.simCancelButton.setEnabled(false);
		this.simEndButton.setEnabled(true);
		this.progressFrame.setTitle("Simulation finished.");
	}
	
	/**
	 * Set the progress of the ongoing simulation in percent.
	 * @param value The simulation progress in percent (value between 0 and 100)
	 */
	public void setProgress(int value) {
		if ((value >= 0) || (value <= 100)) {
			if (value > 0) {
				long realtime = System.currentTimeMillis();
				double secondsSoFar = (realtime - this.simulationStartRealTime) / 1000.0;
				int remainingSeconds = (int) Math.ceil(secondsSoFar * 100 / value);
				this.simProgress.setString(value + " % - " + this.getTimeString(remainingSeconds) + " remaining");
			}
			this.simProgress.setValue(value);
		}
	}
	
	/**
	 * Called to cancel the current simulation.
	 */
	public void abort() {
		this.sim.cancelSimulation();
	}
	
	/**
	 * Converts a number of seconds into a string representation with days, hours and minutes. 
	 * @param remainingSeconds The number of seconds to be converted to a string
	 * @return A string corresponding to the number of seconds
	 */
	private String getTimeString(int remainingSeconds) {
		int days = remainingSeconds / 86400;
		int hours = (remainingSeconds % 86400) / 3600;
		int minutes = ((remainingSeconds % 86400) % 3600) / 60;
		//int seconds = ((remainingSeconds % 86400) % 3600) % 60;
		StringBuffer sb = new StringBuffer();
		if (days > 0) {
			sb.append(days).append("d, ");
		}		
		if (hours > 0) {
			sb.append(hours).append("h, ");
		}
		sb.append(minutes).append("m ");

		//sb.append(seconds).append("s");
		return sb.toString();
	}
}
