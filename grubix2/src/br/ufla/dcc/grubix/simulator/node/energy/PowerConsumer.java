package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;
import br.ufla.dcc.grubix.xml.Configurable;

/**
 * Base interface for all types of power consumers. 
 * 
 * @author Florian Rittmeier
 */
public interface PowerConsumer extends Configurable {

	/**
	 * Returns the average power consumption.
	 * @return average power consumption
	 */
	Watt getAveragePowerConsumption();
	
	/**
	 * inits the configuration of the power consumer.
	 * 
	 * @param configuration configuration of the system run.
	 */
	void initConfiguration(Configuration configuration);
	
	/**
	 * Connects the PowerConsumer with an interface for
	 * communication with the EnergyManager.
	 * 
	 * @param feedback the command interface
	 */
	void setFeedbackConnection(PowerConsumerFeedback feedback);
	
	/**
	 * Suspends the power consumer.
	 * As the energy manager cares on that a usual power consumer
	 * should not care about this, but there might be complex
	 * power consumers which need this information.
	 * 
	 * @param suspended new suspension state
	 */
	void suspend(boolean suspended);
	
}
