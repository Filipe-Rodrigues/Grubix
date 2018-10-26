/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.user.os;

import br.ufla.dcc.grubix.xml.AbstractConfigurable;

/**
 * @author dmeister
 *
 */
public abstract class AbstractService extends AbstractConfigurable implements Service {

	/**
	 * service type.
	 */
	private final ServiceType type;
	
	/**
	 * service description.
	 */
	private ServiceDescription description;
    
    /**
     * id to identify the service.
     */
    private int id;
    
	/**
	 * Constuctor.
	 * 
	 * @param type service type
	 */
	public AbstractService(ServiceType type) {
		this.type = type;
	}
	
	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.node.user.os.Service#getServiceDescription()
	 */
	public ServiceDescription getServiceDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.node.user.os.Service#getServiceType()
	 */
	public ServiceType getServiceType() {
		return type;
	}

	/**
	 * starts a service by initialzing the id and the service description properties and than call the start method
	 * with out these parameters.
	 * 
	 * This start method is final. Subservices should override the start method without id and description.
	 * 
	 * @param id service id
	 * @param serviceDescription description of the service
	 * @param operatingSystem operating system
	 * @param endpoint endpoint the service is bound on.
	 * @exception ServiceException thrown if the service should be started in a running mode.
	 */
	public final void start(int id, ServiceDescription serviceDescription, 
			OperatingSystemServiceFasade operatingSystem, ServiceEndpoint endpoint) throws ServiceException {
		this.id = id;
		this.description = serviceDescription;
		this.start(operatingSystem, endpoint);
	}
	
	/**
	 * starts a service.
	 * 
	 * @param operatingSystem operating system
	 * @param endpoint endpoint the service is bound on.
	 * @exception ServiceException thrown if the service should be started in a running mode.
	 */
	public void start(OperatingSystemServiceFasade operatingSystem, ServiceEndpoint endpoint) throws ServiceException {
		// do nothing
	}

    /**
     * {@inheritDoc}
     */
    public int getServiceId() {
            return this.id;
    }

	/**
	 * @param description the description to set
	 */
	protected void setServiceDescription(ServiceDescription description) {
		this.description = description;
	}

}
