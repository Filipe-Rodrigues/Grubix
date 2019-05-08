/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.PingPong.BackboneXMac;

/**
 * Esse enum implementa os possiveis estados da MAC de um nó sensor, que utiliza X_MAC. 
 * 
 * Cada estado tem um tempo de duração previsto. Os tempos são definidos na classe X_MACConstants.
 * 
 * Não é necessário definir o estado WAITING_MSG, será usado o CS no lugar, visto que ao final do CS,
 * se não chegar mensagem, o receptor simplesmente volta para SLEEP.
 * 
 * Os estados WILL_SEND_ACK e WILL_SEND_MSG são necessários para que não se inicie o envio 
 * da mensagem (ou ACK) logo no início do intervalo, evitando de pegar o Rx despreparado.
 * 
 *  
 * @author João Giacomin
 */

public enum XMacStateTypes {
	/** Estado inativo, corresponde ao estado em que o rádio e as principais funções do nó estão desligados */
	SLEEP,
	
	/** Executando uma prospecção de portadora (Carrier Sense) para verificar se algum vizinho está transmitindo */
	CS,
	
	/** Executando uma prospecção de portadora (Carrier Sense) para início de transmissão, para saber se poderá enviar preâmbulos */
	CS_START,
	
	/** Esperando um earlyACK após envio de preâmbulo*/
	WAITING_CTS,
	
	/** Esperando um ACK após envio de dados*/
	WAITING_ACK,
	
	/** Esperando terminar a recepção de dados */
	WAITING_DATA,
	
	/** MAC está esperando o rádio terminar de enviar uma mensagem, a PHY sinalizará o término do envio */
	SENDING_MSG,
	
	/** MAC enviará mensagem para a PHY */
	WILL_SEND_MSG
	
}