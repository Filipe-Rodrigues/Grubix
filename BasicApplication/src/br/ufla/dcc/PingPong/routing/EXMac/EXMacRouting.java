package br.ufla.dcc.PingPong.routing.EXMac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

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
import static br.ufla.dcc.PingPong.routing.EXMac.AuxiliarConstants.*;

public class EXMacRouting extends NetworkLayer {

	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();

	@ShoXParameter(description = "Porção máxima do campo em que os backbones poderão se propagar. 1 = 100% do campo.", defaultValue = "0.95d")
	private double backboneBoundaryRatio;

	/** Lista de nós vizinhos */
	private List<Node> neighbors = null;
	private List<NodeId> backboneNeighbors = null;

	/** Objeto de invocação do gerador de backbones */
	private EXMacBackboneGenerator generator;

	public EXMacRouting() {
		backboneNeighbors = new ArrayList<>();
		generator = null;
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

	private void routePacketDirectly(Packet packet) {
		ensureNeighborhoodInitialization();

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
		EXMacRoutingControlPacket packet = new EXMacRoutingControlPacket(sender, NodeId.ALLNODES, 
				direction, generator.hypocenter, (generator.nextBackboneNode == node) 
				? (null) 
				: (generator.nextBackboneNode.getId()));
		BackboneConfigurationManager.getInstance().setNextBackboneNode(node.getId(), 
				generator.nextBackboneNode.getId(), direction);
		sendPacket(packet);
	}
	
	private void ensureNeighborhoodInitialization() {
		if (this.neighbors == null) {
			this.neighbors = node.getNeighbors();
		}
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
				routePacketDirectly(enclosed);
			}
		} else if (packet instanceof EXMacRoutingControlPacket) {
			EXMacRoutingControlPacket controlPacket = (EXMacRoutingControlPacket) packet;
			NodeId senderID = controlPacket.getSender().getId();
			if (!backboneNeighbors.contains(senderID)) {
				backboneNeighbors.add(senderID);
				BackboneConfigurationManager.getInstance().addBackboneNeighbor(node.getId(), senderID);
			}
			if (generator.nextBackboneNode == null) {
				NodeId next = controlPacket.getNextSelectedBackbone();
				if (next != null && next.equals(node.getId())) {
					generator.hypocenter = controlPacket.getBackboneLineRoot();
					generator = new HeuristicA(controlPacket.getGrowthDirection());
					generator.convertToBackbone();
				}
			}
		}
	}

	@Override
	public void upperSAP(Packet packet) throws LayerException {
		debug.write(debug.strPkt(packet), sender);
		routePacketDirectly(packet);
	}

	@Override
	protected void processEvent(StartSimulation start) {
		super.processEvent(start);
		if (node.getId().asInt() == 10) {
			generator = new HeuristicA(RIGHT);
			generator.startBackbone();
		} else if (node.getId().asInt() == 11) {
			generator = new HeuristicA(DOWN);
			generator.startBackbone();
		} else if (node.getId().asInt() == 12) {
			generator = new HeuristicA(LEFT);
			generator.startBackbone();
		} else if (node.getId().asInt() == 13) {
			generator = new HeuristicA(UP);
			generator.startBackbone();
		} else {
			generator = new HeuristicA();
		}
	}

	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		debug.write(sender);
	}

	private abstract class EXMacBackboneGenerator {

		public static final Position ORIGIN = new Position(0, 0);
		
		/** Caso eu seja um nó backbone, este é o meu sucessor na cadeia */
		protected Node nextBackboneNode;
		
		/** Posição foco da heurística de formação dos backbones */
		protected Position hypocenter;
		
		public double computeVectorAngles(Position v1, Position v2) {
			return ORIGIN.computeAngle(v1, v2);
		}
		
		abstract void startBackbone();

		abstract void includeBackboneNeighbor(NodeId newBackboneNeighbor);

		abstract void convertToBackbone();
		
		abstract void routePacketUsingBackbone(Packet packet, Queue<Byte> intermediatePaths);
	}

	private class HeuristicA extends EXMacBackboneGenerator {

		/** Acesso rápido às coordenadas máximas do campo */
		private final double MAX_X = Configuration.getInstance().getXSize();
		private final double MAX_Y = Configuration.getInstance().getYSize();

		/** Caso eu seja um nó backbone, esta é a direção da minha viagem */
		private Position travelDirection;
		
		/** Caso eu seja um nó Backbone, este é meu identificador de segmento */
		private byte label;

		private HeuristicA() {
		}
		
		private HeuristicA(Position travelDirection) {
			this.travelDirection = travelDirection;
		}
		
		@Override
		public void startBackbone() {
			hypocenter = node.getPosition();
			convertToBackbone();
		}

		@Override
		public void includeBackboneNeighbor(NodeId newBackboneNeighbor) {
			backboneNeighbors.add(newBackboneNeighbor);
		}

		@Override
		public void convertToBackbone() {
			nextBackboneNode = (canIGrowMore()) ? (selectNextNeighbor()) : (node);
			//System.err.println("CHOOSEN ID for #" + node.getId() + ": " + nextBackboneNode.getId());
			label = getCorrespondingLabel(node.getPosition(), travelDirection);
			announceConversion(travelDirection);
		}

		@Override
		public void routePacketUsingBackbone(Packet packet, Queue<Byte> intermediatePaths) {
			if (amIBackbone()) {
				
			} else {
				
			}
		}
		
		private boolean amIBackbone() {
			return nextBackboneNode != null;
		}
		
		private Node selectNextNeighbor() {
			Node selectedNode = null;
			ensureNeighborhoodInitialization();
			for (Node neighbor : neighbors) {
				if (!backboneNeighbors.contains(neighbor.getId()) && isEligible(neighbor.getPosition())) {
					if (selectedNode == null 
							|| isMoreAlignedThan(neighbor.getPosition(), selectedNode.getPosition())) {
						selectedNode = neighbor;
					}
				}
			}
			if (selectedNode != null) {
				return selectedNode;
			}

			return node;
		}
		
		private boolean canIGrowMore() {
			double nodeX = node.getPosition().getXCoord();
			double nodeY = node.getPosition().getYCoord();
			double lowerXBound = MAX_X * (1 - backboneBoundaryRatio);
			double upperXBound = MAX_X * backboneBoundaryRatio;
			double lowerYBound = MAX_Y * (1 - backboneBoundaryRatio);
			double upperYBound = MAX_Y * backboneBoundaryRatio;
			return nodeX > lowerXBound && nodeX < upperXBound && nodeY > lowerYBound && nodeY < upperYBound;
		}
		
		private boolean isEligible(Position pos) {
			double posX = pos.getXCoord();
			double posY = pos.getYCoord();

			boolean isFartherFromHypo = node.getPosition().getDistance(hypocenter) < pos.getDistance(hypocenter);
			boolean isGoingAtCorrectDirection = true;
			if (travelDirection.equals(LEFT)) {
				isGoingAtCorrectDirection = posX < hypocenter.getXCoord();
			} else if (travelDirection.equals(RIGHT)) {
				isGoingAtCorrectDirection = posX > hypocenter.getXCoord();
			} else if (travelDirection.equals(UP)) {
				isGoingAtCorrectDirection = posY < hypocenter.getYCoord();
			} else if (travelDirection.equals(DOWN)) {
				isGoingAtCorrectDirection = posY > hypocenter.getYCoord();
			}
			return isFartherFromHypo && isGoingAtCorrectDirection;
		}
		
		private boolean isMoreAlignedThan(Position test, Position reference) {
			Position testAux;
			Position refAux;
			Position hypAux;
			if (travelDirection.getXCoord() != 0) {
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
		
		private byte getCorrespondingLabel(Position nodePosition, Position travelDirection) {
			double nodeX = nodePosition.getXCoord();
			double nodeY = nodePosition.getYCoord();
			if (travelDirection.equals(RIGHT)) {
				if (nodeX < (MAX_X / 3d)) {
					return 5;
				} else if (nodeX < 2 * (MAX_X / 3d)) {
					return 1;
				} else {
					return 6;
				}
			} else if (travelDirection.equals(DOWN)) {
				if (nodeY < (MAX_Y / 3d)) {
					return 7;
				} else if (nodeY < 2 * (MAX_Y / 3d)) {
					return 1;
				} else {
					return 8;
				}
			} else if (travelDirection.equals(LEFT)) {
				if (nodeX < (MAX_X / 3d)) {
					return 10;
				} else if (nodeX < 2 * (MAX_X / 3d)) {
					return 3;
				} else {
					return 9;
				}
			} else if (travelDirection.equals(UP)) {
				if (nodeY < (MAX_Y / 3d)) {
					return 12;
				} else if (nodeY < 2 * (MAX_Y / 3d)) {
					return 4;
				} else {
					return 11;
				}
			}
			return -1;
		}
		
		private Position getDirectionOfBackboneFromMe(byte backboneRegionLabel) {
			if (backboneRegionLabel == 5 || backboneRegionLabel == 1 || backboneRegionLabel == 6) {
				if (node.getPosition().getYCoord() < (MAX_Y / 3d)) {
					return DOWN;
				}
				return UP;
			} else if (backboneRegionLabel == 7 || backboneRegionLabel == 2 || backboneRegionLabel == 8) {
				if (node.getPosition().getXCoord() < 2 * (MAX_X / 3d)) {
					return RIGHT;
				}
				return LEFT;
			} else if (backboneRegionLabel == 9 || backboneRegionLabel == 3 || backboneRegionLabel == 10) {
				if (node.getPosition().getYCoord() < 2 * (MAX_Y / 3d)) {
					return DOWN;
				}
				return UP;
			} else if (backboneRegionLabel == 11 || backboneRegionLabel == 4 || backboneRegionLabel == 12) {
				if (node.getPosition().getXCoord() < (MAX_X / 3d)) {
					return RIGHT;
				}
				return LEFT;
			}
			
			return null;
		}
		
		private boolean isDeadEndSource(byte label) {
			return (label == 6 || label == 8 || label == 10 
					|| label == 12 || label < 1 || label > 12);
		}
		
		private boolean isDeadEndTarget(byte label) {
			return (label == 5 || label == 7 || label == 9 
					|| label == 11 || label < 1 || label > 12);
		}
		
		private double getHopDistanceFromBackbone(Position growthDirection) {
			return 0;
		}
		
		private List<Pair<Double, Position>> getOrderedBackboneList() {
			List<Pair<Double, Position>> backbones = new ArrayList<Pair<Double,Position>>();
			
			Collections.sort(backbones);
			return backbones;
		}
		
	}

}
