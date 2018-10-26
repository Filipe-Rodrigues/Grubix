package br.ufla.dcc.grubix.xml;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is the main handler for parsing the XML log file that ShoX produces. It delegates
 * all SAX calls to its registered sub-handlers. This way, an arbitrary number of independent 
 * handlers can be used to process the log file, each potentially with its own purpose.
 * @author jlsx
 */
public final class LogHandlerManager extends DefaultHandler {

	/**
	 * Logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LogHandlerManager.class.getName());

	/**
	 * List of all XML log file handler. Each handler gets notified of all
	 * SAX events and can then decide what to do (chain of responsibility).
	 */
	private ArrayList<DefaultHandler> handlers;
	
	/**
	 * Constructor.
	 */
	public LogHandlerManager() {
		handlers = new ArrayList<DefaultHandler>();
	}
	
	/**
	 * add a Handler for the reader.
	 * @param handler : DefaultHandler
	 */
	public void addHandler(DefaultHandler handler) {
		this.handlers.add(handler);
	}
	
	/**
	 * remove a handler from the reader.
	 * @param handler : DefaultHandler
	 */
	public void removeHandler(DefaultHandler handler) {
		this.handlers.remove(handler);
	}
	
	/**
	 * create a new startDocument event.
	 */
	public void startDocument() {
		for (DefaultHandler handler : this.handlers) {
			try {
				handler.startDocument();
			} catch (SAXException sae) {
				LOGGER.error("startDocument produced an exception" + sae);
			}
		}
	}
	
	/**
	 * create a new startElement event of Handler.
	 * @param namespace : String
	 * @param localname : String
	 * @param qname : String
	 * @param atts : Attributes
	 */
	public void startElement(String namespace, String localname, String qname, Attributes atts) {
		for (DefaultHandler handler : this.handlers) {
			try {
				handler.startElement(namespace, localname, qname, atts);
			} catch (SAXException sae) {
				LOGGER.error("startElement produced an exception" + sae);
			}
		}		
	}
	
	/**
	 * create a new characters event of Handler.
	 * @param wert : char[]
	 * @param start : integer
	 * @param length : length
	 */
	public void characters(char[] wert, int start, int length) {
		for (DefaultHandler handler : this.handlers) {
			try {
				handler.characters(wert, start, length);
			} catch (SAXException sae) {
				LOGGER.error("characters produced an exception" + sae);
			}
		}
	}
	
	/**
	 * create a new endElement event of Handler.
	 * @param namespace : String
	 * @param localname : String
	 * @param qname : String
	 */
	public void endElement(String namespace, String localname, String qname) {
		for (DefaultHandler handler : this.handlers) {
			try {
				handler.endElement(namespace, localname, qname);
			} catch (SAXException sae) {
				LOGGER.error("endElement produced an exception" + sae);
			}
		}
	}
	
	/**
	 * create a new endDocument event.
	 */
	public void endDocument() {
		for (DefaultHandler handler : this.handlers) {
			try {
				handler.endDocument();
			} catch (SAXException sae) {
				LOGGER.error("endElement produced an exception" + sae);
			}
		}
	}

}
