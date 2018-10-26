/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.physical;

import java.util.LinkedList;
import java.util.List;


import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGRectElement;

import br.ufla.dcc.grubix.simulator.Position;



/**
 * This class models rectangular obstacles in the physical environment (deployment
 * area) of the network.
 * @author jlsx
 */
public class RectObstacle extends Obstacle {

	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(RectObstacle.class.getName());

	/** The encapsulated SVG rectangle (obstacle). */
	private SVGRectElement rect;
	
	/**
	 * Constructs an obstacle object that wraps a rectangular SVG element. If the
	 * SVG rectangle represents the environment's boundary, no intersection is
	 * calculated.
	 * @param element A rectangular SVG element (<rect>) that represents an obstacle
	 */
	public RectObstacle(SVGRectElement element) {
		super(element);
		if (!element.getId().equals(Environment.BOUNDARY_ID)) {
			this.rect = element;
			LOGGER.info("Building rectangular obstacle for SVG element " + rect.getId());
		}
	}

	/**
	 * Determines the length of the segment of the line from receiver to sender which
	 * intersects the rectangle defined by rect. Intersection takes place
	 * at all, iff the line from receiver to sender intersects exactly two of the
	 * lines AB, AC, BD or CD where rect = (A, B, C, D);
	 * @param receiver The start point of the line which is tested for intersection
	 * @param sender The end point of the line which is tested for intersection
	 * @return The length of the line segment which intersects rect. If no
	 * intersection occurs, this value is 0.0
	 */
	protected final double getIntersectionLength(Position receiver, Position sender) {
		if (rect == null) {
			LOGGER.debug("Rect is null");
			return 0;
		}
		LOGGER.info("Considering rectangle " + rect.getId());
		
		double x1 = receiver.getXCoord();
		double y1 = receiver.getYCoord();
		double x2 = sender.getXCoord();
		double y2 = sender.getYCoord();

		double ax = rect.getX().getBaseVal().getValue();
		double ay = rect.getY().getBaseVal().getValue();
		double bx = ax + rect.getWidth().getBaseVal().getValue();
		double by = ay;
		double cx = ax;
		double cy = ay + rect.getHeight().getBaseVal().getValue();
		double dx = bx;
		double dy = cy;
		
		//correct way to compare floats and doubles
		if (Math.abs(x1  - x2) < 0.000001) {
			if ((Math.abs(x1 - ax) < 0.000001) || (Math.abs(x1 - bx) < 0.000001)) {
				// we intersect exaclty one of the sidelines of rect
				return 0;    // we consider this case no intersection
			}
			x2 += 0.0000001;   // we add a tiny value, to avoid division by zero
		} else if (Math.abs(y1 - y2) < 0.000001) {
			if ((Math.abs(y1 - ay) < 0.000001) || (Math.abs(y1 - cy) < 0.000001)) {
				// we intersect exaclty one of the sidelines of rect
				return 0;    // we consider this case no intersection
			}
		}
				
		double minX = Math.min(x1, x2);
		double maxX = Math.max(x1, x2);
		double minY = Math.min(y1, y2);
		double maxY = Math.max(y1, y2);
		
		// line xy is described by y = mx + b with: 
		double m = (y2 - y1) / (x2 - x1); 
		double b = (x2 * y1 - x1 * y2) / (x2 - x1);
		
		// start with line a = AB
		double va = (ay - b) / m;
		LOGGER.info("ax: " + ax + ", ay: " + ay + ", bx: " + bx + ", by:" + by + ", va: " + va);
		if ((va <= minX) || (va >= maxX) || (va <= ax) || (va >= bx)) {
			va = Double.NaN;
		}

		// next, we take line b = BD
		double vb = m * bx + b;
		LOGGER.info("dx: " + dx + ", dy:" + dy + ", vb: " + vb);
		if ((vb <= minY) || (vb >= maxY) || (vb <= by) || (vb >= dy)) {
			vb = Double.NaN;
		}		

		// next, we take line c = AC
		double vc = m * ax + b;
		LOGGER.info("cx: " + cx + ", cy:" + cy + ", vc: " + vc);
		if ((vc <= minY) || (vc >= maxY) || (vc <= ay) || (vc >= cy)) {
			vc = Double.NaN;
		}		
				
		// last, we take line d = CD
		double vd = (cy - b) / m;
		LOGGER.info("vd: " + vd);
		if ((vd <= minX) || (vd >= maxX) || (vd <= cx) || (vd >= dx)) {
			vd = Double.NaN;
		}
		
		// determine distance between the two line intersection points
		List<Position> v = new LinkedList<Position>();
		if (!Double.isNaN(va)) {
			v.add(new Position(va, ay));
		}
		if (!Double.isNaN(vb)) {
			v.add(new Position(bx, vb));
		}
		if (!Double.isNaN(vc)) {
			v.add(new Position(ax, vc));
		}
		if (!Double.isNaN(vd)) {
			v.add(new Position(vd, cy));
		}
		if (v.size() < 2) {
			LOGGER.error("Programming bug. Vector v contains less than 2 elements.");
			return 0;
		}
		
		return Math.sqrt(Math.pow(v.get(0).getXCoord() - v.get(1).getXCoord(), 2) 
				+ Math.pow(v.get(0).getYCoord() - v.get(1).getYCoord(), 2));
	}
}
