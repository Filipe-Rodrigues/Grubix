package br.ufla.dcc.PingPong.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import br.ufla.dcc.grubix.simulator.NodeId;

public class SingletonTestResult {

	public class SingleNodeDebugger {
		private int csNumber;
		private boolean isBackbone;

		public SingleNodeDebugger() {
			csNumber = 0;
		}

		public void setBackbone(boolean isBackbone) {
			this.isBackbone = isBackbone;
		}

		public void countCS() {
			csNumber++;

		}
	}

	private static final String CURRENT_DIR = System.getProperty("user.dir") + "/";

	private Map<NodeId, SingleNodeDebugger> nodeInfo;

	private double startSimulationTime;
	private double endSimulationTime;

	private double hopNumber;
	private double preambleNumber;
	private double csNumber;
	private double tData;
	private double tAck;
	private double tCTS;
	private double tPre;
	private double tCS;
	private int backboneCounter;

	private boolean enabled;

	private static volatile SingletonTestResult result = null;

	private SingletonTestResult() {
		nodeInfo = new TreeMap<>();
		startSimulationTime = 0;
		endSimulationTime = 0;
		enabled = false;
	}

	public static SingletonTestResult getInstance() {
		if (result == null) {
			result = new SingletonTestResult();
		}
		return result;
	}

	private void registerNode(NodeId id) {
		nodeInfo.put(id, new SingleNodeDebugger());
	}

	public void setNodeConfiguration(NodeId id, String configurationType) {
		if (enabled) {
			if (id != null) {
				if (!nodeInfo.containsKey(id)) {
					registerNode(id);
				}
				switch (configurationType) {
				case "SET_BACKBONE":
					nodeInfo.get(id).setBackbone(true);
					break;
				case "UNSET_BACKBONE":
					nodeInfo.get(id).setBackbone(false);
					break;
				case "COUNT_CS":
					nodeInfo.get(id).countCS();
					break;
				default:
					break;
				}
			}
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void loadInformation(double tData, double tAck, double tCTS, double tPre, double tCS) {
		this.tData = tData / 10000d;
		this.tAck = tAck / 10000d;
		this.tCTS = tCTS / 10000d;
		this.tPre = tPre / 10000d;
		this.tCS = tCS / 10000d;
	}

	public double getTotalEnergyConsumption() {
		return 0.06 * (hopNumber * (2 * tData + 2 * tAck + tCTS) + preambleNumber * (tPre + tCTS) + csNumber * tCS);
	}

	public void countBackboneNode() {
		backboneCounter++;
	}

	public void countHop() {
		if (enabled) {
			hopNumber++;
		}
	}

	public void countPreamble() {
		if (enabled) {
			preambleNumber++;
		}
	}

	public void countCS() {
		if (enabled) {
			csNumber++;
		}
	}

	public void setStartingTime(double time) {
		startSimulationTime = time;
	}

	public void setEndingTime(double time) {
		endSimulationTime = time;
	}

	public double getStartingTime() {
		return startSimulationTime;
	}

	public double getEndingTime() {
		return endSimulationTime;
	}

	public double getTime() {
		return endSimulationTime - startSimulationTime;
	}

	public void printAllStats() {
//		System.out.println("####### STATS #######");
//		System.out.println("Hops:\t" + hopNumber);
//		System.out.println("Preambles:\t" + preambleNumber);
//		System.out.println("Carrier Senses:\t" + csNumber);
//		System.out.println("tData:\t" + tData);
//		System.out.println("tAck:\t" + tAck);
//		System.out.println("tCTS:\t" + tCTS);
//		System.out.println("tPre:\t" + tPre);
//		System.out.println("tCS:\t" + tCS);
//
//		System.out.println("Backbone nodes: " + backboneCounter);
//		
//		for (Map.Entry<NodeId, SingleNodeDebugger> entry : nodeInfo.entrySet()) {
//			System.out.println("Node #" + entry.getKey() + " Preambles:\t" + entry.getValue().csNumber);
//		}
		
		writeResultsToFile();
	}

	private void writeResultsToFile() {
		if (startSimulationTime != endSimulationTime) {
			String filename = "DEBUG_STATS.txt";
			File file = new File(CURRENT_DIR + filename);
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write("####### STATS #######\n");
				writer.write("Sim time:\t" + (endSimulationTime - startSimulationTime) + "\n");
				writer.write("Hops:\t" + hopNumber + "\n");
				writer.write("Preambles:\t" + preambleNumber + "\n");
				writer.write("Carrier Senses:\t" + csNumber + "\n");
				writer.write("tData:\t" + tData + "\n");
				writer.write("tAck:\t" + tAck + "\n");
				writer.write("tCTS:\t" + tCTS + "\n");
				writer.write("tPre:\t" + tPre + "\n");
				writer.write("tCS:\t" + tCS + "\n");

				writer.write("Backbone nodes: " + backboneCounter + "\n");
				
				for (Map.Entry<NodeId, SingleNodeDebugger> entry : nodeInfo.entrySet()) {
					if (entry.getValue().isBackbone) {
						writer.write("BB Node #" + entry.getKey() + " CS count:\t" + entry.getValue().csNumber + "\n");
					} else {
						writer.write("Node #" + entry.getKey() + " CS count:\t" + entry.getValue().csNumber + "\n");
					}
				}
				writer.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Deu ruim ae men kkkk");
				e.printStackTrace();
			}
		}
	}

}
