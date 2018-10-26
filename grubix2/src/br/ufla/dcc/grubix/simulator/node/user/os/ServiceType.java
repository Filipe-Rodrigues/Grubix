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

/**
 * a type of a service.
 * From every type only one instance can run on a node at one time.
 * 
 * If the service is of no type specified here, we return null. If the ServiceType is null,
 * it has no limitation to one instance per node.
 * 
 * @author dmeister
 *
 */
public enum ServiceType {
	/**
	 * Service type for a distributed data management.
	 */
	DataManagement,

	/**
	 * Service type for the migration of services.
	 */
	ServiceMigration,
	
	/**
     * Service type for CO2 measure service.
     */
    CO2Sensor,
    
    /**
     * Service type for AirHumidity service.
     */
    AirHumiditySensor,
    
    /**
     * service type for the temperature service.
     */
    TemperatureSensor,
	
	/**
	 * Service type for AirCondition service.
	 */
	AirConditionService,
	
	/**
	 * Service type for a user-input device like a PDA.
	 */
	PDAService,
	
	/**
	 * Service type for an alarm syste.
	 */
	AlarmService;		
}
