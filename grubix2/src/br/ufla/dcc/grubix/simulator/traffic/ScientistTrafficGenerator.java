package br.ufla.dcc.grubix.simulator.traffic;

import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.NormalRandomDistribution;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



public class ScientistTrafficGenerator extends TrafficGenerator {

	@ShoXParameter(description="number of scientists", defaultValue = "10")
	private int numScientists;
	
	@ShoXParameter(description="mean value of seconds between querys", defaultValue="216")
	private double meanBetweenQuerys;
	
	@ShoXParameter(description="deviation for calculation of next query", defaultValue="100")
	private double deviationForQuerys;
	
	@ShoXParameter(description="packetType", defaultValue="0")
	private int packetType;
	
	private SortedMap<Double, NodeId> nextQuerys;
	
	private NormalRandomDistribution randomGenerator;
	
	private Configuration config;
	
	private boolean first;
	
	public ScientistTrafficGenerator() {
		super();
		first = true;
	}

	/**
	 * Called at the beginning of simulation to initialize traffic generator. This is useful
	 * since while the constructor is executed, the parameters are not yet set.
	 * 
	 * @param configuration configuration instance
	 * @throws ConfigurationException thrown if configuration is invalid e.g. if no generator is configured.
	 */
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		config = configuration;
		randomGenerator = new NormalRandomDistribution(deviationForQuerys);
		nextQuerys = new TreeMap<Double, NodeId>();
		NodeId []allNodeIds = SimulationManager.getAllNodes().keySet().toArray(new NodeId[2]);
		for (int i = 0; i < numScientists; i++) {
			NodeId nextScientist = allNodeIds[configuration.getRandomGenerator().nextInt(allNodeIds.length)];
			double nextTimeStep = config.getSimulationSteps(meanBetweenQuerys + randomGenerator.nextDouble(configuration.getRandomGenerator()));
			nextQuerys.put(nextTimeStep, nextScientist);
		}
		
	}
	
	@Override
	protected SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes) {
		if (first) {
			SortedMap<Double, NodeId> newNextQuerys = new TreeMap<Double, NodeId>();
			SortedSet<TrafficGeneration> newTraffic = new TreeSet<TrafficGeneration>();
			for (Double d : nextQuerys.keySet()) {
				NodeId id = nextQuerys.get(d);
				newNextQuerys.put(d + SimulationManager.getInstance().getCurrentTime(), id);
			}
			nextQuerys = newNextQuerys;
			first = false;
			return newTraffic;
		}
		double actSimulationStep = SimulationManager.getInstance().getCurrentTime(); 
		NodeId []allNodeIds = SimulationManager.getAllNodes().keySet().toArray(new NodeId[2]);
		SortedSet<TrafficGeneration> newTraffic = new TreeSet<TrafficGeneration>();
		double nextTimeStep = nextQuerys.firstKey();
		while (nextTimeStep < actSimulationStep) {
			NodeId id = nextQuerys.get(nextTimeStep);
			TrafficGeneration tg = new TrafficGeneration(id, 
					allNodeIds[config.getRandomGenerator().nextInt(allNodeIds.length)], 
					1, packetType);
			newTraffic.add(tg);
			
			nextQuerys.remove(nextTimeStep);
			double nextDelay = config.getSimulationSteps(meanBetweenQuerys + randomGenerator.nextDouble(config.getRandomGenerator()));
			NodeId nextScientist = allNodeIds[config.getRandomGenerator().nextInt(allNodeIds.length)];
			
			nextQuerys.put(nextDelay + actSimulationStep, nextScientist);
			nextTimeStep = nextQuerys.firstKey();
		}
		return newTraffic;
	}
	
	/**
	 * The TrafficGenEnvelope of SimulationManager calls this method to determine
	 * when to check for new traffic by the TrafficGenerator.
	 *  
	 * @return -1.0 if TrafficGenEnvelope should not check again,
	 * 		   a value greater or equal to zero is a valid delay in simulation steps,
	 * 		   all other (negative) values are invalid. 
	 */
	public double getDelayToNextQuery() {
		return Math.max(nextQuerys.firstKey() - SimulationManager.getInstance().getCurrentTime(), 
				1);
	}
	
	

}
