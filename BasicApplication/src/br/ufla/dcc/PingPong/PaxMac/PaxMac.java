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

package br.ufla.dcc.PingPong.PaxMac;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.ToolsStatisticsSimulation;
import br.ufla.dcc.PingPong.Phy.ColisionDetectEvent;
import br.ufla.dcc.PingPong.Phy.EventCarrierSense;
import br.ufla.dcc.PingPong.Phy.EventPhyTurnRadio;
import br.ufla.dcc.PingPong.Phy.StartOfFrameDelimiter;
import br.ufla.dcc.grubix.simulator.node.Link;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.MACState;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.SendingTerminated;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 *  Protocolo PAX-MAC
 *
 *  @author Gustavo Araújo
 *  @version 03/11/2016
 */

public class PaxMac extends MACLayer {
	/** Tempo de ciclo em segundos */
	@ShoXParameter(description = " Tempo do ciclo de trabalho em segundos")
	private double cycleTime;
	
	/** Tamanho da mensagem de dados em % do tempo de ciclo */
	@ShoXParameter(description = " Tamanho da mensagem de dados em relação ao tempo de ciclo")
	private double dataSizeRatio;
	
	/** Manter a posse do dado (em % do tempo de transmissão de dado) */
	@ShoXParameter(description = " Manter a posse do dado por quantos períodos")
	private double holdDataTime;
	
	/** Limite de vezes que será enviado uma sequência completa de RTS */
	@ShoXParameter(description = " Limite de vezes para enviar uma sequência completa de RTS")
	private int maxRtsSequences;
	
	/** Mínimo multiplicador usado no recalculo do FCS (Quando hop=4) */
	@ShoXParameter(description = " Desaceleração para o FCS variável")
	private double fcsMinMultiplier;
	
	/** Máximo multiplicador usado no recalculo do FCS (Quando hop=1) */
	@ShoXParameter(description = " Multiplicador usado no recalculo do FCS")
	private double fcsMaxMultiplier;
	
	/** Objeto responsável pela mudança do estado do PAXMac */
    private PaxMacStateMachine paxSMachine;

    /** Objeto que contém todas as variáveis de configuração do PAXMac */
    private PaxMacConfiguration paxConf;

    /** Objeto que armazenas as informações de estados do PAXMac */
    private PaxMacState paxState;

    /** Informação, mantida na MAC, sobre o estado em que o rádio deve estar */
    private PaxMacRadioState paxRadioState;
    
    /** The internal state of the MAC that can be modified from outside.
     * Apenas para manter compatibilidade com Shox */ 
    private MACState macState;

    /** Ferramentas para imprimir informações para a depuração do programa */
 	ToolsDebug debug = ToolsDebug.getInstance();
    
 	/** Ferramentas para guardar e imprimir informações sobre as estatísticas da simulação */
 	ToolsStatisticsSimulation statistics = ToolsStatisticsSimulation.getInstance();
 	
 	/** Ferramentas auxiliares diversas */
 	ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();
    
    /**
     * Inicializa a configuração do objeto
     * @throws ConfigurationException
     *      thrown when the object cannot run with the configured values.
     */
    public void initConfiguration(Configuration config) throws ConfigurationException {
    	super.initConfiguration(config);
        
        /* Declaração obrigatória no Grubix (MAC_IEEE802_11bg_AdHoc)
         * Param1: Política de encaminhamento
         * Param2: Potência máximo de transmissão
         * Param3: Número de pacotes a serem transmitidos em uma fila */
        macState = new MACState(raDefaultPolicy, 16.0, 0);

        // Define o estado inicial do MAC em SLEEP e o contador de sequência de estados em zero.
        paxState = new PaxMacState(sender.getId(), PaxMacStateTypes.SLEEP, 0, node.getNeighbors());         

        // Define o estado inicial do rádio
        paxRadioState = new PaxMacRadioState(RadioState.OFF);

        // Inicializa as variáveis de configuração
        paxConf = new PaxMacConfiguration(cycleTime, dataSizeRatio, holdDataTime, maxRtsSequences, 
        		fcsMinMultiplier, fcsMaxMultiplier);
        
        // Cria a instância da máquina de estados
        paxSMachine = new PaxMacStateMachine(sender, paxState, paxRadioState, paxConf);
        
        // Ferramentas de depuração -------------------------
        if (statistics.paxConf == null) {
        	debug.print(debug.strPaxConf(paxConf), sender);
    		statistics.paxConf = paxConf;
        }
        // --------------------------------------------------
    }
    

    /**
     *  Função para iniciar o funcionamento do nó sensor.
     *  Marcar um tempo aleatório para ligar o rádio pela primeira vez.
     */
    protected void processEvent(StartSimulation start) {
    	// Define o tamanho do grupos FCS padrão
    	paxState.setFcsSize(paxConf.getFcsSize());
    	// Define o novo estado e duração, e incrementa a sequência de estado
    	paxSMachine.changeStateBootNode();
    	createtWucTimeOut();
    }
    
    
    /**
     *  Função que cria um WUC definindo a duração do novo estado e operação do rádio.
     */
    PaxMacWucTimeOut createtWucTimeOut() {
    	// Cria evento para camada física desligar ou ligar rádio baseado no estado atual
    	setRadioOperation();
  
    	switch (paxState.getState()) {
        case CS_SHORT:
        	/* Envia evento EventCarrierSense para camada física para obter resposta sobre a 
        	 * disponibilidade do canal naquele momento. Usado antes de enviar um CTS/RTS */
			sendEventDown(new EventCarrierSense(sender));
			break;
			
		case SENDING_MSG:
				sendPAXMacPacket();
			break;
			
		default:
			break;
		}
    	
    	// Ferramentas de depuração -------------------------
    	if (paxState.getStateDuration() < 0) {
    		debug.print("!!![Erro] Duração do estado possui valor negativo", sender);
    		System.exit(1);
    	}
    	// --------------------------------------------------
    	
    	// Cria evento que após o tempo de delay será tratado pelo processWakeUpCall()
    	PaxMacWucTimeOut wuc = new PaxMacWucTimeOut(
    			sender, paxState.getStateDuration(), paxState.getStateSeqNum());
		// Evento enviado para a própria camada
		sendEventSelf(wuc);
		return wuc;
    }
    
    
    /**
     *  Função para tratar o final dos eventos WUC do tipo WucTimeOut. Chama a função changeStateTimeOut 
     *  da PAXMacStateMachine para indicar qual será o próximo estado da PAX-MAC.
     */
    public final void proceedWucTimeOut(PaxMacWucTimeOut TimeOut) {
    	// Chama a PAXMacStateMachine para definir qual o próximo estado e a duração (delay)
        if (!paxSMachine.changeStateTimeOut(TimeOut))
        	return;

        // Inicia o WUC do novo estado
        createtWucTimeOut();  
    }
    
    
    /**
     *  Função padrão do Grubix para tratar eventos WakeUpCall
     *   Tipos de WakeUpCall:
     *  --> WucTimeOut = marcação de tempo do estados do nó referente a camada MAC.
     *  --> CrossLayerEvent = eventos emitidos pela Physical Layer para a MAC.
     */
    public final void processWakeUpCall(WakeUpCall wuc) {
    	/* Não grava mais a saída do log se todos os dados chegaram ao destino, ou os fluxos atingiram 
    	 * o limite de envio de RTS ou o tempo para finalizar a simulação é menor que um ciclo */
    	if (paxState.remainTimeSteps() < paxConf.getStepsCycle() || statistics.isFinalizeSimulation()) {
    		// Imprime se ainda não imprimiu as estatísticas
    		if (!statistics.printedStatistics) {
    			statistics.printStatistics();
        		statistics.printShortStatistics();
        		statistics.printedStatistics = true;
    		}
    		return;
    	}
    	
    	// Tratamento de evento de fim de marcação de tempo
        if (wuc instanceof PaxMacWucTimeOut) {
        	proceedWucTimeOut((PaxMacWucTimeOut)wuc);
        }
        
        // Tratamento dos eventos emitidos pela PHY.
        if (wuc instanceof CrossLayerEvent) {
            proceedCrossLayerEvent((CrossLayerEvent)wuc);
        }
    }
	
    
	/**
	* Função padrão do GrubiX para tratar pacotes recebidos da camada superior (LogLink).
	* Após receber um pacote da LogLink, é chamada a função de início de transmissão de pacote.
	*/
	public final void upperSAP(Packet packet) {
		// Ferramentas de depuração -------------------------
		debug.print("[Dado-LLC] Mensagem recebida da camada LogLinkControl", sender);
		// --------------------------------------------------
		
		// Se o pacote é para o próprio nó, manda de volta para a camada acima
		if (packet.getReceiver() == id) {
			// Muda a direção do pacote para que seja enviado para cima
			packet.flipDirection();
			sendPacket(packet);
		} else {
			sendDataPacketDown(packet);
		}
	}
	
	
    /**
     *  Função padrão do GrubiX para tratar pacotes recebidos da camada inferior (Física).
     *  Após a camada física terminar de receber uma mensagem pelo rádio, ela envia para a MAC.
     */
    public final void lowerSAP(Packet packet) {
        if (!(packet instanceof PaxMacPacket)) {
        	// Ferramentas de depuração -------------------------
        	debug.print("[!!!Msg-desconhecida] Mensagem desconhecida: "+packet.getClass().getName(), sender);
        	// --------------------------------------------------
        	return;
        }

        // Mudar o estado conforme o tipo de mensagem recebida
        paxSMachine.changeStateReceivedMsg((PaxMacPacket) packet);
        createtWucTimeOut();
    }
    
    
	/**
     *  Inicia o processo de envio de um pacote de dado recebido da camada LogLink (superior).
     *  Depois devem ser seguidos os passos:
     *  1) Construir uma mensagem de dados contendo o pacote recebido da LogLink;
     *  2) Colocar o número de sequência nesta mensagem;
     *  3) Chamar a função startSendDataProcess;
     */
    private boolean sendDataPacketDown(Packet packet) {
        // Cria o pacote de dados e definindo o destino final (se existir será sobrescrito)
        PaxMacPacket newDataPacket = createDataPacket(packet, true);
        paxState.setFinalReceiverNode(newDataPacket.getFinalReceiverNode());
        
        // Se é o primeiro nó do caminho, os dois saltos anteriores serão o próprio nó
        if (paxState.getPreviousReceiverLv1() == null && paxState.getPreviousReceiverLv2() == null) {
            paxState.setPreviousReceiverLv1(sender.getId());
            paxState.setPreviousReceiverLv2(sender.getId());
            paxState.setPreviousReceiverLv3(sender.getId());
            paxState.setCtsDataTime(paxState.currentTimeSteps() + paxConf.getStepsKeepData());
            
            // Ferramentas de depuração -------------------------
            debug.print("[Dado-atributos] Encapsulando pacote do nó fonte", sender);
            misc.vGrubix(sender.getId(), "Recebeu RTS", "BLUE");
            misc.vGrubix(sender.getId(), "Recebeu o dado", "DARK_BLUE");
            // --------------------------------------------------
        }
        
        // Atribui o pacote que será enviado
        paxState.setDataPkt(newDataPacket);

        /* Se o nó não sabe quem é o próximo no do salto, inicia o envio da sequência de RTS. Caso
         * contrário envia o dado. */
 		if (paxState.getNextReceiverNode() == null) {
 			paxState.setSendingPktType(PacketType.RTS);
 		} else {
 			paxState.setSendingPktType(PacketType.DATA);
 		}

 		// Mudança de estado para preparar o envio da mensagem
 		paxSMachine.changeStateSendingMsg();
 		
 		createtWucTimeOut();	
        return true; 
    }

    
    /**
     *  Função para enviar o pacote
     */
    protected boolean sendPAXMacPacket() {
    	// Se não tem definido qual mensagem será enviada, sai sa função
    	if (paxState.getSendingPktType() == null) {
    		return false;
    	}
  
    	PaxMacPacket packet = null;
    	
    	switch (paxState.getSendingPktType()) {
    	case RTS:
    		/* Obtém o FCS baseado no tempo de transmissão do dado */
    		paxState.setFcsSize(paxConf.fcsSizeBasedOnDataTime(paxSMachine.delayUntilCtsData()));
    		List <NodeId> fcs = fcsNodes(paxState.getFinalReceiverNode(), paxState.getFcsSize());
    		
    		/* Cria o RTS se ainda não criou nenhum. Caso contrário somente atualiza */
    		if (paxState.getRtsPkt() == null) {
    			packet = createRtsPacket(sender, paxState.getFinalReceiverNode(), fcs,
    					paxState.getPreviousReceiverLv1(), paxState.getPreviousReceiverLv2(), 
    					paxState.getPreviousReceiverLv3(),paxSMachine.delayUntilCtsDataNextHop(), 
    					paxConf.getSignalStrength(), paxConf.getLengthRTS(), paxConf.getMaxPreambles(), false);
    			paxState.setRtsPkt(packet);
    			
    			// Ferramentas de depuração -------------------------
        		debug.print("@@@[RTS-enviando] RTS criado.\n"+debug.prtPkt(packet, paxConf), sender);
    			// --------------------------------------------------
    		} else {
    			packet = paxState.getRtsPkt();
    			packet.setSendCtsDataDelay(paxSMachine.delayUntilCtsDataNextHop());
    			packet.setFcsNodes(fcs);
    			packet.setPreviousReceiverNodeLv1(paxState.getPreviousReceiverLv1());
    			/* Decrementa a contagem de preâmbulos que ainda poderão ser enviados */
    			packet.decSequenceNum();
    			
    			// Ferramentas de depuração -------------------------
        		debug.print("@@@[RTS-enviando] RTS atualizado.\n"+debug.prtPkt(packet, paxConf), sender);
        		// --------------------------------------------------
    		}
    		
    		// Ferramentas de depuração -------------------------
        	statistics.getTransmission(paxState.getFinalReceiverNode()).setStartTime();
    		statistics.getTransmission(packet).addFcsSize(sender.getId(), fcs.size());
			statistics.getTransmission(packet).addRtsSentStartTime(sender.getId());
    		statistics.getTransmission(packet).addRtsSent(sender.getId());
    		
    		// --------------------------------------------------
    		break;
    			
        case DATA:
        	packet = paxState.getDataPkt();
        	packet.setReceiver(paxState.getNextReceiverNode());
        	packet.setPreviousReceiverNodeLv1(paxState.getPreviousReceiverLv1());
        	
        	// Ferramentas de depuração -------------------------
        	debug.print("@@@[Dado-enviando] Enviando dado.\n"+
        			"[Dado] "+debug.prtPkt(packet, paxConf), sender);
        	// --------------------------------------------------
        	break;

    	case CTS: 
        	packet = createReceiptPacket(sender, paxState.getFinalReceiverNode(), 
        			paxState.getPreviousReceiverLv1(), paxConf.getSignalStrength(), PacketType.CTS, 
        			paxConf.getLengthCTS(), false);
        	
        	// Ferramentas de depuração -------------------------
        	debug.print("@@@[CTS-enviando] Enviando CTS.\n"+
        			"[CTS] "+debug.prtPkt(packet, paxConf), sender);
        	// --------------------------------------------------
        	break;
        
    	case CTS_DATA: 
        	packet = createReceiptPacket(sender, paxState.getFinalReceiverNode(), 
        			paxState.getPreviousReceiverLv1(), paxConf.getSignalStrength(), PacketType.CTS_DATA, 
        			paxConf.getLengthCTSD(), false);
        	
        	// Ferramentas de depuração -------------------------
        	debug.print("@@@[CTS-DATA-enviando] Enviando CTS-DATA.\n"+
        			"[CTS-DATA] "+debug.prtPkt(packet, paxConf), sender);
        	// --------------------------------------------------
        	break;
        	
        case ACK: 
        	packet = createReceiptPacket(sender, paxState.getFinalReceiverNode(), 
        			paxState.getPreviousReceiverLv1(), paxConf.getSignalStrength(), PacketType.ACK, 
        			paxConf.getLengthACK(), false);
        	
        	// Ferramentas de depuração -------------------------
        	debug.print("@@@[ACK-enviando] Enviando ACK.\n"+
        			"[ACK] "+debug.prtPkt(packet, paxConf), sender);
        	// --------------------------------------------------
        	break;
        	
        default:
        	// Ferramentas de depuração -------------------------
        	debug.print("[!!!Msg-desconhecida] Mensagem de tipo desconhecida: "+
        			paxState.getSendingPktType(), sender);
        	// --------------------------------------------------
        	return false;
    	}
    	
    	/* Se o dado está com direção para camada acima, envia o pacote encapsulado  */
    	if (paxState.getSendingPktType() == PacketType.DATA && packet.getDirection() == Direction.UPWARDS) {
    		// Ferramentas de depuração -------------------------
    		statistics.getTransmission(packet).addNode(sender.getId());
    		debug.print("[Data-upwards] O dado será enviado para a camada acima (LLC)", sender);
    		// --------------------------------------------------
    		
    		/* Antes de enviar o dado para camada acima, muda o receiver para o destino final, para que
    		 * a camada de roteamento saiba para quem é a mensagem */
    		packet.setReceiver(paxState.getFinalReceiverNode());
    		sendPacket(packet.getEnclosedPacket());

    	} else {
    		sendPacket(packet);	
    	}
    	
    	// Após enviar uma mensagem define que não há mais mensagens para enviar
    	this.paxState.setSendingPktType(null);
    	return true;
    }
    
    
    /**
     *  Função para tratar eventos vindos da camada física.
     *  Existem 3 eventos previstos:
     *  -> EventCarrierSense: Camada física sinaliza que o canal esta ocupado
     *  -> SendingTerminated: Camada física sinaliza o final da transmissão de uma mensagem
     *  -> StartOfFrameDelimiter: No início de uma mensagem de rádio é colocado o sinalizador 
     *  		SFD (start frame delimiter). A camada física sinaliza a recepção do SFD
     *  -> ColisionDetectEvent: Camada física sinaliza a ocorrência de colisão de mensagens recebidas 
     *  		pelo rádio. Em SendingTerminated o pacote enviado pela MAC para a PHY é retornado 
     *  		para a MAC, para conferência. A rigor, o SFD não traz nenhuma informação da mensagem.
     *  		A MAC deveria apenas esperar o final da recepção. O envio do pacote juntamente com 
     *  		o sinal SFD, aqui, é utilizado apenas para facilitar o uso do temporizador de espera 
     *  		de MSG. ColisionDetectEvent é utilizado para encerrar a espera de um pacote que 
     *  		estava sendo recebido pela PHY.
     */
    public final void proceedCrossLayerEvent(CrossLayerEvent cle) {
    	// A seguir somente será permitido eventos que carregam pacotes do tipo PAXMacPacket
        if (!(cle instanceof EventCarrierSense) && !(cle.getPacket() instanceof PaxMacPacket)) {
        	String className = cle.getPacket() != null ? cle.getPacket().getClass().getName() : "vazio";
        	
        	// Ferramentas de depuração -------------------------
        	debug.print("[!!!Msg-desconhecida] Mensagem desconhecida: "+className, sender);
        	// --------------------------------------------------
            return;
        }
    	
    	// Recebe evento da PHY informando se o canal está ocupado
        if (cle instanceof EventCarrierSense) {
        	if (((EventCarrierSense) cle).isChannelBusy()) { 
        		paxState.setChannelBusy(true);      
        	} else {
        		paxState.setChannelBusy(false);
        	}
        }
        
    	PaxMacPacket packet = (PaxMacPacket)cle.getPacket();
	
    	// Recebe evento da camada física quando uma mensagem é completamente enviada.
        if (cle instanceof SendingTerminated) {  	
        	paxSMachine.changeStateSentMsg(packet);
            createtWucTimeOut();
        }
              
        /* Como no simulador as mensagens são transmitidas sem considerar o tempo de transmissão,
         * será simulado um comportamento em que o nó começaria a receber o início de uma mensagem e 
         * então ficaria no mesmo estado até o fim da transmissão para então saber qual a mensagem e se
         * ela é destinado à ele */
        if (cle instanceof StartOfFrameDelimiter) {
            if (paxSMachine.changeStateReceivingMsg(packet)) {
                createtWucTimeOut();
            }
        }
        
        // Recebe evento da camada física quando ocorre uma colisão na recepção de uma mensagem
        if (cle instanceof ColisionDetectEvent) {

            /* Desliga o rádio, pois aqui o estado do rádio da camada física estará em RECEIVE e
        	 * se a camada MAC enviar uma mensagem antes do nó dormir (desligar o rádio), o nó 
             * irá enviar uma mensagem no estado RECEIVE e será barrado pela upperSap da PhysicalSimple */
        	sendEventDown(new EventPhyTurnRadio(this.sender, false));

        	// Ferramentas de depuração -------------------------
        	debug.print("[!!!Colisão] Colisão informada pela camada física. Mensagem vinda do nó: "+
        			cle.getPacket().getSender().getId() + ". Estado: "+paxState.getState(), sender);
        	misc.vGrubix(sender.getId(), "Colisao", "RED");
        	// --------------------------------------------------
        	
        	if (paxSMachine.changeStateCollision()) {
        		createtWucTimeOut();
        	}
        }
    }
    
    
    /** 
     * Função para criar pacotes de dados que serão enviados por unicast 
     */
    PaxMacPacket createDataPacket(Packet packet, boolean ACKreq) {
    	/* Objeto LogLink para pegar o valor da potência de transmissão que será usada pela camada 
    	 * superior LogLinkLayer, enquanto não for usado outro método para definir SignalStrength */
    	LogLinkPacket llp = (LogLinkPacket) packet;
    	Link link = llp.getMetaInfos().getDownwardsLLCMetaInfo().getLink();
    	paxConf.setSignalStrength(link.getTransmissionPower());
    	/* Esquema de contagem cíclica para não criar sequência infinita
    	 * Número de sequência da mensagem a ser enviada, limitado em 2^16 */
    	int sequence = (paxState.getDataSeqNum() + 1) & paxConf.getMaxSequence();
    	// Cria o pacote
    	PaxMacPacket newPacket = new PaxMacPacket(sender, llp, sequence) ;
        newPacket.setHeaderLength(paxConf.getLengthDATA());
        // Se ACK deve ser enviado depois que o dado chegar no receptor
        newPacket.setAckRequested(ACKreq);
        newPacket.setFinalReceiverNode(packet.getEnclosedPacket(LayerType.APPLICATION).getReceiver());
    	return newPacket;
    }
    
    
    /** 
     * Função para criar pacotes de aviso de recebimento (CTS, ACK) que serão enviados por unicast 
     */
    PaxMacPacket createReceiptPacket(Address sender, NodeId finalReceiver, NodeId receiver, 
    		double signalStrength, PacketType type, int headerLength, boolean ACKreq) {
    	PaxMacPacket newPack = new PaxMacPacket(sender, receiver, signalStrength, type);
    	newPack.setAckRequested(ACKreq);
    	newPack.setHeaderLength(headerLength);
    	newPack.setFinalReceiverNode(finalReceiver);
    	return newPack;
    }
    
 
    /** 
     * Função para criar pacotes RTS que serão enviados por anycast 
     */
    PaxMacPacket createRtsPacket(Address sender, NodeId finalReceiver, List <NodeId> fcs,
    		NodeId previousReceiverLv1, NodeId previousReceiverLv2, NodeId previousReceiverLv3, 
    		double dataTime, double signalStrength, int headerLength, int sequence, boolean ACKreq) {
    	PaxMacPacket newPack = new PaxMacPacket(sender, fcs, dataTime, signalStrength, sequence);
    	newPack.setAckRequested(ACKreq);
    	newPack.setHeaderLength(headerLength);
    	newPack.setFinalReceiverNode(finalReceiver);
    	newPack.setPreviousReceiverNodeLv1(previousReceiverLv1);
    	newPack.setPreviousReceiverNodeLv2(previousReceiverLv2);
    	newPack.setPreviousReceiverNodeLv3(previousReceiverLv3);
    	return newPack;
    }
    
    
    /** 
     * Função para definir o estado do rádio e enviar evento para a camada física ligar ou desligar o 
     * rádio. Ligar = true; Desligar = false. 
     */
    protected void setRadioOperation() {
    	// Define qual deverá ser o estado do rádio.
    	paxSMachine.changeRadioState();
    	// Atribui o novo estado do rádio na camada física
    	boolean isTurnOn = true;
    	if (paxRadioState.getRadioState() == RadioState.OFF) 
    		isTurnOn = false;
        sendEventDown(new EventPhyTurnRadio(this.sender, isTurnOn));
    }
    
    
    /** 
     * Função que retorna a lista de nós vizinhos com avanço positivo até o destino, ordenado pela 
     * distância até o destino 
     */
	protected List <NodeDistanceDestination> advancedNodes(NodeId destinationId) {
    	List <NodeDistanceDestination> nodesDistance = new ArrayList<NodeDistanceDestination>();
    	Node destination = SimulationManager.getInstance().queryNodeById(destinationId);
    	
    	double senderDistance = node.getPosition().getDistance(destination.getPosition());
    	// Criar a lista formada pelo nó e sua respectiva distância até o destino 
    	for (Node neighbor : paxState.getNeighbors()) {
			Position position = neighbor.getPosition();
			double nodeDistance = position.getDistance(destination.getPosition());
			
			// Se distância for maior que a do nó atual (sender) não adiciona na lista
			if (nodeDistance < senderDistance)
				nodesDistance.add(new NodeDistanceDestination(neighbor, nodeDistance));
		}	
    	// Ordenar a lista baseado na distância dos nós
        Collections.sort(nodesDistance, new Comparator<NodeDistanceDestination>() {
            public int compare(NodeDistanceDestination o1, NodeDistanceDestination o2) {
                return o1.getDistance().compareTo(o2.getDistance());
            }
        });
    	return nodesDistance;
    }

	
	/** 
	 * Função que retorna a lista de nós que compõem o FCS 
	 */
	protected List <NodeId> fcsNodes(NodeId destinationId, int fcsSize) {
		List <NodeDistanceDestination> nodesDistance = advancedNodes(destinationId);
		List <NodeId> nodesFcs = new ArrayList<NodeId>();

		boolean haveDestination = false;
		int i=fcsSize;
		for (NodeDistanceDestination nd : nodesDistance) {
			// Se o destino final está no raio de alcance, o FCS conterá apenas o destino
			if (nd.getNode().getId().equals(destinationId)) {
				haveDestination = true;
			}
			if (i > 0) {
				nodesFcs.add(nd.getNode().getId());
				i--;
			}	
		}
		return haveDestination ? new ArrayList<NodeId>(Arrays.asList(destinationId)) : nodesFcs;	
	}
	
    
    /** Apenas para manter compatibilidade com Shox. */
    @Override
    public MACState getState() {
        return this.macState;
    }
    
}



/* TODO
- Verificar se ao receber o dado atras durante uma espera por cts não vai achar que é um cts
- Toda vez que o nó dorme durante o envio de um RTS, quando acorda faz um CS RECEIVE para ver se o
canal está livre de outra transmissão paralela, evitando colisão após acordar
- Verificar o nó esperar o dado chegar ao enviar seq completa de RTS

- NAO PRECISA MAIS: Quando há colisão quando estava esperando um CTS (nó A), envia um RTS imediatamente para que o
próximo nó (B) durante sua primeira escuta de CTS para o envio de RTS, se o nó B também teve colisão
é porque o proximo no C respondeu o primeiro RTS, o processo continua até não ter mais colisão. Não
haverá mais risco de caminho duplo.


 */


/* ******* Alterações em relação a versão original **********
- Evitar criar dois caminhos quando dois nós de um FCS respondem à um mesmo RTS e estes não se ouvem.
	Nesse caso irá causar colisão no nó em enviou o RTS e como ele não entende a mensagem dos dois nós
	do FCS, ele então continua a mandar RTS. Se estes nós do FCS que retornaram o CTS/RTS ouvir novamente
	um RTS do nó anterior, eles apagam as informações obtidas o voltam a dormir por tempo aleatório. Para
	que apenas um desses nós recebam um RTS posteriormente ou outro nó não escolhido anteriormente do FCS
	responda ao RTS.
*/


/* ******* Pontos fracos **********

 */
