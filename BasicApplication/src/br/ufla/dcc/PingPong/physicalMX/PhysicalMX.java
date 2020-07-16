package br.ufla.dcc.PingPong.physicalMX;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.SendingTerminated;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.user.PhysicalLayerDebugState;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayer;
import br.ufla.dcc.grubix.simulator.node.PhysicalTimingParameters;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 *  Protocolo simplificado para a camada física.
 *  Todas as transmissões são feitas em tempo muito curto.
 *  Para isto a função setBPS coloca a velocidade em  1000000000 bits por segundo.
 *  Há outra forma de controlar a velocidade de transmissão, 
 *  tornando curtos os Headers dos pacotes em cada layer.
 *  Mas isto obriga o programador a cuidar dos Headers em todas as camadas.
 *  
 *  Aqui, o tempo de transmissão e recepção de mensagens é controlado por WakeUpCalls.
 *  Ao receber um pacote da MAC, este será encapsulado em um pacote PHY e enviado para a AIR. 
 *  A AIR envia o pacote instantaneamente e avisa o final da transmissão através do evento SendingTerminated.
 *  O tempo de duração da transmissão (sendingDuration) será calculado na PHY com base no tamanho do Header.
 *  O tamanho do Header é definido na MAC.
 *  Ao final da transmissão, um sinal SendingTerminated será enviado para a MAC.
 *  
 *  Ao chegar um pacote pela lowerSAP (vindo da AIR), um sinal será enviado para a MAC, SFD (Start of Frame Delimiter). 
 *  O tempo de recepção será computado de acordo com o tamanho do Header. 
 *  O tamanho do Header é definido na MAC, conforme o tamanho do pacote a ser transmitido.
 *  O tempo previsto para o final da recepção é guardado em lastIncomingEnd.
 *  Ao atingir o tempo lastIncomingEnd o pacote será enviado para a MAC.
 *  
 *  O tempo de recepção de pacote poderá ser usado para verificar a colisão de pacotes.
 *  Sendo identificada a colisão de pacotes, um sinal (Colision) poderá ser enviado para a MAC.
 *  
 *  CarrierSense é uma função de serviço prestado à MAC. 
 *  Antes de iniciar uma transmissão, a MAC deverá pedir um CarrierSense (perguntar à PHY se o canal está ocupado).
 *  
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 18/03/2019
 *  
 */
public class PhysicalMX extends PhysicalLayer {
	
	/** set to "false" from the configuration, to even upward invalid packets. */
	@ShoXParameter(description = "set to false, to even upward invalid packets.", defaultValue = "true")
	private boolean dropInvalidPackets;
	
	/** set to "false" from the configuration, to bypass packets colision. */
	@ShoXParameter(description = "set to false, bypass packets colision.", defaultValue = "true")
	private boolean detectPacketColision;
	
	/** Velocidade de transmissão de dados que será usada na simulação, em bits por segundo. 
	 * 250kbps é o padrão IEEE802.15.4*/
	@ShoXParameter(description = "simulation transmission speed bps.", defaultValue = "250000")
	private int bitsPerSecond;

	/** Velocidade de transmissão de bits, utilizado para calcular colisão na AIR. 
	 Foi definida velocidade alta (1 Gigabit) para evitar colisão na AIR */
	static private double bpsAir = 1000000000.0;
	
	/** Tempo previsto para terminar a mensagem que o rádio está recebendo. 
	 * Usado para detectar colisões*/
	private double lastReceptionEnd = 0;
	
	/** Duração da ultima mensagem enviada */
	private double lastTransmissionDuration = 0;
	
	/** Se houve colisão com a mensagem transmitida ou recebida */
	boolean msgColision = false;
	
	/** Sinaliza canal ocupado, alguma mensagem está chegando */
	boolean busyChannel = false;
	
	/** Estado do rádio controlado pela PHY, diferente do "RadioState radioState" 
	 *  presente na PhysicalLayer, que é controlado pela AIR */
	PhyRadioState phyRadioState;
	
	/** Tamanho adicional do pacote da PHY, será somado ao tamanho do pacote vindo da MAC. 
	 *  Por enquanto será deixado em zero */
	private int headerPlusFooterLength = 0;
	
	public PhysicalMX() {
		super();
	}

	
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		
		phyRadioState = new PhyRadioState(RadioState.OFF);
		// Define a taxa máxima de bits por steps suportada
		double[] bitsPerSimulationStep = { bpsAir / configuration.getSimulationSteps(1.0) };
		timings = new PhysicalTimingParameters(bitsPerSimulationStep);
		
		/* Potência do sinal em mW, atributo sobrescrito de PhysicalLayer.
		 * Potência padrão, caso não seja utilizado o valor informado no pacote */
		signalStrength = 16.0;
		
		/* Rádio controlado pela AIR sempre ficará em LISTENING, o controle sobre atividade do
		 * nó ficará a cargo do phyRadioState */
		PhysicalLayerState phyState = (PhysicalLayerState) this.getState();
		phyState.setRadioState(RadioState.LISTENING);
		setState(phyState);
	}
	
	
	protected void processEvent(StartSimulation start) {
		phyRadioState.setRadioState(RadioState.OFF);
	}
	
    
	@Override
	public void upperSAP(Packet packet) {
		
		/** 
		 *  Função foi criada a partir da upperSAP de PhysicalDebug. 
		 *  A informação de BPS enviada para a AIR foi multiplicada por 40000, para inutilizar os controles da AIR.	
		 *  Controles de tempos de Tx e Rx e de colisão são feito aqui.
		 */
		
		
		// Verificação de erro, pacotes que não são instâncias de MACPacket
        if (!(packet instanceof MACPacket)) {
            System.out.println("PHY - upperSAP: " + this.id + " Mensagem incompatível: "+packet.getClass().getName());
            return;
        }
        
        // Mensagem vinda da camada superior (MAC)
        MACPacket pkt = (MACPacket) packet;
        // Mensagem que será enviada para a AIR
        PhysicalMXPacket nextPacket = new PhysicalMXPacket(sender, pkt, phyRadioState.getCurrentChannel());
	
		// Status do rádio controlado pela AIR
		if (radioState == RadioState.WILL_SEND) {          // copiado de PhysicalDebug
			// Apenas para transmitir rapidamente pela AIR 
			nextPacket.setBPS(bpsAir / getConfig().getSimulationSteps(1.0));  
			// Potência que será utilizada, informada no pacote
			signalStrength = pkt.getSignalStrength();
			// tempo para transmitir o pacote 
			lastTransmissionDuration = transmissionSteps(pkt.getHeaderLength());
			
			// estado do rádio controlado pela PHY.
			if (phyRadioState.getRadioState() == RadioState.LISTENING){					
				// Pacote será enviado pela AIR
				node.transmit(nextPacket, signalStrength);
				phyRadioState.setRadioState(RadioState.SENDING);
			}
		}

	}
	

	@Override
	public void lowerSAP(Packet packet) {
		
		/* Trata apenas o início da recepção de mensagens, 
		 * correspondente ao SFD (Start of Frame Delimiter) de um rádio real. 
		 * O processamento após o final da recepção é feito pelo WUC de rádio   */
		if (packet.isTerminal()){
			return;
		} 
		
		if (!packet.isValid()) {
			if (dropInvalidPackets) {
				return;
			} 
		}
		
		
		if (!(packet instanceof PhysicalPacket)) {
			System.out.println("PHY - lowerSAP: " + this.id + " Mensagem incompatível: " + packet.getClass().getName()); 
			return;
		}

		NodeId senderId = packet.getSender().getId();
		
		// Se receber uma mensagem de um canal diferente do sintonizado, ignora
		
		if (packet instanceof PhysicalMXPacket) {
			if (((PhysicalMXPacket) packet).getPacketChannel() != phyRadioState.getCurrentChannel()
					&& !senderId.equals(NodeId.ALLNODES))
				return;
		}
		
		// Se receber uma mensagem enviada por ele mesmo não faz nada
		
		if (senderId.equals(getNode().getId())) 
			return;
	
		PhysicalPacket incomingPacket = (PhysicalPacket) packet;  
		
		// Se o tempo atual for menor que fim de uma transmissão, ocorrerá uma colisão
		if(currentTimeSteps() < lastReceptionEnd) {
			// se ocorreu uma colisão e não era para detectar colisões, não faça nada até que o pacote corrente termine de chegar
			if (!detectPacketColision) return;
			
			msgColision = true;
			
		} else {
			msgColision = false;
		}
		
		// Camada física começou a receber mensagem, logo o canal está ocupado
		busyChannel = true;
		

		/* O tamanho total do pacote MAC é informado no pacote da MAC utilizando o atributo 
		 * 'headerLength' da superclasse 'Packet'. O tamanho total do PPDU é a soma do tamanho do 
		 * pacote MAC com os incrementos do header e do footer.                                   */
		int packetLength = (incomingPacket.getEnclosedPacket()).getHeaderLength() + headerPlusFooterLength;
		double receivingDuration = transmissionSteps(packetLength);
		
		/* Se o tempo final para a transmissão atual for maior que a vigente, 
		 * então irá atualizar o tempo final e criar evento para marcar o tempo 
		 * em que o rádio ficará recebendo sinais da mensagem que está chegando */
		if ((currentTimeSteps() + receivingDuration) > lastReceptionEnd) {
			lastReceptionEnd = currentTimeSteps() + receivingDuration;
			WucPhyRxTimer timeChannelActivity = new WucPhyRxTimer(sender, receivingDuration, 
					phyRadioState.getRadioState(), incomingPacket);
			sendEventSelf(timeChannelActivity);   
			// só precisa marcar outro tempo de atividade do canal se for ultrapassar o tempo que já estava sendo marcado.
		}
		
		/* Todas as verificações feitas anteriormente são para verificar atividade no canal de comunicação.
		 * A seguir começam as verificações para a recepção de mensagem pelo rádio do nó sensor. 
		 * Caso o rádio não esteja em LISTENING ou RECEIVING, irá ignorar a mensagem.                      */	
		if((phyRadioState.getRadioState() != RadioState.LISTENING) && 
				(phyRadioState.getRadioState() != RadioState.RECEIVING)) {
			return;
		}
		
		phyRadioState.setRadioState(RadioState.RECEIVING);	

		if(msgColision) {
			// Avisa a Mac que ocorreu colisão de mensagens no receptor
			System.out.println("PHY: Node = " + this.id + ", Colisão ");
			sendEventUp(new EventCollisionDetect(sender, incomingPacket.getEnclosedPacket()));
		} else {
			/* Avisa a Mac que tem mensagem chegando. Oficialmente, a MAC só conhecerá a mensagem 
			 posteriormente. */
			sendEventUp(new StartOfFrameDelimiter(sender, incomingPacket.getEnclosedPacket()));
		}
		
	}
	
	
	/**
	 * Method to process the wakeup calls for this layer.
	 * @param wuc the to be processed wakeup call.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {

		// Eventos vindos de outras camadas
		if (wuc instanceof CrossLayerEvent) {
			/* Evento que vem de node.AirModule:processTransmissionEndOutgoing. 
			 * Que é enviado quando acaba de enviar uma mensagem. 
			 * A classe node.AirModule:processTransmissionEndOutgoing 
			 * usa a função sendEventUp() para enviar um evento para a camada física */
			if (wuc instanceof SendingTerminated ) {
				/* Se chegou aqui é porque o rádio foi colocado em SENDING e a transmissão foi feita pela AIR */
				PhysicalPacket packetSent = (PhysicalPacket)(((SendingTerminated) wuc).getPacket());				
				WucPhyTxTimer timerWUC = new WucPhyTxTimer(sender, lastTransmissionDuration, packetSent);
				sendEventSelf(timerWUC);
			}			
			
			/* Evento para desligar ou ligar o rádio, ordem vinda da MAC. 
			 * Não existe aqui uma função para informar a MAC sobre o estado do rádio.
			 * A MAC deverá cuidar para não mandar mudar o estado do rádio se este estiver ocupado.
			 */
			if (wuc instanceof EventPhyTurnRadio){
				// Se TRUE, a ordem é para ligar, se FALSE, para desligar
				if (((EventPhyTurnRadio)wuc).IsTheRadioOn()) {
					phyRadioState.setCurrentChannel(((EventPhyTurnRadio)wuc).getChannel());
					/* O rádio é ligado no estado LISTENING, mas somente se estiver OFF.
					 * Se estiver em outro estado, nada será feito.                      */
					if (phyRadioState.getRadioState() == RadioState.OFF) {
						phyRadioState.setRadioState(RadioState.LISTENING);
			    		// Filtro para o VisualGrubix
						SimulationManager.logNodeState(node.getId(), "Radio", "int", String.valueOf(3));
					}
				} else {
					/* Desliga o rádio, não importando qual seja o estado atual */
		    		phyRadioState.setRadioState(RadioState.OFF);
		    		// Filtro para o VisualGrubix
					SimulationManager.logNodeState(node.getId(), "Radio", "int", String.valueOf(5));
		    	}
			}
			
			// Evento vindo da MAC para verificar se o canal está ocupado.
			if (wuc instanceof EventCarrierSense) {	
				sendEventUp(new EventCarrierSense(sender, busyChannel));
			}	
			
		// Quando o rádio termina de enviar uma mensagem
		} else if ((wuc instanceof WucPhyTxTimer)) { 
		
			if (phyRadioState.getRadioState() == RadioState.SENDING) {
				/* Se chegou aqui é porque o rádio foi colocado em SENDING, a transmissão foi feita pela AIR 
				 * e terminou o tempo de envio 
				 * Então, vamos avisar para a MAC que o pacote acabou de ser enviado */
				sendEventUp(new SendingTerminated(sender, ((WucPhyTxTimer)wuc).getPacket().getEnclosedPacket()));
				phyRadioState.setRadioState(RadioState.LISTENING);
			}
			
		// Quando uma mensagem é completamente recebida da AIR
		} else if ((wuc instanceof WucPhyRxTimer)) {
						
			if (currentTimeSteps() >= lastReceptionEnd) { // acabou a atividade no canal de comunicação ?
                /*
				 * Para receber a mensagem é preciso que o rádio estivesse em modo de recepção.
				 * Esta verificação é feita aqui porque a WucPhyRxTimer é criada antes de se verificar o estado do rádio.
				 */
				if (phyRadioState.getRadioState() == RadioState.RECEIVING) {
					phyRadioState.setRadioState(RadioState.LISTENING);
					/* Após o tempo de duração da mensagem que está chegando, se não teve colisão na 
					 * recepção da mensagem, envia ela para a MAC. */
					if (!msgColision) {
						/* Terminou a recepção da última mensagem, mas é preciso verificar se ocorreu 
						 * colisão durante a recepção. Para que seja confirmada a recepção completa de uma mensagem, 
						 * é preciso que a WucPhyRxTimer tenha sido criada em um tempo em que o rádio estivesse ligado */
						RadioState rsInTheBeginning = ((WucPhyRxTimer)wuc).getStartingState();
						if((rsInTheBeginning == RadioState.LISTENING) || (rsInTheBeginning == RadioState.RECEIVING)) {
							Packet pkPHY = ((WucPhyRxTimer)wuc).getPacket();
							sendPacket(pkPHY.getEnclosedPacket());	
						}
					}
				}
				busyChannel = false;
				msgColision = false;
			} 
			
		} else {
			// Em outros casos chama processWakeUpCall da classe superior
			super.processWakeUpCall(wuc);
		}
	}

	
	/**
	 *  Função para calcular o tempo (em steps) que será gasto na transmissão para 250kbps
	 */
	private double transmissionSteps(int packetLength){
		double stepsTx = ((double)packetLength / bitsPerSecond) * getConfig().getStepsPerSecond();
		if (stepsTx < 0)
			System.out.println("Steps para transmissão menor que zero");
		return stepsTx;
	}
	
	/** 
	 *  Função para obter o tempo corrente da simulação em steps 	
	 */
	private double currentTimeSteps() {
		return SimulationManager.getInstance().getCurrentTime();
	}

	
	@Override
	public LayerState getState() {
		return new PhysicalLayerDebugState(timings, null, radioState, 0, signalStrength, dropInvalidPackets);
	}
	

	@Override
	public boolean setState(LayerState state) {
		if (state instanceof PhysicalLayerDebugState) {
			dropInvalidPackets = ((PhysicalLayerDebugState) state).isDropInvalidPackets();
		}	
		return super.setState(state);
	}


	/* gets e sets */
	public boolean isDropInvalidPackets() {
		return dropInvalidPackets;
	}


	public int getBpsSimulation() {
		return bitsPerSecond;
	}


	public double getLastReceptionEnd() {
		return lastReceptionEnd;
	}


	public double getLastEmissionDuration() {
		return lastTransmissionDuration;
	}


	public boolean isMsgColision() {
		return msgColision;
	}


	public int getHeaderPlusFooterLength() {
		return headerPlusFooterLength;
	}


	public boolean isBusyChannel() {
		return busyChannel;
	}


	public RadioState getPhyRadioState() {
		return phyRadioState.getRadioState();
	}
	
	
	public RadioState getRadioState() {
		return radioState;
	}
	
}
