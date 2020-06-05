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
#include "universal_functions_new.h"


module BaseStationP @safe() {
  uses {
    interface Boot;
    interface SplitControl as SerialControl;
    interface SplitControl as RadioControl;

    interface AMSend as UartSend[am_id_t id];
    interface Receive as UartReceive[am_id_t id];
    interface Packet as UartPacket;
    interface AMPacket as UartAMPacket;
    
    interface AMSend as RadioSend[am_id_t id];
    interface Receive as RadioReceive[am_id_t id];
    interface Receive as RadioSnoop[am_id_t id];
    interface Packet as RadioPacket;
    interface AMPacket as RadioAMPacket;


    interface Timer<TMilli> as time;
 interface Read<uint16_t> as Temperature;
 interface Read<uint16_t> as Humidity;
 interface Read<uint8_t> as vLight;
 interface Read<uint8_t> as iLight;



    interface Leds;
  }
}

implementation
{
  enum {
    UART_QUEUE_LEN = 3,
    RADIO_QUEUE_LEN = 3,
  };

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

  uint16_t* sensorData;

  uint8_t mode;
  uint16_t count;

  uint8_t sensing;
  uint8_t zcompressing;
  uint8_t lec;
  uint8_t tinyPack;
  uint16_t interval;

  message_t  uartQueueBufs[UART_QUEUE_LEN];
  message_t  mmm;
  message_t  * ONE_NOK uartQueue[UART_QUEUE_LEN];
  uint8_t    uartIn, uartOut;
  bool       uartBusy, uartFull;

  message_t  radioQueueBufs[RADIO_QUEUE_LEN];
  message_t  * ONE_NOK radioQueue[RADIO_QUEUE_LEN];
  uint8_t    radioIn, radioOut;
  bool       radioBusy, radioFull;
  uint8_t* buf1;
  uint8_t* buf2;
  uint8_t* out1;
  uint8_t* out2;
  uint8_t* out3;
  uint8_t* buf3;
  uint8_t* buf4;

//v3
  uint8_t ini_H;
  uint8_t hyb_inter;

  task void uartSendTask();
  task void radioSendTask();
void wdt_init(void) __attribute__((naked)) __attribute__((section(".init3")));
  void dropBlink() {
    call Leds.led2Toggle();
  }

  void failBlink() {
    call Leds.led2Toggle();
  }

  event void Boot.booted() {
    uint8_t i;
        buf1=malloc(32);
        buf2=malloc(32);
        buf3=malloc(32);
        buf4=malloc(32);
	out1=malloc(32);
        out2=malloc(32);
        out3=malloc(32);
	sensorData=malloc(17);

   mode=Z_Com;
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
   sensing=0;
   zcompressing=1;
   lec=0;
   tinyPack=0;
   interval=DEFAULT_INTERVAL;
   count=0;
//v3
   ini_H=ini_head;
   hyb_inter=hyb_interval;

   *sensorData=NREADING;




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



void wdt_init(void)
{
    MCUSR = 0;
    wdt_disable();

    return;
}



  event void RadioControl.startDone(error_t error) {
    if (error == SUCCESS) {
      radioFull = FALSE;
    }
  }

  event void SerialControl.startDone(error_t error) {
    if (error == SUCCESS) {
      uartFull = FALSE;

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


  message_t* receive(message_t *msg, void *payload, uint8_t len) {
    am_id_t amID;
    uint8_t* controlMsg;
    message_t *ret = msg;

    call Leds.led0Toggle();
    if(call RadioAMPacket.isForMe(msg)){
        amID=call RadioAMPacket.type(msg);
	
	if(amID==AM_TYPE_BASE&&len==controlPacketSize){
	    controlMsg=payload;
	    if(controlMsg[0]==S1_Setting&&controlMsg[1]==S2_Setting&&controlMsg[2]==S3_Setting&&controlMsg[3]==S4_Setting&&controlMsg[12]==S1_Reset&&controlMsg[13]==S2_Reset&&controlMsg[14]==S3_Reset&&controlMsg[15]==S4_Reset){
		if(controlMsg[5]==RESET_CODE){
		
      		do                          
		{                           
   		 wdt_enable(WDTO_15MS);  
   		 for(;;)                 
   		 {                       
   		 }                       
		} while(0);
		wdt_init();

		}
		    mode=controlMsg[6];
		    interval=(controlMsg[10]<<8)+controlMsg[11];
		    sensing=controlMsg[8];
		//other compression scheme setting below;
		if(controlMsg[9]==START_TIMER&&!(call time.isRunning()))
      		    call time.startPeriodic(interval);
		else if(controlMsg[9]==STOP_TIMER)
		    call time.stop();
	    }
	}

    }
    return ret;
  }

  uint8_t tmpLen;
  
  task void uartSendTask() {
    uint8_t len;
    am_id_t id;
    am_addr_t addr, src;
    message_t* msg;
    am_group_t grp;
    atomic
      if (uartIn == uartOut && !uartFull)
	{
	  uartBusy = FALSE;
	  return;
	}

    msg = uartQueue[uartOut];
    tmpLen = len = call RadioPacket.payloadLength(msg);
    id = call RadioAMPacket.type(msg);
    addr = call RadioAMPacket.destination(msg);
    src = call RadioAMPacket.source(msg);
    grp = call RadioAMPacket.group(msg);
    call UartPacket.clear(msg);
    call UartAMPacket.setSource(msg, src);
    call UartAMPacket.setGroup(msg, grp);

    if (call UartSend.send[id](addr, uartQueue[uartOut], len) == SUCCESS)
      call Leds.led1Toggle();
    else
      {
	failBlink();
	post uartSendTask();
      }
  }

  event void UartSend.sendDone[am_id_t id](message_t* msg, error_t error) {
    if (error != SUCCESS)
      failBlink();
    else
      atomic
	if (msg == uartQueue[uartOut])
	  {
	    if (++uartOut >= UART_QUEUE_LEN)
	      uartOut = 0;
	    if (uartFull)
	      uartFull = FALSE;
	  }
    post uartSendTask();
  }

  event message_t *UartReceive.receive[am_id_t id](message_t *msg,
						   void *payload,
						   uint8_t len) {
    message_t *ret = msg;
    bool reflectToken = FALSE;

    atomic
      if (!radioFull)
	{
	  reflectToken = TRUE;
	  ret = radioQueue[radioIn];
	  radioQueue[radioIn] = msg;
	  if (++radioIn >= RADIO_QUEUE_LEN)
	    radioIn = 0;
	  if (radioIn == radioOut)
	    radioFull = TRUE;

	  if (!radioBusy)
	    {
	      post radioSendTask();
	      radioBusy = TRUE;
	    }
	}
      else
	dropBlink();

    if (reflectToken) {
      //call UartTokenReceive.ReflectToken(Token);
    }
    
    return ret;
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
    len = call UartPacket.payloadLength(msg);
    addr = call UartAMPacket.destination(msg);
    source = call UartAMPacket.source(msg);
    id = call UartAMPacket.type(msg);

    call RadioPacket.clear(msg);
    call RadioAMPacket.setSource(msg, source);
    
    if (call RadioSend.send[id](addr, msg, len) == SUCCESS)
      call Leds.led0Toggle();
    else
      {
	failBlink();
	post radioSendTask();
      }
  }

  event void RadioSend.sendDone[am_id_t id](message_t* msg, error_t error) {
    if (error != SUCCESS)
      failBlink();
    else
      atomic
	if (msg == radioQueue[radioOut])
	  {
	    if (++radioOut >= RADIO_QUEUE_LEN)
	      radioOut = 0;
	    if (radioFull)
	      radioFull = FALSE;
	  }
    
    post radioSendTask();
  }


  event void time.fired() {
    uint8_t count1;
    uint8_t count2;
    uint8_t* loadPointer;
    if(sensing==1){
        call Temperature.read();
        call Humidity.read();
        call vLight.read();
        call iLight.read();
	count++;
	count1=count;
	count2=count>>8;
        if(mode!=No_Com&&mode!=Only_Cas){
	    delta_tmp=current_tmp>previous_tmp?((current_tmp-previous_tmp)<<1):(((previous_tmp-current_tmp)<<1)+1);
		previous_tmp=current_tmp;
	    delta_humi=current_humi>previous_humi?((current_humi-previous_humi)<<1):(((previous_humi-current_humi)<<1)+1);
		previous_humi=current_humi;
	    delta_vLight=get_delta_value(previous_vLight,current_vLight);
		previous_vLight=current_vLight;
	    delta_voltage=get_delta_value(previous_voltage,current_voltage);
		previous_voltage=current_voltage;
//v3
	    if(ini_H){
		delta_tmp=current_tmp<<1;
		delta_humi=current_humi<<1;
		delta_vLight=current_vLight<<1;
		delta_voltage=current_voltage<<1;
		ini_H--;
            }
	    if(count%hyb_inter==0){
		delta_tmp=current_tmp<<1;
		delta_humi=current_humi<<1;
		delta_vLight=current_vLight<<1;
		delta_voltage=current_voltage<<1;
            }
//v3 end
            convertToArray(buf1,delta_tmp);
	    convertToArray(buf2,delta_humi);
            convertToArray(buf3,delta_vLight);
	    convertToArray(buf4,delta_voltage);
	    sensorData[1]=delta_tmp;
	    sensorData[2]=delta_humi;
	    sensorData[3]=delta_vLight;
	    sensorData[4]=delta_voltage;

		call RadioAMPacket.setSource(&mmm,TOS_NODE_ID);
		call RadioAMPacket.setGroup(&mmm,AM_GROUP);

	    if(mode==Z_Com){
	        ZCompress_2(buf1,buf2,out1,0);
	        ZCompress_2(buf3,buf4,out2,0);
	        ZCompress_2(out1,out2,out3,0);
		loadPointer=call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize);
	        memcpy(loadPointer, &count2, 1);
	        memcpy(loadPointer+1, &count1, 1);
	        memcpy(loadPointer+2, out3, *out3+1);


                if (call RadioSend.send[AM_TYPE_LEAF]((TOS_NODE_ID-1)/4, &mmm, *out3+3) == SUCCESS)
                   call Leds.led2Toggle();


	    }
	    else if(mode==LEC_Com){

		compress_LEC(sensorData,out3);
		loadPointer=call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize);
	        memcpy(loadPointer, &count2, 1);
	        memcpy(loadPointer+1, &count1, 1);
	        memcpy(loadPointer+2, out3, *out3+1);

                if (call RadioSend.send[AM_TYPE_LEAF]((TOS_NODE_ID-1)/4, &mmm, *out3+3) == SUCCESS)
                   call Leds.led2Toggle();

	    }
	    else if(mode==TinyPack_Com){


		compress_TinyPack(sensorData,out3);
		loadPointer=call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize);
	        memcpy(loadPointer, &count2, 1);
	        memcpy(loadPointer+1, &count1, 1);
	        memcpy(loadPointer+2, out3, *out3+1);

                if (call RadioSend.send[AM_TYPE_LEAF]((TOS_NODE_ID-1)/4, &mmm, *out3+3) == SUCCESS)
                   call Leds.led2Toggle();



	    }
	    else if(mode==FELACS_Com){






	    }


	}
	else if(mode==No_Com||mode==Only_Cas){
	    call RadioAMPacket.setSource(&mmm,TOS_NODE_ID);
	    call RadioAMPacket.setGroup(&mmm,AM_GROUP);
	    memcpy(call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize), &count2, 1);
	    memcpy(call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize)+1, &count1, 1);
	    memset(call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize)+2, NREADINGS_4*2, 1);	    

	    memcpy(call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize)+3, &current_tmp, 2);
	    memcpy(call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize)+5, &current_humi, 2);
	    memcpy(call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize)+7, &current_vLight, 2);
	    memcpy(call UartSend.getPayload[AM_TYPE_LEAF](&mmm, BufItemSize)+9, &current_voltage, 2);



                if (call RadioSend.send[AM_TYPE_LEAF]((TOS_NODE_ID-1)/4, &mmm, 3+NREADINGS_4*2) == SUCCESS)
                   call Leds.led2Toggle();
	}


     }


  }


  event void Temperature.readDone(error_t result, uint16_t data) {
        
	previous_tmp=current_tmp;
        current_tmp=data;
          
  }


  event void Humidity.readDone(error_t result, uint16_t data) {
	previous_humi=current_humi;
        current_humi=data;
   
  }

  event void vLight.readDone(error_t result, uint8_t data) {
	previous_vLight=current_vLight;
        current_vLight=data;
          
  }


  event void iLight.readDone(error_t result, uint8_t data) {
	previous_voltage=current_voltage;
        current_voltage=data;
          
  }

}  
