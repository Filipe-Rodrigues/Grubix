package br.ufla.dcc.grubix.simulator.util.topology;
import java.io.*;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.random.JavaRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;





public class Topology implements Configurable {
	
	@ShoXParameter(description="X-Coordinate of upperLeft coordinate of Topplogy", defaultValue="0")
	private int x;
	@ShoXParameter(description="Y-Coordinate of upperLeft coordinate of Topplogy", defaultValue="0")
	private int y;
	@ShoXParameter(description="width of Topplogy", required=true)
	private int width;
	@ShoXParameter(description="height of Topplogy", required=true)
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
	
		
	private RandomGenerator random;
	
	private double [][]points;
	private Vector3D hots[];
	
	private Topology() {
		;
	}
	
	private void init(int x, int y, int width, int height, double baseHeight,
			int numHotspot, double maxHeightOfHotspot, int spreizwert,  int numInterval, double alternationFactor,
			RandomGenerator random) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.baseHeight = baseHeight;
		
		this.numHotspot = numHotspot;
		this.maxHeightOfHotspot = maxHeightOfHotspot;
		this.spreizwert = spreizwert;
		this.numInterval = numInterval;
		this.alternationFactor = alternationFactor;
		
		this.random = random;
		this.hots = new Vector3D[numHotspot];
		initializeHotspots();
	}
	
	public Topology(int x, int y, int width, int height, double baseHeight,
			int numHotspot, double maxHeightOfHotspot, int spreizwert,  int numInterval, double alternationFactor,
			RandomGenerator random) {
		super();
		init(x, y, width, height, baseHeight, numHotspot, maxHeightOfHotspot,
				spreizwert, numInterval, alternationFactor, random);
	}

	public Topology(TopologyConfig tc) {
		super();
		int width, height;
		long seed;
		if (tc.getWidth() == 0) {
			width = (int) Configuration.getInstance().getXSize();
		} else {
			width = tc.getWidth();
		}
		
		if (tc.getHeight() == 0) {
			height = (int) Configuration.getInstance().getYSize();
		} else {
			height = tc.getHeight();
		}
		
		if(tc.getSeed() == 0) {
			seed = Configuration.getInstance().getRandomGenerator().getSeed();
		} else {
			seed = tc.getSeed();
		}
		init(tc.getX(), tc.getY(), width, height, tc.getBaseHeight(), tc.getNumHotspot(), tc.getMaxHeightOfHotspot(), 
				tc.getSpreizwert(), tc.getNumInterval(), tc.getAlternationFactor(), new JavaRandomGenerator((int) seed));
	}
	private void initializeHotspots() {
		for (int i = 0; i < numHotspot; i++) {
			hots[i ]= new Vector3D(random.nextDouble() * width, random.nextDouble() * height, 
				random.nextDouble() * maxHeightOfHotspot - maxHeightOfHotspot / 2);
		}
		
	}
	public void generateTerritory() {
		
		Cubic cubic[] = new Cubic[numHotspot];
		double  [][]helperPoints = new double [4][4];
		
		this.points = new double[numInterval + 1][numInterval + 1];
		
		
		for (int i=0; i < numHotspot; i++) {
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 4; k++){
					helperPoints[j][k] = 0;
				}
			}
			double scaledzCoord = this.hots[i].getZ() * spreizwert / width; 
			helperPoints[1][1] = scaledzCoord;
			helperPoints[1][2] = scaledzCoord;
			helperPoints[2][1] = scaledzCoord;
			helperPoints[2][2] = scaledzCoord;
			cubic[i] = new Cubic(Cubic.BEZIER,helperPoints);
		}
		
		
		for (int i = 0; i <= numInterval; i++) {
			for (int j = 0; j <= numInterval; j++){
				double xCoord = i / (double)numInterval * width;
				double yCoord = j / (double)numInterval * height;
				double zCoord = 0;
				for (int k = 0; k < numHotspot; k++) {
					if (xCoord >= hots[k].getX() - spreizwert && xCoord <= hots[k].getX() + spreizwert &&
							yCoord >= hots[k].getY() - spreizwert && yCoord <= hots[k].getY() + spreizwert) {
						double scaledxCoord = (xCoord - hots[k].getX()) / (2 * spreizwert) + 0.5;
						double scaledyCoord = (yCoord - hots[k].getY()) / (2 * spreizwert) + 0.5;
						zCoord += cubic[k].eval(scaledxCoord, scaledyCoord);
						points[i][j] = zCoord;
						//System.out.println(scaledxCoord + " " + scaledyCoord + " " + zCoord);
					}
				}
				
			}
			
		}
		
		
	}
	
	public void plot(int number) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("test"+number+".dat"));
		for (int i = 0; i <= numInterval; i++) {
			for (int j = 0; j <= numInterval; j++){
				double xCoord = i / (double)numInterval * width;
				double yCoord = j / (double)numInterval * height;
				double zCoord = points[i][j];
				bw.write(xCoord + " " + yCoord + " " + (baseHeight + zCoord) +"\n");
			}
			bw.write("\n");
		}
		bw.close();
	}
	public void alterTerritory(double second) {
		baseHeight += (random.nextDouble() - 0.5)  * baseHeight * alternationFactor  * second;
		for (int i = 0; i < numHotspot ; i++) {
			Vector3D delta = new Vector3D ((random.nextDouble() - 0.5 ) * alternationFactor * width * second,
					(random.nextDouble() - 0.5 ) * alternationFactor * width * second,
					(random.nextDouble() - 0.5 ) * alternationFactor * maxHeightOfHotspot * second);
			hots[i].add(delta);
		}
		//TODO alter numHotspots
		this.generateTerritory();
	}
	
	public double getValue(double x, double y) {
		x -= this.x;
		y -= this.y;
		//extrapolate value from the four neighbors with arithmetic average
		double value = 0;
		int i = 0;
		int j = 0;
		i = (int) Math.floor(x / width * numInterval);
		j = (int) Math.floor(y / height * numInterval);
		value += points[i][j] + baseHeight;
		
		i = (int) Math.floor(x / width * numInterval);
		j = (int) Math.ceil(y / height * numInterval);
		value += points[i][j] + baseHeight;
		
		i = (int) Math.ceil(x / width * numInterval);
		j = (int) Math.floor(y / height * numInterval);
		value += points[i][j] + baseHeight;
		
		i = (int) Math.ceil(x / width * numInterval);
		j = (int) Math.ceil(y / height * numInterval);
		value += points[i][j] + baseHeight;
		
		value /= 4;
		
		return value;
	}
	
	public static void main(String []args) throws IOException {
		//2134412
		Topology myCubic = new Topology(0, 0, 300, 300, 25, 10, 250, 80, 100, 0.0001, new JavaRandomGenerator(2134412));
		myCubic.generateTerritory();
		for (int i = 0; i < 10; i++) {
			myCubic.plot(i);
			myCubic.alterTerritory(1);
		}
	}
	public void init() throws ConfigurationException {
		this.hots = new Vector3D[numHotspot];
		initializeHotspots();
		
	}

}
