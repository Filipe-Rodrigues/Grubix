/********************************************************************************
 This file is part of ShoX and was created by the Orcos Team.

 ShoX is free software; you can redistribute it and/or modify it under the terms
 of the GNU General Public License as published by the Free Software Foundation;
 either version 2 of the License, or (at your option) any later version.

 ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 Fifth Floor, Boston, MA 02110-1301, USA

 Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
 and the Orcos developers as defined under http://orcos.cs.upb.de
 ********************************************************************************/
package br.ufla.dcc.grubix.xml;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import br.ufla.dcc.grubix.debug.logging.LogFilter;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.physical.Environment;


/**
 * Writes configuration data in a ShoX configuration file.
 * 
 * @author wolff
 * 
 */
public class XmlConfigurationWriter {

	/**
	 * Logger of the class.
	 */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(XmlConfigurationWriter.class);

	/**
	 * The configuration for a scenario to write into the file.
	 */
	@SuppressWarnings("unchecked")
	private ConfigurableFactory configuration;

	/**
	 * indicates if a svg file is used or not.
	 */
	private boolean svgMode;

	/**
	 * stores the startpositions.
	 */
	@SuppressWarnings("unchecked")
	private Map startpositions;
	
	/**
	 * Constructor.
	 * 
	 * @param factory the configuration data.
	 * @param svgMode specifies whether a svg is used or not. true, a svg file is
	 *            used, false, if not.
	 * @param start the given startpositions.
	 */
	@SuppressWarnings("unchecked")
	public XmlConfigurationWriter(ConfigurableFactory factory, boolean svgMode, Map start) {
		this.configuration = factory;
		this.svgMode = svgMode;
		this.startpositions = start;
	}

	/**
	 * writes the configuration into a file.
	 * 
	 * @param filename the name of the output file.
	 * @throws IOException io error
	 * @throws ConfigurationException thrown if the configuration is not valid
	 */
	public void saveToFile(String filename) throws IOException, ConfigurationException {
		if (!configuration.isValid()) {
			throw new ConfigurationException("Configuration is invalid");
		}
			Document configDocument = new Document();
			Element config = new Element("configuration");

			setXMLSchema(config);

			// write x,y size
			Element field = setupDimensions();
			config.addContent(field);

			setupObserverElement(config);
			setupSuppressElement(config);
			
			setupRandomGenerator(config);
			setupPhysicalModel(config);
			setupTrafficGenerator(config);
			setupMovement(config);
			setupBitmangling(config);

			Element nodes = setupNodeCount();
			

			// layers
			Element layers = setupLayers();

			nodes.addContent(layers);
			config.addContent(nodes);

			// position generator
			Element positionGenerator = setupPositionGenerator();
			config.addContent(positionGenerator);

			// time configuration
			setupTime(config);

			setupLogging(config);
			
			configDocument.addContent(config);

			writeXMLOutput(filename, configDocument);
	}
	
	/**
	 * Sets up the observer element.
	 * 
	 * @param config
	 * @throws ConfigurationException
	 */
	@SuppressWarnings("unchecked")
	private void setupObserverElement(Element config) throws ConfigurationException {
		if (configuration.getValue("observerFactory", null) != null) {
			List<ConfigurableFactory> factories = (List<ConfigurableFactory>) configuration.getValue("observerFactory");
			for (ConfigurableFactory factory : factories) {
				Element factoryElement = factory.saveConfigToXML("observer", false);
				config.addContent(factoryElement);
			}
		}
		
	}
	
	/**
	 * Sets up the suppress element.
	 * 
	 * @param config
	 * @throws ConfigurationException
	 */
	private void setupSuppressElement(Element config) throws ConfigurationException {
		if (configuration.getValue("suppressNeighborhoodDetection", null) != null) {
			Element suppress = new Element("suppress");
			
			Element neighborhoodDetection = new Element("neighborhoodDetection");
			neighborhoodDetection.setText(configuration.getValue("suppressNeighborhoodDetection").toString());
			
			suppress.addContent(neighborhoodDetection);
			config.addContent(suppress);
		}
		
	}

	/**
	 * Sets the logging tag content plus all it's children.
	 * 
	 * @param config element to which to append to
	 * @throws ConfigurationException thrown if required values are missing 
	 */
	private void setupLogging(Element config) throws ConfigurationException {
		if (configuration.getValue(ConfigurationReader.ISLOGGING, null) != null) {
			Element logging = new Element(ConfigurationReader.LOGGING);

			Element loggingOn = new Element(ConfigurationReader.ISLOGGING);
			loggingOn.setText(configuration.getValue(ConfigurationReader.ISLOGGING).toString());
			logging.addContent(loggingOn);

			if (configuration.getValue(ConfigurationReader.SIMULATORID, null) != null) {
				Element simId = new Element(ConfigurationReader.SIMULATORID);
				simId.setText(configuration.getValue(ConfigurationReader.SIMULATORID).toString());
				logging.addContent(simId);
			}

			if (configuration.getValue(ConfigurationReader.NAMEHISTORYFILE, null) != null) {
				Element nameHistory = new Element(ConfigurationReader.NAMEHISTORYFILE);		
				nameHistory.setText(configuration.getValue(ConfigurationReader.NAMEHISTORYFILE).toString());
				logging.addContent(nameHistory);
			}

			if (configuration.getValue(ConfigurationReader.NAMESTATISTICSFILE, null) != null) {
				Element nameStatistics = new Element(ConfigurationReader.NAMESTATISTICSFILE);		
				nameStatistics.setText(configuration.getValue(ConfigurationReader.NAMESTATISTICSFILE).toString());
				logging.addContent(nameStatistics);
			}

			if (configuration.getValue("historyLogger", null) != null) {
				Element logClassElement = new Element("logClass");
				ConfigurableFactory factory = (ConfigurableFactory) configuration.getValue("historyLogger");
				logClassElement.setText(factory.getInstanceClass().getName());
				logging.addContent(logClassElement);
			}

			if (configuration.getValue("liveFilter", null) != null) {
				Element filter = ((LogFilter) configuration.getValue("liveFilter")).getFilterElement();
				logging.addContent(filter);
			}

			config.addContent(logging);
		}
	}

	/**
	 * sets the time.
	 * @param config the element to store the time in.
	 * @throws ConfigurationException thrown if required values are missing
	 */
	private void setupTime(Element config) throws ConfigurationException {

		String stepsPerSecond = configuration.getValue("stepsPerSecond").toString();
		String base = configuration.getValue("simIntervalType", "seconds").toString();
		String simulationTime = configuration.getValue("simulationTime").toString();

		String time = calculateSimulationTime(simulationTime, base, stepsPerSecond);

		Element simulationTimeElement = new Element("simulationtime");
		simulationTimeElement.setAttribute("stepspersecond", stepsPerSecond);
		simulationTimeElement.setAttribute("base", base);
		simulationTimeElement.setText(time);

		config.addContent(simulationTimeElement);

	}

	/**
	 * calculates the time value that should be written to the file.
	 * 
	 * @param simulationTime simulation time in ticks
	 * @param base base
	 * @param stepsPerSecond steps per second
	 * @return time value for the file
	 */
	private String calculateSimulationTime(String simulationTime, String base, String stepsPerSecond) {
		long time = Long.parseLong(simulationTime);
		int sps = Integer.parseInt(stepsPerSecond);
		
		if (base.equals("seconds")) {
			time = time / sps;
		} else if (base.equals("minutes")) {
			time = time / (sps * 60);
		} else if (base.equals("hours")) {
			time = time / (1L * sps * 3600);
		}
		return Long.toString(time);
	}

	/**
	 * sets the bitmangling model.
	 * @param config the element to store the bitmangling model in.
	 */
	@SuppressWarnings("unchecked")
	private void setupBitmangling(Element config) {
		try {
			Element bitmanglingModel = ((ConfigurableFactory) configuration.getValue("bitManglingModel"))
					.saveConfigToXML("bitmanglingmodel", false);
			config.addContent(bitmanglingModel);
		} catch (ConfigurationException e) {
			LOGGER.debug(e);
		}
	}

	/**
	 * sets the movement. 
	 * @param config the element to store the movement in.
	 */
	@SuppressWarnings("unchecked")
	private void setupMovement(Element config) {
		try {
			Element movement = ((ConfigurableFactory) configuration.getValue("movementManager")).saveConfigToXML(
					"movementmanager", false);
			config.addContent(movement);
		} catch (ConfigurationException e) {
			LOGGER.debug(e);
		}
	}

	/**
	 * sets the traffic generator.
	 * @param config the element to store the traffic generator  in.
	 * @throws ConfigurationException configuration cannot be saved
	 */
	@SuppressWarnings("unchecked")
	private void setupTrafficGenerator(Element config) throws ConfigurationException {

		ConfigurableFactory trafficGenerator = (ConfigurableFactory) configuration.getValue("trafficGenerator", null);
		if (trafficGenerator == null) {
			return; // traffic generator not required;
		}
		Element trafficGen = trafficGenerator.saveConfigToXML("trafficgenerator", false);
		config.addContent(trafficGen);
	}

	/**
	 * sets the random generator.
	 * @param config the element to store the random generator in.
	 * @throws ConfigurationException  configuration cannot be saved
	 */
	@SuppressWarnings("unchecked")
	private void setupRandomGenerator(Element config) throws ConfigurationException {
		ConfigurableFactory factory = (ConfigurableFactory) configuration.getValue("randomGenerator", null);
		if (factory == null) {
			return; // random Generator can be null
		}
		Element rangomGeneratorElement = factory.saveConfigToXML("randomgenerator", false);
		config.addContent(rangomGeneratorElement);
	}
	

	/**
	 * sets the physical model.
	 * @param config the element to store the physical model in.
	 * @throws ConfigurationException  configuration cannot be saved
	 */
	@SuppressWarnings("unchecked")
	private void setupPhysicalModel(Element config) throws ConfigurationException {

		Element phyModel = ((ConfigurableFactory) configuration.getValue("physicalModel")).saveConfigToXML(
				"physicalmodel", false);
		config.addContent(phyModel);

	}

	/**
	 * generates a nodes Element and sets up the nodecount.
	 * @return Element Element for all nodes, configured with the nodecount.
	 */
	private Element setupNodeCount() {
		Element nodes = new Element("nodes");

		try {
			Element nodeCount = new Element("count");
			nodeCount.setText(configuration.getValue("nodeCount").toString());
			nodes.addContent(nodeCount);
		} catch (ConfigurationException e) {
			LOGGER.error(e);
		}
		try {
			Element numTypes = new Element("numtypes");
			numTypes.setText(configuration.getValue("nodeTypesCount").toString());
			nodes.addContent(numTypes);
		} catch (ConfigurationException e) {
			LOGGER.error(e);
		}

		return nodes;
	}
	
	
	/**
	 * sets the dimensions.
	 * @return Element containing the information regarding the dimensions.
	 * @throws ConfigurationException error saving the dimension informations
	 */
	private Element setupDimensions() throws ConfigurationException {
		Element field = new Element("field");
		Environment environment = null;

		Object tmp = configuration.getValue("environment", null);
		
		if (tmp != null) {
			environment = (Environment) tmp;
		}

		if (svgMode) {
			if (environment != null) {
				// in case an environment is set, a svg file is specified
				Element svg = new Element("svg");
				svg.addContent(environment.getSVGFileName());
				field.addContent(svg);
			} else {
				LOGGER.error("for some strange reason, the environment was null.");
			}
		} else {
			// no svg chosen, x and y value specified by the gui
			Element dimension = new Element("dimension");
			dimension.setAttribute("x", configuration.getValue("xSize").toString());
			dimension.setAttribute("y", configuration.getValue("ySize").toString());
			field.addContent(dimension);
		}
		return field;
	}

	/**
	 * sets the XML schema and the xsd file.
	 * @param config the XML element.
	 */
	private void setXMLSchema(Element config) {
		Namespace namespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		config.addNamespaceDeclaration(namespace);

		config.setAttribute(new Attribute("noNamespaceSchemaLocation", "configuration.xsd", namespace));
	}

	/**
	 * writes the configDocument to a configuration file.
	 * 
	 * @param outputFile the filename to write to.
	 * @param configDocument the document to write to file.
	 * @throws IOException io error
	 */
	private void writeXMLOutput(String outputFile, Document configDocument) throws IOException {
		String file = outputFile;
		XMLOutputter outputter = new XMLOutputter();
		Format f = Format.getPrettyFormat();
		f.setIndent("   ");
		outputter.setFormat(Format.getPrettyFormat());

		if (!file.endsWith(".xml")) {
			file = file + ".xml";
		}
		BufferedWriter write = null;
		try {
			write = new BufferedWriter(new FileWriter(new File(file)));
			outputter.output(configDocument, write);
		} finally {
			closeCloseable(write);
		}
	}
	
	/**
	 * Closes a closeable (stream).
	 * If the closeable parameter is null, than nothing is done.
	 * Should be called in a finally block.
	 * 
	 * @param closeable stream (may be null)
	 */
	private void closeCloseable(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOGGER.error(e);
			}	
		}
	}

	/**
	 * prepares the Element for the positiongenerator.
	 * @return Element containing the configuration for the positiongenerator.
	 */
	@SuppressWarnings("unchecked")
	private Element setupPositionGenerator() {
		Element positionGenerator = new Element("positions");
		Element generated = new Element("generated");
		try {
			
			String targetFile = "";
			if (configuration.getValue("startPositionsFile") != null) {
				targetFile = configuration.getValue("startPositionsFile").toString();
			}
			Element generator = ((ConfigurableFactory) configuration.getValue("positionGenerator"))
					.saveConfigToXML("generator", false);
			generated.addContent(generator);
			Element targetFileElement = new Element("targetfile");
			targetFileElement.addContent(targetFile);
			generated.addContent(targetFileElement);
			positionGenerator.addContent(generated);
		} catch (ConfigurationException e) {
			LOGGER.debug(e.getMessage());
			// if no positiongenerator is seleceted
			if (startpositions.size() > 0) {
				for (int i = 1; i <= startpositions.size(); i++) {
					Element pos = new Element("position");
					Position p = (Position) startpositions.get(i);
					pos.setAttribute("id", i + "");
					pos.setAttribute("x", p.getXCoord() + "");
					pos.setAttribute("y", p.getYCoord() + "");
					positionGenerator.addContent(pos);
				}
			}
		}
		return positionGenerator;
	}

	/**
	 * prepares the Element for the layers of the node.
	 * @return Element containing the configuration for the layers of the node.
	 * @throws ConfigurationException error while configuring the layers
	 */
	@SuppressWarnings("unchecked")
	private Element setupLayers() throws ConfigurationException {
		Element layers = new Element("layers");

		ConfigurableFactory energyManager = (ConfigurableFactory) configuration.getValue("energyManagerFactory", null);
		if (energyManager != null) {
			Element energyManagerElement = energyManager.saveConfigToXML("energyManager", false);
			layers.addContent(energyManagerElement);
		}
		
		ConfigurableFactory physicalLayer = (ConfigurableFactory) configuration.getValue("physicalLayerFactory", null);
		if (physicalLayer != null) {
			Element physicalLayerElement = physicalLayer.saveConfigToXML("physical", false);
			layers.addContent(physicalLayerElement);
		}

		ConfigurableFactory macLayer = (ConfigurableFactory) configuration.getValue("macLayerFactory", null);
		if (macLayer != null) {
			Element macLayerElement = macLayer.saveConfigToXML("mac", false);
			layers.addContent(macLayerElement);
		}

		ConfigurableFactory logLinkLayer = (ConfigurableFactory) configuration.getValue("logLinkLayerFactory", null);
		if (logLinkLayer != null) {
			Element logLinkLayerElement = logLinkLayer.saveConfigToXML("loglink", false);
			layers.addContent(logLinkLayerElement);
		}

		ConfigurableFactory networkLayerFactory = (ConfigurableFactory) 
			configuration.getValue("networkLayerFactory", null);
		if (networkLayerFactory != null) {
			Element networkLayerElement = networkLayerFactory.saveConfigToXML("network", false);
			layers.addContent(networkLayerElement);
		}

		ConfigurableFactory operatingSystemLayerFactory = (ConfigurableFactory) 
			configuration.getValue("operatingSystemLayerFactory", null);
		if (operatingSystemLayerFactory != null) {
			Element operatingSystemLayerElement = operatingSystemLayerFactory.saveConfigToXML("operatingSystem", false);
			layers.addContent(operatingSystemLayerElement);
		}

		Element applicationLayer = ((ConfigurableFactory) configuration.getValue("applicationLayerFactory"))
				.saveConfigToXML("application", false);
		layers.addContent(applicationLayer);

		return layers;
	}
}
