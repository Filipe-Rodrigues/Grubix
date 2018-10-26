/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event.user;

import java.util.Vector;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;




/**
 * @author edipig
 *
 */
public class AlarmRespondPacket extends ApplicationPacket {

	/**
	 * The payload of the an AlarmRespondPacket is composed by the alarm identifier:
	 * 0: the location (x,y) where the sensor that issued the alarm is located
	 * 1: a timestamp in which the alarm was issued 
	 */
	private Vector payload = new Vector(2);
	
	/**
	 * Class constructor
	 * @param sender
	 * @param receiver
	 */
	public AlarmRespondPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
	}
	
	public void setPayload(Vector v){
		payload = v;
	}
	
	public Vector getPayload(){
		return payload;
	}

}
