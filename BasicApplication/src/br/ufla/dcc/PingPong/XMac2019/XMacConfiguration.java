package br.ufla.dcc.PingPong.XMac2019;

import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

/**
 *  Configuração do X-MAC;
 *  
 *  Usada pela XMac.java
 *
 * 	@author João Giacomin
 *  @version 18/03/2019
 */

public class XMacConfiguration {
	
	/** exige o envio de ACK para confirmar recepção de DATA */
	private boolean ackRequested;
	
	/** Tempo em steps em que o nó ficará em SLEEP */
	private double stepsSleep;
	
	/** Tempo em steps para a escuta do canal na espera de um RTS, Carrier Sense */
	private double stepsCS;
	
	/** Tempo em steps para enviar uma mensagem de Preâmbulo (RTS) */
	private double stepsRTS;
	
	/** Tempo em steps para enviar uma mensagem de ACK */
	private double stepsACK;
	
	/** Tempo em steps para enviar uma mensagem de DATA */
	private double stepsDATA;
	
	/** Tempo em steps para enviar uma mensagem earlyACK (CTS) */
	private double stepsCTS;

	/** Tempo em steps a mais como margem de segurança para garantir o fim de uma transmissão */
	private double stepsEndTx;
	
	/** Tempo em steps para Back Off entre CS_STARTs */
	private double stepsBOstart;
	
	/** Número máximo de tentativas iniciar envio de RTS */
	private int maxBOstarts = XMacConstants.NUM_MAX_RETRANSMISSIONS;
	
	/** Máximo de tentativas de reenvio de uma mensagem  */
	private int maxSendRetries = XMacConstants.NUM_MAX_RETRANSMISSIONS;
	
	/** Número máximo de preâmbulos a serem enviado na tentativa de se alcançar nó vizinho */
	private int maxPreambles;
	
	/** Tamanho do dado em bits */
	private int lengthDATA = XMacConstants.DATA_LENGTH;
	
	/** Tamanho do RTS em bits */
	private int lengthRTS = XMacConstants.RTS_LENGTH;
	
	/** Tamanho do RTS em bits */
	private int lengthACK = XMacConstants.ACK_LENGTH;
	
	/** Tamanho do CTS em bist */
	private int lengthCTS = XMacConstants.CTS_LENGTH;
	
	/** Padrão da intensidade de sinal a ser utilizada no envio do de um pacote */
	private double signalStrength = XMacConstants.SS_DEFAULT;
	
	
	/** Construtor */
	public XMacConfiguration(double lengthCycle, boolean ack) {
		
		// Se é para utilizar ACK
		this.ackRequested = ack;
		
		/* Os steps são a granularidade da simulação definida em "stepspersecond" em  application.xml
		stepsPBit define o número de steps necessários para enviar 1 bit */
		double stepsPBit = (double)Configuration.getInstance().getStepsPerSecond()/XMacConstants.BPS;
		
		// Tempo em steps para enviar cada tipo de pacote
		stepsRTS = stepsPBit * lengthRTS;
		stepsCTS = stepsPBit * lengthCTS;
		stepsACK = stepsPBit * lengthACK;
		stepsDATA= stepsPBit * lengthDATA;
		
		// Tempo de margem de segurança adicional para garantir que a transmissão finalizou
		stepsEndTx = stepsACK/2;
		

		/* Tempo em steps para escutar o canal em busca de preâmbulos. 
         * Para garantir que pelo menos 1 preâmbulo será ouvido por inteiro,
         * é preciso escutar pelo tempo de 1 preâmbulo + 1 wait_CTS + 1 preâmbulo
         * wait_CTS = stepsDelayRx(PacketType.CTS) = stepsCTS + 2*stepsEndTx.
         * Estava ocorrendo muita perda de preâmbulo, por tempo menor que 1 step,
         * por isso foi colocado +1 no stepsCS.
		 */
		stepsCS = 2*stepsRTS + stepsDelayRx(PacketType.CTS) + 1;
		
		/* Tempo de Back Off entre CS_STARTs */
		stepsBOstart = stepsDATA;
		
		double stepsCycle = lengthCycle * Configuration.getInstance().getStepsPerSecond();
		
		// Tempo em steps em que o nó ficará em SLEEP (steps do ciclo - steps Carrier Sense)
		stepsSleep = stepsCycle - stepsCS;
		
		/* Máximo de preâmbulos necessários para cobrir um tempo de ciclo (stepsSleep/(stepsRTS + stepsWaitCTS))
		 * O intervalo entre um preâmbulo e outro é o tempo de envio de 1 preâmbulo mais o tempo de espera por eACK.
		 * É acrescentado um porque a divisão pode não ser exata 12,4 = 13 preâmbulos. 
		 */
		maxPreambles = (int) (stepsCycle / (stepsRTS + stepsDelayRx(PacketType.CTS))) + 1 ;
	}

	
	/* Gets e sets */
	
	/** Tempo em steps do ciclo de trabalho*/
	public double getStepsCycle() {
		return stepsSleep + stepsCS;
	}
	
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
	
	public double getStepsACK() {
		return stepsACK;
	}

	public double getStepsEndTx() {
		return stepsEndTx;
	}
	
	public double getStepsBOstart() {
		return stepsBOstart;
	}

	public int getMaxBOstarts() {
		return maxBOstarts;
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

	public int getLengthRTS() {
		return lengthRTS;
	}

	public int getLengthCTS() {
		return lengthCTS;
	}

	public int getLengthDATA() {
		return lengthDATA;
	}

	public int getLengthACK() {
		return lengthACK;
	}

	public boolean isACKrequested() {
		return ackRequested;
	}

	public void setACKrequested(boolean ack) {
		this.ackRequested = ack;
	}

	/** Informa tempo (em steps) para esperar o rádio transmitir um pacote. 
	 *  É a soma de um intervalo de transmissão do pacote com um possível pequeno atraso do rádio */
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
	
	/** Informa tempo de espera (em steps) para receber um pacote 
	 *  É a soma de um intervalo stepsDelayTx com um possível pequeno atraso do rádio*/
	public double stepsDelayRx(PacketType pkType) {
		switch (pkType) {
		case RTS:
			return getStepsRTS()+2*getStepsEndTx();
		case ACK:
			return getStepsACK()+2*getStepsEndTx();
		case CTS:
			return getStepsCTS()+2*getStepsEndTx();
		case DATA:
			return getStepsDATA()+2*getStepsEndTx();
		default:
			return 0;
		}
	}

	/** Imprime parâmetros da MAC */
	public void imprimeParametros ( ) {
		System.out.println("* \n*   X-MAC \n*");
		System.out.println("* Steps de ciclo =  "+ (stepsSleep+stepsCS) );
		System.out.println("* Steps de sleep =  "+ stepsSleep);
		System.out.println("* Steps de CS =     "+ stepsCS );
		System.out.println("* Steps de RTS =    "+ stepsRTS );
		System.out.println("* Steps de CTS =    "+ stepsCTS );
		System.out.println("* Steps de ACK =    "+ stepsACK );
		System.out.println("* Steps de DATA =   "+ stepsDATA );
		System.out.println("* Steps de Etx =    "+ stepsEndTx );
		System.out.println("* Max Preambles =   "+ maxPreambles );
		System.out.println("* Max sendRetries = "+ maxSendRetries );
		System.out.println("* Max retyCSstart = "+ maxBOstarts );
		System.out.println("* \n  " );
		
	}

}
