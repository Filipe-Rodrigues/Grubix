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

import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.event.Port;
import br.ufla.dcc.grubix.simulator.node.user.os.Service;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceException;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceType;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * This class describes the services configured on a node. It also creates instances of the service and checks if the
 * service is able to run on the node.
 * 
 * Every service should have a matching service description. The semantics are the following:
 * - A node is configured to run all services for which a description is configured in the service manager.
 * - A node is able to run all services which is configured and the isRunnableOn method returns true. This doesn't mean
 * that the service runs all the time, nor does it mean that the service runs sometime. this control is under the
 * responsibility of the service manager which coordinates the starting and stopping or services.
 * - A node can start all services that are able to run.
 * 
 * Note: In generell it is good practice to create the service instance with a configuration factory like the service
 * description itself. Since the service cannot be configured directly, the service description should contain all
 * parameters of the service and delegete the configured values to the factory of the service instance. The
 * RumorRouting implementation shows an example of this pattern.
 * 
 * @author dmeister
 * @see Service
 */
public abstract class ServiceDescription implements Configurable {

	/**
	 * tests the capabilities of a service to run on a given node.
	 * This method is supposed to be overwritten by subclasses.
	 * 
	 * @param node a node
	 * @return true or false
	 */
	public boolean isRunnableOn(Node node) {
		return true;
	}

	/**
	 * Called by the ConfigurableFactory after setting the configured
	 * parameter.
	 * 
	 * @throws ConfigurationException thrown if configuration is invalid.
	 */
	public void init() throws ConfigurationException {
		//nothing to do
	}

	/**
	 * creates a service instance.
	 * 
	 * @return a newly created service instance
	 * @throws ServiceException thrown when the creation of the instance fails
	 */
	public abstract Service createServiceInstance() throws ServiceException;

	/**
	 * gets the default port of the service.
	 * A default port of null means that any free port can be assigned. A default port which
	 * is not null, means that every other client may assure that the service runs on exactly that port.
	 * The configuration should fail if the port is not available on a node.
	 * 
	 * @return default port or null
	 */
	public Port getDefaultPort() {
		return null;
	}

	/**
	 * returns the service type.
	 * An operating system may apply the rule that only one service of a type is configured.
	 * 
	 * @return service type (should not be null)
	 */
	public abstract ServiceType getType();
}
