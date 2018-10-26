package br.ufla.dcc.PingPong.XMac.Stats;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.ufla.dcc.PingPong.ToolsStatisticsSimulation;
import br.ufla.dcc.PingPong.ToolsStatisticsTransmission;
import br.ufla.dcc.PingPong.XMac.XMacConfiguration;
import br.ufla.dcc.PingPong.XMac.XMacPacket;
import br.ufla.dcc.PingPong.node.AppPacket;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

public class Simulation {
	
	static private Simulation instance = null;
	
	/** Referência para as configurações da camada MAC */
	public XMacConfiguration xConf = null;
	
	/** Contador de transmissões*/
	private  int numTransmissions = 0;
	
	private int finalNode = 0;
	
	public int idTransmission = 0;
	
	public int idTransmissionMaster = 0;
	
	private int newData = 0;
	
	private List<Transmission> transmissions = new ArrayList<Transmission>();	
	
	/** Construtor */
	static public Simulation getInstance() {
		if (instance == null) {
			instance = new Simulation();
		}
		return instance;
	}
	
	/** Define o total de transmissões que serão realizadas na simulação */
	public void setTransmission(NodeId senderId, NodeId destinationId) {
		idTransmission++;
		transmissions.add(new Transmission(senderId,destinationId,idTransmission));
		if(senderId.asInt() == 1)
			newData++;
		//transmissions.get(idTransmission-1).setIdTransmissionMaster(idTransmissionMaster);
		//numTransmissions++;		
		
		//transmissions.add(count, new Transmission(senderId.asInt(),destinationId.asInt(),count));
		//++count;
	}
	
	public void getTransmissions() throws FileNotFoundException, UnsupportedEncodingException{
		//System.out.println("Transmissão\n");
		PrintWriter writer = new PrintWriter("log.txt", "UTF-8");
		writer.println("TRANSMISSOES "+transmissions.size());
		writer.println("TRANSMISSOES ID TOTAL "+idTransmission);
		for(int i = 0; i < transmissions.size();++i){
			writer.println("TRANMISSAO SINGULAR ");
			writer.println("SENDER "+transmissions.get(i).getSenderId());
			writer.println("RECEIVER "+transmissions.get(i).getDestinationId());
			writer.println("FINAL "+transmissions.get(i).getDestinationFinalId());
			writer.println("NUM RTS "+transmissions.get(i).getNumRtsTrans());
			writer.println("START TIME "+transmissions.get(i).getStartTx());
			writer.println("END TIME "+transmissions.get(i).getEndTx());
			writer.println("NUM RTS "+transmissions.get(i).getNumRtsTrans());
			writer.println("TEMPO DE TRANSMISSAO "+transmissions.get(i).getTimeTransmission());
			//writer.println("ALIVE "+transmissions.get(i).isEndTransmission());
			//writer.println("ID TRANSMISSAO "+transmissions.get(i).getIdTransmission());
			//writer.println("ID TRANSMISSAO PAI "+transmissions.get(i).getIdTransmissionMaster());
			writer.println("TRANMISSAO SINGULAR \n");
		}
		//writer.println("Numero "+numTransmissions);
		writer.close();
	}
		
	public void endTransmission(XMacPacket packet){
		transmissions.get(idTransmission-1).setEndTx();
	}
	
	
	public double txAverageTime(){
		double totalTime = 0;
		int count = 0;
		for(int i = 0; i<transmissions.size();++i){
			if(transmissions.get(i).getEndTx() > 0 && transmissions.get(i).getStartTx() > 0){
				count++;
				totalTime += transmissions.get(i).getEndTx() - transmissions.get(i).getStartTx();
			}
		}
		
		if(count == 0){
			return 0;
		}else{
			return stepsToSeconds(totalTime/count);
		}
	}
	
	/** Obter tempo útil total da simulação */
	public double getTotalSimulationTime() {
		double start = -1;
		double end = -1;
		for(int i  = 0; i<transmissions.size();++i){
			if(transmissions.get(i).getStartTx() > 0 && transmissions.get(i).getEndTx()>0){
				if (start < 0 || start > transmissions.get(i).getStartTx()) {
					start = transmissions.get(i).getStartTx();
				}
				if (end < 0 || end < transmissions.get(i).getEndTx()) {
					end = transmissions.get(i).getEndTx();
				}
			}
		}
		return end-start;
	}
	
	
	public int numTrans(){
		int num = 0;
		int i = 0;
		while(i<transmissions.size()){
			if(transmissions.get(i).getDestinationId().asInt() == transmissions.get(i).getDestinationFinalId()){
				++num;
			}
			++i;
		}
		return num;
		//System.out.println(num);
	}
	
	public int numRts(){
		int i = 0 ;
		int num = 0;
		while(i<transmissions.size()){
			num += transmissions.get(i).getNumRtsTrans();
			i++;
		}
		return num;
	}
	
	public void printStatistics(){
		System.out.println("\n--------------------------------");
		String sep=";"; 
		System.out.print(
				" fluxos="                   +csv(transmissions.size())+
				" dados="					+csv(newData)+
				" entregas="				+csv(numTrans())+
				" tempo_medio_seg="				+csv(txAverageTime())+
				" numero_rts="					+csv(numRts())+
				" tempo_simulacao_seg="      +csvZeroToNull(csv(stepsToSeconds(getTotalSimulationTime())))+
				" tempo_steps="				+csv(getTotalSimulationTime())
				);
	}
	
	public void setRtsTransmission(NodeId senderId){
		if(transmissions.get(idTransmission-1).getSenderId().asInt() == senderId.asInt()){
			transmissions.get(idTransmission-1).setNumRtsTrans();		
			//System.out.println(transmissions.get(idTransmission-1).getNumRtsTrans());
		}
	}
	
	public void endTrans(XMacPacket packet){
		
	}
		//if(transmissions.get(idTransmission).getDestinationFinalId() == packet.getReceiver().asInt()){
			//transmissions.get(idTransmission-1).setEndTransmission(true);
			//transmissions.get(idTransmission-1).setIdTransmissionMaster(idTransmissionMaster);
			//idTransmissionMaster++;
		//}
		
	
	
	public int getIdTransmission(){
		return idTransmission;
	}
	
	public void setFinalNode(NodeId finalNode){
		vGrubix(finalNode, "Destino", "0");
	}
	
	/** Retorna um valor decimal com o formato pt-BR */
	public String ptBr(double val) {
		NumberFormat br = NumberFormat.getNumberInstance(new Locale("pt","BR"));
		br.setMaximumFractionDigits(5);
		br.setRoundingMode (RoundingMode.FLOOR);
		return br.format(val);
	}
	
	
	/** Retorna um valor decimal com o formato pt-BR */
	public String ptBr(int val) {
		NumberFormat br = NumberFormat.getNumberInstance(new Locale("pt","BR"));
		return br.format(val);
	}
	
	
	/** Retorna valor para o formato csv para local pt-BR */
	public String csv(double val) {
		String sep=";";
		return sep + ptBr(val) + sep;
	}
	
	
	/** Retorna valor para o formato csv para local pt-BR */
	public String csv(int val) {
		String sep=";";
		return sep + ptBr(val) + sep;
	}
	
	
	/** Retorna valor para o formato csv para local pt-BR */
	public String csv(String val) {
		String sep=";";
		return sep + val + sep;
	}
	
	
	/** Retorna valor nulo se zero */
	public String csvZeroToNull(String val) {
		String sep=";";
		if (val == sep + 0 + sep) {
			return sep + "" + sep;
		}
		return val;
	}
	
	public void vGrubix(NodeId node, String text, String color) {
		SimulationManager.logNodeState(node, text, "int", String.valueOf(0));
	}
	
	 /** Converte steps em segundos */
	public double stepsToSeconds(double steps) {
		return Configuration.getInstance().getSeconds(steps);
	}
	

}

