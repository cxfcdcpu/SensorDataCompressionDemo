// $Id: BaseStationP.nc,v 1.12 2010-06-29 22:07:14 scipio Exp $

/*									tab:4
 * Copyright (c) 2000-2005 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the University of California nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (c) 2002-2005 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

/*
 * @author Phil Buonadonna
 * @author Gilman Tolle
 * @author David Gay
 * Revision:	$Id: BaseStationP.nc,v 1.12 2010-06-29 22:07:14 scipio Exp $
 */
  
/* 
 * BaseStationP bridges packets between a serial channel and the radio.
 * Messages moving from serial to radio will be tagged with the group
 * ID compiled into the BaseStation, and messages moving from radio to
 * serial will be filtered by that same group id.
 */

#include "AM.h"
#include "Serial.h"
#include "ZCompression_new.h"
#include <UserButton.h>
//#include "Configuration.h"


module BaseStationP @safe() {
  uses {
    interface Boot;
    interface SplitControl as SerialControl;
    interface SplitControl as RadioControl;

    interface AMSend as UartSend[am_id_t id];
    interface Packet as UartPacket;
    interface AMPacket as UartAMPacket;
    
    interface AMSend as RadioSend[am_id_t id];
    interface Receive as RadioReceive[am_id_t id];
    interface Receive as RadioSnoop[am_id_t id];
    interface Packet as RadioPacket;
    interface AMPacket as RadioAMPacket;

    

    interface Timer<TMilli> as time;
    interface Notify<button_state_t>;
    interface Leds;
  }
}

implementation
{


  uint16_t previous_tmp;
  uint16_t current_tmp;
  uint16_t delta_tmp;
  uint16_t previous_humi;
  uint16_t current_humi;
  uint16_t delta_humi;
  uint16_t previous_vLight;
  uint16_t current_vLight;
  uint16_t delta_vLight;
  uint16_t previous_voltage;
  uint16_t current_voltage;
  uint16_t delta_voltage;

  uint16_t count;

  uint8_t sensing;
  uint8_t zcompressing;
  uint8_t lec;
  uint8_t tinyPack;
  uint8_t mode;

  message_t  uartQueueBufs[UART_QUEUE_LEN];
  message_t  mmm;
  message_t  * ONE_NOK uartQueue[UART_QUEUE_LEN];
  uint8_t    uartIn, uartOut;
  bool       uartBusy, uartFull;

  message_t  radioQueueBufs[RADIO_QUEUE_LEN];
  message_t  * ONE_NOK radioQueue[RADIO_QUEUE_LEN];
  uint8_t    radioIn, radioOut;
  bool       radioBusy, radioFull,bufFull,isCascading;
  uint8_t* buf1;
  uint8_t* buf2;
  uint8_t* out1;
  uint8_t* out2;
  uint8_t* out3;
  uint8_t* buf3;
  uint8_t* buf4;

  uint8_t matrix[BufSize][BufItemSize+4];
  uint8_t cascaded[PacketSize];
  uint8_t start;
  uint8_t end;

  uint16_t interval;

  int preMax;
  int preMin;
  int max;
  int min;
  uint8_t total;
  uint8_t itemNum;


  task void radioSendTask();



  void dropBlink() {
    call Leds.led0Toggle();
  }

  void failBlink() {
    call Leds.led1Toggle();
  }

  event void Boot.booted() {
    uint8_t i;
        buf1=malloc(BufItemSize);
        buf2=malloc(BufItemSize);
        buf3=malloc(BufItemSize);
        buf4=malloc(BufItemSize);
	out1=malloc(BufItemSize);
        out2=malloc(BufItemSize);
        out3=malloc(BufItemSize);
   interval=DEFAULT_INTERVAL;
   previous_tmp=1;
   current_tmp=1;
   delta_tmp=1;
   previous_humi=1;
   current_humi=1;
   delta_humi=1;
   previous_vLight=1;
   current_vLight=1;
   delta_vLight=1;
   previous_voltage=1;
   current_voltage=1;
   delta_voltage=1;
   sensing=1;
   zcompressing=0;
   lec=0;
   tinyPack=0;
   mode=No_Com;
   start=0;
   end=0;
   count=0;
   isCascading=FALSE;


   preMax=0;
   preMin=127;
   max=0;
   min=127;
   total=1;
   itemNum=0;
   bufFull=FALSE;

    call Notify.enable();

    for (i = 0; i < UART_QUEUE_LEN; i++)
      uartQueue[i] = &uartQueueBufs[i];
    uartIn = uartOut = 0;
    uartBusy = FALSE;
    uartFull = TRUE;

    for (i = 0; i < RADIO_QUEUE_LEN; i++)
      radioQueue[i] = &radioQueueBufs[i];
    radioIn = radioOut = 0;
    radioBusy = FALSE;
    radioFull = TRUE;

    if (call RadioControl.start() == EALREADY)
      radioFull = FALSE;
    if (call SerialControl.start() == EALREADY)
      uartFull = FALSE;
  }

  event void RadioControl.startDone(error_t error) {
    if (error == SUCCESS) {
      radioFull = FALSE;
    }
  }

  event void SerialControl.startDone(error_t error) {
    if (error == SUCCESS) {
      uartFull = FALSE;
      call time.startPeriodic(interval);
    }
  }

  event void SerialControl.stopDone(error_t error) {}
  event void RadioControl.stopDone(error_t error) {}


  message_t* ONE receive(message_t* ONE msg, void* payload, uint8_t len);
  
  event message_t *RadioSnoop.receive[am_id_t id](message_t *msg,
						    void *payload,
						    uint8_t len) {
    return receive(msg, payload, len);
  }
  
  event message_t *RadioReceive.receive[am_id_t id](message_t *msg,
						    void *payload,
						    uint8_t len) {
    return receive(msg, payload, len);
  }



  event void Notify.notify( button_state_t state ) {
    if ( state == BUTTON_PRESSED ) {
      mode=mode==No_Com?Only_Cas:No_Com;
      start=0;
      end=0;
      bufFull=FALSE;
      total=1;
   preMax=0;
   preMin=127;
   max=0;
   min=127;
   isCascading=FALSE;
      call Leds.led1On();
    } else if ( state == BUTTON_RELEASED ) {
      call Leds.led1Off();
    }
  }




  message_t* receive(message_t *msg, void *payload, uint8_t len) {
    am_id_t amID;
    uint8_t* controlMsg;
    message_t *ret = msg;
    uint8_t src;

    uint8_t iter;
    uint8_t jter;
    uint8_t preLen;

    uint8_t count1;
    uint8_t count2;

    uint8_t differ;
    uint8_t* loadPointer;
    uint8_t check;

    if(call RadioAMPacket.isForMe(msg)){

        amID=call RadioAMPacket.type(msg);
	
	failBlink();
	if(amID==AM_TYPE_BASE){
	    controlMsg=payload;
	    if(controlMsg[0]==S1_Setting&&controlMsg[1]==S2_Setting&&controlMsg[2]==S3_Setting&&controlMsg[3]==S4_Setting&&controlMsg[12]==S1_Reset&&controlMsg[13]==S2_Reset&&controlMsg[14]==S3_Reset&&controlMsg[15]==S4_Reset){
		if(controlMsg[5]==RESET_CODE){
		
         	    WDTCTL = WDT_ARST_1_9;
         	    while(1);

		}

		    mode=controlMsg[7];
		//other compression scheme setting below;
//v3
		radioIn=radioOut=0;
		start=end=0;

	    }


	}
	else if(amID==21){
    	     src = call RadioAMPacket.source(msg);

	     //memset(call UartSend.getPayload[43](uartQueue[0],32),start,1);
	     //memset(call UartSend.getPayload[43](uartQueue[0],32)+1,end,1);
	     //memset(call UartSend.getPayload[43](uartQueue[0],32)+2,bufFull,1);
	     //memset(call UartSend.getPayload[43](uartQueue[0],32)+3,radioFull,1);


atomic{


		matrix[end][0]=len;
//id which < 256; need check if lower bits
		matrix[end][1]=src;
//timestamp		
		//matrix[end][2]=payload[0];
		memcpy(matrix[end]+2,payload,2);


//sensing data
		memcpy(matrix[end]+4,payload+3,len-3);

//no compress packet like: group, time1BE, time2BE, packetLen,leafnodeID,leafTime1,leafTime2,payloads
//compressed packet: group, time1BE, time2BE, packetLen, leafnodeID

//direct forward here.
	    if(mode==No_Com&&!radioFull){
		loadPointer=call RadioSend.getPayload[AM_TYPE_INTER_RAW](radioQueue[radioIn],BufItemSize);
		call RadioPacket.setPayloadLength(radioQueue[radioIn],BufItemSize);
		call RadioAMPacket.setType(radioQueue[radioIn], AM_TYPE_INTER_RAW);

		count++;
		count1=count;
		count2=count>>8;

		memset(loadPointer,controlGroup,1);
	        memcpy(loadPointer+1, &count2, 1);
	        memcpy(loadPointer+2, &count1, 1);

		memcpy(loadPointer+3,matrix[end],matrix[end][0]+1);
		if(BufItemSize>matrix[end][0]+4)
		memset(loadPointer+matrix[end][0]+4,0,BufItemSize-matrix[end][0]-4);
		memset(loadPointer+BufItemSize-1,lastByte,1);

		    if (++radioIn >= RADIO_QUEUE_LEN)
	   	 	radioIn = 0;
	  	    if (radioIn == radioOut)
	   		radioFull = TRUE;

	  	    if (!radioBusy){
	     		post radioSendTask();
	      		radioBusy = TRUE;
	    	    }

		end=(end+1)%BufSize;
		if(end==start)bufFull=TRUE;
	    }
//cascade here
	    else if(mode==Only_Cas&&!isCascading){


		isCascading=TRUE;
		preMax=max;
		preMin=min;
		max=max<len?len:max;
		min=min>len?len:min;

		total+=len;
	     //memset(call UartSend.getPayload[AM_TYPE_INTER](uartQueue[0],32)+4,total,1);
	     //memset(call UartSend.getPayload[AM_TYPE_INTER](uartQueue[0],32)+5,radioIn,1);
	     //memset(call UartSend.getPayload[AM_TYPE_INTER](uartQueue[0],32)+6,radioOut,1);
	     //memset(call UartSend.getPayload[AM_TYPE_INTER](uartQueue[0],32)+7,radioBusy,1);

	     //call UartSend.send[AM_TYPE_INTER](63,uartQueue[0],8);

		if(total>cascadeSize+preMin-preMax){

 	            check=4;
		    preLen=preMax;
		    loadPointer=call RadioSend.getPayload[AM_TYPE_INTER](radioQueue[radioIn],PacketSize);
		    call RadioAMPacket.setSource(radioQueue[radioIn], TOS_NODE_ID);
		    call RadioAMPacket.setType(radioQueue[radioIn], AM_TYPE_INTER);
//testing group or control group
	            memset(loadPointer,controlGroup,1);

		    count++;
		    count1=count;
		    count2=count>>8;

	            memcpy(loadPointer+1, &count2, 1);
	            memcpy(loadPointer+2, &count1, 1);

                    memset(loadPointer+3,preLen,1);
		    loadPointer=loadPointer+4;
		    call RadioPacket.setPayloadLength(radioQueue[radioIn],PacketSize);


		    for(iter=preMax;iter>=preMin;iter--){
			jter=start;

			while(jter!=end){

			    if(matrix[jter][0]==iter){
				differ=preLen-matrix[jter][0];
				if(differ>0)
				memset(loadPointer,0,differ);
				memcpy(loadPointer+differ,matrix[jter]+1,iter);
  				loadPointer=loadPointer+preLen;
				check=check+preLen;
				if(differ>0){
				    preLen=matrix[jter][0];
				}
			    }
			    jter=(jter+1)%BufSize;
			}
		    }
		    if(PacketSize>check+1)
		    memset(loadPointer,0,PacketSize-check-1);
		    memset(loadPointer+PacketSize-check-1,lastByte,1);

		    start=end;
		    bufFull=FALSE;
		    total=len+1;
		    max=len;
		    min=len;

		    if (++radioIn >= RADIO_QUEUE_LEN)
	   	 	radioIn = 0;
	  	    if (radioIn == radioOut)
	   		radioFull = TRUE;

	  	    if (!radioBusy){
	     		post radioSendTask();
	      		radioBusy = TRUE;
	    	    }

		}

		end=(end+1)%BufSize;
		if(end==start)bufFull=TRUE;
		isCascading=FALSE;
	    }
	    else dropBlink();
}
	}else dropBlink();

    }

    return ret;
  }

  uint8_t tmpLen;
  
  
  event void UartSend.sendDone[am_id_t id](message_t* msg, error_t error) {
    if (error != SUCCESS)
      failBlink();
      
  }

  

  task void radioSendTask() {
    uint8_t len;
    am_id_t id;
    am_addr_t addr,source;
    message_t* msg;

    atomic
      if (radioIn == radioOut && !radioFull)
	{
	  radioBusy = FALSE;
	  
	  return;
	}

    msg = radioQueue[radioOut];
    len = call RadioPacket.payloadLength(msg);
    addr = TOS_NODE_ID;
    source = call RadioAMPacket.source(msg);
    id = call RadioAMPacket.type(msg);
     
    call RadioPacket.clear(msg);
    call RadioAMPacket.setSource(msg, source);
    call RadioAMPacket.setGroup(msg, AM_GROUP);



    if (call RadioSend.send[id]((TOS_NODE_ID-1)/4, msg, len) == SUCCESS)
      call Leds.led2Toggle();

    else
      {
	dropBlink();
	post radioSendTask();
      }
   
  }

  event void RadioSend.sendDone[am_id_t id](message_t* msg, error_t error) {
    if (error != SUCCESS)
      dropBlink();
    else{
      atomic
	if (msg == radioQueue[radioOut])
	  {
	    if (++radioOut >= RADIO_QUEUE_LEN)
	      radioOut = 0;
	    if (radioFull)
	      radioFull = FALSE;

	  }

	
    }
    
    post radioSendTask();
  }


  event void time.fired() {

  }
}  
