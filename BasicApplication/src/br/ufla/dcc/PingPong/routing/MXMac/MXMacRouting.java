package br.ufla.dcc.PingPong.routing.MXMac;

import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.CLOCKWISE_BB_CHANNEL;
import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.COUNTER_CLOCKWISE_BB_CHANNEL;
import static br.ufla.dcc.PingPong.routing.MXMac.AuxiliarConstants.*;
import static br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager.MXMAC_CONFIG;

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
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class MXMacRouting extends NetworkLayer {

	private static final int BASE_DELAY_FOR_BB_FORMATION = 40450;
	
	@ShoXParameter(description = "Porção máxima do campo em que os backbones poderão se propagar. 1 = 100% do campo.", defaultValue = "0.96d")
	private double backboneBoundaryRatio;
	
	/** Configuração de backbone deste nó */
	private MXMacRoutingParameters bbSettings;
	
	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();

	
	private void startupNeighborList() {
		bbSettings = new MXMacRoutingParameters(node);
	}
	
	/** Função para obter o nó vizinho mais próximo do destino */
	private NodeId getMinDistanceNodeId(Position destinationPosition) {
		NodeId minDistanceNodeId = null;
		double minDistance = node.getPosition().getDistance(destinationPosition);

		for (Node neighbor : bbSettings.neighbors) {
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
		}
	}

	
	@Override
	public void upperSAP(Packet packet) throws LayerException {		
		debug.write(debug.strPkt(packet), sender);
		routePacketGeoRouting(packet);
	}

	
	@Override
	protected void processEvent(StartSimulation start) {
		super.processEvent(start);
		startupNeighborList();
		if (Configuration.getInstance().getPositionGenerator() instanceof FromConfigStartPositions) {
			loadBackboneConfiguration();
		} else {
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
	
	private void loadBackboneConfiguration() {
		
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
		bbSettings.backboneType = type;
		bbSettings.direction = direction;
		bbSettings.hypocenter = node.getPosition();
		expandBackbone();
		if (delayLevel > 0) {
			sendEventSelf(new BackboneNotificationWUC(sender, BASE_DELAY_FOR_BB_FORMATION * delayLevel));
		} else {
			announceConversion();
		}
	}
	
	private void turnIntoBackbone(MXMacRoutingControlPacket controlPack) {
		bbSettings.backboneType = controlPack.getBackboneType();
		bbSettings.direction = controlPack.getGrowthDirection();
		bbSettings.hypocenter = controlPack.getBackboneLineRoot();
		if (BackboneDistributor.canExpandBackbone(node)) {
			expandBackbone();
		} else {
			bbSettings.nextBBnode = node;
		}
		announceConversion();
	}

	private void expandBackbone() {
		bbSettings.nextBBnode = BackboneDistributor.selectNextBackboneNode(bbSettings);
	}
	
	private void announceConversion() {
		MXMacRoutingControlPacket packet = new MXMacRoutingControlPacket(sender, NodeId.ALLNODES, 
				bbSettings.direction, bbSettings.hypocenter, (bbSettings.nextBBnode == node) 
				? (null) 
				: (bbSettings.nextBBnode.getId()), bbSettings.backboneType);
		BackboneConfigurationManager.getInstance(MXMAC_CONFIG).setNextBackboneNode(node.getId(), 
				bbSettings.nextBBnode.getId(), bbSettings.direction);
		sendPacket(packet);
	}
	
}
