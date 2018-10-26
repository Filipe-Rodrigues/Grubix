package br.ufla.dcc.PingPong.PaxMac;

public enum  ExecutionMode {
	/** Modo padrão, tamanho do FCS é fixo */
	DEFAULT,
	
	/** FCS é maior no fim, encaminhamento do RTS não para  */
	LARGER_FCS_END,
	
	/** FCS é maior no início, encaminhamento do RTS não para e o caminho é otimizado  */
	LARGER_FCS_START
	
}


