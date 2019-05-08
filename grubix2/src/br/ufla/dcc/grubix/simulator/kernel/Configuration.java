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

package br.ufla.dcc.grubix.simulator.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.debug.compactlogging.CompactFileLogger;
import br.ufla.dcc.grubix.debug.compactlogging.ShoxLogger;
import br.ufla.dcc.grubix.debug.logging.LogFilter;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.movement.MovementManager;
import br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator;
import br.ufla.dcc.grubix.simulator.node.AirModule;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.LogLinkLayer;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayer;
import br.ufla.dcc.grubix.simulator.node.SimulationObserver;
import br.ufla.dcc.grubix.simulator.node.energy.BasicEnergyManager;
import br.ufla.dcc.grubix.simulator.node.energy.EnergyManager;
import br.ufla.dcc.grubix.simulator.node.user.LogLinkDebug;
import br.ufla.dcc.grubix.simulator.node.user.MACDebug;
import br.ufla.dcc.grubix.simulator.node.user.NetworkDebug;
import br.ufla.dcc.grubix.simulator.node.user.PhysicalDebug;
import br.ufla.dcc.grubix.simulator.node.user.os.NullOperatingSystemLayer;
import br.ufla.dcc.grubix.simulator.node.user.os.OperatingSystemLayer;
import br.ufla.dcc.grubix.simulator.nodestartup.NodeStartupManager;
import br.ufla.dcc.grubix.simulator.nodestartup.ShoxClassicNodeStartupManager;
import br.ufla.dcc.grubix.simulator.physical.BitManglingModel;
import br.ufla.dcc.grubix.simulator.physical.Environment;
import br.ufla.dcc.grubix.simulator.physical.PhysicalModel;
import br.ufla.dcc.grubix.simulator.random.JavaRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurableFactory;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ConfigurationReader;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/** 
 * Class for all CONFIGURATION values.
 * 
 * @author Andreas Kumlehn, Dirk Meister
 */
public final class Configuration implements Configurable, Serializable {
	
	/**---------Codigo Jesimar----------*/
	
	/** 
	 * Description of simulation 
	 */	
	@ShoXParameter(required = true)
	private String description;
	
	/** 
	 * Description of simulation 
	 */		
	private double comRadio;
	
	@ShoXParameter
	private LinkedList<String> listIsMobile;

	/**
	 * gets description.
	 * @return current description
	 */
	public String getDescription() {
		return description;
	}
		
	/**
	 * gets description.
	 * @return current description
	 */
	public double getComRadio() {
		return comRadio;
	}
	
	public LinkedList<String> getListIsMobile(){
		return this.listIsMobile;
	}
	
	/**-------Fim codigo Jesimar---------*/
	
	/** 
	 * the delay between the phyState = SENDING and the real start of the transmission.
	 */
	private double propagationDelay;

	/** Logger of the class Configuration. */
	private static transient final Logger LOGGER = Logger.getLogger(Configuration.class);

	/**
	 * singleton configuration instance.
	 */
	private static Configuration configuration;

	/**
	 * access method to the singleton configuration instance.
	 * If no configuration is read from the file system, this call throws an {@link IllegalStateException}.
	 * Before invoking getInstance the method readConfig must be called.
	 * 
	 * @see Configuration#readConfig(String)
	 * @return the configuration singleton instance
	 */
	public static Configuration getInstance() {
		if (configuration == null) {
			throw new IllegalStateException("configuration not set yet");
		}
		return configuration;
	}

	/**
	 * reads the whole configuration from an xml file.
	 * 
	 * A side effect of this method is that the configuration singleton instance is set.
	 * 
	 * @param pathToConfig Path to the config xml file.
	 * @return a new configuration object with parameters set.
	 * @exception ConfigurationException thrown if the configuration contains an error
	 * 
	 */
	public static Configuration readConfig(final String pathToConfig)
			throws ConfigurationException {
		LOGGER.info("Configuration attempts to read file from: " + pathToConfig
				+ ".");
		ConfigurationReader reader = new ConfigurationReader(pathToConfig);
		configuration = reader.getNewConfiguration();

		LOGGER.info("Configuration read.");
		return configuration;
	}

	/**
	 * interface to the simulation.
	 * Should be used as much as possible instead of accessing
	 * the SimulationManager directly.
	 */
	@ShoXParameter(defaultClass = SimulationManagerKernel.class)
	private transient SimulationKernel kernel;
	
	/** 
	 * Filepath to the nodes file. Needed by NodeGenerator. 
	 */
	@ShoXParameter()
	private String startPositionsFile;

	/** 
	 * The start position generator to be used for the simulation. 
	 */
	@ShoXParameter()
	private StartPositionGenerator positionGenerator;

	/** 
	 * Maps node IDs to the nodes' inital positions. 
	 */
	private Map<Integer, Position> startPositions;

	/** 
	 * The total number of nodes which are simulated. 
	 */
	@ShoXParameter(required = true)
	private int nodeCount;

	/**
	 * The number of type of nodes
	 */
	@ShoXParameter(defaultValue = "1")
	private int nodeTypesCount;
	
	@ShoXParameter
	private Map<String, Integer> nameAndAmmount;
	
	@ShoXParameter
	private double maxTimeForMove;
	
	@ShoXParameter
	private double maxRange;
	
	/**
	 * The size of the playing ground in X direction.
	 * Range of xCoords is [0, xSize].
	 */
	@ShoXParameter(required = true)
	private double xSize;

	/** 
	 * The size of the playing ground in Y direction.
	 * Range of xCoords is [0, ySize].
	 */
	@ShoXParameter(required = true)
	private double ySize;	
	
	/** 
	 * The number of simulation steps to be executed. -1 for infinite. 
	 */
	@ShoXParameter(required = true)
	private long simulationTime;

	/** 
	 * The number of simulation steps per real-world second. This value represents
	 * the "granularity" of the simulation.
	 */
	@ShoXParameter(required = true)
	private int stepsPerSecond;

	/** 
	 * Indicates whether all events should be logged to XML or not. 
	 */
	@ShoXParameter(defaultValue = "false")
	private boolean log;
	
	/**
	 * An ID for the simulator instance, used for distributed parallel execution.
	 */
	@ShoXParameter(defaultValue = "default")
	private String simulatorId;
	
	/**
	 * Filename for history logfile.
	 */
	@ShoXParameter(defaultValue = "")
	private String nameHistoryFile;
	
	/**
	 * Filename for statistics logfile.
	 */
	@ShoXParameter(defaultValue = "")
	private String nameStatisticsFile;
	
	/**
	 * suppresses the neighborhood detection of the SimulationManager.
	 * In dynamic, large sensor networks, the neighborhood detection takes a significant
	 * amount of time. Shox user may decide to suppress this detection. Some
	 * configurations (e.g. configs using the OptimalSourceRouting) depend on the
	 * detection.
	 * If the detection is suppresses, any usage of the neighborhood results in an
	 * illegal state exception.
	 */
	@ShoXParameter(description = "suppresses the neighborhood detection", defaultValue="false")
	private boolean suppressNeighborhoodDetection;

//	/**	
//	 * Indicates whether the DATA of LoggableData objects should be logged, too. 
//	 */
//	@ShoXParameter(defaultValue = "true")
//	private boolean logData = true;

	/** 
	 * physical model used for simulation. 
	 */
	@ShoXParameter(required = true)
	private PhysicalModel physicalModel;
	

	/**
	 *  bit mangling model used for simulation. 
	 */
	@ShoXParameter(required = true)
	private BitManglingModel bitManglingModel;

	/** 
	 * traffic generator model used for simulation. 
	 */
	@ShoXParameter()
	private TrafficGenerator trafficGenerator;

	/** 
	 * movement model used for simulation. 
	 */
	@ShoXParameter(required = true)
	private MovementManager movementManager;

	/** 
	 * PositionPerturber to use during simulation. 
	 */
	private final String positionPerturberName = "NoPerturbation";
	
	@ShoXParameter()
	private ConfigurableFactory<SimulationObserver>[] observerFactory;
 
	/** 
	 * application layer used for simulation. 
	 */	
	@ShoXParameter
	private ArrayList<ConfigurableFactory<ApplicationLayer>> applicationLayerFactory;

	/** 
	 * operating system (layer) used for simulation. 
	 */
	@ShoXParameter
	private  ArrayList<ConfigurableFactory<OperatingSystemLayer>> operatingSystemLayerFactory;

	/** 
	 *network layer used for simulation. 
	 * */
	@ShoXParameter
	private  ArrayList<ConfigurableFactory<NetworkLayer>> networkLayerFactory;

	/** 
	 * log link layer used for simulation. 
	 */
	@ShoXParameter
	private  ArrayList<ConfigurableFactory<LogLinkLayer>> logLinkLayerFactory;

	/** 
	 * mac layer used for simulation. 
	 */
	@ShoXParameter
	private  ArrayList<ConfigurableFactory<MACLayer>> macLayerFactory;

	/**
	 * physical layer used for simulation. 
	 */
	@ShoXParameter
	private  ArrayList<ConfigurableFactory<PhysicalLayer>> physicalLayerFactory;

	/**
	 * air module used for simulation.
	 */
	@ShoXParameter(defaultClass = AirModule.class)
	//ShoXParameter(defaultClass = AirModule_DBG.class)
	private ConfigurableFactory<AirModule> airModuleFactory;
	
	/**
	 * factory for an energy manager.
	 */
	@ShoXParameter
	private  ArrayList<ConfigurableFactory<EnergyManager>> energyManagerLayerFactory;

	/**
	 * factory for an node startup manager.
	 */
	@ShoXParameter(defaultClass = ShoxClassicNodeStartupManager.class)
	private NodeStartupManager nodeStartupManager;
	
	/** 
	 * time interval between to movement rounds. 
	 */
	private final double movementTimeInterval = 200.0;

	/** 
	 * time interval between to traffic generation rounds. 
	 */
	private final double trafficTimeInterval = 500.0;

	/**	
	 * Delay for internal events like WakeUps and packets in layerstack.
	 */
	private final double delayForInternalTransmission = 0.0; // talk to Dirk Held, if a change is needed (pleazze).

	/**	
	 * Delay for delivering events to a whole node (e.g. Moved, Initialize) 
	 */
	private final double delayForToNodeEvents = 0.0;

	/**	
	 * consumed power for an internal transmission inside a node. 
	 */
	private final double powerConsumptionForInternalTransmission = 0.001;
	
	/**
	 * history logger.
	 */
	@ShoXParameter(description = "history logger", defaultClass = CompactFileLogger.class)
	private ShoxLogger historyLogger;
	
	/**
	 * Live logging filter.
	 */
	@ShoXParameter(description = "live logging filter, active during simulation run")
	private LogFilter liveFilter;

	/** 
	 * Represents the physical map with obstacles where the network is deployed. 
	 */
	@ShoXParameter()
	private Environment environment;

	/**
	 * Represents the timeintervall (simulation steps, hours, minutes, seconds).
	 */
	@ShoXParameter()
	private String simIntervalType;
	
	/**
	 * the random number generator to generate the sequences of numbers.
	 */
	@ShoXParameter(description = "random number generator", defaultClass = JavaRandomGenerator.class)
	private RandomGenerator randomGenerator;
	
	/** 
	 * Private constructor of the class Configuration.  
	 */
	private Configuration() {
		// nothing to do.
	}

	/**
	 * gets airModuleFactory.
	 * @return current airModuleFactory
	 */
	public ConfigurableFactory<AirModule> getAirModuleFactory() {
		return airModuleFactory;
	}

	/**
	 * gets applicationLayerFactory.
	 * @return current applicationLayerFactory
	 */

	/**
	 * gets bitManglingModel.
	 * @return current bitManglingModel
	 */
	public BitManglingModel getBitManglingModel() {
		return bitManglingModel;
	}

	/**
	 * gets delayForInternalTransmission.
	 * @return current delayForInternalTransmission
	 */
	public double getDelayForInternalTransmission() {
		return delayForInternalTransmission;
	}

	/**
	 * gets delayForToNodeEvents.
	 * @return current delayForToNodeEvents
	 */
	public double getDelayForToNodeEvents() {
		return delayForToNodeEvents;
	}

	/**
	 * gets environment.
	 * @return current environment
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * gets logLinkLayerFactory.
	 * @return current logLinkLayerFactory
	 */
	public ArrayList<ConfigurableFactory<LogLinkLayer>> getLogLinkLayerFactory() {
		return logLinkLayerFactory;
	}

	/**
	 * gets macLayerFactory.
	 * @return current macLayerFactory
	 */
	public ArrayList<ConfigurableFactory<MACLayer>> getMacLayerFactory() {
		return macLayerFactory;
	}

	/**
	 * gets movementManager.
	 * @return current movementManager
	 */
	public MovementManager getMovementManager() {
		return movementManager;
	}

	/**
	 * gets movementTimeInterval.
	 * @return current movementTimeInterval
	 */
	public double getMovementTimeInterval() {
		return movementTimeInterval;
	}

	/**
	 * gets networkLayerFactory.
	 * @return current networkLayerFactory
	 */
	public ArrayList<ConfigurableFactory<NetworkLayer>> getNetworkLayerFactory() {
		return networkLayerFactory;
	}

	/**
	 * gets nodeCount.
	 * @return current nodeCount
	 */
	public int getNodeCount() {
		return nodeCount;
	}
	
	/**
	 * gets operatingSystemLayerFactory.
	 * @return current operatingSystemLayerFactory
	 */
	public ArrayList<ConfigurableFactory<OperatingSystemLayer>> getOperatingSystemLayerFactory() {
		return operatingSystemLayerFactory;
	}

	/**
	 * gets physicalLayerFactory.
	 * @return current physicalLayerFactory
	 */
	public ArrayList<ConfigurableFactory<PhysicalLayer>> getPhysicalLayerFactory() {
		return physicalLayerFactory;
	}

	
	public ArrayList<ConfigurableFactory<ApplicationLayer>> getApplicationLayerFactory(){
		return this.applicationLayerFactory;
	}
	
	/**
	 * gets physicalModel.
	 * @return current physicalModel
	 */
	public PhysicalModel getPhysicalModel() {
		return physicalModel;
	}

	/**
	 * gets positionGenerator.
	 * @return current positionGenerator
	 */
	public StartPositionGenerator getPositionGenerator() {
		return positionGenerator;
	}

	/**
	 * gets positionPerturberName.
	 * @return current positionPerturberName
	 */
	public String getPositionPerturberName() {
		return positionPerturberName;
	}

	/**
	 * gets powerConsumptionForInternalTransmission.
	 * @return current powerConsumptionForInternalTransmission
	 */
	public double getPowerConsumptionForInternalTransmission() {
		return powerConsumptionForInternalTransmission;
	}

	/** 
	 * gets the propagation delay.
	 * @return the propagationDelay 
	 */
	public double getPropagationDelay() {
		return propagationDelay;
	}

	/**
	 * Simple helper method to convert between real-world seconds and simulation steps.
	 * @param seconds A value of real-world seconds
	 * @return The number of simulation steps which correspond to the given number of seconds
	 */
	public double getSimulationSteps(double seconds) {
		return stepsPerSecond * seconds;
	}

	/**
	 * Gets simulationTime in simulation steps,
	 * which is the time when the simulation will end.
	 * @return the maximum simulationTime
	 */
	public long getSimulationTime() {
		return simulationTime;
	}

	/**
	 * gets startPositions.
	 * @return current startPositions
	 */
	public Map<Integer, Position> getStartPositions() {
		return startPositions;
	}

	/**
	 * gets startPositionsFile.
	 * @return current startPositionsFile
	 */
	public String getStartPositionsFile() {
		return startPositionsFile;
	}

	/**
	 * gets stepsPerSecond.
	 * @return current stepsPerSecond
	 */
	public int getStepsPerSecond() {
		return stepsPerSecond;
	}

	/**
	 * gets trafficGenerator.
	 * @return current trafficGenerator
	 */
	public TrafficGenerator getTrafficGenerator() {
		return trafficGenerator;
	}

	/**
	 * gets trafficTimeInterval.
	 * @return current trafficTimeInterval
	 */
	public Double getTrafficTimeInterval() {
		return trafficTimeInterval;
	}

	/**
	 * gets xSize.
	 * @return current xSize
	 */
	public double getXSize() {
		return xSize;
	}

	/**
	 * gets ySize.
	 * @return current ySize
	 */
	public double getYSize() {
		return ySize;
	}	

	/**
	 * inits the configuration after setting the parameters.
	 * 
	 * @see br.ufla.dcc.grubix.xml.Configurable#init()
	 */
	public void init() {
		propagationDelay = getSimulationSteps(1.0e-7); // artificial init value
	}

	/**
	 * get the current logging state.
	 * @return true if logging is enabled, otherwise false.
	 */
	public boolean isLogging() {
		return log;
	}
	
	/**
	 * Returns the set simulator ID, if none is set by the configuration file, "default" is returned.
	 * @return simulator ID, if none is set by the configuration file, "default" is returned
	 */
	public String getSimulatorId() {
		return simulatorId;
	}

	/**
	 * logs all configuration settings.
	 * 
	 * @param writer logger.
	 */
	public void logConfig(ShoxLogger writer) {
		writer.logConfiguration(xSize, ySize, stepsPerSecond, 
				simulationTime, physicalModel, movementManager, description);
	}

	/**
	 * sets the startPositions.
	 * @param startPositions new startPositions value
	 */
	public void setStartPositions(Map<Integer, Position> startPositions) {
		this.startPositions = startPositions;
	}
	
	/**
	 * Gets the intervall type.
	 * @return String the intervall type.
	 */
	public String getIntervallType() {
		return this.simIntervalType;
	}
	
	/**
	 * Simple helper method to convert between simulation steps and real-world seconds.
	 * @param simulationSteps The number of simulation steps which are to be converted
	 * @return The number of real-world seconds which correspond to the given number of simulation steps
	 */
	public double getSeconds(double simulationSteps) {
		return simulationSteps / stepsPerSecond;
	}
	
	/**
	 * gets a "global" random generator.
	 * @return current randomGenerator
	 */
	public RandomGenerator getRandomGenerator() {
		return randomGenerator;
	}

	/**
	 * returns the simulation kernel.
	 * @return the kernel
	 */
	public SimulationKernel getKernel() {
		return kernel;
	}

	/**
	 * @return the energyManagerFactory
	 */
	public ArrayList<ConfigurableFactory<EnergyManager>> getEnergyManagerLayerFactory() {
		return energyManagerLayerFactory;
	}

	/**
	 * @return the historyLogger
	 */
	public ShoxLogger getHistoryLogger() {
		return historyLogger;
	}
	
	/**
	 * @return the live filter
	 */
	public LogFilter getLiveLogFilter() {
		return liveFilter;
	}
	
	public String getNameHistoryLogfile() {
		return nameHistoryFile;
	}
	
	public String getNameStatisticsLogfile() {
		return nameStatisticsFile;
	}

	/**
	 * @return the suppressNeighborhoodDetection
	 */
	public boolean isSuppressNeighborhoodDetection() {
		return suppressNeighborhoodDetection;
	}

	/**
	 * @return the observerFactory
	 */
	public ConfigurableFactory<SimulationObserver>[] getObserverFactory() {
		return observerFactory;
	}

	public NodeStartupManager getNodeStartupManager() {
		return nodeStartupManager;
	}

	public int getNodeTypesCount() {
		return nodeTypesCount;
	}

	public void setNodeTypesCount(int nodeTypesCount) {
		this.nodeTypesCount = nodeTypesCount;
	}
	
	public Map<String, Integer> getNameAndAmmount(){
		return this.nameAndAmmount;
	}
}
