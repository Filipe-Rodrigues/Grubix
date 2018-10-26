package br.ufla.dcc.grubix.debug.compactlogging;

import java.io.IOException;
import java.util.Collection;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.EventEnvelope;
import br.ufla.dcc.grubix.simulator.movement.MovementManager;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.physical.PhysicalModel;
import br.ufla.dcc.grubix.xml.Configurable;



/**
 * The Logger for the ShoX project.
 * Each implementing type is free to realize it's own specific log format.
 * To log the configuration part successfully the following sequence of method calls must be quaranteed:
 * <code>startConfigurationPart(), logConfiguration(...), logNodeSetup(...), logNodePlacement(...), 
 * endConfigurationPart()</code>.  
 * 
 * @author mika
 *
 */
public interface ShoxLogger extends Configurable {

	/**
	 * Logs an event within the ShoX Simulation of type Enqueue.
	 * For information about this event please refer to the ShoX documentation.
	 * 
	 * @param eventenvelope The Event itself
	 * @param receiver Receiver Node of this Event
	 * @param priority An assigned priority to this Event, configurable in LiveFlter
	 */
	void logEnqueueEvent(EventEnvelope eventenvelope, NodeId receiver, int priority);
	
	/**
	 * Logs an event within the ShoX Simulation of type Dequeue.
	 * For information about this event please refer to the ShoX documentation.
	 * 
	 * @param time Simulationtime of the occurence of this Event
	 * @param event The Event which was dequeued from the global message queue
	 */
	void logDequeueEvent(double time, EventId event);

	/**
	 * Logs an event within the ShoX Simulation of type Move.
	 * For information about this event please refer to the ShoX documentation.
	 * 
	 * @param node Node which moved
	 * @param x New x coordinate
	 * @param y New y coordinate
	 * @param time Simulationtime when movement occured
	 * @param priority An assigned priority to this Event, configurable in LiveFlter
	 */
	void logMoveEvent(NodeId node, double x, double y, double time, int priority);

	/**
	 * Logs an event within the ShoX Simulation of type LinkState.
	 * For information about this event please refer to the ShoX documentation.
	 * 
	 * @param node1 One link's end 
	 * @param node2 Other link's end
	 * @param name The name of the state to be logged
	 * @param type Type of the following value
	 * @param value Value
	 */
	void logLinkStateEvent(NodeId node1, NodeId node2, String name, String type, String value);

	/**
	 * Logs an event within the ShoX Simulation of type NodeState.
	 * For information about this event please refer to the ShoX documentation.
	 * 
	 * @param node The ClassNode identifier which fired this event
	 * @param name The name of the state to be logged
	 * @param type The type of the parameter "value"
	 * @param value The value which is contained within this event
	 */
	void logNodeStateEvent(NodeId node, String name, String type, String value);
	
	/**
	 * Logs a text message, with a priority to support filtering during log file screening.
	 * 
	 * @param time The simulationtime
	 * @param sender Calling Node and Layer
	 * @param message The actual message to log
	 * @param priority A priortiy of the logged message, see {@link LogFilter} for priority levels
	 */
	void logMessage(double time, Address sender, String message, int priority);
	
	// ----------------------------------------------------------------------------------
	/**
	 * Begins the configuration tag part. <p> 
	 * Call this after {@link #initLogging(String, String, String)}
	 */
	void startConfigurationPart();
	
	/**
	 * bla bla. <p>
	 * Call this after {@link #startConfigurationPart()}
	 * 
	 * @param fieldXSize x dimension of the simulated area
	 * @param fieldYSize y dimension of the simulated area 
	 * @param stepsPerSecond simulation steps per simulated second
	 * @param simulationTime duration of simulation
	 * @param physicalModel implemtation of the {@link PhysicalModel} used in the simulation
	 * @param movementManager implementation of the {@link MovementManager} used in the simulation
	 */
	void logConfiguration(double fieldXSize, double fieldYSize, 
			int stepsPerSecond, long simulationTime, PhysicalModel physicalModel, 
			MovementManager movementManager, String description);
	
	/**
	 * bla bla. <p>
	 * Call this after {@link #logConfiguration(double, double, int, long, PhysicalModel, MovementManager)}
	 * 
	 * @param nodeCount amount of {@link Node}s simulated
	 * @param physical class implementing {@link PhysicalLayer}
	 * @param mac class implementing {@link MACLayer}
	 * @param loglink class implementing {@link LogLinkLayer}
	 * @param br.ufla class implementing {@link NetworkLayer}
	 * @param app class implementing {@link ApplicationLayer}
	 */
	void logNodeSetup(int nodeCount, Class< ? extends Layer> physical, 
			Class< ? extends Layer> mac, 
			Class< ? extends Layer> loglink, 
			Class< ? extends Layer> net, 
			Class< ? extends Layer> app);
	
	/**
	 * bla bla. <p>
	 * Call this after {@link #logNodeSetup(int, Class, Class, Class, Class, Class)}
	 * 
	 * @param nodes all nodes being simulated
	 */
	void logNodePlacement(Collection<Node> nodes);
	
	/**
	 * bla bla. <p>
	 * Call this after {@link #logNodePlacement(Collection)}
	 * 
	 * Ends the configuration tag part.
	 */
	void endConfigurationPart();

	// ----------------------------------------------------------------------------------	
	
	/**
	 * Logs statistic to the specified statistics file.
	 * 
	 * @param node The node calling this method
	 * @param layer The layer of the ClassNode node
	 * @param xAxisLabel Label for the x axis
	 * @param yAxisLabel Label for the y axis
	 * @param xValue x value
	 * @param yValue y value
	 */
	void logStatistics(NodeId node, LayerType layer, 
						String xAxisLabel, String yAxisLabel, 
						String xValue, String yValue);

	// ----------------------------------------------------------------------------------
	
	/**
	 * Initiates the logging. No logging is possible before a call to this method.
	 * 
	 * @param historyFile Name of the logfile for the history, if <code>null</code> or empty history logging is disabled
	 * @param statisticsFile Name of the logfile for statistics, if <code>null</code> or empty statistics logging is 
	 * disabled
	 * @param simulatorId A unique identification of the simulator using this logger to ensure the possibility of 
	 * running multiple simulations with logging enabled on the same machine simultaneously
	 * @throws IOException IOException while creating the logfiles  
	 */
	void initLogging(String historyFile, String statisticsFile, String simulatorId) throws IOException;
	
	/**
	 * Closes the logfiles and all open XML tags if XML logging is enabled.
	 */
	void finishLogging();
	
	// ----------------------------------------------------------------------------------
	
	/**
	 * Returns the identification of the simulator using this logger in order to distinguish log files.
	 * 
	 * @return Simulator identification
	 */
	String getSimulatorId();
}
