package br.ufla.dcc.PingPong;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.ufla.dcc.PingPong.PaxMac.PaxMacConfiguration;
import br.ufla.dcc.PingPong.PaxMac.PaxMacPacket;
import br.ufla.dcc.PingPong.XMac2019.XMacConfiguration;
import br.ufla.dcc.PingPong.XMac2019.XMacPacket;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/** Guarda e processa informações que serão usadas para gerar as estatísticas da simulação */
public class ToolsStatisticsSimulation {

	static private ToolsStatisticsSimulation instance = null;
	
	/** Referência para as configurações da camada MAC */
	public PaxMacConfiguration paxConf = null;
	
	/** Referência para as configurações da camada MAC */
	public XMacConfiguration xConf = null;
	
	/** Total de transmissões que serão realizadas */
	public int numTransmissions;
	
	/** Lista de objetos para os dados estatísticos do fluxo de transmissão */
	public List<ToolsStatisticsTransmission> transmissions = new ArrayList<ToolsStatisticsTransmission>();
	
	/** Se já imprimiu as estatísticas */
	public boolean printedStatistics = false;
	
	/** Ferramentas auxiliares diversas */
 	ToolsMiscellaneous misc = ToolsMiscellaneous.getInstance();
 	
	
	/** Construtor */
	static public ToolsStatisticsSimulation getInstance() {
		if (instance == null) {
			instance = new ToolsStatisticsSimulation();
		}
		return instance;
	}
	
	
	/** Define o total de transmissões que serão realizadas na simulação */
	public void setNumTransmissions(int numTransmissions) {
		this.numTransmissions = numTransmissions;
	}
	
	
	/** Retorna o objeto das estatísticas de transmissão baseado no id de origem. Se a 
	 * transmissão não existe, cria uma nova e a adiciona na lista */
	public ToolsStatisticsTransmission getTransmission(NodeId destinationId) {
		// Verificar se foi fornecido o nó de destino
		if (destinationId == null) {
			System.out.println("[StatisticsError] Destino nulo informado na estatística em "+
					"getTransmission() tempo: "+SimulationManager.getInstance().getCurrentTime());

			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			String s = st[1].getClassName()+" - "+st[1].getMethodName()+"\n";
			s += st[2].getClassName()+" - "+st[2].getMethodName()+"\n";
			s += st[3].getClassName()+" - "+st[3].getMethodName()+"\n";
			s += st[4].getClassName()+" - "+st[4].getMethodName()+"\n";
			System.out.println(s);
			SimulationManager.getInstance().cancelSimulation();
		}
		
		for (ToolsStatisticsTransmission t : transmissions) {
			if (t.destinationId.asInt() == destinationId.asInt()) {
		        return t;
		    }
		}
		// Adiciona um novo nó na lista e o retorna
		ToolsStatisticsTransmission transmission = new ToolsStatisticsTransmission(destinationId, paxConf);
		transmissions.add(transmission);
		return transmission;
	}
	
	
	/** Retorna transmissão na lista baseado no id de origem. Se a transmissão não existe, 
	 * cria uma nova e a adiciona na lista */
	public ToolsStatisticsTransmission getTransmission(PaxMacPacket packet) {
		return getTransmission(packet.getFinalReceiverNode());
	}
	
	/** Retorna transmissão na lista baseado no id de origem. Se a transmissão não existe, 
	 * cria uma nova e a adiciona na lista */
	public ToolsStatisticsTransmission getTransmission(XMacPacket packet) {
		return getTransmission(packet.getReceiver());
	}
	
	
	/** Retorna transmissão na lista baseado no id de origem. Se a transmissão não existe,
	 * cria uma nova e a adiciona na lista */
	public ToolsStatisticsTransmission getTransmission(Packet packet) {
		PaxMacPacket paxPkt = (PaxMacPacket)packet.getPacket(LayerType.MAC);
		return getTransmission(paxPkt);
	}
	
	/** Retorna transmissão na lista baseado no id de origem. Se a transmissão não existe,
	 * cria uma nova e a adiciona na lista */
	public ToolsStatisticsTransmission getTransmissionXMac(XMacPacket packet) {
		XMacPacket xPkt = (XMacPacket)packet.getPacket(LayerType.MAC);
		return getTransmission(xPkt);
	}
	
	
	/** Obter o total de reinicio de RTS na simulação */
	public double getTotalRestartRts() {
		int total = 0;
		for (ToolsStatisticsTransmission t : transmissions) {
			total += t.getTotalRestartRts();
		}
		return total;
	}
	
	
	/** Obter total de CTS-DATA enviados */
	public int getTotalCtsDataSent() {
		int total = 0;
		for (ToolsStatisticsTransmission t : transmissions) {
			total += t.getTotalCtsDataSent();
		}
		return total;
	}
	
	
	/** Obter total de CS-LONG CTS-DATA ocupados */
	public int getTotalCsLongCtsDataBusy() {
		int total = 0;
		for (ToolsStatisticsTransmission t : transmissions) {
			total += t.getTotalCsLongCtsDataBusy();
		}
		return total;
	}
	
	
	/** Obter total de espera CTS-DATA realizados */
	public int getTotalCtsDataWaiting() {
		int total = 0;
		for (ToolsStatisticsTransmission t : transmissions) {
			total += t.getTotalCtsDataWaiting();
		}
		return total;
	}
	

	/** Obter o total de dados que chegaram ao destino */
	public int getTotalDestinationGetData() {
		int total = 0;
		for (ToolsStatisticsTransmission t : transmissions) {
			if (t.destinationGetData) {
				total++;
			}
		}
		return total;	
	}
	
	
	/** Obter tempo útil total da simulação */
	public double getTotalSimulationTime() {
		double start = -1;
		double end = -1;
		for (ToolsStatisticsTransmission t : transmissions) {
			/* Se tem um tempo igual -1 é porque terminou o tempo da simulação e dado não chegou 
			 * ao destino */
			if (t.startTime > 0 && t.endTime > 0) {
				if (start < 0 || start > t.startTime) {
					start = t.startTime;
				}
				if (end < 0 || end < t.endTime) {
					end = t.endTime;
				}
			}
		}
		return end-start;
	}
	
	
	/** Obter o tempo médio das transmissões */
	public double getAverageTxTime() {
		double totalTime = 0;
		int count = 0;
		for (ToolsStatisticsTransmission t : transmissions) {
			if (t.startTime > 0 && t.endTime > 0) {
				count++;
				totalTime += t.endTime - t.startTime;
			}
		}
		if (count == 0) {return 0;}
		return totalTime/count;
	}
	
	
	/** Verifica se é para finalizar a simulação */
	public boolean isFinalizeSimulation() {
		if (transmissions.size() == 0) {
			return false;
		}
		int count = 0;
		for (ToolsStatisticsTransmission t : transmissions) {
		    if (t.finalizeTx) {
		    	count++;
		    }
		}
		if (count == numTransmissions) {
			return true;
		}
		return false;
	}
	
	
	
	/** Imprimir as estatísticas */
	public void printStatistics() {
		
		System.out.println("---------------------------------------------------\n--- Estatísticas\n"+
				"---------------------------------------------------");
		
		System.out.println(
			"--------[Configurações]\n"+
			"Tamanho Fcs Padrão: "+paxConf.getFcsSize()+"\n"+
			"Segurar Data: "+paxConf.getStepsKeepData()/paxConf.getStepsDATA()+"(tamanhos de data)\n"+
			"Mínimo Multiplicador FCS: "+paxConf.getFcsMinMultiplier()+"\n"+
			"Máximo Multiplicador FCS: "+paxConf.getFcsMaxMultiplier()+"\n"+
			"Máximo envio Rts: "+paxConf.getMaxPreambles()+"\n"+
			"Fluxos de Transmissão: "+transmissions.size()+"\n"+
			"Simulação Máximo: "+Configuration.getInstance().getSimulationTime()+"(steps) "+
				+misc.stepsToSeconds(Configuration.getInstance().getSimulationTime())+"(seg)\n"+
			"Simulação Execução: "+getTotalSimulationTime()+"(steps) "+
				+misc.stepsToSeconds(getTotalSimulationTime())+"(seg)\n"+
			"Total Restart Rts: "+getTotalRestartRts()+"\n"+			
			"Cycle: "+paxConf.getStepsCycle()+"(steps) "+
				+misc.stepsToSeconds(paxConf.getStepsCycle())+"(seg)\n"+
			"Data: "+paxConf.getStepsDATA()+"(steps) "+
				+misc.stepsToSeconds(paxConf.getStepsDATA())+"(seg) "+
				+paxConf.getLengthDATA()+"(bits) "+
				+(paxConf.getStepsDATA()/paxConf.getStepsCycle())+"(%)\n"+
			"Rts: "+paxConf.getStepsRTS()+"(steps) "+
				+misc.stepsToSeconds(paxConf.getStepsRTS())+"(seg)\n"+
			"Sleep: "+paxConf.getStepsSleep()+"(steps) "+
				+misc.stepsToSeconds(paxConf.getStepsSleep())+"(seg)\n"+
			"Cs: "+paxConf.getStepsCsLong()+"(steps) "+
				+misc.stepsToSeconds(paxConf.getStepsCsLong())+"(seg)\n"
		);

		// Imprimir estatísticas dos fluxos de transmissão
		for (ToolsStatisticsTransmission t : transmissions) {
			System.out.println("\n--------[Resultados da transmissão destino: "+t.destinationId+"]");
			t.printStatistics();
		}
		
		System.out.println("\nFim da simulação em: "+SimulationManager.getInstance().getCurrentTime());
	}
	
	/** Imprimir as estatísticas */
	public void printStatisticsXMac() {
		
		System.out.println("---------------------------------------------------\n--- Estatísticas\n"+
				"---------------------------------------------------");
		
		System.out.println(
			"--------[Configurações]\n"+
			"Máximo envio Rts: "+xConf.getMaxPreambles()+"\n"+
			"Fluxos de Transmissão: "+transmissions.size()+"\n"+
			"Simulação Máximo: "+Configuration.getInstance().getSimulationTime()+"(steps) "+
				+misc.stepsToSeconds(Configuration.getInstance().getSimulationTime())+"(seg)\n"+
			"Simulação Execução: "+getTotalSimulationTime()+"(steps) "+
				+misc.stepsToSeconds(getTotalSimulationTime())+"(seg)\n"+
			"Total Restart Rts: "+getTotalRestartRts()+"\n"+			
			"Cycle: "+xConf.getStepsCycle()+"(steps) "+
				+misc.stepsToSeconds(xConf.getStepsCycle())+"(seg)\n"+
			"Data: "+xConf.getStepsDATA()+"(steps) "+
				+misc.stepsToSeconds(xConf.getStepsDATA())+"(seg) "+
				+xConf.getLengthDATA()+"(bits) "+
				+(xConf.getStepsDATA()/xConf.getStepsCycle())+"(%)\n"+
			"Rts: "+xConf.getStepsRTS()+"(steps) "+
				+misc.stepsToSeconds(xConf.getStepsRTS())+"(seg)\n"+
			"Sleep: "+xConf.getStepsSleep()+"(steps) "+
				+misc.stepsToSeconds(xConf.getStepsSleep())+"(seg)\n"
		);

		// Imprimir estatísticas dos fluxos de transmissão
		for (ToolsStatisticsTransmission t : transmissions) {
			System.out.println("\n--------[Resultados da transmissão destino: "+t.destinationId+"]");
			t.printStatistics();
		}
		
		System.out.println("\nFim da simulação em: "+SimulationManager.getInstance().getCurrentTime());
	}
	
	/** Imprimir estatísticas resumidas */
	public void printShortStatisticsXMac() {

		System.out.println("\n--------------------------------");
		String sep=";"; 
		System.out.print("P-->"          +sep+
			" fluxos="                   +misc.csv(transmissions.size())+
			" entregas="                 +misc.csv(getTotalDestinationGetData())+
			" tempo_medio_seg="          +misc.csvZeroToNull(misc.csv(misc.stepsToSeconds(getAverageTxTime())))+
			" dado_%_ciclo="             +misc.csv((xConf.getStepsDATA()/xConf.getStepsCycle()))+
			" total_ctsdata_enviados="   +misc.csv(getTotalCtsDataSent())+
			" total_cslong_ocupados="    +misc.csv(getTotalCsLongCtsDataBusy())+
			" total_espera_ctsdata="     +misc.csv(getTotalCtsDataWaiting())+
			" total_reinicio_rts="       +misc.csv(getTotalRestartRts())+
			" tempo_simulacao_seg="      +misc.csvZeroToNull(misc.csv((misc.stepsToSeconds(getTotalSimulationTime()))))+
			" densidade="                +misc.csv(misc.getDensitySimulation())+
			" alcance_radio="            +misc.csv(misc.getReachableDistance())
		);
		
		// Imprimir as estatísticas dos fluxos de transmissão
		int i = 1;
		for (ToolsStatisticsTransmission t : transmissions) {
			t.printShortStatistics(i);
			i++;
		}
		System.out.println();
	}
	
	/** Imprimir estatísticas resumidas */
	public void printShortStatistics() {

		System.out.println("\n--------------------------------");
		String sep=";"; 
		System.out.print("P-->"          +sep+
			" fluxos="                   +misc.csv(transmissions.size())+
			" entregas="                 +misc.csv(getTotalDestinationGetData())+
			" tempo_medio_seg="          +misc.csvZeroToNull(misc.csv(misc.stepsToSeconds(getAverageTxTime())))+
			" dado_%_ciclo="             +misc.csv((paxConf.getStepsDATA()/paxConf.getStepsCycle()))+
			" max_mult_fcs="             +misc.csv(paxConf.getFcsMaxMultiplier())+
			" min_mult_fcs="             +misc.csv(paxConf.getFcsMinMultiplier())+
			" manter_dado_%_tx="         +misc.csv((paxConf.getStepsKeepData()/paxConf.getStepsDataRetryCicle()))+
			" total_ctsdata_enviados="   +misc.csv(getTotalCtsDataSent())+
			" total_cslong_ocupados="    +misc.csv(getTotalCsLongCtsDataBusy())+
			" total_espera_ctsdata="     +misc.csv(getTotalCtsDataWaiting())+
			" total_reinicio_rts="       +misc.csv(getTotalRestartRts())+
			" tempo_simulacao_seg="      +misc.csvZeroToNull(misc.csv((misc.stepsToSeconds(getTotalSimulationTime()))))+
			" tamanho_fcs_padrao="       +misc.csv(paxConf.getFcsSize())+
			" densidade="                +misc.csv(misc.getDensitySimulation())+
			" alcance_radio="            +misc.csv(misc.getReachableDistance())
		);
		
		// Imprimir as estatísticas dos fluxos de transmissão
		int i = 1;
		for (ToolsStatisticsTransmission t : transmissions) {
			t.printShortStatistics(i);
			i++;
		}
		System.out.println();
	}
}
