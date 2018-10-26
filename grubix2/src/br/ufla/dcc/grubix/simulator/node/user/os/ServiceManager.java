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
package br.ufla.dcc.grubix.simulator.node.user.os;

import java.util.Collection;
import java.util.Set;

import br.ufla.dcc.grubix.simulator.event.Port;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;



/**
 * manages the services on a node.
 * 
 * Starts, stops them and is responsible for the lookup e.g. using the data
 * management.
 * 
 * @See {@link AbstractServiceManager}
 * @author dmeister
 *
 */
public interface ServiceManager extends Configurable {

	/**
	 * lookup the (possible local or nearest) endpoint of a service of the given type.
	 * 
	 * @param type a service type
	 * @return an service endpoint or null.
	 * @throws ServiceException exception thrown when an error with the
	 * service handling occurs.
	 */
	ServiceEndpoint lookupService(ServiceType type) throws ServiceException;

	/**
	 * inits the service manager.
	 * 
	 * @param operatingSystem operating system (layer)
	 * @throws ConfigurationException thrown when the configuration is invalid
	 */
	void start(OperatingSystemLayer operatingSystem) throws ConfigurationException;
	
	/**
	 * Stops the {@link Service}.
	 * 
	 * @param service
	 *            the service to stop.
	 */
	void stopService(Service service);
	
	/**
	 * Starts the {@link Service} represented by the {@link ServiceDescription}.
	 * @param serviceDescription the {@link ServiceDescription} to create a {@link Service} of.
	 * @return The created {@link Service}.
	 * @throws ServiceException Occurs if the {@link Service} could not be started. 
	 */
	Service startService(ServiceDescription serviceDescription)
			throws ServiceException;
	
	/**
	 * gets the list of (running) services registered on the service manager.
	 * 
	 * @return a list of services.
	 */
	Collection<Service> getServiceList();
	
	/**
	 * Gets all  {@link Port}s which are bind by some {@link Service}.
	 * @return Set<Port> a set of all {@link Port}s which are bind to some {@link Service}.
	 */
	Set<Port> getUsedPorts();
	
	/**
	 * gets the services of the given type on the given node.
	 * @return services
	 * @param type service type
	 */
	Collection<Service> getServices(ServiceType type);
	
	/**
	 * gets the available {@link ServiceDescription} for the {@link ServiceType}.
	 * @param type the {@link ServiceType} to request a {@link ServiceDescription} for.
	 * @return a {@link ServiceDescription} for a {@link Service} with the requested {@link ServiceType}.
	 */
	ServiceDescription getDescription(ServiceType type); 
	
	/**
	 * finalizes the ServiceManager at the end of the simulation.
	 */
	void stop();
}
