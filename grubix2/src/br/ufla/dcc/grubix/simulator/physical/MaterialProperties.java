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

import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGElement;

/**
 * This class represents material properties of the obstacles in the physical
 * environment in which the network is deployed. Currently, this includes:
 * - construction material (concrete, glass, wood, steel, soft)
 * - surface properties (reflection factor, scatter angle)
 * Later, any other important properties might be included.
 * This class also contains constants which describe how the material properties
 * are represented in the underlying SVG environment file. 
 * @author jlsx
 */
public final class MaterialProperties {
	
	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(MaterialProperties.class.getName());
	
	/** Signal attenuation factor for wood.	*/
	public static final double WOOD_ATTENUATION = 0.0;
	
	/** The color value which represents wood in the SVG file. */
	public static final String WOOD_COLOR = "#996633";
	
	/** Functions as index for the ATTENUATIONS array. */
	public static final int WOOD = 0;

	/** Signal attenuation factor for steel. */
	public static final double STEEL_ATTENUATION = 0.0;
	
	/** The color value which represents wood in the SVG file. */
	public static final String STEEL_COLOR = "#666666";
	
	/** Functions as index for the ATTENUATIONS array. */
	public static final int STEEL = 1;

	/** Signal attenuation factor for concrete. */
	public static final double CONCRETE_ATTENUATION = 0.0;
	
	/** The color value which represents wood in the SVG file. */
	public static final String CONCRETE_COLOR = "#CCCCCC";
	
	/** Functions as index for the ATTENUATIONS array. */
	public static final int CONCRETE = 2;

	/** Signal attenuation factor for soft walls. */
	public static final double SOFTWALL_ATTENUATION = 0.0;
	
	/** The color value which represents wood in the SVG file. */
	public static final String SOFTWALL_COLOR = "#FFFFFF";
	
	/** Functions as index for the ATTENUATIONS array. */
	public static final int SOFTWALL = 3;

	/** Stores for each material the corresponding attenuation factor. */
	public static final double[] ATTENUATIONS = {WOOD_ATTENUATION, STEEL_ATTENUATION,
		CONCRETE_ATTENUATION, SOFTWALL_ATTENUATION};
	
	/** Stores for each material the color value which represents it in the SVG file. */
	public static final String[] MATERIALS = {WOOD_COLOR, STEEL_COLOR,
		CONCRETE_COLOR, SOFTWALL_COLOR};

	/** Stores the SVGElement to which this class belongs. */
	private SVGElement element;
	
	/** Stores the attenuation constant for this material. */
	private double attenuation;
	
	/**
	 * Constructor for this class. Fills in all necessary object variables by
	 * parsing the SVG code for the corresponding SVGElement element.
	 * @param element The SVGElement to which this class belongs
	 */
	public MaterialProperties(SVGElement element) {
		this.element = element;
		this.fillProperties();
	}
	
	/**
	 * helper method to parse all important data of element into the
	 * corresponding object variables.
	 *
	 */
	private void fillProperties() {
		/* The color represents the construction material.
		 * Determine color value for current element in SVG tree. This should look
		 * like <path style="...;fill:#xxxxxx;..."> in SVG.
		 */
		String style = element.getAttribute("style");
		int s = style.indexOf("fill:") + 5;
		int t = style.indexOf(";", s);
		String fill = style.substring(s, t);
		
		// now determine attenuation factor for the material that corresponds to "fill"
		for (int j = 0; j < MaterialProperties.MATERIALS.length; j++) {
			if (MaterialProperties.MATERIALS[j].equalsIgnoreCase(fill)) {
				attenuation = MaterialProperties.ATTENUATIONS[j];
			}
		}
	}
	
	/**
	 * Determines a signal attenuation factor f for a signal that intersects an
	 * obstacle with these material properties for a length defined in the
	 * parameter. The signal strength at the receiver s(rx) can then be calculated
	 * as s(rx) =  f * s(tx).
	 * Currently, only the construction material is taken into account (i.e. if
	 * r is made of steel, wood, concrete, etc.). Other properties like surface
	 * properties (which determines reflection) are neglected for the calculation.
	 * @param intersectionLength The length of the signal path segment that intersects
	 * an obstacle with these material properties
	 * @return The resulting signal attenuation factor as describe above
	 */	
	public double getAttenuationFactor(double intersectionLength) {

		LOGGER.info("Element " + element.getId() 
				+ " is intersected with length " + intersectionLength
				+ " and has an attenuation factor of " + attenuation);
		
		return intersectionLength * this.attenuation;
	}
}
