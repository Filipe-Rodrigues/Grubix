package br.ufla.dcc.grubix.simulator.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.util.Pair;

/**
 * ESSA CLASSE SERVE APENAS COMO SUPORTE AOS TESTES DO EXMac!!!
 * SE VOCÊ NÃO FOR TRABALHAR COM ELA, EXCLUA ESSE ARQUIVO OU MODIFIQUE COMO DESEJAR!!!
 * 
 * */
public class BackboneConfigurationManager implements Serializable {

	public static final int USAMAC_CONFIG = 0;
	public static final int MXMAC_CONFIG = 1;
	private static final long serialVersionUID = 1L;
	
	private SortedMap<NodeId, BackboneConfiguration> allNodesConfigurations;
	private Queue<Integer> testNodes;
	private static BackboneConfigurationManager singleton;

	private static boolean usingFile;
	
	private BackboneConfigurationManager() {
		allNodesConfigurations = new TreeMap<NodeId, BackboneConfiguration>();
		testNodes = new LinkedList<Integer>();
	}
	
	public static BackboneConfigurationManager getInstance(int protocolType) {
		if (singleton == null) {
			loadConfiguration(protocolType);
		}
		return singleton;
	}
	
	public static void close(boolean saveConfig, int protocolType) {
		if (saveConfig) {
			saveConfiguration(protocolType);
		}
	}
	
	private static void saveConfiguration(int protocolType) {
		String fileName = "config" + ((protocolType == USAMAC_CONFIG) ? ("USAMac") : ("MXMac")) + ".dat";
		try {
			File file = new File(System.getProperty("user.dir") + "/backbone_config/" + fileName);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(singleton);
			oos.close();
			//System.err.println("SUCESSO!! OBJETO DE CONFIGURAÇÃO SALVO!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void loadConfiguration(int protocolType) {
		String fileName = "config" + ((protocolType == USAMAC_CONFIG) ? ("USAMac") : ("MXMac")) + ".dat";
		try {
			if (usingFile) {
				FileInputStream fis = new FileInputStream(
						new File(System.getProperty("user.dir") + "/backbone_config/" + fileName));
				ObjectInputStream ois = new ObjectInputStream(fis);
				singleton = (BackboneConfigurationManager) ois.readObject();
				ois.close();
				//loadTestNodes();
				//System.err.println("SUCESSO!! Objeto de configuração foi carregado!");
			} else {
				throw new Exception("Not using file!");
			}
		} catch (Exception e) {
			System.err.println("N�o encontrei uma configura��o de backbone, inicializando uma nova...");
			System.err.println("Exception thrown: " + e.getMessage());
			singleton = new BackboneConfigurationManager();
		}
	}
	
	private static void loadTestNodes() {
		try {
			FileInputStream fid = new FileInputStream(
					new File(System.getProperty("user.dir") + "/backbone_config/application_targets.dat"));
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public List<NodeId> loadBackboneNeighborsUSAMac(NodeId myId) {
		if (allNodesConfigurations.containsKey(myId)) {
			return allNodesConfigurations.get(myId).backboneNeighborsUSAMac;
		}
		return new ArrayList<NodeId>();
	}
	
	public List<NodeId> loadBackboneNeighborsMXMacType1(NodeId myId) {
		if (allNodesConfigurations.containsKey(myId)) {
			return allNodesConfigurations.get(myId).backboneNeighborsMXMacType1;
		}
		return new ArrayList<NodeId>();
	}
	
	public List<NodeId> loadBackboneNeighborsMXMacType2(NodeId myId) {
		if (allNodesConfigurations.containsKey(myId)) {
			return allNodesConfigurations.get(myId).backboneNeighborsMXMacType2;
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
		allNodesConfigurations.get(myId).backboneNeighborsUSAMac.add(neighborId);
	}
	
	public void addBackboneNeighbor(NodeId myId, NodeId neighborId, int bbChannel) {
		ensureNodeRegistration(myId);
		allNodesConfigurations.get(myId).addBackboneNeighborMXMac(neighborId, bbChannel);
	}
	
	public void setBackboneNodeLabel(NodeId myId, byte label) {
		ensureNodeRegistration(myId);
		allNodesConfigurations.get(myId).label = label;
	}
	
	public void setBackboneNodeChannel(NodeId myId, int channel) {
		ensureNodeRegistration(myId);
		allNodesConfigurations.get(myId).backboneChannel = channel;
	}
	public int getBackboneNodeChannel(NodeId myId) {
		if (amIBackbone(myId)) {
			return allNodesConfigurations.get(myId).backboneChannel;
		}
		return -1;
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
