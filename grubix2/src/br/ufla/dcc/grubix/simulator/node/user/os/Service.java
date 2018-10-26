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

import br.ufla.dcc.grubix.xml.Configurable;


/**
 * Base type for all Service implementations for the operating system.
 * 
 * The prevert way to implement a service is to inherit from AbstractService.
 * 
 * A service is not unequal to a layer but a layer is under the control of the node and a service
 * is under the control of the operating systeme (or the service manager of the OS).
 * 
 * The lifecycle of a service consists of a series of start and stop calls, and events and packets
 * sended to the service.
 * With the start call the service the state of the service is change, so that it is able to receive
 * (ported) packets and events. A second start call in the running mode may cause an runtime exception. 
 * The stop call releases the binding of the service to the service endpoint. The service handles no requests
 * after a stop call and should release all resources. Multiple stop calls in a row should cause no damage.
 * 
 * In most cases services should be created with the fabric method createServiceInstance of a matching
 * service description class.
 * 
 * Every service should have a matching service description. The semantics are the following:
 * - A node is configured to run all services for which a description is configured in the service manager.
 * - A node is able to run all services which is configured and the isRunnableOn method returns true. This doesn't mean
 * that the service runs all the time, nor does it mean that the service runs sometime. this control is under the
 * responsibility of the service manager which coordinates the starting and stopping or services.
 * - A node can start all services that are able to run.
 * 
 * @author dmeister
 * @See {@link ServiceDescription}
 */
public interface Service extends PortedPacketReceiver, Configurable {

	/**
	 * starts a service.
	 * 
	 * @param id service id
	 * @param serviceDescription description of the service
	 * @param operatingSystem operating system
	 * @param endpoint endpoint the service is bound on.
	 * @exception ServiceException thrown if the service should be started in a running mode.
	 */
	void start(int id, ServiceDescription serviceDescription, OperatingSystemServiceFasade operatingSystem, 
			ServiceEndpoint endpoint) throws ServiceException;

	/**
	 * stops a service. This should release the binding to the endpoint and free all resources.
	 * The service should not handle any new requests.
	 */
	void stop();
	
	/**
	 * Gets the ServiceType of the Service. 
	 * @return the ServiceType of the service.
	 */
	ServiceType getServiceType();
	
	/**
	 * Gets the {@link ServiceDescription} of this {@link Service}. 
	 * @return {@link ServiceDescription} the {@link ServiceDescription} of this {@link Service}.
	 */
	ServiceDescription getServiceDescription();
    
    /**
     * Gets the id of the {@link Service}.
     * @return int the id of the {@link Service}
     */
    int getServiceId();
}
