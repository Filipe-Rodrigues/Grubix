package br.ufla.dcc.PingPong.routing;

import java.util.List;

import br.ufla.dcc.PingPong.ToolsDebug;
import br.ufla.dcc.PingPong.routing.SendDelayedWakeUpCall;
import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

public class GeoRouting extends NetworkLayer {

	/** Objeto para a depuração */
	ToolsDebug debug = ToolsDebug.getInstance();
	
	/** Lista de nós vizinhos */
	private List <Node> neighbors = null;
	
	
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
	
	private void sendDelayed(Packet packet, double delay) {
		SendDelayedWakeUpCall sdwuc = new SendDelayedWakeUpCall(sender, packet, delay);
		sendEventSelf(sdwuc);
	}
	
	private void routePacket (Packet packet, int hopCount) {
		if (this.neighbors == null) {
			this.neighbors = node.getNeighbors();
		}

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
		newPacket.setHopCount(hopCount + 1);
		sendDelayed(newPacket, 50);
		//sendPacket(newPacket);
	}
	
	
	@Override
	public void lowerSAP(Packet packet) throws LayerException {
		if (packet instanceof GeoRoutingPacket) {
			GeoRoutingPacket geoRoutingPacket = (GeoRoutingPacket) packet;
			Packet enclosed = geoRoutingPacket.getEnclosedPacket();
			
			/* Se o destino da mensagem é o nó, então manda para a camada de aplicação, senão continua
			 * o roteamento (envia para o nó vizinho mais próximo do destino) */
			if (enclosed.getReceiver().equals(getId())) {
				System.out.print(geoRoutingPacket.getHopCounter() + "\t");
				sendPacket(enclosed);
			} else {
				// Ferramentas de depuração -------------------------
				debug.print("[Roteamento] Mensagem não é para mim, será enviada para camada abaixo (LLC)", sender);
				// --------------------------------------------------
				routePacket(enclosed, geoRoutingPacket.getHopCounter());
			}	
		}
	}

	
	@Override
	public void upperSAP(Packet packet) throws LayerException {		
		debug.write(debug.strPkt(packet), sender);
		Position myPos = node.getPosition();
		Position destPos = SimulationManager.getInstance().queryNodeById(packet.getReceiver()).getPosition();
		System.out.print(myPos.getDistance(destPos) + "\t");
		routePacket(packet, -1);
	}

	
	@Override
	protected void processEvent(StartSimulation start) {
		super.processEvent(start);
	}

	
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException { 
		debug.write(sender);
		if (wuc instanceof SendDelayedWakeUpCall) {
			SendDelayedWakeUpCall sdwuc = (SendDelayedWakeUpCall) wuc;
			sendPacket(sdwuc.getPacket());
		}
	}

}
