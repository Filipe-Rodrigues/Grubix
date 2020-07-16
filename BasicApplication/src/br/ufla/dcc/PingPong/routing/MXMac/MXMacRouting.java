package br.ufla.dcc.PingPong.routing.MXMac;

import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.CLOCKWISE_BB_CHANNEL;
import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.COUNTER_CLOCKWISE_BB_CHANNEL;
import static br.ufla.dcc.PingPong.routing.MXMac.AuxiliarConstants.*;
import static br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager.MXMAC_CONFIG;

import java.util.Queue;

import br.ufla.dcc.PingPong.routing.MXMac.MXMacGraphOperations.CalculationResults;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.movement.FromConfigStartPositions;
import br.ufla.dcc.PingPong.routing.GeoRoutingPacket;
import br.ufla.dcc.PingPong.routing.SendDelayedWakeUpCall;
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
import br.ufla.dcc.grubix.simulator.util.Pair;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class MXMacRouting extends NetworkLayer {

	private static final int BASE_DELAY_FOR_BB_FORMATION = 40450;
	
	@ShoXParameter(description = "Porção máxima do campo em que os backbones poderão se propagar. 1 = 100% do campo.", defaultValue = "0.96d")
	private double backboneBoundaryRatio;
	
	/** Configuração de backbone deste nó */
	private MXMacRoutingParameters bbSettings;
	
	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();
	
	private MXMacGraphOperations graphOps = MXMacGraphOperations.getInstance();
	
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
				routePacketGeoRouting(enclosed);
			}	
		} else if (packet instanceof MXMacRoutingControlPacket) {
			MXMacRoutingControlPacket controlPack = (MXMacRoutingControlPacket) packet;
			bbSettings.addBackbone(controlPack.getSender().getId(), controlPack.getBackboneType());
			NodeId nextBB = controlPack.getNextSelectedBackbone();
			if (nextBB != null && nextBB.equals(node.getId())) {
				turnIntoBackbone(controlPack);
			}
		} else if (packet instanceof MXMacRoutingPacket) {
			
		}
	}

	
	@Override
	public void upperSAP(Packet packet) throws LayerException {		
		debug.write(debug.strPkt(packet), sender);
		chooseRoutingProcedure(packet);
	}

	@Override
	protected void processEvent(StartSimulation start) {
		super.processEvent(start);
		boolean loadingFromFile = Configuration.getInstance().getPositionGenerator() instanceof FromConfigStartPositions;
		startupNeighborList(loadingFromFile);
		if (!loadingFromFile) {
			startBackboneConfiguration();
		}
	}

	
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException { 
		debug.write(sender);
		if (wuc instanceof SendDelayedWakeUpCall) {
			SendDelayedWakeUpCall sdwuc = (SendDelayedWakeUpCall) wuc;
			sendPacket(sdwuc.getPacket());
		} else if (wuc instanceof BackboneNotificationWUC) {
			announceConversion();
		}
	}
	
	private void startupNeighborList(boolean loadSaved) {
		bbSettings = new MXMacRoutingParameters(node, loadSaved);
	}
	
	/** Função para obter o nó vizinho mais próximo do destino */
	private NodeId getMinDistanceNodeId(Position destinationPosition) {
		NodeId minDistanceNodeId = null;
		double minDistance = node.getPosition().getDistance(destinationPosition);

		for (Node neighbor : bbSettings.getNeighbors()) {
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
	
	private void routePacketGeoRouting (Packet packet) {
		Position destinationPosition = SimulationManager.getInstance().
				queryNodeById(packet.getReceiver()).getPosition();

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
	
	private void startBackboneConfiguration() {
		int id = node.getId().asInt();
		switch (id) {
			case 10:
				turnIntoBackbone(LEFT, COUNTER_CLOCKWISE_BB_CHANNEL, 2);
				break;
			case 11:
				turnIntoBackbone(RIGHT, CLOCKWISE_BB_CHANNEL, 0);
				break;
			case 12:
				turnIntoBackbone(DOWN, COUNTER_CLOCKWISE_BB_CHANNEL, 3);
				break;
			case 13:
				turnIntoBackbone(DOWN, CLOCKWISE_BB_CHANNEL, 1);
				break;
			case 14:
				turnIntoBackbone(RIGHT, COUNTER_CLOCKWISE_BB_CHANNEL, 2);
				break;
			case 15:
				turnIntoBackbone(LEFT, CLOCKWISE_BB_CHANNEL, 0);
				break;
			case 16:
				turnIntoBackbone(UP, COUNTER_CLOCKWISE_BB_CHANNEL, 3);
				break;
			case 17:
				turnIntoBackbone(UP, CLOCKWISE_BB_CHANNEL, 1);
				break;
			default:
				break;
		}
	}
	
	private void turnIntoBackbone(Position direction, int type, int delayLevel) {
		bbSettings.setBackboneType(type);
		bbSettings.setHypocenter(node.getPosition());
		bbSettings.setDirection(direction);
		expandBackbone(BackboneDistributor.selectNextBackboneNode(bbSettings));
		if (delayLevel > 0) {
			sendEventSelf(new BackboneNotificationWUC(sender, BASE_DELAY_FOR_BB_FORMATION * delayLevel));
		} else {
			announceConversion();
		}
	}
	
	private void turnIntoBackbone(MXMacRoutingControlPacket controlPack) {
		bbSettings.setBackboneType(controlPack.getBackboneType());
		bbSettings.setHypocenter(controlPack.getBackboneLineRoot());
		bbSettings.setDirection(controlPack.getGrowthDirection());
		if (BackboneDistributor.canExpandBackbone(node)) {
			expandBackbone(BackboneDistributor.selectNextBackboneNode(bbSettings));
		} else {
			expandBackbone(node);
			
		}
		announceConversion();
	}

	private void expandBackbone(Node nextBBnode) {
		bbSettings.setNextBBnode(nextBBnode);
	}
	
	private void announceConversion() {
		//System.err.println("STARTED " + node.getId() + ": " + bbSettings.backboneType);
		MXMacRoutingControlPacket packet = new MXMacRoutingControlPacket(sender, NodeId.ALLNODES, 
				bbSettings.getDirection(), bbSettings.getHypocenter(), (bbSettings.getNextBBnode().equals(node)) 
				? (null) 
				: (bbSettings.getNextBBnode().getId()), bbSettings.getBackboneType());
		BackboneConfigurationManager.getInstance(MXMAC_CONFIG).setNextBackboneNode(node.getId(), 
				bbSettings.getNextBBnode().getId(), bbSettings.getDirection());
		sendPacket(packet);
	}
	
	private void chooseRoutingProcedure(Packet packet) {
		Position thisNode = node.getPosition();
		Position destination = SimulationManager.getInstance().
				queryNodeById(packet.getReceiver()).getPosition();
		CalculationResults results = graphOps.getShortestPath(thisNode, destination);
		double normalRouteWeight = graphOps.getGeoRoutingDistance(thisNode, destination);
		double backboneRouteWeight = results.getDistance();
		if (normalRouteWeight <= backboneRouteWeight) {
			routePacketGeoRouting(packet);
		} else {
			routePacketInBackbone(packet, results.getBackboneRoute());
		}
	}
	
	private void routePacketInBackbone(Packet packet, Queue<Pair<Integer, Position>> backboneSegments) {
		if (!backboneSegments.isEmpty()) {
			Pair<Integer, Position> segment = backboneSegments.peek();
			boolean sent = false;
			if (bbSettings.amIBackbone()) {
				NodeId nextBBnode = bbSettings.getNextBBnode().getId();
				
			}
		}
	}
	
}



























