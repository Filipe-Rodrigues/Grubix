package br.ufla.dcc.grubix.simulator.node.energy.units;

import br.ufla.dcc.grubix.simulator.SimulationFailedException;

/**
 * A class representing a SIUnit in general.
 * Subclasses Joule and Watt rely on this common abstraction.  
 * @author Florian Rittmeier
  */
public abstract class SIUnit implements Cloneable {

	/**
	 * The internal value of a SIUnit.
	 */
	protected long value = 0;
	
	/**
	 * Used for scaling the saved value.
	 * There might be subclasses whose functionality only work with
	 * the given value (or is only tested with it).
	 */
	protected final long adjustmentToBaseUnit = 9;
	
	/**
	 * Friendly name of the SIUnit. Derived classes should set their own name.
	 */
	protected String siUnitName = "SIUnit";

	/**
	 * Default constructor.
	 */
	public SIUnit() {
		super();
	}

	/**
	 * Constructor initializing this SIUnit using the give values.
	 * All parameters are summed up.
	 * @param baseamount amount measured in base units
	 * @param milliamount amount measured in milli rates of base units
	 * @param microamount amount measured in micro rates of base units
	 * @param nanoamount amount measured in nano rates of base units
	 */
	public SIUnit(long baseamount, long milliamount, long microamount, long nanoamount) {
		addAmount(baseamount);
		addMilliAmount(milliamount);
		addMicroAmount(microamount);
		addNanoAmount(nanoamount);
	}
	
	/**
	 * Adds a specific amount of power to the current power value.
	 * @param nanoamount The amount of power to add measured in nano units 
	 */
	protected void addNanoAmount(long nanoamount) {
		value += nanoamount * Math.pow(10, adjustmentToBaseUnit - 9);
	}

	/**
	 * Subtract a specific amount of power from the current power value.
	 * @param nanoamount The amount of power to subtract measured in nano units 
	 */
	void subtractNanoAmount(long nanoamount) {
		value -= nanoamount * Math.pow(10, adjustmentToBaseUnit - 9);
	}

	/**
	 * Adds a specific amount of power to the current power value.
	 * @param microamount The amount of power to add measured in micro units 
	 */
	protected void addMicroAmount(long microamount) {
		value += microamount * Math.pow(10, adjustmentToBaseUnit - 6);
	}

	/**
	 * Subtract a specific amount of power from the current power value.
	 * @param microamount The amount of power to subtract measured in micro units 
	 */
	void subtractMicroAmount(long microamount) {
		value -= microamount * Math.pow(10, adjustmentToBaseUnit - 6);
	}

	/**
	 * Adds a specific amount of power to the current power value.
	 * @param milliamount The amount of power to add measured in milli units 
	 */
	protected void addMilliAmount(long milliamount) {
		value += milliamount * Math.pow(10, adjustmentToBaseUnit - 3);
	}

	/**
	 * Subtract a specific amount of power from the current power value.
	 * @param milliamount The amount of power to subtract measured in milli units 
	 */
	void subtractMilliAmount(long milliamount) {
		value -= milliamount * Math.pow(10, adjustmentToBaseUnit - 3);
	}

	/**
	 * Adds a specific amount of power to the current power value.
	 * @param baseamount The amount of power to add measured in base units 
	 */
	protected void addAmount(long baseamount) {
		value += baseamount * Math.pow(10, adjustmentToBaseUnit - 0);
	}

	/**
	 * Sets the specific amount of this SIUnit.
	 * @param baseamount The amount to set measured in base units 
	 */
	protected void setAmount(long baseamount) {
		value = (long) (baseamount * Math.pow(10, adjustmentToBaseUnit - 0));
	}
	
	/**
	 * Subtract a specific amount of power from the current power value.
	 * @param baseamount The amount of power to subtract measured in watt 
	 */
	void subtractAmount(long baseamount) {
		value -= baseamount * Math.pow(10, adjustmentToBaseUnit - 0);
	}

	/**
	 * Provides a clone of the current instance.
	 * @return a clone
	 */
	public SIUnit clone() {
		try {
			return (SIUnit) super.clone();
		} catch (CloneNotSupportedException ex) {
			/*
			SIUnit copy = new SIUnit();
			copy.value = this.value;
			return copy;
			*/
			throw new SimulationFailedException("SIUnit.clone() must be able to call super.clone()");
		}
	}

	/**
	 * Gets the internal value which represents this unit.
	 * Do not assume anything about this. 
	 * @return the internal value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * 
	 * @return a text representation which can easily be read
	 */
	public String getUserFriendlyString() {
		long amount, milliamount, microamount, nanoamount;
		
		long calcvalue = value;
		amount = (long) (calcvalue /  Math.pow(10, adjustmentToBaseUnit - 0));
		calcvalue -= amount * Math.pow(10, adjustmentToBaseUnit - 0);
		milliamount = (long) (calcvalue /  Math.pow(10, adjustmentToBaseUnit - 3));
		calcvalue -= milliamount * Math.pow(10, adjustmentToBaseUnit - 3);
		microamount = (long) (calcvalue /  Math.pow(10, adjustmentToBaseUnit - 6));
		calcvalue -= microamount * Math.pow(10, adjustmentToBaseUnit - 6);
		nanoamount = (long) (calcvalue /  Math.pow(10, adjustmentToBaseUnit - 9));
		calcvalue -= nanoamount * Math.pow(10, adjustmentToBaseUnit - 9);
		
		return amount + " " + siUnitName + ", "
			   + milliamount + " milli" + siUnitName + ", "
			   + microamount + " micro" + siUnitName + ", "
			   + nanoamount + " nano" + siUnitName;
	}

}
