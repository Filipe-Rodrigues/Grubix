package br.ufla.dcc.PingPong.PaxMac;

/**
 *  Constantes de configuração da MAC;
 *
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 04/07/2016
 */
public class PaxMacConstants {
	
	/** Define se é para usar sementes randômicas ou baseadas no id do nó, ao gerar valores randômicos */
	public static final boolean RANDOM_SEED = false;
	
	/** Quantidade de vezes que será feita a espera/envio CTS-DATA antes de fazer o backoff */
	public static final int CTSDATA_BACKOFF_WATCHDOG = 4;
	
	/** Slots de tempo que serão usados como backoff até a passagem do dado do outro caminho */
	public static final int CTSDATA_BACKOFF_DURATION = 4;

	/** Quantidade máxima de vezes que o nó ira acordar para esperar/enviar um dado que não chegou */
	public static final int MAX_CTSDATA_RETRY = 30;
	
	/** Define se é para usar reserva de canal (vizinhos que recebem RTS, acordam depois que o dado passar) */
	public static final boolean CHANNEL_RESERVATION = true;
	
	/** Bits por segundo = 250kb/s (IEEE802.15.4) - 25 bits por step */
	public static final int BPS = 250000;
	
	/** Essa constante é o valor de sinal de transmissão padrão */
	public static final double SS_DEFAULT = 40;
	
	/** Tamanho máximo do buffer de pacotes da camada MAC */
	public static final int PACKET_BUFFER_MAX_SIZE = 5;
	
	/** Valor máximo de uma sequência, para identificador de mensagem ou para preâmbulos. Deve ser sempre 2^N-1 */
	public static final int MAX_SEQUENCE = 0xFFFF;

	/** Comprimento da mensagem de dados em bits, padrão = 512 bits = 64 bytes. Transmitido a 250 kbps, duração = 2,048 ms
	O rádio CC2420 do MicaZ tem um buffer de 128 bytes */
	//public static final int DATA_LENGTH = 512; // Obtido na application.xml
	
	/** Comprimento da mensagem de preâmbulo em bits, padrão = 256 bits = 32 bytes. Transmitido a 250 kbps, duração = 1,024 ms */
	public static final int RTS_LENGTH = 128;
	
	/** Comprimento da mensagem de CTS em bits, padrão = 128 bits = 16 bytes. Transmitido a 250kbps, duração = 0,512 ms */
	public static final int CTS_LENGTH = 128;
	
	/** Comprimento da mensagem de ACK em bits, padrão = 128 bits = 16 bytes. Transmitido a 250kbps, duração = 0,512 ms  */
	public static final int ACK_LENGTH = 92;
	
	/** Comprimento da mensagem de CTS para avisar que o canal está livre para o envio do dado  */
	public static final int CTS_DATA_LENGTH = 92;
	
}
