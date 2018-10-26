/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.controller;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.energy.PowerConsumer;
import br.ufla.dcc.grubix.simulator.node.energy.PowerConsumerFeedback;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * This is just an example for a Microcontroller
 * with some average power consumption.
 * 
 * @author Florian Rittmeier
 */
public class ExampleMicrocontroller implements PowerConsumer {
	
	/**
	 * {@inheritDoc}
	 */
	public Watt getAveragePowerConsumption() {
		return new Watt(0, 210, 0, 0); // 210 mW
	}

	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFeedbackConnection(PowerConsumerFeedback feedback) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}
	

}
