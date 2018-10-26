/**
 * 
 */
package br.ufla.dcc.PingPong.node;

/**
 * This enum is for the possible radio states of the radio.
 *  
 * @author Florian Rittmeier
 */
public enum RadioState {
	/** the radio is off and not listening. */
	OFF,
	
	/** the radio is on and may receive packets. */
	LISTENING,
	
	/** the radio is currently in receive mode, receiving a packet. */
	RECEIVING,
	
	/** currently (January 2008) a special state for MAC802.11DCF,
	 *  which is used as transit/warm up state to SENDING,
	 *  but in some cases there is also an additional semantics/meaning for it.
	 */
	WILL_SEND,
	
	/** the radio currently is in transmit mode, sending a packet. */ 
	SENDING

}
