/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
 ********************************************************************************/

package br.ufla.dcc.grubix.xml;

/**
 * Class as container for all XMLTags used during logging the SIMULATION.
 * 
 * 
 * @author Andreas Kumlehn
 */
public final class XMLTags {
	
	/**-------Codigo Jesimar----------*/	
	
	public static final String COMMUNICATIONRADIUS = "communicationradius";
	
	public static final String INFO = "info";
	
	public static final String ISMOBILE = "ismobile";
	
	public static final String DESCRIPTION = "description";
	
	public static final String WRITE = "write";
	
	public static final String NODETYPE = "nodetype";
	
	/**-------Fim codigo Jesimar---------*/

	/** Common tags. */
	public static final String X = "x";
	/** Common tags. */
	public static final String Y = "y";
	/** Common tags. */
	public static final String ID = "id";
	/** Common tags. */
	public static final String GLOBAL = "simulatorlog";

	/** Configuration tags. */
	public static final String CONFIGURATION = "configuration";
	/** Configuration tags. */
	public static final String FIELD = "field";
	/** The simulation time tag. */
	public static final String SIMULATIONTIME = "simulationtime";
	/** Configuration tags. */
	public static final String STEPSPERSECOND = "stepspersecond";
	/** Configuration tags. */
	public static final String PHYSMODEL = "physicalmodel";
	/** Configuration tags. */
	public static final String MOVEMENTMANAGER = "movementmanager";

	/** NodeList in configuration. */
	public static final String NODELIST = "nodes";
	/** NodeList in configuration. */
	public static final String NODECOUNT = "count";
	/** NodeList in configuration. */
	public static final String LAYERS = "layers";
	/** NodeList in configuration. */
	public static final String PHYSICALLAYER = "physical";
	/** NodeList in configuration. */
	public static final String MACLAYER = "mac";
	/** NodeList in configuration. */
	public static final String LOGLINKLAYER = "loglink";
	/** NodeList in configuration. */
	public static final String NETWORKLAYER = "network";
	/** NodeList in configuration. */
	public static final String APPLICATIONLAYER = "application";

	/** PositionList in configuration. */
	public static final String POSITIONLIST = "positions";
	/** PositionList in configuration. */
	public static final String POSITION = "position";

	/** Simulation tags. */
	public static final String SIMULATION = "simulationrun";
	/** Simulation tags. */
	public static final String ENQUEUEEVENT = "enqueue";
	/** Simulation tags. */
	public static final String DEQUEUEEVENT = "dequeue";
	/** Simulation tags. */
	public static final String TIMESTAMP = "time";
	/** Simulation tags. */
	public static final String TYPE = "type";
	/** Simulation tags. */
	public static final String RECEIVERID = "receiverid";
	/** Simulation tags. */
	public static final String MOVE = "move";
	/** Simulation tags. */
	public static final String TOLAYER = "tolayer";
	/** Simulation tags. */
	public static final String TODEVICE = "todevice";
	/** Simulation tags. */
	public static final String SENDERID = "senderid";
	/** Simulation tags. */
	public static final String SENDERLAYER = "senderlayer";
	/** Simulation tags. */
	public static final String SENDERDEVICE = "senderdevice";
	/** Simulation tags. */
	public static final String ENVELOPERECEIVERID = "internreceiverid";
	/** Simulation tags. */
	public static final String ENVELOPERECEIVERLAYER = "internreceiverlayer";
	/** Simulation tags. */
	public static final String ENVELOPERECEIVERDEVICE = "internreceiverdevice";
	/** Simulation tags. */
	public static final String DATA = "data";
	/** Simulation tags. */
	public static final String ENCLOSEDEVENTID = "enclosedeventid";
	/** Simulation tags. */
	public static final String NODESTATE = "nodestate";
	/** Simulation tags. */
	public static final String LINKSTATE = "linkstate";
	/** Simulation tags. */
	public static final String MESSAGE = "message";

	/** Statistic tags. */
	public static final String STATISTIC = "statistic";
	/** Statistic tags. */
	public static final String STATISTICS = "statistics";
	/** Statistic tags. */
	public static final String VALUE = "value";
	/** Statistic tags. */
	public static final String AXES = "axes";
}
