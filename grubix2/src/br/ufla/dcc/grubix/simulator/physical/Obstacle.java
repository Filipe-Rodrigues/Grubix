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


import org.w3c.dom.svg.SVGElement;

import br.ufla.dcc.grubix.simulator.Position;


/**
 * This class is an abstraction for an obstacle that is positioned in the physical
 * environment where the network is deployed. An obstacle is mainly characterized by
 * its material properties 
 * @see br.ufla.dcc.grubix.simulator.physical.MaterialProperties
 * @author jlxs
 *
 */
public abstract class Obstacle {
	
	/**
	 * Represents the material properties of this obstacle.
	 */
	protected MaterialProperties properties;
	
	/**
	 * The SVGElement which is represented by this Obstacle object.
	 */
	protected SVGElement element;
	
	/**
	 * Constructor for this class. Since the class is abstract, it cannot be
	 * directly invoked.
	 * @param element The SVGElement which is represented by this Obstacle object.
	 */
	public Obstacle(SVGElement element) {
		this.element = element;
		this.properties = new MaterialProperties(element);
	}
	
	/**
	 * In case the methods and fields of this class are not sufficient to compute
	 * certain signal propagation values, with this method the network designer
	 * gets a direct reference to the encapsulated SVGElement, so he/she can
	 * access all desired properties directly. 
	 * @return The encapsulated SVGElement that forms the obstacle
	 */
	public final SVGElement getSVGElement() {
		return element;
	}
	
	/**
	 * Calculates the signal attenuation factor that a signal encounters by
	 * directly penetrating through the obstacle. The resulting value is the product
	 * of a material properties factor and the length of the intersection segment.
	 * @param receiver The position of receiving node
	 * @param sender The position of the sending node
	 * @return Signal attenuation factor as described above
	 */
	public final double getAttenuation(Position receiver, Position sender) {
		double intersectionLength = this.getIntersectionLength(receiver, sender);
		return properties.getAttenuationFactor(intersectionLength);
	}
	
	/**
	 * Method designed to be overridden by subclasses to compute the length of the
	 * segment of the direct line between sender and receiver which intersects this
	 * obstacle.
	 * @param receiver The position of receiving node
	 * @param sender The position of the sending node
	 * @return The length of the intersection segment
	 */
	protected abstract double getIntersectionLength(Position receiver, Position sender);
}
