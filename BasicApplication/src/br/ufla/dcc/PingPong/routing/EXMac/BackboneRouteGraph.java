package br.ufla.dcc.PingPong.routing.EXMac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BackboneRouteGraph {

	private static final float INFINITE = Float.MAX_VALUE;
	private static Map<String, BackboneRouteGraph> instances = new HashMap<String, BackboneRouteGraph>();

	private float[][] adjacencyMatrix;
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
				adjacencyMatrix[i][j] = Float.parseFloat(line.split(" ")[2]);
			}
			br.close();
		} catch (Exception e) {
			System.err.println("I couldn't read your file \"" + fileName + "\" :(");
		}
	}

	private void emptyAdjMatrix() {
		adjacencyMatrix = new float[vertexCount][vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			for (int j = 0; j < vertexCount; j++) {
				adjacencyMatrix[i][j] = -1;
			}
		}
	}

	private byte getMinimumVertex(boolean[] mst, float[] key) {
		float minKey = INFINITE;
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

	public Entry<Float, Queue<Byte>> getshortestPath(int source, int target) {
		return getshortestPath((byte) source, (byte) target);
	}
	
	public Entry<Float, Queue<Byte>> getshortestPath(byte source, byte target) {
		boolean[] shortestPathTree = new boolean[vertexCount];
		float[] distances = new float[vertexCount];
		byte[] parents = new byte[vertexCount];
		if (!startsOnZero) {
			source--;
			target--;
		}
		for (int i = 0; i < vertexCount; i++) {
			shortestPathTree[i] = false;
			distances[i] = INFINITE;
			parents[i] = -1;
		}
		distances[source] = 0;
		byte u;
		while ((u = getMinimumVertex(shortestPathTree, distances)) > -1) {
			shortestPathTree[u] = true;
			for (byte v = 0; v < vertexCount; v++) {
				if (adjacencyMatrix[u][v] >= 0) {
					if (shortestPathTree[v] == false && adjacencyMatrix[u][v] != INFINITE) {
						float newKey = adjacencyMatrix[u][v] + distances[u];
						if (newKey < distances[v]) {
							distances[v] = newKey;
							parents[v] = u;
						}
					}
				}
			}
		}
		Queue<Byte> path = new LinkedList<Byte>();
		fillPathQueue(path, parents, target);
		Entry<Float, Queue<Byte>> shortestPathToTarget = new Entry<Float, Queue<Byte>>
		(distances[target], path);
		System.out.println("PATH: " + path);
		System.out.println("PATH DISTANCE: " + distances[target]);
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
