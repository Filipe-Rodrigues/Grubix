package br.ufla.dcc.PingPong.EXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.PingPong.EXMac.EXMacState.EXMacActionType;
import br.ufla.dcc.PingPong.EXMac.EXMacState.EXMacEventType;
import br.ufla.dcc.PingPong.EXMac.EXMacState.EXMacStateType;

/** Classe que define os estados do XMac e suas respectivas durações
 * 
 *  Usada pela XMac.java
 * 
 * 	@author João Giacomin
 *  @version 18/03/2019
 *
 */
public class EXMacStateMachine  {
	
	/** referência para xMacState */
    EXMacState xState; 
    /** referência para XMacConfiguration */
    EXMacConfiguration xConf;
    /** Endereço do nó atual */
    Address address;
    /** Tempo de operação do nó desde a última vez que saiu de SLEEP*/
    double stepsFromLastSleep;
    
    boolean debug = false;

    /**
	 * Default constructor 
	 */
    public EXMacStateMachine(Address address, EXMacState xmacState, EXMacConfiguration xmacConfig) {
    	this.xState      = xmacState;
    	this.xConf       = xmacConfig;
    	this.address     = address;
	}
		
    
    /** Função para colocar na XMacState o novo estado da MAC e a ação a ser executada pela XMac.
     * 
     * @param newState = novo estado, para qual estado a MAC deverá ir
     * @param delay    = qual o máximo tempo previsto para se manter nesse estado
     * @param action   = qual ação imediata a MAC deverá executar
     * @return         = retorna TRUE ao terminar de executar a função.
     */
    
    private boolean setNewState (EXMacStateType newState, double delay, EXMacActionType action){
		xState.setAction(action);
    	return xState.setState(newState, delay);
    }
    
    /** Funçao para designação do próximo estado. 
     *  Chamada pela XMac quando ocorre algum evento.
     *  Retorna TRUE se uma nova WUC (marcação de tempo) deve ser colocada pela XMAC.
     *  Se o retorno for FALSE, a XMAC não inicia uma nova WUC.
     *  A cada decisão, uma ordem é estabelecida, para ser executada pela XMac,
     *  como ligar ou desligar o rádio.
     * 
     *  @param  event =      o tipo de evendo recebido pela XMac
     *  @return changeState: TRUE, indica que uma nova WUC deve ser criada; FALSE, não cria nova WUC
     */
    
    public boolean changeState(EXMacEventType event){
    	
    	boolean changeState = false;
    	xState.setAction(EXMacActionType.CONTINUE);
    	
    	 	
    	switch (xState.getState()) {

    	

    	/** ---------------------- Estado Sleep - inativo ---------------------- **/
    	
    	case SLEEP:

    		stepsFromLastSleep = SimulationManager.getInstance().getCurrentTime();
    		
    		switch (event){
    		
    		case TIME_OUT:
    			if (xState.isDataPending()) { 
    				/* Quando ocorre uma falha no envio de MSG, voltará aqui. */
    				// Decrementa o número de chances que tem para tentar enviar DATA 
    				if(xState.getDataPkt().decRetryCount() > 0){
    					// Reiniciar o contador numCSstart, número de vezes que se tentou estabelecer comunicação   */
        				xState.setRetryCSstart(xConf.getMaxBOstarts());
    					changeState = setNewState(EXMacStateType.CS_START, xConf.getStepsCS(), EXMacActionType.ASK_CHANNEL);
    					if(debug) System.out.println("Machine: " + address.getId()+ " State = SLEEP, Event = TIME_OUT com dataPending" );
    				} else {
    					changeState = setNewState(EXMacStateType.CS, xConf.getStepsCS(), EXMacActionType.TURN_ON);
    					if(debug) System.out.println("Machine: " + address.getId()+ " State = SLEEP, Event = TIME_OUT, falhou envio de DATA" );
    				}
    			} else if (xState.isWaitingBroadcast()) {
    				xState.setWaitingBroadcast(false);
    				//System.err.println("ACORDEI EM: " + SimulationManager.getInstance().getCurrentTime() + " steps");
    				changeState = setNewState(EXMacStateType.WAITING_DATA, xConf.stepsDelayRx(PacketType.DATA), EXMacActionType.TURN_ON);
    				if(debug) System.out.println("Machine: " + address.getId()+ " State = SLEEP, Event = TIME_OUT com waitingBroadcast" );
    			} else {
    	        	changeState = setNewState(EXMacStateType.CS, xConf.getStepsCS(), EXMacActionType.TURN_ON);
    	        	if(debug) System.out.println("Machine: " + address.getId()+ " State = SLEEP, Event = TIME_OUT" );
    	        }
    		break;
    		
    		case LOG_LINK:
    			/* Uma ordem da Log Link Layer para enviar mensagem. Deve-se verificar se o canal está ocupado */
    			xState.setRetryCSstart(xConf.getMaxBOstarts());
    			changeState = setNewState(EXMacStateType.CS_START, xConf.getStepsCS(), EXMacActionType.ASK_CHANNEL);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SLEEP, Event = LOG_LINK" );
    		break;
    		
       		default:
       			if(debug) System.out.println("Machine: " + address.getId()+ " State = SLEEP, evento não previsto" );
			break;

            }
    	break;
  
    	/** ---------------- Estado Carrier Sense - Procura um preâmbulo (RTS) ----------------- **/
    	
    	case CS:
    		
    		switch (event){
    		
    		case TIME_OUT:
    			/* Se venceu o tempo do estado que verifica se algum vizinho está transmitindo 
    			 * alguma mensagem para o nó, é porque não havia transmissão, então volta a dormir  */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS, Event = TIME_OUT" );
            break;
    		
    		case LOG_LINK:
    			/* Uma ordem da Log Link Layer para enviar mensagem. Deve-se verificar se o canal está ocupado */
    			xState.setRetryCSstart(xConf.getMaxBOstarts());
    			changeState = setNewState(EXMacStateType.CS_START, xConf.getStepsCS(), EXMacActionType.ASK_CHANNEL);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS, Event = LOG_LINK" );
        	break;
        		
    		case RTS_RECEIVED:
    			/* Se recebeu uma mensagem RTS durante o CS, 
    			 * verifique para quem é a mensagem e responda com CTS se necessário  */
    			changeState = changeStateRTSreceived(EXMacStateType.CS);
    		break;
    			
    		default:
    			/* Se recebeu um CTS ou ACK, então vá dormir.  */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS, Event = " + event);
    		break;

            }
    	break;
            
        /** --------- Estado Carrier Sense Start - Verifica atividade no canal antes de enviar RTS  ----- **/
    	
    	case CS_START:
            /* O único evento que importa no estado CS_START é TIME_OUT.
             * Neste caso, passa a enviar RTS, se o canal estiver livre.
             * Em qualquer outro caso, faz um Back Off para tentar comunicação mais tarde  */
    		
    		switch (event){
    		
    		case TIME_OUT:
    			if (xState.isChannelBusy()) {
    				/* Se o canal de comunicação está ocupado e não recebeu um evento RTS_RECEIVED 
        		     * deve fazer um BackOff de um tempo igual à duração do envio de DATA.            */
    				changeState = setNewState(EXMacStateType.BO_START, xConf.getStepsBOstart(), EXMacActionType.TURN_OFF);
    				if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_START, Event = TIME_OUT, Channel busy " );
    			}
    			else {
    				// Se o canal está livre, envia RTS
    				changeState = setNewState(EXMacStateType.SENDING_RTS, xConf.stepsDelayTx(PacketType.RTS), EXMacActionType.START_RTS);
    				if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_START, Event = TIME_OUT, Channel free" );
    			}
    		break;
    		
    		case CHANNEL_BUSY:
    		case CHANNEL_FREE:
    			/* Não deve desistir se receber um evento CHANNEL_BUSY, porque pode estar recebendo um RTS.
    			 * Se for CHANNEL_FREE, ainda pode receber um RTS. Espere o final do CS_START                                            			 */
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_START, Evento = " + event );
    		break;
    	    
    		case RTS_RECEIVED:
    		/* Se recebeu uma mensagem RTS durante o CS_START, 
    		 * responda como se fosse um CS e tente iniciar envio mais tarde  */
    			changeState = changeStateRTSreceived(EXMacStateType.CS_START);
       		break;
    			
    		default:
    			/* Se receber qualquer outro tipo de mensagem, vá dormir      */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_START, Evento = " + event + " não previsto" );
    		break;
    	}
        break;
        
        /** ---------- Estado Carrier Sense End - Verifica atividade no canal após comunicação  -------- **/
        
    	case CS_END:
    		/* Escuta canal depois de uma transmissão. Se não recebe nada, volta a dormir depois do tempo de CS 
    		   Diferente de CS e CS_START, neste estado, o nó sensor pode receber uma msg DATA */
    		switch (event){
    		
    		case TIME_OUT:
    			/* Se não recebeu uma mensagem completa (pela Lower SAP), pode ser um DATA. Verifique o rádio */
    			xState.setAction(EXMacActionType.ASK_CHANNEL);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_END, Event = TIME_OUT, verifique o canal " );
    		break;
    			
    		case CHANNEL_FREE:
    			/* Nenhuma mensagem adicional foi enviada para este nó. Vá dormir   */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_END, Event = CHANNEL_FREE" );
    		break;
    		
    		case CHANNEL_BUSY:
    			/* Se o canal de comunicação está ocupado então pode ser DATA sendo transmitido.
        		 * Deve continuar na escuta por um tempo adicional igual ao necessário para receber DATA. */
    			changeState = setNewState(EXMacStateType.WAITING_DATA, xConf.stepsDelayRx(PacketType.DATA), EXMacActionType.TURN_ON);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_END, Event = CHANNEL_BUSY" );
    		break;
    		
    		case RTS_RECEIVED:
    			/* Se recebeu uma mensagem RTS durante o CS_END, 
        		 * responda como se fosse um CS e tente iniciar envio mais tarde  */
    			changeState = changeStateRTSreceived(EXMacStateType.CS_END);
    		break;
    		
    		case DATA_RECEIVED:
       			/* Se recebeu um DATA, então já estará no estado WAITING_DATA */	
    		break;
 
    		default:
    			/* Se recebeu um CTS ou ACK, então vá dormir.  */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = CS_END, Event = " + event);
				break;
            }
    	break;
            
    	/** -------------- Estado Enviando RTS - Espera o rádio terminar de enviar RTS  ----------- **/
    	
        case SENDING_RTS:          
         	/* MAC está esperando o rádio terminar de enviar uma mensagem RTS.   */       	
    		switch (event){
    		
    		case TIME_OUT:
    			/* Se o rádio não enviou RTS, vá dormir. Quando acordar iniciará o processo novamente */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_RTS, Event = TIME_OUT" );
    		break;
    			
    		case MSG_SENT:
    			/* Se o rádio enviou RTS, então espere por uma resposta CTS */
    			changeState = setNewState(EXMacStateType.WAITING_CTS, xConf.stepsDelayRx(PacketType.CTS), EXMacActionType.TURN_ON);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_RTS to " 
    					    		+ xState.getRtsPkt().getReceiver() + ". Event = MSG_SENT. PRE = " 
    					    		+ xState.getRtsPkt().getRetryCount() + "/"+ xConf.getMaxPreambles() );
   			break;
        	
        	default:
        		if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_RTS, evento não previsto" );
        	break;
    		}	
        break;
        
        /** -------------- Estado Enviando CTS - Espera o rádio terminar de enviar CTS  ----------- **/
        
        case SENDING_CTS: 
        	/* MAC está esperando o rádio terminar de enviar uma mensagem CTS.   */ 
        	switch (event){
    		
    		case TIME_OUT:
    			/* Se o rádio não enviou CTS, vá dormir. A comunicação falhou */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_CTS, Event = TIME_OUT" );
    		break;
    			
    		case MSG_SENT:
    			/* Se o rádio enviou CTS, então espere para receber DATA */
    			changeState = setNewState(EXMacStateType.WAITING_DATA, xConf.stepsDelayRx(PacketType.DATA), EXMacActionType.TURN_ON);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_CTS, Event = MSG_SENT" );
   			break;
        	
        	default:
        		if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_CTS, evento não previsto" );
        	break;
    		}
   		break;
        
   		/** -------------- Estado Enviando DATA - Espera o rádio terminar de enviar DATA  ----------- **/
   		
        case SENDING_DATA: 
        	/* MAC está esperando o rádio terminar de enviar uma mensagem DATA.   */ 
        	switch (event){
    		
    		case TIME_OUT:
    			/* Se o rádio não enviou DATA, vá dormir. Quando acordar iniciará o processo novamente */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_DATA, Event = TIME_OUT" );
    		break;
    			
    		case MSG_SENT:
    			//System.err.println("TERMINEI O ENVIO EM: " + SimulationManager.getInstance().getCurrentTime() + " steps");
    			/* Se o rádio enviou DATA, então espere por uma resposta ACK, se necessáio */
    			if (xState.getDataPkt().isAckRequested()){
    				changeState = setNewState(EXMacStateType.WAITING_ACK, xConf.stepsDelayRx(PacketType.ACK), EXMacActionType.TURN_ON);
    			}
    			else {
    				xState.setDataPending(false);
    				changeState = setNewState(EXMacStateType.CS_END, xConf.getStepsCS(), EXMacActionType.TURN_ON);
    			}
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_DATA, Event = MSG_SENT" 
    					            + " next State = " + xState.getState());
    		break;
        	
        	default:
        		if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_DATA, evento não previsto" );
        	break;
    		}
   		break;
            
   		/** -------------- Estado Enviando ACK - Espera o rádio terminar de enviar ACK  ----------- **/
   		
        case SENDING_ACK: 
        	/* MAC está esperando o rádio terminar de enviar uma mensagem ACK.   */ 
        	switch (event){
    		
    		case TIME_OUT:
    			/* Se o rádio não enviou ACK, vá dormir. A comunicação falhou */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_ACK, Event = TIME_OUT" );
    		break;
    			
    		case MSG_SENT:
    			/* Se o rádio enviou ACK, então mande DATA para a Log Link Layer e vá dormir */
    			changeState = goToSleep(EXMacActionType.MSG_UP);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_ACK, Event = MSG_SENT" );
   			break;
        	
        	default:
        		if(debug) System.out.println("Machine: " + address.getId()+ " State = SENDING_ACK, evento não previsto" );
        	break;
    		}
        break;  
  
        /** -------------- Estado Esperando CTS - Espera o rádio receber um CTS  ---------------- **/
        
    	case WAITING_CTS: 
         	/* MAC está esperando receber uma mensagem CTS pelo rádio. */
    		switch (event){
    		
    		case TIME_OUT:
    			/* Decrementa o número da sequência de RTS e obtém o novo valor da sequência  */
    			int rtsSeqNum = xState.getRtsPkt().decRetryCount();
    			/* Esperava um CTS depois de enviar um RTS, mas CTS não chegou. 
    			 * Ainda não terminou a sequência de RTS. Sinaliza o envio do próximo RTS */
    			if (rtsSeqNum > 0) {
    				/* Vai avisar a XMAC que tem um pacote RTS a ser enviado em seguida  */
    				changeState = setNewState(EXMacStateType.SENDING_RTS, xConf.stepsDelayTx(PacketType.RTS), EXMacActionType.MSG_DOWN);
    				if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_CTS, Event = TIME_OUT" );
    			} 
    			else {
    				/* Enviou o último RTS, rtsSeqNum = 0.
    				 * Fim da sequência de RTS, não é mensagem BroadCast e nó de destino não respondeu  */
    				if (xState.getRtsPkt().getReceiver() != NodeId.ALLNODES) {
    					/* Irá para CS e depois para SLEEP.   */
    					changeState = setNewState(EXMacStateType.CS, xConf.getStepsCS(), EXMacActionType.TURN_ON);
    					if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_CTS - Fim de RTS, sem resposta <--------------" );
    				} else {
    					/* Fim da sequência de RTS, mensagem BroadCast, todos os vizinhos estão acordados.
    					 * Logo pode enviar a mensagem.                                                    */
    					//System.err.println("COMECEI ENVIAR EM: " + SimulationManager.getInstance().getCurrentTime() + " steps");
    					changeState = setNewState(EXMacStateType.SENDING_DATA, xConf.stepsDelayTx(PacketType.DATA), EXMacActionType.MSG_DOWN);
    					if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_CTS - Mensagem Broadcast - envia DATA ");
    				}
    			}
    		break;
    			
    		case CTS_RECEIVED:
    			/* O número do preâmbulo é copiado na mensagem de CTS pelo vizinho que responde.  */
                if (xState.getRecPkt().getRetryCount() == xState.getRtsPkt().getRetryCount()) { 
                	/* Avisa XMAC que tem um pacote DATA a ser enviado em seguida   */
    				changeState = setNewState(EXMacStateType.SENDING_DATA, xConf.stepsDelayTx(PacketType.DATA), EXMacActionType.MSG_DOWN);
    				if(debug) System.out.println("Machine: " + address.getId() + " State = WAITING_CTS, Event = CTS_RECEIVED " );
                } 
                else{
                	changeState = goToSleep(EXMacActionType.TURN_OFF);
                	if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_CTS, Event = CTS_RECEIVED, sequencia errada = "
        					                 + xState.getRecPkt().getRetryCount() + " ** ");
                } 
            break;
                
            default:
            	if(debug) System.out.println("Machine: " + address.getId() + " State = WAITING_CTS, evento não previsto = " + event);
            break;
    		}
    		
        break;
            
        /** -------------- Estado Esperando DATA - Espera o rádio receber um DATA  ---------------- **/
        
        case WAITING_DATA: 
         	/* MAC está esperando receber uma mensagem DATA pelo rádio.   */ 
    		switch (event){
    		case TIME_OUT:
    			System.err.println("PERDI A MSG");
    			/* Acabou o tempo e não recebeu o pacote de dados, então o próximo estado será CS, 
    			 * pois talvez outro nó queira transmitir uma mensagem                             */
     			changeState = goToSleep(EXMacActionType.TURN_OFF);
     			if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_DATA, Event = TIME_OUT" );
    		break;
    			
    		case DATA_RECEIVED:
    			/* Envie ACK se foi requisitado.
    			 * Ou envie DATA para a Log Link Layer e vá pra o CS_END  */
    			if (xState.getRecPkt().isAckRequested()){
    				changeState = setNewState(EXMacStateType.SENDING_ACK, xConf.stepsDelayTx(PacketType.ACK), EXMacActionType.MSG_DOWN);
    			}
    			else {
    				changeState = setNewState(EXMacStateType.CS_END, xConf.getStepsCS(), EXMacActionType.MSG_UP);
    			}
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_DATA, Event = DATA_RECEIVED" );
    		break;
    			
    		default:
    			if(debug) System.out.println("Machine: " + address.getId() + " State = WAITING_DATA, evento não previsto = " + event);
            break;
		}
    		
        break;
    		
        /** -------------- Estado Esperando ACK - Espera o rádio receber um ACK  ---------------- **/
        
        case WAITING_ACK: 
        	/* Esperava um ACK depois de enviar DATA       */
        	switch (event){
    		
    		case TIME_OUT:
    			/* Acabou o tempo e não recebeu ACK. A comunicação falhou              */
    			changeState = goToSleep(EXMacActionType.TURN_OFF);
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_ACK, Event = TIME_OUT" );
    		break;
    			
    		case ACK_RECEIVED:
    			/* Recebeu ACK. Sucesso na comunicação. Verifique se outro nó quer enviar mensagem     */
    			xState.setDataPending(false);
   				changeState = setNewState(EXMacStateType.CS_END, xConf.getStepsCS(), EXMacActionType.TURN_ON);
   				if(debug) System.out.println("Machine: " + address.getId()+ " State = WAITING_ACK, Event = ACK_RECEIVED" );
            break;
                
            default:
            	if(debug) System.out.println("Machine: " + address.getId() + " State = WAITING_ACK, evento não previsto = " + event);
            break;
   		}
   		break;

   		/** --- Estado Back Off de Início - Back Off para iniciar envio de RTS quando canal está ocupado --- **/
        
        case BO_START:
        	/* Back Off porque o rádio estava ocupado ao tentar iniciar processo de comunicação. */
        	switch (event){
    		
    		case TIME_OUT:
    			if (xState.decRetryCSstart() == 0){
    				/* tentou iniciar envio de RTS várias vezes, mas o canal está ocupado. Reiniciará o processo quando acordar. */
    				changeState = goToSleep(EXMacActionType.TURN_OFF);
    			}
    			else {
    				/* tentou iniciar envio de RTS, mas o canal está ocupado. Tentará mais uma vez  */
    				changeState = setNewState(EXMacStateType.CS_START, xConf.getStepsCS(), EXMacActionType.ASK_CHANNEL);
    			}
    			if(debug) System.out.println("Machine: " + address.getId()+ " State = BO_START, Event = TIME_OUT, Retry = " + xState.getRetryCSstart() );
        	break;
            
    		default:
    			if(debug) System.out.println("Machine: " + address.getId() + " State = BO_START, evento não previsto = " + event);
    		break;
		}
		break;
        	
		/** ------------------ Estado não previsto ------------------- **/
		
        default:
        	if(debug) System.out.println("Machine: " + address.getId() + " Estado não previsto. State = " + xState.getState());
    	break;
    	
    	} // Fim de lista de estados

    	return changeState;  
    	
    }  // Final da função  changeState
    
    
    private boolean goToSleep(EXMacActionType action) {
    	double operationSteps = SimulationManager.getInstance().getCurrentTime() - stepsFromLastSleep;
    	return setNewState(EXMacStateType.SLEEP, xConf.getStepsSleep(), action);
    }
    
    /** Função para decidir o que fazer quando receber um RTS.     
     */ 
    public boolean changeStateRTSreceived(EXMacStateType oldState) {
    	boolean changeState;

		/* Se recebeu uma mensagem RTS durante o CS. RTS é a o único tipo de mensagem que importa aqui  */
		if (xState.getRecPkt().getReceiver().equals(this.address.getId())){
	    	// Vai avisar a XMac que tem um pacote CTS a ser enviado em seguida
	        changeState = setNewState(EXMacStateType.SENDING_CTS, xConf.stepsDelayTx(PacketType.CTS), EXMacActionType.MSG_DOWN);
	        if(debug) System.out.println("Machine: " + address.getId()+ " State = " + oldState + ", Event = RTS_RECEIVED unicast" );
		}
	    /* Se a mensagem é de Broadcast, então não precisa responder com CTS. 
	     * Vá para Sleep até que chegue a hora do envio de DATA.
	     * Espera = (número de RTS que faltam) * (intervalo entre envios de RTS) */
	    else if (xState.getRecPkt().getReceiver() == NodeId.ALLNODES){
	    	double delay = xState.getRecPkt().getRetryCount() 
	    			* (xConf.getStepsRTS() + xConf.stepsDelayRx(PacketType.CTS)) - xConf.getStepsRTS();
	    	changeState = setNewState(EXMacStateType.SLEEP, delay, EXMacActionType.TURN_OFF);
	    	xState.setWaitingBroadcast(true);
	    	if(debug) System.out.println("Machine: " + address.getId()+ " State = " + oldState + ", Event = RTS_RECEIVED broadcast" );
	    }
	    else {
	    	/* RTS não é para este nó  */
	    	changeState = goToSleep(EXMacActionType.TURN_OFF);
	    	if(debug) System.out.println("Machine: " + address.getId()+ " State = " + oldState + ", Event = RTS_RECEIVED para outro nó" );
	    }
		return changeState;
    }
    
    /** Função que faz a primeira mudança de estado do XMac. O nó irá entrar em modo SLEEP 
     *  por um tempo aleatório, antes de iniciar suas atividades 
     */ 
    public void changeStateBootNode() {
    	// Se for necessário testar com seed fixa para cada nó
    	// Random gerador = new Random(address.getId().asInt());
    	// int delay = gerador.nextInt(Integer.valueOf((int) Math.round(xConf.getStepsCycle())));
    	double delay;
    	if (xConf.isBackboneNode()) {
    		delay = xConf.getStepsSleep();
    	} else {
    		// Tempo pequeno aleatório em steps para ligar o nó sensor
        	delay = (Math.random() * (xConf.getStepsCycle()));
    	}
    	xState.setState(EXMacStateType.SLEEP, delay);
    }
  	
}

