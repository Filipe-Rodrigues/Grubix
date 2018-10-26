package br.ufla.dcc.PingPong.testing;

public class SingleNodeDebugger {
	private int csNumber;
	private boolean isBackbone;
	private boolean enabled;
	
	public SingleNodeDebugger() {
		csNumber = 0;
		this.isBackbone = false;
		enabled = false;
	}
	
	public void setBackbone(boolean isBackbone) {
		this.isBackbone = isBackbone;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void countCS() {
		if (enabled) {
			csNumber++;
		}
	}
	
	public void printStats() {
		if (isBackbone)
		System.err.println("CS count:\t" + csNumber);
	}
}
