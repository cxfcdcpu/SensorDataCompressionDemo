#ifndef CONFIGURATION_H
#define CONFIGURATION_H

enum {
  /* Number of readings per message. If you increase this, you may have to
     increase the message_t size. This is used for not compressed data*/
  NREADINGS_3 = 3,
  NREADINGS_2= 2,
  NREADINGS_4 = 4,
  NREADING=4,
  NREADINGS_6 = 6,
  
  AM_GROUP=56,
  AM_TYPE_LEAF=21,
//v3
  AM_TYPE_INTER=43,
  AM_TYPE_BASE=123,
AM_TYPE_INTER_RAW=66,

  BufSize=80,
  BufItemSize=42,
  controlPacketSize=16,
  PacketSize=114,
  cascadeSize=110,
  /* Default sampling period. */
  DEFAULT_INTERVAL = 256,
  /*AM_type of the sensors*/
  AM_OSCILLOSCOPE = 0x93
};


  enum {
    UART_QUEUE_LEN = 1,
    RADIO_QUEUE_LEN = 10,
  };

/*Reset Password*/
enum {
  S1_Reset = 233,
  S2_Reset=55,
  S3_Reset =156,
  S4_Reset=3
};

/*setting Password*/
enum {
  S1_Setting = 1,
  S2_Setting=25,
  S3_Setting =36,
  S4_Setting=222
};

/*setting Password*/
enum {
  START_TIMER=0X77,
  STOP_TIMER=0X33,
  RESET_CODE=0x57,
  lastByte=99,
  testingGroup=20,
  controlGroup=40,
  Z_Com=56,
  LEC_Com=89,
  TinyPack_Com=34,
  FELACS_Com=10,
  No_Com=18,
  Only_Cas=44,
//v3
  ini_head=10,
  hyb_interval=50

};


#endif
