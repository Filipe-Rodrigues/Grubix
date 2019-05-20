package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.ToolsStatisticsSimulation;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/** Classe que define a aplicação */
public class EXMacRegularNode extends ApplicationLayer {
	
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
 	ToolsStatisticsSimulation statistics = ToolsStatisticsSimulation.getInstance();
		
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
		
		
		
		if (node.getId().asInt() == -1) {			
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
			// Ferramentas de depuração -------------------------
			misc.vGrubix(node.getId(), "Chegou destino", "BLUE");
			debug.print("$$$[Dado-chegou] Dado recebido pela aplicação!!!.", sender);
			// --------------------------------------------------
		}		
	}
	
	/** Executa após o tempo definido do wakeUpCall */
	public void processWakeUpCall(WakeUpCall wakeUpCall) {
		// Se é uma instância de PingPongWakeUpCall
		if (wakeUpCall instanceof PingPongWakeUpCall) {			
				
				AppPacket pk = new AppPacket(sender, NodeId.get(99));
				// O destino será o nó de próximo id
				pk.setDestinationId(99);
				System.out.println("CABECALHO ENVIADO "+pk.PacketId);
				// Envia o pacote	
				sendPacket(pk);
				// Criar filtro para o VisualGrubix
				SimulationManager.logNodeState(node.getId(), "Primeiro envio", "int", String.valueOf(10));
				SimulationManager.logNodeState(NodeId.get(99), "Destino", "int", String.valueOf(10));
				debug.write(debug.str("Primeiro pacote criado e enviado pela aplicação")+
						debug.strPkt(pk), sender, "sendPacket");
				// Ferramentas de depuração -------------------------
				debug.print("[Dado-criado] Dado criado pela aplicação!", sender);
				PingPongWakeUpCall wakeUpCall2 = new PingPongWakeUpCall(sender, 6000);
				//System.out.println(wakeUpCall2.getId());
				sendEventSelf(wakeUpCall2);
				
		}
	}
	
	
	
}
