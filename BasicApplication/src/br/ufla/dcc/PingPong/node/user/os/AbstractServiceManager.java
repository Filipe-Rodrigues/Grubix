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
package br.ufla.dcc.PingPong.node.user.os;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.log4j.Logger;


import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.Port;
import br.ufla.dcc.grubix.simulator.node.user.os.BindingException;
import br.ufla.dcc.grubix.simulator.node.user.os.OperatingSystemLayer;
import br.ufla.dcc.grubix.simulator.node.user.os.OperatingSystemServiceFasade;
import br.ufla.dcc.grubix.simulator.node.user.os.PortMapper;
import br.ufla.dcc.grubix.simulator.node.user.os.Service;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceDescription;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceEndpoint;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceException;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceManager;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceType;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;



/**
 * Abstract service manager providing a base implementation that may be used by
 * several concrete service managers.
 * 
 * It provides a ways to configure the service description inside the GUI, and
 * methods the simplify the management of services, e.g. the startService method.
 * It doesn't provides a strategy when services should be started or stopped.
 * 
 * 
 * @author dmeister
 * 
 */
public abstract class AbstractServiceManager implements ServiceManager {

	/**
	 * Logger for the operating system.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(AbstractServiceManager.class);

	/**
	 * maps the service types to the descriptions.
	 */
	private final List<ServiceDescription> descriptionList;

	/**
	 * maps the service types to the running services.
	 */
	private final Multimap<ServiceType, Service> runningServices;

	/**
	 * operating system.
	 */
	private OperatingSystemLayer operatingSystem;

	/**
	 * reference to a port mapper (from the operating system).
	 */
	private PortMapper mapper;

	/**
	 * address of the operating system.
	 */
	private Address address;
	
	/**
	 * maps a service type the a service endpoint.
	 */
	private final Map<ServiceType, ServiceEndpoint> mapping;

	/**
	 * Facade for the operating system.
	 */
	private OperatingSystemServiceFasade osFacade;
	
	/**
	 * array of all service descriptions. Used only for configuration purposed.
	 * Consider using the descriptionMap or the getDescriptions method.
	 */
	@ShoXParameter(description = "services", name = "services")
	private ServiceDescription[] descriptionArray;
    
    /**
     * id for the services.
     */
    private int idCounter = 0;
    
	/**
	 * Constructor for the abstract service manager.
	 */
	public AbstractServiceManager() {
		descriptionList = new ArrayList<ServiceDescription>();
		runningServices = Multimaps.newHashMultimap();
		mapping = new HashMap<ServiceType, ServiceEndpoint>();
		mapper  = new PortMapper();

	}

	/**
	 * Called by the ConfigurableFactory after setting the configured parameter.
	 * 
	 * @throws ConfigurationException
	 *             thrown if configuration is invalid.
	 */
	public void init() throws ConfigurationException {
		for (ServiceDescription description : descriptionArray) {
			ServiceType type = description.getType();
			for (ServiceDescription serviceDescription : descriptionList) {
				if (type == null) {
					continue; // no type that collision possible
				}
				// check for double type
				ServiceType currentType = serviceDescription.getType();
				if (currentType != null && currentType == type) {
					throw new ConfigurationException(
							"double defined service type");
				}
			}
			
			// valid service => add to list
			descriptionList.add(description);
		}
	}

	/**
	 * returns a collection of services with a given type.
	 * 
	 * @param type service type used as filter
	 * @return collection of services with that type.
	 */
	public Collection<Service> getServices(ServiceType type) {
		return Collections.unmodifiableCollection(this.runningServices.get(type));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ServiceDescription getDescription(ServiceType type) {

		for (ServiceDescription serviceDescription : descriptionList) {
			if (serviceDescription.getType().equals(type)) {
				return serviceDescription;
				// only one ServiceDescription per ServiceType.
			}
		}
		return null;
	}

	/**
	 * gets all descriptions available. It is ensured that at most one
	 * description for every service type is returned. The Collection cannot be
	 * modified.
	 * 
	 * @return a list of all configured service descriptions
	 */
	protected Collection<ServiceDescription> getDescriptions() {
		return Collections.unmodifiableCollection(descriptionList);
	}

	/**
	 * starts a service. Checks if there is already a service of the type,
	 * creates an instance, and register it in runngingServices. This method is
	 * supposed to be overwritten. It calls containsKey and put on
	 * runningServices, but no other self methods.
	 * 
	 * Sets the {@link ServiceDescription} in the started {@link Service}. 
	 * Binds the {@link Port} to a {@link Service}.
	 * 
	 * @param serviceDescription
	 *            service description
	 * @return new service instance (start not called, start() should be called
	 *         after a binding from the subclass) TODO: May use a template
	 *         pattern
	 * @throws ServiceException
	 *             thrown when the service could not be started.
	 */
	public Service startService(ServiceDescription serviceDescription)
			throws ServiceException {
		
		try {
			ServiceType type  = serviceDescription.getType();
			Collection<Service> serviceList = runningServices.get(type);
				if (serviceList.size() > 0) {
					if (type != null) {
						for (Service service : serviceList) {

							if (service.getServiceType().equals(type)) {
								throw new ServiceException(
										"There is already a service of the given type");
							}
						}
					}
				}
			

			Service service = serviceDescription.createServiceInstance();

			Port port = serviceDescription.getDefaultPort();
			if (port == null) {
				port = mapper.bind(service);
			} else {
				mapper.bind(port, service);
			}
			
			ServiceEndpoint endpoint =  new ServiceEndpoint(this.address, port);
			mapping.put(serviceDescription.getType(), endpoint);
			
			runningServices.put(service.getServiceType(), service);
			service.start(idCounter++, serviceDescription, osFacade, endpoint);
			return service;
		} catch (BindingException e) {
			throw new ServiceException("Cannot start service " + serviceDescription, e);
		}
	}

	
	/**
	 * starts the service manager.
	 * 
	 * @param operatingSystem
	 *            operating system (layer)
	 * @throws ConfigurationException
	 *             thrown when more than one service description is configured
	 *             for the same type
	 * 
	 * @see br.ufla.dcc.PingPong.node.user.os.ServiceManager#
	 *      init(br.ufla.dcc.PingPong.node.user.os.BasicOperatingSystemLayer)
	 */
	public void start(OperatingSystemLayer operatingSystem)
			throws ConfigurationException {
		
		this.operatingSystem = operatingSystem;
		address = new Address(operatingSystem.getId(), LayerType.OPERATINGSYSTEM);
		this.mapper = operatingSystem.getPortMapper();
		this.osFacade = operatingSystem.createFasade();
		//this.mapping = new HashMap<ServiceType, ServiceEndpoint>();
	}

	/**
	 * lookup the local endpoint of a service of the given type.
	 * @param type a service type
	 * @return an service endpoint or null.
	 * @throws ServiceException exception thrown if no service for the type is configured.
	 * @see br.ufla.dcc.PingPong.node.user.os.ServiceManager
	 * #lookupService(br.ufla.dcc.PingPong.node.user.os.ServiceType)
	 */
	public ServiceEndpoint lookupService(ServiceType type) throws ServiceException {
		ServiceEndpoint endpoint = mapping.get(type);
		
		if (endpoint != null) {
			return endpoint;
		}
		throw new ServiceException("Unknown service type " + type);
	}

	
	/**
	 * gets operatingSystem.
	 * 
	 * @return current operatingSystem
	 */
	public OperatingSystemLayer getOperatingSystem() {
		return operatingSystem;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return a List with all services that run on the node.
	 * 
	 * @see br.ufla.dcc.PingPong.node.user.os.ServiceManager#getServiceList()
	 */
	public Collection<Service> getServiceList() {
		return Collections.unmodifiableCollection(runningServices.values());
	}

	/**
	 * Stops a {@link Service}.
	 * This means removing the {@link Service} from
	 * the runningService map. 
	 * 
	 * Unbinding of the portmapping of the {@link Service}.
	 * @param service
	 *            the {@link Service} to stop.
	 * @see br.ufla.dcc.PingPong.node.user.os.ServiceManager#stopService(br.ufla.dcc.PingPong.node.user.os.Service)
	 */
	public void stopService(Service service) {
 		LOGGER.debug("stop service " + service.getClass());
		
		ServiceType serviceType = service.getServiceType();
		Collection<Service> serviceList = runningServices.get(serviceType);
		serviceList.remove(service);
		
		mapper.unbind(service); 
		service.stop();
	}
	
	/**
	 * {@inheritDoc}
	 * @return
	 * @see br.ufla.dcc.PingPong.node.user.os.ServiceManager#getUsedPorts()
	 */
	public Set<Port> getUsedPorts() {
		return mapper.getUsedPorts();
	}
	
	/**
	 * {@inheritDoc}
	 * stops all running Services.
	 */
	public void stop() {
		for (Service s : runningServices.values()) {
			s.stop();
		}
	}
}
