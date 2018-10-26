package br.ufla.dcc.grubix.simulator.util.topology;

import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class TopologyConfig implements Configurable{

	@ShoXParameter(description="X-Coordinate of upperLeft coordinate of Topplogy", defaultValue="0")
	private int x;
	@ShoXParameter(description="Y-Coordinate of upperLeft coordinate of Topplogy", defaultValue="0")
	private int y;
	@ShoXParameter(description="width of Topplogy", defaultValue="0")
	private int width;
	@ShoXParameter(description="height of Topplogy", defaultValue="0")
	private int height;
	@ShoXParameter(description="mean height of the topology", required=true)
	private double baseHeight;
	@ShoXParameter(description="initial number of hotspots", required=true)
	private  int numHotspot;
	@ShoXParameter(description="maximal amplitude of hotspot", required=true)
	private  double maxHeightOfHotspot;
	@ShoXParameter(description="half of the width of the square influenced by the hotspots", required=true)
	private  int spreizwert;
	//TODO auf auflösung umbauen
	@ShoXParameter(description="number of intervalls the complete area is divided to", defaultValue="100")
	private  int numInterval;
	@ShoXParameter(description="factor for to influence the changes per second", required=true)
	private double alternationFactor = 0.05;
	@ShoXParameter(description="seed for random generator", defaultValue="0")
	private int seed;
	
	public void init() throws ConfigurationException {
	
		
	}

	public double getAlternationFactor() {
		return alternationFactor;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	public int getHeight() {
		return height;
	}

	public double getMaxHeightOfHotspot() {
		return maxHeightOfHotspot;
	}

	public int getNumHotspot() {
		return numHotspot;
	}

	public int getNumInterval() {
		return numInterval;
	}

	public int getSpreizwert() {
		return spreizwert;
	}

	public int getWidth() {
		return width;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public long getSeed() {
		return seed;
	}
	
	

}
