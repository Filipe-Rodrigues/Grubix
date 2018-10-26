package br.ufla.dcc.grubix.xml;

public class ConfigurationRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8785789573862671053L;

	public ConfigurationRuntimeException(String message) {
		super(message);
	}
	
	public ConfigurationRuntimeException(String message, Exception e) {
		super(message, e);
	}
}
