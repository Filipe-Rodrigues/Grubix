package br.ufla.dcc.PingPong.routing.USAMac;

import static br.ufla.dcc.PingPong.routing.USAMac.AuxiliarConstants.DOWN;
import static br.ufla.dcc.PingPong.routing.USAMac.AuxiliarConstants.LEFT;
import static br.ufla.dcc.PingPong.routing.USAMac.AuxiliarConstants.RIGHT;
import static br.ufla.dcc.PingPong.routing.USAMac.AuxiliarConstants.UP;
import static br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager.USAMAC_CONFIG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.ToolsMiscellaneous;
import br.ufla.dcc.PingPong.BackboneXMac.Stats.Simulation;
import br.ufla.dcc.PingPong.USAMac.EventFinishedCSEnd;
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

public class USAMacRouting extends NetworkLayer {

	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();

	@ShoXParameter(description = "Porção máxima do campo em que os backbones poderão se propagar. 1 = 100% do campo.", defaultValue = "0.96d")
	private double backboneBoundaryRatio;

	/** Lista de nós vizinhos */
	private List<Node> neighbors = null;
	private List<NodeId> backboneNeighbors = null;
	
	/** Fila de pacotes a serem roteados */
	private Queue<Pair<Queue<Byte>, Pair<Packet, Integer>>> packetQueue = null;

	/** Objeto de invocação do gerador de backbones */
	private EXMacBackboneGenerator generator;

	public USAMacRouting() {
		backboneNeighbors = new ArrayList<>();
		packetQueue = new LinkedList<Pair<Queue<Byte>, Pair<Packet, Integer>>>();
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

	private void routePacketDirectly(Packet packet, int hopCount) {
		ensureNeighborhoodInitialization();

		Position destinationPosition = SimulationManager.getInstance().queryNodeById(packet.getReceiver())
				.getPosition();

		NodeId closestId = getMinDistanceNodeId(destinationPosition);
		if (closestId == null) {
			System.err.println(this.id + " >>>> BURACO ENCONTRADO, SAINDO DA APLICAÇÃO!!!");
			System.exit(1);
		}
		GeoRoutingPacket newPacket = new GeoRoutingPacket(sender, closestId, packet);
		
		newPacket.setHopCount(hopCount + 1);

		sendPacket(newPacket);
	}

	private void announceConversion(Position direction) {
		
		USAMacRoutingControlPacket packet = new USAMacRoutingControlPacket(sender, NodeId.ALLNODES, 
				direction, generator.hypocenter, (generator.nextBackboneNode == node) 
				? (null) 
				: (generator.nextBackboneNode.getId()));
		BackboneConfigurationManager.getInstance(USAMAC_CONFIG).setNextBackboneNode(node.getId(), 
				generator.nextBackboneNode.getId(), direction);
		sendPacket(packet);
	}
	
	private void ensureNeighborhoodInitialization() {
		if (this.neighbors == null) {
			this.neighbors = node.getNeighbors();
		}
	}
	
	private void ensureNextBackboneNodeRegistration() {
		NodeId nextBBNode = BackboneConfigurationManager.getInstance(USAMAC_CONFIG).getNextBackboneNode(node.getId());
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
				System.out.print(geoRoutingPacket.getHopCounter() + "\t");
				sendPacket(enclosed);
			} else {
				// Ferramentas de depuração -------------------------
				debug.print("[Roteamento] Mensagem não é para mim, será enviada para camada abaixo (LLC)", sender);
				// --------------------------------------------------
				Pair<Packet, Integer> packPair = new Pair<Packet, Integer>(enclosed, geoRoutingPacket.getHopCounter());
				packetQueue.add(new Pair<Queue<Byte>, Pair<Packet, Integer>>(null, packPair));
			}
		} else if (packet instanceof USAMacRoutingControlPacket) {
			USAMacRoutingControlPacket controlPacket = (USAMacRoutingControlPacket) packet;
			NodeId senderID = controlPacket.getSender().getId();
			if (!backboneNeighbors.contains(senderID)) {
				backboneNeighbors.add(senderID);
				BackboneConfigurationManager.getInstance(USAMAC_CONFIG).addBackboneNeighbor(node.getId(), senderID);
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
		} else if (packet instanceof USAMacRoutingPacket) {
			USAMacRoutingPacket usamacPacket = (USAMacRoutingPacket) packet;
			Packet enclosed = usamacPacket.getEnclosedPacket();
			if (enclosed.getReceiver().equals(getId())) {
				System.out.print(usamacPacket.getHopCount() + "\t");
				sendPacket(enclosed);
			} else {
				Pair<Packet, Integer> packPair = new Pair<Packet, Integer>(enclosed, usamacPacket.getHopCount());
				packetQueue.add(new Pair<Queue<Byte>, Pair<Packet, Integer>>(usamacPacket.getBackboneSegmentPath(), packPair));
			}
		}
	}

	@Override
	public void upperSAP(Packet packet) throws LayerException {
		debug.write(debug.strPkt(packet), sender);
		Position myPos = node.getPosition();
		Position destPos = SimulationManager.getInstance().queryNodeById(packet.getReceiver()).getPosition();
		System.out.print(myPos.getDistance(destPos) + "\t");
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
				BackboneStartupWakeUpCall bswuc = new BackboneStartupWakeUpCall(sender, DOWN, 40450);
				sendEventSelf(bswuc);
			} else if (node.getId().asInt() == 12) {
				generator = new HeuristicA(LEFT);
				generator.startBackbone();
			} else if (node.getId().asInt() == 13) {
				BackboneStartupWakeUpCall bswuc = new BackboneStartupWakeUpCall(sender, UP, 40450);
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
					Pair<Queue<Byte>, Pair<Packet, Integer>> nextPacket = packetQueue.poll();
					if (nextPacket.getLeft() == null) {
						routePacketDirectly(nextPacket.getRight().getLeft(), nextPacket.getRight().getRight());
					} else {
						generator.routePacketUsingBackbone(nextPacket.getRight().getLeft(), nextPacket.getLeft(), nextPacket.getRight().getRight());
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
		
		abstract void routePacketUsingBackbone(Packet packet, Queue<Byte> intermediatePaths, int hopCount);
	}

	private class HeuristicA extends EXMacBackboneGenerator {

		/** Acesso rápido às coordenadas máximas do campo */
		private final double MAX_X = Configuration.getInstance().getXSize();
		private final double MAX_Y = Configuration.getInstance().getYSize();
		private final double MEAN_HOP_DISTANCE = 35.64d;
		private final double MEAN_PREAMBLE_COUNT_NORMAL = 24;
		private final double MEAN_PREAMBLE_COUNT_BACKBONE = 5;
		

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
				backboneNeighbors = BackboneConfigurationManager.getInstance(USAMAC_CONFIG).loadBackboneNeighborsUSAMac(myId);
				this.travelDirection = BackboneConfigurationManager.getInstance(USAMAC_CONFIG).getBackboneDirection(myId);
				label = getCorrespondingLabel(node.getPosition(), this.travelDirection);
				//label = getCorrespondingLabel(node.getPosition(), travelDirection);
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
			
			Position enter = getIntersectionWithBackbone(source, getDirectionFromLabel(sourceBBSegment));
			Position exit = getIntersectionWithBackbone(target, getDirectionFromLabel(targetBBSegment));
			Entry<Double, Queue<Byte>> shortestPath = BackboneRouteGraph.getInstance("heuristicA.cfg")
					.getshortestPath(sourceBBSegment, targetBBSegment, enter, exit);
			//System.err.println(sourceBBSegment + " ==> " + targetBBSegment);
			double backbonedDist = estimatePreambleCountToBackbone(source, backboneDirectionSource)
					+ estimatePreambleCountToBackbone(target, backboneDirectionTarget)
					+ (MEAN_PREAMBLE_COUNT_BACKBONE * shortestPath.getLeft() / MEAN_HOP_DISTANCE);
			double directDist = estimatePreambleCountDirect(node.getPosition(), target);
			//System.err.print("BB DIST: " + backbonedDist + " ///// ");
			//System.err.println("DIR DIST: " + directDist);
			if (directDist <= backbonedDist) {
				routePacketDirectly(packet, -1);
				//System.err.println("Using DIRECT........");
			} else {
				routePacketUsingBackbone(packet, shortestPath.getRight(), -1);
				//System.err.println("Using BACKBONE........");
			}
		}

		@Override
		public void routePacketUsingBackbone(Packet packet, Queue<Byte> intermediatePaths, int hopCount) {
			ensureNextBackboneNodeRegistration();
			ensureNeighborhoodInitialization();
			if (!intermediatePaths.isEmpty()) {
				byte backboneSegment = intermediatePaths.peek();
				boolean sentMessage = false;
				if (amIBackbone()) {
					NodeId nextBBNodeId = nextBackboneNode.getId();
					Position nextBBNodePos = SimulationManager.getInstance().queryNodeById(nextBBNodeId).getPosition();
					Position nextBBNodeDir = BackboneConfigurationManager.getInstance(USAMAC_CONFIG).getBackboneDirection(nextBBNodeId);
					byte neighLabel = getCorrespondingLabel(nextBBNodePos, nextBBNodeDir);
					if (checkLabelClassCompatibility(neighLabel, getDirectionFromLabel(backboneSegment))) {
						intermediatePaths.poll();
						sendEXMacRoutingPacket(packet, nextBBNodeId, intermediatePaths, hopCount);
						sentMessage = true;
					}
				}
				if (!sentMessage && !backboneNeighbors.isEmpty()) {
					for (NodeId neighbor : backboneNeighbors) {
						Position neighPos = SimulationManager.getInstance().queryNodeById(neighbor).getPosition();
						Position neighDir = BackboneConfigurationManager.getInstance(USAMAC_CONFIG).getBackboneDirection(neighbor);
						byte neighLabel = getCorrespondingLabel(neighPos, neighDir);
						if (checkLabelClassCompatibility(neighLabel, getDirectionFromLabel(backboneSegment))) {
							//System.err.println("FOUND BACKBONE BRANCH: ");
							intermediatePaths.poll();
							sendEXMacRoutingPacket(packet, neighbor, intermediatePaths, hopCount);
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
							sendEXMacRoutingPacket(packet, closestNeighbor, intermediatePaths, hopCount);
						} else {
							System.err.println(id + " >>>> BURACO ENCONTRADO, SAINDO DA APLICAÇÃO!!!");
							System.exit(1);
						}
					} else {
						NodeId nextBBNode = nextBackboneNode.getId();
						if (nextBBNode.equals(node.getId())) {
							System.err.println("MESSAGE LEAKAGE, SOLVE THIS PROBLEM!!!!");
						} else {
							sendEXMacRoutingPacket(packet, nextBBNode, intermediatePaths, hopCount);
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
						routePacketDirectly(packet, hopCount);
					} else {
						NodeId nextBBNode = nextBackboneNode.getId();
						sendEXMacRoutingPacket(packet, nextBBNode, intermediatePaths, hopCount);
					}
				} else {
					routePacketDirectly(packet, hopCount);
				}
			}
		}
		
		private void sendEXMacRoutingPacket(Packet packet, NodeId destination, Queue<Byte> intermediatePath, int hopCount) {
			USAMacRoutingPacket exmacPacket = new USAMacRoutingPacket(sender, destination, packet, intermediatePath);
			exmacPacket.setHopCount(hopCount + 1);
			sendPacket(exmacPacket);
		}
		
		private boolean amIBackbone() {
			ensureNextBackboneNodeRegistration();
			return nextBackboneNode != null;
		}
		
//		private Node selectNextNeighbor() {
//			Node selectedNode = null;
//			ensureNeighborhoodInitialization();
//			for (Node neighbor : neighbors) {
//				if (!backboneNeighbors.contains(neighbor.getId()) && isEligible(neighbor.getPosition())) {
//					if (selectedNode == null 
//							|| isMoreAlignedThan(neighbor.getPosition(), selectedNode.getPosition())) {
//						selectedNode = neighbor;
//					}
//				}
//			}
//			if (selectedNode != null) {
//				return selectedNode;
//			}
//
//			return node;
//		}
		
		private Node selectNextNeighbor() {
			Node selectedNode = node;
			ensureNeighborhoodInitialization();
			for (Node neighbor : neighbors) {
				if (!backboneNeighbors.contains(neighbor.getId()) && isEligible(neighbor.getPosition())) {
					if (selectedNode == node 
							|| isMoreAlignedThan(neighbor.getPosition(), selectedNode.getPosition())) {
						selectedNode = neighbor;
					}
				}
			}
			return selectedNode;
		}
		
		private double getDistanceFromTarget(Position neighbor) {
			Position hypoTarget = getHypocenterTarget();
			return neighbor.getDistance(hypoTarget);
		}
		
		private Position getHypocenterTarget() {
			Position hypoTarget;
			if (travelDirection.getXCoord() != 0) {
				if (travelDirection.getXCoord() > 0) {
					hypoTarget = new Position(MAX_X, hypocenter.getYCoord());
				} else {
					hypoTarget = new Position(0, hypocenter.getYCoord());
				}
			} else {
				if (travelDirection.getYCoord() > 0) {
					hypoTarget = new Position(hypocenter.getXCoord(), MAX_Y);
				} else {
					hypoTarget = new Position(hypocenter.getXCoord(), 0);
				}
			}
			return hypoTarget;
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
			return getDistanceFromTarget(test) < getDistanceFromTarget(reference);
		}
		
		private boolean checkLabelClassCompatibility(byte label, Position travelDirection) {
			if (travelDirection.equals(RIGHT)) {
				return (label == 5 || label == 1 || label == 6);
			} else if (travelDirection.equals(DOWN)) {
				return (label == 7 || label == 2 || label == 8);
			} else if (travelDirection.equals(LEFT)) {
				return (label == 9 || label == 3 || label == 10);
			} else if (travelDirection.equals(UP)) {
				return (label == 11 || label == 4 || label == 12);
			}
			return false;
		}
		
		private byte getCorrespondingLabel(Position nodePosition, Position travelDirection) {
			if (travelDirection == null) return -1;
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
		
		private double estimatePreambleCountDirect(Position source, Position target) {
			return MEAN_PREAMBLE_COUNT_NORMAL * source.getDistance(target) / MEAN_HOP_DISTANCE;
		}
		
		private double estimatePreambleCountToBackbone(Position test, Position travelDirection) {
			double distance;
			if (travelDirection.equals(RIGHT)) {
				distance = Math.abs(test.getYCoord() - (MAX_Y / 3d));
			} else if (travelDirection.equals(DOWN)) {
				distance = Math.abs(test.getXCoord() - 2 * (MAX_X / 3d));
			} else if (travelDirection.equals(LEFT)) {
				distance = Math.abs(test.getYCoord() - 2 * (MAX_Y / 3d));
			} else {
				distance = Math.abs(test.getXCoord() - (MAX_X / 3d));
			}
			return MEAN_PREAMBLE_COUNT_NORMAL * distance / MEAN_HOP_DISTANCE;
		}
		
		private Position getIntersectionWithBackbone(Position test, Position travelDirection) {
			if (travelDirection.equals(RIGHT)) {
				return new Position(test.getXCoord(), (MAX_Y / 3d));
			} else if (travelDirection.equals(DOWN)) {
				return new Position(2 * (MAX_X / 3d), test.getYCoord());
			} else if (travelDirection.equals(LEFT)) {
				return new Position(test.getXCoord(), 2 * (MAX_Y / 3d));
			} else {
				return new Position((MAX_X / 3d), test.getYCoord());
			}
		}
		
		private List<Entry<Double, Position>> getOrderedBackboneList(Position test) {
			List<Entry<Double, Position>> backbones = new ArrayList<Entry<Double,Position>>();
			backbones.add(new Entry<Double, Position>(
					estimatePreambleCountToBackbone(test, RIGHT), RIGHT));
			backbones.add(new Entry<Double, Position>(
					estimatePreambleCountToBackbone(test, DOWN), DOWN));
			backbones.add(new Entry<Double, Position>(
					estimatePreambleCountToBackbone(test, LEFT), LEFT));
			backbones.add(new Entry<Double, Position>(
					estimatePreambleCountToBackbone(test, UP), UP));
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
