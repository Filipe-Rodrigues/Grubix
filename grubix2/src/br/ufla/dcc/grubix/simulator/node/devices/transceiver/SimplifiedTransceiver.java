/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.transceiver;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.node.energy.PowerConsumerFeedback;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * This transceiver only reacts to a subset of all radio states,
 * cause currently (January 2008) the
 * {@link br.ufla.dcc.grubix.simulator.node.user.MAC_IEEE802_11bg_DCF}
 * does not switch correctly, but nevertheless we want to simulate
 * somewhat precise energy consumptions.
 *  
 * @author Florian Rittmeier
 */
public class SimplifiedTransceiver extends GenericTransceiver {

	/**
	 * The transceiver type name is given by the configuration.
	 * The SimplifiedTransceiver will determine the appropriate transceiver characteristics
	 * using the {@link br.ufla.dcc.grubix.simulator.node.devices.transceiver.TransceiverType}.
	 */
	@ShoXParameter
	private String transceiverTypeName;
	
	/**
	 * The transceiver type, determined by the transceiver type name given in the configuration.
	 */
	private TransceiverType transceiverType;
	
	/**
	 * The current consumption, determined by the last state change.
	 */
	private Watt currentConsumption = new Watt(0, 0, 0, 0);
	
	/**
	 * Required to inform the EnergyManager about changes.
	 */
	private PowerConsumerFeedback feedback;
	
	
	
	/** 
	 * {@inheritDoc}
	 */
	public Watt getAveragePowerConsumption() {
		return (Watt) this.currentConsumption.clone();
	}

	/** 
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
		boolean found = false;
		
		for (TransceiverType transceiver : TransceiverType.values()) {
			if (transceiver.name().compareToIgnoreCase(transceiverTypeName) == 0) {
				transceiverType = transceiver;
				found = true;
				break;
			}
		}
		
		if (!found) {
			// TODO Should be a more precise exception (type)
			throw new RuntimeException("invalid transceiver type name");
		}
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
	public void suspend(boolean suspended) {
		// we don`t need this
	}

	/** 
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
		// we don`t need this
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void switchToRadioState(RadioState newstate) {

		if (newstate == RadioState.LISTENING) {
			feedback.beforeUpdate();
			currentConsumption = transceiverType.getIdleConsumption();
			feedback.afterUpdate();
		}
		if (newstate == RadioState.OFF) {
			feedback.beforeUpdate();
			currentConsumption = transceiverType.getOffConsumption();
			feedback.afterUpdate();
		}
		if (newstate == RadioState.RECEIVING) {
			feedback.beforeUpdate();
			currentConsumption = transceiverType.getReceiveConsumption();
			feedback.afterUpdate();
		}
		if (newstate == RadioState.SENDING) {
			feedback.beforeUpdate();
			currentConsumption = transceiverType.getTransmitConsumption();
			feedback.afterUpdate();
		}
		
	}

}
