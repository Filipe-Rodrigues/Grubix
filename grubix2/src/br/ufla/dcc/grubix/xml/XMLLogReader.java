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

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import br.ufla.dcc.grubix.debug.compactlogging.CompactFileLogger;
import br.ufla.dcc.grubix.debug.compactlogging.CompactLogInputSource;
import br.ufla.dcc.grubix.debug.compactlogging.CompactLogReader;
import br.ufla.dcc.grubix.debug.compactlogging.XMLFileLogger;

/** 
 * Class representing a parser for log.xml.
 * A parse consists of a static classmethod for start parsing 
 * and many another methodes for parsing.
 * @author Yuan,Zhi
 */
public class XMLLogReader extends DefaultHandler {
  /**
   * the element of statistic unit. 
   *
   */
	private StatisticDataItem st;
	
	private HashMap<Integer, Vector<StatisticDataItem>> subStatistics;
	private boolean flag, flagChac;
	private final String fileName;
	/**
	 * Empty constructor here.	 
	 * 
	 **/
	public XMLLogReader(String fileName, Vector<StatisticDataItem> stVector) {
		//super();
		this.fileName = fileName;
		
	}
	public XMLLogReader(String fileName, HashMap<Integer, Vector<StatisticDataItem>> subStatistics) {
		this.fileName = fileName;
		this.subStatistics = subStatistics;
	}
	public XMLLogReader(String fileName, HashMap<Integer, Vector<StatisticDataItem>> subStatistics, Vector<StatisticDataItem> stVector) {
		this.fileName = fileName;
		this.subStatistics = subStatistics;
	}
   /**
	 * this methode for staring the xml parse and initialises all the values. 	 
	 * @throws java.lang.Exception the exception during the parse.
	 */
	public void logReaderStart() throws Exception {
		InputSource inputSource = null;
		XMLReader xmlReader = null;
		if (fileName.endsWith(XMLFileLogger.FILENAME_EXTENSION)) {
			/** 
			 *Create a JAXP SAXParserFactory and configure it
			 */
			SAXParserFactory spf = SAXParserFactory.newInstance();

			/** 
			 * Set namespaceAware to true to get a parser that corresponds to
			 * the default SAX2 namespace feature setting.  This is necessary
			 * because the default value from JAXP 1.0 was defined to be false.
			 */
			spf.setNamespaceAware(true);

			/** 
			 * Create a JAXP SAXParser
			 * 
			 */
			SAXParser saxParser = spf.newSAXParser();

			/** 
			 * Get the encapsulated SAX XMLReader
			 * 
			 */
			xmlReader = saxParser.getXMLReader();
			inputSource = new InputSource(convertToFileURL(this.fileName));
		} else if (fileName.endsWith(CompactFileLogger.FILENAME_EXTENSION)) {
			// create a .compact file reader which generates SAX reader events
			xmlReader = new CompactLogReader();
			// create an InputSource for the choosen .compact history file
			inputSource = new CompactLogInputSource(fileName, CompactLogInputSource.STATISTICS, null); 
		} else {
			throw new IllegalArgumentException("can process " + XMLFileLogger.FILENAME_EXTENSION 
					+ " and " + CompactFileLogger.FILENAME_EXTENSION + " files only");
		}


        /** 
        * Set the ContentHandler of the XMLReader
         * 
         */
        xmlReader.setContentHandler(this);

        /** 
        * Set an ErrorHandler before parsing
         * 
         */
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        /** 
        * create a DocumentBuilderFactory and configure it
         * 
         */
        //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Set namespaceAware to true to get a DOM Level 2 tree with nodes
        // containing namesapce information.  This is necessary because the
        // default value from JAXP 1.0 was defined to be false.
        // dbf.setNamespaceAware(true);
        /**
        * parse works from now
         */
        xmlReader.parse(inputSource);
	}
	
	
	
	/** 
	 * an interger for test.
	 * we can test here for example how many elements in log file
	 */
    private int count = 0;

    /**
     * the method to create an unique id by simply increment
     * a counter and return a string with this counter.
      * 
      *  
      */
    public String getUniqueID() {
    	return String.valueOf(count);
    }

    /** 
     * Parser calls this once at the beginning of a document. 
     */
    @Override
	public void startDocument() throws SAXException {
    }

    /** 
     * Parser calls this for each element in a document.
     */
    @Override
	public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
	throws SAXException {
    	count++;
    	if (localName.equals("senderid") && flag) {
			flagChac = true;
		}
    	if (localName.equals("value") && flag) {
			// System.out.println( atts.getQName(0)+"="+ atts.getValue(0));
			st.yValue = atts.getValue("y");
			st.xValue = atts.getValue("x");
		}
    	if (localName.equals("axes") && flag) {
			// System.out.println( atts.getQName(0)+"="+ atts.getValue(0));
			st.yAxes = atts.getValue("y");
			st.xAxes = atts.getValue("x");
		}
    	if (localName.equals("statistic")) {
    		flag = true;
    		st = new StatisticDataItem();
    	}
   	
    }

    /** 
     * Parser calls this for each element when reaching its end. 
     */
    @Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	if (localName.equals("statistic")) {
    		flag = false;
    		//stVector.add(st);
    		Integer id = st.getSenderId();
    		Vector<StatisticDataItem> v = this.subStatistics.get(id);
    		if (v == null) {
    		      v = new Vector<StatisticDataItem>();
    		      v.add(st);
    		      this.subStatistics.put(id, v);
    		  } else {
    		      v.add(st);
    		  }
    		 
    	}
    	if (localName.equals("senderid")) {
			flagChac = false;
		}
    }
    
    /**
     * Parser calls this once after parsing a document.
     */ 
    @Override
	public void endDocument() throws SAXException {
    }
    
    /**
     * Parser reports regular character here,
     * that means this methode will be called by every date the simulator returned! 
     * @param ch the chararcter array
     * @param start is the start index of char[] 
     * @param length is the length of char[] 
     */ 
    @Override
	public void characters(char[] ch, int start, int length) {
    	if (flagChac) {
			st.SenderId = new String(ch, start, length);
		}
    }
   
    /**
     * Convert from a filename to a file URL.
     * @param filename is the input filepath plus file name
     * @return the file path will be returned
     */
    private static String convertToFileURL(String filename) {
        // On JDK 1.2 and later, simplify this to:
        // "path = file.toURL().toString()".
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
    
    /**
     * failure will be reported 
     *  use later
     */
    /*private static void usage() {
        System.err.println("Usage: java SAXTransform <file.xml>");
        System.exit(1);
    }*/
    
    /**
     * @author yuanzhi
     *  the ErrorHandler just report the exception
     */
    private static class MyErrorHandler implements ErrorHandler {
        /** 
         * Error handler output goes here. 
         */
        private final PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details.
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId 
            + " Line=" + spe.getLineNumber() 
            + ": " + spe.getMessage();
            return info;
        }

        /**
         * The following methods are standard SAX ErrorHandler methods.
         */ 
        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }

}

