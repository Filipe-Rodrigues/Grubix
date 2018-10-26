package br.ufla.dcc.PingPong;

import br.ufla.dcc.grubix.simulator.NodeId;

public class ToolsStatisticsNode {

	/** Id do nó */
	private NodeId nodeId;
	
	/** Tamanho do FCS, quando o nó enviou o RTS */
	private int fcsSize;

	/** Quantas vezes sondou o canal antes de enviar CTS-DATA mas estava ocupado */
	private int csLongCtsDataBusy = 0;
	
	/** Quantas vezes o nó aguardou o CTS-DATA */
	private int ctsDataWaiting = 0;
	
	/** Quantas vezes estava esperando ACK */
	private int ackWaiting = 0;
	
	/** CTS-DATA enviados pelo nó. Quantas vezes estava aguardando o dado */
	private int ctsDataSent = 0;
	
	/** Dados enviados pelo nó */
	private int dataSent = 0;
	
	/** Preâmbulos enviados pelo nó */
	private int rtsSent;
	
	/** Quantas vezes teve que reiniciar a sequência de RTS */
	private int restartRtsProcesses = 0;
	
	/** Tempo que o nó começou envio do primeiro RTS */
	private double rtsSentStartTime = -1;
	
	/** Tempo que começou envio do RTS */
	private double rtsSentEndTime = -1;
	
	
	/** Construtor */
	public ToolsStatisticsNode(NodeId nodeId) {
		this.nodeId = nodeId;
	}
	
	
	/** Sets e Gets */
	public NodeId getNodeId() {
		return nodeId;
	}

	public int getFcsSize() {
		return fcsSize;
	}

	public void setFcsSize(int fcsSize) {
		this.fcsSize += fcsSize;
	}

	public int getCsLongCtsDataBusy() {
		return csLongCtsDataBusy;
	}

	public void incCsLongCtsDataBusy() {
		this.csLongCtsDataBusy++;
	}

	public int getCtsDataWaiting() {
		return ctsDataWaiting;
	}

	public void incCtsDataWaiting() {
		this.ctsDataWaiting++;
	}

	public int getAckWaiting() {
		return ackWaiting;
	}

	public void incAckWaiting() {
		this.ackWaiting++;
	}

	public int getCtsDataSent() {
		return ctsDataSent;
	}

	public void incCtsDataSent() {
		this.ctsDataSent++;
	}

	public int getDataSent() {
		return dataSent;
	}

	public void incDataSent() {
		this.dataSent++;
	}

	public int getRtsSent() {
		return rtsSent;
	}

	public void incRtsSent() {
		this.rtsSent++;
	}

	public int getRestartRtsProcesses() {
		return restartRtsProcesses;
	}

	public void setRestartRtsProcesses(int restartRtsProcesses) {
		this.restartRtsProcesses = restartRtsProcesses;
	}

	public double getRtsSentStartTime() {
		return rtsSentStartTime;
	}

	public void setRtsSentStartTime(double rtsSentStartTime) {
		this.rtsSentStartTime = rtsSentStartTime;
	}

	public double getRtsSentEndTime() {
		return rtsSentEndTime;
	}

	public void setRtsSentEndTime(double rtsSentEndTime) {
		this.rtsSentEndTime = rtsSentEndTime;
	}

}
