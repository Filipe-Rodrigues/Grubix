/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
 ********************************************************************************/

package br.ufla.dcc.PingPong.USAMac;

import static br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager.USAMAC_CONFIG;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.SendingTerminated;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Link;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.MACState;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.USAMac.USAMacState.USAMacEventType;
import br.ufla.dcc.PingPong.USAMac.USAMacState.USAMacStateType;
import br.ufla.dcc.PingPong.physicalX.EventCarrierSense;
import br.ufla.dcc.PingPong.physicalX.EventCollisionDetect;
import br.ufla.dcc.PingPong.physicalX.EventPhyTurnRadio;
import br.ufla.dcc.PingPong.physicalX.StartOfFrameDelimiter;
import br.ufla.dcc.PingPong.routing.USAMac.USAMacRoutingControlPacket;
import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * Protocolo X-MAC
 * 
 * Deve ser usado em conjunto com PhysicalX.java.
 * 
 * Controla a criação e o fluxo de pacotes. Cria um pacote de dados em
 * startSendDataProcess. Cria um pacote de preambulo (RTS) em startRTSsequence.
 * Controla a marcação de tempo para todos os estados. Recebe todos os eventos e
 * chama a XMacStateMachine para indicar o próximo estado. Todas as mudanças de
 * estado são decididas pela XMacStateMachine.java.
 * 
 * Sempre que recebe pacote pela UpperSAP: - encapsula dentro de um pacote MAC
 * (o DATA); - cria o pacote preambulo (RTS); - executa a sequencia de envio de
 * RTS e espera de CTS; - recebendo CTS, envia DATA e espera ACK (opcional).
 * 
 *
 * @author João Giacomin
 * @version 18/03/2019
 */

public class USAMac extends MACLayer {

	/** Tempo de ciclo em segundos (obrigatório estar aqui) */
	@ShoXParameter(description = " Tempo de Ciclo em segundos")
	private double cycleTime;

	/** Requisitar o uso de ACK para confirmar recebimento de DATA */
	@ShoXParameter(description = " Habilitar o uso de ACK ", defaultValue = "true")
	private boolean ackRequested;

	/** Objeto responsável pela mudança do estado do XMac */
	private USAMacStateMachine xStateMachine;

	/** Objeto que contém todas as variáveis de configuração do XMac */
	private USAMacConfiguration xConf;

	/** Objeto que armazenas as informações de estados do XMac */
	private USAMacState xState;

	/**
	 * The internal state of the MAC that can be modified from outside. Apenas para
	 * manter compatibilidade com Shox
	 */
	private MACState macState;

	/** Ativa funções de depuração */
	private boolean debug = false;

	/** Ferramenta para o VisualGrubix */
	private ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();

	/**
	 * Função padrão do Grubix para fazer a configuração do objeto
	 * 
	 * @throws ConfigurationException thrown when the object cannot run with the
	 *                                configured values.
	 */
	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);

		/*
		 * Declaração obrigatória no Grubix (MAC_IEEE802_11bg_AdHoc) Param1: Política de
		 * encaminhamento Param2: Potência máximo de transmissão Param3: Número de
		 * pacotes a serem transmitidos em uma fila
		 */
		macState = new MACState(raDefaultPolicy, 16.0, 0);

		// Define o estado inicial do MAC em SLEEP e o contador de sequência de estados
		// em zero.
		xState = new USAMacState(USAMacStateType.SLEEP, 0);

		// Inicializa as variáveis de configuração
		xConf = new USAMacConfiguration(cycleTime, ackRequested);

		// Cria a instância da máquina de estados
		xStateMachine = new USAMacStateMachine(sender, xState, xConf);
	}

	/**
	 * Função padrão do Grubix para iniciar o funcionamento do nó sensor. Marcar um
	 * tempo aleatório para ligar o rádio pela primeira vez.
	 */
	protected void processEvent(StartSimulation start) {
		// Define o novo estado e duração, e incrementa a sequência de estado
		if (BackboneConfigurationManager.getInstance(USAMAC_CONFIG).amIBackbone(node.getId())) {
			switchToBackbone();
			double cycleRatio = BackboneConfigurationManager.getInstance(USAMAC_CONFIG).getNodeCycleSyncTiming(node.getId());
			xConf.setCycleSyncTiming(cycleRatio);
			SingletonTestResult.getInstance().countBackboneNode();
		}
		xStateMachine.changeStateBootNode();
		createtWucTimeOut();
//		if (node.getId().asInt() == 755 || node.getId().asInt() == 533) {
//			System.err.println("#" + node.getId() + ": " + xConf.getCycleSyncTimingRatio());
//		}
		// testBackbone();
	}

	private boolean switchToBackbone() {
		if (!xConf.isBackboneNode()) {
			xConf.setBackboneState(true);
			misc.vGrubix(node.getId(), "Backbone", "DARK_BLUE");
			return true;
		}
		return false;
	}

	/**
	 * Função padrão do Grubix para tratar eventos WakeUpCall
	 *
	 * Tipos de WakeUpCall: --> WucTimeOut = marcação de tempo de SLEEP, de Carrier
	 * Sense e espera por CTS, ACK ou DATA. --> CrossLayerEvent = eventos emitidos
	 * pela Physical Layer para a MAC, com informações do rádio.
	 */
	public final void processWakeUpCall(WakeUpCall wuc) {

		USAMacEventType event = USAMacEventType.VOID;
		// Tratamento de evento de fim de marcação de tempo
		if (wuc instanceof USAMacWucTimeOut) {

			/*
			 * Verifica se o número do estado armazeno no WUC é o mesmo do estado atual. Se
			 * não for, já houve mudança no estado do XMac antes do término do WUC, portanto
			 * esse WUC deve ser ignorado, já que não é mais válido.
			 */
			if (xState.getStateSeqNum() != ((USAMacWucTimeOut) wuc).getStateNumber()) {
				return;
			}
			// xState.setEvent(XMacEventType.TIME_OUT);
			event = USAMacEventType.TIME_OUT;
		}

		// Tratamento dos eventos emitidos pela PHY.
		if (wuc instanceof CrossLayerEvent) {
			CrossLayerEvent cle = (CrossLayerEvent) wuc;

			if (cle instanceof EventCarrierSense) {
				// Recebe evento da PHY informando se o canal está ocupado
				if (((EventCarrierSense) cle).isChannelBusy()) {
					xState.setChannelBusy(true);
					event = USAMacEventType.CHANNEL_BUSY;
				} else {
					xState.setChannelBusy(false);
					event = USAMacEventType.CHANNEL_FREE;
				}
			}

			// A seguir somente serão permitidos eventos que carregam pacotes do tipo
			// XMacPacket
			else if (!(cle.getPacket() instanceof USAMacPacket)) {
				return;
			}

			USAMacPacket packet = (USAMacPacket) cle.getPacket();

			// Recebe evento da PHY quando uma mensagem é completamente enviada.
			if (cle instanceof SendingTerminated) {
				event = USAMacEventType.MSG_SENT;
				if (debug)
					System.out.println(
							"MAC: " + this.id + ", Event = SendingTerminated - pack type = " + packet.getType());
			}

			// Recebe evento da PHY quando ocorre uma colisão na recepção de uma mensagem
			if (cle instanceof EventCollisionDetect) {
				// vou manter esta função para debug. Não tem como a PHY saber que houve
				// colisão, apenas erro de CRC.
				event = USAMacEventType.COLLISION;
				if (debug)
					System.out.println("MAC: " + this.id + ", Colisão informada pela PHY");
			}

			// Recebe evento da PHY quando uma mensagem está chegando
			if (cle instanceof StartOfFrameDelimiter) {
				// O rádio indica que identificou o início de uma mensagem (Start of Frame
				// Delimiter)
				event = USAMacEventType.SFD;
			}

		} // Fim de tratamento dos eventos emitidos pela PHY.

		// SFD e COLLISION não serão tratados por enquanto
		if (event == USAMacEventType.COLLISION || event == USAMacEventType.SFD)
			return;

		// Chamar a função que busca o novo estado, conforme o evento observado, e
		// executa as ações pertinentes.
		gotoNextState(event);

	} // Fim da função processWakeUpCall

	/**
	 * Função padrão do GrubiX para tratar pacotes recebidos da camada superior
	 * (LogLink) Após receber um pacote da LogLink, é chamada a função de início de
	 * transmissão de pacote.
	 */
	public final void upperSAP(Packet llPacket) {

		if (this.node.getId().asInt() == -1) {
			System.out.println("\n  +  Este é o USA-MAC 2019  +\n");
			xConf.imprimeParametros();
		}

		// Se o pacote é para o próprio nó, manda de volta para a camada acima
		if (llPacket.getReceiver() == id) {
			// Muda a direção do pacote para que seja enviado para cima
			llPacket.flipDirection();
			sendPacket(llPacket);
			return;
		}

		if (llPacket.getEnclosedPacket() instanceof USAMacRoutingControlPacket) {
			// System.err.println("FOI");
			switchToBackbone();
			if (xConf.getCycleSyncTimingRatio() < 0) {
				xConf.setCycleSyncTiming((double) (SimulationManager.getInstance().getCurrentTime() % 1000) / 1000d);
				BackboneConfigurationManager.getInstance(USAMAC_CONFIG).setNodeCycleSyncTiming(node.getId(), 0);
			}
		}

		startSendDataProcess(llPacket);

		gotoNextState(USAMacEventType.LOG_LINK);

	}

	/**
	 * Função padrão do GrubiX para tratar pacotes recebidos da camada inferior.
	 * Após a PHY terminar de receber uma mensagem pelo rádio, envia para a MAC. A
	 * MAC chamará a função de tratamento da mensagem recebida.
	 */
	public final void lowerSAP(Packet packet) {

		if (!(packet instanceof USAMacPacket)) {
			return;
		}

		// Ignorar caso a mensagem foi gerada pelo próprio nó
		if (packet.getSender().getId() == getNode().getId())
			return;

		if (packet instanceof USAMacBackbonePacket) {
			USAMacBackbonePacket bbPacket = (USAMacBackbonePacket) packet;
			NodeId nextBB = bbPacket.getNextBackboneTarget();
			if (node.getId().equals(nextBB)) {
				double cycleShiftRatio = bbPacket.getParentBackboneCycleShiftRatio();
				xConf.updateCycleSyncTiming(cycleShiftRatio);
				BackboneConfigurationManager.getInstance(USAMAC_CONFIG).setNodeCycleSyncTiming(node.getId(), xConf.getCycleSyncTimingRatio());
			}
		}

		USAMacPacket xPack = (USAMacPacket) packet;
		xState.setRecPkt(xPack);

		if (debug)
			System.out.println("MAC: " + this.id + ", Pacote tipo " + xPack.getType() + " recebido de "
					+ xPack.getSender().getId());

		USAMacEventType event;

		switch (xPack.getType()) {
		case RTS:
			event = USAMacEventType.RTS_RECEIVED;
			break;
		case CTS:
			event = USAMacEventType.CTS_RECEIVED;
			break;
		case ACK:
			event = USAMacEventType.ACK_RECEIVED;
			break;
		case DATA:
			event = USAMacEventType.DATA_RECEIVED;
			break;
		default:
			event = USAMacEventType.VOID;
		}

		gotoNextState(event);
	}

	/**
	 * Função para chamar a XMacStateMachine, para decidir o próximo estado, e
	 * executar próximas ações
	 */
	private void gotoNextState(USAMacEventType event) {

		/* Chama a State Machine para decidir qual será o próximo estado */
		if (xStateMachine.changeState(event)) {
			/*
			 * Todo estado tem um tempo previsto de duração. O tempo previsto e o número de
			 * sequencia do estado foram colocados pela State Machine na XMacState. Se a
			 * XMacStateMachine mudou o estado (retorno = true), então a XMac deverá criar
			 * uma nova WUC
			 */
			createtWucTimeOut();
		}
		/*
		 * Se não foi criado um novo estado, continua no estado anterior, marcando o
		 * tempo anterior. Apenas execute as ações previstas
		 */

		switch (xState.getAction()) {

		case ASK_CHANNEL:
			/* Manda ligar o rádio - será ligado no estado LISTEN */
			sendEventDown(new EventPhyTurnRadio(this.sender, true));
			/*
			 * Pergunta ao rádio se canal está ocupado - PHY enviará um evento com resposta
			 */
			sendEventDown(new EventCarrierSense(sender));

			if (debug)
				System.out.println("MAC: " + this.id + ", Event = " + event + ", Action = ASK_CHANNEL, next State = "
						+ xState.getState());
			break;

		case TURN_ON:
			/* Manda ligar o rádio - será ligado no estado LISTEN */
			sendEventDown(new EventPhyTurnRadio(this.sender, true));
			break;

		case TURN_OFF:
			/* Manda desligar o rádio - estado OFF */
			sendEventDown(new EventPhyTurnRadio(this.sender, false));
			break;
			
		case TURN_OFF_CS_END:
			/* Manda desligar o rádio - estado OFF */
			sendEventDown(new EventPhyTurnRadio(this.sender, false));
			sendEventTo(new EventFinishedCSEnd(sender), LayerType.NETWORK);
			break;

		case START_RTS:
			/* Preparar o início de envio de preâmbulos */
			if (!startRTSsequence()) {
				// Rádio pode estar SENDING
				System.out.println("MAC: " + this.id + ", gotoNextState - falhou envio de MSG, rádio ocupado ");
			} else {
				sendXMacPacket(USAMacStateType.SENDING_RTS);
			}
			if (debug)
				System.out.println("MAC: " + this.id + ", Event = " + event + ", Action = START_RTS, next State = "
						+ xState.getState());
			break;

		case MSG_UP:
			/* Enviar DATA para a camada superior */
			if (xState.getRecPkt().getType() == PacketType.DATA
					&& xState.getRecPkt().getDirection() == Direction.UPWARDS) {
				// Vamos garantir que é pacote DATA e que vai para a Log Link Layer
				sendPacket(xState.getRecPkt().getEnclosedPacket());
			}
			if (debug)
				System.out.println("MAC: " + this.id + ", Event = " + event
						+ ", Action = MSG_UP, Type = DATA, next State = " + xState.getState());
			break;

		case MSG_DOWN:
			/*
			 * Enviará mensagem para o rádio, se houver. O estado SENDING_RTS envia RTS;
			 * SENDING_CTS envia CTS; SENDING_DATA envia DATA; SENDING_ACK envia ACK. Em
			 * qualquer outro estado da MAC, nenhuma mensagem é enviada. Ao final do envio,
			 * a PHY envia um evento MSG_SENT
			 */
			PacketType type = sendXMacPacket(xState.getState());

			if (debug)
				System.out.println("MAC: " + this.id + ", Event = " + event + ", Action = MSG_DOWN, Type = " + type
						+ ", next State = " + xState.getState());
			break;

		case CONTINUE:
			// Nenhuma ação a executar
			break;

		default:
			// Ordem não prevista
			break;
		}

	} // Fim da função gotoNextState

	/**
	 * Inicia o processo de envio de um pacote de DATA recebido da camada LogLink
	 * (superior). Construir uma mensagem de dados contendo o pacote recebido da
	 * LogLink e atribuir um número sequencial. Guardar o pacote DATA na XMacState
	 */
	private boolean startSendDataProcess(Packet packet) {

		/*
		 * Objeto LogLink para pegar o valor da potência de transmissão pelo rádio,
		 * definida na camada superior, LogLinkLayer, enquanto não for usado outro
		 * método para definir SignalStrength
		 */
		LogLinkPacket llpack = (LogLinkPacket) packet;
		Link link = llpack.getMetaInfos().getDownwardsLLCMetaInfo().getLink();
		xConf.setSignalStrength(link.getTransmissionPower());

		xState.setReceiverNode(llpack.getReceiver());

		// Cria o pacote de DATA da camada MAC para ser enviado depois dos preâmbulos.
		USAMacPacket newDATApacket = createDataPacket(llpack, xConf.isACKrequested());

		// Armazena o pacote que será enviado
		xState.setDataPkt(newDATApacket);
		xState.setDataPending(true); // só voltará a ser false quando terminar de enviar DATA, ou quando esgotarem as
										// tentativas

		if (debug)
			System.out.println("MAC: " + this.id + ", criado pacote de dados ");

		return true;
	}

	/**
	 * Função para iniciar o envio de uma sequência de preâmbulos (RTS) Inicialmente
	 * verifica-se se o rádio está livre para iniciar uma transmissão. Depois devem
	 * ser seguidos os passos: 1) Construir uma mensagem RTS (preâmbulo); 2) Colocar
	 * o número de sequência da mensagem RTS igual ao máximo número de preâmbulos em
	 * um ciclo; 3) Iniciar a escuta do canal
	 */
	private boolean startRTSsequence() {

		// Início do envio da sequência de RTS, portanto não precisa reiniciar a
		// sequência de envio

		// Verifica se já fez todas tentativas de envio. Deveria ser verificado por quem
		// chama a startRTSsequence
		if (xState.getDataPkt().getRetryCount() == 0) {
			if (debug)
				System.out.println("MAC: " + this.id + ", Terminaram as tentativas de enviar DATA ");
			return false;
		}

		/*
		 * Se o rádio estiver ocupado não poderá enviar RTS Esta verificação deve ser
		 * feita antes de chamar startRTSsequence mas é feita aqui, novamente, por
		 * motivo de segurança de código
		 */
		if (xState.isChannelBusy())
			return false;

		/*
		 * Cria um pacote de preâmbulo (RTS) para ser enviado após CS_START. Guarda o
		 * RTS no XMacState.
		 */
		USAMacPacket newRtsPacket = createCtrlPacket(xState.getReceiverNode(), PacketType.RTS, xConf.getMaxPreambles(),
				(xState.getReceiverNode() != NodeId.ALLNODES), xConf.getLengthRTS(), xConf.getSignalStrength());

		xState.setRtsPkt(newRtsPacket);

		// Decrementa a tentativa de envio de DATA
		xState.getDataPkt().decRetryCount();

		return true;
	}

	/**
	 * Função para enviar o pacote
	 */
	private PacketType sendXMacPacket(USAMacStateType stateType) {

		USAMacPacket packet = null;

		switch (stateType) {

		case SENDING_RTS:
			packet = xState.getRtsPkt();
			break;

		case SENDING_CTS:
			// CTS em resposta a um RTS. Tem que ser criado, depois enviado para a PHY
			packet = createCtrlPacket(xState.getRecPkt().getSender().getId(), PacketType.CTS,
					xState.getRecPkt().getRetryCount(), false, xConf.getLengthCTS(), xConf.getSignalStrength());
			break;

		case SENDING_ACK:
			/*
			 * ACK em resposta a um DATA. ACK tem que ser criado, depois enviado para a PHY
			 */
			packet = createCtrlPacket(xState.getRecPkt().getSender().getId(), PacketType.ACK,
					xState.getRecPkt().getRetryCount(), false, xConf.getLengthACK(), xConf.getSignalStrength());
			break;

		case SENDING_DATA:
			/* Pacote DATA a ser enviado para a PHY está guardado em xMacState. */
			packet = xState.getDataPkt();
			break;

		default:
			return PacketType.VOID;
		}

		sendPacket(packet); // Função da MacLayer
		return packet.getType();

	}

	/**
	 * Função que cria um WUC do tipo WucTimeOut definindo a duração do novo estado
	 * e operação do rádio.
	 */
	USAMacWucTimeOut createtWucTimeOut() {
		USAMacWucTimeOut wuc = new USAMacWucTimeOut(sender, xState.getStateDuration(), xState.getStateSeqNum());
		sendEventSelf(wuc);
		return wuc;
	}

	/** Função para criar pacotes de controle (RTS, CTS, ACK) */
	private USAMacPacket createCtrlPacket(NodeId receiver, PacketType type, int retryCount, boolean ACKreq,
			int headerLength, double signalStrength) {
		USAMacPacket newPack = new USAMacPacket(this.sender, receiver, type, signalStrength);
		// Tamanho do pacote em bits
		newPack.setHeaderLength(headerLength);
		// Número de tentativas de estabelecer comunicação
		newPack.setRetryCount(retryCount);
		return newPack;
	}

	/** Função para criar pacotes do tipo DATA */
	private USAMacPacket createDataPacket(LogLinkPacket packet, boolean ACKreq) {
		USAMacPacket newPacket;
		if (BackboneConfigurationManager.getInstance(USAMAC_CONFIG).amIBackbone(node.getId())) {
			NodeId nextBB = BackboneConfigurationManager.getInstance(USAMAC_CONFIG).getNextBackboneNode(node.getId());
			newPacket = new USAMacBackbonePacket(this.sender, packet, ACKreq, nextBB, xConf.getCycleSyncTimingRatio());
		} else {
			newPacket = new USAMacPacket(this.sender, packet, ACKreq);
		}
		// Tamanho do pacote em bits
		newPacket.setHeaderLength(xConf.getLengthDATA());
		// Número de tentativas para enviar DATA
		newPacket.setRetryCount(xConf.getMaxSendRetries());
		return newPacket;
	}

	/** Apenas para manter compatibilidade com Shox. */
	@Override
	public MACState getState() {
		return this.macState;
	}

}
