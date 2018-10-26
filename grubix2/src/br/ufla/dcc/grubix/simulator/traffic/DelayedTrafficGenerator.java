package br.ufla.dcc.grubix.simulator.traffic;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



public class DelayedTrafficGenerator extends TrafficGenerator implements Configurable {

	@ShoXParameter(defaultClass = br.ufla.dcc.grubix.simulator.traffic.OneTimeRandomTrafficGenerator.class)
	private TrafficGenerator generator;
	
	@ShoXParameter(defaultValue = "20")
	private int secondsToWait;

	private int rounds;
	
	private boolean asked = false;
	
	private Configuration config;
	
	@Override
	public void init() throws ConfigurationException {
		super.init();
		generator.init();
		rounds = 0;
	}
	
	/**
	 * @param configuration configuration
	 * @throws ConfigurationException throws if the generation configuration fails
	 */
	@Override
	public void initConfiguration(Configuration configuration)
			throws ConfigurationException {
		config = configuration;
		super.initConfiguration(configuration);
		generator.initConfiguration(configuration);
	}
	
	@Override
	protected SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes) {
		if (!asked) {
			return new TreeSet<TrafficGeneration>();
		} else {
			return generator.generateTraffic(allNodes);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public double getDelayToNextQuery() {
		if (!asked) {
			if (SimulationManager.getInstance().getCurrentTime() < config.getSimulationSteps(secondsToWait)) {
				return config.getSimulationSteps(secondsToWait) - SimulationManager.getInstance().getCurrentTime();
			} else {
				asked = true;
				return generator.getDelayToNextQuery();
			}
		} else {
			return generator.getDelayToNextQuery();
		}
	}

}
