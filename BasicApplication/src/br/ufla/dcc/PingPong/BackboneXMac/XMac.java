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

package br.ufla.dcc.PingPong.BackboneXMac;


import static br.ufla.dcc.PingPong.routing.BackboneRoutingPacketType.SWITCH_SELF_AND_BROADCAST;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.BackboneXMac.Stats.Simulation;
import br.ufla.dcc.PingPong.Phy.ColisionDetectEvent;
import br.ufla.dcc.PingPong.Phy.EventCarrierSense;
import br.ufla.dcc.PingPong.Phy.EventPhyTurnRadio;
import br.ufla.dcc.PingPong.Phy.StartOfFrameDelimiter;
import br.ufla.dcc.PingPong.node.AppPacket;
import br.ufla.dcc.PingPong.node.RadioState;
import br.ufla.dcc.PingPong.routing.BackboneRoutingPacket;
import br.ufla.dcc.PingPong.routing.ExpandedBackboneRoutingPacket;
import br.ufla.dcc.PingPong.testing.SingletonTestResult;
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
import br.ufla.dcc.grubix.simulator.node.Link;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.MACState;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 *  Protocolo X-MAC
 *
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 22/06/2016
 */

public class XMac extends MACLayer {
	
	/** Tempo de ciclo em segundos (obrigatório estar aqui) */
	@ShoXParameter(description = " Tempo de Ciclo em segundos")
	private double cycleTime;
	
	/** Se é para usar o ACK */
	@ShoXParameter(description = " Se após o envio do dado o nó fica esperando por um ACK")
	private boolean ackEnable;
	
	/** Tempo de formação do backbone em ciclos (obrigatório estar aqui) */
	@ShoXParameter(description = " Tempo de formação do backbone em ciclos")
	private double backboneFormingTime;
	
	/** Objeto responsável pela mudança do estado do XMac */
    private XMacStateMachine xStateMachine;

    /** Objeto que contém todas as variáveis de configuração do XMac */
    private XMacConfiguration xConf;

    /** Objeto que armazenas as informações de estados do XMac */
    private XMacState xState;

    /** Informação, mantida na MAC, sobre o estado do rádio da PHY */
    private XMacRadioState xRadioState;
    
    /** The internal state of the MAC that can be modified from outside.
     * Apenas para manter compatibilidade com Shox */ 
    private MACState macState;
    
    /** Ferramentas para imprimir informações para a depuração do programa */
 	ToolsDebug debug = ToolsDebug.getInstance();
    
 	/** Ferramentas para guardar e imprimir informações sobre as estatísticas da simulação */
 	//static ToolsStatisticsSimulation statistics = ToolsStatisticsSimulation.getInstance();
 	
 	/** Ferramentas para guardar e imprimir informações sobre as estatísticas da simulação */
 	static Simulation statistics = Simulation.getInstance();
 	
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
           Param1: Política de encaminhamento
           Param2: Potência máximo de transmissão
           Param3: Número de pacotes a serem transmitidos em uma fila */
        macState = new MACState(raDefaultPolicy, 16.0, 0);
        
        // Define o estado inicial do MAC em SLEEP e o contador de sequência de estados em zero.
        xState = new XMacState(XMacStateTypes.SLEEP, 0);
        xState.id = this.node.getId();

        // Define o estado inicial do rádio
        xRadioState = new XMacRadioState(RadioState.OFF);

        // Inicializa as variáveis de configuração
        xConf = new XMacConfiguration(cycleTime, ackEnable);
        
        // Cria a instância da máquina de estados
        xStateMachine = new XMacStateMachine(sender, xState, xRadioState, xConf);
        
        // Ferramentas de depuração -------------------------
        if (statistics.xConf == null) {
        	//debug.print(debug.strPaxConf(paxConf), sender);
    		statistics.xConf = xConf;
        }
        
    }
    
    
    /**
     *  Função para iniciar o funcionamento do nó sensor.
     *  Marcar um tempo aleatório para ligar o rádio pela primeira vez.
     */
    /*
    protected void processEvent(StartSimulation start) {
    	// Define o novo estado e duração, e incrementa a sequência de estado
    	xStateMachine.changeStateBootNode();
    	createtWucTimeOut();
    }
    */
    
    protected void processEvent(StartSimulation start) {
    	// Define o novo estado e duração, e incrementa a sequência de estado
    	xStateMachine.changeStateBootNode();
    	
    	createWucTimeOut();
    	
    	XMacNormalizeWUC xnwuc = new XMacNormalizeWUC(sender, backboneFormingTime);
    	
    	StuckNodeWatchdogWUC snwwuc = new StuckNodeWatchdogWUC(sender, backboneFormingTime - 1);
    	
    	sendEventSelf(xnwuc);
    	sendEventSelf(snwwuc);
    }
    
    private void switchToBackbone() {
    	boolean willConvert = !xState.isBackboneNode();
    	xState.setBackboneNodeState(willConvert);
		xConf.adjustLengthCycle(willConvert);
    	if (willConvert) {
    		misc.vGrubix(node.getId(), "Backbone", "DARK_BLUE");
    		SingletonTestResult.getInstance().countBackboneNode();
    	} else {
    		misc.vGrubix(node.getId(), "Ex-Backbone", "LIGHT_GREEN");
    	}
    }
    
    /**
     *  Função que cria um WUC do tipo WucTimeOut definindo a duração do novo estado e operação 
     *  do rádio.
     */
    XMacWucTimeOut createWucTimeOut() {
    	double delay = xState.getStateDuration();
    	if (delay > xConf.getStepsCycle() * 3 || delay < 0) {
    		delay = xConf.getStepsCycle() * 3;
    	}
    	XMacWucTimeOut wuc = new XMacWucTimeOut(sender, delay, xState.getStateSeqNum());
		//XMacWucTimeOut wuc = new XMacWucTimeOut(sender, xState.getStateDuration(), xState.getStateSeqNum());
		sendEventSelf(wuc);
		// Cria evento para camada física desligar ou ligar rádio
		setPhyRadioOperation();	
		// Debug, escreve entrada no arquivo de depuração somente para o nó com id=1
		debug.writeIfNodes(debug.strWucTimeOut(wuc), sender, new Integer[]{1});
		return wuc;
    }
    
    
    /**
     *  Função para tratar eventos de final de marcação de tempo, quando termina wuc's WucTimeOut
     *  Chama a função changeStateTimeOut da XMacStateMachine para indicar qual será o próximo estado 
     *  da XMAC. Será indicado o tempo limite para permanecer neste estado. Se houver mensagens a 
     *  serem transmitidas, será indicado o tipo dessa mensagem, se diferente de null.
     */
    public final void proceedWucTimeOut(XMacWucTimeOut TimeOut) {
    	// Debug, escreve entrada no arquivo de depuração somente para o nó com id=1
    	//debug.writeIfNodes(debug.strXLayer(xState, xRadioState)+
    	//		debug.strWucTimeOut(TimeOut), sender, new Integer[]{1});
    	
    	// Chama a XMacStateMachine para definir qual o próximo estado e a duração (delay)
        if (!xStateMachine.changeStateTimeOut(TimeOut))
        	return;

        /* Se deverá reiniciar o processo de envio de RTS, começa o processo startSendDataProcess
         * e sai da função, pois startSendDataProcess já irá criar um novo evento  WucTimeOut */
        if (xState.isRestartRTS()) {
        	startSendDataProcess();
        	return;
        }
        
        // Envia o tipo de pacote informado pelo getSendingPktType
        sendXMacPacket();
        // Inicia o WUC do novo estado
        createWucTimeOut();
    }
    
    
    /**
     *  Função padrão do Grubix para tratar eventos WakeUpCall
     *
     *   Tipos de WakeUpCall:
     *  --> WucTimeOut = marcação de tempo de SLEEP, de Carrier Sense e espera por ACK ou DATA.
     *  --> CrossLayerEvent = eventos emitidos pela Physical Layer para a MAC, com informações do rádio.
     *  --> WucBackOff = tempo de backoff quando o rádio está ocupado ao enviar uma mensagem.
     */
    /*public final void processWakeUpCall(WakeUpCall wuc) {
    	// Debug, escreve entrada no arquivo de depuração somente para o nó com id=1
    	debug.writeIfNodes(debug.strXLayer(xState, xRadioState)+debug.strWuc(wuc), sender, 
    			new Integer[]{1});

    	
    	// Se o tempo restante da simulação for menor que um ciclo, termina o processo
        if ((limitTimeSteps() - currentTimeSteps()) < (xConf.getStepsCycle())){ 
        	return;
        }
        
    	// Tratamento de evento de fim de marcação de tempo
        if (wuc instanceof XMacWucTimeOut) {
        	proceedWucTimeOut((XMacWucTimeOut)wuc);
        }
        // Tratamento dos eventos emitidos pela PHY.
        if (wuc instanceof CrossLayerEvent) {
            proceedCrossLayerEvent((CrossLayerEvent)wuc);
        }
        // Tratamento do evento de nova tentativa de envio de mensagens, quando o rádio estiver ocupado.
        if (wuc instanceof XMacWucBackOff) {
            // Recupera o tempo da primeira tentativa de envio
            double firstTime = ((XMacWucBackOff)wuc).getStartingTime();
            /* Se o tempo desde a primeira tentativa é maior que um ciclo, o nó não tentará novamente.
             * A aplicação deverá tratar essa situação e talvez reenviar DATA num tempo posterior. */
            /*if ((currentTimeSteps() - firstTime) < xConf.getStepsCycle()) {
            	if (!startSendDataProcess()) {
                    XMacWucBackOff wucRetry = new XMacWucBackOff(sender, xConf.getStepsCS(), firstTime);
                    sendEventSelf(wucRetry);
                }
            } else {
            	debug.printw("AVISO! Tempo de BackOff maior que um ciclo", sender);
            }
        }
    }*/

    /**
     *  Função padrão do Grubix para tratar eventos WakeUpCall
     *   Tipos de WakeUpCall:
     *  --> WucTimeOut = marcação de tempo do estados do nó referente a camada MAC.
     *  --> CrossLayerEvent = eventos emitidos pela Physical Layer para a MAC.
     */
    public final void processWakeUpCall(WakeUpCall wuc) {
    	if (wuc instanceof XMacNormalizeWUC) {
    		xConf.normalizeStepsSleep();
    		xConf.normalizeMaxPreambles(xState.isBackboneNode());
    		xStateMachine.changeStateBootNode();
    		createWucTimeOut();
    		if (xState.isBackboneNode()) {
    			//System.err.println("CS: " + xConf.getStepsCS());
    			//System.err.println("Sleep: " + xConf.getStepsSleep());
    		}
    	}
    	
    	if (wuc instanceof StuckNodeWatchdogWUC) {
    		if (!xRadioState.isRadioOn()) {
    			xRadioState.setRadioState(RadioState.LISTENING);
    		}
    		//System.err.println("PELO MENOS ENTROU AQUI????????????????????????");
    	}
    	
    	/* Não grava mais a saída do log se todos os dados chegaram ao destino, ou os fluxos atingiram 
    	 * o limite de envio de RTS ou o tempo para finalizar a simulação é menor que um ciclo */
    	
    	if ((limitTimeSteps() - currentTimeSteps()) < xConf.getStepsCycle() /*|| statistics.isFinalizeSimulation()*/) {
    		// Imprime se ainda não imprimiu as estatísticas
    		//if (!statistics.printedStatistics) {
    		//	statistics.printStatisticsXMac();
        	//	statistics.printShortStatisticsXMac();
        	//	statistics.printedStatistics = true;
    		//}
//    		if (node.getId().asInt() == 2) {
//    			statistics.printStatistics();
//        		try {
//    				statistics.getTransmissions();
//    			} catch (FileNotFoundException e) {
//    				// TODO Auto-generated catch block
//    				e.printStackTrace();
//    			} catch (UnsupportedEncodingException e) {
//    				// TODO Auto-generated catch block
//    				e.printStackTrace();
//    			}
//    		}
    		return;
    	}
    	
    	// Tratamento de evento de fim de marcação de tempo
        if (wuc instanceof XMacWucTimeOut) {
        	proceedWucTimeOut((XMacWucTimeOut)wuc);
        }
        // Tratamento dos eventos emitidos pela PHY.
        if (wuc instanceof CrossLayerEvent) {
            proceedCrossLayerEvent((CrossLayerEvent)wuc);
        }
    }
	
    
	/**
	* Função padrão do GrubiX para tratar pacotes recebidos da camada superior (LogLink)
	* Após receber um pacote da LogLink, é chamada a função de início de transmissão de pacote.
	*/
	public final void upperSAP(Packet packet) {
		// Debug, escreve entrada no arquivo de depuração
		//debug.write(debug.strXLayer(xState, xRadioState)+debug.strPkt(packet), sender);
		// Se o pacote é para o próprio nó, manda de volta para a camada acima
		if (packet.getReceiver() == id) {
			// Muda a direção do pacote para que seja enviado para cima
			packet.flipDirection();
			sendPacket(packet);
			return;
		}
		
		if (packet.getReceiver() == NodeId.ALLNODES) {
			if (packet.getEnclosedPacket() instanceof ExpandedBackboneRoutingPacket) {
				ExpandedBackboneRoutingPacket brp = (ExpandedBackboneRoutingPacket) packet.getEnclosedPacket();
				if (brp.getType() == SWITCH_SELF_AND_BROADCAST) {
					switchToBackbone();
				}
			} else if (packet.getEnclosedPacket() instanceof BackboneRoutingPacket) {
				BackboneRoutingPacket brp = (BackboneRoutingPacket) packet.getEnclosedPacket();
				if (brp.getType() == SWITCH_SELF_AND_BROADCAST) {
					switchToBackbone();
				}
			}
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
    	//debug.write(debug.strXLayer(xState, xRadioState)+debug.strPkt(packet), sender);
    	
    	
        if (!(packet instanceof XMacPacket)) {
        	debug.printw("AVISO! Mensagem desconhecida: "+packet.getClass().getName(), sender);
            return;
        }
		if (packet.getReceiver() == NodeId.ALLNODES) {
			//System.err.println(packet.getReceiver());
        }
        // Ignorar caso a mensagem foi gerada pelo próprio nó
        if (packet.getSender().getId() == getNode().getId())
        	return; 
        
        if (packet.getReceiver() == this.node.getId()) {
        	if (((XMacPacket) packet).getType() == PacketType.DATA) {
        		SingletonTestResult.getInstance().countHop();
        	}
        }
		//if(pktApp.getDestinationId() == getNode().getId().asInt()){
		//	System.out.println("Chegou \n");
		//}
        // Mudar o estado conforme o tipo de mensagem recebida
        xStateMachine.changeStateReceivedMsg((XMacPacket) packet);
        
    	/* Após receber um pacote vindo da PHY, poderá ser gerado outro pacote a ser enviado. Por 
		exemplo, ao receber um RTS, deverá enviar um CTS. */
        sendXMacPacket();
        // Inicia o WUC
        createWucTimeOut();
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
        xConf.setSignalStrength(link.getTransmissionPower());

        // Para qual nó será enviado a mensagem 
        xState.setReceiverNode(llp.getReceiver());
        
        /* Esquema de contagem cíclica para não criar sequência infinita
         Número de sequência da mensagem a ser enviada, limitado em 2^16 */
        xState.setDataSeqNum((xState.getDataSeqNum()+1) & xConf.getMaxSequence());
        
        /** Cria o pacote de DATA da camada MAC para ser enviado depois dos preâmbulos.
         * setAckRequested() e setRetryCount() estão definidos em createDataPacket() abaixo */
        XMacPacket newDataPacket = createDataPacket(sender, llp, xState.getDataSeqNum(), xConf.isAckEnable());
        
        AppPacket pkt = (AppPacket)newDataPacket.getPacket(LayerType.APPLICATION);
        //statistics.setInitNode(pkt.getSender().getId().asInt());
        if (pkt != null) {
        	statistics.setFinalNode(pkt.getReceiver());
        }
        // Atribui o pacote que será enviado
        xState.setDataPkt(newDataPacket);
        
        // Debug, escreve entrada no arquivo de depuração
        //debug.write(debug.str("Pacote DATA criado")+debug.strXLayer(xState, xRadioState)+
        //		debug.strPkt(newDataPacket), sender);
        
        // Se não iniciou o processo de envio do pacote de dados
        if (!startSendDataProcess()) {
            /* Se o rádio estiver ocupado, ou desligado, tente mais tarde após certo tempo.
            Tempo igual a StepsCS, mas poderia ser outro. */
            XMacWucBackOff wucRetry = new XMacWucBackOff(sender, xConf.getStepsCS(), currentTimeSteps());
            //debug.write(debug.strWucBackOff(wucRetry), sender);
            sendEventSelf(wucRetry);
        }
        
        return true; 
    }

    
    /**
     *  Função para iniciar o processo de envio de um pacote da Xmac.
     *  Inicialmente verifica-se se o rádio pode iniciar uma transmissão, estado OFF ou LISTENING.
     *  Depois devem ser seguidos os passos:
     *  1) Construir uma mensagem RTS (preâmbulo);
     *  2) Colocar o número de sequência da mensagem RTS;
     *  3) Colocar o número do RTS como sendo o máximo previsto para alcançar o destinatário;
     *  4) Iniciar a escuta do canal
     */
    protected boolean startSendDataProcess() {   
    	// Debug, escreve entrada no arquivo de depuração
    	//debug.write(debug.strXLayer(xState, xRadioState)+debug.strPkt(xState.getDataPkt()), sender);
    	
    	
    	
    	// Início do envio da sequência de RTS, portanto não precisa reiniciar a sequência de envio
    	xState.setRestartRTS(false);
    	
    	// Verifica se já fez todas tentativas de envio
        if (xState.getDataPkt().getRetryCount() == 0) {
        	debug.printw("AVISO! Tentativas de envio de DATA esgotado", sender);
        	return false;
        }
        
        // Decrementa a tentativa de envio de DATA
        xState.getDataPkt().decRetryCount();
        
        /* Se rádio não estiver OFF ou LISTENING, é porque está em processo de envio ou recepção e 
    	 portanto não pode enviar outra mensagem */
        if ((xRadioState.getRadioState() != RadioState.OFF) && 
        		(xRadioState.getRadioState() != RadioState.LISTENING)) {
        	debug.write("Rádio ocupado: "+xRadioState.getRadioState(), sender);
            return false;
        }  
        
        // Cria um pacote de preâmbulo (RTS) para ser enviado após CS_START
        XMacPacket newRtsPacket = createAuxPacket(sender, xState.getReceiverNode(), 
        		xConf.getSignalStrength(), PacketType.RTS, xConf.getLengthRTS(), 
        		xConf.getMaxPreambles(), (xState.getReceiverNode() != NodeId.ALLNODES));

        
        // Manter referência para o RTS na xMACstate
        xState.setRtsPkt(newRtsPacket);
        
        //AppPacket pkt = (AppPacket)newRtsPacket.getPacket(LayerType.APPLICATION);
        statistics.setTransmission(sender.getId(),newRtsPacket.getReceiver());   
        //System.out.println("ID DA TRANSMISSAO "+statistics.getIdTransmission());
        // Definir o tipo de pacote que será enviado
        xState.setSendingPktType(PacketType.RTS);
        
        /* Irá mudar para o estado do XMac para CS_START e do rádio para LISTENING.
         * Estado do rádio na PHY será mudado em createtWucTimeOut */
        xStateMachine.changeStateSendingMsg();
        
        // Cria evento para ser executado após duração de CS_START
        createWucTimeOut();
        
        // Início do envio da sequência de RTS, portanto não precisa reiniciar a sequência de envio
    	xState.setRestartRTS(false);
    	
    	// Verificar se há atividade no canal antes de iniciar o envio de Preâmbulos.
    	sendEventDown(new EventCarrierSense(sender));
    	return true;
    }
    
    
    /**
     *  Função para enviar o pacote
     */
    protected void sendXMacPacket() {
  	
    	// Se não possui mensagem para enviar, sai sa função
    	if (xState.getSendingPktType() == null)
    		return;
    	
    	XMacPacket packet = null;
    	
    	switch (xState.getSendingPktType()) {
    		// Quando termina o tempo de espera por um CTS, deve reenviar os preâmbulos
    	case RTS:
    		packet = xState.getRtsPkt();
    		statistics.setRtsTransmission(sender.getId());
    		break;
    		// CTS em resposta a um RTS. Tem que ser criado, depois enviado para a PHY	
        case CTS: 
        	packet = createAuxPacket(sender, xState.getReceiverNode(), 
        			xConf.getSignalStrength(), PacketType.CTS, xConf.getLengthCTS(), 
        			xState.getCtsSeqNum(), false);
        	break;
        	/* Poderá ser do tipo ACK, em resposta a um DATA. ACK tem que ser criado, depois enviado 
        	 para a PHY */
        case ACK: 
        	packet = createAuxPacket(sender, xState.getReceiverNode(), 
        			xConf.getSignalStrength(), PacketType.ACK, xConf.getLengthACK(), 
        			xState.getAckSeqNum(), false);
        	break;
        	/* O pacote DATA atual está guardado em xMacState. 
         	Pode ser DATA a enviar ou DATA recebido. No segundo caso enviar para a LogLink */
        case DATA: 
        	packet = xState.getDataPkt();
        	//System.out.println("RECEIVER "+packet.getReceiver());
        	//System.out.println("DATA PACKET "+statistics.getIdTransmission());
        	statistics.endTransmission(packet);
        	break;
        default:
        	// Debug, escreve entrada no arquivo de depuração
        	debug.printw("AVISO! Mensagem de tipo desconhecido: "+xState.getSendingPktType(), sender);
        	return;
    	}
    	
    	// Se for DATA com direção para camada acima, envia o pacote encapsulado
    	if (xState.getSendingPktType() == PacketType.DATA && packet.getDirection() == Direction.UPWARDS) {
    		
    		sendPacket(packet.getEnclosedPacket());
    	}
    	else
    		sendPacket(packet);	

    	// Debug, escreve entrada no arquivo de depuração
    	debug.printw(debug.str("Enviando "+xState.getSendingPktType())+debug.strPkt(packet), sender);
    	
    	// Após enviar uma mensagem define o tipo de mensagem novamente como null
    	this.xState.setSendingPktType(null);
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
    	//debug.write(debug.strXLayer(xState, xRadioState)+debug.strEventCrossLayer(cle), sender);
    	
    	// Recebe evento da PHY informando se o canal está ocupado
        if (cle instanceof EventCarrierSense) {
        	debug.write(debug.str("Evento: EventCarrierSense"), sender);
        	// Aqui a PHY envia uma resposta à consulta sobre atividade no canal.
        	if (((EventCarrierSense) cle).isChannelBusy()) { 
        		xState.setChannelBusy(true);
        		debug.printw("AVISO! Camada PHY indica canal ocupado", sender);       
        	} else {
        		xState.setChannelBusy(false);
        	}
        }
        
    	// A seguir somente será permitido eventos que carregam pacotes do tipo XMacPacket
        if (!(cle.getPacket() instanceof XMacPacket)) {
        	String className = cle.getPacket() != null ? cle.getPacket().getClass().getName() : "vazio";
        	debug.printw("AVISO! Mensagem desconhecida: "+className, sender);
            return;
        }
               
    	XMacPacket packet = (XMacPacket)cle.getPacket();
    	
        // Recebe evento da PHY quando uma mensagem é completamente enviada.
        if (cle instanceof SendingTerminated) {
        	debug.write(debug.str("Evento: SendingTerminated"), sender);
            xStateMachine.changeStateSentMsg(packet);
            /* Envia o tipo de pacote informado pelo setSendingPktType. No caso de acabar de enviar 
             um ACK, deverá enviar DATA para a LOGLINK */
            sendXMacPacket();
            createWucTimeOut();
        }
        // Recebe evento da PHY quando uma mensagem está chegando
        if (cle instanceof StartOfFrameDelimiter) {
        	debug.write(debug.str("Evento: StartOfFrameDelimiter"), sender);
            if ((packet.getReceiver() == id) || (packet.getReceiver() == NodeId.ALLNODES)) {
            	/* Irá manter o estado atual do XMac, até o final da recepção da mensagem pela PHY */
                xStateMachine.changeStateReceivingMsg(packet);
                createWucTimeOut();
            }
        }
        // Recebe evento da PHY quando ocorre uma colisão na recepção de uma mensagem
        if (cle instanceof ColisionDetectEvent) {
        	debug.write(debug.str("Evento: ColisionDetectEvent"), sender);
            if (packet.getReceiver() == id) {
            	debug.write("Colisão informada pela PHY", sender);
            	xState.setState(XMacStateTypes.SLEEP, 100);
            	xRadioState.setRadioState(RadioState.OFF);
            	createWucTimeOut();
            	//System.err.println("COLISÃO SOS SOS SOS !!!!!!!!!!! ################## <><><><><><><><><><><><><><");
            }
        }
    }
    
    
    /** Função para criar pacotes terminais (RTS, CTS, ACK) */
    XMacPacket createAuxPacket(Address sender, NodeId id, double signalStrength, PacketType type, 
    		int headerLength, int sequence, boolean ACKreq) {
    	XMacPacket newPack = new XMacPacket(sender, id, signalStrength, type);
    	newPack.setAckRequested(ACKreq);
    	newPack.setHeaderLength(headerLength);
    	newPack.setSequenceNumber(sequence);
    	return newPack;
    }
    
    
    /** Função para criar pacotes do tipo DATA */
    XMacPacket createDataPacket(Address sender, LogLinkPacket packet, int sequence, boolean ACKreq) {
        XMacPacket newPacket = new XMacPacket(sender, packet, sequence) ;
        newPacket.setHeaderLength(xConf.getLengthDATA());
        // Se ACK deve ser enviado depois que DATA chegar no receptor
        newPacket.setAckRequested(ACKreq);
        // Número de tentativas para enviar DATA
        newPacket.setRetryCount(xConf.getMaxSendRetries());
    	return newPacket;
    }
    
    
    /** Função para mandar a Physical Layer ligar e desligar o rádio
     *  Ligar = true; Desligar = false. */
    protected void setPhyRadioOperation() {
    	boolean isTurnOn = true;
    	if (xRadioState.getRadioState() == RadioState.OFF) 
    		isTurnOn = false;
        sendEventDown(new EventPhyTurnRadio(this.sender, isTurnOn));
    }
    
    
    /** Função para obter o tempo limite de simulação em steps */
    protected double limitTimeSteps() {
        return Configuration.getInstance().getSimulationTime();
    }
    
    
    /** Função para obter o tempo corrente da simulação em steps */
    protected double currentTimeSteps() {
        return SimulationManager.getInstance().getCurrentTime();
    }
    
    
    /** Apenas para manter compatibilidade com Shox. */
    @Override
    public MACState getState() {
        return this.macState;
    }
    
}


/*
 * ALTERAÇÔES DAS VARIÁVEIS EM RELAÇÃO A VERSÃO ANTERIOR
 * 
 * XMac ------------------------------------
 * numPkSequence >>> XMacState:dataSeqNum (Movido para o XMacState)
 * tempoDeCiclo >>> cycleTime
 * 
 * XMacState -------------------------------
 * sendingMSG >>> getDataPkt() (Removida, agora verifica se é diferente de NULL e direção para baixo)
 * stateNow >>> state
 * seqStateNow >>> stateSeqNum
 * sendingPackType >>> sendingPktType
 * dataPacket >>> dataPkt
 * PREAMBLE >>> rtsPkt
 * earlyACKseq >>> ctsSeqNum
 * ACKseq >>> ackSeqNum
 * sendingTo >>> receiverNode
 * pendingStartNode >>> (removido, não é necessário)
 * reStartPRE >>> restartRTS
 * 
 * XMacPacket ------------------------------
 * preambNumber >>> (Foi retirado, é usado apenas a variável sequenceNumber do XMacPacket)
 * messageSent >>> (Foi retirado, não usado)
 * previousWakeTime >>> (Foi retirado, não usado)
 * destination >>> (Foi retirado, não usado)
 * 
 * PhysicalSimple --------------------------
 * radioEmule >>> phyRadioState (Criado uma classe pra armazenar o estado)
 * lastReceivingEnd >>> lastReceptionEnd
 * sendingDuration >>>  lastEmissionDuration
 * receivingDuration >>> (Removido, é criado e usado na mesma função)
 */


/*
 * ALTERAÇÔES DOS MÉTODOS EM RELAÇÃO A VERSÃO ANTERIOR
 * 
 * XMac ------------------------------------
 * proceedXmacTimerWuc() >>> createtWucTimeOut() (Agora apenas cria WUC's do tipo WucTimeOut usando 
 * 		os valores de XMacState e cria o evento para mudar estado do rádio da camada PHY. A mudança 
 * 		de estado é feita antes de chamar esse método).
 * TurnPhysicalRadio() >>> setPhyRadioOperation() (Agora também verifica qual valor correto para 
 * 		passar para EventPhyTurnRadio().
 * proceed_XMacStateTimeOutEvent() >>> proceedWucTimeOut()
 * enviaPacote() >>> sendXMacPacket()
 * sendPacketDown() >>> sendDataPacketDown() (Agora as funções de setAckRequested() e setRetryCount(),
 * 		estão na função createDataPacket)
 * 
 * XMacState -------------------------------
 * setPendingStartNode() >>> Não é mais necessário, nó inicia em modo SLEEP
 * setSendingPackType() >>> setSendingPktType() (Agora quando não tem pacotes para enviar
 * 		sendingPktType=null e não igual a VOID)
 * sendingMSG() >>> Não é mais necessário, agora é verificado se existe pacote DATA com getDataPkt(), 
 * 		se pacote não é null e é direcionado para baixo, então existe DATA para enviar
 * getStateNumber() >>> getStateSeqNum()
 * getDeltaT() >>> getStateDuration()
 * isReStartPRE() >>> isRestartRTS()
 * thereIsPacketToBeSent() >>> getSendingPktType() (Agora quando não tem pacote é null)
 * getACKseq() >>> getAckSeqNum()
 * getEarlyACKseq() >>> getCtsSeqNum()
 * numPkSequence() >>> getDataSeqNum()
 * getReceiver() >>> getReceiverNode()
 * 
 * XMacStateMachine ------------------------
 * changeStateBootNode() >>> (Faz a primeira mudança de estado do XMac)
 * changeStateTimeOut() >>> (muda o estado do XMac e do radio quando o tempo do Wuc da XMac chega 
 * 		ao fim)
 * changeStateSentMsg() >>> (muda o estado do XMac e do radio quando uma mensagem foi enviada pelo rádio)
 * changeStateReceivedMsg >>> (processa as mensagem recebidas pelo rádio, quando o rádio acaba de receber 
 * 		uma mensagem e a PHY a repassa para a MAC)
 * changeStateSendingMsg() >>> (muda o estado do XMac e do rádio quando possui uma mensagem para enviar)
 * changeStateReceivingMsg() >>> (muda o estado do XMac e do radio quando o rádio está recebendo uma mensagem)
 */ 


