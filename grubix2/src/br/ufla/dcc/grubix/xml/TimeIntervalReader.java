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

package br.ufla.dcc.grubix.xml;

import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX Parser that parses an XML trace file in chunks. Each chunk is determined by a (simulation)
 * time interval t. Thus, first, all XML elements starting from time 0 to time t are
 * parsed, then the parsing is put on hold. Only when explicitly resumed by an external object,
 * the next chunk starting from time t + 1 to 2t is parsed, and so on. ShoX trace files all
 * consist of empty XML elements with several attributes. Hence, a chunk is stored as a list of
 * (name, value) pairs. 
 * @author jlsx
 */
public class TimeIntervalReader extends DefaultHandler implements Runnable {

	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(TimeIntervalReader.class.getName());

	/** The XML reader used to parse the trace file. */
	private XMLReader xmlReader;
	
	/** The trace file name. */
	private String xmlTraceFile;
	
	/** The time interval that determines the trace chunks. */
	private double interval; 
	
	/** Set to true whenever an external object wakes us up. */
	private boolean wasInterrupted;
	
	/** Flag indicating whether or not the parsing of the current chunk is already done. */
	private boolean ready = false;
	
	/** The number of times that {@link #generateTraffic(Collection)} was already called plus 1. */
	private int round = 2;
	
	/** The list of (name, value) pairs representing all traces per chunk. */
	private Vector<HashMap<String, String>> attributes;
	
	/**
	 * Sets up the underlying SAX parser.
	 * @param xmlTraceFile The ShoX trace file to be parsed
	 * @param interval The time interval that determines the trace chunks
	 */
	public TimeIntervalReader(String xmlTraceFile, double interval) {
		try {
			this.xmlTraceFile = xmlTraceFile;
			this.interval = interval;
			this.wasInterrupted = false;
			this.attributes = new Vector<HashMap<String, String>>();
	        SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setNamespaceAware(true);
	        SAXParser saxParser = spf.newSAXParser();
	        xmlReader = saxParser.getXMLReader();
	        xmlReader.setContentHandler(this);
		} catch (Exception e) {
			LOGGER.fatal("The ShoX trace file " + xmlTraceFile + " could not be parsed successfully. " + e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/** In this method the TimeIntervalReader is concurrently executed. */
	public void run() {
		try {
			xmlReader.parse(this.xmlTraceFile);
		} catch (Exception se) {
			LOGGER.fatal("The ShoX trace file " + xmlTraceFile + " could not be parsed successfully. " + se);
			System.exit(1);
		}
	}
	
    /** 
     * The parser calls this function for each element in a document.
     * @param namespaceURI The Namespace URI, or empty string, if no URI is there
     * @param localName The local name (without prefix)
     * @param qName The qualified name (with prefix)
     * @param atts The attributes attached to the element
     * @throws SAXException Any SAX exception, possibly wrapping another exception
     */
    public final void startElement(String namespaceURI, String localName,
            String qName, Attributes atts)	throws SAXException {
     	if (!localName.equals("trace")) {
     		if (Double.parseDouble(atts.getValue("time")) <= this.round * this.interval) {
	    		HashMap<String, String> m = new HashMap<String, String>();
	    		for (int i = 0; i < atts.getLength(); i++) {
	    			m.put(atts.getLocalName(i), atts.getValue(i));
	    		}
	     		this.attributes.add(m);
     		} else {
     			this.round++;
     			this.ready = true;
     			
    		
     			while (!this.wasInterrupted) {
     				// wait until current trace chunk is read by getNewTraces() 
     			}
     			this.wasInterrupted = false;
     			this.attributes = new Vector<HashMap<String, String>>();
     			
     			// add the last element directly because that belongs already to next round
     			HashMap<String, String> m = new HashMap<String, String>();
	    		for (int i = 0; i < atts.getLength(); i++) {
	    			m.put(atts.getLocalName(i), atts.getValue(i));
	    		}
	     		this.attributes.add(m);
     		}
    	}
    }
    
    /** Called at the end of the document. */
    public void endDocument() {
    	this.ready = true;
    }
    
    /** @return The trace entries from the trace file for the current simulation time interval. */
    public Vector<HashMap<String, String>> getNewTraces() {
    	while (!this.ready) {
    		// wait until next round of traces is generated
    	}
    	return this.attributes;
    }
    
    /** Used to resume parsing. */
    public void interrupt() {
    	this.ready = false;
    	this.wasInterrupted = true;
    }
}
