package br.ufla.dcc.grubix.debug.compactlogging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Loggable;
import br.ufla.dcc.grubix.simulator.kernel.EventEnvelope;
import br.ufla.dcc.grubix.simulator.movement.MovementManager;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.physical.PhysicalModel;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.XMLTags;



/**
 * This is the central logging class for the Shox system.
 * It generates a compact history and statistics file. 
 * 
 * @author mika
 *
 */
public class CompactFileLogger implements ShoxLogger {
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CompactFileLogger.class);
	/**
	 * Writer for the file used for history output.
	 */
	private PrintWriter hist;
	/**
	 * Writer for the file used for statistics output.
	 */
	private PrintWriter statistics;
	/**
	 * Identification of the simulator using this logger.
	 */
	private String simulatorId; 
	/**
	 * Filehandle for the file used for history output.
	 */
	private File histFile;
	/**
	 * Filehandle for the file used for statistics output.
	 */
	private File statFile;
	/**
	 * Extension of log filenames created by this logger. 
	 */
	public static final String FILENAME_EXTENSION = ".compact";
	/**
	 * Name of map file which stores the mapping of event type names to their keys.
	 */
	private String mapFilename;
	/**
	 * Extension of the mapfile filename.
	 */
	private static final String MAPFILE_EXTENSION = ".map";
	/**
	 * Seperator used to seperate key and values in the mapfile.
	 */
	private static final String MAPFILE_SEPERATOR = ";";
	
	/**
	 * Three letter code representing the AirModule.
	 */
	public static final String LAYER_CODE_AIRMODULE 	= "AIR";
	/**
	 * Three letter code representing the application layer.
	 */
	public static final String LAYER_CODE_OPERATINGSYSTEM = "OPS";
	/**
	 * Three letter code representing the application layer.
	 */
	public static final String LAYER_CODE_APPLICATION 	= "APP";
	/**
	 * Three letter code representing the logical link layer.
	 */
	public static final String LAYER_CODE_LOGLINK 		= "LOG";
	/**
	 * Three letter code representing the MAC layer.
	 */
	public static final String LAYER_CODE_MAC 			= "MAC";
	/**
	 * Three letter code representing the network layer.
	 */
	public static final String LAYER_CODE_NETWORK 		= "NET";
	/**
	 * Three letter code representing the physical layer.
	 */
	public static final  String LAYER_CODE_PHYSICAL 	= "PHY";
	
	/**
	 * Log line marker to identify an Dequeue event.
	 */
	public static final String DEQUEUE_EVENT_CODE	= "dq";
	/**
	 * Log line marker to identify an Enqueue event.
	 */
	public static final String ENQUEUE_EVENT_CODE	= "eq";
	/**
	 * Log line marker to identify an Move event.
	 */
	public static final String MOVE_EVENT_CODE		= "mv";
	/**
	 * Log line marker to identify an NodeState event.
	 */
	public static final String NODESTATE_EVENT_CODE	= "ns";
	/**
	 * Log line marker to identify an LinkState event.
	 */
	public static final String LINKSTATE_EVENT_CODE	= "ls";
	/**
	 * Log line marker to identify the simulation configuration part of the hist.
	 */
	public static final String CONFIGURATION_SECTION = "cf";
	/**
	 * Log line marker to identify a text message.
	 */
	public static final String TEXT_MESSAGE = "ms";
	/**
	 * Log line marker to identify a remark entry.
	 */
	public static final String REMARK_SECTION = "##";
	/**
	 * Log line marker to identify a statistics entry.
	 */
	public static final String STATISTICS_ENTRY = "st";

	/**
	 * Denotes if file write position is within the configuration tag in the statistics file.
	 */
	private boolean inConfiguration = false;
	
	/**
	 * Denotes if file write position is beyond the configuration tag in the statistics file.
	 */
	private boolean doneConfiguration = false;
	
	/**
	 * Denotes if output files containing the logging output were specified and are ready for logging.
	 */
	private boolean initComplete = false;
	
	/**
	 * Holds the content of the configuration tag.
	 */
	private static StringBuffer configurationPart = new StringBuffer();
	
	/**
	 * Index for class names. Class name is the key, their index the value (@see CompactFileLogger.typeMapCounter).
	 */
	private static HashMap<String, Integer> typeMap = new HashMap<String, Integer>();
	/**
	 * Representative of a class name.
	 */
	private static int typeMapCounter = 0;
	
	/**
	 * Closes the history and statistic file upon end of logging.
	 */
	private void closeFiles() {
		if (hist != null) {
			hist.flush();
			hist.close();
		}
		if (statistics != null) {
			statistics.flush();
			statistics.close();
		}
	}
	
	/**
	 * This method masks the passed input into the UTF-8 format.
	 * @param input String to mask
	 * @return input String masked to UTF-8 format
	 */
	public static String mask(String input) {
		try {
			return URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();
		}
		return "mask-error";
	}
	
	/**
	 * This method converts the passed UTF-8 encoded String into readable presentation.
	 * @param input an UTF-8 encoded String
	 * @return readable presentation of input
	 */
	public static String unmask(String input) {
		try {
			return URLDecoder.decode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "unmask-error";
	}
	
	/**
	 * Writes the mapfile which was generated during the simulation run to file. The CompactFileLogger replaces all
	 * Event type names with an unique key to save space. The mapping of those Event type names to their keys is stored
	 * in the mapfile. The mapfile is written when #finsihLogging() is called.
	 * 
	 * @param map HashMap containing the Event type names to their keys mapping
	 * @param simId ID of Simulator which used the ShoX logger
	 * @param filename filename for the typemap file
	 * @return <true> if write operation was successful
	 */
	static boolean writeTypeMap(HashMap<String, Integer> map, String simId, String filename) {
		try {
			PrintWriter mapfile = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			mapfile.println(REMARK_SECTION + " simulator " + simId);
			mapfile.println(REMARK_SECTION + " created " + Calendar.getInstance().getTime());
			for (String i : map.keySet()) {
				mapfile.println(i + MAPFILE_SEPERATOR + map.get(i));
			}
			mapfile.flush();
			mapfile.close();
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();			
		}
		return false;
	}
	
	/**
	 * Reads a .map typemap which holds the mapping of type names to their index value. 
	 * Such a map is created together with a history compact log file. It's filename is equal to the one of the log 
	 * file, except it's extension. 
	 * 
	 * @param mapname name of the mapfile minus the file extension (equals to name of compact log file) 
	 * @return a HashMap holding the typename to index mapping
	 */
	static HashMap<String, Integer> readTypeMapByFilename(String mapname) {
		 HashMap<String, Integer> result = new HashMap<String, Integer>();
		 try {
			 BufferedReader reader = new BufferedReader(new FileReader(mapname + MAPFILE_EXTENSION));
			 String line = null;
			 while ((line = reader.readLine()) != null) {
				 if (!line.startsWith(REMARK_SECTION)) {
					 String[] items = line.split(MAPFILE_SEPERATOR);
					 if (items != null && items.length == 2) {
						 String key = items[0];
						 String val = items[1];
						 result.put(key, Integer.parseInt(val));
					 }
				 }
			 }
			 reader.close();
			 return result;
		 } catch (FileNotFoundException fnfe) {
			 fnfe.printStackTrace();		 
		 } catch (IOException ioe) {
			 ioe.printStackTrace();		 
		 } 
		 return null;
	}
	
	/**
	 * Inverses the matching of keys to values in the passed HashMap. Assumes each key and value exist exaclty once.
	 * 
	 * @param old {@link HashMap} to inverse
	 * @return the inversed {@link HashMap}
	 */
	static HashMap<Integer, String> reverseHashMap(HashMap<String, Integer> old) {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		for (String key : old.keySet()) {
			Integer value = old.get(key);
			result.put(value, key);
		}
		return result;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void logDequeueEvent(double time, EventId event) {
		// DEQUEUE:= dq tDOUBLE idINT
		
		if (hist != null) {
			if (doneConfiguration) {
				hist.format("%s t%s id%d\n", DEQUEUE_EVENT_CODE, String.valueOf(time), event.asInt());
			}
		}
	}
	
	/**
	 * {@inheritDoc} 
	 */
	public void logMoveEvent(NodeId node, double x, double y, double time, int priority) {
		if (hist != null) {
			if (doneConfiguration) {
				hist.format("%s id%d x%s y%s pr%d t%s\n", MOVE_EVENT_CODE, 
				        node.asInt(), 
				             String.valueOf(x), 
				                 String.valueOf(y),
				                 	priority,
				                     String.valueOf(time));
			}
		}		
	}
	
	
	/** 
	 * {@inheritDoc} 
	 */
	public void logStatistics(NodeId node, LayerType layer, String xAxisLabel, String yAxisLabel, 
			String xValue, String yValue) {
		if (statistics != null) {
			if (initComplete) {	
				statistics.format("%s id%d l%s ax%s ay%s x%s y%s\n", STATISTICS_ENTRY,
						              node.asInt(), 
						              	   getLayerCode(layer), 
						              	   	   mask(xAxisLabel), 
						              	   	   		mask(yAxisLabel), 
						              	   	   			 mask(xValue), 
						              	   	   			 	 mask(yValue));
			}
		}
	}
	
	/**
	 * Returns a three letter code for the passed {@link LayerType}.
	 * 
	 * @param layerType {@link LayerType} for which the code should be retrieved
	 * @return a three letter code, null on unrecognized parameter
	 * 
	 * @see #getLayerType(String)
	 */
	public static String getLayerCode(LayerType layerType)	{
		if (layerType == LayerType.AIR) {
			return LAYER_CODE_AIRMODULE;
		} else if (layerType == LayerType.APPLICATION) {
			return LAYER_CODE_APPLICATION;
		} else if (layerType == LayerType.LOGLINK) {
			return LAYER_CODE_LOGLINK;
		} else if (layerType == LayerType.MAC) {
			return LAYER_CODE_MAC;
		} else if (layerType == LayerType.NETWORK) {
			return LAYER_CODE_NETWORK;
		} else if (layerType == LayerType.PHYSICAL)	{
			return LAYER_CODE_PHYSICAL;
		} else if (layerType == LayerType.OPERATINGSYSTEM) {
			return LAYER_CODE_OPERATINGSYSTEM;		
		} else {			
			return null;
		}
	}
	
	/**
	 * Returns the {@link LayerType} from the passed three letter code.
	 * 
	 * @param layerCode the three letter code associated with a {@link LayerType}
	 * @return the matching {@link LayerType}, null if no matching was found
	 * 
	 * @see #getLayerCode(LayerType)
	 */
	public static LayerType getLayerType(String layerCode)	{
		if (layerCode.equalsIgnoreCase(LAYER_CODE_AIRMODULE)) {
			return LayerType.AIR;
		} else if (layerCode.equalsIgnoreCase(LAYER_CODE_APPLICATION)) {
			return LayerType.APPLICATION;
		} else if (layerCode.equalsIgnoreCase(LAYER_CODE_LOGLINK)) {
			return LayerType.LOGLINK;
		} else if (layerCode.equalsIgnoreCase(LAYER_CODE_MAC)) {
			return LayerType.MAC;
		} else if (layerCode.equalsIgnoreCase(LAYER_CODE_NETWORK)) {
			return LayerType.NETWORK;
		} else if (layerCode.equalsIgnoreCase(LAYER_CODE_PHYSICAL)) {
			return LayerType.PHYSICAL;
		} else if (layerCode.equalsIgnoreCase(LAYER_CODE_OPERATINGSYSTEM)) {
			return LayerType.OPERATINGSYSTEM;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void finishLogging() {
		if (mapFilename != null) {
			writeTypeMap(typeMap, simulatorId, mapFilename);
		}
		closeFiles();		
	}
	
	/**
	 * Returns the index of the given type as stored in the typeMap. If it is a new type which is not included in the
	 * typeMap yet, it will be added and it's new index is returned.
	 * 
	 * @param typeName type to lookup
	 * @return int representing type's index
	 */
	private int getTypeIndex(String typeName) {
		int typeEntry = Integer.MIN_VALUE;
		// typeMap [type,int %generated% ]
		
		if (typeMap.containsKey(typeName)) {
			typeEntry = typeMap.get(typeName);
		} else {
			typeMapCounter++;
			typeEntry = typeMapCounter;
			typeMap.put(typeName, typeEntry);
		}
		
		return typeEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	public void logEnqueueEvent(EventEnvelope eventEnvelope, NodeId receiver, int priority) {
		if (hist != null) {
			if (doneConfiguration) {
				
				Loggable lgabl = (Loggable) eventEnvelope;
				
				String type = lgabl.getEventType();			
				
				hist.format("%s t%s id%d rId%d t%d pr%d %s\n", ENQUEUE_EVENT_CODE, 
						       String.valueOf(eventEnvelope.getTime()), 
						           lgabl.getEventId().asInt(), 
						                receiver.asInt(), 
						                      getTypeIndex(type),
						                           priority,
						                           	    lgabl.log(Loggable.COMPACT));
				
			} else {
				LOGGER.fatal("logDequeueEvent must be after doneConfiguration");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logLinkStateEvent(NodeId node1, NodeId node2, String name,
			String type, String value) {		
		if (hist != null) {
			if (doneConfiguration) {

			hist.format("%s a%d b%d n%s tp%d v%s\n", LINKSTATE_EVENT_CODE, 
					       node1.asInt(), 
					           node2.asInt(), 
					               mask(name), 
					                   getTypeIndex(type), 
					                       mask(value));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logNodeStateEvent(NodeId node, String name, String type, String value) {
		
		if (hist != null) {
			if (doneConfiguration) {
			hist.format("%s id%d n%s tp%d v%s\n", NODESTATE_EVENT_CODE,
					        node.asInt(), 
					             mask(name), 
					                 getTypeIndex(type), 
					                      mask(value));	
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void initLogging(String historyFile, String statisticsFile, String simulatorId) throws IOException {

		this.simulatorId = simulatorId;
		
		if (historyFile != null && !historyFile.equalsIgnoreCase("")) {
			histFile = new File(historyFile);

			hist = new PrintWriter(new BufferedWriter(
					new FileWriter(histFile + this.simulatorId + FILENAME_EXTENSION)));
			hist.println(REMARK_SECTION + " ShoX logfile, created " + Calendar.getInstance().getTime());			
			inConfiguration = false;
			doneConfiguration = false;
			// create name for mapfile
			this.mapFilename = histFile + this.simulatorId + MAPFILE_EXTENSION;
		}
		
		if (statisticsFile != null && !statisticsFile.equalsIgnoreCase("")) {
			statFile = new File(statisticsFile);
			
			statistics = new PrintWriter(new BufferedWriter(
					new FileWriter(statFile + this.simulatorId + FILENAME_EXTENSION)));
			statistics.println(REMARK_SECTION + "ShoX statistics file, created " + Calendar.getInstance().getTime());
		}
		
		initComplete = true;		
	}

	// --------------------------- configuration part -----------------------------------------------------------
	
	/**
	 * {@inheritDoc}
	 */
	public void startConfigurationPart() {
		if (initComplete && !inConfiguration && !doneConfiguration) {
			configurationPart = configurationPart.append("<" + XMLTags.CONFIGURATION + ">");
			inConfiguration = true;
		} else {
			LOGGER.fatal(" startConfiguration called outside limits ");
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void endConfigurationPart() {
		if (inConfiguration && !doneConfiguration && initComplete) {
			configurationPart = configurationPart.append("</" + XMLTags.CONFIGURATION + ">");
			inConfiguration = false;
			doneConfiguration = true;
			if (hist != null) {
				hist.printf("%s %s\n", CONFIGURATION_SECTION, mask(configurationPart.toString()));
			}
		} else {
			LOGGER.fatal(" endConfiguration called outside limits ");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void logConfiguration(double fieldXSize, double fieldYSize, 
			int stepsPerSecond, long simulationTime, PhysicalModel physicalModel, 
			MovementManager movementManager, String description) {		
		if (inConfiguration) {
			configurationPart = configurationPart.append(
				"<" + XMLTags.FIELD + ">"
			+	"<" + XMLTags.X + ">" + fieldXSize + "</" + XMLTags.X + ">"
			+	"<" + XMLTags.Y + ">" + fieldYSize + "</" + XMLTags.Y + ">"
			+	"</" + XMLTags.FIELD + ">"
			+   "<" + XMLTags.SIMULATIONTIME + " " + XMLTags.STEPSPERSECOND + "=\"" + stepsPerSecond + "\""
					+ " base=\"steps\">" + simulationTime + "</" + XMLTags.SIMULATIONTIME + ">"
			+	"<" + XMLTags.PHYSMODEL + ">" + physicalModel.getClass().getName() + "</" + XMLTags.PHYSMODEL + ">"
			+	"<" + XMLTags.MOVEMENTMANAGER + ">" + movementManager.getClass().getName() 
					+ "</" + XMLTags.MOVEMENTMANAGER + ">");
		} else {
			LOGGER.fatal("logConfiguration must be inConfiguration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public 	void logNodeSetup(int nodeCount, Class<? extends Layer> physical, 
			Class<? extends Layer> mac, 
			Class<? extends Layer> loglink, 
			Class<? extends Layer> net, 
			Class<? extends Layer> app) {		
		
		if (inConfiguration) {
			configurationPart = configurationPart.append(
				"<" + XMLTags.NODELIST + ">" 
			+ 	"<" + XMLTags.NODECOUNT + ">" + nodeCount + "</" + XMLTags.NODECOUNT + ">"
			+	"<" + XMLTags.LAYERS + ">" 
			+	"<" + XMLTags.PHYSICALLAYER + ">" + physical.getName() + "</" + XMLTags.PHYSICALLAYER + ">"
			+	"<" + XMLTags.MACLAYER + ">" + mac.getName() + "</" + XMLTags.MACLAYER + ">"
			+	"<" + XMLTags.LOGLINKLAYER + ">" + loglink.getName() + "</" + XMLTags.LOGLINKLAYER  + ">"
			+	"<" + XMLTags.NETWORKLAYER + ">" + net.getName() + "</" + XMLTags.NETWORKLAYER + ">"
			+	"<" + XMLTags.APPLICATIONLAYER + ">" + app.getName() + "</" + XMLTags.APPLICATIONLAYER + ">"
			+	"</" + XMLTags.LAYERS + ">"
			+	"</" + XMLTags.NODELIST + ">");
		} else {
			LOGGER.fatal("logNodePlacement must be in inConfiguration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void logNodePlacement(Collection<Node> nodes) {				
		if (inConfiguration) {
			if (nodes.size() > 0) {
				configurationPart = configurationPart.append("<" + XMLTags.POSITIONLIST + ">");
				for (Node n : nodes) {
					configurationPart = configurationPart.append(
						"<" + XMLTags.POSITION + ">"
					+	"<" + XMLTags.ID + ">" + n.getId().asInt() + "</" + XMLTags.ID + ">"
					+	"<" + XMLTags.X + ">" + n.getPosition().getXCoord() + "</" + XMLTags.X + ">"
					+	"<" + XMLTags.Y + ">" + n.getPosition().getYCoord() + "</" + XMLTags.Y + ">"
					+	"</" + XMLTags.POSITION + ">");
				}
				configurationPart = configurationPart.append("</" + XMLTags.POSITIONLIST + ">");
			} else {
				LOGGER.warn("no nodes for position");
			}
		} else {
			LOGGER.fatal("logNodePlacement must be in inConfiguration");
		}
	}
	
	// ----------------------- end configuration part -----------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public String getSimulatorId() {
		return simulatorId;
	}
	
	/**
	 * Returns the filename extension for a compact format logfile.
	 *  
	 * @return {@link #FILENAME_EXTENSION}
	 */
	public String getFilenameExtension() {
		return CompactFileLogger.FILENAME_EXTENSION;
	}

	/**
	 * {@inheritDoc}
	 */
	public void logMessage(double time, Address sender, String message, int priority) {
		if (hist != null) {
			if (doneConfiguration) {
				hist.format(Locale.US, "%s t%s n%d ly%s pr%d m%s", 
										TEXT_MESSAGE,
										   String.valueOf(time),
										       sender.getId().asInt(),
										       		sender.getFromLayer().getShortName(),
										       			priority,
										       			mask(message)
						);
			}
		}		
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
		// do nothing		
	}
}
