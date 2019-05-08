package br.ufla.dcc.PingPong.BackboneXMac;

import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

/**
 *  Configuração da MAC;
 *
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 04/07/2016
 */

public class XMacConfiguration {

	/** Se é para habilitar o uso de ACK */
	private boolean ackEnable;
	
	private double lengthCycle;
	
	/** Tempo em steps em que o nó ficará em SLEEP */
	private double stepsSleep;
	
	/** Tempo em steps para a escuta do canal na espera de um RTS */
	private double stepsCS;
	
	/** Tempo em steps para enviar uma mensagem de Preâmbulo (RTS) */
	private double stepsRTS;
	
	/** Tempo em steps para enviar uma mensagem earlyACK (CTS) */
	private double stepsCTS;
	
	/** Tempo em steps para enviar uma mensagem de ACK */
	private double stepsACK;
	
	/** Tempo em steps para enviar uma mensagem de DATA */
	private double stepsDATA;
	
	/** Tempo em steps a mais como margem de segurança para garantir o fim de uma transmissão */
	private double stepsEndTx;
	
	/** Número máximo de preâmbulos a serem enviado na tentativa de se alcançar nó vizinho */
	private int maxPreambles;
	
	/** Máximo de tentativas de reenvio de uma mensagem  */
	private int maxSendRetries = XMacConstants.NUM_MAX_RETRANSMISSIONS;
	
	/** Padrão da intensidade de sinal a ser utilizada no envio do de um pacote */
	private double signalStrength = XMacConstants.SS_DEFAULT;
	
	/** Tamanho do dado em bits */
	private int lengthDATA = XMacConstants.DATA_LENGTH;
	
	/** Tamanho do RTS em bits */
	private int lengthRTS = XMacConstants.RTS_LENGTH;
	
	/** Tamanho do RTS em bits */
	private int lengthACK = XMacConstants.ACK_LENGTH;
	
	/** Tamanho do CTS em bist */
	private int lengthCTS = XMacConstants.CTS_LENGTH;
	
	/** Valor máximo de uma sequência, para identificador de mensagem ou para preâmbulos. Deve ser sempre 2^N-1 */
	private int maxSequence = XMacConstants.MAX_SEQUENCE;
	
	/** Construtor */
	public XMacConfiguration(double lengthCycle, boolean ackEnable) {
		double stepsPerSecond = Configuration.getInstance().getStepsPerSecond();
		
		this.ackEnable = ackEnable;
		this.lengthCycle = lengthCycle;
		
		/* Os steps são a granularidade da simulação definida em "stepspersecond" em  applicatio.xml
		stepsPBit define o número de steps necessários para enviar 1 bit */
		double stepsPBit = (double)Configuration.getInstance().getStepsPerSecond()/XMacConstants.BPS;
		
		// Tempo em steps para enviar cada tipo de pacote
		stepsRTS = stepsPBit * lengthRTS;
		stepsCTS = stepsPBit * lengthCTS;
		stepsACK = stepsPBit * lengthACK;
		stepsDATA= stepsPBit * lengthDATA;
		
		/* Tempo em steps para escutar o canal em busca de preâmbulos. Como no simulador camada física 
		 * manda o preâmbulo de uma única vez, não tem como acordar no meio de um preâmbulo como num rádio 
		 * real. Por isso é necessário esperar um tempo de RTS completo mais um tempo de CTS e mais um pouco 
		 * (usado outro tempo de RTS)
		 */
		stepsSleep = 5;
		
		// Tempo em steps em que o nó ficará em SLEEP (steps do ciclo - steps escuta ociosa)
		stepsCS = (lengthCycle * Configuration.getInstance().getStepsPerSecond()) - stepsSleep;

		// Tempo de margem de segurança adicional para garantir que a transmissão finalizou
		stepsEndTx = stepsACK/2;
		
		/* O máximo de preâmbulos necessário para até o nó acordar (stepsSleep/(stepsRTS + stepsCTS)
		 * É acrescentado um porque a divisão pode não ser exata 1,1 = 2 preâmbulos. Também é acrescentado
		 * outro preâmbulo (analisar para ver motivo)
		 */
		maxPreambles = 1;
		SingletonTestResult.getInstance().loadInformation(stepsDATA, stepsACK, stepsCTS, stepsRTS, stepsCS);
	}

	/** Normaliza a quantidade de tempo em steps em que o nó ficará em SLEEP*/
	public void normalizeStepsSleep() {
		double stepsPerSecond = Configuration.getInstance().getStepsPerSecond();
		stepsCS = 2*stepsRTS + stepsCTS;
		stepsSleep = (lengthCycle * stepsPerSecond) - stepsCS;
		SingletonTestResult.getInstance().loadInformation(stepsDATA, stepsACK, stepsCTS, stepsRTS, stepsCS);
	}
	
	public void normalizeMaxPreambles(boolean isBackbone) {
		double stepsPerSecond = Configuration.getInstance().getStepsPerSecond();
		maxPreambles = (int) ((lengthCycle*stepsPerSecond*((isBackbone) ? (10) : (1)) - stepsRTS) / (stepsRTS + stepsCTS) + 1);
	}
	
	/** Tempo em steps do ciclo de trabalho*/
	public double getStepsCycle() {
		return stepsSleep + stepsCS;
	}
	
	/** Gets e sets */
	public double getStepsSleep() {
		return stepsSleep;
	}

	public double getStepsCS() {
		return stepsCS;
	}

	public double getStepsRTS() {
		return stepsRTS;
	}

	public double getStepsCTS() {
		return stepsCTS;
	}

	public double getStepsDATA() {
		return stepsDATA;
	}

	public int getMaxPreambles() {
		return maxPreambles;
	}

	public int getMaxSendRetries() {
		return maxSendRetries;
	}

	public double getSignalStrength() {
		return signalStrength;
	}
	
	public void setSignalStrength(double signalStrength) {
		this.signalStrength = signalStrength;
	}

	public int getLengthDATA() {
		return lengthDATA;
	}

	public int getLengthRTS() {
		return lengthRTS;
	}

	public int getLengthCTS() {
		return lengthCTS;
	}

	public double getStepsACK() {
		return stepsACK;
	}

	public int getLengthACK() {
		return lengthACK;
	}

	public boolean isAckEnable() {
		return ackEnable;
	}

	public void setAckEnable(boolean ackEnable) {
		this.ackEnable = ackEnable;
	}

	public int getMaxSequence() {
		return maxSequence;
	}

	public double getStepsEndTx() {
		return stepsEndTx;
	}

	/** Obtém steps baseado no tipo de pacote */
	public double stepsDelayTx(PacketType pkType) {
		switch (pkType) {
		case RTS:
			return getStepsRTS()+getStepsEndTx();
		case ACK:
			return getStepsACK()+getStepsEndTx();
		case CTS:
			return getStepsCTS()+getStepsEndTx();
		case DATA:
			return getStepsDATA()+getStepsEndTx();
		default:
			return 0;
		}
	}
	
	public void adjustLengthCycle(boolean backboned) {
		if (backboned) {
			lengthCycle = 0.01d;
		} else {
			lengthCycle = 0.1d;
		}
	}
	
	/** Obtém steps baseado no tipo de pacote */
	
}
