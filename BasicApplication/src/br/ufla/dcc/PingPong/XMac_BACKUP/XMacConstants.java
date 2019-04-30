package br.ufla.dcc.PingPong.XMac;

/**
 *  Constantes de configuração da MAC;
 *
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 04/07/2016
 */
public class XMacConstants {
	
	/** Bits por segundo = 250kb/s (IEEE802.15.4) */
	public static final int BPS = 250000;
	
	/** Essa constante é o valor de sinal de transmissão padrão */
	public static final double SS_DEFAULT = 30;
	
	/** Tamanho máximo do buffer de pacotes da camada MAC */
	public static final int PACKET_BUFFER_MAX_SIZE = 5;
	
	/** Número máximo de tentativas de retransmitir um Data Packet */
	public static final int NUM_MAX_RETRANSMISSIONS = 5;
	
	/** Valor máximo de uma sequencia, para identificador de mensagem ou para preâmbulos. Deve ser sempre 2^N-1 */
	public static final int MAX_SEQUENCE = 0xFFFF;

	/** Comprimento da mensagem de dados em bits, padrão = 512 bits = 64 bytes. Transmitido a 250 kbps, duração = 2,048 ms
	O rádio CC2420 do MicaZ tem um buffer de 128 bytes */
	public static final int DATA_LENGTH = 512;
	
	/** Comprimento da mensagem de preâmbulo em bits, padrão = 256 bits = 32 bytes. Transmitido a 250 kbps, duração = 1,024 ms */
	public static final int RTS_LENGTH = 256;
	
	/** Comprimento da mensagem de CTS em bits, padrão = 128 bits = 16 bytes. Transmitido a 250kbps, duração = 0,512 ms */
	public static final int CTS_LENGTH = 128;
	
	/** Comprimento da mensagem de ACK em bits, padrão = 128 bits = 16 bytes. Transmitido a 250kbps, duração = 0,512 ms  */
	public static final int ACK_LENGTH = 128;
	
}
