/*
 * Copyright (c) 2006 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

// @author David Gay

#ifndef OSCILLOSCOPE_H
#define OSCILLOSCOPE_H

enum {
  /* Number of readings per message. If you increase this, you may have to
     increase the message_t size. */
  NREADINGS = 6,
  PACKET_SIZE=56,
  ITEM=7,
  /* Default sampling period. */
  DEFAULT_INTERVAL = 256,
  CHILDREN_INTERVAL=256,
    RADIO_QUEUE_LEN = 50,
  AM_OSCILLOSCOPE = 0x93
};


enum {
  S1 = 233,
  S2=55,
  S3 =156,
  S4=3
};


typedef nx_struct baseControl{
  nx_uint16_t version;
  nx_uint8_t settingOrControl;
  nx_uint16_t interval;
  nx_uint8_t mode;
  nx_uint8_t start;
}baseControl_t;

typedef nx_struct superReset{

  nx_uint8_t key1;
  nx_uint8_t key2;
  nx_uint8_t key3;
  nx_uint8_t key4;

}superReset_t;


typedef nx_struct oscilloscope {
  nx_uint16_t version; /* Version of the interval. */
  nx_uint16_t interval; /* Samping period. */
  nx_uint16_t id; /* Mote id of sending mote. */
  nx_uint16_t count; /* The readings are samples count * NREADINGS onwards */
  nx_uint16_t readings[NREADINGS];
} oscilloscope_t;

typedef nx_struct compressed {
  nx_uint16_t interval; /* Samping period. */ 
  nx_uint16_t data[PACKET_SIZE];
} compressed_t;



#endif
