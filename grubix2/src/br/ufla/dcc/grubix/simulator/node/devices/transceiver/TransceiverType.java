package br.ufla.dcc.grubix.simulator.node.devices.transceiver;

import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;

/**
 * This enumeration shall provide Shox with some common transceiver configurations.
 *  
 * @author Florian Rittmeier
  */
public enum TransceiverType {

	/**
	 * A fictive transceiver.
	 */
	SampleTransceiver(new Watt(1, 0, 0, 0), new Watt(0, 7, 0, 0), new Watt(0, 5, 0, 0), new Watt(0, 0, 0, 0),
					  0.01, new Watt(1, 5, 0, 0),
					  0.015, new Watt(1, 7, 0, 0),
					  0.0, new Watt(0, 0, 0, 0));
	
	/**
	 * Consumption while in transmit state.
	 */
	private Watt transmitConsumption;
	
	/**
	 * Consumption while in receive state.
	 */
	private Watt receiveConsumption;
	
	/**
	 * Consumption while in idle state (state before receive).
	 */
	private Watt idleConsumption;
	
	/**
	 * Consumption when in off state.
	 * As there are transceivers which only provide something like a sleep
	 * state which has a consumption greater than zero, the off state may have
	 * a consumption bigger than zero.
	 */
	private Watt offConsumption;
	
	/**
	 * Time in seconds how long a switch from receive to transmit state takes. 
	 */
	private double switchTimeReceiveTransmit;
	/**
	 * Consumption while switching from receive to transmit state.
	 */
	private Watt switchConsumptionReceiveTransmit;
	
	/**
	 * Time in seconds how long a switch from idle to transmit state takes. 
	 */
	private double switchTimeIdleTransmit;
	/**
	 * Consumption while switching from idle to transmit state.
	 */
	private Watt switchConsumptionIdleTransmit;
	
	/**
	 * Time in seconds how long a switch from transmit to idle state takes. 
	 */
	private double switchTimeTransmitIdle;
	/**
	 * Consumption while switching from transmit to idle state.
	 */
	private Watt switchConsumptionTransmitIdle;
	
	/**
	 * Constructor allowing this enum to held
	 * the consumption values for the different state.
	 *
	 *	@param transmitConsumption the consumption in transmit state
	 *	@param receiveConsumption the consumption in receive state
	 *	@param idleConsumption the consumption in idle state
	 *	@param offConsumption the consumption in off state
	 *	@param switchTimeReceiveTransmit the time to switch from receive to transmit state
	 *	@param switchConsumptionReceiveTransmit the consumption while switching from receive to transmit state
	 *	@param switchTimeIdleTransmit the time to switch from idle to transmit state
	 *	@param switchConsumptionIdleTransmit the consumption while switching from idle to transmit state
	 *	@param switchTimeTransmitIdle the time to switch from transmit to idle state
	 *	@param switchConsumptionTransmitIdle the consumption while switching from transmit to idle state
	 */
	TransceiverType(Watt transmitConsumption, Watt receiveConsumption,
					Watt idleConsumption, Watt offConsumption,
					double switchTimeReceiveTransmit, Watt switchConsumptionReceiveTransmit,
					double switchTimeIdleTransmit, Watt switchConsumptionIdleTransmit,
					double switchTimeTransmitIdle, Watt switchConsumptionTransmitIdle) {
		
		this.transmitConsumption = transmitConsumption;
		this.receiveConsumption = receiveConsumption;
		this.idleConsumption = idleConsumption;
		this.offConsumption = offConsumption;

		this.switchTimeReceiveTransmit = switchTimeReceiveTransmit;
		this.switchConsumptionReceiveTransmit = switchConsumptionReceiveTransmit;

		this.switchTimeIdleTransmit = switchTimeIdleTransmit;
		this.switchConsumptionIdleTransmit = switchConsumptionIdleTransmit;

		this.switchTimeTransmitIdle = switchTimeTransmitIdle;
		this.switchConsumptionTransmitIdle = switchConsumptionTransmitIdle;
		
	}

	/**
	 * @return the idleConsumption
	 */
	public final Watt getIdleConsumption() {
		return idleConsumption;
	}

	/**
	 * @return the offConsumption
	 */
	public final Watt getOffConsumption() {
		return offConsumption;
	}

	/**
	 * @return the receiveConsumption
	 */
	public final Watt getReceiveConsumption() {
		return receiveConsumption;
	}

	/**
	 * @return the switchConsumptionIdleTransmit
	 */
	public final Watt getSwitchConsumptionIdleTransmit() {
		return switchConsumptionIdleTransmit;
	}

	/**
	 * @return the switchConsumptionReceiveTransmit
	 */
	public final Watt getSwitchConsumptionReceiveTransmit() {
		return switchConsumptionReceiveTransmit;
	}

	/**
	 * @return the switchConsumptionTransmitIdle
	 */
	public final Watt getSwitchConsumptionTransmitIdle() {
		return switchConsumptionTransmitIdle;
	}

	/**
	 * @return the switchTimeIdleTransmit
	 */
	public final double getSwitchTimeIdleTransmit() {
		return switchTimeIdleTransmit;
	}

	/**
	 * @return the switchTimeReceiveTransmit
	 */
	public final double getSwitchTimeReceiveTransmit() {
		return switchTimeReceiveTransmit;
	}

	/**
	 * @return the switchTimeTransmitIdle
	 */
	public final double getSwitchTimeTransmitIdle() {
		return switchTimeTransmitIdle;
	}

	/**
	 * @return the transmitConsumption
	 */
	public final Watt getTransmitConsumption() {
		return transmitConsumption;
	}
}
