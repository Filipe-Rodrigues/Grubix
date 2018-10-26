package br.ufla.dcc.PingPong.testing;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;


public class SingletonTestResult {

	private static final String CURRENT_DIR = System.getProperty("user.dir");
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
	
	private static SingletonTestResult result = null;
	
	private SingletonTestResult() {
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
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void loadInformation(double tData, double tAck, double tCTS, double tPre, double tCS) {
		this.tData = tData/10000d;
		this.tAck = tAck/10000d;
		this.tCTS = tCTS/10000d;
		this.tPre = tPre/10000d;
		this.tCS = tCS/10000d;
	}
	
	public double getTotalEnergyConsumption() {
		return 0.06*(hopNumber*(2*tData + 2*tAck + tCTS) + preambleNumber*(tPre + tCTS) + csNumber*tCS);
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
		System.out.println("####### STATS #######");
		System.out.println("Hops:\t" + hopNumber);
		System.out.println("Preambles:\t" + preambleNumber);
		System.out.println("Carrier Senses:\t" + csNumber);
		System.out.println("tData:\t" + tData);
		System.out.println("tAck:\t" + tAck);
		System.out.println("tCTS:\t" + tCTS);
		System.out.println("tPre:\t" + tPre);
		System.out.println("tCS:\t" + tCS);
		
		System.out.println("Backbone nodes: " + backboneCounter);
	}
	
	public void writeResults() {
		if (startSimulationTime != endSimulationTime) {
			String filename = "execution_time.txt";
			File file = new File (CURRENT_DIR + "/" + filename);
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write((endSimulationTime - startSimulationTime) + "");
				writer.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Deu ruim ae men kkkk");
				e.printStackTrace();
			}
		}
	}
	
}
