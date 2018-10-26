package br.ufla.dcc.grubix.debug.compactlogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;


import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.xml.XMLTags;


/**
 * The purpose of the CompactLogReader is to read a .compact history or statistics file. Passing the file's
 * contents to ShoX works like with any other XMLReader which parses an XML formatted file. This enables already
 * existing ShoX tools like the "statistics visualizer" and "monitor" to work.
 * 
 * @author mika
 *
 */
public class CompactLogReader implements XMLReader, ContentHandler {
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CompactLogReader.class);
	/**
	 * Index for class names. Class name is the key, their index the value (@see CompactFileLogger.typeMapCounter).
	 */
	private static HashMap<String, Integer> typeMap = new HashMap<String, Integer>();
	/**
	 * Reversed -> typeMap.
	 */
	private static HashMap<Integer, String> reversedTypeMap = new HashMap<Integer, String>();
	/**
	 * Set {@link ContentHandler} to be notified of any XML event.
	 */
	private ContentHandler contentHandler = null;
	/**
	 * unused.
	 */
	private DTDHandler dtdHandler = null;
	/**
	 * unused.
	 */
	private EntityResolver entityResolver = null;
	/**
	 * unused.
	 */
	private ErrorHandler errorHandler = null;
	/**
	 * Linefeed character used in logfiles.
	 */
	private static final char[] LINEFEED = {'\n'};
	/**
	 * Empty XML attributes list constant.
	 */
	private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
	
	/**
	 * {@inheritDoc}
	 */
	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public DTDHandler getDTDHandler() {
		return dtdHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	/**
	 * {@inheritDoc}
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return null;
	}
	
	/**
	 * Parses a compact log file and creates SAX Reader XML calls like a {@link XMLReader}. 
	 * <p>
	 * Content of the log file is specified together with the location in the passed 
	 * {@link CompactLogInputSource} parameter.  
	 * 
	 * @param input description of the location of the compact log file
	 * @throws IOException on error during file handling
	 * @throws SAXException on error during parsing
	 */
	public void parse(CompactLogInputSource input) throws IOException, SAXException {
		
		BufferedReader reader = new BufferedReader(input.getCharacterStream());
		String line = null;
		int lineCounter = 0;
		String[] tokens; 
		
		if (input.isHistory()) {
			typeMap = CompactFileLogger.readTypeMapByFilename(input.getHistoryFilenamePrefix());
			reversedTypeMap = CompactFileLogger.reverseHashMap(typeMap);
		}
		
		contentHandler.startDocument();
		
		if (input.isHistory()) {		
			contentHandler.startElement("", XMLTags.GLOBAL, XMLTags.GLOBAL, EMPTY_ATTRIBUTES);
		} else if (input.isStatistics()) {
			contentHandler.startElement("", XMLTags.STATISTICS, XMLTags.STATISTICS, EMPTY_ATTRIBUTES);
		}
		lf();
		
		while ((line = reader.readLine()) != null) {
			lineCounter++;
					
			if (line.length() > 0) {
				if (line.charAt(0) != '#') {	

					tokens = line.split(" ");
				
					if (tokens != null && tokens.length > 0) {
						// linkstate event
						if (tokens[0].equalsIgnoreCase(CompactFileLogger.LINKSTATE_EVENT_CODE)) {
							handleLinkState(tokens);
						} else if (tokens[0].equalsIgnoreCase(CompactFileLogger.DEQUEUE_EVENT_CODE)) {
							handleDequeue(tokens);
						} else if (tokens[0].equalsIgnoreCase(CompactFileLogger.MOVE_EVENT_CODE)) {
							handleMove(tokens);
						} else if (tokens[0].equalsIgnoreCase(CompactFileLogger.NODESTATE_EVENT_CODE)) {
							handleNodeState(tokens);							
						} else if (tokens[0].equalsIgnoreCase(CompactFileLogger.ENQUEUE_EVENT_CODE)) {
							handleEnqueue(tokens);
						} else if (tokens[0].equalsIgnoreCase(CompactFileLogger.TEXT_MESSAGE)) {
							handleTextMessage(tokens);
						} else if (tokens[0].equalsIgnoreCase(CompactFileLogger.CONFIGURATION_SECTION)) {
							String config = tokens[1]; // token is masked UTF-8
							config = CompactFileLogger.unmask(config); // unmask to get original text
							
							// let XMLReader generate the SAX events, we just forward them							
							XMLReader cRead = XMLReaderFactory.createXMLReader();
							cRead.setContentHandler(this);
							cRead.parse(new InputSource(new StringReader(config)));
							
							contentHandler.startElement("", XMLTags.SIMULATION, XMLTags.SIMULATION, EMPTY_ATTRIBUTES);
							lf();
						} else if (tokens[0].equalsIgnoreCase(CompactFileLogger.STATISTICS_ENTRY)) {
							handleStatistc(tokens);
						}
					}
				} 				
			}
		}
		
		if (input.isHistory()) {
			contentHandler.endElement("", XMLTags.SIMULATION, XMLTags.SIMULATION);
			lf();
			contentHandler.endElement("", XMLTags.GLOBAL, XMLTags.GLOBAL);
			lf();
		} else if (input.isStatistics()) {
			contentHandler.endElement("", XMLTags.STATISTICS, XMLTags.STATISTICS);
			lf();
		}

		contentHandler.endDocument();
		LOGGER.debug(lineCounter + " lines read");
	}
	
	/**
	 * Emits all XML events needed to report an enqueue tag.
	 * @param tokens enqueue entry
	 * @throws SAXException on communication with content handler
	 */
	private void handleEnqueue(String[] tokens) throws SAXException {
//		<enqueue>
//		<time>float</time>
//		<type>loggable.getEventType() classname</type>
//		<id>int</id>
//		<receiverid>int</receiverid>
//		Delegate to loggable.log(), classes are: NodeInternalEnvelope NodeInternalToDeviceEnvelope
//		</enqueue>
		
//		log.format("%s t%s id%d rId%d t%d pr%d %s\n", ENQUEUE_EVENT_CODE, 
//	       			   String.valueOf(eventEnvelope.getTime().doubleValue()),
		   			   String time = getString("t", tokens[1], false);
//           			   lgabl.getEventId().asInt(), 
		   			       String id = getString("id", tokens[2], false);
//                				receiver.asInt(), 
		   			            String recId = getString("rId", tokens[3], false);
//                      			  typeEntry,
		   			                  String typeStr = getString("t", tokens[4], false);
		   			                  int type = Integer.parseInt(typeStr);
		   			                  String typeName = reversedTypeMap.get(type);
		   			                      String prioStr = getString("pr", tokens[5], false);
//                           			  	   lgabl.log(Loggable.COMPACT));
		   			                  	  	   String internal = tokens[6];

		AttributesImpl attr = new AttributesImpl();
		attr.addAttribute("", "priority", "priority", "String", prioStr);
		
		contentHandler.startElement("", XMLTags.ENQUEUEEVENT, XMLTags.ENQUEUEEVENT, attr);
		lf();
		
		emitString(time, XMLTags.TIMESTAMP);
		lf();
		emitString(typeName, XMLTags.TYPE);
		lf();
		emitString(id, XMLTags.ID);
		lf();
		emitString(recId, XMLTags.RECEIVERID);
		lf();
		
		int fieldSize = tokens.length - 7;
		String[] leftTokens = new String[fieldSize];
		System.arraycopy(tokens, 7, leftTokens, 0, fieldSize);
		
		if (internal.equalsIgnoreCase("toDev")) {
			// handle SimulationManager.NodeInternalToDeviceEnvelope log
			handleToDevice(leftTokens);
		} else if (internal.equalsIgnoreCase("intEnv")) {
			// handle SimulationManager.NodeInternalEnvelope log
			handleToLayer(leftTokens);
		} else {
			System.err.println("handleEnqueue wrong Internal Code");
		}
		
		contentHandler.endElement("", XMLTags.ENQUEUEEVENT, XMLTags.ENQUEUEEVENT);
		lf();
	}
	
	/**
	 * Emits all XML events needed to report an enbedded tolayer tag.
	 * @param tokens tolayer embedded entry
	 * @throws SAXException on communication with content handler
	 */
	private void handleToLayer(String[] tokens) throws SAXException {
		contentHandler.startElement("", XMLTags.TOLAYER, XMLTags.TOLAYER, EMPTY_ATTRIBUTES);			
		lf();

		String senderId = getString("sId", tokens[0], false);
		String senderLayerCode = getString("sLy", tokens[1], false);
		LayerType senderLayerType = CompactFileLogger.getLayerType(senderLayerCode);
		String senderLayer = senderLayerType.toString();
		String recId = getString("rId", tokens[2], false);
		String recLayerCode = getString("rLy", tokens[3], false);
		LayerType recLayerType = CompactFileLogger.getLayerType(recLayerCode);
		String recLayer = recLayerType.toString();
		String enclosedId = null;
		String data = null;
		if (tokens.length >= 5) {
			if (tokens[4].startsWith("eId")) {
				enclosedId = getString("eId", tokens[4], false);
			} else if (tokens[4].startsWith("data")) {
				data = getString("data", tokens[4], true);
			}
		}
		if (tokens.length == 6) {
			if (tokens[5].startsWith("eId")) {
				enclosedId = getString("eId", tokens[5], false);
			} else if (tokens[5].startsWith("data")) {
				data = getString("data", tokens[5], true);
			}
		}
		
		emitString(senderId, XMLTags.SENDERID);
		lf();
		emitString(senderLayer, XMLTags.SENDERLAYER);
		lf();
		emitString(recId, XMLTags.ENVELOPERECEIVERID);
		lf();
		emitString(recLayer, XMLTags.ENVELOPERECEIVERLAYER);
		lf();
		
		if (enclosedId != null) {
			emitString(enclosedId, XMLTags.ENCLOSEDEVENTID);
			lf();
		}
		
		if (data != null) {
			emitString(data, XMLTags.DATA);
			lf();
		}
		
		contentHandler.endElement("", XMLTags.TOLAYER, XMLTags.TOLAYER);
		lf();
	}
	
	/**
	 * Emits all XML events needed to report an embedded todevice tag.
	 * @param tokens todevice embedded entry
	 * @throws SAXException on communication with content handler
	 */
	private void handleToDevice(String[] tokens) throws SAXException {
		
		if (tokens.length == 4 || tokens.length == 5) {
			String senderId = getString("sID", tokens[0], false);
			String senderDev = getString("sDv", tokens[1], false);
			String recId = getString("rID", tokens[2], false);
			String recDev = getString("rDv", tokens[3], false);
						
			contentHandler.startElement("", XMLTags.TODEVICE, XMLTags.TODEVICE, EMPTY_ATTRIBUTES);			
			lf();
				emitString(senderId, XMLTags.SENDERID);
				lf();
				emitString(senderDev, XMLTags.SENDERDEVICE);
				lf();
				emitString(recId, XMLTags.ENVELOPERECEIVERID);
				lf();
				emitString(recDev, XMLTags.ENVELOPERECEIVERDEVICE);
				lf();
				
				if (tokens.length == 5) {
					String data = getString("data", tokens[4], true);
					emitString(data, XMLTags.DATA);
				}
			contentHandler.endElement("", XMLTags.TODEVICE, XMLTags.TODEVICE);
			lf();
		}
		
	}
	
	/**
	 * Emits all XML events needed to report a nodestate tag.
	 * @param tokens nodestate entry
	 * @throws SAXException on communication with content handler
	 */
	private void handleNodeState(String[] tokens) throws SAXException {		
		if (tokens.length == 5) {
			AttributesImpl attr = new AttributesImpl();
			
			String id = getString("id", tokens[1], false);
			attr.addAttribute("", "id", "id", "int", id);
			
			String name = getString("n", tokens[2], true);
			attr.addAttribute("", "name", "name", "String", name);
			
			int typeKey = getInt("tp", tokens[3]); 
			String typeName = reversedTypeMap.get(typeKey);
			attr.addAttribute("", "type", "type", "String", typeName);
			
			String value = getString("v", tokens[4], true);
			attr.addAttribute("", "value", "value", "String", value);
			
			contentHandler.startElement("", XMLTags.NODESTATE, XMLTags.NODESTATE, attr);
			contentHandler.endElement("", XMLTags.NODESTATE, XMLTags.NODESTATE);
			lf();
		}
	}
	
	/**
	 * Emits all XML events needed to report a linkstate tag.
	 * @param tokens linkstate entry
	 * @throws SAXException on communication with content handler
	 */
	private void handleLinkState(String[] tokens) throws SAXException {		
		if (tokens.length == 6)	{
			int a = getInt("a", tokens[1]);
			int b = getInt("b", tokens[2]);
			String n = getString("n", tokens[3], false);
			int typeKey = getInt("tp", tokens[4]);
			String type = reversedTypeMap.get(typeKey);
			String val = getString("v", tokens[5], false);
			
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute("", "id1", "id1", "int", "" + a);
			attr.addAttribute("", "id2", "id2", "int", "" + b);
			attr.addAttribute("", "name", "name", "String", n);
			attr.addAttribute("", "type", "type", "String", type);
			attr.addAttribute("", "value", "value", "String", val);
			
			emitString(null, XMLTags.LINKSTATE, attr);
			lf();
		}
	}
	
	/**
	 * Emits all XML events needed to report a move tag.
	 * @param tokens move entry
	 * @throws SAXException on communication with content handler
	 */
	private void handleMove(String[] tokens) throws SAXException {		
		if (tokens.length == 6) {
			AttributesImpl attr = new AttributesImpl();
			
			String id = getString("id", tokens[1], false);
			attr.addAttribute("", "id", "id", "int", id);
			
			String x = getString("x", tokens[2], false);
			attr.addAttribute("", "x", "x", "double", x);
			
			String y = getString("y", tokens[3], false);
			attr.addAttribute("", "y", "y", "double", y);
			
			String prio = getString("pr", tokens[4], false);
			attr.addAttribute("", "priority", "priority", "int", prio);
			
			String time = getString("t", tokens[5], false);
			attr.addAttribute("", "time", "time", "double", time);
			
			emitString(null, XMLTags.MOVE, attr);
			lf();
		}
	}
	
	/**
	 * Emits all XML events needed to report a dequeue tag.
	 * @param tokens dequeue entry
	 * @throws SAXException on communication error with the attached {@link ContentHandler}
	 */
	private void handleDequeue(String[] tokens) throws SAXException {
		if (tokens.length == 3) {

			String time = getString("t", tokens[1], false);
			String id	= getString("id", tokens[2], false);

			contentHandler.startElement("", XMLTags.DEQUEUEEVENT, XMLTags.DEQUEUEEVENT, EMPTY_ATTRIBUTES);
			lf();

			emitString(time, XMLTags.TIMESTAMP);
			lf();
			emitString(id, XMLTags.ID);
			lf();

			contentHandler.endElement("", XMLTags.DEQUEUEEVENT, XMLTags.DEQUEUEEVENT);
			lf();
		}
	}
	
	/**
	 * Emits all XML events needed to report a textmessage tag.
	 * @param tokens text message entry
	 * @throws SAXException on communication error with the attached {@link ContentHandler} 
	 */
	private void handleTextMessage(String[] tokens) throws SAXException {		
		if (tokens.length == 6) {
			String time = getString("t", tokens[1], false);
			String node = getString("n", tokens[2], false);
			String layer = getString("ly", tokens[3], false);
			String prio = getString("pr", tokens[4], false);
			String msg = getString("m", tokens[5], true);
			
			contentHandler.startElement("", XMLTags.MESSAGE, XMLTags.MESSAGE, EMPTY_ATTRIBUTES);
			lf();
				emitString(time, XMLTags.TIMESTAMP);
				lf();
				emitString(prio, "priority");
				lf();
				emitString(node, "node");
				lf();
				emitString(layer, "layer");
				lf();
				emitString(msg, "text");
				lf();
			contentHandler.endElement("", XMLTags.MESSAGE, XMLTags.MESSAGE);
			lf();
		}
	}
	
	/**
	 * Emits all XML events needed to report a statstic tag.
	 * @param tokens statistic entry
	 * @throws SAXException on communication error with the attached {@link ContentHandler}
	 */
	private void handleStatistc(String[] tokens) throws SAXException {
		
		if (tokens.length == 7) {
			
			String id = getString("id", tokens[1], false);
			
			String layerCode = getString("l", tokens[2], false);
			LayerType ltype = CompactFileLogger.getLayerType(layerCode);
			String layer = ltype.toString();
			
			String axisx = getString("ax", tokens[3], true);
			String axisy = getString("ay", tokens[4], true);
	
			String valx = getString("x", tokens[5], true);
			String valy = getString("y", tokens[6], true);

			AttributesImpl attrAxis = new AttributesImpl();
			attrAxis.addAttribute("", "x", "x", "CDATA", axisx);
			attrAxis.addAttribute("", "y", "y", "CDATA", axisy);
			
			AttributesImpl attrValue = new AttributesImpl();
			attrValue.addAttribute("", "x", "x", "CDATA", valx);
			attrValue.addAttribute("", "y", "y", "CDATA", valy);
	
			
			contentHandler.startElement("", XMLTags.STATISTIC, XMLTags.STATISTIC, EMPTY_ATTRIBUTES);
			lf();
				emitString(id, XMLTags.SENDERID);
				lf();
				emitString(layer, XMLTags.SENDERLAYER);
				lf();
				emitString(null, XMLTags.AXES, attrAxis);
				lf();
				emitString(null, XMLTags.VALUE, attrValue);
				lf();
			contentHandler.endElement("", XMLTags.STATISTIC, XMLTags.STATISTIC);
			lf();
		}
	}
	
	/**
	 * Parses an int value from the passed token by stripping it off the passed prefix.
	 * 
	 * @param prefix part to strip off the token
	 * @param token token containing the prefix followed immediately by the int value
	 * @return parsed int value, throws a {@link NumberFormatException} if no int value is found
	 */
	private static int getInt(String prefix, String token) {
		token = token.replaceFirst(prefix, "");
		return Integer.parseInt(token);
	}
	
	/**
	 * Parses a {@link String} from the passed token by stripping it off the passed prefix.
	 * 
	 * @param prefix part to strip off the token
	 * @param token token containing the prefix followed immediately by the {@link String}
	 * @param unmask <code>true</true> if the String in the passed token is UTF-8 encoded and should be unmasked 
	 * @return parsed {@link String}
	 * 
	 * @see CompactFileLogger#unmask(String)
	 */
	private static String getString(String prefix, String token, boolean unmask) {
		token = token.replaceFirst(prefix, "");
		if (unmask) {
			return CompactFileLogger.unmask(token);
		} else {
			return token;
		}
	}
	
	
	/**
	 * Emits a linefeed character to the current content handler.
	 * @throws SAXException on communication error with the attached {@link ContentHandler}
	 */
	private void lf() throws SAXException {
		contentHandler.characters(LINEFEED, 0, LINEFEED.length);
	}
	
	/**
	 * Emits a sequence of XML calls representing the readback of a simple tag together with it's text.
	 * 
	 * @param characters The text enclosed in this tag
	 * @param tag Name of the tag to emit
	 * @throws SAXException on communication error with the attached {@link ContentHandler}
	 */
	private void emitString(String characters, String tag) throws SAXException {
		emitString(characters, tag, EMPTY_ATTRIBUTES);
	}
	
	/**
	 * Emits a sequence of XML calls representing the readback of a simple tag together with it's text and element 
	 * attributes.
	 * 
	 * @param characters The text enclosed in this tag
	 * @param tag Name of the tag to emit
	 * @param attributes Attributes contained in the opening tag
	 * @throws SAXException on communication error with the attached {@link ContentHandler}
	 */
	private void emitString(String characters, String tag, Attributes attributes) throws SAXException { 
		contentHandler.startElement("", tag, tag, attributes);
		if (characters != null && characters.length() != 0) {
			contentHandler.characters(characters.toCharArray(), 0, characters.length());
		}
		contentHandler.endElement("", tag, tag);
	}

	/**
	 * Using this method will always fail, as it is needed to also specify the content of the file to parse. 
	 * Use {@link #parse(CompactLogInputSource)} instead.
	 * 
	 * @param systemId location of the file
	 * @throws IOException on error while handling the XML file
	 * @throws SAXException on communication error with the attached {@link ContentHandler}
	 */
	public void parse(String systemId) throws IOException, SAXException {
		throw new SAXException("need to specify log content via CompactLogInputSource");
	}
	
	/**
	 * Using this method will always fail, as it is needed to also specify the content of the file to parse. 
	 * Use {@link #parse(CompactLogInputSource)} instead.
	 * 
	 * @param input description of the source of the XML item to parse
	 * @throws IOException on error while handling the XML file, or source
	 * @throws SAXException on communication error with the attached {@link ContentHandler}
	 */
	public void parse(InputSource input) throws IOException, SAXException {
		if (input instanceof CompactLogInputSource) {
			parse((CompactLogInputSource) input);
		} else {
			throw new SAXException("need to specify log format via CompactLogInputSource");
		}		
	}

	/**
	 * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.ContentHandler)
	 * {@inheritDoc}
	 */
	public void setContentHandler(ContentHandler handler) {
		contentHandler = handler;
	}

	/**
	 * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.ContentHandler)
	 * {@inheritDoc}
	 */
	public void setDTDHandler(DTDHandler handler) {
		dtdHandler = handler;
	}

	/**
	 * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.ContentHandler)
	 * {@inheritDoc}
	 */
	public void setEntityResolver(EntityResolver resolver) {
		entityResolver = resolver;
	}

	/**
	 * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ContentHandler)
	 * {@inheritDoc}
	 */
	public void setErrorHandler(ErrorHandler handler) {
		errorHandler = handler;
	}

	/**
	 * @see org.xml.sax.XMLReader#setFeature(org.xml.sax.ContentHandler)
	 * {@inheritDoc}
	 */
	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException();
	}

	/**
	 * @see org.xml.sax.XMLReader#setProperty(org.xml.sax.ContentHandler)
	 * {@inheritDoc}
	 */
	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException();
	}

	// -------------------- ContentHandler -----------------------------------------------------------------
	/*
	 * used to read configuration part from compactlog, simply parse xml in memory and forwards all events
	 */
	/**
	 * {@inheritDoc}
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.characters(ch, start, length);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public void endDocument() throws SAXException {
		// do not forward endDocument.		 
	}
	/**
	 * {@inheritDoc}
	 */
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.endElement(uri, localName, name);
			lf();
		}
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.endPrefixMapping(prefix);
		}
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.ignorableWhitespace(ch, start, length);
		}
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void processingInstruction(String target, String data) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.processingInstruction(target, data);
		}		
	}
	/**
	 * {@inheritDoc}
	 */
	public void setDocumentLocator(Locator locator) {
//		if (this.contentHandler != null) {
//			this.contentHandler.setDocumentLocator(locator);
//		}		
	}
	/**
	 * {@inheritDoc}
	 */
	public void skippedEntity(String name) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.skippedEntity(name);
		}		
	}
	/**
	 * {@inheritDoc}
	 */
	public void startDocument() throws SAXException {
		/*
		 * do not forward startDocument.
		 */
	}
	/**
	 * {@inheritDoc}
	 */
	public void startElement(String uri, String localName, String name,	Attributes atts) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.startElement(uri, localName, name, atts);
			// should linefeed only after:
			// configuration, field, nodes, layers, positions, position
			if (name.endsWith(XMLTags.CONFIGURATION) 
					|| name.equals(XMLTags.FIELD)
					|| name.equals(XMLTags.NODELIST)
					|| name.equals(XMLTags.LAYERS)
					|| name.equals(XMLTags.POSITIONLIST)
					|| name.equals(XMLTags.POSITION)) {
				this.contentHandler.characters(LINEFEED, 0, LINEFEED.length);
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (this.contentHandler != null) {
			this.contentHandler.startPrefixMapping(prefix, uri);
		}
	}
}

