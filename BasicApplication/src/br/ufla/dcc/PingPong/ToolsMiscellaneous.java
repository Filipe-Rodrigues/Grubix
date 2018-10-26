package br.ufla.dcc.PingPong;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

import br.ufla.dcc.PingPong.PaxMac.PaxMacConstants;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.physical.UnitDisc;


/** Classe que possui ferramentas para auxiliar no VisualGrubix e em alguns 
 * pontos da simulação 
 * 
 *  @author Gustavo Araújo
 *  @version 01/11/2016
 * 
 */
public class ToolsMiscellaneous {

	static private ToolsMiscellaneous instance = null;
	
	/** Define se é para usar sementes randômicas ao gerar valores randômicos */
	public boolean randomSeed = PaxMacConstants.RANDOM_SEED;
	
	/** Construtor */
	static public ToolsMiscellaneous getInstance() {
		if (instance == null) {
			instance = new ToolsMiscellaneous();
		}
		return instance;
	}
	
	
	/** Função para obter o tempo limite de simulação em steps */
    public double getLimitTimeSteps() {
        return Configuration.getInstance().getSimulationTime();
    }
    
    
	/** Obter densidade da rede */
	public double getDensitySimulation() {
		double x = Configuration.getInstance().getXSize();
		double y = Configuration.getInstance().getYSize();
		double density = Configuration.getInstance().getNodeCount();
		return density/(x*y);	
	}
	
	
	/** Obter o alcance do rádio */
	public double getReachableDistance() {
		UnitDisc UD = new UnitDisc();
		return UD.getReachableDistance();
	}
    
    
    /** Converte steps em segundos */
	public double stepsToSeconds(double steps) {
		return Configuration.getInstance().getSeconds(steps);
	}
	
	
	/** Retorna um valor randômico entre 0 e 1 */
	public double rand(NodeId node) {
		if (randomSeed) {
			return Math.random();
		} else {
			Random generator = new Random(node.asInt()*100000);
			double num = generator.nextDouble();
			return num;
		}
	}
	
	
	/** Define as cores no VisualGrubiX para o modo de alertar vizinhos para evitar colisão */
	public void vGrubixChannelReservation(NodeId node, NodeId destination) {
		String color;
		if (destination == null) {
			color = "GREEN";
		} else {
			switch (destination.asInt()) {
			case 2:  color = "RED";       break;
			case 4:  color = "DARK_RED"; break;
			default: color = "DARK_RED";  break;
			}
		}
		vGrubix(node, "Reserva Canal", color);
	}
	
	
	/** Chama a função para gerar o log no VisualGrubiX */
	public void vGrubix(NodeId node, String text, String color) {
		SimulationManager.logNodeState(node, text, "int", vGrubixNodeColor(color));
	}
	
	
	/** Mapa das cores para usar no VisualGrubiX */
	private String vGrubixNodeColor(String color) {
		int idColor;
		
		switch (color) {
		case "RED":         idColor = 0;  break;
		case "DARK_RED":    idColor = 9;  break;
		case "GREEN":       idColor = 3;  break;
		case "DARK_GREEN":  idColor = 4;  break;
		case "LIGHT_GREEN": idColor = 1;  break;
		case "BLUE":        idColor = 6;  break;
		case "DARK_BLUE":   idColor = 7;  break;
		case "LIGHT_BLUE":  idColor = 11; break;
		case "PINK":        idColor = 10; break;
		case "PURPLE":      idColor = 2;  break;
		default:            idColor = 2;  break;
		}
		return String.valueOf(idColor);
	}
	

	/** Retorna um valor decimal com o formato pt-BR */
	public String ptBr(double val) {
		NumberFormat br = NumberFormat.getNumberInstance(new Locale("pt","BR"));
		br.setMaximumFractionDigits(5);
		br.setRoundingMode (RoundingMode.FLOOR);
		return br.format(val);
	}
	
	
	/** Retorna um valor decimal com o formato pt-BR */
	public String ptBr(int val) {
		NumberFormat br = NumberFormat.getNumberInstance(new Locale("pt","BR"));
		return br.format(val);
	}
	
	
	/** Retorna valor para o formato csv para local pt-BR */
	public String csv(double val) {
		String sep=";";
		return sep + ptBr(val) + sep;
	}
	
	
	/** Retorna valor para o formato csv para local pt-BR */
	public String csv(int val) {
		String sep=";";
		return sep + ptBr(val) + sep;
	}
	
	
	/** Retorna valor para o formato csv para local pt-BR */
	public String csv(String val) {
		String sep=";";
		return sep + val + sep;
	}
	
	
	/** Retorna valor nulo se zero */
	public String csvZeroToNull(String val) {
		String sep=";";
		if (val == sep + 0 + sep) {
			return sep + "" + sep;
		}
		return val;
	}
	
	
	/** Converte booleano em inteiro */
	public int boolToInt(boolean d) {
		if (d) {
			return 1;
		}
		return 0;
	}
	
	
	/** Cria nomes únicos para criar logs */
	public String nameGenerator(String prefix) {
		return prefix+"_"+System.currentTimeMillis();
	}

}
