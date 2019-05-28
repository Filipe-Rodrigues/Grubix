package br.ufla.dcc.PingPong.routing.EXMac;

import static br.ufla.dcc.PingPong.routing.EXMac.AuxiliarConstants.DOWN;
import static br.ufla.dcc.PingPong.routing.EXMac.AuxiliarConstants.LEFT;
import static br.ufla.dcc.PingPong.routing.EXMac.AuxiliarConstants.RIGHT;
import static br.ufla.dcc.PingPong.routing.EXMac.AuxiliarConstants.UP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.EXMac.EventFinishedCSEnd;
import br.ufla.dcc.PingPong.movement.FromConfigStartPositions;
import br.ufla.dcc.PingPong.routing.GeoRoutingPacket;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.movement.FromFileStartPositions;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class EXMacRouting extends NetworkLayer {

	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();

	@ShoXParameter(description = "Porção máxima do campo em que os backbones poderão se propagar. 1 = 100% do campo.", defaultValue = "0.96d")
	private double backboneBoundaryRatio;

	/** Lista de nós vizinhos */
	private List<Node> neighbors = null;
	private List<NodeId> backboneNeighbors = null;
	
	/** Fila de pacotes a serem roteados */
	private Queue<Pair<Queue<Byte>, Packet>> packetQueue = null;

	/** Objeto de invocação do gerador de backbones */
	private EXMacBackboneGenerator generator;

	public EXMacRouting() {
		backboneNeighbors = new ArrayList<>();
		packetQueue = new LinkedList<Pair<Queue<Byte>, Packet>>();
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
	
	private void ensureNextBackboneNodeRegistration() {
		NodeId nextBBNode = BackboneConfigurationManager.getInstance().getNextBackboneNode(node.getId());
		if (nextBBNode != null) {
			generator.nextBackboneNode = SimulationManager.getInstance().queryNodeById(nextBBNode);
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
				packetQueue.add(new Pair<Queue<Byte>, Packet>(null, enclosed));
			}
		} else if (packet instanceof EXMacRoutingControlPacket) {
			EXMacRoutingControlPacket controlPacket = (EXMacRoutingControlPacket) packet;
			NodeId senderID = controlPacket.getSender().getId();
			if (!backboneNeighbors.contains(senderID)) {
				backboneNeighbors.add(senderID);
				BackboneConfigurationManager.getInstance().addBackboneNeighbor(node.getId(), senderID);
			}
			ensureNextBackboneNodeRegistration();
			if (generator.nextBackboneNode == null) {
				NodeId next = controlPacket.getNextSelectedBackbone();
				if (next != null && next.equals(node.getId())) {
					generator = new HeuristicA(controlPacket.getGrowthDirection());
					generator.hypocenter = controlPacket.getBackboneLineRoot();
					generator.convertToBackbone();
				}
			}
		} else if (packet instanceof EXMacRoutingPacket) {
			EXMacRoutingPacket exmacPacket = (EXMacRoutingPacket) packet;
			Packet enclosed = exmacPacket.getEnclosedPacket();
			if (enclosed.getReceiver().equals(getId())) {
				sendPacket(enclosed);
			} else {
				packetQueue.add(new Pair<Queue<Byte>, Packet>(exmacPacket.getBackboneSegmentPath(), enclosed));
			}
		}
	}

	@Override
	public void upperSAP(Packet packet) throws LayerException {
		debug.write(debug.strPkt(packet), sender);
		generator.chooseRoutingMode(packet);
	}

	@Override
	protected void processEvent(StartSimulation start) {
		super.processEvent(start);
		if (Configuration.getInstance().getPositionGenerator() instanceof FromConfigStartPositions) {
			generator = new HeuristicA(null);
		} else {
			if (node.getId().asInt() == 10) {
				generator = new HeuristicA(RIGHT);
				generator.startBackbone();
			} else if (node.getId().asInt() == 11) {
				BackboneStartupWakeUpCall bswuc = new BackboneStartupWakeUpCall(sender, DOWN, 40000);
				sendEventSelf(bswuc);
			} else if (node.getId().asInt() == 12) {
				generator = new HeuristicA(LEFT);
				generator.startBackbone();
			} else if (node.getId().asInt() == 13) {
				BackboneStartupWakeUpCall bswuc = new BackboneStartupWakeUpCall(sender, UP, 40000);
				sendEventSelf(bswuc);
			} else {
				generator = new HeuristicA();
			}
		}
	}

	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		debug.write(sender);
		if (wuc instanceof CrossLayerEvent) {
			if (wuc instanceof EventFinishedCSEnd) {
				if (!packetQueue.isEmpty()) {
					Pair<Queue<Byte>, Packet> nextPacket = packetQueue.poll();
					if (nextPacket.getLeft() == null) {
						routePacketDirectly(nextPacket.getRight());
					} else {
						generator.routePacketUsingBackbone(nextPacket.getRight(), nextPacket.getLeft());
					}
				}
			}
		} else if (wuc instanceof BackboneStartupWakeUpCall) {
			BackboneStartupWakeUpCall bswuc = (BackboneStartupWakeUpCall) wuc;
			generator = new HeuristicA(bswuc.getGrowthDirection());
			generator.startBackbone();
		}
	}

	private abstract class EXMacBackboneGenerator {

		public final Position ORIGIN = new Position(0, 0);
		
		/** Caso eu seja um nó backbone, este é o meu sucessor na cadeia */
		protected Node nextBackboneNode;
		
		/** Posição foco da heurística de formação dos backbones */
		protected Position hypocenter;
		
		public double computeVectorAngles(Position v1, Position v2) {
			return ORIGIN.computeAngle(v1, v2);
		}
		
		public Position getVectorFromPositions(Position a, Position b) {
			return new Position(b.getXCoord() - a.getXCoord(), b.getYCoord() - a.getYCoord());
		}
		
		public Double getScalarProjection(Position v1, Position v2) {
			return (v1.getXCoord() * v2.getXCoord() + v1.getYCoord() * v2.getYCoord())
					/ Math.sqrt(Math.pow(v2.getXCoord(), 2) + Math.pow(v2.getYCoord(), 2));
		}
		
		abstract void startBackbone();

		abstract void includeBackboneNeighbor(NodeId newBackboneNeighbor);

		abstract void convertToBackbone();
		
		abstract void chooseRoutingMode(Packet packet);
		
		abstract void routePacketUsingBackbone(Packet packet, Queue<Byte> intermediatePaths);
	}

	private class HeuristicA extends EXMacBackboneGenerator {

		/** Acesso rápido às coordenadas máximas do campo */
		private final double MAX_X = Configuration.getInstance().getXSize();
		private final double MAX_Y = Configuration.getInstance().getYSize();
		private final double MEAN_HOP_DISTANCE = 34.64d;

		/** Caso eu seja um nó backbone, esta é a direção da minha viagem */
		private Position travelDirection;
		
		/** Caso eu seja um nó Backbone, este é meu identificador de segmento */
		private byte label;

		private HeuristicA() {
		}
		
		private HeuristicA(Position travelDirection) {
			if (travelDirection != null) {
				this.travelDirection = travelDirection;
			} else {
				NodeId myId = node.getId();
				label = BackboneConfigurationManager.getInstance().getBackboneNodeLabel(myId);
				backboneNeighbors = BackboneConfigurationManager.getInstance().loadBackboneNeighbors(myId);
				this.travelDirection = BackboneConfigurationManager.getInstance().getBackboneDirection(myId);
			}
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
			BackboneConfigurationManager.getInstance().setBackboneNodeLabel(node.getId(), label);
			announceConversion(travelDirection);
		}
		
		@Override
		public void chooseRoutingMode(Packet packet) {
			Position source = node.getPosition();
			Position target = SimulationManager.getInstance().queryNodeById(packet.getReceiver())
					.getPosition();
			List<Entry<Double, Position>> backboneChains = getOrderedBackboneList(source);
			Position stDirection = getVectorFromPositions(source, target);
			Position backboneDirectionSource;
			byte sourceBBSegment;
			int count = 0;
			do {
				backboneDirectionSource = backboneChains.get(count++).getRight();
				sourceBBSegment = getCorrespondingLabel(source, backboneDirectionSource);
			} while (!isDirectionViable(stDirection, backboneDirectionSource, sourceBBSegment));
			
			backboneChains = getOrderedBackboneList(target);
			Position backboneDirectionTarget;
			byte targetBBSegment;
			count = 0;
			do {
				backboneDirectionTarget = backboneChains.get(count++).getRight();
				targetBBSegment = getCorrespondingLabel(target, backboneDirectionTarget);
			} while (isDeadEndTarget(targetBBSegment));
			
			Entry<Integer, Queue<Byte>> shortestPath = BackboneRouteGraph.getInstance("heuristicA.cfg")
					.getshortestPath(sourceBBSegment, targetBBSegment);
			//System.err.println(sourceBBSegment + " ==> " + targetBBSegment);
			double backbonedDist = getHopDistanceFromBackbone(source, backboneDirectionSource)
					+ getHopDistanceFromBackbone(target, backboneDirectionTarget)
					+ shortestPath.getLeft();
			double directDist = getDirectHopDistance(node.getPosition(), target);
			//System.err.println("BB DIST: " + backbonedDist);
			//System.err.println("DIR DIST: " + directDist);
			if (directDist <= backbonedDist) {
				routePacketDirectly(packet);
			} else {
				routePacketUsingBackbone(packet, shortestPath.getRight());
			}
		}

		@Override
		public void routePacketUsingBackbone(Packet packet, Queue<Byte> intermediatePaths) {
			ensureNextBackboneNodeRegistration();
			ensureNeighborhoodInitialization();
			if (!intermediatePaths.isEmpty()) {
				byte backboneSegment = intermediatePaths.peek();
				boolean sentMessage = false;
				if (amIBackbone()) {
					NodeId nextBBNode = nextBackboneNode.getId();
					byte neighLabel = BackboneConfigurationManager.getInstance().getBackboneNodeLabel(nextBBNode);
					if (neighLabel == backboneSegment) {
						intermediatePaths.poll();
						sendEXMacRoutingPacket(packet, nextBBNode, intermediatePaths);
						sentMessage = true;
					}
				}
				if (!sentMessage && !backboneNeighbors.isEmpty()) {
					for (NodeId neighbor : backboneNeighbors) {
						byte neighLabel = BackboneConfigurationManager.getInstance().getBackboneNodeLabel(neighbor);
						if (neighLabel == backboneSegment) {
							//System.err.println("FOUND BACKBONE BRANCH: ");
							intermediatePaths.poll();
							sendEXMacRoutingPacket(packet, neighbor, intermediatePaths);
							sentMessage = true;
							break;
						}
					}
				}
				if (!sentMessage) {
					if (!amIBackbone()) {
						Position source = node.getPosition();
						Position direction = getDirectionOfBackboneFromReference(source, backboneSegment);
						NodeId closestNeighbor = null;
						double maxDistance = 0;
						for (Node neighbor : neighbors) {
							Position neighPos = neighbor.getPosition();
							double scalarProj = getScalarProjection(getVectorFromPositions(source, neighPos), direction);
							if (scalarProj > maxDistance) {
								maxDistance = scalarProj;
								closestNeighbor = neighbor.getId();
							}
						}
						if (closestNeighbor != null) {
							sendEXMacRoutingPacket(packet, closestNeighbor, intermediatePaths);
						} else {
							System.err.println(id + " >>>> BURACO ENCONTRADO, SAINDO DA APLICAÇÃO!!!");
							System.exit(1);
						}
					} else {
						NodeId nextBBNode = nextBackboneNode.getId();
						if (nextBBNode.equals(node.getId())) {
							System.err.println("MESSAGE LEAKAGE, SOLVE THIS PROBLEM!!!!");
						} else {
							sendEXMacRoutingPacket(packet, nextBBNode, intermediatePaths);
						}
					}
				}
			} else {
				if (amIBackbone()) {
					Position source = node.getPosition();
					Position nextBBPosition = nextBackboneNode.getPosition();
					Position destination = SimulationManager.getInstance().queryNodeById(packet.getReceiver())
							.getPosition();
					if (source.getDistance(destination) <= nextBBPosition.getDistance(destination)) {
						routePacketDirectly(packet);
					} else {
						NodeId nextBBNode = nextBackboneNode.getId();
						sendEXMacRoutingPacket(packet, nextBBNode, intermediatePaths);
					}
				} else {
					routePacketDirectly(packet);
				}
			}
		}
		
		private void sendEXMacRoutingPacket(Packet packet, NodeId destination, Queue<Byte> intermediatePath) {
			EXMacRoutingPacket exmacPacket = new EXMacRoutingPacket(sender, destination, packet, intermediatePath);
			sendPacket(exmacPacket);
		}
		
		private boolean amIBackbone() {
			ensureNextBackboneNodeRegistration();
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
					return 2;
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
		
		private Position getDirectionFromLabel(byte label) {
			if (label == 5 || label == 1 || label == 6) {
				return RIGHT;
			} else if (label == 7 || label == 2 || label == 8) {
				return DOWN;
			} else if (label == 9 || label == 3 || label == 10) {
				return LEFT;
			} else {
				return UP;
			}
		}
		
		private Position getDirectionOfBackboneFromReference(Position test, byte backboneRegionLabel) {
			if (backboneRegionLabel == 5 || backboneRegionLabel == 1 || backboneRegionLabel == 6) {
				if (test.getYCoord() < (MAX_Y / 3d)) {
					return DOWN;
				}
				return UP;
			} else if (backboneRegionLabel == 7 || backboneRegionLabel == 2 || backboneRegionLabel == 8) {
				if (test.getXCoord() < 2 * (MAX_X / 3d)) {
					return RIGHT;
				}
				return LEFT;
			} else if (backboneRegionLabel == 9 || backboneRegionLabel == 3 || backboneRegionLabel == 10) {
				if (test.getYCoord() < 2 * (MAX_Y / 3d)) {
					return DOWN;
				}
				return UP;
			} else if (backboneRegionLabel == 11 || backboneRegionLabel == 4 || backboneRegionLabel == 12) {
				if (test.getXCoord() < (MAX_X / 3d)) {
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
		
		private double getHopDistanceFromBackbone(Position test, Position travelDirection) {
			double distance;
			if (travelDirection.equals(RIGHT)) {
				distance = Math.abs(test.getYCoord() - (MAX_Y / 3d)) / MEAN_HOP_DISTANCE;
			} else if (travelDirection.equals(DOWN)) {
				distance = Math.abs(test.getXCoord() - 2 * (MAX_X / 3d)) / MEAN_HOP_DISTANCE;
			} else if (travelDirection.equals(LEFT)) {
				distance = Math.abs(test.getYCoord() - 2 * (MAX_Y / 3d)) / MEAN_HOP_DISTANCE;
			} else {
				distance = Math.abs(test.getXCoord() - (MAX_X / 3d)) / MEAN_HOP_DISTANCE;
			}
			return distance + 2;
		}
		
		private double getDirectHopDistance(Position source, Position target) {
			return source.getDistance(target) / MEAN_HOP_DISTANCE;
		}
		
		private List<Entry<Double, Position>> getOrderedBackboneList(Position test) {
			List<Entry<Double, Position>> backbones = new ArrayList<Entry<Double,Position>>();
			backbones.add(new Entry<Double, Position>(
					getHopDistanceFromBackbone(test, RIGHT), RIGHT));
			backbones.add(new Entry<Double, Position>(
					getHopDistanceFromBackbone(test, DOWN), DOWN));
			backbones.add(new Entry<Double, Position>(
					getHopDistanceFromBackbone(test, LEFT), LEFT));
			backbones.add(new Entry<Double, Position>(
					getHopDistanceFromBackbone(test, UP), UP));
			Collections.sort(backbones);
			return backbones;
		}
		
		private boolean isDirectionViable(Position vectorST, Position travelDir, byte label) {
			if (!isDeadEndSource(label)) {
				return true;
			}
			double angle = computeVectorAngles(vectorST, travelDir);
			return (angle < Math.PI / 2d || angle > 3d * Math.PI / 2d);
		}
		
	}

}
