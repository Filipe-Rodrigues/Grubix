package br.ufla.dcc.PingPong.PaxMac;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.ToolsStatisticsSimulation;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/** Classe que define os estados do PaxMac e suas respectivas durações
 * 
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 22/06/2016
 *
 */
public class PaxMacStateMachine  {
	
	/** Referência para PaxMacState */
    PaxMacState paxState; 
    /** Referência para PaxMacRadioState */
    PaxMacRadioState paxRadioState;
    /** Referência para PaxMacConfiguration */
    PaxMacConfiguration paxConf;
    /** Endereço do nó atual */
    Address address;

    /** Ferramentas para imprimir informações para a depuração do programa */
 	ToolsDebug debug = ToolsDebug.getInstance();
    
 	/** Ferramentas para guardar e imprimir informações sobre as estatísticas da simulação */
 	ToolsStatisticsSimulation statistics = ToolsStatisticsSimulation.getInstance();
 	
 	/** Ferramentas auxiliares diversas */
 	ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();

    /**
	 * Default constructor 
	 */
    public PaxMacStateMachine(Address address, PaxMacState paxState, PaxMacRadioState paxRadioState, 
    		PaxMacConfiguration paxConfig) {
    	this.paxState      = paxState;
    	this.paxRadioState = paxRadioState; 
    	this.paxConf       = paxConfig;
    	this.address       = address;
	}
		
    
    /** Função que faz a primeira mudança de estado do PaxMac. O nó irá entrar em modo SLEEP por um
     tempo aleatório, antes de iniciar suas atividades */ 
    public void changeStateBootNode() {
    	// #BackOff aleatório para ligar o nó sensor pela primeira vez
        double delay = misc.rand(address.getId()) * (paxConf.getStepsCycle());
    	paxState.setState(PaxMacStateTypes.SLEEP, delay);
    }
    
    
    /** Função que muda o estado do PaxMac quando o tempo de algum estado criado pelo Wuc da PaxMac chega
     *  ao fim.
     */
    public boolean changeStateTimeOut(WakeUpCall wuc) {
        /* Verifica se o número do estado armazeno no WUC é o mesmo do estado atual. Se não for,
         * já houve mudança no estado do PaxMac antes do término do WUC, portanto esse WUC deve ser
         * ignorado, já que não é mais válido. */
		if (paxState.getStateSeqNum() != ((PaxMacWucTimeOut)wuc).getStateNumber()) {
			return false;        
		}
		
		double delay;
		
    	switch (paxState.getState()) {
    	case SLEEP:
    		/* Se já enviou do dado e está esperando confirmação com um ACK/DATA ou ACK */
    		if (paxState.isWaitingAck()) {
    			paxState.setState(PaxMacStateTypes.WAITING_ACK, paxConf.getStepsWaitingAck());
	    	
    		/* Se houve colisão durante a espera pelo CTS. Provável problema de terminal oculto, irá enviar
    		 * um RTS imediatamente para que os nós saibam que como o nó anterior continua mandando RTS,
    		 * quer dizer que houve colisão. */
    		} else if (paxState.isWaitingCtsCollision()) {
    			paxState.setSendingPktType(PacketType.RTS);
				changeStateSendingMsg();
				paxState.setWaitingCtsCollision(false);
				
    		/* Se tem a posse do dado */
    		} else if (paxState.getDataPkt() != null) {
    			/* Se o dado chegou ao destino, envia um ACK para o nó anterior, já que o dado não será
    			 * mais enviado como ACK/DATA */
    			if (paxState.getDataPkt().getFinalReceiverNode() == address.getId()) {
    				paxState.setSendingPktType(PacketType.ACK);
    				changeStateSendingMsg();
    			
    			/* Se o nó posterior faz parte do caminho, espera pelo CTS-DATA */
    			} else if (paxState.getNextReceiverNode() != null) {
					paxState.setState(PaxMacStateTypes.WAITING_CTS_DATA, paxConf.getStepsWaitingCtsData());
							
					// Ferramentas de depuração -------------------------
					debug.print("[Dado-procedimento] Irá esperar pelo CTS-DATA do nó: "+
							paxState.getNextReceiverNode(), address);
					statistics.getTransmission(paxState.getFinalReceiverNode()).addCtsDataWaiting(address.getId());		
					// --------------------------------------------------
    			} else {
    				/* Se é o nó de origem e já enviou uma sequência completa de RTS e fez BackOff,
    				 * reinicia o envio de RTS */
    				if (paxState.isNodeSource()) {	
	    				paxState.setSendingPktType(PacketType.RTS);
	    				changeStateSendingMsg();
	    				
	    				// Ferramentas de depuração -------------------------
	    				debug.print("[RTS-procedimento] Irá reiniciar o envio de RTS", address);
	    				// --------------------------------------------------
	    			
    				/* Não deve ocorrer: posse do dado, não sabe quem é o próximo nó e não é a origem */
    				} else {
						// Ferramentas de depuração -------------------------
						debug.print("!!![Erro] Tem posse do dado mas não sabe qual o próximo nó.", address);
						System.exit(1);
						// --------------------------------------------------
					}
    			}
			
			/* Não tem a posse do dado, situações: 
			 * - Esta fazendo o ciclo de trabalho
			 * - Pertence ao caminho e não tem RTS para enviar (prepara para enviar CTS-DATA)
			 * - Enviou uma sequência completa de RTS e fez backOff e irá reiniciar o envio de RTS */
			} else {
	        	if (paxState.isWakeUpToSendCtsData()) {
	        		
	        		// Ferramentas de depuração -------------------------
					debug.print("[CS-LONG-início] Iniciando o CS-LONG antes do CTS-DATA", address);
					// --------------------------------------------------
	    		} else if (paxState.getSendingPktType() == PacketType.RTS) {
	    			
	    			// Ferramentas de depuração -------------------------
					debug.print("[CS-LONG-início] Iniciando o CS-LONG antes de reiniciar o RTS", address);
					// --------------------------------------------------
	    		}
	        	paxState.setState(PaxMacStateTypes.CS_LONG, paxConf.getStepsCsLong());
	        }
    		break;
    	
    	case WAITING_CTS_DATA:
    		/* Não recebeu CTS DATA, irá tentar novamente no próximo intervalo */
    		
    		// Ferramentas de depuração -------------------------
    		debug.print("[!!!CTS-DATA-espera-fim] Estava esperando o CTS-DATA mas não recebeu", address);
    		// --------------------------------------------------
    		
    		retryDataSend();
    		break;
    		
    	case CS_LONG:
    		/* Venceu o estado que verifica se algum vizinho está transmitindo uma sequência de RTS.*/
    		/* Não faz parte do caminho, esta fazendo os ciclos de trabalho */
    		if (!paxState.isNodeBelongsPath()) {
    			paxState.setState(PaxMacStateTypes.SLEEP, paxConf.getStepsSleep()); 
    		
    		/* Não tem mensagens para enviar, então está na hora de enviar um CTS-DATA para então receber 
    		 * o dado */
    		} else {
    			if (paxState.isWakeUpToSendCtsData()) {
        			
            		// Ferramentas de depuração -------------------------
            		debug.print("[!!!CS-LONG-fim] Canal livre!!!", address);
            		// --------------------------------------------------
            		
    				paxState.setSendingPktType(PacketType.CTS_DATA);
    				changeStateSendingMsg();
    				
    			/* Se enviou uma sequência completa de RTS, mas não recebeu resposta, ou se teve que 
    			 * interromper o envio de RTS mas não tem a posse do dado, será tratado aqui */ 
        		} else if (paxState.getSendingPktType() == PacketType.RTS) {
        			
    				changeStateSendingMsg();
    				
    				// Ferramentas de depuração -------------------------
    				debug.print("[RTS-procedimento] Irá reiniciar o envio de RTS", address);
    				// --------------------------------------------------
    			
    			/* Após o ultimo nó enviar um CTS, escuta o canal para ver se quando recebeu o RTS não houve
    			 * problema de terminal oculto e o nó recomeçou a transmitir o RTS. Se chegou aqui está tudo 
    			 * certo e pode voltar a dormir para esperar o dado */
        		} else if (paxState.getSendingPktType() == PacketType.CTS) {
        			paxState.setSendingPktType(null);
        			/* Acorda antes para fazer o CS para o envio do CTS-DATA */
    				paxState.setState(PaxMacStateTypes.SLEEP, delayWakeupDataHandle());
                	
                	// Ferramentas de depuração -------------------------
    				debug.print("[CTS-enviado-sucesso] Irá esperar o dado", address);
    				// --------------------------------------------------
        		}
    		}
            break;
  
    	case CS_SHORT:
    		if (paxState.isChannelBusy()) {
    			
        		// Ferramentas de depuração -------------------------
        		debug.print("[!!!CarrierSense-fim] O canal está ocupado (isChannelBusy)", address);
        		// --------------------------------------------------
        		
    			/* Se o nó recebeu um RTS e ia começar o envio de seu primeiro RTS mas o canal está
    			 * ocupado. Nesse caso algum nó vizinho já respondeu primeiro ou esta acontecendo alguma
    			 * transmissão de outro caminho. Volta para o ciclo de trabalho. */
    			if (paxState.getSendingPktType() == PacketType.RTS && paxState.getRtsPkt() == null) {
    				paxState.nodeWillBeFired();
    				paxState.setState(PaxMacStateTypes.SLEEP, paxConf.getStepsSleep());
    				
    				// Ferramentas de depuração -------------------------
            		debug.print("[CTS-contido] Algum nó já respondeu o CTS/RTS primeiro.", address);
            		// --------------------------------------------------
    			}
    		} else {
    			changeStateSendingMsg();

    			// Ferramentas de depuração -------------------------
        		debug.print("[CarrierSense-fim] Canal livre!!!", address);
        		// --------------------------------------------------	
        	}
            break;
    	
    	case SENDING_MSG:  
    		/* Se chegou aqui, é porque o rádio não enviou a mensagem. Se o rádio enviasse, ia para 
    		 * proceedCrossLayerEvent(). Faz um BackOff */
    		delay = paxConf.getStepsBackOffChannelBusy(address.getId());
    		paxState.setState(PaxMacStateTypes.CS_LONG, delay);
            break;   
    
    	case WAITING_CTS:
        	/* Esperava um CTS depois de enviar um RTS, mas CTS não chegou. Se ainda não terminou a
        	 * sequência de RTS, envia o próximo RTS. */
            if (paxState.getRtsSeqNum() > 1) {
    			paxState.setSendingPktType(PacketType.RTS);
    			changeStateSendingMsg();
    			
    			// Ferramentas de depuração -------------------------
    			debug.print("[CTS-espera-fim] Estava esperando um CTS/RTS mas não recebeu", address);
    			// --------------------------------------------------
            } else {
        		// Se já atingiu o limite de sequências de RTS enviados
        		if (paxState.getRtsSequenceCount() >= paxConf.getMaxRtsSequences()) {
        			
        			// Ferramentas de depuração -------------------------
            		debug.print("[!!!RTS-sequência-limite] Já reiniciou as sequências de RTS até o " +
            				"limite.\nVolta ao ciclo de trabalho.", address);
            		statistics.getTransmission(paxState.getFinalReceiverNode()).finalizeTx = true;
            		// --------------------------------------------------	
        			
        			paxState.nodeWillBeFired();
            		paxState.setState(PaxMacStateTypes.SLEEP, paxConf.getStepsSleep());

            	} else {
    				/* Incrementa a contagem de sequências completas de RTS enviadas */
    				paxState.incRtsSequenceCount();
    				
    				/* A nova sequência de preâmbulos é definido como máximo + 1 pois já será decrementado 
    				 * em sendPAXMacPacket() antes do envio do RTS. */
    				paxState.getRtsPkt().setSequenceNum(paxConf.getMaxPreambles() + 1);
    				
                	paxState.setSendingPktType(PacketType.RTS);
                	// #BackOff
                	delay = paxConf.getStepsBackOffWaitDataPass(address.getId(), 0);
            		paxState.setState(PaxMacStateTypes.SLEEP, delay);
	            		
            		// Ferramentas de depuração -------------------------
                	debug.print("[!!!CTS-espera-fim] Mandou toda a sequência de RTS mas ninguém " +
            				"respondeu com um CTS/RTS\nReinicia o envio de RTS após BackOff", address);
    				statistics.getTransmission(paxState.getFinalReceiverNode()).setRestartRts(address.getId(), 
    						paxState.getRtsSequenceCount());
    				// --------------------------------------------------
            	}
            }
            break;

        case WAITING_DATA: 
        	/* Estava esperando o dado mas ele não chegou. Irá acordar no próximo tempo de envio do dado.
        	 * Se o número máximo de tentativas acabou, volta para o ciclo de trabalho */
        	
        	// Ferramentas de depuração -------------------------
            debug.print("[!!!Dado-espera-fim] Estava esperando dado mas não recebeu. Nó anterior: "+
            		paxState.getPreviousReceiverLv1(), address);
            misc.vGrubix(address.getId(), "Data nao chegou", "DARK_RED");
            // --------------------------------------------------
            
            retryDataSend();
            break;
    	
        case WAITING_ACK:
        	/* Esperava um ACK depois de enviar o dado, mas ACK/DADO ou ACK/RTS  não chegou. Provavelmente 
        	 * houve colisão. Nesse caso espera um tempo aleatório e reinicia o envio de RTS após um
        	 * cs_start que será executado por causa do RTS definido como nulo */
        	
        	// Ferramentas de depuração -------------------------
        	debug.print("[!!!Ack-espera-fim] Não recebeu o ACK do nó: "+paxState.getNextReceiverNode() + 
        			" Irá tentar novamente."
        	, address);
        	statistics.getTransmission(paxState.getFinalReceiverNode()).addAckWaiting(address.getId());
        	misc.vGrubix(address.getId(), "Ack nao chegou", "DARK_RED");
        	// --------------------------------------------------
        	
        	retryDataSend();
        	break;

        default:
        	debug.print("[!!!Aviso] Estado desconhecido"+paxState.getState(), address);
    		break;
    	}

    	// Ferramentas de depuração -------------------------
    	if (paxState.getState() != PaxMacStateTypes.CS_LONG && 
    			paxState.getState() != PaxMacStateTypes.SLEEP)
    		debug.print("[Estado-atual] "+debug.prtState(paxState), address);
    	// --------------------------------------------------
    	return true;
    }
    
    
    /** Função que muda o estado do PaxMac quando uma mensagem é enviada pelo rádio. Ao ser chamada, 
     * verifica qual o tipo de mensagem, para então mudar o estado e definir o tempo previsto para o 
     * estado atual.
     */
    public void changeStateSentMsg(PaxMacPacket packet) {
    	// Ferramentas de depuração -------------------------
	    debug.print("["+packet.getType()+"-enviou] Enviou "+packet.getType(), address);
	    // --------------------------------------------------
    	
	    switch (packet.getType()) {
    	case RTS:
    		/* Espera pelo CTS/RTS */
    		paxState.setState(PaxMacStateTypes.WAITING_CTS, paxConf.getStepsWaitingCts());
    		
    		// Ferramentas de depuração -------------------------
    		statistics.getTransmission(packet).addRtsSentEndTime(address.getId());
    		if (packet.getSequenceNum() == paxConf.getMaxPreambles()) {
    			misc.vGrubix(address.getId(), "Recebeu RTS", "LIGHT_BLUE");
    		}
    		// --------------------------------------------------
            break; 
            
    	case CTS:
    		/* Após o último nó enviar o CTS, verifica se quando recebeu o RTS não houve problema de terminal
    		 * oculto e se o nó anterior continua a mandar RTS. Tipo de pacote continua a ser CTS, para poder 
    		 * diferenciar no tratamento após o tempo de CS_LONG */
    		paxState.setSendingPktType(PacketType.CTS);
    		paxState.setState(PaxMacStateTypes.CS_LONG, paxConf.getStepsCsLong());
    		
    		// Ferramentas de depuração -------------------------
    			misc.vGrubix(address.getId(), "Recebeu RTS", "LIGHT_BLUE");
    		// --------------------------------------------------
    		break;
    	
    	case CTS_DATA:
    		paxState.setState(PaxMacStateTypes.WAITING_DATA, paxConf.getStepsWaitingData());
			
    		// Ferramentas de depuração -------------------------
    		debug.print("[Dado-procedimento] Irá esperar o dado.\n" +
    				"[PATH] "+debug.prtPath(paxState, paxConf, this), address);
    		statistics.getTransmission(packet.getFinalReceiverNode()).addCtsDataSent(address.getId());
    		// --------------------------------------------------
    		break;
    	case DATA:
    		/* Se o nó é o destino final, então ele enviou o dado para sua camada superior  */
    		if (packet.getFinalReceiverNode() == address.getId()) {
    			paxState.setDataPkt(null);
				paxState.setSendingPktType(null);
    		} else {
    			/* O nó que enviou o dado acordará para esperar que o próximo nó envie um ACK/DATA ou
    			 * novamente um CTS-DATA caso a dado não chegou */ 
            	paxState.setWaitingtAck(true);
            	
    			/* Ajuste para acordar no início do próximo tempo */
            	incCtsDataTime(1);
    			paxState.setState(PaxMacStateTypes.SLEEP, delayWakeupDataHandle());
            }
    		/* Contagem de tentativas de espera do dado é reiniciada */ 
        	paxState.setMissingDataCount(0);
        	
    		// Ferramentas de depuração -------------------------
    		statistics.getTransmission(packet.getFinalReceiverNode()).addDataSent(address.getId());
    		// --------------------------------------------------
            break;
        
    	case ACK: 
            /* O último nó envia um ACK para o nó anterior e envia a mensagem de dado para a camada superior.
             * Já que a direção do pacote será UPWARDS */
    		paxState.setSendingPktType(PacketType.DATA);
			changeStateSendingMsg();
            break;
            
    	case NACK:
        case CONTROL:
        case VOID: 
    	default:
            paxState.setState(PaxMacStateTypes.CS_LONG, paxConf.getStepsCsLong());
           
            // Ferramentas de depuração -------------------------
            debug.print("[!!!Aviso] Mensagem desconhecida: "+packet.getType(), address);
            // --------------------------------------------------
    		break;
    	}
    	
	    // Ferramentas de depuração -------------------------
    	debug.print("[Estado-atual] "+debug.prtState(paxState), address);
    	// --------------------------------------------------
    }
    
    
    /**
     *  Função que processa as mensagem recebidas pelo rádio, quando o rádio acaba de receber uma 
     *  mensagem e a camada física a repassa para a MAC.
     */
    public void changeStateReceivedMsg(PaxMacPacket packet) {
    	// Se acabou de receber uma mensagem, então o canal de rádio agora está livre.
    	paxState.setChannelBusy(false);

    	double delay;
    	switch (packet.getType()) {
    	case RTS:

    		/* Após enviar um RTS, o nó fica aguardando um dos nós de seu FCS propagar o envio de RTS.
    		 * Caso receba um CTS/RTS de um nó do FCS, volta a dormir até o tempo de chegada do dado. 
    		 * O último nó mandará um CTS, pois nesse caso não irá mais enviar RTS que servirá como CTS */
    		if (paxState.getState() == PaxMacStateTypes.WAITING_CTS) {
    			
    			/* Se o RTS recebido é de outro caminho, faz BackOff para não gerar colisões de RTS com
    			 * o outro caminho */
    			if (packet.getFinalReceiverNode() != paxState.getFinalReceiverNode()) {
    			
    				// #BackOff
            		delay = paxConf.getStepsBackOffWaitDataPass(address.getId(), 
            				packet.getSendCtsDataDelay());
            		paxState.setState(PaxMacStateTypes.SLEEP, delay);
  
            		// Ferramentas de depuração -------------------------
            		debug.print("!!![RTS-recebeu] Recebeu RTS de outro caminho, faz backOff", address);
            		misc.vGrubixChannelReservation(address.getId(), packet.finalReceiverNode);
            		// --------------------------------------------------
    				
    			/* Se recebeu o CTS/RTS do próximo salto */
    			} else if (packet.getPreviousReceiverNodeLv1() == address.getId()) {
	    			paxState.setNextReceiverNode(packet.getSender().getId());
	    			paxState.setSendingPktType(null);
	    			paxState.setRtsPkt(null);
	    			
	    			/* Acorda antes para fazer o CS para o envio do CTS-DATA */
    				paxState.setState(PaxMacStateTypes.SLEEP, delayWakeupDataHandle());

    				// Ferramentas de depuração -------------------------
	    			misc.vGrubix(address.getId(), "Recebeu CTS/RTS", "LIGHT_GREEN");
	    			statistics.getTransmission(packet).addNode(address.getId());
	            	debug.print("$$$[RTS-recebeu] Recebeu CTS/RTS, volto a dormir.\n"+
	            			"[PATH] "+debug.prtPath(paxState, paxConf, this), address);
	            	// --------------------------------------------------
    			
	            /* Se recebeu o RTS em que o nó anterior é o mesmo que desse nó, então é um nó
	             * informando que alcança esse nó e deve ser escolhido por estar mais longe. O
	             * nó voltará para o ciclo de trabalho. */
    			} else if (packet.getPreviousReceiverNodeLv1() == paxState.getPreviousReceiverLv1()) {
    				paxState.nodeWillBeFired();
    				paxState.setState(PaxMacStateTypes.SLEEP, paxConf.getStepsSleep());
    				
    				// Ferramentas de depuração -------------------------
    				misc.vGrubix(address.getId(), "Excluido", "PINK");
    				debug.print("[!!!Otimização-caminho] Outro nó mais longe já respondeu. Volto para o " +
                    		"ciclo de trabalho", address);
                    // --------------------------------------------------
	            	
            	/* Se recebeu um RTS do nó anterior novamente, quer dizer que houve colisão no nó 
                 * anterior causado pelo problema de terminal oculto (dois membros do FCS que não
                 * se enxergam enviaram um CTS/RTS). Neste caso os dois nós volta a dormir por
                 * tempo aleatório */
    			} else if (packet.getSender().getId() == paxState.getPreviousReceiverLv1()) {
    				paxState.nodeWillBeFired();
    				paxState.setState(PaxMacStateTypes.SLEEP, 
    						paxConf.getStepsBackOffChannelBusy(address.getId()));

    				// Ferramentas de depuração -------------------------
                    debug.print("[!!!Terminal-oculto] Nó anterior não recebeu CTS/RTS. Volto a dormir " +
                    		"por um tempo aleatório", address);
                    // --------------------------------------------------
    			}
    			
    		/* Se estava escutando o canal na espera de receber alguma mensagem e recebeu um RTS */
    		} else if (paxState.getState() == PaxMacStateTypes.CS_LONG) {
    			
    			if (packet.getFcsNodes() == null) {
    				// Não faz nada, continua no mesmo estado
 
    			/* Se o nó recebeu um RTS mas ele não pertence ao FCS indicado na mensagem. Volta a 
    			 * dormir e acorda somente depois que o dado foi enviado pelo próximo nó do caminho. 
    			 * É definido um tempo aleatório para os nós acordarem, para não acordarem sincronizados */
    			} else if (!packet.getFcsNodes().contains(address.getId())) {
    				/* Se é para habilitar a reserva de canal */
    				if (paxConf.isChannelReservation()) {
	            		// #BackOff
	            		delay = paxConf.getStepsBackOffWaitDataPass(address.getId(), 
	            				packet.getSendCtsDataDelay());
	            		paxState.setState(PaxMacStateTypes.SLEEP, delay);
	  
	            		// Ferramentas de depuração -------------------------
	            		misc.vGrubixChannelReservation(address.getId(), packet.finalReceiverNode);
	            		// --------------------------------------------------
    				}
	    		/* Se o nó recebeu um RTS mas ele pertence ao FCS indicado na mensagem, prepara um novo 
	    		 * RTS para ser encaminhado. Cada nó vizinho que recebeu esse RTS espera um tempo aleatório 
	    		 * para encaminhar o novo RTS para evitar colisões */
	    		} else {
	    			/* O tempo em que o nó irá receber a mensagem é calculado pelo tempo atual mais o 
	    			 * delay informado mais o tempo que demorou até chegar nessa função, que é o tempo para 
	    			 * o RTS ser totalmente transmitido, além disso tem o decremento do tempo de transmissão 
	    			 * usado pela função transmit() da classe AirModule, quando cria um wuc do tipo
	    			 * TransmissionEndOutgoing, que indica fim do envio do pacote. */
		   			
	    			double delayReceiveData = paxState.currentTimeSteps() + packet.getSendCtsDataDelay() - 
		   					paxConf.getStepsRTS() - 0.00228; // 0.00228 fica igual
		   			
	    			// Ferramentas de depuração -------------------------
	    			debug.print("$$$[RTS-recebeu] Recebeu o RTS no nó: "+ packet.getSender().getId(), address);
	    			misc.vGrubix(address.getId(), "Recebeu RTS", "GREEN");
	    			// --------------------------------------------------
	    			
	    			paxState.setPreviousReceiverLv1(packet.getSender().getId());
		           	paxState.setPreviousReceiverLv2(packet.getPreviousReceiverNodeLv1());
		           	paxState.setPreviousReceiverLv3(packet.getPreviousReceiverNodeLv2());
		           	paxState.setFinalReceiverNode(packet.getFinalReceiverNode());
	    			
		           	/* Se o nó alcança três nós atrás, não é possível otimizar o caminho e evitar colisão,
		           	 * nesse caso o nó não será escolhido e outro nó mais perto do destino irá responder */
		           	if (paxState.isReachThreeNodePath()) {
		           		paxState.nodeWillBeFired();
		           		delay = paxConf.getStepsBackOffWaitDataPass(address.getId(), 
	            				packet.getSendCtsDataDelay());
		           		paxState.setState(PaxMacStateTypes.SLEEP, delay);
		           		
		           		// Ferramentas de depuração -------------------------
		    			debug.print("!!![Impossibilitado] Nó alcança três nós atrás no caminho ", address);
		    			// --------------------------------------------------
		           		
		           	/* Se o nó alcança o segundo nó atrás, então irá considerá-lo como seu nó anterior. 
		           	 * No RTS será informado que o segundo nó anterior é agora seu primeiro nó 
	    			 * anterior, de forma que o nó anterior (que espera pelo CTS) irá recebê-lo e ver que
	    			 * foi desconsiderado como membro do caminho, e irá voltar para o ciclo de trabalho 
	    			 */
	    			} else if (paxState.isReachTwoNodePath()) {
	    				/* Irá acordar no tempo do nó excluído e receber informações de seus dois nós 
	    				 * anteriores. */
	    				delayReceiveData -= paxConf.getStepsDataRetryCicle();
	    				paxState.setCtsDataTime(delayReceiveData);
			           	paxState.setPreviousReceiverLv1(packet.getPreviousReceiverNodeLv1());
			           	paxState.setPreviousReceiverLv2(packet.getPreviousReceiverNodeLv2());
			           	paxState.setPreviousReceiverLv3(packet.getPreviousReceiverNodeLv3());
	    			} else {
	    				paxState.setCtsDataTime(delayReceiveData);
	    			}
	    			
	    			if (paxState.isNodeBelongsPath()) {
	    				/* Se é o nó do destino final que recebeu o RTS, então ele irá enviar um CTS para
		    			 * o penúltimo nó, uma vez que não haverá mais transmissão de CTS/RTS. Caso contrário
		    			 * envia o RTS a procura do próximo salto */
		    			if (address.getId() == paxState.getFinalReceiverNode()) {
		    				paxState.setNextReceiverNode(address.getId());
		    				paxState.setSendingPktType(PacketType.CTS);
		    			} else {
			            	paxState.setSendingPktType(PacketType.RTS);
			            	
			    		}
		    			/* Se a mensagem será enviada para múltiplos nós, é necessário uma espera aleatória para que 
		        		 * os nós não enviem RTS ao mesmo tempo, gerando colisões. */
		    			// #BackOff
		    			delay = paxConf.getStepsBackOffSendCts(address.getId(), packet);
		    			changeStateSendingMsg(delay);
		    			
		    			// Ferramentas de depuração -------------------------
		    			debug.print("[CS-SHORT-inicio] Espera um tempo aleatório de: " + delay + " steps", address);
		    			// --------------------------------------------------
	    			}	
	    		}
    		}
            break;
        
    	case DATA: 
            if (paxState.getState() == PaxMacStateTypes.WAITING_DATA) {
            	
            	// Se a mensagem é destinada ao nó
	            if (packet.getReceiver() == address.getId()) {
	            	
	            	// Contagem de tentativas de espera do dado é reiniciada
	            	paxState.setMissingDataCount(0);
	            	
	            	// Armazena o pacote de dados recebido
	            	paxState.setDataPkt(packet);
	            	
	                // Ferramentas de depuração -------------------------
	                misc.vGrubix(address.getId(), "Recebeu o dado", "DARK_BLUE");
	                // --------------------------------------------------

	            	// Se o nó não é o destino final
	            	if (packet.getFinalReceiverNode() != address.getId()) {
	            		
	        			// Ferramentas de depuração -------------------------
	        			debug.print("$$$[Dado-recebeu] Recebeu o dado, não sou o destino final.\n "+
	        					"[Dado] "+debug.prtPkt(packet, paxConf), address);
	        			// --------------------------------------------------
	        		
	    
	        		// Se o nó é o destino final
	            	} else {
	            		// Ferramentas de depuração -------------------------
	            		debug.print("$$$[Dado-recebeu] Recebeu dado, sou o nó de destino final", address);
	            		misc.vGrubix(address.getId(), "Recebeu o dado", "DARK_BLUE");
	            		statistics.getTransmission(paxState.getFinalReceiverNode()).setEndTime(); 
	        			statistics.getTransmission(packet).destinationGetData = true;    			
	        			// --------------------------------------------------
	        			
	            		/* Se ACK foi exigido, armazena o número de sequência. O ACK será usado apenas pelo 
	            		 * último nó, uma vez que o envio do dado pelo próximo hop já faz o papel de ACK */
	            	}
	            	/* Ajuste para acordar no início do próximo tempo */
	            	incCtsDataTime(1);
	    			paxState.setState(PaxMacStateTypes.SLEEP, delayWakeupDataHandle());
	            }
            }
            break;
            
    	case ACK:
    		// O último nó irá enviar um ACK para o penúltimo, já que não será mais enviado ACK/DADO
    		if (paxState.getState() == PaxMacStateTypes.WAITING_ACK) {

    			if (packet.getFinalReceiverNode() != paxState.getFinalReceiverNode()) {
    				// não faz nada, continua no mesmo estado
    				
    			} else if (packet.getReceiver() == address.getId()) {
	
	        		// Ferramentas de depuração -------------------------
	                debug.print("$$$[ACK-recebeu] Recebeu ACK. Transmissão para destino "+
	                		packet.getFinalReceiverNode()+" simulado com sucesso!", address);
	                misc.vGrubix(address.getId(), "Recebeu ACK/DADO", "DARK_GREEN");
	    	    	statistics.getTransmission(paxState.getFinalReceiverNode()).setEndTime(); 
	    			statistics.getTransmission(packet).finalizeTx = true;
	    			// --------------------------------------------------
	    			paxState.setState(PaxMacStateTypes.CS_LONG, paxConf.getStepsCsLong());
	    			paxState.nodeWillBeFired();
	    		}
    		}
    		break;
    	
    	case CTS:
    		// O último nó irá enviar um CTS para o penúltimo, já que não será mais enviado CTS/RTS
    		if (paxState.getState() == PaxMacStateTypes.WAITING_CTS) {
    			
    			if (packet.getFinalReceiverNode() != paxState.getFinalReceiverNode()) {
    				// não faz nada, continua no mesmo estado
    				
    			} else if (packet.getReceiver() == address.getId()) {
	
	    			// Ferramentas de depuração -------------------------
	            	debug.print("$$$[CTS-recebeu] Recebeu CTS do nó: "+ packet.getSender().getId()+
	            			", irá dormir até: "+paxState.getCtsDataTime(), address);
	            	statistics.getTransmission(packet).addNode(address.getId());
	            	// --------------------------------------------------
	            	
	            	paxState.setNextReceiverNode(packet.getSender().getId());
	    			paxState.setSendingPktType(null);
	    			paxState.setRtsPkt(null);
	    			
	    			/* Acorda antes para fazer o CS para o envio do CTS-DATA */
    				paxState.setState(PaxMacStateTypes.SLEEP, delayWakeupDataHandle());	
	    		}
    		}
    		break;
    		
    	case CTS_DATA:
    		/* Se o CTS-DATA recebido é de outro caminho. Espera pela passagem do dado do outro caminho */
    		if (packet.getFinalReceiverNode() != paxState.getFinalReceiverNode()) {
    			/* Não faz nada. Já fiz teste para colocar backOff mas resultado foi pior que fazer backPff
    			 * baseado no número de tentativas de receber/enviar CTS-DATA */

    		} else if (paxState.getState() == PaxMacStateTypes.WAITING_CTS_DATA) { 
    			if (packet.getReceiver() == address.getId()) {
    				/* Atualiza quem é o próximo nó, pois o nó posterior pode ter sido substituído por outro
    				 * nó de alcance maior */
    				paxState.setNextReceiverNode(packet.getSender().getId());
    				paxState.setSendingPktType(PacketType.DATA);
	    			changeStateSendingMsg();
		    		// Ferramentas de depuração -------------------------
		            debug.print("[$$$CTS-DATA-recebeu] Recebeu CTS-DATA do nó: " +packet.getSender().getId() +
		            		"\nIrá enviar o dado", address);
		            // --------------------------------------------------
	    		}
    		} else if (paxState.getState() == PaxMacStateTypes.WAITING_ACK) {
    			if (packet.getReceiver() == address.getId()) {
    				//Não está mais esperando pelo ACK, pois ira enviar o dado novamente
    				paxState.setWaitingtAck(false);
    				
    				paxState.setSendingPktType(PacketType.DATA);
	    			changeStateSendingMsg();
		    		// Ferramentas de depuração -------------------------
		            debug.print("[$$$NOACK-CTS-DATA-recebeu] Recebeu CTS-DATA ao esperar por ACK, " +
		            		"irá enviar o dado novamente", address);
		            // --------------------------------------------------
    			}
    		}
    		break;
        default:
            paxState.setState(PaxMacStateTypes.CS_LONG, paxConf.getStepsCsLong());
            
            // Ferramentas de depuração -------------------------
            debug.print("[!!!Aviso] Mensagem desconhecida: "+packet.getType(), address);
            // --------------------------------------------------
            break;
    	}

    }
    
    
    /** Antes de enviar a mensagem, é feita um CS para ver se o canal está livre 
     */
    public void changeStateSendingMsg() {
    	double delay;
    	if (paxState.getSendingPktType() == PacketType.RTS) {
    		
    		if (paxState.isNodeSource() || paxState.isNodeNextHopAfterSource()) {
    			/* Se durante o envio da sequência de RTS não dá tempo de enviar e receber RTS além de fazer
        		 * o CS-LONG, atualiza o tempo para a próxima recepção para que os próximos RTS informem o novo 
        		 * tempo de envio. Decrementa paxConf.getStepsRTS()/2 pois o nó pode responder até na metade
        		 * do tempo de CTS */
        		while ((paxState.getCtsDataTime() - (2 * paxConf.getStepsCsLong()) - 
        				(paxConf.getStepsRTS() / 2) - paxConf.getStepsToDesynchronizeEvent()) <= 
        				paxState.currentTimeSteps()) {
        			incCtsDataTime(1);
        		}
    		} else {
    			/* Se durante o envio da sequência de RTS ainda não desocupou o canal para o nó anterior enviar 
        		 * o CTS-DATA, incrementa o tempo para a próxima recepção. */
    			while (paxState.getCtsDataTime() - paxConf.getStepsDataRetryCicle() - 
    					paxConf.getStepsToDesynchronizeEvent() - paxConf.getStepsCsLong() <= 
    					paxState.currentTimeSteps()) {
        			incCtsDataTime(1);
        		}
    		}
    	} 
    	
    	if (paxState.getSendingPktType() == PacketType.DATA && 
				paxState.getDataPkt().getDirection() == Direction.UPWARDS) {
			delay = 0;
		} else {
			delay = paxConf.getStepsTx(paxState.getSendingPktType()) + paxConf.getStepsTxAir();
		}
    	
		/* Se o 2o hop anterior está no raio de alcance mesmo depois da otimização do caminho. Caso em 
		 * que o FCS está extremamente grande, além do suportado. */
		if (paxState.getSendingPktType() == PacketType.CTS_DATA && paxState.isReachTwoNodePath()) {
			// Ferramentas de depuração -------------------------
			debug.print("[!!!2Hop-anterior] 2o hop anterior no raio de alcance.", address);
			// --------------------------------------------------
		}
    	
		paxState.setState(PaxMacStateTypes.SENDING_MSG, delay);
    }

    
    /** Antes de enviar o CTS/RTS é feito um backOff para evitar que os nós enviem a mensagem
     * ao mesmo tempo 
     */
    public void changeStateSendingMsg(double delay) {
    	paxState.setState(PaxMacStateTypes.CS_SHORT, delay);
    }

    
    /** Função que muda o estado do PaxMac quando o rádio está recebendo uma mensagem. Por regra geral
     * o estado atual será expandido até o fim da recepção da mensagem, para que possa tomar alguma
     * ação sobre a mensagem recebida.
     */
    public boolean changeStateReceivingMsg(PaxMacPacket packet) {
    	double delay;
    	/* Se o rádio está enviando ou desligado não é possível receber a mensagem */
    	if (paxRadioState.getRadioState() == RadioState.OFF || 
    			paxRadioState.getRadioState() == RadioState.SENDING) {
    		return false;
    	}
    	
    	/* Se estava fazendo um CS antes de enviar um CTS-DATA ou um RTS, mas está recebendo alguma mensagem */
    	if (paxState.getState() == PaxMacStateTypes.CS_LONG) {
    		/* Se estava preparando para enviar o CTS-DATA */
    		if (paxState.isWakeUpToSendCtsData()) {
    			// Ferramentas de depuração -------------------------
    			debug.print("[!!!CarrierSense-fim] O canal está ocupado, não irei enviar CTS-DATA agora. "
    					+ "Mensagem vinda de: "+packet.getSender().getId() + " ("+packet.getType()+")", address);
				statistics.getTransmission(paxState.getFinalReceiverNode()).addCsLongCtsDataBusy(address.getId());			
    			// --------------------------------------------------
    			
    			retryDataSend();
    			return true;
    		}
    		/* Se estava preparando para enviar o primeiro RTS após um backOff */
    		if (paxState.getSendingPktType() == PacketType.RTS) {
    			// Ferramentas de depuração -------------------------
    			debug.print("[!!!CarrierSense-fim] O canal está ocupado, não irei reiniciar o RTS agora. "
    					+ "Mensagem vinda de: "+packet.getSender().getId(), address);
                // --------------------------------------------------

    			delay = paxConf.getStepsBackOffWaitDataPass(address.getId(), packet.getSendCtsDataDelay());
        		paxState.setState(PaxMacStateTypes.SLEEP, delay);
    			return true;
    		}
    	}
    	
    		
    	/* Se estava fazendo um CS antes de enviar um CTS/RTS, mas está recebendo alguma mensagem, 
    	 * provavelmente alguém enviou o CTS primeiro, ou o canal está ocupado com outra mensagem
    	 * de outro caminho. Em ambos casos não irá enviar o CTS e voltará para o ciclo de trabalho.
    	 * Se nenhum nó enviar o CTS, o nó anterior continuará a enviar o RTS */
    	if (paxState.getState() == PaxMacStateTypes.CS_SHORT) {
    		if (paxState.getSendingPktType() == PacketType.RTS && paxState.getRtsPkt() == null) {
    			paxState.nodeWillBeFired();
    			paxState.setState(PaxMacStateTypes.SLEEP, paxConf.getStepsCTS() + paxConf.getStepsRTS());
    			
    			// Ferramentas de depuração -------------------------
        		debug.printw("[!!!CST-already-sent] Recebeu alguma mensagem enquanto preparava envio do CST, "
        				+ "volto para o ciclo de trabalho.", address);
        		// --------------------------------------------------
        		return true;
    		}
    	}
			
    	
    	/* Se estava esperando por um ACK quando começou a receber a mensagem. É considerado que o
    	 * nó vai ler somente os primeiros bits (do tamanho de um RTS, que é o tempo de espera pelo ACK) 
    	 * da mensagem para saber qual o tipo de pacote e o receiver, e se é o ACK que esperava receber */
    	if (paxState.getState() == PaxMacStateTypes.WAITING_ACK) {
    		
    		/* Se a mensagem não é do nó posterior, então não é válida para o nó */
    		if (packet.getPreviousReceiverNodeLv1() != address.getId() || 
    				packet.getFinalReceiverNode() != paxState.getFinalReceiverNode()) {
    			return false;
    		}
    		
			/* Se estava esperando ACK e recebeu o ACK/DADO volta a dormir. */
    		if (packet.getType() == PacketType.DATA) {
    			
				// Ferramentas de depuração -------------------------
        		debug.print("$$$[Dado-recebeu] Recebeu ACK/DADO do nó: "+packet.getSender().getId()+"."+
        				" Volto para o ciclo de trabalho", address);
        		misc.vGrubix(address.getId(), "Recebeu ACK/DADO", "DARK_GREEN");
        		// --------------------------------------------------
        		
        		paxState.nodeWillBeFired();
        		paxState.setState(PaxMacStateTypes.CS_LONG, paxConf.getStepsCsLong()); 
        		return true;
            }
    	}

    	/* Demais casos, irá manter o estado atual do PAXMac, até o final da recepção da mensagem 
    	 * pela PHY */
    	delay = paxConf.getStepsTx(packet.getType()) + paxConf.getStepsTxAir();
    	paxState.setState(paxState.getState(), delay);

    	// Ferramentas de depuração -------------------------
    	if (paxState.getState() == PaxMacStateTypes.WAITING_DATA && packet.getType() == PacketType.DATA) {
    		misc.vGrubix(address.getId(), "Recebendo DADO", "PURPLE");
    	}
    	if (paxState.getPreviousReceiverLv1() != null) {
    		debug.print("[Estado-tempo-estendido] Estado="+paxState.getState()+
    				" Tempo estendido="+delay+" Término="+(delay+paxState.currentTimeSteps()+
    				" Mensagem="+packet.getType()), address);
    	}
    	// --------------------------------------------------
    	return true;
    }
    
    
    /** Função que muda o estado do PaxMac quando percebe uma colisão de mensagens recebidas */
    public boolean changeStateCollision() {
		/* Se estava esperando um CTS quando houve colisão, continua o envio de RTS após o tempo de 
		 * espera pelo CTS, pois o próximo nó provavelmente enviou um CTS/RTS e continuará a 
		 * propagação do RTS. Se o próximo nó receber novamente um RTS do nó anterior irá cancelar o 
		 * envio, pois saberá que houve colisão nó nó anterior */
		if (paxState.getState() == PaxMacStateTypes.WAITING_CTS) {
			paxState.setSendingPktType(PacketType.RTS);
			paxState.setState(PaxMacStateTypes.SLEEP, paxConf.getStepsRTS() + 
					paxConf.getStepsToDesynchronizeEvent());
			paxState.setWaitingCtsCollision(true);
			return true;
		}
		return false;
    }
    
    
    /** Função que muda o estado do Radio baseado no estado da MAC */
    public void changeRadioState() {
    	switch (paxState.getState()) {
		case SLEEP:
			paxRadioState.setRadioState(RadioState.OFF);
			break;
		case CS_LONG:
		case CS_SHORT:
		case WAITING_ACK:
		case WAITING_CTS:
		case WAITING_CTS_DATA:
		case WAITING_DATA:
			paxRadioState.setRadioState(RadioState.LISTENING);
			break;
		case SENDING_MSG:
			paxRadioState.setRadioState(RadioState.SENDING);
			break;
		default:
			break;
		}
    }
	
	
    /** Retorna daqui a quanto tempo o nó deverá receber ou enviar o CTS-DATA */
    public double delayUntilCtsData() {
    	double remaining = paxState.getCtsDataTime() - paxState.currentTimeSteps();
    	return remaining < 0 ? 0 : remaining;
    }

   
    /** Retorna daqui a quanto tempo o nó deverá acordar para iniciar a Tx ou Rx do dado */
    public double delayWakeupDataHandle() {
    	double delay = delayUntilCtsData();
		/* Se não tem a posse do dado, acorda mais cedo para fazer um CS antes de enviar CTS-DATA.
		 * É descontado um tempo para que o nó envie o CTS-DATA um pouco depois que o nó que mantém o
		 * dado inicio a espera pelo CTS-DATA */

    	if (paxState.getDataPkt() == null) {	
			delay -= paxConf.getStepsCsLong() - paxConf.getStepsToDesynchronizeEvent();
		}
    	return delay;
    }
    
    
    /** Retorna daqui a quanto tempo o próximo nó deverá receber ou enviar o CTS-DATA */
	public double delayUntilCtsDataNextHop() {
		double delay = paxState.getCtsDataTime() - paxState.currentTimeSteps();
		// O primeiro nó não precisa somar o tempo de transmissão do dado, pois já possui o dado
		if (!paxState.isNodeSource()) {
			delay += paxConf.getStepsDataRetryCicle();
		}
		return delay;
	}
	

	/** Incrementa o tempo para o nó que possui o dado enviá-lo. */
	public void incCtsDataTime(int incTimes) {
		/* Incrementa o tempo para envio/recepção do CTS-DATA, até que o próximo quadro comece depois do
		 * tempo corrente. O incremento ocorrerá obrigatoriamente pelo menos um vez */
		for (int i = 0; i < incTimes; i++) {
			double time = 0;
			while (time < paxState.currentTimeSteps()) {
				time = paxState.getCtsDataTime() + paxConf.getStepsDataRetryCicle();
				paxState.setCtsDataTime(time);
			}
		}
		
		// Ferramentas de depuração -------------------------
		debug.print("!!![Dado-tempo-reajuste] Tempo para enviar/receber CTS-DATA reajustado para: " + 
				paxState.getCtsDataTime(), address);
		// --------------------------------------------------
	}
	
	
	/** Prepara a próxima tentativa de envio do dado */
	public void retryDataSend() {
		
		if (paxState.getMissingDataCount() < paxConf.getMaxCtsDataRetry()) {
			int incTimes;
			
    		/* Incrementa quantas vezes já esperou pelo dado */
        	paxState.incMissingDataCount();
        	
        	/* Em quantos quadros de tempo será acrescido */        	
        	int count = paxState.getMissingDataCount() % paxConf.getCtsDataBackOffWatchdog();

        	if (count == 0 && paxState.getState() == PaxMacStateTypes.WAITING_DATA) {
        		
        		incTimes = paxConf.getCtsDataBackOffDuration();
        		
        		// Ferramentas de depuração -------------------------
                debug.print("[!!!Dado-envio-backOff] Não recebeu o dado, possível colisão, faz backOff", address);
                misc.vGrubix(address.getId(), "BackOff", "PINK");
                // --------------------------------------------------
        	} else {
        		incTimes = 1;
        	}
        	
        	incCtsDataTime(incTimes);
    		paxState.setState(PaxMacStateTypes.SLEEP, delayWakeupDataHandle());
    		
    		// Ferramentas de depuração -------------------------
            debug.print("[Dado-envio-continua] Irá enviar/receber o dado novamente. Tentativas " +
            		"restantes: "+ (paxConf.getMaxCtsDataRetry() - paxState.getMissingDataCount()) + 
            		" Irá acordar em: " +(paxState.currentTimeSteps() + delayWakeupDataHandle()) + 
            		" (restando "+delayWakeupDataHandle()+")", address);
            // --------------------------------------------------
    	} else {
    		paxState.nodeWillBeFired();
			paxState.setState(PaxMacStateTypes.SLEEP, paxConf.getStepsSleep());
			
			// Ferramentas de depuração -------------------------
            debug.print("[!!!Dado-envio-fim] Não irá mais enviar o dado. Volta ao ciclo", address);
            // --------------------------------------------------
    	}
	}
}
