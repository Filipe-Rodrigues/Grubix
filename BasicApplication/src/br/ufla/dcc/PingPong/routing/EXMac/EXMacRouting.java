package br.ufla.dcc.PingPong.routing.EXMac;

import java.util.ArrayList;
import java.util.List;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.routing.GeoRoutingPacket;
import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.Node;

public class EXMacRouting extends NetworkLayer {

	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();

	/** Lista de nós vizinhos */
	private List<Node> neighbors = null;
	private List<NodeId> backboneNeighbors = null;
	
	/** Controla se sou ou não um nó interno ao backbone */
	private boolean amIBackbone;
	
	/** Caso eu seja um nó backbone, este é o meu sucessor na cadeia*/
	private Node nextBackboneNode;
	
	/** Posição foco da heurística de formação dos backbones*/
	private Position hypocenter;
	
	/** Objeto de invocação do gerador de backbones*/
	private EXMacBackboneGenerator generator;

	public EXMacRouting() {
		backboneNeighbors = new ArrayList<>();
		generator = new HeuristicA();
		
	}
	
	/** Função para obter o nó vizinho mais próximo do destino */
	private NodeId getMinDistanceNodeId(Position destinationPosition) {
		NodeId minDistanceNodeId = null;
		double minDistance = node.getPosition().getDistance(destinationPosition);

		for (Node neighbor : neighbors) {
			Position position = neighbor.getPosition();
			neighbor.setPosition(position);
			double distance = position.getDistance(destinationPosition);

			if (distance < minDistance) {
				minDistance = distance;
				minDistanceNodeId = neighbor.getId();
			}
		}
		return minDistanceNodeId;
	}

	private void routePacket(Packet packet) {
		if (this.neighbors == null) {
			this.neighbors = node.getNeighbors();
		}

		Position destinationPosition = SimulationManager.getInstance().queryNodeById(packet.getReceiver())
				.getPosition();

		NodeId closestId = getMinDistanceNodeId(destinationPosition);
		if (closestId == null) {
			System.err.println(this.id + " >>>> BURACO ENCONTRADO, SAINDO DA APLICAÇÃO!!!");
			SingletonTestResult.getInstance().setEndingTime(-1);
			SingletonTestResult.getInstance().printAllStats();
			System.exit(1);
		}
		GeoRoutingPacket newPacket = new GeoRoutingPacket(sender, closestId, packet);

		sendPacket(newPacket);
	}

	@Override
	public void lowerSAP(Packet packet) throws LayerException {
		if (packet instanceof GeoRoutingPacket) {
			GeoRoutingPacket geoRoutingPacket = (GeoRoutingPacket) packet;
			Packet enclosed = geoRoutingPacket.getEnclosedPacket();

			/*
			 * Se o destino da mensagem é o nó, então manda para a camada de aplicação,
			 * senão continua o roteamento (envia para o nó vizinho mais próximo do destino)
			 */
			if (enclosed.getReceiver().equals(getId())) {
				sendPacket(enclosed);
			} else {
				// Ferramentas de depuração -------------------------
				debug.print("[Roteamento] Mensagem não é para mim, será enviada para camada abaixo (LLC)", sender);
				// --------------------------------------------------
				routePacket(enclosed);
			}
		}
	}

	@Override
	public void upperSAP(Packet packet) throws LayerException {
		debug.write(debug.strPkt(packet), sender);
		routePacket(packet);
	}

	@Override
	protected void processEvent(StartSimulation start) {
		super.processEvent(start);
		if (node.getId().asInt() == 10) {
			initializeBackbonePaths();
		}
	}

	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		debug.write(sender);
	}
	
	private interface EXMacBackboneGenerator {
		public static final Position LEFT = new Position(-1, 0);
		public static final Position RIGHT = new Position(1, 0);
		public static final Position UP = new Position(-1, 0);
		public static final Position DOWN = new Position(1, 0);
		
		void startBackbone();
		void includeBackboneNeighbor(NodeId newBackboneNeighbor);
		void convertToBackbone();
	}
	
	private class HeuristicA implements EXMacBackboneGenerator {

		@Override
		public void startBackbone() {
			if (node.getId().asInt() == 10) {
				NodeId nextNeighbor = selectNextNeighbor(RIGHT);
				if (nextNeighbor != null) {
					
				}
			}
		}

		@Override
		public void includeBackboneNeighbor(NodeId newBackboneNeighbor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void convertToBackbone() {
			// TODO Auto-generated method stub
			
		}
		
		private NodeId selectNextNeighbor(Position direction) {
			
		}
		
	}

}
