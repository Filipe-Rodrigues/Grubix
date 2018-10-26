package br.ufla.dcc.PingPong.routing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ShoXParameter;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import static br.ufla.dcc.PingPong.routing.BackboneRoutingPacketType.*;

public class ExpandedBackboneRouting extends NetworkLayer {

	@ShoXParameter(description = "Coordenada máxima para o eixo x dos nós")
	private double xAxisLimit;

	@ShoXParameter(description = "Coordenada máxima para o eixo y dos nós")
	private double yAxisLimit;

	private Position backboneRootPosition;

	private boolean iAmBackbone = false;

	private int convertedNeighbors = 0;
	private boolean canPropagate = false;
	
	private NodeId backboneWhomConvertedMe;

	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();

	/** Lista de nós vizinhos */
	private List<Node> neighbors = null;
	private List<NodeId> backboneNeighbors = new ArrayList<>();

	private void testBackbones() {
		if (!backboneNeighbors.isEmpty()) {
			for (NodeId id : backboneNeighbors) {
				System.err.print(id.asInt() + " << ");
			}
			System.err.println();
		} else {
			System.err
					.println("DEU RUIM PRA CARALHO AUAHAUHAUHAUAHUAHAUHAUAHUAHAUHAUAH");
		}
	}

	/** Função para obter o nó vizinho mais próximo do destino */
	private NodeId getMinDistanceNodeId(Position destinationPosition) {
		NodeId minDistanceNodeId = null;
		double minDistance = node.getPosition()
				.getDistance(destinationPosition);
		Position myPosition = node.getPosition();
		NodeId selectedBackboneNode = node.getId();
		if (!backboneNeighbors.isEmpty()) {

			double maxAngleAperture = Math.PI / 2
					* (1 - (1 / (minDistance * 0.8)));
			for (NodeId neighbor : backboneNeighbors) {
				Position currentBackboneNeighborPosition = SimulationManager
						.getInstance().queryNodeById(neighbor).getPosition();
				double neighDistanceFromDestination = currentBackboneNeighborPosition
						.getDistance(destinationPosition);
				if (neighDistanceFromDestination < minDistance
						&& (myPosition.computeAngle(destinationPosition,
								currentBackboneNeighborPosition) < maxAngleAperture || 2
								* Math.PI
								- myPosition.computeAngle(destinationPosition,
										currentBackboneNeighborPosition) < maxAngleAperture)) {
					selectedBackboneNode = neighbor;
					minDistance = neighDistanceFromDestination;
				}
			}
		}

		if (selectedBackboneNode == node.getId()) {
			for (Node neighbor : neighbors) {
				Position position = neighbor.getPosition();
				neighbor.setPosition(position);
				double distance = position.getDistance(destinationPosition);

				if (distance < minDistance) {
					minDistance = distance;
					minDistanceNodeId = neighbor.getId();
				}
			}
		} else {
			// System.err.println("SELECIONOU O BACKBONE!!!!!!!!!!!!!!!!!!!!!!!!!");
			return selectedBackboneNode;
		}

		return minDistanceNodeId;
	}

	private void routePacket(Packet packet) {
		startupNeighborhood();

		Position destinationPosition = SimulationManager.getInstance()
				.queryNodeById(packet.getReceiver()).getPosition();

		NodeId closestId = getMinDistanceNodeId(destinationPosition);
		if (closestId == null) {
			System.err.println(this.id
					+ " >>>> BURACO ENCONTRADO, SAINDO DA APLICAÇÃO!!!");
			System.exit(1);
		}
		GeoRoutingPacket newPacket = new GeoRoutingPacket(sender, closestId,
				packet);

		sendPacket(newPacket);
	}

	private void startupNeighborhood() {
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
			 * Se o destino da mensagem é o nó, então manda para a camada de
			 * aplicação, senão continua o roteamento (envia para o nó vizinho
			 * mais próximo do destino)
			 */
			if (enclosed.getReceiver().equals(getId())) {
				sendPacket(enclosed);
			} else {
				// Ferramentas de depuração -------------------------
				debug.print(
						"[Roteamento] Mensagem não é para mim, será enviada para camada abaixo (LLC)",
						sender);
				// --------------------------------------------------
				routePacket(enclosed);
			}
		} else if (packet instanceof ExpandedBackboneRoutingPacket) {
			ExpandedBackboneRoutingPacket backbonePacket = (ExpandedBackboneRoutingPacket) packet;
			if (backbonePacket.getType() == SWITCH_SELF_AND_BROADCAST) {
				NodeId senderId = backbonePacket.getSender().getId();
				if (!backboneNeighbors.contains(senderId)) {
					backboneNeighbors.add(senderId);
				}
			} else if (backbonePacket.getType() == CHOOSE_TO_SWITCH) {
				if (!iAmBackbone) {
					
					int jump = backbonePacket.getJumpCounter();
					//System.out.println("JUMP NOW: " + jump);
					if (jump < 1) {
						jump = 3;
						canPropagate = true;
					}
					jump--;
					
					//System.out.println("JUMP THEN: " + jump);
					//System.out.println("CAN PROPAGATE: " + canPropagate);
					
//					BackboneStartAdviseWUC wuc = new BackboneStartAdviseWUC(
//							backbonePacket.getSender(), backbonePacket.getRootPosition(), backbonePacket.getJumpCounter() - 1, 10);
//					sendEventSelf(wuc);
//					Position rootPosition = bsawuc.getcentralBackboneNode();
//					NodeId parent = bsawuc.getSender().getId();
					switchToBackbone(false, backbonePacket.getSender().getId(), backbonePacket.getRootPosition(), jump);
				}
			} else if (backbonePacket.getType() == FINISHED_BRANCH) {
				if (canPropagate) {
					if (backboneWhomConvertedMe != node.getId()) {
						if (convertedNeighbors < 2) {
							selectNextBackboneNode(2);
							convertedNeighbors++;
						} else {
							notifyAncestor();
						}
					} else {
						if (convertedNeighbors < 4) {
							createBackboneBranches(2);
							convertedNeighbors++;
						}
					}
				} else {
					notifyAncestor();
				}
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
		backboneWhomConvertedMe = node.getId();
		if (node.getId().asInt() == 10) {
			canPropagate = true;
			switchToBackbone(true, node.getId(), node.getPosition(), 2);
		}
	}

	private void switchToBackbone(boolean isTheFirst, NodeId parentBackbone, Position rootPosition, int jumpCount) {
		iAmBackbone = true;
		backboneWhomConvertedMe = parentBackbone;
		backboneRootPosition = rootPosition;
//		ExpandedBackboneRoutingPacket packet = new ExpandedBackboneRoutingPacket(sender,
//				NodeId.ALLNODES, SWITCH_SELF_AND_BROADCAST);
//		sendPacket(packet);
		sendNetworkMessage(SWITCH_SELF_AND_BROADCAST, NodeId.ALLNODES, -1);
		startupNeighborhood();
		if (isTheFirst) {
			createBackboneBranches(jumpCount);
		} else {
			selectNextBackboneNode(jumpCount);
		}
	}

	private void createBackboneBranches(int jumpCount) {
		int delayCount = 1;
		do {
			Position myXaxis = new Position(xAxisLimit, node.getPosition()
					.getYCoord());
			//double rad45 = Math.PI / 4d;
			double rad45 = 0d;
			double rad90 = Math.PI / 2d;
			Position myPosition = node.getPosition();
			double nearestPerfectAngleRatio = 2 * Math.PI;
			Node nextBackboneNode = node;
			//System.err.println("Angle: " + (convertedNeighbors * rad90 + rad45));
			for (Node neighbor : neighbors) {
				Position currentNeighborPosition = neighbor.getPosition();
				if (Math.abs(myPosition.computeAngle(myXaxis,
						currentNeighborPosition)
						- (convertedNeighbors * rad90 + rad45)) < nearestPerfectAngleRatio) {
					nearestPerfectAngleRatio = Math.abs(myPosition.computeAngle(
							myXaxis, currentNeighborPosition)
							- (convertedNeighbors * rad90 + rad45));
					nextBackboneNode = neighbor;
				}
			}
			if (nextBackboneNode != node) {
				propagateBackboneSignal(nextBackboneNode, jumpCount, 1100 * delayCount);
				//System.err.println("Node: " + nextBackboneNode.getId());
				delayCount += 10;
			}
			convertedNeighbors++;
		} while (convertedNeighbors < 4);
	}

	private void selectNextBackboneNode(int jumpCount) {
		Node nextBackboneNode = getNodeOppositeTo(backboneRootPosition);
		if (nextBackboneNode != node && !backboneNeighbors.contains(nextBackboneNode.getId())) {
			propagateBackboneSignal(nextBackboneNode, jumpCount, 1100);
		} else {
			notifyAncestor();
		}
	}
	
	private Node getFarthestNonBackboneNodeFrom(Position position) {
		Node selectedNode = node;
		Position selectedNodePosition = node.getPosition();
		
		for(Node neighbor : neighbors) {
			Position neighborPosition = neighbor.getPosition();
			//System.err.println("NEIGH: " + neighbor.getId() + ":");
			//testBackbones();
			if (neighborPosition.getDistance(position) > selectedNodePosition.getDistance(position) &&
					!backboneNeighbors.contains(neighbor.getId())) {
				selectedNode = neighbor;
				selectedNodePosition = neighborPosition;
			}
		}
		
		return selectedNode;
	}
	
	private Node getNodeOppositeTo(Position position) {
		double rad123 = 11d*Math.PI/16d;
		double rad12 = Math.PI/16d;
		Position myPosition = node.getPosition();
		Node nextBackboneNode = node;
		for (Node neighbor : neighbors) {
			Position currentNeighborPosition = neighbor.getPosition();
			Position nextBackbonePosition = nextBackboneNode.getPosition();
			if (!backboneNeighbors.contains(neighbor.getId()) && 
					myPosition.getDistance(currentNeighborPosition) > myPosition
					.getDistance(nextBackbonePosition)
					&& myPosition.computeAngle(backboneRootPosition,
							currentNeighborPosition) >= (rad123 + 6*rad12*convertedNeighbors)
					&& myPosition.computeAngle(backboneRootPosition,
							currentNeighborPosition) < (rad123 +  6*rad12*convertedNeighbors + 4*rad12)) {
				
				//System.err.println(myPosition.computeAngle(backboneRootPosition, currentNeighborPosition));
				
				nextBackboneNode = neighbor;
			}
		}
		if (nextBackboneNode == node && convertedNeighbors < 2 &&
				myPosition.getXCoord() > 60 && myPosition.getXCoord() < xAxisLimit - 60 &&
				myPosition.getYCoord() > 60 && myPosition.getYCoord() < yAxisLimit - 60) {
			return getFarthestNonBackboneNodeFrom(position);
		}
		return nextBackboneNode;
	}
	
	private void propagateBackboneSignal(Node nextBackboneNode, int jumpCount, double delay) {
		//System.err.println(delay);
		BackbonePropagationWUC wuc = new BackbonePropagationWUC(sender,
				nextBackboneNode.getId(), CHOOSE_TO_SWITCH, jumpCount, delay);
		sendEventSelf(wuc);
		
		//sendNetworkMessage(CHOOSE_TO_SWITCH, nextBackboneNode.getId(), jumpCount);
		
	}
	
	private void notifyAncestor() {
		BackbonePropagationWUC bpwuc = new BackbonePropagationWUC(sender, backboneWhomConvertedMe, FINISHED_BRANCH, -1, 1100);
		sendEventSelf(bpwuc);
		
		//sendNetworkMessage(FINISHED_BRANCH, backboneWhomConvertedMe, -1);
	}
	
	private void sendNetworkMessage(BackboneRoutingPacketType messageType, NodeId target, int jumpCount) {
		ExpandedBackboneRoutingPacket packet = new ExpandedBackboneRoutingPacket(sender,
				target, messageType, backboneRootPosition, jumpCount);
		sendPacket(packet);
	}

	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		debug.write(sender);
		if (wuc instanceof BackbonePropagationWUC) {
			BackbonePropagationWUC bpwuc = (BackbonePropagationWUC) wuc;
			NodeId nextBackboneNode = bpwuc.getReceiverId();
			ExpandedBackboneRoutingPacket packet = new ExpandedBackboneRoutingPacket(sender,
					nextBackboneNode,
					bpwuc.getMode(),
					backboneRootPosition, bpwuc.getJumpCounter());
			sendPacket(packet);
			convertedNeighbors++;
		} 
//		else if (wuc instanceof BackboneStartAdviseWUC) {
//			BackboneStartAdviseWUC bsawuc = (BackboneStartAdviseWUC) wuc;
//			Position rootPosition = bsawuc.getcentralBackboneNode();
//			NodeId parent = bsawuc.getSender().getId();
//			switchToBackbone(false, parent, rootPosition, bsawuc.getJumpCounter());
//		}
	}

}
