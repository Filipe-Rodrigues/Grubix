package br.ufla.dcc.PingPong;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.ufla.dcc.PingPong.PaxMac.PaxMacConfiguration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/** Guarda e processa informações que serão usadas para geras a estatísticas do fluxo da transmissão */
public class ToolsStatisticsTransmission {

	/** Destino final da mensagem, usada para distinguir os dados estatísticos de outras transmissões */
	public NodeId destinationId;
	
	/** Referência para as configurações */
	public PaxMacConfiguration paxConf;
	
	/** Flag para que informa se o destino recebeu o dado */
	public boolean destinationGetData = false;
	
	/** Flag que indica que terminou a transmissão  */
	public boolean finalizeTx = false;
	
	/** Tempo em que começou o envio da primeira mensagem **/
	public double startTime = -1;
	
	/** Tempo da chegada do ACK no nó que enviou DATA para o destino total **/
	public double endTime = -1;
	
	/** Lista que mantém informações referentes aos nós que compõem o caminho */
	List<ToolsStatisticsNode> nodesPath = new ArrayList<ToolsStatisticsNode>();

	/** Ferramentas diversas auxiliares */
 	ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();
	
	
 	
	/** Construtor */
	public ToolsStatisticsTransmission(NodeId destinationId, PaxMacConfiguration paxConf) {
		if (destinationId == null || destinationId.asInt() == -1) {
			System.out.println("[StatisticsError] Destino Inválido nas estatísticas: "+
					destinationId.asInt());
			SimulationManager.getInstance().cancelSimulation();
		}
		this.destinationId = destinationId;
		this.paxConf = paxConf;
	}
	
	
	/** Retorna nó na lista baseado no id. Se o nó não existe, cria um novo e o adiciona na lista */
	public ToolsStatisticsNode getPathNode(NodeId id) {
		for (ToolsStatisticsNode n : nodesPath) {
		    if (n.getNodeId().asInt() == id.asInt()) {
		        return n;
		    }
		}
		// Cria um novo nó na lista e o retorna
		ToolsStatisticsNode node = new ToolsStatisticsNode(id);
		nodesPath.add(node);
		return node;
	}
	
	
	/** Adicionar nó à lista de nós */
	public void addNodeSentMsg(NodeId id) {
		getPathNode(id);
	}
	
	
	/** Adicionar nó à lista de nós */
	public void addNode(NodeId id) {
		getPathNode(id);
	}
	
	
	/** Adicionar no nó, tempo que iniciou o envio de RTS */
	public void addRtsSentStartTime(NodeId id) {
		if (getPathNode(id).getRtsSentStartTime() < 0) {
			getPathNode(id).setRtsSentStartTime(SimulationManager.getInstance().getCurrentTime());
		}
	}
	
	
	/**  Adicionar no nó, tempo que terminou o envio do último  RTS */
	public void addRtsSentEndTime(NodeId id) {
		getPathNode(id).setRtsSentEndTime(SimulationManager.getInstance().getCurrentTime());
	}
	
	
	/** Adicionar tamanho do FCS no nó */
	public void addFcsSize(NodeId id, int size) {
		getPathNode(id).setFcsSize(size);
	}
	
	
	/** Adicionar o número de RTS enviados no nó */
	public void addRtsSent(NodeId id) {
		getPathNode(id).incRtsSent();
	}
	
	
	/** Adicionar o número de CTA-DATA enviados no nó */
	public void addCtsDataSent(NodeId id) {
		getPathNode(id).incCtsDataSent();
	}
	
	
	/** Adicionar o número de dados enviados no nó */
	public void addDataSent(NodeId id) {
		getPathNode(id).incDataSent();
	}
	

	/**  Adicionar no nó, quantas vezes teve que reiniciar o processo de envio de RTS */
	public void setRestartRts(NodeId id, int numRetry) {
		getPathNode(id).setRestartRtsProcesses(numRetry);
	}
	

	/**  Adicionar no nó, quantas vezes estava esperando ACK */
	public void addAckWaiting(NodeId id) {
		getPathNode(id).incAckWaiting();
	}
	
	
	/**  Adicionar no nó, quantas vezes estava esperando dado */
	public void addCtsDataWaiting(NodeId id) {
		getPathNode(id).incCtsDataWaiting();
	}
	
	
	/**  Adicionar no nó, quantas vezes estava sondando o canal, antes de enviar CTS-DATA */
	public void addCsLongCtsDataBusy(NodeId id) {
		getPathNode(id).incCsLongCtsDataBusy();
	}
	
	
	/** Define o tempo do início do envio da primeira mensagem */
	public void setStartTime() {
		if (this.startTime < 0) {
			this.startTime = SimulationManager.getInstance().getCurrentTime();
		}
	}
	
	
	/** Define o tempo em que o penúltimo nó recebe um ACK */
	public void setEndTime() {
		if (this.endTime < 0) {
			this.endTime = SimulationManager.getInstance().getCurrentTime();
		}
	}

	
	/** Obter total de RTS enviados */
	public int getTotalRtsSent() {
		int total = 0;
		for (ToolsStatisticsNode n : nodesPath) {
			total += n.getRtsSent();
		}
		return total;
	}
	
	
	/** Obter total de CTS-DATA enviados */
	public int getTotalCtsDataSent() {
		int total = 0;
		for (ToolsStatisticsNode n : nodesPath) {
			total += n.getCtsDataSent();
		}
		return total;
	}
	
	
	/** Obter total de CS-LONG CTS-DATA ocupados */
	public int getTotalCsLongCtsDataBusy() {
		int total = 0;
		for (ToolsStatisticsNode n : nodesPath) {
			total += n.getCsLongCtsDataBusy();
		}
		return total;
	}
	
	
	/** Obter total de espera CTS-DATA realizados */
	public int getTotalCtsDataWaiting() {
		int total = 0;
		for (ToolsStatisticsNode n : nodesPath) {
			total += n.getCtsDataWaiting();
		}
		return total;
	}
	
	
	/** Obter o total de reinicio de RTS na fluxo de transmissão */
	public double getTotalRestartRts() {
		int total = 0;
		for (ToolsStatisticsNode n : nodesPath) {
			total += n.getRestartRtsProcesses();
		}
		return total;
	}
	
	
	/** Obter tempo total da transmissão atual até o destino */
	public double getTotalTxTime() {
		if (startTime < 0 || endTime < 0) {
			return 0;
		}
		return endTime-startTime;
	}
	
	
	/** Obter distância entre os nós  */
	public double nodesDistance() {
		Node destination = SimulationManager.getInstance().queryNodeById(destinationId);
		Node source = SimulationManager.getInstance().queryNodeById(NodeId.get(destination.getId().asInt()-1));

		return source.getPosition().getDistance(destination.getPosition());
	}
	
	
	/** Obter string da lista */
	public String getListView(String attr) {
		String s="";
		for (int i=0; i<nodesPath.size(); i++) {
			String tag = " ["+(i+1)+"]";
			switch (attr) {
			case "ID":
				s += tag + nodesPath.get(i).getNodeId();
				break;
			case "FCS":
				s += tag + nodesPath.get(i).getFcsSize();
				break;
			case "TIME_RTS":
				s += tag + (nodesPath.get(i).getRtsSentEndTime() - nodesPath.get(i).getRtsSentStartTime())+"(steps)";
				break;
			case "RESTART_RTS":
				if (nodesPath.get(i).getRestartRtsProcesses() != 0)
					s += tag + nodesPath.get(i).getRestartRtsProcesses();
				break;
			case "WAITING_ACK":
				if (nodesPath.get(i).getAckWaiting() > 1)
					s += tag + (nodesPath.get(i).getAckWaiting() - 1);
				break;
			case "WAITING_CTS_DATA":
				if (nodesPath.get(i).getCtsDataWaiting() > 1)
					s += tag + (nodesPath.get(i).getCtsDataWaiting() - 1);
				break;
			case "CSLONG_CTS_DATA":
				if (nodesPath.get(i).getCsLongCtsDataBusy() > 1)
					s += tag + (nodesPath.get(i).getCsLongCtsDataBusy() - 1);
				break;
			case "SENT_CTS_DATA":
				if (nodesPath.get(i).getCtsDataSent() > 1)
					s += tag + (nodesPath.get(i).getCtsDataSent() - 1);
				break;
			case "SENT_DATA":
				if (nodesPath.get(i).getDataSent() > 1)
					s += tag + (nodesPath.get(i).getDataSent() - 1);
				break;
			case "SENT_RTS":
				s += tag + nodesPath.get(i).getRtsSent();
				break;
			default:
			}
		}
		return s;
	}
	
	
	/** Imprimir estatísticas detalhada */
	public void printStatistics() {

		// Resultados geral transmissão
		System.out.println(
			"TempoUtil(steps)="        +getTotalTxTime()+
			" TempoUtil(s)="           +misc.stepsToSeconds(getTotalTxTime())+
			" TamanhoCaminho="         +nodesPath.size()+
			" Saltos="                 +(nodesPath.size()-1)+
			" RtsEnviados="            +getTotalRtsSent()+
			" DadoChegouAoDestino="    +destinationGetData+
			" TempoPorDuracaoDeData="  +(getTotalTxTime()/paxConf.getStepsDATA())
		);
		
		// Resultados por nó
		System.out.println("\nPath: "                         +getListView("ID"));
		System.out.println("RTS enviados: "                   +getListView("SENT_RTS"));
		//System.out.println("Tempo de envio do RTS: "        +getListView("TIME_RTS"));
		System.out.println("Reinicio sequência RTS: "         +getListView("RESTART_RTS"));
		System.out.println("Esperas por ACK excedidas: "      +getListView("WAITING_ACK"));
		System.out.println("Esperas por CTS-DATA excedidas: " +getListView("WAITING_CTS_DATA"));
		System.out.println("CSLong CTS-DATA excedidos: "      +getListView("CSLONG_CTS_DATA"));
		System.out.println("Envios de dados excedidos: "      +getListView("SENT_DATA"));
		System.out.println("Envios de CTS-DATA excedidos: "   +getListView("SENT_CTS_DATA"));
	}
	
	
	/** Imprimir estatísticas resumidas */
	public void printShortStatistics(int id) {

		System.out.print(" >>>fluxo="   +misc.csv(id)+
			" tempo_fluxo_seg="         +misc.csvZeroToNull(misc.csv(misc.stepsToSeconds(getTotalTxTime())))+
			" data_chegou_destino="     +misc.csv(misc.boolToInt(destinationGetData))+
			" reinicio_envio_rts="      +misc.csv(getTotalRestartRts())+
			" distancia="               +misc.csv(nodesDistance())+ 
			" saltos="                  +misc.csv((nodesPath.size()-1))+
			" total_ctsdata_enviados="  +misc.csv(getTotalCtsDataSent())+
			" total_cslong_ocupados="   +misc.csv(getTotalCsLongCtsDataBusy())+
			" total_espera_ctsdata="    +misc.csv(getTotalCtsDataWaiting())+
			" total_rts_enviados="      +misc.csv(getTotalRtsSent())
		);
	}
}
