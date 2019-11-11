package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.ToolsStatisticsSimulation;
import br.ufla.dcc.PingPong.testing.SingletonTestResult;
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
public class EXMacRegularNodeList extends ApplicationLayer {

	private static final boolean LIST_MODE = false;
	
	private static int TARGET_COUNTER_ID = 2;
	
	private static int SOURCE_ID = 1;
	
	private static int TARGET_ID = 2;
	
	private static final int PING_PONG_COUNTER = 10;
	
	/** Posição dos nós que irão inicializar o envio do dado */
	@ShoXParameter(description = " Nós que iniciarão o envio do dado")
	private int simultaneousTx;

	/**
	 * Espera máxima para começar as atividades dos nós que irão iniciar o envio do
	 * dado
	 */
	@ShoXParameter(description = " Espera máxima para iniciar as atividades dos nós de origem")
	private int maxDelayToStart;

	@ShoXParameter(description = "Habilita ou desabilita a produção de mensagens para o teste", defaultValue = "true")
	private boolean testingMode;

	
	/** Ferramentas para exibir informações para a depuração do programa */
	ToolsDebug debug = ToolsDebug.getInstance();

	/** Ferramentas auxiliares diversas */
	ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();

	/**
	 * Ferramentas para guardar e imprimir informações sobre as estatísticas da
	 * simulação
	 */
	ToolsStatisticsSimulation statistics = ToolsStatisticsSimulation.getInstance();

	/** Id do último nó que receberá a mensagem */
	// private int lastNodeId = 2;

	private void sendMessage() {
		int id = TARGET_ID;
		if (LIST_MODE) {
			id = TARGET_COUNTER_ID++;
		}
		AppPacket pk = new AppPacket(sender, NodeId.get(id));
		pk.setDestinationId(id);
		sendPacket(pk);
		SimulationManager.logNodeState(node.getId(), "Primeiro envio", "int", String.valueOf(10));
		SimulationManager.logNodeState(NodeId.get(id), "Destino", "int", String.valueOf(10));
	}
	
	@Override
	public int getPacketTypeCount() {
		return 1;
	}

	@Override
	public void processEvent(TrafficGeneration tg) {
	}

	protected void processEvent(Finalize finalize) {
	}

	/**
	 * Inicia a execução da camada de aplicação (primeira função a ser executada)
	 */
	protected void processEvent(StartSimulation start) {

		if (testingMode && node.getId().asInt() == SOURCE_ID) {
			SingletonTestResult.getInstance().setStartingTime(SimulationManager.getInstance().getCurrentTime());
			sendMessage();
		}

	}

	/** Trata pacotes vindos da camada de baixo */
	@SuppressWarnings("unused")
	@Override
	public void lowerSAP(Packet packet) {
		// Se o packet é instância de AppPacket
		if (packet instanceof AppPacket) {
			if (LIST_MODE && TARGET_COUNTER_ID <= PING_PONG_COUNTER) {
				System.err.println("PING PONG");
				sendMessage();
			} else {
				SingletonTestResult.getInstance().setEndingTime(SimulationManager.getInstance().getCurrentTime());
				System.out.println(SingletonTestResult.getInstance().getTime());
			}
		}
	}

	/** Executa após o tempo definido do wakeUpCall */
	public void processWakeUpCall(WakeUpCall wakeUpCall) {
		// Se é uma instância de PingPongWakeUpCall
		if (wakeUpCall instanceof PingPongWakeUpCall) {
			int id = TARGET_COUNTER_ID++;
			AppPacket pk = new AppPacket(sender, NodeId.get(id));
			// O destino será o nó de próximo id
			pk.setDestinationId(id);
			System.out.println("CABECALHO ENVIADO " + pk.PacketId);
			// Envia o pacote
			sendPacket(pk);
			// Criar filtro para o VisualGrubix
			SimulationManager.logNodeState(node.getId(), "Primeiro envio", "int", String.valueOf(10));
			SimulationManager.logNodeState(NodeId.get(id), "Destino", "int", String.valueOf(10));
			debug.write(debug.str("Primeiro pacote criado e enviado pela aplicação") + debug.strPkt(pk), sender,
					"sendPacket");
			// Ferramentas de depuração -------------------------
			debug.print("[Dado-criado] Dado criado pela aplicação!", sender);
			// PingPongWakeUpCall wakeUpCall2 = new PingPongWakeUpCall(sender, 6000);
			// System.out.println(wakeUpCall2.getId());
			// sendEventSelf(wakeUpCall2);

		}
	}

}
