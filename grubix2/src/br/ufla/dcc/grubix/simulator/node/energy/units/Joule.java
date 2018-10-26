/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.energy.units;


/**
 * This class is used to store and work with power values for the energy management and calculation
 * of the simulation.
 * @author Florian Rittmeier
 *
 */
public class Joule extends SIUnit {

	/* 
	 * TODO Contructor Joule(long) is not a good idea. Someone might assume that amount
	 * 		specifies full joule. If we provide a Constructor for full joule its parameter
	 * 		should be of type double.
	 */

	
	/**
	 * This is the default constructor.
	 */
	public Joule() {
		setAmount(0);
		siUnitName = "Joule";
	}

	/**
	 * Constructor initalizing using the specified value.
	 * All parameters are added up togehter.
	 * @param joule amount of consumption/production measured in joule
	 * @param millijoule amount of consumption/production measured in millijoule
	 * @param microjoule amount of consumption/production measured in microjoule
	 * @param nanojoule amount of consumption/production measured in nanojoule
	 */
	public Joule(long joule, long millijoule, long microjoule, long nanojoule) {
		super(joule, millijoule, microjoule, nanojoule);
		siUnitName = "Joule";
	}

	/**
	 * This constructor should only be used
	 * if amount is returned by getValue() of another instance.
	 * @param amount amount of energy in internal units
	 */
	public Joule(long amount) {
		this.value = amount;
		siUnitName = "Joule";
	}
	
	/**************************************************************************
	 * functions for simply adding and substracting values in specific ranges *
	 **************************************************************************/

	/**
	 * Adds some amount of Joule to this Joule.
	 * @param pw amount of Joule
	 */
	public void addJoule(Joule pw) {
		this.value += pw.value;
	}

	/**
	 * Substracts some amount of Joule from this Joule.
	 * @param pw amount of Joule
	 */
	public void substractJoule(Joule pw) {
		this.value -= pw.value;
	}
	
}
