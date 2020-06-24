package br.ufla.dcc.PingPong.routing.MXMac;

import static br.ufla.dcc.PingPong.routing.MXMac.AuxiliarConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import br.ufla.dcc.PingPong.routing.USAMac.Entry;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.util.Pair;

public class MXMacGraphOperations {

	class LabelProperties {
		Position direction;
		double fieldProportion;
		
		public LabelProperties(Position dir, double proportion) {
			direction = dir;
			proportion = fieldProportion;
		}
	}
	
	private static final MXMacGraphOperations instance = new MXMacGraphOperations("heuristicMX.cfg", "labelProperties.cfg");
	
	private char[][] regions;
	private Map<Character, List<Integer>> regionLabels;
	private boolean directed;
	private Position[][] adjMatrix;
	private LabelProperties[] labelDirections;
	private int vertexCount;
	
	
	private MXMacGraphOperations(String graphFile, String labelFile) {
		try {
			startupAuxObs();
			readGraphFile(graphFile);
			readLabelFile(labelFile);
		} catch (FileNotFoundException e) {
			System.err.println("File not found at backbone_config directory!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException! Check the stack trace:");
			e.printStackTrace();
		}
	}
	
	public static MXMacGraphOperations getInstance() {
		return instance;
	}

	private void startupAuxObs() {
		regions = new char[3][3];
		regionLabels = new HashMap<Character, List<Integer>>();
		for (int i = 0; i < 9; i++) {
			regions[i / 3][i % 3] = (char) ('A' + i);
			regionLabels.put((char) ('A' + i), new ArrayList<Integer>());
		}
	}

	private void readGraphFile(String graphFile) throws NumberFormatException, IOException {
		File file = new File(System.getProperty("user.dir") + "/backbone_config/" + graphFile);
		FileReader fr = new FileReader(file);
		BufferedReader reader = new BufferedReader(fr);
		String line;
		String args[];
		while ((line = reader.readLine()) != null) {
			args = line.split(" ");
			if (args[0].contains("directed")) {
				directed = Boolean.parseBoolean(args[1]);
			} else if (args[0].contains("V/E")) {
				vertexCount = Integer.parseInt(args[1]);
				initAdjMatrix();
			} else if (args[0].matches("[0-9]+")){
				int s = Integer.parseInt(args[0]);
				int t = Integer.parseInt(args[1]);
				double xCoord = Double.parseDouble(args[2]) * MAX_X;
				double yCoord = Double.parseDouble(args[3]) * MAX_Y;
				Position intersection = new Position(xCoord, yCoord);
				adjMatrix[s][t] = intersection;
				if (!directed) adjMatrix[t][s] = intersection;
			}
		}
		reader.close();
	}

	private void readLabelFile(String labelFile) throws NumberFormatException, IOException {
		File file = new File(System.getProperty("user.dir") + "/backbone_config/" + labelFile);
		FileReader fr = new FileReader(file);
		BufferedReader reader = new BufferedReader(fr);
		String line;
		String args[];
		while ((line = reader.readLine()) != null) {
			args = line.split(" ");
			if (args[0].contains("[")) continue;
			else if (args[0].matches("[0-9]+")) {
				Position dir = getDirectionFromString(args[1]);
				double proportion = Double.parseDouble(args[2]);
				labelDirections[Integer.parseInt(args[0])] = new LabelProperties(dir, proportion);
			} else if (args[0].matches("[A-Z]")) {
				for (int i = 1; i < args.length; i++) {
					regionLabels.get(args[0].charAt(0)).add(Integer.parseInt(args[i]));
				}
			}
		}
		reader.close();
	}

	private Position getDirectionFromString(String param) {
		if (param.contains("LEFT")) return LEFT;
		if (param.contains("RIGHT")) return RIGHT;
		if (param.contains("UP")) return UP;
		if (param.contains("DOWN")) return DOWN;
		return null;
	}

	private void initAdjMatrix() {
		labelDirections = new LabelProperties[vertexCount];
		adjMatrix = new Position[vertexCount][vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			labelDirections[i] = null;
			for (int j = 0; j < vertexCount; j++) {
				adjMatrix[i][j] = null;
			}
		}
	}

	private char getRegionFromPosition(Position pos) {
		int x = (int) Math.floor(pos.getXCoord() * (3d / MAX_X));
		int y = (int) Math.floor(pos.getYCoord() * (3d / MAX_Y));
		x = (x < 3) ? ((x >= 0) ? (x) : 0) : (3);
		y = (y < 3) ? ((y >= 0) ? (y) : 0) : (3);
		return regions[x][y];
	}
	
	private int getNearestViableLabel(Position position, Position dirVector) {
		double pX = position.getXCoord(), pY = position.getYCoord();
		char region = getRegionFromPosition(position);
		List<Integer> labels = regionLabels.get(region);
		int nearestLabel = -1;
		double nearestDistance = Double.POSITIVE_INFINITY;
		for (int label : labels) {
			Position dir = labelDirections[label].direction;
			double propPos = labelDirections[label].fieldProportion;
			double distance = (dir.getXCoord() != 0) ? (propPos * MAX_Y - pY) : (propPos * MAX_X - pX);
			if (distance < nearestDistance) {
				double dirX = dir.getXCoord(), dirY = dir.getYCoord();
				double dirVX = dirVector.getXCoord(), dirVY = dirVector.getYCoord();
				if ((dirX != 0 && dirX * dirVX >= 0)
						|| (dirY != 0 && dirY * dirVY >= 0)) {
					nearestLabel = label;
					nearestDistance = distance;
				}
			}
		}
		return nearestLabel;
	}
	
	private Pair<Integer, Integer> getBestLabelPair(Position source, Position target, boolean goingFrom) {
		double sX = source.getXCoord(), sY = source.getYCoord();
		double tX = target.getXCoord(), tY = target.getYCoord();
		double moveDirX = tX - sX;
		double moveDirY = tY - sY;
		Position dirVector = new Position(moveDirX, moveDirY);
		int s = getNearestViableLabel(source, dirVector);
		int t = getNearestViableLabel(target, dirVector);
		if (s == -1 || t == -1) return null;
		return new Pair<Integer, Integer>(s, t);
	}
	
	private int getMinimumVertex(boolean[] mst, double[] key) {
		double minKey = Double.NEGATIVE_INFINITY;
		int vertex = -1;
		for (int i = 0; i < vertexCount; i++) {
			if (mst[i] == false && minKey > key[i]) {
				minKey = key[i];
				vertex = i;
			}
		}
		return vertex;
	}
	
	private void fillPathQueue(Queue<Integer> queue, int[] parents, int currentIndex) {
		if (currentIndex == -1) {
			return;
		}
		fillPathQueue(queue, parents, parents[currentIndex]);
		queue.add(currentIndex);
	}
	
	public Entry<Double, Queue<Integer>> getshortestPath(int source, int target, Position enter, Position exit) {
		boolean[] shortestPathTree = new boolean[vertexCount];
		double[] distances = new double[vertexCount];
		int[] parents = new int[vertexCount];
		Position[] enterPositions = new Position[vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			shortestPathTree[i] = false;
			distances[i] = Double.POSITIVE_INFINITY;
			parents[i] = -1;
			enterPositions[i] = null;
		}
		distances[source] = 0;
		enterPositions[source] = enter;
		int u;
		while ((u = getMinimumVertex(shortestPathTree, distances)) > -1) {
			shortestPathTree[u] = true;
			for (int v = 0; v < vertexCount; v++) {
				if (adjMatrix[u][v] != null) {
					if (shortestPathTree[v] == false) {
						double newKey = enterPositions[u].getDistance(adjMatrix[u][v]) + distances[u];
						if (newKey < distances[v]) {
							distances[v] = newKey;
							enterPositions[v] = adjMatrix[u][v];
							parents[v] = u;
						}
					}
				}
			}
		}
		Queue<Integer> path = new LinkedList<Integer>();
		double totalDistance = distances[target] + enterPositions[target].getDistance(exit);
		fillPathQueue(path, parents, target);
		Entry<Double, Queue<Integer>> shortestPathToTarget = new Entry<Double, Queue<Integer>>
		(totalDistance, path);
		//System.out.println("PATH: " + path);
		//System.out.println("PATH DISTANCE: " + totalDistance);
		//printDijkstra(source, distances, parents);
		return shortestPathToTarget;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}