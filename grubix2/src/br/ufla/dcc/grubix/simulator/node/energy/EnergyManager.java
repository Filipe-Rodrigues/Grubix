/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.event.EnergyManagerWakeUpCallEvent;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.Configurable;

/**
 * Interface for the Energy Management component.
 * 
 * @author dmeister
 *
 */
public interface EnergyManager extends Configurable {

	/**
	 * Registers a callback function
	 * {@see br.ufla.dcc.grubix.simulator.energy.PowerLevelCallback#performed()}
	 * which is called when the level of power is equal to or below that level.
	 * Each registered callback is only called once.
	 * 
	 * @param level a value between 0 and 100
	 * @param clb the callback function
	 */
	void registerPowerLevelCallback(long level,
			PowerLevelCallback clb);
	 
	/**
	 * This method is called when the Node detects that
	 * some "interesting" level of power may be reached,
	 * using and event/message send around.
	 * If you are a node call isValidWakeUpCall() first.
	 */
	void checkPowerLevelCallbacks();
	
	/**
	 * Sets the node for the energy manager.
	 * @param node node of the energy manager
	 */
	void setNode(Node node);
	
	/**
	 * inits the configuration of the energy manager.
	 * 
	 * @param configuration configuration of the system run.
	 */
	void initConfiguration(Configuration configuration);

	/**
	 * @return true if node is power off, false if node is still operating
	 */
	boolean isEmpty();
	
	/**
	 * suspends the energy manager.
	 * 
	 * @param suspended new suspension state
	 */
	void suspend(boolean suspended);
	
	/**
	 * Called by Node when the simulation starts.
	 */
	void startup();

	/**
	 * This is used by a Node to check if it should really inform
	 * a energy manager of a certain EnergyManagerWakeupCallEvent.
	 * This should be used to avoid unnessary calculations resulting
	 * in outdated WakeUpCalls.
	 * @param event the event to be checked for validity
	 * @return true if event is valid, else returns false
	 */
	boolean isValidWakeUpCall(EnergyManagerWakeUpCallEvent event);

	/**
	 * Determines the current power level of a EnergyManager.
	 * Usually this should be the combined fill state of all EnergyReservoirSuppliers.
	 * 
	 * Algorithms using this information should not query it, when they like to check
	 * for certain levels to be reached. Instead they should register a PowerLevelCallback
	 * using {@link #registerPowerLevelCallback(long, PowerLevelCallback)}, which helps to prevent possible
	 * issues regarding rounding errors.
	 *  
	 * @return fill state in percent between 0 and 100;
	 * 		   if maximum capacity is zero, a EnergyManager has always to return 100.
	 * @see #registerPowerLevelCallback(long, PowerLevelCallback) 
	 */
	long getPowerLevel();
	
}
