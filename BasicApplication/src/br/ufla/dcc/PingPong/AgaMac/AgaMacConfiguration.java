package br.ufla.dcc.PingPong.AgaMac;

import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

public class AgaMacConfiguration {

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
	private int maxSendRetries = AgaMacConstants.NUM_MAX_RETRANSMISSIONS;
	
	/** Padrão da intensidade de sinal a ser utilizada no envio do de um pacote */
	private double signalStrength = AgaMacConstants.SS_DEFAULT;
	
	/** Tamanho do dado em bits */
	private int lengthDATA = AgaMacConstants.DATA_LENGTH;
	
	/** Tamanho do RTS em bits */
	private int lengthRTS = AgaMacConstants.RTS_LENGTH;
	
	/** Tamanho do RTS em bits */
	private int lengthACK = AgaMacConstants.ACK_LENGTH;
	
	/** Tamanho do CTS em bist */
	private int lengthCTS = AgaMacConstants.CTS_LENGTH;
	
	/** Valor máximo de uma sequência, para identificador de mensagem ou para preâmbulos. Deve ser sempre 2^N-1 */
	private int maxSequence = AgaMacConstants.MAX_SEQUENCE;

	
	
	/** Construtor */
	public AgaMacConfiguration(double lengthCycle) {
		
		/* Os steps são a granularidade da simulação definida em "stepspersecond" em  applicatio.xml
		stepsPBit define o número de steps necessários para enviar 1 bit */
		double stepsPBit = (double)Configuration.getInstance().getStepsPerSecond()/AgaMacConstants.BPS;
		
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
		stepsCS = 2*stepsRTS + stepsCTS;
		
		// Tempo em steps em que o nó ficará em SLEEP (steps do ciclo - steps escuta ociosa)
		stepsSleep = (lengthCycle * Configuration.getInstance().getStepsPerSecond()) - stepsCS;

		// Tempo de margem de segurança adicional para garantir que a transmissão finalizou
		stepsEndTx = stepsACK/2;
		
		/* O máximo de preâmbulos necessário para até o nó acordar (stepsSleep/(stepsRTS + stepsCTS)
		 * É acrescentado um porque a divisão pode não ser exata 1,1 = 2 preâmbulos. Também é acrescentado
		 * outro preâmbulo (analisar para ver motivo)
		 */
		maxPreambles = (int) (stepsSleep / (stepsRTS + stepsCTS)) + 2;
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
	
	/** Obtém steps baseado no tipo de pacote */
	
}
