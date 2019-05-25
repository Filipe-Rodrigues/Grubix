package br.ufla.dcc.grubix.simulator.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;

/**
 * ESSA CLASSE SERVE APENAS COMO SUPORTE AOS TESTES DO EXMac!!!
 * SE VOCÊ NÃO FOR TRABALHAR COM ELA, EXCLUA ESSE ARQUIVO OU MODIFIQUE COMO DESEJAR!!!
 * 
 * */
public class BackboneConfigurationManager implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private SortedMap<NodeId, BackboneConfiguration> allNodesConfigurations;
	private static BackboneConfigurationManager singleton;

	private static boolean usingFile;
	
	private BackboneConfigurationManager() {
		allNodesConfigurations = new TreeMap<NodeId, BackboneConfiguration>();
	}
	
	public static BackboneConfigurationManager getInstance() {
		if (singleton == null) {
			loadConfiguration();
		}
		return singleton;
	}
	
	public static void close(boolean saveConfig) {
		if (saveConfig) {
			saveConfiguration();
		}
	}
	
	private static void saveConfiguration() {
		try {
			File file = new File(System.getProperty("user.dir") + "/backbone_config/config.dat");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(singleton);
			oos.close();
			System.err.println("SUCESSO!! OBJETO DE CONFIGURAÇÃO SALVO!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void loadConfiguration() {
		try {
			if (usingFile) {
				FileInputStream fis = new FileInputStream(
						new File(System.getProperty("user.dir") + "/backbone_config/config.dat"));
				ObjectInputStream ois = new ObjectInputStream(fis);
				singleton = (BackboneConfigurationManager) ois.readObject();
				ois.close();
				System.err.println("SUCESSO!! Objeto de configuração foi carregado!");
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			System.err.println("Não encontrei uma configuração de backbone, inicializando uma nova...");
			singleton = new BackboneConfigurationManager();
		}
	}
	
	static void startup(boolean willUseFile) {
		usingFile = willUseFile;
	}
	
	public boolean amIBackbone(NodeId myId) {
		if (allNodesConfigurations.containsKey(myId)) {
			return allNodesConfigurations.get(myId).nextBackboneNode != null;
		}
		return false;
	}
	
	public NodeId getNextBackboneNode(NodeId myId) {
		if (amIBackbone(myId)) {
			return allNodesConfigurations.get(myId).nextBackboneNode;
		}
		return null;
	}
	
	public Position getBackboneDirection(NodeId myId) {
		if (amIBackbone(myId)) {
			return allNodesConfigurations.get(myId).direction;
		}
		return null;
	}
	
	public List<NodeId> loadBackboneNeighbors(NodeId myId) {
		if (allNodesConfigurations.containsKey(myId)) {
			return allNodesConfigurations.get(myId).backboneNeighbors;
		}
		return new ArrayList<NodeId>();
	}
	
	private void ensureNodeRegistration(NodeId myId) {
		if (!allNodesConfigurations.containsKey(myId)) {
			allNodesConfigurations.put(myId, new BackboneConfiguration());
		}
	}
	
	public void setNextBackboneNode(NodeId myId, NodeId nextNode, Position direction) {
		ensureNodeRegistration(myId);
		allNodesConfigurations.get(myId).nextBackboneNode = nextNode;
		allNodesConfigurations.get(myId).direction = direction;
	}
	
	public void addBackboneNeighbor(NodeId myId, NodeId neighborId) {
		ensureNodeRegistration(myId);
		allNodesConfigurations.get(myId).backboneNeighbors.add(neighborId);
	}
	
	public void setBackboneNodeLabel(NodeId myId, byte label) {
		ensureNodeRegistration(myId);
		allNodesConfigurations.get(myId).label = label;
	}
	
	public byte getBackboneNodeLabel(NodeId myId) {
		if (amIBackbone(myId)) {
			return allNodesConfigurations.get(myId).label;
		}
		return -1;
	}
	
	public void setNodeCycleSyncTiming(NodeId myId, double cycleTimeBase) {
		ensureNodeRegistration(myId);
		allNodesConfigurations.get(myId).cycleStart = cycleTimeBase;
	}
	
	public double getNodeCycleSyncTiming(NodeId myId) {
		if (amIBackbone(myId)) {
			return allNodesConfigurations.get(myId).cycleStart;
		}
		return -1;
	}

}
