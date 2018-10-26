package br.ufla.dcc.grubix.xml;

/**
 * Exception used while configuration or to indicate missing or wrong configuration.
 * values
 * @author dmeister
 *
 */
@SuppressWarnings("serial")
public class ConfigurationException extends Exception {

	/**
	 * Constructs the exception with a message only.
	 * If an cause exception is available, please use
	 * the other ctor.
	 * @param message failure message
	 */
	public ConfigurationException(String message) {
		super(message);
	}
	
	/**
	 * Contructs the exception with a message and a cause exception.
	 * @param message failure message
	 * @param e cause exception e.g. NumberFormatException
	 */
	public ConfigurationException(String message, Exception e) {
		super(message, e);
	}
}
