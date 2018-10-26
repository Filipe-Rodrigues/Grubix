package br.ufla.dcc.PingPong.PaxMac;

import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

/**
 *  Configuração da MAC;
 *
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 04/07/2016
 */

public class PaxMacConfiguration {
	
	/** Tempo em steps em que o nó ficará em SLEEP */
	private double stepsSleep;
	
	/** Tempo em steps para a escuta do canal na espera de um RTS */
	private double stepsCsLong;
	
	/** Tempo em steps para enviar uma mensagem de Preâmbulo (RTS) */
	private double stepsRTS;
	
	/** Tempo em steps para enviar uma mensagem earlyACK (CTS) */
	private double stepsCTS;
	
	/** Tempo em steps para enviar uma mensagem CTS para habilitar o envio do dado */
	private double stepsCTSD;
	
	/** Tempo em steps para enviar uma mensagem de ACK */
	private double stepsACK;
	
	/** Tempo em steps para enviar uma mensagem de dado */
	private double stepsDATA;
	
	/** Tempo em steps a mais como margem de segurança para garantir o fim de uma transmissão */
	private double stepsTx;
	
	/** Tempo em steps para manter a posse do dado até sua transmissão */
	private double stepsHoldData;
	
	/** Mínimo multiplicador usado no recalculo do FCS (Hop=4) */
	private double fcsMinMultiplier;
	
	/** Máximo multiplicador usado no recalculo do FCS (Hop=1) */
	private double fcsMaxMultiplier;
	
	/** Número máximo de preâmbulos a serem enviado na tentativa de se alcançar nó vizinho */
	private int maxPreambles;
	
	/** Padrão da intensidade de sinal a ser utilizada no envio do de um pacote */
	private double signalStrength = PaxMacConstants.SS_DEFAULT;
	
	/** Define se é para usar reserva de canal (vizinhos que recebem RTS, acordam depois que o dado passar) */
	private boolean channelReservation = PaxMacConstants.CHANNEL_RESERVATION;
	
	/** Tamanho do dado em bits */
	private int lengthDATA; // Usar valor passado no application.xml
	
	/** Tamanho do RTS em bits */
	private int lengthRTS = PaxMacConstants.RTS_LENGTH;
	
	/** Tamanho do CTS em aviso a um RTS em bits */
	private int lengthCTS = PaxMacConstants.CTS_LENGTH;
	
	/** Tamanho do CTS que habilita o envio do dado bits */
	private int lengthCTSD = PaxMacConstants.CTS_DATA_LENGTH;
	
	/** Tamanho do RTS em bits */
	private int lengthACK = PaxMacConstants.ACK_LENGTH;

	/** Valor máximo de uma sequência, para identificar o dado. Deve ser sempre 2^N-1 */
	private int maxSequence = PaxMacConstants.MAX_SEQUENCE;
	
	/** Tamanho do FCS. Calculado em função do tamanho do dado */
	private int fcsSize;

	/** Limite de sequências de RTS que podem ser enviadas */
	private int maxRtsSequences;
	
	/** Quantidade máxima de vezes que o nó ira acordar para esperar o dado que não chegou */
	private int maxCtsDataRetry = PaxMacConstants.MAX_CTSDATA_RETRY;
	
	/** Quantas tentativas de envio/espera de CTS-DATA */
	private int ctsDataBackOffWatchdog = PaxMacConstants.CTSDATA_BACKOFF_WATCHDOG;
	
	/** Quantidade máxima de vezes que o nó ira acordar para esperar o dado que não chegou */
	private int ctsDataBackOffDuration = PaxMacConstants.CTSDATA_BACKOFF_DURATION;
	
	/** Ferramentas auxiliares diversas */
 	ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();
	

	/** Construtor */
	public PaxMacConfiguration(double cycleTime, double dataSizeRatio, double holdDataTime, 
			int maxRestartRtsSequence, double fcsMinMultiplier, double fcsMaxMultiplier) {

		this.maxRtsSequences = maxRestartRtsSequence;
		this.fcsMinMultiplier = fcsMinMultiplier;
		this.fcsMaxMultiplier = fcsMaxMultiplier;

		/* Os steps são a granularidade da simulação definida em "stepspersecond" em  applicatio.xml
		stepsPBit define o número de steps necessários para enviar 1 bit */
		double stepsPBit = (double)Configuration.getInstance().getStepsPerSecond()/PaxMacConstants.BPS;

		// Tempo em steps para enviar cada tipo de pacote
		stepsRTS = stepsPBit * lengthRTS;
		stepsCTS = stepsPBit * lengthCTS;
		stepsCTSD = stepsPBit * lengthCTSD;
		stepsACK = stepsPBit * lengthACK;
		
		// Tempo aproximado para a propagação de uma mensagem pelo meio físico até o próximo nó.
		stepsTx = stepsACK/2;
		
		// Tempo escutando o canal para verificar se não acordou no meio de CTS
		stepsCsLong = 2 * stepsTx + stepsCTS + stepsRTS;
		
		// Tempo em steps em que o nó ficará em SLEEP (steps do ciclo - steps escuta ociosa)
		stepsSleep = (cycleTime * Configuration.getInstance().getStepsPerSecond()) - stepsCsLong;
		
		/* O máximo de preâmbulos necessário para até o nó acordar (stepsSleep/(2*stepsRTS),
		 * uma vez que o próprio RTS é usado como CTS e para cada RTS enviado o nó espera um tempo de
		 * RTS. É acrescentado mais um preâmbulo como margem de segurança. */
		maxPreambles = (int) Math.ceil(stepsSleep / (2 * stepsRTS)) + 1;

		// Define tamanho e a quantidade de steps para enviar DATA	
		lengthDATA = (int) ((getStepsCycle() * dataSizeRatio)/stepsPBit);
		stepsDATA = stepsPBit * lengthDATA;
		
		// Define tamanho do FCS
		fcsSize = fcsSizeBasedOnDataSize();
		
		// Define o tempo para manter a posse do dado
		stepsHoldData = getStepsDataRetryCicle() * holdDataTime;
	}

	
	/** Função que retorna o delay para manter o dado até o início de seu envio */
	public double getStepsKeepData() {
		return stepsHoldData;
	}
	
	/** Retorna o tempo em que um nó deve adiantar o atrasar um evento para comunicar com outro nó com
	 * eventos que terminam ou iniciam no mesmo tempo que seus eventos */
	public double getStepsToDesynchronizeEvent() {
		return 0.005;
	}
	
    /** Função que retorna o delay para manter um nó acordado esperando o início da transmissão do dado */
	public double getStepsWaitingData() {
		return getStepsCsLong();
	}
	
    /** Função que retorna o delay para manter um nó acordado esperando um ACK/DATA. Se receber um 
     * CTS-DATA novamente quer dizer houve falha ao receber o ACK. Se após o tempo do CTS-DATA receber
     * o dado quer dizer que o nó recebeu o dado com sucesso */
	public double getStepsWaitingAck() {
		return getStepsCTSD() + getStepsTxAir();
	}
	
	/** Função que retorna o delay para manter um nó acordado esperando um CTS */
	public double getStepsWaitingCts() {
		return getStepsCTS();
	}
	
	/** Função que retorna o delay para manter um nó acordado esperando um CTS-DATA */
	public double getStepsWaitingCtsData() {
		return getStepsCTSD();
	}
	
	/** Função que retorna o delay do ciclo das tentativas de envio do dado. Formado pelo tempo de 
	 * envio do dado, mais um tempo de espera pelo ACK ou CTS-DATA  */
	public double getStepsDataRetryCicle() {
		// Margem para realizar as transmissões pela AIR e poder começar o novo quadro de tempo
		double safetyMargin = getStepsDATA() * 0.0003;
		return getStepsDATA() + getStepsWaitingCtsData() + getStepsToDesynchronizeEvent() + safetyMargin;
	}

	/** Função que retorna o delay para espera o dado de outro caminho passar, evitando colisões. Usado
	 * quando algum nó envia uma sequência completa de RTS de obter resposta  */
	public double getStepsBackOffWaitDataPass(NodeId id, double ctsDataDelay) {
		return misc.rand(id) * 2 * getStepsDataRetryCicle() + ctsDataDelay  + 
				(getCtsDataBackOffDuration() - 1) * getStepsDataRetryCicle();
	}

	
	/** Função que retorna o delay para que um nó envie seu primeiro RTS, evitando colisão com outros
	 * nós que também irão enviar o CTS/RTS */
	public double getStepsBackOffSendCts(NodeId id, PaxMacPacket packet) {
		double ranking = 0;
		// Descobrindo o ranking do nó
		if (packet.getFcsNodes() != null  && packet.getFcsNodes().size() > 1) {
			for (int i = 0; i < packet.getFcsNodes().size(); i++) {
				if (packet.getFcsNodes().get(i) == id) {
					ranking = i + 1; // o primeiro é 1 e não 0
					break;
				}
			}
			// Divide metade do tempo de um CTS pelo tamanho do FCS
			return (getStepsWaitingCts() / 2) * (ranking / packet.getFcsNodes().size());
		} else {
			return 0;
		}
	}
	
	/** Função que retorna o delay de espera ao ver que o canal está ocupado */
	public double getStepsBackOffChannelBusy(NodeId id) {
		return misc.rand(id) * getStepsDataRetryCicle();
	}
	
	
	/** Calcula o tamanho do FCS baseado no tamanho do dado */
	public int fcsSizeBasedOnDataSize() {
		int fcsSize;
		double rtsPerDataTime = getStepsDATA()/(2*stepsRTS);
		double rtsPerSleepTime = stepsSleep/(2*stepsRTS);
		
		for (fcsSize = 1; fcsSize <= 20; fcsSize++) {
			double sum = 0;
			for (int i = 1; i <= rtsPerSleepTime; i++) {
				sum += Math.pow(i/rtsPerSleepTime, fcsSize);
			}
			if (sum < rtsPerDataTime)
				return fcsSize;
		}
		return fcsSize;
	}
	
	
	/** Calcula o tamanho do FCS baseado no tempo em que o dado começará chegar */
	public int fcsSizeBasedOnDataTime(double delayUntilCtsData) {
		/* Em qual salto atrás o dado provavelmente estará no melhor caso */
		double currentHop;
		double multiplier;
		double hopStart = 1;
		double hopEnd   = 6;
		
		if (delayUntilCtsData == 0) {
			currentHop = 0;
		} else {
			currentHop = delayUntilCtsData/getStepsDataRetryCicle();
		}

		// Se for FCS fixo
		if (getFcsMaxMultiplier() == getFcsMinMultiplier()) {
			multiplier = getFcsMaxMultiplier();
		// Se FCS é variável
		} else {
			// Menor que 1 vai ocorrer somente para o nó fonte, pois o período é atualizado. (< 1 == 1)
			if (currentHop < hopStart) {
				multiplier = getFcsMaxMultiplier();
			} else if (currentHop > hopEnd) {
				multiplier = getFcsMinMultiplier();
			} else {
				double unitMult = (getFcsMaxMultiplier() - getFcsMinMultiplier()) / (hopEnd - hopStart);
				multiplier = (unitMult * (hopEnd - currentHop) + getFcsMinMultiplier());
			}
		}
		return (int) Math.ceil(multiplier * getFcsSize());
	}

	
	/** Gets e sets */
	public int getMaxRtsSequences() {
		return maxRtsSequences;
	}

	public int getMaxCtsDataRetry() {
		return maxCtsDataRetry;
	}
	
	public int getCtsDataBackOffWatchdog() {
		return ctsDataBackOffWatchdog;
	}
	
	public int getCtsDataBackOffDuration() {
		return ctsDataBackOffDuration;
	}
	
	public int getMaxPreambles() {
		return maxPreambles;
	}
	
	public double getFcsMaxMultiplier() {
		return fcsMaxMultiplier;
	}

	public double getFcsMinMultiplier() {
		return fcsMinMultiplier;
	}
	
	public double getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(double signalStrength) {
		this.signalStrength = signalStrength;
	}

	public double getStepsCycle() {
		return stepsSleep + stepsCsLong;
	}
	
	public double getStepsSleep() {
		return stepsSleep;
	}

	public double getStepsCsLong() {
		return stepsCsLong;
	}
	
	public double getStepsDATA() {
		return stepsDATA;
	}
	
	public int getLengthDATA() {
		return lengthDATA;
	}

	public double getStepsRTS() {
		return stepsRTS;
	}
	
	public int getLengthRTS() {
		return lengthRTS;
	}

	public double getStepsCTS() {
		return stepsCTS;
	}
	
	public double getStepsCTSD() {
		return stepsCTSD;
	}
	
	public int getLengthCTS() {
		return lengthCTS;
	}

	public int getLengthCTSD() {
		return lengthCTSD;
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

	public double getStepsTxAir() {
		return stepsTx;
	}
	
	public boolean isChannelReservation() {
		return channelReservation;
	}

	public double getStepsTx(PacketType pkType) {
		switch (pkType) {
		case RTS:
			return getStepsRTS();
		case ACK:
			return getStepsACK();
		case CTS:
			return getStepsCTS();
		case CTS_DATA:
			return getStepsCTSD();
		case DATA:
			return getStepsDATA();
		default:
			return 0;
		}
	}
	
	public int getFcsSize() {
		return fcsSize;
	}

	public void setFcsSize(int fcsSize) {
		this.fcsSize = fcsSize;
	}	
}
