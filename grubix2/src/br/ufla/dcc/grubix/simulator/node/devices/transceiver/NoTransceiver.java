/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.transceiver;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.node.energy.PowerConsumerFeedback;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * This is no transceiver.
 * 
 * @author Florian Rittmeier
 */
public class NoTransceiver extends GenericTransceiver {

	/** 
	 * {@inheritDoc}
	 */
	public Watt getAveragePowerConsumption() {
		return new Watt(0, 0, 0, 0);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
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

	/** 
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void switchToRadioState(RadioState newstate) {
	}

}
