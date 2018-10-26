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


import java.util.Vector;


import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGRectElement;
import org.w3c.dom.NodeList;

import br.ufla.dcc.grubix.simulator.Position;



/**
 * Represents the physical map with obstacles where the network is deployed.
 * The map is constructed from a configuration file in SVG format called "physicalmap.svg".
 * So far, only rectangles ("walls") arranged horizontally or vertically are supported.
 * For the calculation of the signal attenuations, thickness and material of "walls"
 * are taken into account. The material of a wall is represented as a {@link MaterialProperties} 
 * object. The calculation of attenuation values is left to subclasses of {@link PhysicalModel}.
 * @author jlsx
 *
 */
public final class Environment {
	
	/**
	 * Logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(Environment.class.getName());
	
	/** ID of the SVG rectangle which represents the deployment area's outer boundary. */
	public static final String BOUNDARY_ID = "boundary";
		
	/** The name of the SVG file which stores the environment information. */
	private String svgFileName;
	
	/**
	 * Stores all rectangles in SVG document.
	 */
	private NodeList rects;
	 
	/** Stores the width of the deployment area. */
	private double width = -1.0;
	
	/** Stores the height of the deployment area. */
	private double height = -1.0;
	
	/**
	 * Constructor for class Environment is private since class is Singleton.
	 * In order to obtain an instance of this class, use {@link getInstance}
	 * @param svgFileName The name of the SVG file representing the environment
	 */
	public Environment(String svgFileName) {
		try {
			this.svgFileName = svgFileName;
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		    String uri = "file:" + svgFileName;
		    SVGDocument doc = (SVGDocument) f.createDocument(uri);
		    SVGSVGElement root = doc.getRootElement();
		    rects = root.getElementsByTagName("rect");
		    LOGGER.info("Rects has " + rects.getLength() + " elements.");
		} catch (Exception ex) {
			LOGGER.fatal("Exception " + ex + " occured.", ex);
		}		
	}

	/**
	 * Retrieves the list of obstacles that lie on the direct path between receiver
	 * and sender. Currently, these are only SVGRectElement objects. In the future,
	 * other shapes might be supported, as well.
	 * @param receiver The position of the receiving node
	 * @param sender The position of the sending node
	 * @return A list of obstacles that are intersected by the path between receiver
	 * and sender
	 */
	public Vector<Obstacle> getIntersectedObstacles(Position receiver, Position sender) {
		Vector<Obstacle> v = new Vector<Obstacle>();
		int max = rects.getLength();
		for (int i = 0; i < max; i++) {
			Obstacle o = new RectObstacle((SVGRectElement) rects.item(i));
			if (o.getIntersectionLength(receiver, sender) > 0) {
				v.add(o);
			}
		}
		return v;
	}
	
	/**
	 * @return The width of the deployment area in meters.
	 */
	public double getWidth() {
		if (this.width > 0) {
			return this.width;
		}
		int max = rects.getLength();
		for (int i = 0; i < max; i++) {
			SVGRectElement rect = (SVGRectElement) rects.item(i);
			if (rect.getId().equals(BOUNDARY_ID)) {
				this.width = rect.getWidth().getBaseVal().getValue();
				this.height = rect.getHeight().getBaseVal().getValue();
				return this.width;
			}
		}
		return 0.0;
	}
	
	/**
	 * @return The height of the deployment area in meters.
	 */
	public double getHeight() {
		if (this.height > 0) {
			return this.height;
		}
		int max = rects.getLength();
		for (int i = 0; i < max; i++) {
			SVGRectElement rect = (SVGRectElement) rects.item(i);
			if (rect.getId().equals(BOUNDARY_ID)) {
				this.height = rect.getHeight().getBaseVal().getValue();
				this.width = rect.getWidth().getBaseVal().getValue();
				return this.height;
			}
		}
		return 0.0;
	}
	
	/**
	 * @return The name of the SVG file which stores the environment information.
	 */
	public String getSVGFileName() {
		return this.svgFileName;
	}
}
