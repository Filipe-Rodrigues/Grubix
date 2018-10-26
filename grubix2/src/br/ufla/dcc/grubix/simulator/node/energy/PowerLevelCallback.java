package br.ufla.dcc.grubix.simulator.node.energy;

/**
 * This interface can be used by algorithms/layers to be called
 * back by the EnergyManager of a Node when a specific power level
 * is reached.
 * 
 * @author Florian Rittmeier
 */
public interface PowerLevelCallback {
	/**
	 * This method is called by the EnergyManager of a Node
	 * to inform that a certain level of power was reached.
	 * 
	 * @param node the node whose energy manager called this method
	 * @param level the power level which forced the EnergyManager to call this method
	 */
	 void performed(br.ufla.dcc.grubix.simulator.node.Node node, long level);
}
