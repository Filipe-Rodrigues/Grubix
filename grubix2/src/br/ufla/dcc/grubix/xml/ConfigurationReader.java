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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import br.ufla.dcc.grubix.debug.logging.LogFilter;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.LogLinkLayer;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayer;
import br.ufla.dcc.grubix.simulator.node.energy.EnergyManager;
import br.ufla.dcc.grubix.simulator.node.user.os.OperatingSystemLayer;
import br.ufla.dcc.grubix.simulator.physical.Environment;


/**
 * Parses the XML configuration file for the simulator and makes the values
 * available in corresponding objects.
 * 
 * Used a XML DOM reading the configuration files and creates a new {@link Configuration} object
 * when the method {@link ConfigurationReader#getNewConfiguration()} is called. It used the
 * {@link ConfigurableFactory} object configuration and injection framework.
 * 
 * The methods are structured according to the Configuration XML Schema which can be found in
 * the ShoX conf directory.
 * 
 * @author dmeister
 */
public class ConfigurationReader {

	/**
	 * XML tag name for the random generator.
	 */
	public static final String RANDOM_GENERATOR = "randomgenerator";
	
	/**
	 * XML tag name for the physical model.
	 */
	public static final String PHYSICAL_MODEL = "physicalmodel";
	
	/**
	 * XML tag name for the bit mangling model.
	 */
	public static final String BITMANGLING_MODEL = "bitmanglingmodel";

	/**
	 * XML tag name for the movement model.
	 */
	public static final String MOVEMENT_MANAGER = "movementmanager";

	/**
	 * XML tag name for the node startup manager.
	 */
	public static final String NODE_STARTUP_MANAGER = "nodestartupmanager";
	
	/**
	 * XML tag name for the traffic generator.
	 */
	public static final String TRAFFIC_GENERATOR = "trafficgenerator";
	
	/**
	 * XML tag name for the start position generator.
	 */
	public static final String START_POSITION_GENERATOR = "generator";
	
	/**
	 * XML tag name for the application layer.
	 */
	public static final String APPLICATION_LAYER = "application";
	
	/**
	 * XML tag name for the operating system.
	 */
	public static final String OPERATING_SYSTEM_LAYER = "operatingSystem";
	
	/**
	 * XML tag name for the network layer.
	 */
	public static final String NETWORK_LAYER = "network";

	/**
	 * XML tag name for the logical link layer.
	 */
	public static final String LOGLINK_LAYER = "loglink";
	
	/**
	 * XML tag name for the MAC layer.
	 */
	public static final String MAC_LAYER = "mac";
	
	/**
	 * XML tag name for the physical layer.
	 */
	public static final String PHYSICAL_LAYER = "physical";
	
	/**
	 * XML tag name for the logging section.
	 */
	public static final String LOGGING = "logging";
	
	/**
	 * XML tag name for the name of the history logfile.
	 */
	public static final String NAMEHISTORYFILE = "nameHistoryFile";
	
	/**
	 * XML tag name for the name of the statistics logfile.
	 */
	public static final String NAMESTATISTICSFILE = "nameStatisticsFile";
	/**
	 * XML tag name for the logging switch.
	 */
	public static final String ISLOGGING = "log";
	/**
	 * XML tag name for the simulator/job ID. Used only if simulation is executed from a {@link Worker} instance.
	 */
	public static final String SIMULATORID = "simulatorId";

	/** 
	 * Used to read node positions stored in configuration or external file. 
	 */
	private PositionHandler posHandler;
	
	/**
	 * factory instance of the configuration object.
	 * The configuration object is the root of the configuration object tree.
	 */
	private final ConfigurableFactory<Configuration> factory
		= new DefaultConfigurableFactory<Configuration>(Configuration.class);
	
	/**
	 * Parses the passed XML configuration file and fills all member variables
	 * with their corresponding values.
	 * 
	 * @param xmlConfFile The path to the XML configuration file for ShoX
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	public ConfigurationReader(String xmlConfFile) throws ConfigurationException {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document configDocument = builder.build(xmlConfFile);
			parseConfigurationElement(configDocument.getRootElement());
		} catch (Exception e) {
			throw new ConfigurationException("config error", e);
		}
	}
	
	/**
	 * gets the map with positions readed from the configuration file.
	 * 
	 * @return A table with (node ID, position [x, y coordinates]) for all nodes. May be null if no
	 * positon informations are provided by the configuration file.
	 */
	public final Map<Integer, Position> getPositions() {
		if (this.posHandler != null) {
			return this.posHandler.getPositions();
		}
		return null;
	}
	
	/**
	 * parses the root configuration element.
	 * 
	 * @param configurationElement root element
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	@SuppressWarnings("unchecked")
	private void parseConfigurationElement(Element configurationElement) throws ConfigurationException {
		
		/**Jesimar*/
		Element descriptionElement = configurationElement.getChild("description");
		if (descriptionElement != null){
			parseDescriptionElement(descriptionElement);
		}
		
		Element fieldElement = configurationElement.getChild("field");
		parseFieldElement(fieldElement);
		
		Element loggingElement = configurationElement.getChild("logging");
		parseLoggingElement(loggingElement);
		
		List observerElements = configurationElement.getChildren("observer");
		if (observerElements != null) {
			for (Object observerObject : observerElements) {
				Element observerElement = (Element) observerObject;
				parseObserverElement(observerElement);
				
			}
		}
		
		Element suppressElement = configurationElement.getChild("suppress");
		if (suppressElement != null) {
			parseSuppressElement(suppressElement);			
		}
		
		Element randomGeneratorElement = configurationElement.getChild(RANDOM_GENERATOR);
		if (randomGeneratorElement != null) {
			ConfigurableFactory randomGeneratorFactory =  parseParamSetElement(randomGeneratorElement);
			factory.putConfiguration("randomGenerator", randomGeneratorFactory);
		}
		
		ConfigurableFactory physicalModelFactory =
			parseParamSetElement(configurationElement.getChild(PHYSICAL_MODEL));
		factory.putConfiguration("physicalModel", physicalModelFactory);
	
		Element trafficGeneratorElement = configurationElement.getChild(TRAFFIC_GENERATOR);
		if (trafficGeneratorElement != null) {
			ConfigurableFactory trafficGeneratorFactory =  parseParamSetElement(trafficGeneratorElement);
			factory.putConfiguration("trafficGenerator", trafficGeneratorFactory);
		}
		
		ConfigurableFactory movementManagerFactory
			=  parseParamSetElement(configurationElement.getChild(MOVEMENT_MANAGER));
		factory.putConfiguration("movementManager", movementManagerFactory);
		
		ConfigurableFactory bitmanglingmodelFactory
			= parseParamSetElement(configurationElement.getChild(BITMANGLING_MODEL));
		factory.putConfiguration("bitManglingModel", bitmanglingmodelFactory);

		Element nodeStartupManagerElement = configurationElement.getChild(NODE_STARTUP_MANAGER);
		if (nodeStartupManagerElement != null) {
			ConfigurableFactory nodeStartupManagerFactory =  parseParamSetElement(nodeStartupManagerElement);
			factory.putConfiguration("nodeStartupManager", nodeStartupManagerFactory);
		}		
		
		Element nodesElement = configurationElement.getChild("nodes");
		parseNodesElement(nodesElement);
		
		Element positionsElement = configurationElement.getChild("positions");
		parsePositionsElement(positionsElement);
		
		parseSimulationTimeElement(configurationElement.getChild("simulationtime"));
	}

	private void parseObserverElement(Element observerElement) throws ConfigurationException {
		ConfigurableFactory observerFactory = parseParamSetElement(observerElement);
		factory.putConfiguration("observerFactory", observerFactory);
	}

	/**
	 * parses the suppress element of the configuration.
	 * 
	 * @param suppressElement suppress xml element (not null)
	 * @throws ConfigurationException
	 */
	private void parseSuppressElement(Element suppressElement) throws ConfigurationException {
		String neighborhoodDetection = suppressElement.getChildText("neighborhoodDetection");
		if (neighborhoodDetection != null) {
			factory.putConfiguration("suppressNeighborhoodDetection", neighborhoodDetection);
		}
	}

	/**
	 * parses a logging element.
	 * 
	 * @param loggingElement logging xml element
	 * @throws ConfigurationException thrown if the configuration is invalid.
	 */
	private void parseLoggingElement(Element loggingElement) throws ConfigurationException {
		String log = null;
		String logClass = null;
		String logHistoryFilename = null;
		String logStatisticsFilename = null;
		String simId = null;
		Element logLiveFilter = null;
		
		if (loggingElement != null) {
			log = loggingElement.getChildText(ISLOGGING);
			logClass = loggingElement.getChildText("logClass");
			logHistoryFilename = loggingElement.getChildText(NAMEHISTORYFILE);
			logStatisticsFilename = loggingElement.getChildText(NAMESTATISTICSFILE);
			simId = loggingElement.getChildText(SIMULATORID);
			logLiveFilter = loggingElement.getChild(LogFilter.TAG_FILTER);
		}
		 
		if (log != null) {
			factory.putConfiguration(ISLOGGING, log);
		}
		
		if (simId != null) {
			factory.putConfiguration(SIMULATORID, simId);
		}
		
		if (logHistoryFilename != null) {
			factory.putConfiguration(NAMEHISTORYFILE, logHistoryFilename);
		}
		
		if (logStatisticsFilename != null) {
			factory.putConfiguration(NAMESTATISTICSFILE, logStatisticsFilename);
		}
		
		if (logClass != null) {
			factory.putConfiguration("historyLogger", logClass);
		}
		
		if (logLiveFilter != null) {
			factory.putConfiguration("liveFilter", new LogFilter(logLiveFilter, true));
		} else {
			factory.putConfiguration("liveFilter", new LogFilter(true));
		}
	}
	

	/**
	 * parses a dimension element.
	 * 
	 * @param dimensionElement dimension xml element
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	private void parseDimensionElement(Element dimensionElement) throws ConfigurationException {
		String x = dimensionElement.getAttributeValue("x");
		String y = dimensionElement.getAttributeValue("y");
		try {
			factory.putConfiguration("xSize", x);
			factory.putConfiguration("ySize", y);
		} catch (NumberFormatException e) {
			throw new ConfigurationException("invalid dimension element", e);
		}
	}
	
	/**
	 * parses a field element.
	 * @param fieldElement field xml element
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	private void parseFieldElement(Element fieldElement) throws ConfigurationException {
		Element dimensionElement = fieldElement.getChild("dimension");
		if (dimensionElement != null) {
			parseDimensionElement(dimensionElement);
			return;
		}
		Element svgElement = fieldElement.getChild("svg");
		parseSvgElement(svgElement);
	}
	
	/**Jesimar*/
	private void parseDescriptionElement(Element descriptionElement) throws ConfigurationException {
		String desc = descriptionElement.getAttributeValue("write");
		try {
			factory.putConfiguration("description", desc);			
		} catch (NumberFormatException e) {
			throw new ConfigurationException("invalid dimension element", e);
		}
	}

	/**
	 * parses a generated element.
	 * @param generatedElement generated xml element
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	@SuppressWarnings("unchecked")
	private void parseGeneratedElement(Element generatedElement) throws ConfigurationException {
		ConfigurableFactory generatorFactory = parseParamSetElement(generatedElement.getChild("generator"));
		factory.putConfiguration("positionGenerator", generatorFactory);
		String targetFileName = generatedElement.getChildText("targetfile");
		
		factory.putConfiguration("startPositionsFile", targetFileName);
	}

	/**
	 * parses a node file element.
	 * @param nodeFileElement node file xml element
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	private void parseNodeFileElement(Element nodeFileElement) throws ConfigurationException {
		String filename = nodeFileElement.getTextTrim();
		this.posHandler = new PositionHandler(filename);
		
		factory.putConfiguration("startPositionsFile", filename);
	}

	/**
	 * parses a nodes element.
	 * @param nodesElement nodes xml element
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	private void parseNodesElement(Element nodesElement) throws ConfigurationException {
		List nodes = nodesElement.getChildren("node");
		Integer nodeCount = this.getNodeNumber((List<Element>)nodes);
		Integer nodeTypesCount = nodes.size();
		factory.putConfiguration("nodeTypesCount", nodeTypesCount.toString() );
		factory.putConfiguration("nodeCount", nodeCount.toString());
		this.generateNameAndAmmount((List<Element>)nodes);
		if (nodes != null) {
			this.parseMultipleNodesList(nodes);
		}
	}
	
	private void generateNameAndAmmount(List<Element> nodes) throws ConfigurationException{
		Map<String, Integer> nameAndAmmount = new TreeMap<String, Integer>(); 
		LinkedList<String> listIsMobile = new LinkedList<String>(); 
		for (Element nodeElement : nodes) {
			String sCount = nodeElement.getAttributeValue("count");
			String name = nodeElement.getAttributeValue("name");
			String isMobile = nodeElement.getAttributeValue("ismobile");
			if (isMobile != null && !isMobile.equals("")){
				listIsMobile.add(isMobile);
			}
			
			nameAndAmmount.put(name, Integer.parseInt(sCount));
		}
		factory.putConfiguration("listIsMobile", listIsMobile);
		factory.putConfiguration("nameAndAmmount", nameAndAmmount);
	}
	
	
	private int getNodeNumber(List<Element> nodes){
		int total = 0;
		for (Element nodeElement : nodes) {
			String sCount = nodeElement.getAttributeValue("count");
			int count = Integer.parseInt(sCount);
			total += count;
		}
		return total;
	}
	
	
	@SuppressWarnings("unchecked")
	private void parseLayersElement(Element layersElement, Map<String, ArrayList> layersMap) throws ConfigurationException {
		
		Set<String> keySet = layersMap.keySet();
		for (String key : keySet) {
			Element layerElement = layersElement.getChild(key);
			ConfigurableFactory genericFactory = this.parseParamSetElement(layerElement);
			
			ArrayList list = layersMap.get(key);
			list.add(genericFactory);
			factory.putConfiguration(key+"LayerFactory", list);
		}
	}
	
	private void parseMultipleNodesList(List nodes) throws ConfigurationException{
		Map<String, ArrayList> layersMap = new HashMap<String, ArrayList>();
		
		ArrayList<ConfigurableFactory<ApplicationLayer>> applicationLayerList = new ArrayList<ConfigurableFactory<ApplicationLayer>>();	
		layersMap.put("application", applicationLayerList);
		
		ArrayList<ConfigurableFactory<EnergyManager>> energyManagerList = new ArrayList<ConfigurableFactory<EnergyManager>>();
		layersMap.put("energyManager", energyManagerList);
		
		ArrayList<ConfigurableFactory<PhysicalLayer>> physicalLayerList = new ArrayList<ConfigurableFactory<PhysicalLayer>>();
		layersMap.put("physical", physicalLayerList);
		
		ArrayList<ConfigurableFactory<MACLayer>> macLayerList = new ArrayList<ConfigurableFactory<MACLayer>>();
		layersMap.put("mac", macLayerList);
		
		ArrayList<ConfigurableFactory<LogLinkLayer>> logLinkLayerList = new ArrayList<ConfigurableFactory<LogLinkLayer>>();
		layersMap.put("logLink", logLinkLayerList);
		
		ArrayList<ConfigurableFactory<NetworkLayer>> networkLayerList = new ArrayList<ConfigurableFactory<NetworkLayer>>();
		layersMap.put("network", networkLayerList);
		
		ArrayList<ConfigurableFactory<OperatingSystemLayer>> operatingSystemLayerList = new ArrayList<ConfigurableFactory<OperatingSystemLayer>>();
		layersMap.put("operatingSystem", operatingSystemLayerList);		
				
		for (Object node : nodes) {
			Element layersElement = ((Element)node).getChild("layers");
			this.parseLayersElement(layersElement, layersMap);
		}
	}
	
	/**
	 * creates a new configurable factory object.
	 * @param paramSetElement paramSet xml element
	 * @return a new configurable factory object
	 * @throws ConfigurationException thrown if configuration is invalid
	 */
	private ConfigurableFactory< ? extends Configurable> parseParamSetElement(Element paramSetElement) 
			throws ConfigurationException {
		String className = paramSetElement.getChildText("class");
		Element paramsElement = paramSetElement.getChild("params");
		
		ConfigurableFactory< ? extends Configurable> newFactory = ConfigurationParameter.getFactory(className);
		
		if (paramsElement != null) {
			for (Object paramObject : paramsElement.getChildren("param")) {
				Element paramElement = (Element) paramObject;
				String name = paramElement.getAttributeValue("name");
				String value = paramElement.getText();
				newFactory.putConfiguration(name, value);
			}
			for (Object classParamObject : paramsElement.getChildren("classparam")) {
				Element classParam = (Element) classParamObject;
							
				String name = classParam.getAttributeValue("name");
				ConfigurableFactory< ? extends Configurable> nestedFactory
					= parseParamSetElement(classParam);
				newFactory.putConfiguration(name, nestedFactory);
			}
		}
		return newFactory;
	}
	
	/**
	 * parses a position element.
	 * @param positionElement position xml element
	 */
	private void parsePositionElement(Element positionElement) {
		int id = Integer.parseInt(positionElement.getAttributeValue("id"));
    	double x = Double.parseDouble(positionElement.getAttributeValue("x"));
    	double y = Double.parseDouble(positionElement.getAttributeValue("y"));
    	
    	this.posHandler.putPosition(id, new Position(x, y));
	}
	
	/**
	 * parses the positions element.
	 * @param positionsElement positions xml element
	 * @throws ConfigurationException thrown if configuration is invalid
	 */
	private void parsePositionsElement(Element positionsElement) throws ConfigurationException {
		Element nodeFileElement = positionsElement.getChild("nodefile");
		if (nodeFileElement != null) {
			parseNodeFileElement(nodeFileElement);
			return;
		}
		
		Element generatedElement = positionsElement.getChild("generated");
		if (generatedElement != null) {
			parseGeneratedElement(generatedElement);
			return;
		}
		
		this.posHandler = new PositionHandler();
		for (Object positionObject : positionsElement.getChildren("position")) {
			Element positionElement = (Element) positionObject;
			parsePositionElement(positionElement);
		}
	}
	
	/**
	 * parse the simulation time element.
	 * @param simulationTimeElement simulation time xml element
	 * @throws ConfigurationException thrown if the configuration is invalid
	 */
	private void parseSimulationTimeElement(Element simulationTimeElement) throws ConfigurationException {
		int stepsPerSecond = Integer.parseInt(simulationTimeElement.getAttributeValue("stepspersecond"));
		String timeBase = simulationTimeElement.getAttributeValue("base");
		int time = Integer.parseInt(simulationTimeElement.getTextTrim());
		long simulationTime = 0;
		if (timeBase == null) {
			timeBase = "seconds";
		}
		int timeInSeconds;
		if (timeBase.equals("hours")) {
			timeInSeconds = time * 3600;
		} else if (timeBase.equals("minutes")) {
			timeInSeconds = time * 60;
		} else {
			timeInSeconds = time;
		}
		if (!timeBase.equals("steps")) {
			simulationTime = 1L * timeInSeconds * stepsPerSecond;
		} else {
			simulationTime = time;
		}
		
		factory.putConfiguration("stepsPerSecond", stepsPerSecond);
		factory.putConfiguration("simulationTime", simulationTime);
		factory.putConfiguration("simIntervalType", timeBase);
	}

	/**
	 * parse the svg xml element.
	 * @param svgElement svg xml element
	 * @throws ConfigurationException thrown if configuration is invalid
	 */
	private void parseSvgElement(Element svgElement) throws ConfigurationException {
		Environment environment = new Environment(svgElement.getText());
		factory.putConfiguration("environment", environment);
		factory.putConfiguration("xSize", environment.getWidth());
		factory.putConfiguration("ySize", environment.getHeight());
	}
	
	/**
	 * gets a new configuration object.
	 * Its sets not the getInstance() global state.
	 * 
	 * @return a new configuration
	 * @throws ConfigurationException thrown when configuration is invalid
	 */
	public Configuration getNewConfiguration() throws ConfigurationException {
		Configuration configuration = factory.newInstance();
		configuration.setStartPositions(getPositions());
		
		configuration.getPhysicalModel().initConfiguration(configuration);
		configuration.getBitManglingModel().initConfiguration(configuration);
		configuration.getMovementManager().initConfiguration(configuration);
		configuration.getNodeStartupManager().initConfiguration(configuration);
		return configuration;
	}
	
	/**
	 * gets the configuration factory configured using the file contents.
	 * 
	 * @return the configured factory.
	 */
	public ConfigurableFactory<Configuration> getConfigurableFactory() {
		return factory;
	}
}
