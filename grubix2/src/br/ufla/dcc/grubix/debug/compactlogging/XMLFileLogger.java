package br.ufla.dcc.grubix.debug.compactlogging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;

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
import br.ufla.dcc.grubix.simulator.physical.UnitDisc;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.XMLTags;



/**
 * This ShoXLogger implementation generates XML formatted output.
 * 
 * @author mika
 *
 */
public class XMLFileLogger implements ShoxLogger {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(XMLFileLogger.class);

	/**
	 * Denotes if file write position is within the configuration tag.
	 */
	private boolean inConfiguration = false;
	
	/**
	 * Denotes if file write position is beyond the configuration tag.
	 */
	private boolean doneConfiguration = false;
	
	/**
	 * Denotes if output files containing the hist output were specified and are ready for logging.
	 */
	private boolean initComplete = false;
	
	/**
	 * Denotes the Id of the simulation which uses this logger.
	 */
	private String simulatorId;
	
	/**
	 * Logging output.
	 */
	private PrintWriter hist;
	
	/**
	 * Statistics output.
	 */
	private PrintWriter stat;
	
	/**
	 * Logging file handle.
	 */
	private File logFile;
	
	/**
	 * Statistics file handle.
	 */
	private File statFile;
	/**
	 * Extension of log filenames created by this logger. 
	 */
	public static final String FILENAME_EXTENSION = ".xml";
	
	/**
	 * {@inheritDoc}
	 */
	public void endConfigurationPart() {
		if (!inConfiguration || doneConfiguration) {
			LOGGER.fatal("either not in configuration or already out");
		}

		doneConfiguration = true;
		inConfiguration = false;
		if (hist != null) {
			hist.println("</" + XMLTags.CONFIGURATION + ">");
			hist.println("<" + XMLTags.SIMULATION + ">");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logConfiguration(double fieldXSize, double fieldYSize, int stepsPerSecond, 
			long simulationTime, PhysicalModel physicalModel, MovementManager movementManager, 
			String description) {
		
		if (hist != null) {
			if (inConfiguration) {
				hist.println("<" + XMLTags.DESCRIPTION + " " + XMLTags.WRITE + "=\"" + description + "\" />");/**Jesimar*/
				hist.println("<" + XMLTags.FIELD + ">");
				hist.println("<" + XMLTags.X + ">" + fieldXSize + "</" + XMLTags.X + ">");
				hist.println("<" + XMLTags.Y + ">" + fieldYSize + "</" + XMLTags.Y + ">");
				hist.println("</" + XMLTags.FIELD + ">");
				hist.println("<" + XMLTags.SIMULATIONTIME + " " + XMLTags.STEPSPERSECOND + "=\"" + stepsPerSecond 
						+ "\" base=\"steps\">" + simulationTime + "</" + XMLTags.SIMULATIONTIME + ">");
				hist.println("<" + XMLTags.PHYSMODEL + ">" + physicalModel.getClass().getName() 
						+ "</" + XMLTags.PHYSMODEL + ">");
				hist.println("<" + XMLTags.MOVEMENTMANAGER + ">" + movementManager.getClass().getName() 
						+ "</" + XMLTags.MOVEMENTMANAGER + ">");
				hist.println("<" + XMLTags.COMMUNICATIONRADIUS + ">" + UnitDisc.getRadiusCommuntion()
						+ "</" + XMLTags.COMMUNICATIONRADIUS  + ">");/**Jesimar*/
			} else {
				LOGGER.fatal("logConfiguration must be inConfiguration");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logDequeueEvent(double time, EventId event) {
		/**Jesimar*/
//		if (hist != null) {
//			if (doneConfiguration) {
//				hist.println("<" + XMLTags.DEQUEUEEVENT + ">");
//				hist.println("<" + XMLTags.TIMESTAMP + ">" + time + "</" + XMLTags.TIMESTAMP + ">");
//				hist.println("<" + XMLTags.ID + ">" + event.asInt() + "</" + XMLTags.ID + ">");
//				hist.println("</" + XMLTags.DEQUEUEEVENT + ">");
//			} else {
//				LOGGER.fatal("logDequeueEvent must be after doneConfiguration");
//			}
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logEnqueueEvent(EventEnvelope eventEnvelope, NodeId receiver, int priority) {
		if (hist != null) {
			if (doneConfiguration) {
				Loggable lgabl = (Loggable) eventEnvelope;
				String str = lgabl.log(Loggable.XML);
				if (!str.contains("<senderlayer>Air</senderlayer>")){/**Jesimar*/
					hist.println("<" + XMLTags.ENQUEUEEVENT + ">");
					hist.println("<" + XMLTags.TIMESTAMP + ">" + eventEnvelope.getTime() 
							+ "</" + XMLTags.TIMESTAMP + ">");
					hist.println("<" + XMLTags.TYPE + ">" + lgabl.getEventType() + "</" + XMLTags.TYPE + ">");
					hist.println("<" + XMLTags.ID + ">" + lgabl.getEventId().asInt() + "</" + XMLTags.ID + ">");
					hist.println("<" + XMLTags.RECEIVERID + ">" + receiver + "</" + XMLTags.RECEIVERID + ">");				
					hist.println(str);			

					hist.println("</" + XMLTags.ENQUEUEEVENT + ">");
				}
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
		/**Jesimar*/
//		if (hist != null) {
//			if (doneConfiguration) {
//				hist.println("<" + XMLTags.LINKSTATE + " id1=\"" + node1.asInt() + "\" id2=\"" + node2.asInt() 
//						+ "\" name=\"" + name + "\" type=\"" + type + "\" value=\"" + value + "\" />");
//			} else {
//				LOGGER.fatal("logLinkStateEvent must be after doneConfiguration");
//			}
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logMoveEvent(NodeId node, double x, double y, double time, int priority) {
		if (hist != null) {
			if (doneConfiguration) {
				hist.println("<" + XMLTags.MOVE + " id=\"" + node.asInt() + "\" x=\"" + x + "\" y=\"" + y 
						+ "\" time=\"" + time + "\" />");
			} else {
				LOGGER.fatal("logMoveEvent must be after doneConfiguration");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logNodePlacement(Collection<Node> nodes) {
		if (hist != null) {
			if (inConfiguration) {
				if (nodes.size() > 0) {
					hist.println("<" + XMLTags.POSITIONLIST + ">");					
					for (Node n : nodes) {
						hist.println("<" + XMLTags.POSITION + ">");
						hist.println("<" + XMLTags.ID + ">" + n.getId().asInt() + "</" + XMLTags.ID + ">");
						hist.println("<" + XMLTags.X + ">" + n.getPosition().getXCoord() + "</" + XMLTags.X + ">");
						hist.println("<" + XMLTags.Y + ">" + n.getPosition().getYCoord() + "</" + XMLTags.Y + ">");
						hist.println("<" + XMLTags.INFO + " " + XMLTags.NODETYPE + "=\"" + 
								n.getNodeName() + "\" />");/**Jesimar*/
						hist.println("<" + XMLTags.ISMOBILE + ">" + n.getIsMobile() + "</" + 
								XMLTags.ISMOBILE + ">");/**Jesimar*/
						hist.println("</" + XMLTags.POSITION + ">");
					}
					hist.println("</" + XMLTags.POSITIONLIST + ">");
				} else {
					LOGGER.warn("no nodes for position");
				}
			} else {
				LOGGER.fatal("logNodePlacement must be in inConfiguration");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logNodeSetup(int nodeCount, Class< ? extends Layer> physical, 
			Class< ? extends Layer> mac, Class< ? extends Layer> loglink, 
			Class< ? extends Layer> net, Class< ? extends Layer> app) {
		if (hist != null) {
			if (inConfiguration) {
				hist.println("<" + XMLTags.NODELIST + ">");
				hist.println("<" + XMLTags.NODECOUNT + ">" + nodeCount + "</" + XMLTags.NODECOUNT + ">");
				hist.println("<" + XMLTags.LAYERS + ">");
				hist.println("<" + XMLTags.PHYSICALLAYER + ">" + physical.getName() 
						+ "</" + XMLTags.PHYSICALLAYER + ">");
				hist.println("<" + XMLTags.MACLAYER + ">" + mac.getName() + "</" + XMLTags.MACLAYER + ">");
				hist.println("<" + XMLTags.LOGLINKLAYER + ">" + loglink.getName() + "</" + XMLTags.LOGLINKLAYER 
						+ ">");
				hist.println("<" + XMLTags.NETWORKLAYER + ">" + net.getName() + "</" + XMLTags.NETWORKLAYER 
						+ ">");
				hist.println("<" + XMLTags.APPLICATIONLAYER + ">" + app.getName() + "</" 
						+ XMLTags.APPLICATIONLAYER + ">");
				hist.println("</" + XMLTags.LAYERS + ">");
				hist.println("</" + XMLTags.NODELIST + ">");
			} else {
				LOGGER.fatal("logNodePlacement must be in inConfiguration");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logNodeStateEvent(NodeId node, String name, String type,
			String value) {
		if (hist != null) {
			if (doneConfiguration) {
				hist.println("<" + XMLTags.NODESTATE + " id=\"" + node.asInt() + "\" name=\"" + name + "\" type=\"" 
						+ type + "\" value=\"" + value + "\" />");
			} else {
				LOGGER.fatal("logNodeStateEvent must be after doneConfiguration");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logStatistics(NodeId node, LayerType layer, String xAxisLabel, String yAxisLabel, 
															String xValue, String yValue) {
		if (stat != null) {
			if (doneConfiguration) {
				stat.println("<" + XMLTags.STATISTIC + ">\n"
						+ "<" + XMLTags.SENDERID + ">" + node.asInt() + "</" + XMLTags.SENDERID + ">\n"
						+ "<" + XMLTags.SENDERLAYER + ">" + layer.toString() + "</" + XMLTags.SENDERLAYER + ">\n"
						+ "<" + XMLTags.AXES + " x=\"" + xAxisLabel + "\" y=\"" + yAxisLabel + "\" />\n"
						+ "<" + XMLTags.VALUE + " x=\"" + xValue + "\" y=\"" + yValue + "\" />\n"
						+ "</" + XMLTags.STATISTIC + ">");
			} else {
				LOGGER.fatal("logStatistics must be after doneConfiguration");
			}			
		} else {
			LOGGER.warn("logStatistics w/o statistics file");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void startConfigurationPart() {
		if (inConfiguration || doneConfiguration || !initComplete) {
			LOGGER.fatal("either in configuration or already out, or files not inited yet");
		} else {
			inConfiguration = true;
			if (hist != null) {
				hist.println("<" + XMLTags.CONFIGURATION + ">");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void initLogging(String historyFile, String statisticsFile, String simulatorId) throws IOException {
		this.simulatorId = simulatorId;
		
		if (historyFile != null && !historyFile.equalsIgnoreCase("")) {
			logFile = new File(historyFile);
			
			hist = new PrintWriter(new BufferedWriter(new FileWriter(logFile + FILENAME_EXTENSION)));
			hist.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			hist.println("<!-- ShoX XML-historyfile, created " + Calendar.getInstance().getTime() 
					+ " by " + this.getClass().getCanonicalName() + "  -->");
			hist.println("<" + XMLTags.GLOBAL + ">");
		}
		
		if (statisticsFile != null && !statisticsFile.equalsIgnoreCase("")) {
			statFile = new File(statisticsFile);
			
			stat = new PrintWriter(new BufferedWriter(new FileWriter(statFile + FILENAME_EXTENSION)));
			stat.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			stat.println("<!-- ShoX XML-statisticsfile, created " + Calendar.getInstance().getTime()
					+ " by " + this.getClass().getCanonicalName() + " -->");
			stat.println("<" + XMLTags.STATISTICS + ">");
		}
		
		initComplete = true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void finishLogging() {
		if (hist != null && initComplete && doneConfiguration) {
			hist.println("</" + XMLTags.SIMULATION + ">");
			hist.println("</" + XMLTags.GLOBAL + ">");
			hist.close();
		}
		
		if (stat != null) {
			stat.println("</" + XMLTags.STATISTICS + ">");
			stat.flush();
			stat.close();
		}
	}

	/**
	 * Returns the ID of the simulation which is using this logger for logging.
	 * @return ID of the simulation which is using this logger for logging
	 */
	public String getSimulatorId() {
		return simulatorId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
		// does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void logMessage(double time, Address sender, String message, int priority) {
		/**Jesimar*/
//		if (hist != null) {
//			if (doneConfiguration) {
//				hist.println("<" + XMLTags.MESSAGE + ">");
//				hist.println("<time>" + time + "</time>");
//				hist.println("<priority>" + priority + "</priority>");
//				hist.println("<node>" + sender.getId().asInt() + "</node>");
//				hist.println("<layer>" + sender.getFromLayer().getShortName() + "</layer>");
//				hist.println("<text>" + message + "</text>");
//				hist.println("</" + XMLTags.MESSAGE + ">");
//			}
//		}
	}
}
