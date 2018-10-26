package br.ufla.dcc.PingPong;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/*
import br.ufla.dcc.PingPong.WiseMac.WiseMacConfiguration;
import br.ufla.dcc.PingPong.WiseMac.WiseMacPacket;
import br.ufla.dcc.PingPong.WiseMac.WiseMacRadioState;
import br.ufla.dcc.PingPong.WiseMac.WiseMacState;
import br.ufla.dcc.PingPong.WiseMac.WiseMacWucBackOff;
import br.ufla.dcc.PingPong.WiseMac.WiseMacWucTimeOut;
*/

import br.ufla.dcc.PingPong.AgaMac.AgaMacConfiguration;
import br.ufla.dcc.PingPong.AgaMac.AgaMacConstants;
import br.ufla.dcc.PingPong.AgaMac.AgaMacPacket;
import br.ufla.dcc.PingPong.AgaMac.AgaMacRadioState;
import br.ufla.dcc.PingPong.AgaMac.AgaMacState;
import br.ufla.dcc.PingPong.AgaMac.AgaMacWucBackOff;
import br.ufla.dcc.PingPong.AgaMac.AgaMacWucTimeOut;
import br.ufla.dcc.PingPong.PaxMac.NodeDistanceDestination;
import br.ufla.dcc.PingPong.PaxMac.PaxMacConfiguration;
import br.ufla.dcc.PingPong.PaxMac.PaxMacConstants;
import br.ufla.dcc.PingPong.PaxMac.PaxMacPacket;
import br.ufla.dcc.PingPong.PaxMac.PaxMacRadioState;
import br.ufla.dcc.PingPong.PaxMac.PaxMacState;
import br.ufla.dcc.PingPong.PaxMac.PaxMacStateMachine;
import br.ufla.dcc.PingPong.PaxMac.PaxMacWucBackOff;
import br.ufla.dcc.PingPong.PaxMac.PaxMacWucTimeOut;
import br.ufla.dcc.PingPong.Phy.PhyRadioState;
import br.ufla.dcc.PingPong.Phy.PhysicalSimple;
import br.ufla.dcc.PingPong.Phy.WucPhyRxTimer;
import br.ufla.dcc.PingPong.XMac.XMacConfiguration;
import br.ufla.dcc.PingPong.XMac.XMacConstants;
import br.ufla.dcc.PingPong.XMac.XMacPacket;
import br.ufla.dcc.PingPong.XMac.XMacRadioState;
import br.ufla.dcc.PingPong.XMac.XMacState;
import br.ufla.dcc.PingPong.XMac.XMacWucBackOff;
import br.ufla.dcc.PingPong.XMac.XMacWucTimeOut;
import br.ufla.dcc.PingPong.node.AppPacket;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.PingPong.node.PingPongWakeUpCall;
import br.ufla.dcc.PingPong.node.RadioState;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;


/** Classe que organiza as informações para imprimir ou gerar arquivo para a depuração
 * 
 *  @author Gustavo Araújo
 *  @version 01/11/2016
 * 
 */
public class ToolsDebug {
	/**  Instância da classe, processo para criar uma classe Singleton */
	private static ToolsDebug instance = null;
	
	/** Se é para habilitar o debug **/
	private boolean disable_debug = true;
	
	/** Define se é para escrever no arquivo, é possível deixar tudo em buffer  */
	private boolean writeFile = true;
	
	/** Caminho do arquivo que será gerado. Por padrão fica no diretório da aplicação */
	private String path = "DebugRecord.txt";
	
	/** Arquivo */
	private File file;
	
	/** Buffer de escrita */
	private BufferedWriter writer;
	
	/** Contador do número de registros criados. Usado no cabeçalho */
	private int countId = 0;
	
	/** A string que identifica qual foi o último registro gerado. Usado para evitar criar outro
	 * registro caso seja chamado outras funções de debug em sequência num mesmo método */
	private String lastStackTraceKey = "";
	
	/** Filtrar somente registros gerados por nós que possuam ou não certos id's */
	private String[] filterNodes = {};
	
	/** Filtrar somente registros gerados por nós que possuam ou não certas etiquetas */
	private String[] filterLabels = {};
	
	/** Filtrar somente registros gerados ou não por certos métodos.
	 * Os métodos devem ser informados depois do nome da classe e antes de um ponto e com parênteses 
	 * no final. Não é necessário informar o método, nesse caso serão inclusos todos os métodos da 
	 * classe. Ex: br.ufla.dcc.PingPong.node.RegularNode ou 
	 * br.ufla.dcc.PingPong.node.RegularNodelowerSAP() */
	private String[] filterMethods = {};

	/** Se é para excluir ou somente exibir items que casam com os id's informados */
	private boolean filterNodesExcludeMode = true;
	
	/** Se é para excluir ou somente exibir items que casam com as etiquetas informadas */
	private boolean filterLabelsExcludeMode = true;
	
	/** Se é para excluir ou somente exibir items que casam com os métodos informados */
	private boolean filterMethodsExcludeMode = true;
	
	
	/** Construtor */
	public ToolsDebug() {
		if (disable_debug) {return;}
		create();

	}

	
	/**
	 * Método sincronizado para evitar que devido a concorrência sejam criados mais de uma
	 * instância, já é uma classe singleton 
	 */
	public static synchronized ToolsDebug getInstance() {
		if(instance == null) {
		     instance = new ToolsDebug();
		}
		return instance;
	}
	
	
	/** Cria o arquivo */
	private void create() {
		if (!isWriteFile()) {return;}
		// Evitar que as classes filha sobrescrevam o arquivo criado
		this.file = new File(path);
		try {
			this.writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			System.out.println("BufferedWriter error");
		}
	}
	
	
	/** Função para obter o tempo corrente da simulação em steps */
    protected double currentTimeSteps() {
        return SimulationManager.getInstance().getCurrentTime();
    }
    
	
	/** Fechar o arquivo */
	public void close() {
		if (!isWriteFile()) {return;}
		try {
			// Fechando conexão
			this.writer.close();
		} catch (IOException e) {
			System.out.println("BufferedWriter close error");
		}
	}
	
	
	/** Escreve no buffer */
	public void writeBuffer(String text) {
		if (!isWriteFile()) {return;}
		try {
			this.writer.write(text);
		} catch (IOException e) {
			System.out.println("BufferedWriter error");
		}
	}
	
	
	/** Escreve buffer no arquivo */
	public void writeFile() {
		if (!isWriteFile()) {return;}
		try {
			// Criando o conteúdo do arquivo
			this.writer.flush();
		} catch (IOException e) {
			System.out.println("BufferedWriter error");
		}
	}
	
	
	/** Chama método para criar e escrever registro 
	 * @param addr  id do nó para informar no cabeçalho e ser comparado no filtro por id's 
	 */
	public void write(Address addr) {
		if (disable_debug || !isWriteFile()) {return;}
		writeRegister("", addr, "");
	}
	
	
	/** Chama método para criar e escrever registro
	 * @param text  texto que será escrito
	 * @param addr  id do nó para informar no cabeçalho e ser comparado no filtro por id's 
	 */
	public void write(String text, Address addr) {
		if (disable_debug || !isWriteFile()) {return;}
		writeRegister(text, addr, "");
	}
	
	
	/** Chama método para criar e escrever registro
	 * @param text   texto que será escrito
	 * @param addr   id do nó para informar no cabeçalho e ser comparado no filtro por id's
	 * @param label  etiqueta que será escrita e ser comparada no filtro por labels 
	 */
	public void write(String text, Address addr, String label) {
		if (disable_debug || !isWriteFile()) {return;}
		writeRegister(text, addr, label);
	}

	
	/** Chama método para criar e escrever registro somente se o nó possui o um dos ids informados
	 * @param text          texto que será escrito
	 * @param addr          id do nó para informar no cabeçalho e ser comparado no filtro por id's
	 * @param nodeIdFilter  somente nó com este id poderá criar e escrever o registro
	 */
	public void writeIfNodes(String text, Address addr, Integer[] nodes) {
		if (disable_debug || !isWriteFile()) {return;}
		if (Arrays.asList(nodes).contains(addr.getId().asInt())) 
			writeRegister(text, addr, "");
	}
	
	
	/** Cria e escrever registro 
	 * @param text   texto que será escrito
	 * @param addr   id do nó para informar no cabeçalho e ser comparado no filtro por id's
	 * @param label  etiqueta que será escrita e ser comparada no filtro por labels 
	 */
	private boolean writeRegister(String text, Address nodeAdr, String label) {
		
		// Adquire a pilha de métodos chamados durante a execução
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String s = "";
		
		// Escrever somente registros que passarem no filtro
		if (!filter(stackTrace[3].getClassName(), stackTrace[3].getMethodName(), nodeAdr, label)) 
			return false;

		/* Monta um id-chave do registro para comparar com o último id-chave, evitando criar mais 
		 de um registro para um mesmo método caso seja depurado mais de uma vez em sequência */
		String StackTraceKey = stackTrace[3].getClassName()+stackTrace[3].getMethodName()+
				stackTrace[4].getClassName()+stackTrace[4].getMethodName();
		if (nodeAdr != null) {
			StackTraceKey += nodeAdr.getId();
		}

		/* Se o id-chave é diferente, escreva o cabeçalho do novo registro, contendo qual o método
		que chamou a classe Debug */
		if (!getLastStackTrace().equals(StackTraceKey)) 
			s+="\n"+strHeader(stackTrace, nodeAdr);
		
		if (!label.equals("")) 
			s+= tagLabel(label);
		if (!text.equals("")) {
			/* Se foi passado o texto sem chamar o método str(), deve tratá-lo para receber os 
			delimitadores do campo ([]) */
			if (!text.substring(0,1).equals(tagField("").substring(0,1)))
				text = str(text);
			s+= text;
		}
		// Se não possui quebra de linha no final, acrescenta quebra de linha
		if (!s.substring(s.length()-1).equals("\n"))
			s+='\n';
		
		// Escreve no buffer
		writeBuffer(s);
		// Escreve no arquivo
		writeFile();

		
		// Guardar o ultimo id-chave para comparação posterior
		setLastStackTrace(StackTraceKey);
		return true;
	}
	
	
	/** Define as tags entre os tipos de informação */
	public String tagField(String text) {
		return '['+String.format("%1$-" + 12 + "s", text)+']';
	}
	
	
	/** Define as tags entre a etiqueta */
	public String tagLabel(String text) {
		return tagField("@Label")+" #"+text+'\n';
	}
	
	
	/** Define as tags entre o texto */
	public String tagText(String text) {
		return tagField("@Text")+" \""+text+"\"\n";
	}
	
	
	/** Define formato do texto */
	public String str(String text) {
		return tagText(text);
	}
	
	
	/** Define o formato do cabeçalho do registro */
	private String strHeader(StackTraceElement[] st, Address adr) {
		this.countId++;
		double steps = SimulationManager.getInstance().getCurrentTime();
		String nodeInfo = "";
		// Se não foi informado o id do nó, será informado "?"
		if (adr == null) 
			nodeInfo += "?";
		else
			nodeInfo += adr.getId()+" "+adr.getFromLayer();

		return countId + fillString(8,'-').substring(String.valueOf(countId).length())+
			"Node "+nodeInfo + fillString(20,'-').substring(nodeInfo.length())+
			"Steps "+String.format("%1$,.4f", steps) + 
			fillString(60,'-').substring(String.format("%1$,.4f", steps).length())+
			"\n"+tagField("Method")+" "+st[3].getMethodName()+"() "+st[3].getClassName()+
			"\n"+tagField("Previous")+String.format("%1$-"+(st[3].getMethodName().length()+4)+"s", "")+
			st[4].getClassName()+":"+st[4].getMethodName()+"()"+'\n';
	}
	
	
	/** Define o formato das informações do pacote da classe Packet */
	public String strPkt(Packet p) {
		if (p == null)
			return tagField("Packet")+"Vazio";
		
		String s;
		s = "";
		AppPacket pktApp = (AppPacket)p.getPacket(LayerType.APPLICATION);
		MACPacket pktMac = (MACPacket)p.getPacket(LayerType.MAC);
		PhysicalPacket pktPhy = (PhysicalPacket)p.getPacket(LayerType.PHYSICAL);
		
		s += tagField("Packet")+
		" IdPktApp="+p.getHighestEnclosedPacket().getId();
		if (pktApp != null) {
			s += " Destination="+pktApp.getDestinationId()+
			" HopCount="+pktApp.getHopCount();
		}
		s += " Sender="+p.getSender().getId()+
		" Receiver="+p.getReceiver()+
		" Receivers="+p.getReceivers()+
		" Classe="+p.getClass().getSimpleName()+
		" Layer="+p.getLayer()+
		" Direction="+p.getDirection()+
		" Time="+p.getTime()+
		" Delay="+p.getDelay()+
		" isTerminal="+p.isTerminal()+
		" isValid="+p.isValid()+
		" Total Size="+p.getTotalPacketSizeInBit()+'\n';
		if (pktMac != null) {
			s += tagField("PacketMac")+
			" Type="+pktMac.getType()+
			" RetryCount="+pktMac.getRetryCount()+
			" isAckRequested="+pktMac.isAckRequested()+
			" SignalStrength="+pktMac.getSignalStrength()+'\n';
		}
		if (pktPhy != null) {
			s += tagField("PacketPhy")+
			" BPS="+pktPhy.getBPS()+
			" isTransitToWillSend="+pktPhy.isTransitToWillSend()+
			" TransmissionSteps="+pktPhy.getDuration()+'\n';
		}
		return s;
	}

	
	/** Define o formato das informações do nó */
	public String strNode(Node node) {
		return tagField("Node")+
				" Id="+node.getId().toString()+
				" CurrentTime="+node.getCurrentTime()+
				" NeighborCount="+node.getNeighborCount()+
				" Neighbors="+node.getNeighbors()+
				" Position="+node.getPosition()+'\n';
	}
	
	
	/** Define o formato das informações do WakeUpCall geral */
	public String strWuc(WakeUpCall wuc) {
		return tagField("Wuc")+
				" Id="+wuc.getId()+
				" Sender="+wuc.getSender().getId()+
				" Receiver="+wuc.getReceiver()+
				" Delay="+wuc.getDelay()+
				" Class="+wuc.getClass().getSimpleName()+'\n';
	}
	
	/** Define o formato das informações do WakeUpCall da camada de aplicação*/
	public String strWucPingPong(PingPongWakeUpCall wuc) {
		return strWuc(wuc)+tagField("WucPingPong")+
				" NodeDestination="+wuc.getDestination()+'\n';
	}
	
	
	/** Define o formato das informações do WakeUpCall de recepção da camada física */
	public String strWucRxTimer(WucPhyRxTimer wuc) {
		return strWuc(wuc)+tagField("WucPhyRxTimer")+
				" EndingState="+wuc.getStartingState()+
				" StartTime="+wuc.getStartTime()+
				" EndTime="+wuc.getEndTime()+'\n'+
				strPkt(wuc.getPacket());
	}
	
	
	/** Define o formato das informações do CrossLayerEvent */
	public String strEventCrossLayer(CrossLayerEvent ev) {
		return strWuc(ev)+tagField("EventCrossLayer")+
				" Originator="+ev.getOriginator()+
				" Result="+ev.getResult()+
				" TerminalLayer="+ev.getTerminalLayer()+'\n'+
				strPkt(ev.getPacket());
	}
	
	
	/** Define o formato das informações do WakeUpCall */
	public String strRouting(Packet pk, NodeId closer) {
		return tagField("Routing")+
				" Destination="+pk.getReceiver()+
				" CloserNode="+closer+'\n';
	}
	

	/** Define o formato das informações do rádio mantido pela AIR */
	public String strRadioState(RadioState p) {
		return tagField("RadioState")+" state="+p.toString()+'\n';
	}
	
	
	/** Define o formato das informações do rádio mantido pela camada física */
	public String strPhyRadioState(PhyRadioState p) {
		return tagField("PhyRadioState")+" state="+p.getRadioState()+'\n';
	}
	
	
	/** Define o formato das informações da camada PHY */
	public String strPhyLayer(PhysicalSimple p) {
		return tagField("PhyLayer")+" radioPhy="+p.getPhyRadioState()+
				" RadioAir="+p.getRadioState()+
				" LastReceptionDuration="+p.getLastEmissionDuration()+
				" LastReceivingEnd="+p.getLastReceptionEnd()+
				" MsgColision="+p.isMsgColision()+
				" HeaderPprocessWakeUpCalllusFooterLength="+p.getHeaderPlusFooterLength()+
				" ChannelBusy="+p.isBusyChannel()+
				" DropInvalidPackets="+p.isDropInvalidPackets()+
				" BpsSimulation="+p.getBpsSimulation()+ '\n';
	}

	
	/** Imprime texto do usuário com informações do nó e steps, sem passar pelos filtros. */
	public void print(String text, Address sender) {
		if (disable_debug) {return;}
		String s = printRegister(sender, text, false);
		System.out.println(s);
	}
	

	/** Imprime e escreve no arquivo o texto do usuário com informações do nó e steps passando
	 pelos filtros. */
	public void printw(String text, Address sender) {
		if (disable_debug) {return;}
		String s = printRegister(sender, text, true);
		if (s.equals(""))
			return;
		writeRegister(tagField("@Print")+" "+text, sender, "");
		System.out.println(s);
	}

	
	/** Cria o registro que será impresso e/ou escrito **/
	private String printRegister(Address adr, String text, boolean hasFilter) {
		String s ="";
		
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (hasFilter && !filter(stackTrace[3].getClassName(), stackTrace[3].getMethodName(), adr, ""))
			return s;
		
		double steps = SimulationManager.getInstance().getCurrentTime();
		s += ">>>>> NODE:"+adr.getId()+" STEP:"+String.format("%1$,.10f", steps)+" >>>>>>"+
				" METHOD:"+stackTrace[3].getMethodName()+"()"+
				" ROUTINE:"+stackTrace[4].getMethodName()+
				" CLASS:"+stackTrace[3].getClassName()+"\n"+text;
		if (!s.substring(s.length()-1).equals("\n"))
			s+='\n';
		return s;
	}
	
	
	/** Preenche uma string com uma certa quantidade do mesmo caractere */
	private static String fillString(int size, char c) {
	    final char[] array = new char[size];
	    Arrays.fill(array, c);
	    return new String(array);
	}
	
	
	/** Realiza a verificação do filtro */
	private boolean filter(String clas, String method, Address adr, String label) {
		if (adr != null && Arrays.asList(filterNodes).contains(adr.getId().toString())) {
			if (filterNodesExcludeMode) return false;
		} else {
			if (!filterNodesExcludeMode) return false;
		}
		
		if (!label.equals("") && Arrays.asList(filterLabels).contains(label)) {
			if (filterLabelsExcludeMode) return false;
		} else {
			if (!filterLabelsExcludeMode) return false;
		}
		
		if (Arrays.asList(filterMethods).contains(clas) ||
				Arrays.asList(filterMethods).contains(clas+'.'+method+"()")) {
			if (filterMethodsExcludeMode) return false;
		} else {
			if (!filterMethodsExcludeMode) return false;
		}
		return true;
	}
	
	
	
	/* gets e sets */
	private void setLastStackTrace(String s) {
		this.lastStackTraceKey = s;
	}
	
	private String getLastStackTrace() {
		return this.lastStackTraceKey;
	}
	
	private boolean isWriteFile() {
		return writeFile;
	}

	
	/*
	 * -------------------------------------------------------------------------------
	 * Métodos usados para o XMAC
	 *  ------------------------------------------------------------------------------
	 */
	
	/** Define o formato das informações do WakeUpCall do tipo strWucTimeOut (XMac)*/
	public String strWucTimeOut(XMacWucTimeOut wuc) {
		return strWuc(wuc)+tagField("WucTimeOut")+
				" StateNumber="+wuc.getStateNumber()+
				" StartTime="+wuc.getStartTime()+
				" EndTime="+wuc.getEndTime()+'\n';
	}
	
	/** Define o formato das informações do WakeUpCall do tipo WucBackOff (XMac)*/
	public String strWucBackOff(XMacWucBackOff wuc) {
		return strWuc(wuc)+tagField("WucBackOff")+
				" StartingTime="+wuc.getStartingTime()+'\n'+
				strPkt(wuc.getPacket());
	}
	
	/** Define o formato das informações do pacote da camada de mac */
	public String strXPkt(XMacPacket p) {
		return strPkt(p)+tagField("PacketXmac")+
				" SequenceNumber="+p.getSequenceNumber()+'\n';
	}
	
	
	/** Define o formato das informações das configurações do XMac */
	public String strXConf(XMacConfiguration x) {
		return tagField("XmacConfig")+" stepsSleep="+x.getStepsSleep()+
				" stepsRTS="+x.getStepsRTS()+
				" stepsIdleListening="+x.getStepsCS()+
				" stepsACK="+x.getStepsACK()+
				" stepsCicle="+x.getStepsCycle()+
				" stepsPerSec="+
				(double)Configuration.getInstance().getStepsPerSecond()/XMacConstants.BPS+
				" signalStrength="+x.getSignalStrength()+
				" maxPreambles="+x.getMaxPreambles()+
				" maxSendRetries="+x.getMaxSendRetries()+'\n';
	}
	
	
	/** Define o formato das informações das configurações do XMac */
	public String strXState(XMacState x) {
		String s;
		s = tagField("XmacState")+" state="+x.getState()+
				" SeqNum="+x.getStateSeqNum()+
				" Duration="+x.getStateDuration()+
				" RestartRTS="+x.isRestartRTS()+
				" SendingPktType="+x.getSendingPktType()+
				" AckSeqNum="+x.getAckSeqNum()+
				" CtsSeqNum="+x.getCtsSeqNum()+
				" DataSeqNum="+x.getDataSeqNum()+
				" ReceiverNode="+x.getReceiverNode();
		if (x.getDataPkt() != null)	
			s += strPkt(x.getDataPkt());
		if (x.getRtsPkt() != null)	
			s += strPkt(x.getRtsPkt());
		if (!s.substring(s.length()-1).equals("\n"))
			s+='\n';
		return s;
	}
	
	
	/** Define o formato das informações referentes ao rádio mantido pela XMac */
	public String strXRadioState(XMacRadioState x) {
		return tagField("XRadioState")+" state="+x.getRadioState()+'\n';
	}
	
	
	/** Define o formato das informações referentes ao rádio mantido pela XMac */
	public String strXLayer(XMacState xState, XMacRadioState xRadio) {
		return strXState(xState)+strXRadioState(xRadio);		
	}
	
	
	
	/*
	 * -------------------------------------------------------------------------------
	 * Métodos usados para o AGA-MAC
	 *  ------------------------------------------------------------------------------
	 */

	/** Define o formato das informações do WakeUpCall do tipo strWucTimeOut (PAX-Mac)*/
	public String strWucTimeOut(AgaMacWucTimeOut wuc) {
		return strWuc(wuc)+tagField("WucTimeOut")+
				" StateNumber="+wuc.getStateNumber()+
				" StartTime="+wuc.getStartTime()+
				" EndTime="+wuc.getEndTime()+'\n';
	}
	
	
	/** Define o formato das informações do WakeUpCall do tipo WucBackOff (PAX-Mac)*/
	public String strWucBackOff(AgaMacWucBackOff wuc) {
		return strWuc(wuc)+tagField("WucBackOff")+
				" StartingTime="+wuc.getStartingTime()+'\n'+
				strPkt(wuc.getPacket());
	}
	
	/** Define o formato das informações do pacote da camada de mac */
	public String strAgaPkt(AgaMacPacket p) {
		return strPkt(p)+tagField("PacketAGAMac")+
				" SequenceNumber="+p.getSequenceNumber()+'\n';
	}
	
	
	/** Define o formato das informações das configurações do AGAMac */
	public String strAgaConf(AgaMacConfiguration a) {
		return tagField("AGAMacConfig")+" stepsSleep="+a.getStepsSleep()+
				" stepsRTS="+a.getStepsRTS()+
				" stepsIdleListening="+a.getStepsCS()+
				" stepsACK="+a.getStepsACK()+
				" stepsCicle="+a.getStepsCycle()+
				" stepsPerSec="+
				(double)Configuration.getInstance().getStepsPerSecond()/AgaMacConstants.BPS+
				" signalStrength="+a.getSignalStrength()+
				" maxPreambles="+a.getMaxPreambles()+
				" maxSendRetries="+a.getMaxSendRetries()+'\n';
	}
	
	
	/** Define o formato das informações das configurações do AGAMac */
	public String strAgaState(AgaMacState a) {
		String s;
		s = tagField("AGAMacState")+" state="+a.getState()+
				" StateSeqNum="+a.getStateSeqNum()+
				" Duration="+a.getStateDuration()+
				" LastReceiver="+a.getLastReceiver()+
				" Threshold="+a.getThreshold()+
				" CurrentThreshold="+a.getCurrentThreshold()+
				" RestartRTS="+a.isRestartRTS()+
				" SendingPktType="+a.getSendingPktType()+
				" AckSeqNum="+a.getAckSeqNum()+
				" CtsSeqNum="+a.getCtsSeqNum()+
				" DataSeqNum="+a.getDataSeqNum();
				//" ReceiverNode="+a.getReceiverNode();		
		if (a.getDataPkt() != null)	
			s += '\n'+strPkt(a.getDataPkt());
		if (a.getRtsPkt() != null)	
			s += '\n'+strPkt(a.getRtsPkt());
		if (!s.substring(s.length()-1).equals("\n"))
			s+='\n';
		return s;
	}
	
	
	/** Define o formato das informações referentes ao rádio mantido pela AGAMac */
	public String strAgaRadioState(AgaMacRadioState a) {
		return tagField("XRadioState")+" state="+a.getRadioState()+'\n';
	}
	
	
	/** Define o formato das informações referentes ao rádio mantido pela AGAMac */
	public String strAgaLayer(AgaMacState AGAState, AgaMacRadioState agaRadio) {
		return strAgaState(AGAState)+strAgaRadioState(agaRadio);		
	}
	
	
	
	/*
	 * -------------------------------------------------------------------------------
	 * Métodos usados para o PAX-MAC
	 *  ------------------------------------------------------------------------------
	 */
	
	/** Define o formato das informações do WakeUpCall do tipo strWucTimeOut (PAX-Mac)*/
	public String strWucTimeOut(PaxMacWucTimeOut wuc) {
		return strWuc(wuc)+tagField("WucTimeOut")+
				" SeqNum="+wuc.getStateNumber()+
				" StateNumber="+wuc.getStateNumber()+
				" StartTime="+wuc.getStartTime()+
				" EndTime="+wuc.getEndTime()+'\n';
	}
	
	
	/** Define o formato das informações do WakeUpCall do tipo WucBackOff (PAX-Mac)*/
	public String strWucBackOff(PaxMacWucBackOff wuc) {
		return strWuc(wuc)+tagField("WucBackOff")+
				" StartingTime="+wuc.getStartingTime()+'\n'+
				strPkt(wuc.getPacket());
	}
	
	
	/** Define o formato das informações do pacote da camada de mac */
	public String strPaxPkt(Packet p) {
		if (p instanceof PaxMacPacket) {
			PaxMacPacket pktMac = (PaxMacPacket)p.getPacket(LayerType.MAC);
			return strPkt(p)+tagField("PacketPaxMac")+
				" SendCtaDataDelay="+pktMac.getSendCtsDataDelay()+
				" SequenceNumber="+pktMac.getSequenceNum()+
				" PreviousReceiverNode="+pktMac.getPreviousReceiverNodeLv1()+
				" FinalReceiverNode="+pktMac.getFinalReceiverNode()+
				" FCS="+pktMac.getFcsNodes()+'\n';
		}
		return strPkt(p);	
	}
	
	
	/** Define o formato das informações das configurações do PAX-Mac */
	public String strPaxConf(PaxMacConfiguration pax) {
		return tagField("PaxMacConfig")+" stepsSleep="+pax.getStepsSleep()+
				// Debug
				" fcsSize="+pax.getFcsSize()+
				" lengthDATA="+pax.getLengthDATA()+
				" stepsDATA="+pax.getStepsDATA()+
				" lengthRTS="+pax.getLengthRTS()+
				" stepsRTS="+pax.getStepsRTS()+
				" stepsCTS="+pax.getStepsCTS()+
				" stepsCS="+pax.getStepsCsLong()+
				" stepsACK="+pax.getStepsACK()+
				" stepsCicle="+pax.getStepsCycle()+
				" stepsPerSec="+
				(double)Configuration.getInstance().getStepsPerSecond()/PaxMacConstants.BPS+
				" signalStrength="+pax.getSignalStrength()+
				" maxPreambles="+pax.getMaxPreambles()+'\n';
	}
	
	
	/** Define o formato das informações das configurações do PAX-Mac */
	public String strPaxState(PaxMacState pax) {
		String s;
		s = tagField("PaxMacState")+" state="+pax.getState()+
				" CtsDataTime="+pax.getCtsDataTime()+
				" NextReceiver="+pax.getNextReceiverNode()+
				" SeqNum="+pax.getStateSeqNum()+
				" Duration="+pax.getStateDuration()+
				" End="+(pax.getStateDuration()+pax.getStateStartTime())+
				" ReceiveDataTime="+pax.getCtsDataTime()+
				" SendingPktType="+pax.getSendingPktType()+
				" DataSeqNum="+pax.getDataSeqNum()+
				" FinalReceiver="+pax.getFinalReceiverNode()+'\n';
		if (pax.getDataPkt() != null)
			s += strPkt(pax.getDataPkt());
		if (pax.getRtsPkt() != null)	
			s += strPkt(pax.getRtsPkt());
		if (!s.substring(s.length()-1).equals("\n"))
			s+='\n';
		return s;
	}
	
	
	/** Define o formato das informações referentes ao rádio mantido pela PAX-Mac */
	public String strPaxRadioState(PaxMacRadioState pax) {
		return tagField("PaxRadioState")+" state="+pax.getRadioState()+'\n';
	}
	
	
	/** Define o formato das informações referentes ao rádio mantido pela PAX-Mac */
	public String strPaxLayer(PaxMacState paxState, PaxMacRadioState paxRadio) {
		return strPaxState(paxState)+strPaxRadioState(paxRadio);		
	}
	
	
//	/** Define o formato das informações referentes ao FCS */
//	public String strAdvancedNodes(int fcsSize, double senderDistance,
//			List <NodeDistanceDestination> nodesDistance, Node destination) {
//		String s = tagField("AdvancedNodes")+ " AdvancedSize="+nodesDistance.size()+
//				" FCSSize="+ fcsSize+
//				" Destination="+destination.getId()+			
//				" SenderDistance="+String.format("%.2f", senderDistance)+
//				" Nodes:Distance(";
//		for (NodeDistanceDestination nd : nodesDistance) {
//			s += nd.getNode().getId()+
//				"="+String.format("%.2f", nd.getDistance())+", ";
//        }
//		s+=")\n";
//		return s;
//	}
	
	
	/** Define o formato das informações referentes ao FCS */
	public String strFcs(int fcsSize, List <NodeId> nodes) {
		String s = tagField("FCS")+" Size="+ fcsSize+
				" Nodes=(";
		if (nodes == null) {
			s += "null";
		} else {
			for (NodeId node : nodes) {
				s += "Id="+node+", ";
	        }
		}
		s+=")\n";
		return s;
	}
	
	
	/*
	 * Strings para usar no print() e printw()
	 */
	
	/** Retorna o tempo restante para recebimento e envio de DATA */
	public String prtDataTime(PaxMacState paxState, PaxMacConfiguration paxConf, 
			PaxMacStateMachine paxStateMachine) {
		return "CtsDataTime="+paxState.getCtsDataTime()+
				" ReceiveTime="+paxState.getCtsDataTime()+
					" (remaining "+paxStateMachine.delayUntilCtsData()+")"+
				" SendTIme="+paxStateMachine.delayUntilCtsDataNextHop()+
					" (remaining "+paxStateMachine.delayUntilCtsDataNextHop()+")";
	}

	
	/** Retorna informações sobre o pacote do PaxMac */
	public String prtPkt(PaxMacPacket p, PaxMacConfiguration paxConf) {
		List <NodeId> fcsNodes = p.getFcsNodes();
		String fcs = "";
		int fcsSize;
		if (fcsNodes == null) {
			fcsSize = 0;
			fcs = "null";
		} else {
			fcsSize = fcsNodes.size();
			for (NodeId node : fcsNodes) {
				fcs += "Id="+node+", ";
	        }
		}
		 
		String s = "Sequence="+p.getSequenceNum()+
				" Receiver="+p.getReceiver()+
				" Sender="+p.getSender().getId()+
				" PreviousSender="+p.getPreviousReceiverNodeLv1()+
				" FinalReceiver="+p.getFinalReceiverNode()+
				" CtsDataDelay="+p.getSendCtsDataDelay()+	
					" (Time "+String.format("%1$,.4f",(p.getSendCtsDataDelay()+currentTimeSteps()))+")";
				if (p.getType() == PacketType.RTS) {
					s += " EndTxStepsRTS="+String.format("%1$,.4f", (paxConf.getStepsRTS()+currentTimeSteps()));
				} else if (p.getType() == PacketType.DATA) {
					s += " EndTxStepsDATA="+String.format("%1$,.4f", (paxConf.getStepsDATA()+currentTimeSteps()));
				}
				s += " Direction="+p.getDirection()+
				" FCSSize="+fcsSize+"" +
				" FCS=("+fcs+")" ;
		return s;
	}
	
	
	/** Retorna informações sobre o recebimento do pacote do PaxMac */
	public String prtReceivedPkt(PaxMacPacket p, PaxMacConfiguration paxConf, double delay) {
		String s = prtReceivedPktSender(p, delay)+
				" "+prtPkt(p, paxConf);
		return s;
	}
	
	
	/** Retorna informações sobre o sender do pacote PaxMac */
	public String prtReceivedPktSender(PaxMacPacket p, double delay) {
		String s = "Sender="+p.getSender().getId();
		if (delay > 0) {
			s += " SleepUntil="+(delay+currentTimeSteps());
		}
		return s;
	}
	
	
	/** Retorna informações sobre o reajuste do RTS no primeiro nó, quando o tempo de enviar DATA está
	chegando  */
	public String prtKeepSendingRts(PaxMacState paxState, PaxMacConfiguration paxConf, 
			PaxMacStateMachine paxStateMachine) {

		return "CtsDataTime="+paxState.getCtsDataTime()+
					" (remaining "+paxStateMachine.delayUntilCtsData()+")"+
				" StepsRTS="+paxConf.getStepsRTS()+
					" (over "+(paxStateMachine.delayUntilCtsData()-paxConf.getStepsRTS())+")"+
				" NewDataTime="+(paxConf.getStepsKeepData()+currentTimeSteps());
	}
	
	
	/** Retorna informações sobre a parada do envio de RTS para evitar colisão com DATA  */
	public String prtStopSendingRts(PaxMacState paxState, PaxMacConfiguration paxConf, 
			PaxMacStateMachine paxStateMachine) {

		return "CtsDataTime="+paxState.getCtsDataTime()+
				" ReceiveTime="+paxState.getCtsDataTime()+
					" (remaining "+paxStateMachine.delayUntilCtsData()+")"+
				" StepsRTS="+paxConf.getStepsRTS()+
				" StepsDATA="+paxConf.getStepsDATA()+
				" (over RTS+DATA"+(paxStateMachine.delayUntilCtsData()-paxConf.getStepsRTS()-
						paxConf.getStepsDATA())+")"+
				" (over RTS"+(paxStateMachine.delayUntilCtsData()-paxConf.getStepsRTS())+")"+
				" SleepUntil="+(paxStateMachine.delayUntilCtsData()+currentTimeSteps());
	}

	
	/** Retorna informações sobre o estado do nó */
	public String prtState(PaxMacState paxState) {
		return "State="+paxState.getState()+
				" CtsDataTime="+paxState.getCtsDataTime()+
				" PktType="+paxState.getSendingPktType()+
				" Delay="+paxState.getStateDuration()+
				" StartTime="+paxState.getStateStartTime()+
				" EndTime="+(paxState.getStateDuration()+currentTimeSteps())+
				" Sequence="+paxState.getStateSeqNum();
	}

	
	/** Retorna informações sobre o caminho */
	public String prtPath(PaxMacState paxState, PaxMacConfiguration paxConf, 
			PaxMacStateMachine paxStateMachine) {
		return 	"NextReceiverNode="+paxState.getNextReceiverNode()+
				" PreviousReceiverNode"+
				" Lv1="+paxState.getPreviousReceiverLv1()+
				" Lv2="+paxState.getPreviousReceiverLv2()+
				" Lv3="+paxState.getPreviousReceiverLv3()+
				" FinalReceiverNode="+paxState.getFinalReceiverNode()+
				" FcsSize="+paxState.getFcsSize()+
				" isChannelBusy="+paxState.isChannelBusy()+
				" "+prtDataTime(paxState, paxConf, paxStateMachine);
	}
	
	
	/** Retorna informações quando o nó para de esperar por um CTS  */
	public String prtStopWaitingCts(PaxMacState paxState, PaxMacConfiguration paxConf, 
			PaxMacStateMachine paxStateMachine, double dataDelay) {

		return "ReceiveTime="+paxState.getCtsDataTime()+
					" (remaining "+paxStateMachine.delayUntilCtsData()+")"+
				" StepsRTS="+paxConf.getStepsRTS()+
				" (over "+(paxStateMachine.delayUntilCtsData()-paxConf.getStepsRTS())+")"+
				" SleepUntil="+(dataDelay+currentTimeSteps());
	}
	
}
