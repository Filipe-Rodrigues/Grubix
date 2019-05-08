package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.BackboneXMac.Stats.Simulation;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/** Classe que define a aplicação */
public class XMacRegularNode extends ApplicationLayer {
	
	/** Posição dos nós que irão inicializar o envio do dado */
	@ShoXParameter(description = " Nós que iniciarão o envio do dado")
	private int simultaneousTx;
	
	/** Espera máxima para começar as atividades dos nós que irão iniciar o envio do dado */
	@ShoXParameter(description = " Espera máxima para iniciar as atividades dos nós de origem")
	private int maxDelayToStart;
	

	
	/** Ferramentas para exibir informações para a depuração do programa */
 	ToolsDebug debug = ToolsDebug.getInstance();
 	
 	/** Ferramentas auxiliares diversas */
 	ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();
	
 	/** Ferramentas para guardar e imprimir informações sobre as estatísticas da simulação */
 	//ToolsStatisticsSimulation statistics = ToolsStatisticsSimulation.getInstance();
 	
 	/** Ferramentas para guardar e imprimir1768 informações sobre as estatísticas da simulação */
 	static Simulation statistics = Simulation.getInstance();

 	private int maxPackets = 0;
 	
 	private int counterSteps = 10000;
 	/** Id do último nó que receberá a mensagem */
	//private int lastNodeId = 2;
	
	@Override
	public int getPacketTypeCount() {
		return 1;
	}

	@Override
	public void processEvent(TrafficGeneration tg) {
	}

	protected void processEvent(Finalize finalize) {
	}
	
	/** Inicia a execução da camada de aplicação (primeira função a ser executada) */
	protected void processEvent(StartSimulation start) {
		// Se o nó possui id=1 destinado será id=2, se id=3 destino será id=4
		/*if (node.getId().asInt() == 1 || node.getId().asInt() == 3) {*/
		
		
		
		if (node.getId().asInt() == 1 /*|| node.getId().asInt() == 3*/) {			
			// Cria um evento wakeUpCall para si mesma para acordar após 1000 steps
			//System.out.println("currentregularnode - node=1");
				PingPongWakeUpCall wakeUpCall = new PingPongWakeUpCall(sender, 2000);
				sendEventSelf(wakeUpCall);
			}	
		
		
		
	}

	/** Trata pacotes vindos da camada de baixo */
	@Override
	public void lowerSAP(Packet packet) {
		// Se o packet é instância de AppPacket
		if (packet instanceof AppPacket){	
			AppPacket pk = (AppPacket)packet;
			
			System.out.println("CABECALHO RECEBIDO "+pk.PacketId);
			//System.out.println("ID APP "+pk.getId());
			// Ferramentas de depuração -------------------------
			misc.vGrubix(node.getId(), "Chegou destino", "BLUE");
			debug.print("$$$[Dado-chegou] Dado recebido pela aplicação!!!.", sender);
			//statistics.printStatistics();
			// --------------------------------------------------
		}		
	}
	
	/** Executa após o tempo definido do wakeUpCall */
	public void processWakeUpCall(WakeUpCall wakeUpCall) {
		// Se é uma instância de PingPongWakeUpCall
		if (wakeUpCall instanceof PingPongWakeUpCall) {		
				//System.out.println("MAX PACKETS "+maxPackets);
				
				if(maxPackets < 4){
				////AppPacket pk = new AppPacket(sender, NodeId.get(node.getId().asInt()+1));
				AppPacket pk = new AppPacket(sender, NodeId.get(2));
				//AppPacket pk = new AppPacket(sender, NodeId.get(1459));
				//AppPacket pk = new AppPacket(sender, NodeId.get(250));
				// O destino será o nó de próximo id
				//pk.setDestinationId(node.getId().asInt()+1);
				//pk.setDestinationId(1459);
				pk.setDestinationId(2);
				System.out.println("CABECALHO ENVIADO "+pk.PacketId);
				//System.out.println("ID "+pk.getId());
				//statistics.setTransmission(sender.getId(),node.getId());
				//System.out.println("PACOTE TESTE "+pk.getEnclosedPacket(LayerType.MAC));
				// Envia o pacote	
				sendPacket(pk);
				maxPackets++;
				
				// Criar filtro para o VisualGrubix
				SimulationManager.logNodeState(node.getId(), "Primeiro envio", "int", String.valueOf(10));
				SimulationManager.logNodeState(NodeId.get(node.getId().asInt()+1), "Destino", "int", String.valueOf(10));
				debug.write(debug.str("Primeiro pacote criado e enviado pela aplicação")+
						debug.strPkt(pk), sender, "sendPacket");
				// Ferramentas de depuração -------------------------
				debug.print("[Dado-criado] Dado criado pela aplicação!", sender);
				PingPongWakeUpCall wakeUpCall2 = null;
				System.out.println("VALOR STEPS "+counterSteps);
				if(maxPackets == 1) {
					 wakeUpCall2 = new PingPongWakeUpCall(sender, 36000);
				}else {
					wakeUpCall2 = new PingPongWakeUpCall(sender, counterSteps);
				}
				counterSteps = counterSteps *2;
				
				//System.out.println(wakeUpCall2.getId());
				sendEventSelf(wakeUpCall2);
				}
				
		}
	}
	
	
	
}
