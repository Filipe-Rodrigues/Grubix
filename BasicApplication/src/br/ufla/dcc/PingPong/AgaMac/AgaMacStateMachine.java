package br.ufla.dcc.PingPong.AgaMac;

import java.util.Random;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.PingPong.node.RadioState;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.physical.UnitDisc;

/** Classe que define os estados do AGAMac e suas respectivas durações
 * 
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 04/07/2016
 *
 */
public class AgaMacStateMachine  {
	
	/** referência para AGAMacState */
    AgaMacState agaState; 
    /** referência para AGAMacRadioState */
    AgaMacRadioState agaRadioState;
    /** referência para AGAMacConfiguration */
    AgaMacConfiguration agaConf;
    /** Endereço do nó atual */
    Address address;

    /** Objeto para realizar a depuração */
 	ToolsDebug debug = ToolsDebug.getInstance();
 	

    /**
	 * Default constructor 
	 */
    public AgaMacStateMachine(Address address, AgaMacState AGAMacState, AgaMacRadioState radioState, 
    		AgaMacConfiguration AGAMacConfig) {
    	this.agaState      = AGAMacState;
    	this.agaRadioState = radioState; 
    	this.agaConf       = AGAMacConfig;
    	this.address       = address;
	}
		
    
		/**
		 *  Função do AGAMac para decidir se um vizinho fará parte do FCS (Forward Candidate Set) 	
		 *  O nó corrente, vizinho do sender, recebe um RTS com informações de pré-requisitos para 
		 *  fazer parte do FCS. Nesta função, são feitos os cálculos para o nó indicar ao sender 
		 *  sua aptidão em fazer parte do FCS, indicada por rank. Retorna o rank que é o fator 
		 *  de aproximação (0 < rank < 1.0)
		 */
		private double checkFCS (AgaMacPacket rts) {

			Node myNode = SimulationManager.getInstance().queryNodeById(address.getId());
    		Node senderNode  = SimulationManager.getInstance().queryNodeById(rts.getSender().getId());
    		Node lastNode = SimulationManager.getInstance().queryNodeById(rts.getLastReceiver());
    			
    		Position senderPos = senderNode.getPosition();
    		Position lastPos = lastNode.getPosition();
    		Position myPos = myNode.getPosition();
    		
    		/* Mensagem alcançou o destino, retorna maior rank possível para não deixar outro nó ser
    		 eleito */
    		if (lastNode.getId() == address.getId()) 
    			return 1.0;
    		
    		double myDistToLastNode = lastPos.getDistance(myPos);
    		double senderDistToLastNode = lastPos.getDistance(senderPos);
    	
    		/* Faz o cálculo do alcance do nó quando o modelo físico for UnitDisc (deterministic 
    		 model with a constant transmission delay) */
    		UnitDisc UD = new UnitDisc();
    		double radioRange = UD.getReachableDistance();
    		
    		/* O destino já é alcançável pelo sender, então o nó atual retorna 
    		 o menor valor possível para não ser escolhido e deixar o sender ser eleito */
    		if (senderDistToLastNode < radioRange) 
    			return -1.0;	
    			
    		double myRank = (senderDistToLastNode - myDistToLastNode)/radioRange;
    		
    		/* TODO Ajusta o myRank para que fique entre o limiar padrão e 1. Talvez pode ser retirado */
    		if (myDistToLastNode < radioRange) 
    			return (agaState.getThreshold() + myRank * (1 - agaState.getThreshold())); 

    		agaState.setCurrentThreshold(rts.getCurrentThreshold());
    		
    		/* Se limiar atual é 0.001 e o nó (myRank) está além do limiar padrão, o limiar 
    		 atual está subutilizado */
    		if ((agaState.getCurrentThreshold() != agaState.getThreshold()) && 
    				(myRank > agaState.getThreshold())) {
    			debug.printw("myRank="+myRank+" threshold="+agaState.getThreshold()+
    					" currentThreshold="+agaState.getCurrentThreshold(), address);
    		}
    		/* Se myRank > limiar atual, limiar atual passa a ser o limiar padrão, se já não era, e
    		 o rank é retornado */
    		if (myRank > agaState.getCurrentThreshold()) {
    			agaState.setCurrentThreshold(agaState.getThreshold());
    			return myRank;
    		}
    		/* Se o sender está mais perto do destino final do que o nó atual, ou se, o nó atual não 
    		alcançou o limiar, o ranking é 0 */
    		return 0;  
		}
    
    
    /** Função que faz a primeira mudança de estado do AGAMac. O nó irá entrar em modo SLEEP por um
     tempo aleatório, antes de iniciar suas atividades */ 
    public void changeStateBootNode() {
    	// Se for necessário testar com seed fixa para cada nó
    	Random gerador = new Random(address.getId().asInt());
        int delay = gerador.nextInt(Integer.valueOf((int) Math.round(agaConf.getStepsCycle())));
        
    	// Tempo pequeno aleatório em steps para ligar o nó sensor
        //double delay = ((int) (Math.random() * (agaConf.getStepsCycle())));
    	agaState.setState(AgaMacStateTypes.SLEEP, delay);
        agaRadioState.setRadioState(RadioState.OFF);
    }
    
    
    /** Função que muda o estado do AGAMac e do radio quando o tempo do Wuc da AGAMac chega ao fim. 
     * Ao ser chamada, verifica qual o estado atual, para então mudar o estado e definir o tempo 
     * previsto para o estado atual.
     */
    public boolean changeStateTimeOut(WakeUpCall wuc) {
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.writeIfNodes(debug.str("Estado Antes")+debug.strAgaLayer(agaState, agaRadioState)+
    				debug.strWuc(wuc), address, new Integer[]{1});
    	
        /* Verifica se o número do estado armazeno no WUC é o mesmo do estado atual. Se não for,
         já houve mudança no estado do AGAMac antes do término do WUC, portanto esse WUC deve ser
         ignorado, já que não é mais válido. */
		if (agaState.getStateSeqNum() != ((AgaMacWucTimeOut)wuc).getStateNumber()) {
			debug.write(debug.str("AVISO! Número do estado não corresponde ao número do WUC"), address);
			return false;        
		}
		
		/* Se rádio está OFF e estado do AGAMac não é SLEEP, não prosseguir. Isso pode ocorrer se 
		durante um CS_START receber um DATA indesejado */
        if((agaRadioState.getRadioState() == RadioState.OFF) && 
        		(agaState.getState() != AgaMacStateTypes.SLEEP)) {
        	debug.printw("AVISO! Estado do radio: OFF e AGAMac: "+agaState.getState(), address);
        	return false;
        }
    	
    	switch (agaState.getState()) {
    	case CS:
    		/* Se venceu o estado que verifica se algum vizinho está transmitindo alguma mensagem 
    		 para o nó. Se não, volta a dormir depois do tempo de CS */
            agaState.setState(AgaMacStateTypes.SLEEP, agaConf.getStepsSleep());
            agaRadioState.setRadioState(RadioState.OFF); 
            break;
            
    	case SLEEP:
    		/* Se possui um pacote na AGAMacState e ele está direcionado para baixo, recomeça o 
    		 envio do RTS. Caso o pacote seja para a camada acima, ou não tenha pacotes armazenados,
    		 fica em CS */
    		if (agaState.getDataPkt() != null && 
    				agaState.getDataPkt().getDirection() == Direction.DOWNWARDS) { 
            	/* Quando ocorre uma falha no envio de MSG, voltará aqui. Indica para a AGAMac 
            	 que deverá reiniciar o processo de envio de preâmbulos */
            	agaState.setRestartRTS(true);	
            } else {
            	agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
                agaRadioState.setRadioState(RadioState.LISTENING);
            }
    		break;
    		
    	case CS_START:
            /* Se venceu o estado que verifica se o canal está livre para iniciar a transmissão do RTS.
             Agora em SENDING_MSG, a MAC aguarda um aviso vindo da PHY sobre o final da transmissão do
             RTS. Funciona como Watch-dog. 
             SendingPktType já foi definido na AGAMac em startSendDataProcess() */
    		agaState.setState(AgaMacStateTypes.SENDING_MSG, agaConf.stepsDelayTx(PacketType.RTS));
            agaRadioState.setRadioState(RadioState.SENDING);
            break;
            
    	case SENDING_MSG:  
    		/* MAC está esperando o rádio terminar de enviar uma mensagem, a PHY sinalizará o término 
    		 * do envio. Se chegou aqui, é porque o rádio não enviou o pacote. Se o rádio enviasse, ia 
    		 * para proceedCrossLayerEvent(), depois para proceedRadioSent().
    		 * Vai para CS e depois para SLEEP, ao acordar verifica se ainda deve mandar a msg novamente
    		 */
            agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
            agaRadioState.setRadioState(RadioState.LISTENING);
            break;
            
    	case WAITING_CTS: 
        	// Decrementa o número da sequência de RTS e obtém o novo valor da sequência
    		int rtsSeqNum = agaState.getRtsPkt().decSeqNum();
        	/* Esperava um CTS depois de enviar um RTS, mas CTS não chegou. Ainda não terminou a
        	 sequência de RTS. Sinaliza o envio do próximo RTS */
            if (rtsSeqNum > 0) {
            	// Vai avisar a AGAMac que tem um pacote RTS a ser enviado em seguida
                agaState.setSendingPktType(PacketType.RTS);
                agaState.setState(AgaMacStateTypes.SENDING_MSG, agaConf.stepsDelayTx(PacketType.RTS));
                agaRadioState.setRadioState(RadioState.SENDING);
                break;
            } 
            
            // Fim da sequência de RTS, não é mensagem BroadCast e nó de destino não respondeu
            if (agaState.getRtsPkt().getReceiver() != NodeId.ALLNODES) {
                // Irá para CS e depois para SLEEP.
                agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
                agaRadioState.setRadioState(RadioState.LISTENING);  
            } else {
            	// +AGAMac Se ninguém respondeu então reinicie o processo de envio com threshold menor
            	agaState.setRestartRTS(true);
            	debug.printw("AVISO! Final de WAITING_CTS e ninguém respondeu", address);
            }
            break;
            
        case WAITING_DATA: 
        	/* Acabou o tempo e não recebeu o pacote de dados, então o próximo estado será CS, 
        	 * pois talvez outro nó queira transmitir uma mensagem*/
            agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
            agaRadioState.setRadioState(RadioState.LISTENING);
            break;
    	
        case WAITING_ACK: 
        	/* Esperava um ACK depois de enviar DATA, mas ACK não chegou. Vai para CS e depois 
        	 para sleep, ao acordar verifica se ainda deve mandar a mensagem novamente */
            agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
            agaRadioState.setRadioState(RadioState.LISTENING);  
            break;
            
        case WILL_SEND_MSG:  
        	/* +AGAMac Após receber um RTS, AGAMac tem que aguardar um tempo de BackOff antes de enviar CTS, 
        	 evitando colisões. O tempo de BackOff depende do rank do nó (maior prioridade para quem 
        	 oferece maior avanço). Vencido esse tempo, pode enviar a mensagem CTS. */
            agaState.setState(AgaMacStateTypes.SENDING_MSG, agaConf.stepsDelayTx(PacketType.CTS));
            agaRadioState.setRadioState(RadioState.SENDING);
            break;  
            
        default:
        	debug.printw("AVISO! Estado desconhecido"+agaState.getState(), address);
    		break;
    	}
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.writeIfNodes(debug.str("Estado Depois")+debug.strAgaLayer(agaState, agaRadioState)+
    				debug.strWuc(wuc), address, new Integer[]{1});
    
    	return true;
    }
    
    
    /** Função que muda o estado do AGAMac e do radio quando uma mensagem foi enviada pelo rádio. 
     * Ao ser chamada, verifica qual o tipo de mensagem, para então mudar o estado e definir 
     * o tempo previsto para o estado atual.
     */
    public void changeStateSentMsg(AgaMacPacket packet) {
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Antes")+debug.strAgaLayer(agaState, agaRadioState)+
				debug.strAgaPkt(packet), address);
    	
    	switch (packet.getType()) {
    	case RTS:
            agaState.setState(AgaMacStateTypes.WAITING_CTS, agaConf.getStepsCTS());
            // Vai ouvir o canal para esperar o CTS
            agaRadioState.setRadioState(RadioState.LISTENING);
            debug.write(debug.strAgaPkt(packet), address);
            break;
            
    	case CTS:
            agaState.setState(AgaMacStateTypes.WAITING_DATA, agaConf.getStepsDATA());
            // Vai ouvir o canal para esperar o DATA
            agaRadioState.setRadioState(RadioState.LISTENING);
            break;
        
    	case DATA:
            // Se precisará enviar ACK
            if (agaState.getDataPkt().isAckRequested()) {
                // Tempo de espera por um ACK
                agaState.setState(AgaMacStateTypes.WAITING_ACK, agaConf.getStepsACK());
                // Vai esperar um ACK de DATA.
                agaRadioState.setRadioState(RadioState.LISTENING);
              // Se não precisa enviar ACK
            } else {
            	// Pacote de DATA é removido, pois já enviou o DATA e não precisa enviar ACK
                agaState.setDataPkt(null);
                agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
                agaRadioState.setRadioState(RadioState.LISTENING);
            }
            break;
        
    	case ACK: 
    		// Acabou de enviar o ACK após receber DATA
            agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
            // Verificar se há outra mensagem.
            agaRadioState.setRadioState(RadioState.LISTENING);
            /* Depois de enviar a mensagem ACK, envie a mensagem de DATA para a camada superior.
             Avisa a AGAMac que tem um pacote DATA a ser enviado para a LOGLINK em seguida.
             Como DATA veio da camada de baixo, a direção é do pacote é UPWARDS */
            agaState.setSendingPktType(PacketType.DATA);
            break;
            
    	case NACK:
        case CONTROL:
        case VOID: 
    	default:
            agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
            agaRadioState.setRadioState(RadioState.LISTENING);
            debug.printw("Mensagem desconhecida: "+packet.getType(), address);
    		break;
    	}
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Depois")+debug.strAgaLayer(agaState, agaRadioState)+
				debug.strAgaPkt(packet), address);
    }
    
    
    /**
     *  Função que processa as mensagem recebidas pelo rádio, quando o rádio acaba de receber uma 
     *  mensagem e a PHY a repassa para a MAC.
     *  Se a mensagem não é para este nó e ele está no estado CS_START, ele volta para CS_START pois 
     *  o envio de RTS's deverá ser adiado. Em outros estados quando a mensagem não era para este nó,
     *  não foi considerado.
     */
    public void changeStateReceivedMsg(AgaMacPacket packet) {
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Antes")+debug.strAgaLayer(agaState, agaRadioState)+
				debug.strAgaPkt(packet), address);
    	
    	// Se a mensagem é destinado para outro nó
        if ((packet.getReceiver().asInt() != address.getId().asInt()) && 
        		(packet.getReceiver() != NodeId.ALLNODES)) {    	
        	if ((agaState.getState() == AgaMacStateTypes.CS_START) &&
                    ((packet.getType() == PacketType.RTS) || (packet.getType() == PacketType.CTS))) {
                double delay = packet.getSequenceNumber()*(agaConf.getStepsRTS() + agaConf.getStepsACK());
                agaState.setState(AgaMacStateTypes.SLEEP, delay);
                agaRadioState.setRadioState(RadioState.OFF);
        	}
        	// Debug, escreve entrada no arquivo de depuração
        	debug.write(debug.str("Estado Depois")+debug.strAgaLayer(agaState, agaRadioState)+
    				debug.strAgaPkt(packet), address);
        	/* Nos demais casos, ignora a chegada de mensagem destinada a outro nó, e aguarda o final 
            da WUC em curso */
        	return; 
        }
    	
    	switch (packet.getType()) {
    	case RTS:
    		/* Se estivesse em CS_START, vai receber a mensagem que chegou, entra em CS e vai
    		 verificar que existe um DATA pendente para ser enviado */
            if ((agaState.getState() == AgaMacStateTypes.CS) || 
            		(agaState.getState() == AgaMacStateTypes.CS_START)) {
                /* Se foi requisitado CTS, espera um tempo para enviar o CTS. Se for Broadcast não é 
				 requisitado o ACK */
                if (packet.isAckRequested()) {
                	// Rank do nó, de 0 a 1 ou -2, em relação ao destino
                	double myRank = checkFCS(packet);
                	/* +AGAMac Se o nó não vai responder, então vai para Sleep esperar o tempo normal.
                	 Rank -2 significa que o destino já é alcançável pelo sender, então o nó pode
                	 voltar a dormir */
                	if (myRank <= 0) { 
						agaRadioState.setRadioState(RadioState.OFF); 
						// avisar o AGAMac que não tem pacote a enviar
						agaState.setSendingPktType(null);
						agaState.setState(AgaMacStateTypes.SLEEP, agaConf.getStepsSleep());
                	} else {
                    	/* +AGAMac Para evitar colisão em Broadcast, quem tem o melhor rank, limitado 
                    	 a 1.0, responde antes. O fator random é critério de desempate. O tempo máximo
                    	 para envio é 90% StepsCTS */
                		double delay = ((1 - myRank) * 0.9 * Math.random()) * agaConf.getStepsCTS();
                    	agaState.setState(AgaMacStateTypes.WILL_SEND_MSG, delay);
                    	// Avisa o AGAMac que tem um pacote CTS a ser enviado em seguida
                    	agaState.setSendingPktType(PacketType.CTS);
                    	agaState.setNextReceiverNode(packet.getSender().getId());
                    	agaState.setCtsSeqNum(packet.getSequenceNumber());
                    	agaRadioState.setRadioState(RadioState.LISTENING);
                	}
                /* Se não foi requisitado CTS então a mensagem é de Broadcast. Mantenha-se em CS até 
                 o último preâmbulo, depois do qual será enviado DATA */
                } else {
                    agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
                    agaRadioState.setRadioState(RadioState.LISTENING);
                }
            } else {
            	debug.printw("AVISO! RTS recebido no estado: "+agaState.getState(), address);
            }
            break;
            
    	case CTS:
            if ((agaState.getState() == AgaMacStateTypes.WAITING_CTS)) {
                // O número do preâmbulo é copiado na mensagem de CTS pelo vizinho que responde.
                if (packet.getSequenceNumber() == agaState.getRtsPkt().getSequenceNumber()) { 
                    // Avisa AGAMac que tem um pacote DATA a ser enviado em seguida
                	agaState.setSendingPktType(PacketType.DATA);
                	// +AGAMac Guarda o ID do nó do próximo salto
                	agaState.setNextReceiverNode((packet.getSender().getId()));
                	agaState.setState(AgaMacStateTypes.SENDING_MSG, agaConf.stepsDelayTx(PacketType.DATA));
                    agaRadioState.setRadioState(RadioState.SENDING);
                } else {
                	System.out.println("[AVISO! MAC Nó:"+address.getId()+"] Sequência errada no CTS: "+ 
                			packet.getSequenceNumber());
                }
            } else {
            	debug.printw("AVISO! CTS recebido no estado: "+agaState.getState(), address);
            }
            break;
            
    	case DATA: 
    		/* Mensagem de dados, o que deverá ser enviado para a LogLink. DATA também é esperado 
    		 * durante um CS depois do último preâmbulo de Broadcast */
            if ((agaState.getState() == AgaMacStateTypes.WAITING_DATA) || 
            		(agaState.getState() == AgaMacStateTypes.CS)) {
            	// Guarde o pacote de dados recebido para enviar para a LogLink depois.
            	agaState.setDataPkt(packet);
                // Se ACK é exigido
                if (packet.isAckRequested()) {
                	// Vai avisar a X_MAC que tem um pacote ACK a ser enviado em seguida
                	agaState.setSendingPktType(PacketType.ACK);
                	agaState.setState(AgaMacStateTypes.SENDING_MSG, agaConf.stepsDelayTx(PacketType.ACK));
                	agaRadioState.setRadioState(RadioState.SENDING);
                	// Guardar atributos para montar pacote ACK
                	agaState.setNextReceiverNode(packet.getSender().getId());
                	agaState.setAckSeqNum(packet.getSequenceNumber());
                } else {
                    /* Avisar a AGAMac que tem um pacote do tipo DATA a ser enviado. Como veio da camada
                     abaixo está a mensagem está definida como UPWARDS e será enviada para a LogLink */
                    agaState.setSendingPktType(PacketType.DATA);   
                    // Escuta o canal por mais um tempo, para ver se haverá mais alguma transmissão
                    agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
                    agaRadioState.setRadioState(RadioState.LISTENING);
                }
              /* Se foi recebido um DATA indesejado durante um CS_START, deve reiniciar o envio de RTS.
               Se o estado atual não era CS ou CS_START, então pode ignorar a mensagem recebida */
            } else if (agaState.getState() == AgaMacStateTypes.CS_START) {
                /* Desligue para não receber outra mensagem indesejada. Ao acabar o WUC e chamar 
            	changeStateTimeOut() novamente, não vai mudar de estado, vai sair na verificação de 
            	rádio pois vai estar OFF. Irá reiniciar o envio de RTS's chamando startSendDataProcess()
            	da classe AGAMac  */
            	agaRadioState.setRadioState(RadioState.OFF);
                // Reiniciar o envio de RTS.
            	agaState.setRestartRTS(true);
            }
            break;
            
    	case ACK:
            if (agaState.getState() == AgaMacStateTypes.WAITING_ACK) {
            	// Confira se o ACK corresponde ao DATA que foi enviado.
            	if(packet.getSequenceNumber() == agaState.getDataPkt().getSequenceNumber()) {
                    // A mensagem foi enviada e confirmado o recebimento.
                    agaState.setDataPkt(null);
                    // Acabou a transmissão de DATA, vá para CS.
                    agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
                    agaRadioState.setRadioState(RadioState.LISTENING);
                }
            }
            break;
        
    	case NACK:
        case CONTROL:
        case VOID:
        default:
            // Volta para CS
            agaState.setState(AgaMacStateTypes.CS, agaConf.getStepsCS());
            agaRadioState.setRadioState(RadioState.LISTENING);
            debug.printw("Mensagem desconhecida: "+packet.getType(), address);
            break;
    	}

    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.str("Estado Depois")+debug.strAgaLayer(agaState, agaRadioState)+
				debug.strAgaPkt(packet), address);
    }
    
    
    /** Função que muda o estado do AGAMac e do rádio quando possui uma mensagem para enviar. 
     * Ao ser chamada, verifica qual o tipo de mensagem, para então mudar o estado e definir 
     * o tempo previsto para o estado atual.
     * No AGAMac todo envio de DATA é precedido por RTS, portanto nesse caso existe apenas a
     * opção RTS.
     */
    public void changeStateSendingMsg() {
    	switch (agaState.getSendingPktType()) {
    	case RTS:
    		// Quando o AGAMac vai iniciar o envio de RTS
            agaState.setState(AgaMacStateTypes.CS_START, agaConf.getStepsCS());
            agaRadioState.setRadioState(RadioState.LISTENING);
            break;
            
    	default:
    		break;
    	}
    }
    
    
    /** Função que muda o estado do AGAMac e do radio quando o rádio está recebendo uma mensagem.
     * AGAMac irá manter seu estado atual mais irá atualizar o delay para até o fim da mensagem
     * para que possa recebê-la para depois então continuar a mudança de estados.
     */
    public void changeStateReceivingMsg(AgaMacPacket packet) {

    	double delay = 1;
    	switch (packet.getType()) {
    	case RTS:
    		delay += agaConf.getStepsRTS();
    		break;
    	case CTS:
    		delay += agaConf.getStepsCTS();
    		break;
    	case ACK:
    		delay += agaConf.getStepsACK();
    		break;
    	case DATA:
    		delay += agaConf.getStepsDATA();
    		break;
    	default:
    		break;
    	}
    	agaState.setState(agaState.getState(), delay);
    }
    
    
    /** Função que muda o estado do AGAMac e do radio quando o rádio está recebendo uma mensagem mas
     *  estava fazendo BackOff antes de enviar CTS, mas outro nó enviou CTS. Neste caso, volta dormir
     */
    public void changeStateReceivingMsg() {
    	agaState.setState(AgaMacStateTypes.SLEEP, agaConf.getStepsSleep());
    }
  	
}

