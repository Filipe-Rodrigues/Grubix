package br.ufla.dcc.PingPong.XMac.Stats;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import br.ufla.dcc.PingPong.ToolsStatisticsSimulation;
import br.ufla.dcc.PingPong.ToolsStatisticsTransmission;
import br.ufla.dcc.PingPong.XMac.XMacConfiguration;
import br.ufla.dcc.PingPong.XMac.XMacPacket;
import br.ufla.dcc.PingPong.node.AppPacket;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;

public class SimulationBK {
	
	static private SimulationBK instance = null;
	
	/** Referência para as configurações da camada MAC */
	public XMacConfiguration xConf = null;
	
	/** Total de transmissões que serão realizadas AKA numero de saltos*/
	public int numTransmissions;
	
	public int idTransmission = -1;
	
	public int finalNode = 0;
	
	public int countAlive = 0;
	
	/** Lista de objetos para os dados estatísticos do fluxo de transmissão */
	public List<TransmissionBK> transmissions = new ArrayList<TransmissionBK>();
	
	/** Se já imprimiu as estatísticas */
	public boolean printedStatistics = false;
	
	/** Construtor */
	static public SimulationBK getInstance() {
		if (instance == null) {
			instance = new SimulationBK();
		}
		return instance;
	}
	
	/** Define o total de transmissões que serão realizadas na simulação */
	public void setTransmission(NodeId senderId, NodeId destinationId) {
		transmissions.add(new TransmissionBK(senderId,destinationId));
		numTransmissions++;
		idTransmission++;
	}
	
	public int getIdTransmission(){
		return idTransmission;
	}
		
	public void getTransmissions() throws FileNotFoundException, UnsupportedEncodingException{
		//System.out.println("Transmissão\n");
		PrintWriter writer = new PrintWriter("log.txt", "UTF-8");
		writer.println("TRANSMISSOES "+transmissions.size());
		for(int i = 0; i < transmissions.size();++i){
			writer.println("TRANMISSAO SINGULAR ");
			writer.println("SENDER "+transmissions.get(i).getSenderId());
			writer.println("RECEIVER "+transmissions.get(i).getDestinationId());
			//writer.println("FINAL "+transmissions.get(i).getDestinationFinalId());
			//writer.println("NUM RTS "+transmissions.get(i).getNumRtsTrans());
			//writer.println("START TIME "+transmissions.get(i).getStartTx());
			//writer.println("END TIME "+transmissions.get(i).getEndTx());
			writer.println("NUM RTS "+transmissions.get(i).getNumRtsTrans());
			writer.println("ALIVE "+transmissions.get(i).isEndTransmission());
			writer.println("TRANMISSAO SINGULAR \n");
		}
		//writer.println("Numero "+numTransmissions);
		writer.close();
	}
	
	
	public void setRtsTransmission(NodeId senderId,int idTransmission){
		//int i  = countAlive;
		int i = 0;
		//while(i < transmissions.size()){
			if(transmissions.get(idTransmission).getSenderId().asInt() == senderId.asInt() &&
					!transmissions.get(idTransmission).isEndTransmission()){
				transmissions.get(idTransmission).setNumRtsTrans();
				System.out.println(transmissions.get(idTransmission).getNumRtsTrans());
				
			}
		//	++i;
		
		//countAlive = i;
	}
	
	public void getEndTrans(XMacPacket packet){
		System.out.println("RECEIVER "+packet.getReceiver());		
	}
	
	public void setFinalNode(int finalNode){
		this.finalNode = finalNode;
	}
	
	public void getEndTransTotal(){
			System.out.println("RESULTADO TOTAL "+ 
			(transmissions.get(transmissions.size()-1).getEndTx() - transmissions.get(0).getStartTx())
			);
	}
}
