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

package br.ufla.dcc.PingPong.AgaMac;


import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.Phy.ColisionDetectEvent;
import br.ufla.dcc.PingPong.Phy.EventCarrierSense;
import br.ufla.dcc.PingPong.Phy.EventPhyTurnRadio;
import br.ufla.dcc.PingPong.Phy.StartOfFrameDelimiter;
import br.ufla.dcc.grubix.simulator.node.Link;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.MACState;
import br.ufla.dcc.PingPong.node.RadioState;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
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
 *  Protocolo X-MAC
 *
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 04/07/2016
 */

public class AgaMac extends MACLayer {
	/** Tempo de ciclo em segundos (obrigatório estar aqui) */
	@ShoXParameter(description = " Tempo de Ciclo em segundos")
	private double cycleTime;
	
	/** +AGAMac Limiar padrão para decidir se envia CTS em resposta a um RTS, 
	 0.001 < ThreShold < 1.000 */
	@ShoXParameter(description = " Limiar de decisão para um vizinho enviar um ACK a um Preâmbulo ", 
			defaultValue = "0.5")
	private double threshold;
	
	/** Objeto responsável pela mudança do estado do AGAMac */
    private AgaMacStateMachine agaStateMachine;

    /** Objeto que contém todas as variáveis de configuração do AGAMac */
    private AgaMacConfiguration agaConf;

    /** Objeto que armazenas as informações de estados do AGAMac */
    private AgaMacState agaState;

    /** Informação, mantida na MAC, sobre o estado do rádio da PHY */
    private AgaMacRadioState agaRadioState;
    
    /** The internal state of the MAC that can be modified from outside.
     * Apenas para manter compatibilidade com Shox */ 
    private MACState macState;

    
    /** Objeto para realizar a depuração */
 	ToolsDebug debug = ToolsDebug.getInstance();
    
    
    /**
     * Inicializa a configuração do objeto
     * @throws ConfigurationException
     *      thrown when the object cannot run with the configured values.
     */
    public void initConfiguration(Configuration config) throws ConfigurationException {
        super.initConfiguration(config);

        /* Declaração obrigatória no Grubix (MAC_IEEE802_11bg_AdHoc)
           Param1: Política de encaminhamento
           Param2: Potência máximo de transmissão
           Param3: Número de pacotes a serem transmitidos em uma fila */
        macState = new MACState(raDefaultPolicy, 16.0, 0);

        // Define o estado inicial do MAC em SLEEP e o contador de sequência de estados em zero.
        agaState = new AgaMacState(AgaMacStateTypes.SLEEP, 0);         

        // Define o estado inicial do rádio
        agaRadioState = new AgaMacRadioState(RadioState.OFF);

        // Inicializa as variáveis de configuração
        agaConf = new AgaMacConfiguration(cycleTime);
        
        // Cria a instância da máquina de estados
        agaStateMachine = new AgaMacStateMachine(sender, agaState, agaRadioState, agaConf);
    }
    
    
    /**
     *  Função para iniciar o funcionamento do nó sensor.
     *  Marcar um tempo aleatório para ligar o rádio pela primeira vez.
     */
    protected void processEvent(StartSimulation start) {
    	// +AGAMac Guarda na agaMacState o limiar definido na configuração
        agaState.setThreshold(threshold);
    	
        // Define o novo estado e duração, e incrementa a sequência de estado
        agaStateMachine.changeStateBootNode();
    	createtWucTimeOut();
    }
    
    
    /**
     *  Função que cria um WUC do tipo WucTimeOut definindo a duração do novo estado e operação 
     *  do rádio.
     */
    AgaMacWucTimeOut createtWucTimeOut() {
		AgaMacWucTimeOut wuc = new AgaMacWucTimeOut(sender, agaState.getStateDuration(), agaState.getStateSeqNum());
		sendEventSelf(wuc);
		// Cria evento para camada física desligar ou ligar rádio
		setPhyRadioOperation();
		// Debug, escreve entrada no arquivo de depuração somente para o nó com id=1
		debug.writeIfNodes(debug.strWucTimeOut(wuc), sender, new Integer[]{1});
		return wuc;
    }
    
    
    /**
     *  Função para tratar eventos de final de marcação de tempo, quando termina wuc's WucTimeOut
     *  Chama a função changeStateTimeOut da AGAMacStateMachine para indicar qual será o próximo estado 
     *  da AGAMac. Será indicado o tempo limite para permanecer neste estado. Se houver mensagens a 
     *  serem transmitidas, será indicado o tipo dessa mensagem, se diferente de null.
     */
    public final void proceedWucTimeOut(AgaMacWucTimeOut TimeOut) {
    	// Debug, escreve entrada no arquivo de depuração somente para o nó com id=1
    	debug.writeIfNodes(debug.strAgaLayer(agaState, agaRadioState)+
    			debug.strWucTimeOut(TimeOut), sender, new Integer[]{1});
    	
    	// Chama a AGAMacStateMachine para definir qual o próximo estado e a duração (delay)
        if (!agaStateMachine.changeStateTimeOut(TimeOut))
        	return;

        /* Se deverá reiniciar o processo de envio de RTS, começa o processo startSendDataProcess
         * e sai da função, pois startSendDataProcess já irá criar um novo evento  WucTimeOut */
        if (agaState.isRestartRTS()) {
        	startSendDataProcess();
        	return;
        }

        // Envia o tipo de pacote informado pelo getSendingPktType
        sendAGAMacPacket();
        // Inicia o WUC do novo estado
        createtWucTimeOut();
    }
    
    
    /**
     *  Função padrão do Grubix para tratar eventos WakeUpCall
     *
     *   Tipos de WakeUpCall:
     *  --> WucTimeOut = marcação de tempo de SLEEP, de Carrier Sense e espera por ACK ou DATA.
     *  --> CrossLayerEvent = eventos emitidos pela Physical Layer para a MAC, com informações do rádio.
     *  --> WucBackOff = tempo de backoff quando o rádio está ocupado ao enviar uma mensagem.
     */
    public final void processWakeUpCall(WakeUpCall wuc) {
    	// Debug, escreve entrada no arquivo de depuração somente para o nó com id=1
    	debug.writeIfNodes(debug.strAgaLayer(agaState, agaRadioState)+debug.strWuc(wuc), sender, 
    			new Integer[]{1});

    	// Se o tempo restante da simulação for menor que um ciclo, termina o processo
        if ((limitTimeSteps() - currentTimeSteps()) < (agaConf.getStepsCycle()))
            return;
    	
    	// Tratamento de evento de fim de marcação de tempo
        if (wuc instanceof AgaMacWucTimeOut) {
        	proceedWucTimeOut((AgaMacWucTimeOut)wuc);
        }
        // Tratamento dos eventos emitidos pela PHY.
        if (wuc instanceof CrossLayerEvent) {
            proceedCrossLayerEvent((CrossLayerEvent)wuc);
        }
        // Tratamento do evento de nova tentativa de envio de mensagens, quando o rádio estiver ocupado.
        if (wuc instanceof AgaMacWucBackOff) {
            // Recupera o tempo da primeira tentativa de envio
            double firstTime = ((AgaMacWucBackOff)wuc).getStartingTime();
            /* Se o tempo desde a primeira tentativa é maior que um ciclo, o nó não tentará novamente.
             * A aplicação deverá tratar essa situação e talvez reenviar DATA num tempo posterior. */
            if ((currentTimeSteps() - firstTime) < agaConf.getStepsCycle()) {
            	if (!startSendDataProcess()) {
                    AgaMacWucBackOff wucRetry = new AgaMacWucBackOff(sender, agaConf.getStepsCS(), firstTime);
                    sendEventSelf(wucRetry);
                }
            } else {
            	debug.printw("AVISO! Tempo de BackOff maior que um ciclo", sender);
            }
        }
    }
	
    
	/**
	* Função padrão do GrubiX para tratar pacotes recebidos da camada superior (LogLink)
	* Após receber um pacote da LogLink, é chamada a função de início de transmissão de pacote.
	*/
	public final void upperSAP(Packet packet) {
		// Debug, escreve entrada no arquivo de depuração
		debug.write(debug.strAgaLayer(agaState, agaRadioState)+debug.strPkt(packet), sender);
		
		// Se o pacote é para o próprio nó, manda de volta para a camada acima
		if (packet.getReceiver() == id) {
			// Muda a direção do pacote para que seja enviado para cima
			packet.flipDirection();
			sendPacket(packet);
			return;
		}
		sendDataPacketDown (packet);
	}
	
	
    /**
     *  Função padrão do GrubiX para tratar pacotes recebidos da camada inferior.
     *  Após a PHY terminar de receber uma mensagem pelo rádio, envia para a MAC.
     *  A MAC chamará a função de tratamento da mensagem recebida.
     */
    public final void lowerSAP(Packet packet) {
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.strAgaLayer(agaState, agaRadioState)+debug.strPkt(packet), sender);
    	
        if (!(packet instanceof AgaMacPacket)) {
        	debug.printw("AVISO! Mensagem desconhecida: "+packet.getClass().getName(), sender);
            return;
        }

        // Ignorar caso a mensagem foi gerada pelo próprio nó
        if (packet.getSender().getId() == getNode().getId()) 
        	return;

        // Mudar o estado conforme o tipo de mensagem recebida
        agaStateMachine.changeStateReceivedMsg((AgaMacPacket) packet);
        
        /* +AGAMac Após receber um pacote vindo da PHY, poderá ser gerado outro pacote a ser enviado. 
         Por exemplo, ao receber um RTS, deverá enviar um CTS. No AGAMac, o estado WILL_SEND_MSG será 
         usado antes de enviar um CTS. Neste caso, haverá um pacote CTS a ser enviado, mas posteriormente. */
        if (agaState.getState() != AgaMacStateTypes.WILL_SEND_MSG) 
        	sendAGAMacPacket();
        	
        createtWucTimeOut();
    }
    
    
	/**
     *  Inicia o processo de envio de um pacote de DATA recebido da camada LogLink (superior).
     *  Depois devem ser seguidos os passos:
     *  1) Construir uma mensagem de dados contendo o pacote recebido da LogLink;
     *  2) Colocar o número de sequência nesta mensagem;
     *  3) Chamar a função startSendDataProcess;
     */
    private boolean sendDataPacketDown(Packet packet) {

        /* Objeto LogLink para pegar o valor da potência de transmissão que será usada pela camada 
         superior LogLinkLayer, enquanto não for usado outro método para definir SignalStrength */
        LogLinkPacket llp = (LogLinkPacket) packet;
        Link link = llp.getMetaInfos().getDownwardsLLCMetaInfo().getLink();
        agaConf.setSignalStrength(link.getTransmissionPower());
        
        /* Para qual nó será enviado a mensagem, virá da LogLinkLayer a informação sobre o next hop, 
         mas será desprezada no AGA-MAC */
        agaState.setNextReceiverNode(llp.getReceiver());
        
        /* Esquema de contagem cíclica para não criar sequência infinita
         Número de sequência da mensagem a ser enviada, limitado em 2^16 */
        agaState.setDataSeqNum((agaState.getDataSeqNum()+1) & agaConf.getMaxSequence());
        
        /** Cria o pacote de DATA da camada MAC para ser enviado depois dos preâmbulos.
         * setAckRequested() e setRetryCount() estão definidos em createDataPacket() abaixo */
        AgaMacPacket newDataPacket = createDataPacket(sender, llp, agaState.getDataSeqNum(), true);
        
        // +AGAMac Armazena limiar inicial no pacote
        newDataPacket.setCurrentThreshold(agaState.getThreshold());
        
        // +AGAMac Define o nó do destino final
        agaState.setLastReceiver(newDataPacket.getEnclosedPacket(LayerType.APPLICATION).getReceiver());
        newDataPacket.setLastReceiver(agaState.getLastReceiver());
        
        // Atribui o pacote que será enviado
        agaState.setDataPkt(newDataPacket);
        
        // Debug, escreve entrada no arquivo de depuração
        debug.write(debug.str("Pacote DATA criado")+debug.strAgaLayer(agaState, agaRadioState)+
        		debug.strPkt(newDataPacket), sender);
        
        // Se não iniciou o processo de envio do pacote de dados
        if (!startSendDataProcess()) {
            /* Se o rádio estiver ocupado, ou desligado, tente mais tarde após certo tempo.
            Tempo igual a StepsCS, mas poderia ser outro. */
            AgaMacWucBackOff wucRetry = new AgaMacWucBackOff(sender, agaConf.getStepsCS(), currentTimeSteps());
            debug.write(debug.strWucBackOff(wucRetry), sender);
            sendEventSelf(wucRetry);
        }
        return true; 
    }

    
    /**
     *  Função para iniciar o processo de envio de um pacote da AGAMac.
     *  Inicialmente verifica-se se o rádio pode iniciar uma transmissão, estado OFF ou LISTENING.
     *  Depois devem ser seguidos os passos:
     *  1) Construir uma mensagem RTS (preâmbulo);
     *  2) Colocar o número de sequência da mensagem RTS;
     *  3) Colocar o número do RTS como sendo o máximo previsto para alcançar o destinatário;
     *  4) Iniciar a escuta do canal
     */
    protected boolean startSendDataProcess() {   
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.strAgaLayer(agaState, agaRadioState)+debug.strPkt(agaState.getDataPkt()), sender);
    	
    	// Início do envio da sequência de RTS, portanto não precisa reiniciar a sequência de envio
    	agaState.setRestartRTS(false);
    	
    	// Verifica se já fez todas tentativas de envio
        if (agaState.getDataPkt().getRetryCount() == 0) {
        	debug.printw("AVISO! Tentativas de envio de DATA esgotado", sender);
        	return false;
        }
        
        // Decrementa a tentativa de envio de DATA
        agaState.getDataPkt().decRetryCount();
        
        /* Se rádio não estiver OFF ou LISTENING, é porque está em processo de envio ou recepção e 
    	 portanto não pode enviar outra mensagem */
        if ((agaRadioState.getRadioState() != RadioState.OFF) && 
        		(agaRadioState.getRadioState() != RadioState.LISTENING)) {
        	debug.write("Rádio ocupado: "+agaRadioState.getRadioState(), sender);
            return false;
        }  
        
        // +AGAMac Cria um pacote de preâmbulo (RTS) para ser enviado após CS_START
        AgaMacPacket newRtsPacket = createAuxPacket(sender, NodeId.ALLNODES, 
        		agaConf.getSignalStrength(), PacketType.RTS, agaConf.getLengthRTS(), 
        		agaConf.getMaxPreambles(), true);

        // Define o destino final, que está informado no pacote
        newRtsPacket.setLastReceiver(agaState.getDataPkt().getLastReceiver()); 
        
        // +AGAMac Tem que retornar ao valor original depois de passar por uma região ruim.
        agaState.setCurrentThreshold(agaState.getThreshold());
        
		if (agaState.getDataPkt().getRetryCount() < (agaConf.getMaxSendRetries() - 1)) {
			// ajustar para o valor mínimo
			agaState.setCurrentThreshold(0.001);					
			debug.printw("AVISO! Abaixou o threshold para: "+ agaState.getCurrentThreshold(), sender);
		}
		// +AGAMac
		newRtsPacket.setCurrentThreshold(agaState.getCurrentThreshold()); 

        // Manter referência para o RTS na AGAMacstate
        agaState.setRtsPkt(newRtsPacket);
        
        // Definir o tipo de pacote que será enviado
        agaState.setSendingPktType(PacketType.RTS);
        
        /* Irá mudar para o estado do AGAMac para CS_START e do rádio para LISTENING.
         * Estado do rádio na PHY será mudado em createtWucTimeOut */
        agaStateMachine.changeStateSendingMsg();
        
        // Cria evento para ser executado após duração de CS_START
        createtWucTimeOut();
        
        // Início do envio da sequência de RTS, portanto não precisa reiniciar a sequência de envio
    	agaState.setRestartRTS(false);
    	
    	return true;
    }
    
    
    /**
     *  Função para enviar o pacote
     */
    protected void sendAGAMacPacket() {
    	
    	// Se não possui mensagem para enviar, sai sa função
    	if (agaState.getSendingPktType() == null)
    		return;
    	
    	AgaMacPacket packet = null;
    	
    	switch (agaState.getSendingPktType()) {
    		// Quando termina o tempo de espera por um CTS, deve reenviar os preâmbulos
    	case RTS:
    		packet = agaState.getRtsPkt();
    		break;
    		// CTS em resposta a um RTS. Tem que ser criado, depois enviado para a PHY	
        case CTS: 
        	packet = createAuxPacket(sender, agaState.getNextReceiverNode(), 
        			agaConf.getSignalStrength(), PacketType.CTS, agaConf.getLengthCTS(), 
        			agaState.getCtsSeqNum(), false);
        	break;
        	/* Poderá ser do tipo ACK, em resposta a um DATA. ACK tem que ser criado, depois enviado 
        	 para a PHY */
        case ACK: 
        	packet = createAuxPacket(sender, agaState.getNextReceiverNode(), 
        			agaConf.getSignalStrength(), PacketType.ACK, agaConf.getLengthACK(), 
        			agaState.getAckSeqNum(), false);
        	break;
        	/* O pacote DATA atual está guardado em AGAMacState. 
         	Pode ser DATA a enviar ou DATA recebido. No segundo caso enviar para a LogLink */
        case DATA:
        	packet = agaState.getDataPkt();
        	// +AGAMac
        	if (packet.getDirection() == Direction.DOWNWARDS) {
        		packet.setReceiver(agaState.getNextReceiverNode());
        	} else if (agaState.getDataPkt().getLastReceiver() != node.getId()) {
        		debug.write("Não sou destino final de DATA", sender);
        		packet.setSender(getSender());
        		packet.setRetryCount(agaConf.getMaxSendRetries());
        		packet.flipDirection();
        		startSendDataProcess();
        	}
        	break; 	
        	
        default:
        	// Debug, escreve entrada no arquivo de depuração
        	debug.printw("AVISO! Mensagem de tipo desconhecido: "+agaState.getSendingPktType(), sender);
        	return;
    	}
    	
    	// Se for DATA com direção para camada acima, envia o pacote encapsulado
    	if (agaState.getSendingPktType() == PacketType.DATA && 
    			packet.getDirection() == Direction.UPWARDS &&
    			agaState.getDataPkt().getLastReceiver() == node.getId()) {
    		AgaMacStatisticsRecord.getInstance().terminar = true;
    		sendPacket(packet.getEnclosedPacket());
    	} else {
    		sendPacket(packet);	
    	}
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.printw(debug.str("Enviando "+agaState.getSendingPktType())+debug.strPkt(packet), sender);
    	
    	// Após enviar uma mensagem define o tipo de mensagem novamente como null
    	this.agaState.setSendingPktType(null);
    }
    
    
    /**
     *  Função para tratar eventos vindos da PHY.
     *  Existem 3 eventos previstos:
     *  -> SendingTerminated: A PHY sinaliza o final da transmissão de uma mensagem
     *  -> StartOfFrameDelimiter: No início de uma mensagem de rádio é colocado o sinalizador 
     *  		SFD (start frame delimiter). A PHY sinaliza a recepção do SFD
     *  -> ColisionDetectEvent:  A PHY sinaliza a ocorrência de colisão de mensagens recebidas 
     *  		pelo rádio. Em SendingTerminated o pacote enviado pela MAC para a PHY é retornado 
     *  		para a MAC, para conferência. A rigor, o SFD não traz nenhuma informação da mensagem.
     *  		A MAC deveria apenas esperar o final da recepção. O envio do pacote juntamente com 
     *  		o sinal SFD, aqui, é utilizado apenas para facilitar o uso do temporizador de espera 
     *  		de MSG. ColisionDetectEvent é utilizado para encerrar a espera de um pacote que 
     *  		estava sendo recebido pela PHY.
     */
    public final void proceedCrossLayerEvent(CrossLayerEvent cle) {
    	
    	// Debug, escreve entrada no arquivo de depuração
    	debug.write(debug.strAgaLayer(agaState, agaRadioState)+debug.strEventCrossLayer(cle), sender);
    	
    	// Recebe evento da PHY informando se o canal está ocupado
        if (cle instanceof EventCarrierSense) {
        	debug.write(debug.str("Evento: EventCarrierSense"), sender);
        	// Aqui a PHY envia uma resposta à consulta sobre atividade no canal.
        	if (((EventCarrierSense) cle).isChannelBusy()) {
        		debug.printw("AVISO! Camada PHY indica canal ocupado", sender);       
        	}
        }
        
    	// A seguir somente será permitido eventos que carregam pacotes do tipo _AGAMacPacket
        if (!(cle.getPacket() instanceof AgaMacPacket)) {
        	String className = cle.getPacket() != null ? cle.getPacket().getClass().getName() : "vazio";
        	debug.printw("AVISO! Mensagem desconhecida: "+className, sender);
            return;
        }
               
        AgaMacPacket packet = (AgaMacPacket)cle.getPacket();
    	
        // Recebe evento da PHY quando uma mensagem é completamente enviada.
        if (cle instanceof SendingTerminated) {
        	debug.write(debug.str("Evento: SendingTerminated"), sender);
            agaStateMachine.changeStateSentMsg(packet);
            /* Envia o tipo de pacote informado pelo setSendingPktType. No caso de acabar de enviar 
             um ACK, deverá enviar DATA para a LOGLINK */
            sendAGAMacPacket();
            createtWucTimeOut();
        }
        // Recebe evento da PHY quando uma mensagem está chegando
        if (cle instanceof StartOfFrameDelimiter) {
        	debug.write(debug.str("Evento: StartOfFrameDelimiter"), sender);
            if ((packet.getReceiver() == id) || (packet.getReceiver() == NodeId.ALLNODES)) {
                if (agaState.getState() == AgaMacStateTypes.WILL_SEND_MSG) { 
                	/* +AGAMac Significa que estava fazendo BackOff antes de enviar CTS, mas outro nó 
                	 enviou CTS, então vá dormir. */
                	agaStateMachine.changeStateReceivingMsg();
                } else {
                	/* Irá manter o estado atual do AGAMac, até o final da recepção da mensagem pela PHY */
                    agaStateMachine.changeStateReceivingMsg(packet);
                }
                createtWucTimeOut(); 
            }
        }
        // Recebe evento da PHY quando ocorre uma colisão na recepção de uma mensagem
        if (cle instanceof ColisionDetectEvent) {
        	debug.write(debug.str("Evento: ColisionDetectEvent"), sender);
            if (packet.getReceiver() == id) {
            	debug.write("Colisão informada pela PHY", sender);
            }
        }
    }
    
    
    /** Função para criar pacotes terminais (RTS, CTS, ACK) */
    AgaMacPacket createAuxPacket(Address sender, NodeId id, double signalStrength, PacketType type, 
    		int headerLength, int sequence, boolean ACKreq) {
    	AgaMacPacket newPack = new AgaMacPacket(sender, id, signalStrength, type);
    	newPack.setAckRequested(ACKreq);
    	newPack.setHeaderLength(headerLength);
    	newPack.setSequenceNumber(sequence);
    	return newPack;
    }
    
    
    /** Função para criar pacotes do tipo DATA */
    AgaMacPacket createDataPacket(Address sender, LogLinkPacket packet, int sequence, boolean ACKreq) {
    	AgaMacPacket newPacket = new AgaMacPacket(sender, packet, sequence) ;
        newPacket.setHeaderLength(agaConf.getLengthDATA());
        // Se ACK deve ser enviado depois que DATA chegar no receptor
        newPacket.setAckRequested(ACKreq);
        // Número de tentativas para enviar DATA
        newPacket.setRetryCount(agaConf.getMaxSendRetries());
    	return newPacket;
    }
    
    
    /** Função para mandar a Physical Layer ligar e desligar o rádio
     *  Ligar = true; Desligar = false. */
    protected void setPhyRadioOperation() {
    	boolean isTurnOn = true;
    	if (agaRadioState.getRadioState() == RadioState.OFF) 
    		isTurnOn = false;
        sendEventDown(new EventPhyTurnRadio(this.sender, isTurnOn));
    }
    
    
    /** Função para obter o tempo corrente da simulação em steps */
    protected double currentTimeSteps() {
        return SimulationManager.getInstance().getCurrentTime();
    }
    
    
    /** Função para obter o tempo limite de simulação em steps */
    protected double limitTimeSteps() {
        return Configuration.getInstance().getSimulationTime();
    }
    
    
    /** Apenas para manter compatibilidade com Shox. */
    @Override
    public MACState getState() {
        return this.macState;
    }
    
}



/* TODO
 * Remover linha: br.ufla.dcc.PingPong.movement.FromFileStartPositions
 * 
 * 
 */
