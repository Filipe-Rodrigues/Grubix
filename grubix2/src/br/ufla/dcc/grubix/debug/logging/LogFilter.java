package br.ufla.dcc.grubix.debug.logging;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * The purpose of this class is to maintain a filter for the logging process. It is used for live logging during a
 * ShoX Simulation run, as well as for filtering events to display in the LogViewer.
 * 
 * @author mika
 *
 */
public class LogFilter {
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LogFilter.class);
	/**
	 * Textual representation of log levels applicable for layers.
	 */
	public static final String[] PRIORITIES = {"info", "normal", "high", "off"};
	/**
	 * Layer logging level INFO. Highest level, all events will be logged.
	 */
	public static final int PRIORITY_INFO = 0;
	/**
	 * Layer logging level NORMAL. Moderate level, most events will be logged.
	 */
	public static final int PRIORITY_NORMAL = 1;
	/**
	 * Layer logging level HIGH. Restrictive level, Highly important events will be logged. 
	 */
	public static final int PRIORITY_HIGH = 2;
	/**
	 * Layer logging level OFF. No logging is done at this level.
	 */
	public static final int PRIORITY_OFF = 3;
	/**
	 * Stores class names of events which are to log, together with their priority.
	 */
	private HashMap<String, Integer> acceptedEventTypes = null;	

	/**
	 * Description of the current filter.
	 */
	private String description = "";
	
	/** 
	 * Indicates if logging of packet-attached data is enabled. 
	 */
	private boolean logData = true;
	
	/**
	 * Tag name for this filter, as stored in configuration.xml.
	 */
	public static final String TAG_FILTER = "filter";
	/**
	 * Tag name for this filter's description, as stored in configuration.xml.
	 */
	private static final String TAG_DESCRIPTION = "description";
	/**
	 * Tag name for this filter's logdata field, as stored in configuration.xml.
	 */
	private static final String TAG_LOGDATA = "logdata";
	/**
	 * Tag name for this filter's list of events and their priorities, as stored in configuration.xml.
	 */
	private static final String TAG_ACCEPTEDTYPES = "acceptedtypes";
	/**
	 * Tag name for an event name entry, as stored in configuration.xml.
	 */
	private static final String TAG_CLASS = "class";
	/**
	 * Attribute name for an event's priority, as stored in configuration.xml.
	 */
	private static final String TAG_PRIORITY = "priority";
	
	/**
	 * Returns the priority constant which matches the passed priority name, if not match exists Integer.MIN_VALUE is
	 * returned.
	 * @param priorityName Name to lookup
	 * @return priority constant
	 */
	private int getPriorityValue(String priorityName) {	
		for (int i = 0; i < PRIORITIES.length; i++) {
			if (PRIORITIES[i].equalsIgnoreCase(priorityName)) {
				return i;
			}
		}
		return Integer.MIN_VALUE;
	}
	/**
	 * Constructor, to create a new LogFilter.
	 */
	private LogFilter() {
		acceptedEventTypes = new HashMap<String, Integer>();
	}
	
	/**
	 * Constructor, to create a new LogFilter.
	 * 
	 * @param scanForNewTypes if true the local package structure will be scanned for new Event types. If an event type
	 * is found, it will be added to this filter with PRIORITY_OFF priority
	 */
	public LogFilter(boolean scanForNewTypes) {
		this();
		if (scanForNewTypes) {
			scanForNewTypes(PRIORITY_OFF);
		}
	}
	
	/**
	 * Constructor, to create a new LogFilter based on the passed XML data.
	 * 
	 * @param filter An XML Element containing the description of this filter 
	 * @param scanForNewTypes if true the local package structure will be scanned for new Event types. If an event type
	 * is found, it will be added to this filter with PRIORITY_OFF priority
	 */
	public LogFilter(Element filter, boolean scanForNewTypes) {
		this();
		if (filter != null) {
			
			if (filter.getChildText(TAG_LOGDATA) != null) {
				this.logData = Boolean.parseBoolean(filter.getChildText(TAG_LOGDATA).toLowerCase());
			} else {
				LOGGER.fatal("logdata missing");
			}
			
			if (filter.getChildText(TAG_DESCRIPTION) != null) {
				this.description = filter.getChildText(TAG_DESCRIPTION);
			}
			
			if (scanForNewTypes) {
				scanForNewTypes(PRIORITY_OFF);
			}
			
			Element accTypesElement = filter.getChild(TAG_ACCEPTEDTYPES);
		
			if (accTypesElement != null) {
				List<Element> types = accTypesElement.getChildren(TAG_CLASS);

				if (types != null) {
					for (Element t : types) {
						String name = t.getValue();
						String prio = t.getAttributeValue(TAG_PRIORITY);
						
						if (TypeFilter.isPresent(name)) {
							acceptedEventTypes.put(name, getPriorityValue(prio));
						} else {
							LOGGER.warn(name + "is not present in classpath anymore");
							acceptedEventTypes.put(name, PRIORITY_OFF);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Scans the local package structure for ShoX Events and accepts them into this filter with the passed priority.
	 * 
	 * @param defaultPriority priority for all included ShoX Events
	 */
	private void scanForNewTypes(int defaultPriority) {
		try {
			String[] alltypes = new TypeFilter().getTypes();
			for (String t : alltypes) {
				acceptedEventTypes.put(t, defaultPriority);
			}
		} catch (URISyntaxException urise) {
			urise.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Sets a user specified description of the filter.
	 * @param newDescription String containing a user specified description of the filter
	 */
	public void setDescription(String newDescription) {
		this.description = newDescription;
	}
	
	/**
	 * Returns a user specified description of the filter.
	 * @return String containing a user specified description of the filter
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Sets the log data field.
	 * 
	 * @param enabled enable log data
	 */
	public void setLogData(boolean enabled) {
		this.logData = enabled;
	}
	
	/**
	 * Returns the log data field.
	 * @return boolean value of log data
	 */
	public boolean isLogData() {
		return this.logData;
	}
	/**
	 * Returns true is the passed Class represents an event which is to be logged.
	 * @param classType Class of event type
	 * @return true if the passed Class represents an event which is to be logged
	 */
	public boolean isAcceptedEventType(Class< ? extends Object> classType) {
		return isAcceptedEventType(classType.getName());
	}
	
	/**
	 * Returns true is the passed name represents an event which is to be logged.
	 * @param classType Name of event type
	 * @return true if the passed name represents an event which is to be logged
	 */
	public boolean isAcceptedEventType(String classType) {
		return acceptedEventTypes.containsKey(classType);
	}
	
	/**
	 * Returns the priority for the given Event class. See {@link #PRIORITY_HIGH}, {@link #PRIORITY_INFO}, 
	 * {@link #PRIORITY_NORMAL} and {@link #PRIORITY_OFF}.
	 * 
	 * @param classType Event to query
	 * @return int value priority 
	 */
	public int getEventTypePriority(String classType) {
		if (acceptedEventTypes.containsKey(classType)) {
			return acceptedEventTypes.get(classType);
		} else {
			return PRIORITY_OFF;
		}
		
	}
	
	/**
	 * Return the priority description of the given Event class. See {@link #PRIORITIES}.
	 *  
	 * @param classType Event to query
	 * @return String with priority description
	 */
	public String getEventTypePriorityString(String classType) {
		if (acceptedEventTypes.containsKey(classType)) {
			return PRIORITIES[acceptedEventTypes.get(classType)];
		} else {
			return "MISSING!";
		}
	}
	
	/**
	 * Sets the priority of the given Event class.
	 * 
	 * @param classType Event to modify
	 * @param priority The priority to be set
	 */
	public void setEventTypePriority(String classType, int priority) {
		acceptedEventTypes.put(classType, priority);
	}
	
	/**
	 * Returns an alphabetically sorted String array with all Event types.  
	 * @return an alphabetically sorted String array with all Event types.
	 */
	public String[] getAcceptedTypes() {
		LinkedList<String> typelist = new LinkedList<String>(acceptedEventTypes.keySet());
		Collections.sort(typelist);
		String[] result = new String[typelist.size()];
		int c = 0;
		for (String t : typelist) {
			result[c] = t;
			c++;
		}
		return result;
	}
	
	/**
	 * Returns an XML formatted representation of this filter.
	 * 
	 * @return XML formatted String containing a representation of this filter 
	 * @deprecated
	 */
	public String toXML() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<logfilter>\n");
		// write description
		sb.append("<description>" + description + "</description>\n");
		// write logdata
		sb.append("<logdata>" + logData + "</logdata>");
		// write accepted types
		sb.append("<acceptedtypes>\n");
		if (acceptedEventTypes != null) { 
			Set<String> keys = acceptedEventTypes.keySet();
			for (String key : keys) {
				sb.append("<class priority=\"" + acceptedEventTypes.get(key) + "\">" + key + "</class>\n");
			}
		}
		sb.append("</acceptedtypes>\n");
		
		sb.append("</logfilter>");
		
		return sb.toString();
	}
	
	/**
	 * Returns the <code><filter></code> Element representing the log filter, used to save the configuration.
	 * 
	 * @return Element representing the setup of this filter
	 */
	public Element getFilterElement() {
		Element result = new Element(TAG_FILTER);
		
		Element desc = new Element(TAG_DESCRIPTION);
		desc.setText(this.description);
		result.addContent(desc);
		
		Element logdata = new Element(TAG_LOGDATA);
		logdata.setText("" + this.logData);
		result.addContent(logdata);
		
		Element accepted = new Element(TAG_ACCEPTEDTYPES);
		if (acceptedEventTypes != null) {
		    Set<String> keys = acceptedEventTypes.keySet();
			// sort output by event type name
			LinkedList<String> sortedKeyList = new LinkedList<String>(keys);
			Collections.sort(sortedKeyList);
			for (String key : sortedKeyList) {
				// only save event types which are actually set for logging
				if (getEventTypePriority(key) != PRIORITY_OFF) {
					Element type = new Element(TAG_CLASS);
					type.setText(key);
					type.setAttribute(TAG_PRIORITY, "" + getEventTypePriorityString(key));
					accepted.addContent(type);
				}
			}
		}
		result.addContent(accepted);
		
		return result;
	}
}
