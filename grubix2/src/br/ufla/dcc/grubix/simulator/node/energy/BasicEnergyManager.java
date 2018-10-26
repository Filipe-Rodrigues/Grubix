/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.energy;

import java.util.LinkedList;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.DeviceType;
import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.event.EnergyManagerWakeUpCallEvent;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.node.devices.transceiver.GenericTransceiver;
import br.ufla.dcc.grubix.simulator.node.energy.units.Joule;
import br.ufla.dcc.grubix.simulator.node.energy.units.SIUnit;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;
import br.ufla.dcc.grubix.simulator.util.Pair;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * TODO Implement interface so that all devices get the possibility to be informed by power level callbacks
 * TODO Check if nodes can receive packets/events from already powered off nodes
 */ 

/**
 * This class represents the energy system of a node.
 * It separates between power providing and power consuming devices of the node.
 * 
 * @author Florian Rittmeier
 */
public class BasicEnergyManager implements EnergyManager {

	/**
	 * The node this energy manager belongs to.
	 */ 
	private Node theNode;

	/**
	 * Instance of the logging system for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(BasicEnergyManager.class.getName());

	/**
	 * A reference to the configuration of the current system run.
	 */
	private Configuration config;
	
	/**
	 * All suppliers which follow an energy reservoir characteristic.
	 */
	@ShoXParameter
	private EnergyReservoirSupplier[] supplierER;
	
	/**
	 * All suppliers which follow an power scavenging characteristic,
	 * therefore they provide a specific amount of energy each "round".
	 */
	@ShoXParameter
	private PowerScavengingSupplier[] supplierPS;
	
	/**
	 * Primary controller of the node. 
	 */	
	@ShoXParameter
	private PowerConsumer consumerController;
	
	/**
	 * Primary communication device (network interface card) of the node. 
	 */
	@ShoXParameter(defaultClass = br.ufla.dcc.grubix.simulator.node.devices.transceiver.NoTransceiver.class)
	private GenericTransceiver consumerTransceiver;
	
	/**
	 * All other devices which might consume energy.
	 */
	@ShoXParameter
	private PowerConsumer[] consumerOtherDevices;

	/**
	 * Time of last update of energy reservoirs.
	 */
	private double lastEnergyUpdateTime;
	
	/**
	 * Flag if EnergyManager has passed the situation
	 * where not enough energy is left.
	 */
	private boolean isEmpty = false;
	
	/**
	 * Flag if EnergyManager has changed to suspend
	 * mode and now records some request for later
	 * execution/calculation.
	 */
	private boolean isSuspended = false;
	
	/**
	 * Flag if EnergyManager was suspended and therefore
	 * has not processed a EnergyManagerWakeUpCall.
	 */
	private boolean missedWakeUpCall = false;
	
	/**
	 * Used to track which was the last EnergyManagerWakeUpCall being send.
	 * That one is the only valid one, all other are outdated.
	 */
	private EventId lastSendWakeUpCallId;
	
	/**
	 * This is the banner which is prepended to all log / debug output of the BasicEnergyManager.
	 */
	private String energyManagerBanner = "";
	
	/**
	 * A list of callbacks, which can be used to be informed when
	 * a certain level of power is reached.
	 */
	private LinkedList<Pair<Long, PowerLevelCallback> > registeredCallbacks =
								new LinkedList<Pair<Long, PowerLevelCallback> >();

	/**
	 * A list of information about all active (or not yet removed) one time consumptions.
	 */
	private LinkedList<OneTimeConsumptionInformation> activeOneTimeConsumptionInformation =
								new LinkedList<OneTimeConsumptionInformation>();
	
	/**
	 * feedback object for PowerConsumers.
	 */
	private PowerConsumerFeedback powerConsumerFeedback = new PowerConsumerFeedback() {

		public void beforeUpdate() {
			updateUsedEnergy();
		}

		public void afterUpdate() {
			updatePowerCallBacks();
		}

		public void registerPowerLevelCallback(long level, PowerLevelCallback clb) {
			// forward request
			BasicEnergyManager.this.registerPowerLevelCallback(level, clb);
		}

		public void announceOneTimeConsumption(Watt consumption, double duration) {
			updateUsedEnergy();
			
			OneTimeConsumptionInformation information = new OneTimeConsumptionInformation(config);
			information.setConsumption(consumption);
			information.setDuration(duration);
			activeOneTimeConsumptionInformation.add(information);
			
			updatePowerCallBacks();
		}

		public NodeId getNode() {
			return BasicEnergyManager.this.theNode.getId();
		}
	};

	
	/**
	 * This is the default constructor.
	 */
	public BasicEnergyManager() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerPowerLevelCallback(long level, PowerLevelCallback clb) {
		
		// restrict values
		if (level > 100) {
			level = 100;
		}
		if (level < 0) {
			level = 0;
		}
		
		LOGGER.debug(this.energyManagerBanner
					 + "Adding EnergyManager callback to call back at level " + level);
		registeredCallbacks.add(new Pair<Long, PowerLevelCallback>(level, clb));
		updatePowerCallBacks();
	}


	/**
	 * {@inheritDoc}
	 */
	public void checkPowerLevelCallbacks() {
		LinkedList<Pair<Long, PowerLevelCallback> > thisTimeFiredCallbacks =
								new LinkedList<Pair<Long, PowerLevelCallback> >();
		
		LOGGER.debug(this.energyManagerBanner 
					 + "checkPowerLevelCallbacks() called at step "
					 + config.getKernel().getCurrentTime());
		
		/* If node resp. energy manager is suspended,
		 * keep in mind that a check regarding WakeUpCalls
		 * has to be performed.
		 */
		if (isSuspended) {
			missedWakeUpCall = true;
			return;
		}
		
		// update the energy reservoirs
		updateUsedEnergy();
		
		long level = getPowerLevel();
	
		// check callbacks and collect those which have to fire
		LinkedList<PowerLevelCallback> collectedCallbacks = new LinkedList<PowerLevelCallback>();
		
		for (Pair<Long, PowerLevelCallback> clbpair : registeredCallbacks) {
			
			if (level <= clbpair.first) {
				// execute the callback
				collectedCallbacks.add(clbpair.second);
				thisTimeFiredCallbacks.add(clbpair);
			}
			
		}
		
		/* callbacks which will fired, will not be fired again
		 * and have to be removed from the list before beeing fired,
		 * because other wise we get stuck in a loop...
		 */
		for (Pair<Long, PowerLevelCallback> clbpair : thisTimeFiredCallbacks) {
			LOGGER.debug(this.energyManagerBanner
						  + "Removing already fired callback at level " + clbpair.first);
			
			registeredCallbacks.remove(clbpair);
		}
		
		// and now, fire all collected callbacks
		for (PowerLevelCallback callback : collectedCallbacks) {
			callback.performed(theNode, level);
		}
		
		if (!isEmpty) {
			updatePowerCallBacks();
		}
	}

	/**
	 * Removes OneTimeConsumptionInformation whos enddate is now or already passed.
	 */
	private void removeOldOneTimeConsumptionInformation() {
		LinkedList<OneTimeConsumptionInformation> outdated = new LinkedList<OneTimeConsumptionInformation>();
		double currentTime = config.getKernel().getCurrentTime();
		
		for (OneTimeConsumptionInformation information : activeOneTimeConsumptionInformation) {
			if (information.endtime <= currentTime) {
				outdated.add(information);
			}
		}
		
		for (OneTimeConsumptionInformation information : outdated) {
			activeOneTimeConsumptionInformation.remove(information);
		}
	}
	
	/**
	 * This method determines which callback will be raised first
	 * and estimates when to check for that.
	 * If no callback is registered, checks when node runs out of power.
	 */
	private void updatePowerCallBacks() {
		LOGGER.debug(this.energyManagerBanner 
					 + "updatePowerCallBacks() called at step "
					 + config.getKernel().getCurrentTime());
		
		removeOldOneTimeConsumptionInformation();
		
		/*
		 * for printing debug information
		 */
		if (LOGGER.isDebugEnabled()) {

			Pair<Joule, Joule> erSupData = getSumOfERSupplierData();
			Joule curLevel = erSupData.first;
			Joule maxCap = erSupData.second;
			
			LOGGER.debug(this.energyManagerBanner
						 + "curLevel(sum): " + curLevel.getUserFriendlyString());
			LOGGER.debug(this.energyManagerBanner
					 	 + "maxCap(sum): " + maxCap.getUserFriendlyString());
			LOGGER.debug(this.energyManagerBanner
					 	 + "remainRequest(sum): "
					 	 + getPowerRequiredFromEnergyReservoirSuppliers().getUserFriendlyString());
			LOGGER.debug(this.energyManagerBanner
						 + "relLevel(sum): " + getPowerLevel());
		}

		double wakeupStep = Double.NaN;
		double callbackLevelTime = Double.NaN;
		double powerOffTime = Double.NaN;
		double oneTimeConsumptionEndsTime = Double.NaN;
		
		callbackLevelTime = getWakeUpTimeForNextCallback();
		powerOffTime = getWakeUpTimeForPowerDown();
		oneTimeConsumptionEndsTime = getWakeUpTimeForEndOfNextOneTimeConsumption();
	
		if (!Double.isNaN(callbackLevelTime)) {
			wakeupStep = callbackLevelTime;
		}
		if (!Double.isNaN(powerOffTime) && (Double.isNaN(wakeupStep) || powerOffTime < wakeupStep)) {
			wakeupStep = powerOffTime;
		}
		if (!Double.isNaN(oneTimeConsumptionEndsTime)
			&& (Double.isNaN(wakeupStep) || oneTimeConsumptionEndsTime < wakeupStep)) {
			wakeupStep = oneTimeConsumptionEndsTime;
		}

		if (Double.isNaN(wakeupStep)) {
				
			LOGGER.info(this.energyManagerBanner 
						+ "UpdatePowerCallBacks stopped, "
						+ "no remaining request or pending change at step " + config.getKernel().getCurrentTime());
				
			return;
		}
	
		double remainingSteps = wakeupStep - config.getKernel().getCurrentTime();
		
		sendWakeupEvent(remainingSteps);
	}

	/**
	 * Calculates at which simulation time the next one time consumption ends.
	 * @return the simulation step or Double.NaN if no one time consumption is registered
	 */
	private double getWakeUpTimeForEndOfNextOneTimeConsumption() {
		double wakeUpTime = Double.NaN;
		
		for (OneTimeConsumptionInformation information : activeOneTimeConsumptionInformation) {
			if (Double.isNaN(wakeUpTime) || wakeUpTime > information.getEndtime()) {
				wakeUpTime = information.getEndtime();
			}
		}
		
		return wakeUpTime;
	}

	/**
	 * Calculates at which simulation step the next callback has to be fired.
	 * @return the simulation step or Double.NaN if node will never power down
	 */
	private double getWakeUpTimeForPowerDown() {
		long availableEnergy = getSumOfERSupplierData().first.getValue(); // curLevel
		long requiredEnergy = getAveragePowerConsumption().getJoule(1).getValue()
						      + getActiveOneTimeConsumption().getJoule(1).getValue()
							  - getSumOfPSSupplierData().getValue();
		
		// node will never power down
		if (requiredEnergy == 0) {
			return Double.NaN;
		}
		
		double seconds = (double) availableEnergy / requiredEnergy;
		
		return config.getKernel().getCurrentTime() + config.getSimulationSteps(seconds);
	}

	/**
	 * Calculates at which simulation step the next callback has to be fired.
	 * @return the simulation step or Double.NaN if no callback registered or no request from energy reservoirs 
	 */
	private double getWakeUpTimeForNextCallback() {
		long highestlevel = -1;  
		
		for (Pair<Long, PowerLevelCallback> clbpair : registeredCallbacks) {
			if (clbpair.first > highestlevel) {
				highestlevel = clbpair.first;
			}
		}

		if (highestlevel != -1) {
			// levels till interesting level is reached
			double deltaLevel = getPowerLevel() - highestlevel;
			
			if (deltaLevel < 0.0) {
				/*
				 * Looks like we have missed a callback, wake up asap.
				 * 
				 * This first looks like a problem, but there might be
				 * consumers registering a callback after the level has
				 * already been reached and in this case this is a legal
				 * condition.
				 * 
				 * XXX At least I hope thats true.
				 */ 
				return config.getKernel().getCurrentTime();
			}
			
			// request per second
			long remainReq = getPowerRequiredFromEnergyReservoirSuppliers().getValue();
			
			// return no wakeup time, cause we will not run out of reservoir energy
			if (remainReq == 0) {
				return Double.NaN;
			}

			double powerPerPercent = (double) getSumOfERSupplierData().second.getValue() / 100;

			double remainingLevelTime = (deltaLevel * powerPerPercent  / remainReq);
			
			return config.getKernel().getCurrentTime() + config.getSimulationSteps(remainingLevelTime);
		} else {
			return Double.NaN;
		}
	}

	/**
	 * This method provides a unique way of checking state of energy manager
	 * in a specified offset in simulation steps.
	 * @param offsetInSteps offset in simulation steps, when to check for raising callbacks again
	 */
	private void sendWakeupEvent(double offsetInSteps) {
		EnergyManagerWakeUpCallEvent wakeup = new EnergyManagerWakeUpCallEvent(theNode.getId(), offsetInSteps);
		lastSendWakeUpCallId = wakeup.getId();

		LOGGER.debug(this.energyManagerBanner
				     + "Sending WakeUpEvent for calling back in "
				     + config.getSeconds(offsetInSteps) + " seconds " 
					 + "(" + offsetInSteps + " steps)");
		
		SimulationManager.enqueue(wakeup, DeviceType.POWERMANAGEMENT);
	}

	/**
	 * Updates EnergyReservoirSuppliers fill state according
	 * to the average power consumption per second.
	 * This may switch of the node if no more power is left.
	 */
	private void updateUsedEnergy() {
		double curTime = config.getKernel().getCurrentTime();
		double diffSeconds = config.getSeconds(curTime - lastEnergyUpdateTime);
		
		lastEnergyUpdateTime = curTime;
		Joule thePowerFromERS = getPowerRequiredFromEnergyReservoirSuppliers(); 
		
		/*
		 * Check if we did not have been run out of power already
		 */
		if (thePowerFromERS.getValue() > 0 && getSumOfERSupplierData().first.getValue() == 0) {
			switchTheNodeOff();			
		}
		
		/*
		 * Reduce the energy reservoirs
		 */
		double rawEnergyToSubstract = diffSeconds * thePowerFromERS.getValue();
		long energyToSubstract;
		
		/* if the amount of energy is too small to be greater than zero
		 * set it to the smallest possible energy value which makes a difference (1 nJ).
		 */ 
		if (rawEnergyToSubstract > 0d && rawEnergyToSubstract < 1d) {
			energyToSubstract = 1;
		} else {
			energyToSubstract = (long) rawEnergyToSubstract;
		}
		
		Joule amount = new Joule(energyToSubstract);

		genericUpdateUsedEnergy(amount);
	}

	/**
	 * Call this method to switch this energy manager and therefore its node off. 
	 */
	private void switchTheNodeOff() {
		// switch the node off
		this.isEmpty = true;
		LOGGER.info("Node " + theNode.getId().asInt() + " ran out of energy "
				    + "(time: " + SimulationManager.getInstance().getCurrentTime() + ")!");
	}

	/**
	 * Updates EnergyReservoirSuppliers fill state according
	 * to the given amount of consumption.
	 * This may switch of the node if no more power is left.
	 * @param amount the usage this function call should satisfy
	 */
	private void genericUpdateUsedEnergy(Joule amount) {
		for (EnergyReservoirSupplier erSupplier : supplierER) {
			amount = erSupplier.reduceEnergy(amount);
		}
		
		// if there is some amount of energy which could not be satisfied by the energy reservoirs
		if (amount.getValue() > 0) {
			switchTheNodeOff();			
		}
	}
	
	/**
	 * Determines the amount of Energy which is required
	 * from EnergyReservoirSuppliers each second.
	 * @return the calculated amount of Energy
	 */
	private Joule getPowerRequiredFromEnergyReservoirSuppliers() {
		Watt suppliedPS = new Watt();
		for (PowerScavengingSupplier psSupplier : supplierPS) {
			suppliedPS.addWatt(psSupplier.currentOutput());
		}
		
		Watt restReq = new Watt();
		restReq.addWatt(getAveragePowerConsumption());
		restReq.addWatt(getActiveOneTimeConsumption());
		restReq.substractWatt(suppliedPS);
		
		if (restReq.getValue() < 0) {
			restReq = new Watt();
		}

		return restReq.getJoule(1.0);
	}

	/**
	 * Calculates the sum of consumption of all active one time consumptions.
	 * @return the sum of all one time consumptions 
	 */
	private Watt getActiveOneTimeConsumption() {
		Watt activeConsumption = new Watt();

		for (OneTimeConsumptionInformation information : activeOneTimeConsumptionInformation) {
			activeConsumption.addWatt(information.getConsumption());
		}
		
		return activeConsumption;
	}

	/**
	 * Sums up the maximum average output of all power scavenging suppliers of this node.
	 * @return combined maximum average output
	 */
	private Watt getSumOfPSSupplierData() {
		Watt suppliedPS = new Watt();
		
		for (PowerScavengingSupplier psSupplier : supplierPS) {
			suppliedPS.addWatt(psSupplier.currentOutput());
		}
		
		return suppliedPS;
	}
	
	/**
	 * Returns information of all EnergyReservoirSuppliers
	 * combined to one information, as if it would be
	 * one EnergyReservoirSupplier.
	 * @return the aggregated information
	 */
	private Pair<Joule, Joule> getSumOfERSupplierData() {
		Joule maxCap = new Joule();
		Joule curLevel = new Joule();
		
		for (EnergyReservoirSupplier erSupplier : supplierER) {
			maxCap.addJoule(erSupplier.getCapacity());
			curLevel.addJoule(erSupplier.getCurrentLevel());
		}

		return new Pair<Joule, Joule>(curLevel, maxCap);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getPowerLevel() {
		Pair<Joule, Joule> erSupData = getSumOfERSupplierData();
		
		SIUnit curLevel = erSupData.first;
		SIUnit maxCap =  erSupData.second;
		
		/* INFO This is a design decision, cause choosing zero
		 * 		instead would make less sense for the default
		 * 	 	case without any energy management.
		 */
		if (maxCap.getValue() == 0) {
			return 100;
		}
		
		if (curLevel.getValue() > maxCap.getValue()) {
			throw new SimulationFailedException("maximum capacity of energy reservoirs "
					+ "can never be less then current level");
		}
		
		long curLevelInternal = curLevel.getValue();
		long maxCapInternal = maxCap.getValue();
		
		return (long) Math.floor(((double) curLevelInternal / maxCapInternal) * 100);	
	}
	
	/**
	 * Determines the average amount of power consumed by all components.
	 * @return average consumption
	 */
	private Watt getAveragePowerConsumption() {
		Watt avgConsumption = new Watt();
	
		if (consumerController != null) {
			avgConsumption.addWatt(consumerController.getAveragePowerConsumption());
		}
		
		if (consumerTransceiver != null) {
			avgConsumption.addWatt(consumerTransceiver.getAveragePowerConsumption());
		}
		
		for (PowerConsumer consumer : consumerOtherDevices) {
			avgConsumption.addWatt(consumer.getAveragePowerConsumption());
		}

		return avgConsumption;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
		LOGGER.debug(this.energyManagerBanner + "Initalizing. " 
					 + "(time: " + SimulationManager.getInstance().getCurrentTime() + ")");

		this.config = configuration;
		lastEnergyUpdateTime = config.getKernel().getCurrentTime();
		
		if (consumerController != null) {
			LOGGER.debug(this.energyManagerBanner
						 + "Using microcontroller: " + consumerController.toString());
			consumerController.setFeedbackConnection(powerConsumerFeedback);
			consumerController.initConfiguration(configuration);
		}
		
		if (consumerTransceiver != null) {
			LOGGER.debug(this.energyManagerBanner
						 + "Using Transceiver: " + consumerTransceiver.toString());
			consumerTransceiver.setFeedbackConnection(powerConsumerFeedback);
			consumerTransceiver.initConfiguration(configuration);
		}

		for (PowerConsumer consumer : consumerOtherDevices) {
			LOGGER.debug(this.energyManagerBanner
						 + "Using device: " + consumer.toString());
			consumer.setFeedbackConnection(powerConsumerFeedback);
			consumer.initConfiguration(configuration);
		}
		
		for (EnergyReservoirSupplier supplier : supplierER) {
			LOGGER.debug(this.energyManagerBanner
						 + "Using EnergyReservoir: " + supplier.toString());
			supplier.initConfiguration(configuration);
		}
		
		for (PowerScavengingSupplier supplier : supplierPS) {
			LOGGER.debug(this.energyManagerBanner
						 + "Using PowerScavengingSup.: " + supplier.toString());
			supplier.initConfiguration(configuration);
		}
				
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
		LOGGER.debug(this.energyManagerBanner
					 + "Switching from suspend state " + isSuspended + " to " + suspended);
		
		if (isSuspended != suspended) {
			isSuspended = suspended;
			
			if (isSuspended) {
				/* if we update used energy now, we can later simply
				 * set the lastEnergyUpdateTime to the new time and
				 * everything will be fine.
				 * 
				 * NOTE This might shut down the node in rare situations.
				 */
				
				updateUsedEnergy();
				
				changeSuspendStateOfDevices(suspended);
				
			} else {
				/* come back to life
				 */
				
				// just ignore the time we were suspended
				lastEnergyUpdateTime = config.getKernel().getCurrentTime();
				
				changeSuspendStateOfDevices(suspended);				

				/* if there were any EnergyManagerWakeUpCalls,
				 * process them now as one EnergyManagerWakeUpCall
				 */
				if (missedWakeUpCall) {
					checkPowerLevelCallbacks();
					missedWakeUpCall = false;
				}
			}
		}
	}
	
	/**
	 * This method is called to change the suspend state of all
	 * devices this energy manager manages, when the energy manager
	 * is sure that it should do that.
	 * @param suspended defines the suspend state for the devices
	 */
	private void changeSuspendStateOfDevices(boolean suspended) {
		
		if (this.consumerController != null) {
			this.consumerController.suspend(suspended);
		}
		
		if (this.consumerTransceiver != null) {
			this.consumerTransceiver.suspend(suspended);
		}

		for (PowerConsumer consumer : this.consumerOtherDevices) {
			consumer.suspend(suspended);
		}
		
		for (PowerSupplier supplier : this.supplierER) {
			supplier.suspend(suspended);
		}
		
		for (PowerSupplier supplier : this.supplierPS) {
			supplier.suspend(suspended);
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void startup() {
		updatePowerCallBacks();		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValidWakeUpCall(EnergyManagerWakeUpCallEvent event) {
		return this.lastSendWakeUpCallId.equals(event.getId());
	}
	
	
	/****************************************************************
	 * 							Accessor methods					*
	 ****************************************************************/
	
	/**
	 * @return the primary controller of the node
	 */
	public PowerConsumer getConsumerController() {
		return consumerController;
	}

	/**
	 * @param consumerCPU the new primary controller of the node
	 */
	public void setConsumerController(PowerConsumer consumerCPU) {
		this.consumerController = consumerCPU;
	}

	/**
	 * @return the primary transceiver of the node
	 */
	public GenericTransceiver getConsumerTransceiver() {
		return consumerTransceiver;
	}

	/**
	 * @param consumerTransceiver the new primary transceiver of the node
	 */
	public void setConsumerTransceiver(GenericTransceiver consumerTransceiver) {
		this.consumerTransceiver = consumerTransceiver;
	}

	/**
	 * @param node the node this energy manager belongs to
	 */
	public void setNode(Node node) {
		this.theNode = node;
		this.energyManagerBanner = "Node " + theNode.getId() + " [EnergyManager] ";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return isEmpty;
	}

}
