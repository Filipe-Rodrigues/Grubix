package br.ufla.dcc.PingPong.EXMac;

/**
 *  Constantes de configuração do X-MAC;
 *  
 *  Usada pela XMac.java
 *
 * 	@author João Giacomin
 *  @version 18/03/2019
 */
public class EXMacConstants {
	
	/** Bits por segundo = 250kb/s (IEEE802.15.4) */
	public static final int BPS = 250000;
	
	/** Essa constante é o valor de sinal de transmissão padrão */
	public static final double SS_DEFAULT = 1;
	
	/** Número máximo de tentativas de retransmitir um Data Packet */
	public static final int NUM_MAX_RETRANSMISSIONS = 5;
	
	/** Comprimento da mensagem de dados em bits, padrão = 512 bits = 64 bytes. 
	 *  Transmitido a 250 kbps, duração = 2,048 ms. O rádio CC2420 do MicaZ tem um buffer de 128 bytes */
	public static final int DATA_LENGTH = 512;
	
	/** Comprimento da mensagem de preâmbulo em bits, padrão = 256 bits = 32 bytes. Transmitido a 250 kbps, duração = 1,024 ms */
	public static final int RTS_LENGTH = 256;
	
	/** Comprimento da mensagem de CTS em bits, padrão = 128 bits = 16 bytes. Transmitido a 250kbps, duração = 0,512 ms */
	public static final int CTS_LENGTH = 128;
	
	/** Comprimento da mensagem de ACK em bits, padrão = 128 bits = 16 bytes. Transmitido a 250kbps, duração = 0,512 ms  */
	public static final int ACK_LENGTH = 128;
	
}
