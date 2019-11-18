package br.ufla.dcc.PingPong.routing.EXMac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import br.ufla.dcc.grubix.simulator.Position;

public class BackboneRouteGraph {

	private static final double INFINITE = Double.MAX_VALUE;
	private static Map<String, BackboneRouteGraph> instances = new HashMap<String, BackboneRouteGraph>();

	private Position[][] adjacencyMatrix;
	private byte vertexCount;
	private byte edgeCount;
	private boolean directed;
	private boolean startsOnZero;

	public static BackboneRouteGraph getInstance(String key) {
		if (!instances.containsKey(key)) {
			instances.put(key, new BackboneRouteGraph(key));
		}
		return instances.get(key);
	}

	private BackboneRouteGraph(String fileName) {
		try {
			File file = new File(System.getProperty("user.dir") + "/backbone_config/" + fileName);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			directed = Boolean.parseBoolean(br.readLine().split(" ")[1]);
			startsOnZero = Boolean.parseBoolean(br.readLine().split(" ")[1]);
			String line = br.readLine();
			vertexCount = Byte.parseByte(line.split(" ")[1]);
			edgeCount = Byte.parseByte(line.split(" ")[2]);
			emptyAdjMatrix();
			while ((line = br.readLine()) != null) {
				int i = Integer.parseInt(line.split(" ")[0]);
				int j = Integer.parseInt(line.split(" ")[1]);
				if (!startsOnZero) {
					i--;
					j--;
				}
				adjacencyMatrix[i][j] = new Position(Double.parseDouble(line.split(" ")[2]), Double.parseDouble(line.split(" ")[3]));
			}
			br.close();
		} catch (Exception e) {
			System.err.println("I couldn't read your file \"" + fileName + "\" :(");
		}
	}

	private void emptyAdjMatrix() {
		adjacencyMatrix = new Position[vertexCount][vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			for (int j = 0; j < vertexCount; j++) {
				adjacencyMatrix[i][j] = null;
			}
		}
	}

	private byte getMinimumVertex(boolean[] mst, double[] key) {
		double minKey = INFINITE;
		byte vertex = -1;
		for (byte i = 0; i < vertexCount; i++) {
			if (mst[i] == false && minKey > key[i]) {
				minKey = key[i];
				vertex = i;
			}
		}
		return vertex;
	}

	/*
	private void printDijkstra(byte start, byte[] distances, byte[] parents) {
		if (!startsOnZero) start++;
		System.err.println("DJIKSTRA ON NODE #" + start + ":");
		for (int i = 0; i < vertexCount; i++) {
			System.err.print("Dist from #" + ((!startsOnZero) ? (i + 1) : (i)) + ": ");
			System.err.print(distances[i]);
			System.err.print(" ~~~~ Parent: #");
			System.err.println(((!startsOnZero) ? (parents[i] + 1) : (parents[i])));
		}
	}
	*/
	
	private void fillPathQueue(Queue<Byte> queue, byte[] parents, byte currentIndex) {
		if (currentIndex == -1) {
			return;
		}
		fillPathQueue(queue, parents, parents[currentIndex]);
		queue.add((byte) ((startsOnZero) ? (currentIndex) : (currentIndex + 1)));
	}

	public Entry<Double, Queue<Byte>> getshortestPath(int source, int target, Position enter, Position exit) {
		return getshortestPath((byte) source, (byte) target, enter, exit);
	}
	
	public Entry<Double, Queue<Byte>> getshortestPath(byte source, byte target, Position enter, Position exit) {
		boolean[] shortestPathTree = new boolean[vertexCount];
		double[] distances = new double[vertexCount];
		byte[] parents = new byte[vertexCount];
		Position[] enterPositions = new Position[vertexCount];
		if (!startsOnZero) {
			source--;
			target--;
		}
		for (int i = 0; i < vertexCount; i++) {
			shortestPathTree[i] = false;
			distances[i] = INFINITE;
			parents[i] = -1;
			enterPositions[i] = null;
		}
		distances[source] = 0;
		enterPositions[source] = enter;
		byte u;
		while ((u = getMinimumVertex(shortestPathTree, distances)) > -1) {
			shortestPathTree[u] = true;
			for (byte v = 0; v < vertexCount; v++) {
				if (adjacencyMatrix[u][v] != null) {
					if (shortestPathTree[v] == false) {
						double newKey = enterPositions[u].getDistance(adjacencyMatrix[u][v]) + distances[u];
						if (newKey < distances[v]) {
							distances[v] = newKey;
							enterPositions[v] = adjacencyMatrix[u][v];
							parents[v] = u;
						}
					}
				}
			}
		}
		Queue<Byte> path = new LinkedList<Byte>();
		double totalDistance = distances[target] + enterPositions[target].getDistance(exit);
		fillPathQueue(path, parents, target);
		Entry<Double, Queue<Byte>> shortestPathToTarget = new Entry<Double, Queue<Byte>>
		(totalDistance, path);
		System.out.println("PATH: " + path);
		System.out.println("PATH DISTANCE: " + totalDistance);
		//printDijkstra(source, distances, parents);
		return shortestPathToTarget;
	}

	public void printAdjMatrixOnConsole() {
		System.err.println("DIRECTED: " + directed);
		System.err.println("ZERO_START: " + startsOnZero);
		System.err.println("MATRIX:\n");
		for (int i = 0; i < vertexCount; i++) {
			for (int j = 0; j < vertexCount; j++) {
				System.err.print(adjacencyMatrix[i][j] + "\t");
			}
			System.err.println();
		}
	}

}
