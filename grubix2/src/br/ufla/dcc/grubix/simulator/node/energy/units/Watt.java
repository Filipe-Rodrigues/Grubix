/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.energy.units;


/**
 * @author Florian Rittmeier
 *
 */
public class Watt extends SIUnit {
	
	/**
	 * Default constructor, initalizing with 0 Watt.
	 */
	public Watt() {
		setAmount(0);
		siUnitName = "Watt";
	}

	/**
	 * Constructor initalizing using the specified value.
	 * All parameters are added up togehter.
	 * @param watt amount of consumption/production measured in watt
	 * @param milliwatt amount of consumption/production measured in milliwatt
	 * @param microwatt amount of consumption/production measured in microwatt
	 * @param nanowatt amount of consumption/production measured in nanowatt
	 */
	public Watt(long watt, long milliwatt, long microwatt, long nanowatt) {
		super(watt, milliwatt, microwatt, nanowatt);
		siUnitName = "Watt";
	}

	/**
	 * Adds some amount of Watt to this Watt.
	 * @param pw amount of Watt
	 */
	public void addWatt(Watt pw) {
		this.value += pw.value;
	}
	
	/**
	 * Substracts some amount of Watt from this Watt.
	 * @param pw amount of Watt
	 */
	public void substractWatt(Watt pw) {
		this.value -= pw.value;
	}

	
	/**
	 * Calculates how much energy (in Joule)
	 * corresponds to this production/consumption (in Watt)
	 * of a specified amount of time.
	 * @param seconds amount of time
	 * @return energy for designated time span
	 */
	public Joule getJoule(double seconds) {
		// XXX Nasty hack!
		return new Joule((long) (this.value * seconds));
	}
}
