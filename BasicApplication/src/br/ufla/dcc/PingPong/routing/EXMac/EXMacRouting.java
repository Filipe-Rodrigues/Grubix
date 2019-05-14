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
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class EXMacRouting extends NetworkLayer {

	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();

	@ShoXParameter(description = "", defaultValue = "0.9d")
	private double backboneBoundaryRatio;

	/** Lista de nós vizinhos */
	private List<Node> neighbors = null;
	private List<NodeId> backboneNeighbors = null;

	/** Caso eu seja um nó backbone, este é o meu sucessor na cadeia */
	private Node nextBackboneNode;

	/** Posição foco da heurística de formação dos backbones */
	private Position hypocenter;

	/** Objeto de invocação do gerador de backbones */
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

	private void announceConversion(Position direction) {
		EXMacRoutingControlPacket packet = new EXMacRoutingControlPacket(sender, NodeId.ALLNODES, direction, hypocenter, nextBackboneNode.getId());
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
		generator.startBackbone();
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

		private double maxX = Configuration.getInstance().getXSize();
		private double maxY = Configuration.getInstance().getYSize();
		
		@Override
		public void startBackbone() {
			Node nextNeighbor = null;
			if (node.getId().asInt() == 10) {
				hypocenter = node.getPosition();
				nextNeighbor = selectNextNeighbor(RIGHT);
			} else if (node.getId().asInt() == 11) {
				hypocenter = node.getPosition();
				nextNeighbor = selectNextNeighbor(DOWN);
			} else if (node.getId().asInt() == 12) {
				hypocenter = node.getPosition();
				nextNeighbor = selectNextNeighbor(LEFT);
			} else if (node.getId().asInt() == 13) {
				hypocenter = node.getPosition();
				nextNeighbor = selectNextNeighbor(UP);
			}
			if (nextNeighbor != null) {
				nextBackboneNode = nextNeighbor;
				announceConversion();
			}
		}

		@Override
		public void includeBackboneNeighbor(NodeId newBackboneNeighbor) {
			backboneNeighbors.add(newBackboneNeighbor);
		}

		@Override
		public void convertToBackbone() {
			
		}

		private Node selectNextNeighbor(Position direction) {
			Node selectedNode = null;
			for (Node neighbor : neighbors) {
				if (!backboneNeighbors.contains(neighbor.getId()) && isEligible(neighbor.getPosition(), direction)) {
					if (selectedNode == null 
							|| isMoreAlignedThan(neighbor.getPosition(), selectedNode.getPosition(), direction)) {
						selectedNode = neighbor;
					}
				}
			}
			if (selectedNode != null) {
				return selectedNode;
			}

			return null;
		}
		
		private boolean isEligible(Position pos, Position direction) {
			double posX = pos.getXCoord();
			double posY = pos.getYCoord();
			double lowerXBound = Configuration.getInstance().getXSize() * (1 - backboneBoundaryRatio);
			double upperXBound = Configuration.getInstance().getXSize() * backboneBoundaryRatio;
			double lowerYBound = Configuration.getInstance().getYSize() * (1 - backboneBoundaryRatio);
			double upperYBound = Configuration.getInstance().getYSize() * backboneBoundaryRatio;
			boolean isFartherFromHypo = node.getPosition().getDistance(hypocenter) < pos.getDistance(hypocenter);
			boolean isGoingAtCorrectDirection = true;
			if (direction == LEFT) {
				isGoingAtCorrectDirection = pos.getXCoord() < hypocenter.getXCoord();
			} else if (direction == RIGHT) {
				isGoingAtCorrectDirection = pos.getXCoord() > hypocenter.getXCoord();
			} else if (direction == UP) {
				isGoingAtCorrectDirection = pos.getYCoord() < hypocenter.getYCoord();
			} else if (direction == DOWN) {
				isGoingAtCorrectDirection = pos.getYCoord() > hypocenter.getYCoord();
			}
			return (posX > lowerXBound && posX < upperXBound && posY > lowerYBound && posY < upperYBound)
					 && isFartherFromHypo && isGoingAtCorrectDirection;
		}
		
		private boolean isMoreAlignedThan(Position test, Position reference, Position axis) {
			Position testAux;
			Position refAux;
			Position hypAux;
			if (axis.getXCoord() != 0) {
				testAux = new Position(0, test.getYCoord());
				refAux = new Position(0, reference.getYCoord());
				hypAux = new Position(0, hypocenter.getYCoord());
			} else {
				testAux = new Position(test.getXCoord(), 0);
				refAux = new Position(reference.getXCoord(), 0);
				hypAux = new Position(hypocenter.getXCoord(), 0);
			}
			return testAux.getDistance(hypAux) < refAux.getDistance(hypAux);
		}

	}

}
