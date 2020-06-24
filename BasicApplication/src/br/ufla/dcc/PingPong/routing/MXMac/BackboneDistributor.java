package br.ufla.dcc.PingPong.routing.MXMac;

import static br.ufla.dcc.PingPong.routing.MXMac.AuxiliarConstants.*;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.node.Node;

public class BackboneDistributor {
	
	private static BackboneDistributor distributor;
	private double backboneBoundaryRatio = 0.96;
	
	private BackboneDistributor() {
	}
	
	public static void startup(double bbBoundaryRatio) {
		distributor = new BackboneDistributor();
		distributor.backboneBoundaryRatio = bbBoundaryRatio;
	}
	
	public static Node selectNextBackboneNode(MXMacRoutingParameters params) {
		if (distributor == null) {
			distributor = new BackboneDistributor();
		}
		return distributor.selectNext(params);
	}
	
	public static boolean canExpandBackbone(Node node) {
		return distributor.canExpand(node);
	}
	
	private Node selectNext(MXMacRoutingParameters params) {
		Position myPosition = params.node.getPosition();
		Position target = getTargetFromDirection(params.hypocenter, params.direction);
		Node selected = params.node;
		double minDistance = myPosition.getDistance(target);
		for (Node neighbor : params.neighbors) {
			if (!canSelect(neighbor, params)) continue;
			double neighDistance = neighbor.getPosition().getDistance(target);
			if (neighDistance < minDistance) {
				minDistance = neighDistance;
				selected = neighbor;
			}
		}
		return selected;
	}
	
	private boolean canSelect(Node node, MXMacRoutingParameters params) {
		return (node.getId().asInt() < 10 || node.getId().asInt() > 17)
			   && (params.backboneType == 1)
				? (!params.bbNeighborsType1.contains(node))
				: (!params.bbNeighborsType2.contains(node));
	}
	
	private Position getTargetFromDirection(Position source, Position direction) {
		double x, y;
		double dirX = direction.getXCoord(), dirY = direction.getYCoord();
		double sX = source.getXCoord(), sY = source.getYCoord();
		x = (dirX == 0) ? (sX) : (MAX_X * dirX);
		y = (dirY == 0) ? (sY) : (MAX_Y * dirY);
		x = (x < 0) ? (0) : (x);
		x = (y < 0) ? (0) : (y);
		return new Position(x, y);
	}
	
	private boolean canExpand(Node node) {
		double nodeX = node.getPosition().getXCoord();
		double nodeY = node.getPosition().getYCoord();
		double lowerXBound = MAX_X * (1 - backboneBoundaryRatio);
		double upperXBound = MAX_X * backboneBoundaryRatio;
		double lowerYBound = MAX_Y * (1 - backboneBoundaryRatio);
		double upperYBound = MAX_Y * backboneBoundaryRatio;
		return nodeX > lowerXBound && nodeX < upperXBound && nodeY > lowerYBound && nodeY < upperYBound;
	}
	
}
