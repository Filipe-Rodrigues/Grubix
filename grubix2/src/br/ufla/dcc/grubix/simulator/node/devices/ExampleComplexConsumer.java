/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.node.energy.PowerConsumer;
import br.ufla.dcc.grubix.simulator.node.energy.PowerConsumerFeedback;
import br.ufla.dcc.grubix.simulator.node.energy.PowerLevelCallback;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * This is an example for a more complex PowerConsumer.
 * It uses the additional feature like
 * - beeing informed when a certain power level is reached
 * - changing its average consumption
 * - announcing some one time consumption
 * 
 * @author Florian Rittmeier
 */
public class ExampleComplexConsumer implements PowerConsumer {

	/**
	 * Used to store the average consumption of this consumer.
	 */
	private Watt avgConsumption = new Watt(0, 50, 0, 0);
	
	/**
	 * Used to store the feedack connection to the EnergyManager.
	 */
	private PowerConsumerFeedback feedback;
	
	/**
	 * Sample callback which changes the average consumption the way this should be done.
	 */
	private PowerLevelCallback firstCallback = new PowerLevelCallback() {
		public void performed(Node node, long level) {
			feedback.beforeUpdate();
			avgConsumption = new Watt(0, 25, 0, 0);
			feedback.afterUpdate();
		}
	};

	/**
	 * Sample callback which announces a one time consumption.
	 */
	private PowerLevelCallback secondCallback = new PowerLevelCallback() {
		public void performed(Node node, long level) {
			feedback.announceOneTimeConsumption(new Watt(0, 200, 0, 0), 0.5);
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public Watt getAveragePowerConsumption() {
		return (Watt) avgConsumption.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
		feedback.registerPowerLevelCallback(50, firstCallback);
		feedback.registerPowerLevelCallback(25, secondCallback);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFeedbackConnection(PowerConsumerFeedback feedback) {
		this.feedback = feedback;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}
	
}
