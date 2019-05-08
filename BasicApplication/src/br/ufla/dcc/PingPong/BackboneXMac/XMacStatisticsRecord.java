package br.ufla.dcc.PingPong.BackboneXMac;

import java.util.ArrayList;

public class XMacStatisticsRecord {

	static private XMacStatisticsRecord instance = null;
	
	/** origem da mensagem corrente */
	public int origem_da_msg_atual;
	/** flag para terminar a simulação se não for encaminhar mais mensagens  */
	public boolean terminar;
	/** quantas vezes ocorreu o número I de nós nas lentes, onde I indica a posição no vetor nosNaLente  */
	public int nosNaLente[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	/** o mesmo do anterior, mas considerando sempre threshold zero, lente máxima  */
	public int nosNaLente_Thzero[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	/** total de saltos gastos do início ao fim da simulação  */
	public int totalSaltos = 0;
	/** total de preâmbulos enviados do início ao fim da simulação  */
	public int totalPreambulos = 0;
	/** total de saltos gastos para enviar de uma App até a próxima: do 1 para 2, do 2 para 3, etc.  */
	public int parcialSaltos[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	/** total de preâmbulos usados para enviar de uma App até a próxima: do 1 para 2, do 2 para 3, etc.  */
	public int parcialPreambulos [] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	/** total de tempo gasto para enviar de uma App até a próxima: do 1 para 2, do 2 para 3, etc.  */
	public double tempoParcial [] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	/** número de nós encontrados na lente em cada salto  */
	public ArrayList<Integer> sequenciaLente = null;    
	/** melhores ranks encontrados em cada salto  */
	public ArrayList<Double> melhoresRanks = null;

	static XMacStatisticsRecord getInstance() {
		if (instance==null) {
			instance = new XMacStatisticsRecord();
		}
		return instance;
	}
	
	public XMacStatisticsRecord() {
			sequenciaLente = new ArrayList<Integer>();
			melhoresRanks = new ArrayList<Double>();
	}

	public void contaPreambulos (int nPre) {
		parcialPreambulos[origem_da_msg_atual] += nPre;
	}
	
	public void contaSaltos (int nSaltos) {
		parcialSaltos[origem_da_msg_atual] += nSaltos;
	}
	
	public void contaTempo (double tempo) {
		tempoParcial[origem_da_msg_atual] = tempo;
	}
	
	public void apresentaDados(int idNode) {
				
		terminar = true;
		int id = idNode > 30 ? 30 : idNode;		
		
		for (int i = 1; i <= origem_da_msg_atual ; i++) {
			totalSaltos += parcialSaltos[i];
			totalPreambulos += parcialPreambulos[i];
		}	
		
		System.out.println("R--> ,    Noh =   , " + id + " , Saltos = , " + totalSaltos + " , TotalPre = , " 
							+ totalPreambulos + " , Tempo(seg) = , " + tempoParcial [origem_da_msg_atual]);
				
		System.out.print("R--> , saltos : , ");
		for (int i = 1; i <= origem_da_msg_atual ; i++)	System.out.print( parcialSaltos[i] + " , ");
		System.out.println(" ");
				
		System.out.print("R--> , preambulos : , ");
		for (int i = 1; i <= origem_da_msg_atual ; i++)	System.out.print( parcialPreambulos[i] + " , ");
		System.out.println(" ");
				
		System.out.print("R--> , tempos : , ");
		for (int i = 1; i <= origem_da_msg_atual ; i++)	System.out.print( tempoParcial[i] + " , ");
		System.out.println(" ");
	}
}
