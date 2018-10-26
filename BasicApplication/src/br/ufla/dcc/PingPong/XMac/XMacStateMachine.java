package br.ufla.dcc.PingPong.XMac;

import java.util.Random;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.node.AppPacket;
import br.ufla.dcc.PingPong.node.RadioState;
import br.ufla.dcc.PingPong.testing.SingleNodeDebugger;
import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;

/** Classe que define os estados do XMac e suas respectivas durações
 * 
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 22/06/2016
 *
 */
public class XMacStateMachine  {
	
	/** referência para xMacState */
    XMacState xState; 
    /** referência para XMacRadioState */
    XMacRadioState xRadioState;
    /** referência para XMacConfiguration */
    XMacConfiguration xConf;
    /** Endereço do nó atual */
    Address address;

    /** Objeto para realizar a depuração */
    ToolsDebug debug = ToolsDebug.getInstance();
    
    public SingleNodeDebugger nodeDebugger;
 	

    /**
	 * Default constructor 
	 */
    public XMacStateMachine(Address address, XMacState xmacState, XMacRadioState xradioState, 
    		XMacConfiguration xmacConfig) {
    	this.xState      = xmacState;
    	this.xRadioState = xradioState; 
    	this.xConf       = xmacConfig;
    	this.address     = address;
	}
		
    
    /** Função que faz a primeira mudança de estado do XMac. O nó irá entrar em modo SLEEP por um
     tempo aleatório, antes de iniciar suas atividades */ 
    public void changeStateBootNode() {
    	// Se for necessário testar com seed fixa para cada nó
    	//Random gerador = new Random(address.getId().asInt());
        //int delay = gerador.nextInt(Integer.valueOf((int) Math.round(xConf.getStepsCycle())));
        
    	// Tempo pequeno aleatório em steps para ligar o nó sensor
        double delay = ((int) (Math.random() * (xConf.getStepsCycle())));
    	xState.setState(XMacStateTypes.SLEEP, delay);
        xRadioState.setRadioState(RadioState.OFF);
    }
    
    
    /** Função que muda o estado do XMac e do radio quando o tempo do Wuc da XMac chega ao fim. 
     * Ao ser chamada, verifica qual o estado atual, para então mudar o estado e definir o tempo 
     * previsto para o estado atual.
     */
    public boolean changeStateTimeOut(WakeUpCall wuc) {
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.writeIfNodes(debug.str("Estado Antes")+debug.strXLayer(xState, xRadioState)+
    				debug.strWuc(wuc), address, new Integer[]{1});
    	
        /* Verifica se o número do estado armazeno no WUC é o mesmo do estado atual. Se não for,
         já houve mudança no estado do XMac antes do término do WUC, portanto esse WUC deve ser
         ignorado, já que não é mais válido. */
		if (xState.getStateSeqNum() != ((XMacWucTimeOut)wuc).getStateNumber()) {
			debug.write(debug.str("AVISO! Número do estado não corresponde ao número do WUC"), address);
			return false;        
		}
		
		/* Se rádio está OFF e estado do XMac não é SLEEP, não prosseguir. Isso pode ocorrer se 
		durante um CS_START receber um DATA indesejado */
        if((xRadioState.getRadioState() == RadioState.OFF) && 
        		(xState.getState() != XMacStateTypes.SLEEP)) {
        	debug.printw("AVISO! Estado do radio: OFF e XMac: "+xState.getState(), address);
        	return false;
        }
    	
    	switch (xState.getState()) {
    	case CS:
    		/* Se venceu o estado que verifica se algum vizinho está transmitindo alguma mensagem 
    		 para o nó. Se não, volta a dormir depois do tempo de CS */
            xState.setState(XMacStateTypes.SLEEP, xConf.getStepsSleep());
            xRadioState.setRadioState(RadioState.OFF); 
            SingletonTestResult.getInstance().countCS();
            nodeDebugger.countCS();
            
            break;
            
    	case SLEEP:
    		/* Se possui um pacote na XMacState e ele está direcionado para baixo, recomeça o 
    		 envio do RTS. Caso o pacote seja para a camada acima, ou não tenha pacotes armazenados,
    		 fica em CS */
    		if (xState.getDataPkt() != null && 
    				xState.getDataPkt().getDirection() == Direction.DOWNWARDS) { 
            	/* Quando ocorre uma falha no envio de MSG, voltará aqui. Indica para a XMAC 
            	 que deverá reiniciar o processo de envio de preâmbulos */
            	xState.setRestartRTS(true);	
            } else {
            	xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
                xRadioState.setRadioState(RadioState.LISTENING);
            }
    		break;
    		
    	case CS_START:
            /* Se venceu o estado que verifica se o canal está livre para iniciar a transmissão do RTS.
             Agora em SENDING_MSG, a MAC aguarda um aviso vindo da PHY sobre o final da transmissão do
             RTS. Funciona como Watch-dog. 
             SendingPktType já foi definido na XMac em startSendDataProcess() */
    		if (xState.isChannelBusy()) {
        		/* Se o canal de comunicação estivesse ocupado, e não recebeu um evento de 
        		 StartOfFrameDelimiter ou PACKET_RECEIVED é porque a mensagem era ACK (de DATA) que 
        		 já terminou, e o canal está livre, ou é DATA que está sendo transmitido. De qualquer
        		 forma, deve fazer um BackOff em Sleep. */
        		xState.setState(XMacStateTypes.SLEEP, xConf.getStepsSleep() + xConf.getStepsEndTx());
        		xRadioState.setRadioState(RadioState.OFF);
        		break;
        	}
    		xState.setState(XMacStateTypes.SENDING_MSG, xConf.stepsDelayTx(PacketType.RTS));
            xRadioState.setRadioState(RadioState.SENDING);
            //SingletonTestResult.getInstance().countCS();
            break;
            
    	case SENDING_MSG:  
    		/* MAC está esperando o rádio terminar de enviar uma mensagem, a PHY sinalizará o término 
    		  do envio. Se chegou aqui, é porque o rádio não enviou o pacote. Se o rádio enviasse, ia 
    		  para proceedCrossLayerEvent(), depois para proceedRadioSent().
    		  Vai para CS e depois para SLEEP, ao acordar verifica se ainda deve mandar a msg novamente
    		 */
            xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
            xRadioState.setRadioState(RadioState.LISTENING);
            break;
            
    	case WAITING_CTS: 
        	// Decrementa o número da sequência de RTS e obtém o novo valor da sequência
    		int rtsSeqNum = xState.getRtsPkt().decSeqNum();
        	/* Esperava um CTS depois de enviar um RTS, mas CTS não chegou. Ainda não terminou a
        	 sequência de RTS. Sinaliza o envio do próximo RTS */
            if (rtsSeqNum > 0) {
            	// Vai avisar a XMAC que tem um pacote RTS a ser enviado em seguida
                xState.setSendingPktType(PacketType.RTS);
                xState.setState(XMacStateTypes.SENDING_MSG, xConf.stepsDelayTx(PacketType.RTS));
                xRadioState.setRadioState(RadioState.SENDING);
                break;
            } 
            
            // Fim da sequência de RTS, não é mensagem BroadCast e nó de destino não respondeu
            if (xState.getRtsPkt().getReceiver() != NodeId.ALLNODES) {
                // Irá para CS e depois para SLEEP.
                xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
                xRadioState.setRadioState(RadioState.LISTENING);
            } else {
	            /* Fim da sequência de RTS, mensagem BroadCast, todos os vizinhos estão acordados.
	             * Logo pode enviar a mensagem. */
	            xState.setSendingPktType(PacketType.DATA);
	            xState.setState(XMacStateTypes.SENDING_MSG, xConf.stepsDelayTx(PacketType.DATA));
	            xRadioState.setRadioState(RadioState.SENDING);
            }
            break;
            
        case WAITING_DATA: 
        	/* Acabou o tempo e não recebeu o pacote de dados, então o próximo estado será CS, 
        	 * pois talvez outro nó queira transmitir uma mensagem*/
            xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
            xRadioState.setRadioState(RadioState.LISTENING);
            break;
    	
        case WAITING_ACK: 
        	/* Esperava um ACK depois de enviar DATA, mas ACK não chegou. Vai para CS e depois 
        	 para sleep, ao acordar verifica se ainda deve mandar a mensagem novamente */
            xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
            xRadioState.setRadioState(RadioState.LISTENING);  
            break;
            
        default:
        	debug.printw("AVISO! Estado desconhecido"+xState.getState(), address);
    		break;
    	}
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.writeIfNodes(debug.str("Estado Depois")+debug.strXLayer(xState, xRadioState)+
    				debug.strWuc(wuc), address, new Integer[]{1});
    	return true;
    }
    
    
    /** Função que muda o estado do XMac e do radio quando uma mensagem foi enviada pelo rádio. 
     * Ao ser chamada, verifica qual o tipo de mensagem, para então mudar o estado e definir 
     * o tempo previsto para o estado atual.
     */
    public void changeStateSentMsg(XMacPacket packet) {
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Antes")+debug.strXLayer(xState, xRadioState)+
				debug.strPaxPkt(packet), address);
    	
    	switch (packet.getType()) {
    	case RTS:
            xState.setState(XMacStateTypes.WAITING_CTS, xConf.getStepsCTS());
            // Vai ouvir o canal para esperar o CTS
            xRadioState.setRadioState(RadioState.LISTENING);
            debug.write(debug.strPaxPkt(packet), address);
            SingletonTestResult.getInstance().countPreamble();
            break;
            
    	case CTS:
            xState.setState(XMacStateTypes.WAITING_DATA, xConf.getStepsDATA());
            // Vai ouvir o canal para esperar o DATA
            xRadioState.setRadioState(RadioState.LISTENING);
            break;
        
    	case DATA:
            // Se precisará enviar ACK
            if (xState.getDataPkt().isAckRequested()) {
                // Tempo de espera por um ACK
                xState.setState(XMacStateTypes.WAITING_ACK, xConf.getStepsACK());
                // Vai esperar um ACK de DATA.
                xRadioState.setRadioState(RadioState.LISTENING);
              // Se não precisa enviar ACK
            } else {
            	// Pacote de DATA é removido, pois já enviou o DATA e não precisa enviar ACK
                xState.setDataPkt(null);
                xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
                xRadioState.setRadioState(RadioState.LISTENING);
            }
            break;
        
    	case ACK: 
    		// Acabou de enviar o ACK após receber DATA
            xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
            // Verificar se há outra mensagem.
            xRadioState.setRadioState(RadioState.LISTENING);
            /* Depois de enviar a mensagem ACK, envie a mensagem de DATA para a camada superior.
             Avisa a XMac que tem um pacote DATA a ser enviado para a LOGLINK em seguida.
             Como DATA veio da camada de baixo, a direção é do pacote é UPWARDS */
            xState.setSendingPktType(PacketType.DATA);
            break;
            
    	case NACK:
        case CONTROL:
        case VOID: 
    	default:
            xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
            xRadioState.setRadioState(RadioState.LISTENING);
            debug.printw("Mensagem desconhecida: "+packet.getType(), address);
    		break;
    	}
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Depois")+debug.strXLayer(xState, xRadioState)+
				debug.strPaxPkt(packet), address);
    }
    
    
    /**
     *  Função que processa as mensagem recebidas pelo rádio, quando o rádio acaba de receber uma 
     *  mensagem e a PHY a repassa para a MAC.
     *  Se a mensagem não é para este nó e ele está no estado CS_START, ele volta para CS_START pois 
     *  o envio de RTS's deverá ser adiado. Em outros estados quando a mensagem não era para este nó,
     *  não foi considerado.
     */
    public void changeStateReceivedMsg(XMacPacket packet) {
    	
    	
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Antes")+debug.strXLayer(xState, xRadioState)+
				debug.strPaxPkt(packet), address);
    	
    	// Se acabou de receber uma mensagem, então o canal de rádio agora está livre.
    	xState.setChannelBusy(false);
    	
    	// Se a mensagem é destinado para outro nó
        if ((packet.getReceiver().asInt() != address.getId().asInt()) && 
        		(packet.getReceiver() != NodeId.ALLNODES)) {    	
        	if ((xState.getState() == XMacStateTypes.CS_START) &&
                    ((packet.getType() == PacketType.RTS) || (packet.getType() == PacketType.CTS))) {
                double delay = packet.getSequenceNumber()*(xConf.getStepsRTS() + xConf.getStepsACK());
                xState.setState(XMacStateTypes.SLEEP, delay);
                xRadioState.setRadioState(RadioState.OFF);
        	}
        	// Debug, escreve entrada no arquivo de depuração
        	debug.write(debug.str("Estado Depois")+debug.strXLayer(xState, xRadioState)+
    				debug.strPaxPkt(packet), address);
        	/* Nos demais casos, ignora a chegada de mensagem destinada a outro nó, e aguarda o final 
            da WUC em curso */
        	return; 
        }
    	
    	switch (packet.getType()) {
    	case RTS:
    		/* Se estivesse em CS_START, vai receber a mensagem que chegou, entra em CS e vai
    		 verificar que existe um DATA pendente para ser enviado */
            if ((xState.getState() == XMacStateTypes.CS) || 
            		(xState.getState() == XMacStateTypes.CS_START)) {
                /* Se foi requisitado CTS, espera um tempo para enviar o CTS. Se for Broadcast não é 
				 requisitado o ACK */
                if (packet.isAckRequested()) {
                	// Vai avisar a XMac que tem um pacote CTS a ser enviado em seguida
                    xState.setSendingPktType(PacketType.CTS);
                	xState.setReceiverNode(packet.getSender().getId());
                	xState.setCtsSeqNum(packet.getSequenceNumber());
                    xState.setState(XMacStateTypes.SENDING_MSG, xConf.stepsDelayTx(PacketType.CTS));
                    xRadioState.setRadioState(RadioState.SENDING);
                /* Se não foi requisitado CTS então a mensagem é de Broadcast. Mantenha-se em CS até 
                 o último preâmbulo, depois do qual será enviado DATA */
                } else {
                    xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
                    xRadioState.setRadioState(RadioState.LISTENING);
                }
            } else {
            	debug.printw("AVISO! RTS recebido no estado: "+xState.getState(), address);
            }
            break;
            
    	case CTS:
            if ((xState.getState() == XMacStateTypes.WAITING_CTS)) {
                // O número do preâmbulo é copiado na mensagem de CTS pelo vizinho que responde.
                if (packet.getSequenceNumber() == xState.getRtsPkt().getSequenceNumber()) { 
                    // Avisa XMAC que tem um pacote DATA a ser enviado em seguida
                    xState.setSendingPktType(PacketType.DATA);
                    xState.setState(XMacStateTypes.SENDING_MSG, xConf.stepsDelayTx(PacketType.DATA));
                    xRadioState.setRadioState(RadioState.SENDING);
                } else {
                	System.out.println("[AVISO! MAC Nó:"+address.getId()+"] Sequência errada no CTS: "+ 
                			packet.getSequenceNumber());
                }
            } else {
            	debug.printw("AVISO! CTS recebido no estado: "+xState.getState(), address);
            }
            break;
            
    	case DATA: 
    		/* Mensagem de dados, o que deverá ser enviado para a LogLink. DATA também é esperado 
    		 * durante um CS depois do último preâmbulo de Broadcast */
            if ((xState.getState() == XMacStateTypes.WAITING_DATA) || 
            		(xState.getState() == XMacStateTypes.CS)) {
            	// Guarde o pacote de dados recebido para enviar para a LogLink depois.
            	xState.setDataPkt(packet);
                // Se ACK é exigido
                if (packet.isAckRequested()) {
                	// Vai avisar a X_MAC que tem um pacote ACK a ser enviado em seguida
                	xState.setSendingPktType(PacketType.ACK);
                	xState.setState(XMacStateTypes.SENDING_MSG, xConf.stepsDelayTx(PacketType.ACK));
                	xRadioState.setRadioState(RadioState.SENDING);
    			    // Guardar atributos para montar pacote ACK
                	xState.setReceiverNode(packet.getSender().getId());
                	xState.setAckSeqNum(packet.getSequenceNumber());
                } else {
                	//////if (packet.getReceiver() != NodeId.ALLNODES)//////
                    /* Avisar a XMac que tem um pacote do tipo DATA a ser enviado. Como veio da camada
                     abaixo está a mensagem está definida como UPWARDS e será enviada para a LogLink */
                    xState.setSendingPktType(PacketType.DATA);   
                    // Escuta o canal por mais um tempo, para ver se haverá mais alguma transmissão
                    xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
                    xRadioState.setRadioState(RadioState.LISTENING);

                    
                }
              /* Se foi recebido um DATA indesejado durante um CS_START, deve reiniciar o envio de RTS.
               Se o estado atual não era CS ou CS_START, então pode ignorar a mensagem recebida */
            } else if (xState.getState() == XMacStateTypes.CS_START) {
                /* Desligue para não receber outra mensagem indesejada. Ao acabar o WUC e chamar 
            	changeStateTimeOut() novamente, não vai mudar de estado, vai sair na verificação de 
            	rádio pois vai estar OFF. Irá reiniciar o envio de RTS's chamando startSendDataProcess()
            	da classe XMac  */
            	xRadioState.setRadioState(RadioState.OFF);
                // Reiniciar o envio de RTS.
            	xState.setRestartRTS(true);
            }
            /*
            AppPacket pktApp = (AppPacket)packet.getPacket(LayerType.APPLICATION);
    		if(pktApp.getDestinationId() == address.getId().asInt()){
    			XMac.printStatistics();
    		}*/
            break;
            
    	case ACK:
            if (xState.getState() == XMacStateTypes.WAITING_ACK) {
            	// Confira se o ACK corresponde ao DATA que foi enviado.
            	if(packet.getSequenceNumber() == xState.getDataPkt().getSequenceNumber()) {
                    // A mensagem foi enviada e confirmado o recebimento.
                    xState.setDataPkt(null);
                    // Acabou a transmissão de DATA, vá para CS.
                    xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
                    xRadioState.setRadioState(RadioState.LISTENING);
                }
            }
            break;
        
    	case NACK:
        case CONTROL:
        case VOID:
        default:
            // Volta para CS
            xState.setState(XMacStateTypes.CS, xConf.getStepsCS());
            xRadioState.setRadioState(RadioState.LISTENING);
            debug.printw("Mensagem desconhecida: "+packet.getType(), address);
            break;
    	}

    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Depois")+debug.strXLayer(xState, xRadioState)+
				debug.strPaxPkt(packet), address);
    }
    
    
    /** Função que muda o estado do XMac e do rádio quando possui uma mensagem para enviar. 
     * Ao ser chamada, verifica qual o tipo de mensagem, para então mudar o estado e definir 
     * o tempo previsto para o estado atual.
     * No XMac todo envio de DATA é precedido por RTS, portanto nesse caso existe apenas a
     * opção RTS.
     */
    public void changeStateSendingMsg() {
    	switch (xState.getSendingPktType()) {
    	case RTS:
    		// Quando o XMac vai iniciar o envio de RTS
            xState.setState(XMacStateTypes.CS_START, xConf.getStepsCS());
            xRadioState.setRadioState(RadioState.LISTENING);
            break;
            
    	default:
    		break;
    	}
    }
    
    
    /** Função que muda o estado do XMac e do radio quando o rádio está recebendo uma mensagem.
     * XMac irá manter seu estado atual mais irá atualizar o delay para até o fim da mensagem
     * para que possa recebê-la para depois então continuar a mudança de estados.
     */
    public void changeStateReceivingMsg(XMacPacket packet) {

    	double delay = 1;
    	switch (packet.getType()) {
    	case RTS:
    		delay += xConf.getStepsRTS();
    		break;
    	case CTS:
    		delay += xConf.getStepsCTS();
    		break;
    	case ACK:
    		delay += xConf.getStepsACK();
    		break;
    	case DATA:
    		delay += xConf.getStepsDATA();
    		break;
    	default:
    		break;
    	}
    	xState.setState(xState.getState(), delay);
    }
  	
}

