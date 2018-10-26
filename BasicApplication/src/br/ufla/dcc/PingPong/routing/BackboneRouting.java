package br.ufla.dcc.PingPong.routing;

import java.util.ArrayList;
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

public class BackboneRouting extends NetworkLayer {

	@ShoXParameter(description = "Coordenada máxima para o eixo x dos nós")
	private double xAxisLimit;
	
	@ShoXParameter(description = "Coordenada máxima para o eixo y dos nós")
	private double yAxisLimit;
	
	private Position backboneRootPosition;
	
	private boolean iAmBackbone = false;
	
	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();
	
	/** Lista de nós vizinhos */
	private List <Node> neighbors = null;
	private List<NodeId> backboneNeighbors = new ArrayList<>();
	
	private void testBackbones() {
		if (!backboneNeighbors.isEmpty()) {
			for (NodeId id : backboneNeighbors) {
				System.err.print(id.asInt() + " <<<<< ");
			}
			System.err.println();
		} else {
			System.err.println("DEU RUIM PRA CARALHO AUAHAUHAUHAUAHUAHAUHAUAHUAHAUHAUAH");
		}
	}
	
	/** Função para obter o nó vizinho mais próximo do destino */
	private NodeId getMinDistanceNodeId(Position destinationPosition) {
		NodeId minDistanceNodeId = null;
		double minDistance = node.getPosition().getDistance(destinationPosition);
		Position myPosition = node.getPosition();
		NodeId selectedBackboneNode = node.getId();
		if (!backboneNeighbors.isEmpty()) {
			
			double maxAngleAperture = Math.PI/2 * (1 - (1/(minDistance * 0.8)));
			for	(NodeId neighbor : backboneNeighbors) {
				Position currentBackboneNeighborPosition = SimulationManager.getInstance().
						queryNodeById(neighbor).getPosition();
				double neighDistanceFromDestination = currentBackboneNeighborPosition.getDistance(destinationPosition);
				if (neighDistanceFromDestination < minDistance &&
						(myPosition.computeAngle(destinationPosition, currentBackboneNeighborPosition) < maxAngleAperture ||
						2*Math.PI - myPosition.computeAngle(destinationPosition, currentBackboneNeighborPosition) < maxAngleAperture)) {
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
			//System.err.println("SELECIONOU O BACKBONE!!!!!!!!!!!!!!!!!!!!!!!!!");
			return selectedBackboneNode;
		}
		
		return minDistanceNodeId;
	}	

	
	private void routePacket (Packet packet) {
		startupNeighborhood();

		Position destinationPosition = SimulationManager.getInstance().
				queryNodeById(packet.getReceiver()).getPosition();

		NodeId closestId = getMinDistanceNodeId(destinationPosition);
		if (closestId == null) {
			System.err.println(this.id + " >>>> BURACO ENCONTRADO, SAINDO DA APLICAÇÃO!!!");
			System.exit(1);
		}
		GeoRoutingPacket newPacket = new GeoRoutingPacket(sender, closestId, packet);
		
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
			
			/* Se o destino da mensagem é o nó, então manda para a camada de aplicação, senão continua
			 * o roteamento (envia para o nó vizinho mais próximo do destino) */
			if (enclosed.getReceiver().equals(getId())) {
				sendPacket(enclosed);
			} else {
				// Ferramentas de depuração -------------------------
				debug.print("[Roteamento] Mensagem não é para mim, será enviada para camada abaixo (LLC)", sender);
				// --------------------------------------------------
				routePacket(enclosed);
			}	
		} else if (packet instanceof BackboneRoutingPacket) {
			BackboneRoutingPacket backbonePacket = (BackboneRoutingPacket) packet;
			//System.err.println("<><><><> " + backbonePacket.getType() + " <><><><>");
			//System.err.println("<><><> "+ backbonePacket.getType() == SWITCH_SELF_AND_BROADCAST + " <><><>");
			if (backbonePacket.getType() == SWITCH_SELF_AND_BROADCAST) {
				NodeId senderId = backbonePacket.getSender().getId();
				//System.out.println("<><><><><><><><><>" + !backboneNeighbors.contains(senderId));
				if (!backboneNeighbors.contains(senderId)) {
					backboneNeighbors.add(senderId);
					//testBackbones();
				}
			} else if (backbonePacket.getType() == CHOOSE_TO_SWITCH) {
				if (!iAmBackbone) {
					BackboneStartAdviseWUC wuc = new BackboneStartAdviseWUC(sender, backbonePacket.getRootPosition(), 127);
					sendEventSelf(wuc);
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
		if (node.getId().asInt() == 10) {
			switchToBackbone(true, node.getPosition());
		}
	}
	
	private void switchToBackbone(boolean isTheFirst, Position rootPosition) {
		iAmBackbone = true;
		BackboneRoutingPacket packet = new BackboneRoutingPacket(sender, 
				NodeId.ALLNODES, SWITCH_SELF_AND_BROADCAST);
		sendPacket(packet);
		startupNeighborhood();
		backboneRootPosition = rootPosition;
		if (isTheFirst) {
			generateCentralNodeBackboneV2();
		} else {
			propagateBackboneAwayFromCenterV2();
		}
    }
	
	private void propagateBackboneAwayFromCenterV1() {
		double rad23 = Math.PI/8;
		double rad157 = 7*Math.PI/8;
		Position myPosition = node.getPosition();
		for (int i = 0; i < 1; i++) {
			Node nextBackboneNode = node;
			for (Node neighbor : neighbors) {
				Position currentNeighborPosition = neighbor.getPosition();
				Position nextBackbonePosition = nextBackboneNode.getPosition();
				if (myPosition.getDistance(currentNeighborPosition) > 
				myPosition.getDistance(nextBackbonePosition) &&
				myPosition.computeAngle(backboneRootPosition, currentNeighborPosition) >= rad157 + i*rad23 &&
				myPosition.computeAngle(backboneRootPosition, currentNeighborPosition) < rad157 + (i + 1)*rad23) {
					nextBackboneNode = neighbor;
				}
			}
			if (nextBackboneNode != node) {
				BackbonePropagationWUC wuc =
						new BackbonePropagationWUC(sender, nextBackboneNode.getId(), CHOOSE_TO_SWITCH, 5037 * (i+1));
				sendEventSelf(wuc);
			}
		}
	}
	
	private int getQuadrant(Position testPosition) {
		if (testPosition.getXCoord() > backboneRootPosition.getXCoord()) {
			if (testPosition.getYCoord() > backboneRootPosition.getYCoord()) {
				return 0;
			} else {
				return 3;
			}
		} else {
			if (testPosition.getYCoord() > backboneRootPosition.getYCoord()) {
				return 1;
			} else {
				return 2;
			}
		}
	}
	
	private void propagateBackboneAwayFromCenterV2() {
		Position myPosition = node.getPosition();
		Position myXaxis = new Position(xAxisLimit, myPosition.getYCoord());
		double rad90 = Math.PI/2d;
		double rad45 = Math.PI/4d;
		int quadrant = getQuadrant(myPosition);
		
		for (int i = 0; i < 1; i++) {
			double nearestPerfectAngleRatio = 2*Math.PI;
			Node nextBackboneNode = node;
			for (Node neighbor : neighbors) {
				Position currentNeighborPosition = neighbor.getPosition();
				if (Math.abs(myPosition.computeAngle(myXaxis, currentNeighborPosition) - (quadrant*rad90 + rad45)) < nearestPerfectAngleRatio) {
					nearestPerfectAngleRatio = Math.abs(myPosition.computeAngle(myXaxis, currentNeighborPosition) - (quadrant*rad90 + rad45));
					nextBackboneNode = neighbor;
				}
			}
			if (nextBackboneNode != node) {
				BackbonePropagationWUC wuc =
						new BackbonePropagationWUC(sender, nextBackboneNode.getId(), CHOOSE_TO_SWITCH, 5037 * (i+1));
				sendEventSelf(wuc);
			}
		}
	}

	private void generateCentralNodeBackboneV1() {
		Position myXaxis = new Position(xAxisLimit, node.getPosition().getYCoord());
		double rad90 = Math.PI/2d;
		Position myPosition = node.getPosition();
		for (int i = 0; i < 4; i++) {
			Node nextBackboneNode = node;
			for (Node neighbor : neighbors) {
				Position currentNeighborPosition = neighbor.getPosition();
				Position nextBackbonePosition = nextBackboneNode.getPosition();
				if (myPosition.getDistance(currentNeighborPosition) > 
				myPosition.getDistance(nextBackbonePosition) && (
				myPosition.computeAngle(myXaxis, currentNeighborPosition) >= i * rad90 &&
				myPosition.computeAngle(myXaxis, currentNeighborPosition) < (i+1) * rad90)) {
					nextBackboneNode = neighbor;
				}
			}
			if (nextBackboneNode != node) {
				BackbonePropagationWUC wuc = 
						new BackbonePropagationWUC(sender, nextBackboneNode.getId(), CHOOSE_TO_SWITCH, 10037 * (i+1));
				sendEventSelf(wuc);
			}
		}
	}
	
	private void generateCentralNodeBackboneV2() {
		Position myXaxis = new Position(xAxisLimit, node.getPosition().getYCoord());
		double rad45 = Math.PI/4d;
		double rad90 = Math.PI/2d;
		Position myPosition = node.getPosition();
		for (int i = 0; i < 4; i++) {
			double nearestPerfectAngleRatio = 2*Math.PI;
			Node nextBackboneNode = node;
			for (Node neighbor : neighbors) {
				Position currentNeighborPosition = neighbor.getPosition();
				if (Math.abs(myPosition.computeAngle(myXaxis, currentNeighborPosition) - (i*rad90 + rad45)) < nearestPerfectAngleRatio) {
					nearestPerfectAngleRatio = Math.abs(myPosition.computeAngle(myXaxis, currentNeighborPosition) - (i*rad90 + rad45));
					nextBackboneNode = neighbor;
				}
			}
			if (nextBackboneNode != node) {
				BackbonePropagationWUC wuc = 
						new BackbonePropagationWUC(sender, nextBackboneNode.getId(), CHOOSE_TO_SWITCH, 10037 * (i+1));
				sendEventSelf(wuc);
			}
		}
	}

	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException { 
		debug.write(sender);
		if (wuc instanceof BackbonePropagationWUC) {
			NodeId nextBackboneNode = ((BackbonePropagationWUC) wuc).getReceiverId();
			BackboneRoutingPacket packet = new BackboneRoutingPacket(sender, nextBackboneNode,
					CHOOSE_TO_SWITCH, backboneRootPosition);
			sendPacket(packet);
		} else if (wuc instanceof BackboneStartAdviseWUC) {
			Position rootPosition = ((BackboneStartAdviseWUC) wuc).getcentralBackboneNode();
			switchToBackbone(false, rootPosition);
		}
	}

}
