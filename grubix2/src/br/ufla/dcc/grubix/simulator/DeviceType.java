/**
 * 
 */
package br.ufla.dcc.grubix.simulator;

/**
 * Enum representing types of a devices.
 * 
 * This should be used for identifing the destination of a packet send
 * from one device/component (EnergyManager, NIC) to itself or another
 * device/component
 * 
 * This enum is build using the architecture of the LayerType enum. 
 *   
 * @author Florian Rittmeier
 *
 */
public enum DeviceType implements Comparable<DeviceType> {

	/**
	 * PowerManagement (EnergyManager).
	 */
	POWERMANAGEMENT(0, "Power Management", "pm"),
	
	/**
	 * NIC.
	 */
	NIC(1, "Network Interface Card", "nic");
	
	/** 
	 * String describing a device. 
	 */
	private final String description;
	
	/**
	 * Index of a device type.
	 * Should not be used outside this class.
	 * 
	 * INFO I'm not sure if we really need and ID,
	 * 		but I`ll leave it here, cause its easier to have
	 * 		one and not to use it 
	 */
	private final int index;
	
	/**
	 * Short name of the device type.
	 */
	private final String shortName;
	
	/**
	 * Returns the short name of the device type.
	 * @return a short name
	 */
	public String getShortName() {
		return shortName;
	}
    
    /**
     * Private constructor of the enum DeviceType.
     * 
     * @param idx The index of the created device type.
     * @param desc String description of the new device type.
     * @param shortName a short name of the device type
     */
    private DeviceType(int idx, String desc, String shortName) {
    	this.index = idx;
    	this.description = desc;
    	this.shortName = shortName;
    }
	
	/**
	 * @see java.lang.Object#toString()
	 * @return Description of the devicetype as String.
	 */
	@Override
	public String toString() {
		return description;
	}
	
}
