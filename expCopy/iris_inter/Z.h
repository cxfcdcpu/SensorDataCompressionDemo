/*
 * Copyright (c) 2016-2020 Missouri University Of Science and Technology
 * Computer Science Department
 * 
 * All rights reserved.
 *
 * Author @Xiaofei Cao
*/

#ifndef Z_H
#define Z_H

#include "universal_functions.h"

enum{
    prefered_payload_size=10,
    default_decoded_data_size=10

};

//the size refer to the number of bytes the payload contains,if size=0, there is no data in the payload, the maximum size is prefered_payload_size
//the starting_position indicate the where the first bit of 1 start. for example if starting_position is 0, the buf[0] will contain the first bit of the payload.
typedef struct preprocessed_payload{
    uint8_t buf[prefered_payload_size];
    uint8_t starting_position;
    uint8_t size;
    uint16_t number_of_bits;
}preprocessedPayload;


typedef struct decoded_data{
    uint16_t buf[default_decoded_data_size];
    uint8_t size;

}decodedData;

//decoded data data structure need to be reset. Setting all members of decoded_data into zero.
void reset_decoded_data(decodedData *decodedData_needToBe_reset);
//Setting all members of preprocessed payload data into zero.
void reset_preprocessed_payload(preprocessedPayload *payload_needToBe_reset);
//print the preprocessed payload data in binary format. Ending with \n.
//void print_compressedData_inBinary(preprocessedPayload payload_needToBe_process);
//push '1' into right hand side of preprocessedPayload
void push_one(preprocessedPayload *payload_needToBe_process);
//push '0' into right hand side of preprocessedPayload
void push_zero(preprocessedPayload *payload_needToBe_process);
//pop one bit from right hand side of preprocessedPayload and return it.
uint8_t pop(preprocessedPayload *payload_needToBe_process);
//poll one bit from left hand side of preprocessedPayload and return it.
uint8_t poll(preprocessedPayload *payload_needToBe_process);
//find the number of bits of the compressed data rather than the number of bytes.
uint16_t get_bits_of_compressed_data(preprocessedPayload *payload_needToBe_process);

void reset_preprocessed_payload(preprocessedPayload *payload_needToBe_reset){
    uint8_t i=0;
    
    for(i=0;i<prefered_payload_size;i++){
       payload_needToBe_reset->buf[i]=0;
    }
    payload_needToBe_reset->size=0;
    payload_needToBe_reset->starting_position=prefered_payload_size;
    payload_needToBe_reset->number_of_bits=0;
}

void reset_decoded_data(decodedData *decodedData_needToBe_reset){
    uint16_t i=0;
    
    for(i=0;i<default_decoded_data_size;i++){
       decodedData_needToBe_reset->buf[i]=0;
    }
    decodedData_needToBe_reset->size=0;

}
/*
void print_compressedData_inBinary(preprocessedPayload payload_needToBe_process){
    uint8_t *byteData;
    uint8_t numberOf_byteData;
    byteData=&payload_needToBe_process.buf[0]+payload_needToBe_process.starting_position;

    for(numberOf_byteData=0;numberOf_byteData<payload_needToBe_process.size;numberOf_byteData++){

        print_byteData_inBinary(*byteData);
        byteData++;
    }
    printf("\n");

}

void print_decodedData_inBinary(decodedData* decoded_data){
    uint8_t iterator;
    for(iterator=0;iterator<decoded_data->size;iterator++){
        print_rawData_inBinary(decoded_data->buf[iterator]);
    }
}
*/
uint8_t pop(preprocessedPayload *payload_needToBe_process){
    uint8_t poped_bit=0;
    uint8_t numOf_element;
    if(payload_needToBe_process->size){
        payload_needToBe_process->number_of_bits--;

        for(numOf_element=0;numOf_element<payload_needToBe_process->size;numOf_element++){
            uint8_t poped_bit_previous=poped_bit;
            poped_bit=payload_needToBe_process->buf[payload_needToBe_process->starting_position+numOf_element]&1;
            if(poped_bit_previous){
                payload_needToBe_process->buf[payload_needToBe_process->starting_position+numOf_element]>>=1;
                payload_needToBe_process->buf[payload_needToBe_process->starting_position+numOf_element]+=128;
            }
            else
                payload_needToBe_process->buf[payload_needToBe_process->starting_position+numOf_element]>>=1;
        }
        if(payload_needToBe_process->buf[payload_needToBe_process->starting_position]==0){
            payload_needToBe_process->size--;
            payload_needToBe_process->starting_position++;
        }
    }
    return poped_bit;

}

void push_one(preprocessedPayload *payload_needToBe_process){
    uint8_t pushed_bit=1;
    uint8_t numOf_element;
    payload_needToBe_process->number_of_bits++;
    for(numOf_element=0;numOf_element<payload_needToBe_process->size;numOf_element++){
        uint8_t pushed_bit_previous=pushed_bit;
        if(payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]&128)
            pushed_bit=1;
        else
            pushed_bit=0;
        if(pushed_bit_previous){
            payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]<<=1;
            payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]+=1;
        }
        else
            payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]<<=1;            

    }
    if(payload_needToBe_process->starting_position&&pushed_bit){
        payload_needToBe_process->size++;
        payload_needToBe_process->starting_position--;
        payload_needToBe_process->buf[payload_needToBe_process->starting_position]=1;
    }
}

void push_zero(preprocessedPayload *payload_needToBe_process){
    uint8_t pushed_bit=0;
    uint8_t numOf_element;
    payload_needToBe_process->number_of_bits++;
    for(numOf_element=0;numOf_element<payload_needToBe_process->size;numOf_element++){
        uint8_t pushed_bit_previous=pushed_bit;
        if(payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]&128)
            pushed_bit=1;
        else
            pushed_bit=0;
        if(pushed_bit_previous){
            payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]<<=1;
            payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]+=1;
        }
        else{
            payload_needToBe_process->buf[prefered_payload_size-numOf_element-1]<<=1;
        }

    }
    if(payload_needToBe_process->starting_position&&pushed_bit){
        payload_needToBe_process->size++;
        payload_needToBe_process->starting_position--;
        payload_needToBe_process->buf[payload_needToBe_process->starting_position]=1;
    }
}

uint16_t get_bits_of_compressed_data(preprocessedPayload *payload_needToBe_process){
    uint16_t bits_of_first_byte=8;
    uint8_t probe_of_bits=1<<(bits_of_first_byte-1);
    while((probe_of_bits&payload_needToBe_process->buf[payload_needToBe_process->starting_position])==0&&bits_of_first_byte>0){
        bits_of_first_byte--;
        probe_of_bits>>=1;
    }
    return bits_of_first_byte+8*(payload_needToBe_process->size-1);
}
#endif
