/*
 * Copyright (c) 2016-2020 Missouri University Of Science and Technology
 * Computer Science Department
 * 
 * All rights reserved.
 *
 * Author @Xiaofei Cao
*/


#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <limits.h>
#include "Z.h"
#include "universal_functions.h"

//Naive Z compression on two individual data item. The data item must be uint16_t.
void Zcompress_2(preprocessedPayload *payload_needToBe_process,uint16_t delta_data1,uint16_t delta_data2,uint8_t compressedTo_last_n_bit, uint8_t flag_setLength);
//Naive Z compression on multi-dimensional uint16_t data.
void Zcompress_multi_dimensional(preprocessedPayload *payload_needToBe_process,uint16_t *delta_data,uint8_t numberOf_data,uint8_t compressedTo_last_n_bit, uint8_t flag_setLength);
//Optimized Z compression (version 1) that compress two dimensional data that one's 
//length is two times of the others into one dimenional data.
void Zcompress_optimized(preprocessedPayload *payload_needToBe_process,uint16_t delta_data1,uint16_t delta_data2);
//Concatenate mulitiple Z compressed data into a single packet.
void cascade_data_toPayload(preprocessedPayload *payload_needToBe_process,uint16_t taget_data, uint8_t bits_needTo_cascade);
//decode naive Z compressed data into multidimensional data. Number of encoded data is specified. 
void decode_naive_ZCompression(preprocessedPayload *payload_needToBe_process,decodedData *multi_dimensional_data);
// Optimized Z compression (version 2) that compress two dimensional data at 
//any desired ration of the length of bits between the larger and smaller.
void Zcompression_optimized_setRatio(preprocessedPayload *payload_needToBe_process,float bit_ratio_of_larger_over_smaller,uint16_t delta_data1,uint16_t delta_data2);


void Zcompress_2(preprocessedPayload *payload_needToBe_process,uint16_t delta_data1,uint16_t delta_data2,uint8_t compressedTo_last_n_bit, uint8_t flag_setLength){

    uint8_t length_data1=find_length_of_data(delta_data1);
    uint8_t length_data2=find_length_of_data(delta_data2);
    uint8_t max_length;
    uint16_t checking_number;
    if(length_data1>=length_data2)max_length=length_data1;
    else max_length=length_data2;

    if(flag_setLength)max_length=flag_setLength;

    push_one(payload_needToBe_process);
    for(max_length;max_length>compressedTo_last_n_bit;max_length--){
        checking_number=1;
        checking_number<<=(max_length-1);
        if(delta_data1&checking_number)push_one(payload_needToBe_process);
        else push_zero(payload_needToBe_process);
        if(delta_data2&checking_number)push_one(payload_needToBe_process);
        else push_zero(payload_needToBe_process);
    }
}

void Zcompress_multi_dimensional(preprocessedPayload *payload_needToBe_process,uint16_t *delta_data,uint8_t numberOf_data,uint8_t compressedTo_last_n_bit, uint8_t flag_setLength){
    uint8_t max_length;
    uint8_t iterator;
    uint16_t checking_number;
    max_length=get_maximum_length_Of_Data_Array(delta_data,numberOf_data);
    
    if(flag_setLength)max_length=flag_setLength;
    push_one(payload_needToBe_process);
    for(max_length;max_length>compressedTo_last_n_bit;max_length--){
        checking_number=1;
        checking_number<<=(max_length-1);
        iterator=0;
        for(iterator=0;iterator<numberOf_data;iterator++){
            delta_data=delta_data+iterator;
            if(*delta_data&checking_number)push_one(payload_needToBe_process);
            else push_zero(payload_needToBe_process);
        }
        delta_data=delta_data-(numberOf_data-1);

    }

}

void Zcompress_optimized(preprocessedPayload *payload_needToBe_process,uint16_t delta_data1,uint16_t delta_data2){

    uint8_t length_data1=find_length_of_data(delta_data1);
    uint8_t length_data2=find_length_of_data(delta_data2);
    uint8_t max_length;
    uint8_t min_length;
    uint8_t labelOf_biggerOne;
        uint8_t n;
        uint8_t L_v1;
        uint8_t L_v2;
        uint16_t v1;
        uint16_t v2;
        uint16_t vs;
    if(length_data1>=length_data2){
        labelOf_biggerOne=0;
        max_length=length_data1;
        min_length=length_data2;
    }
    else{
        labelOf_biggerOne=1;
        max_length=length_data2;
        min_length=length_data1;
    }
    
    if(min_length>=max_length/2)
        Zcompress_2(payload_needToBe_process,delta_data1,delta_data2,0,0);
    else{

        if(max_length%4==0){
             n=max_length/4;
             L_v1=2*n+1;
             L_v2=2*n-1;
             if(labelOf_biggerOne){
                 v1=delta_data2>>L_v2;
                 v2=delta_data2;
                 vs=delta_data1;
                 Zcompress_2(payload_needToBe_process,vs,v1,0,0);
             }
             else {
                 v1=delta_data1>>L_v2;
                 v2=delta_data1;
                 vs=delta_data2;
                 Zcompress_2(payload_needToBe_process,v1,vs,0,0);
             }
             cascade_data_toPayload(payload_needToBe_process,v2, L_v2);

        }
        else if(max_length%4==2){
             n=max_length/4;
             L_v1=2*n+1;
             L_v2=2*n+1;
             if(labelOf_biggerOne){
                 v1=delta_data2>>L_v2;
                 v2=delta_data2;
                 vs=delta_data1;
                 Zcompress_2(payload_needToBe_process,vs,v1,0,0);
             }
             else {
                 v1=delta_data1>>L_v2;
                 v2=delta_data1;
                 vs=delta_data2;
                 Zcompress_2(payload_needToBe_process,v1,vs,0,0);
             }
             cascade_data_toPayload(payload_needToBe_process,v2, L_v2);

        }
        else if(max_length%4==3){
             n=max_length/4;
             L_v1=2*n+2;
             L_v2=2*n+1;
             if(labelOf_biggerOne){
                 v1=delta_data2>>L_v2;
                 v2=delta_data2;
                 vs=delta_data1;
                 Zcompress_2(payload_needToBe_process,vs,v1,0,0);
             }
             else {
                 v1=delta_data1>>L_v2;
                 v2=delta_data1;
                 vs=delta_data2;
                 Zcompress_2(payload_needToBe_process,v1,vs,0,0);
             }
             cascade_data_toPayload(payload_needToBe_process,v2, L_v2);

        }
        else{
             n=max_length/4;
             L_v1=2*n+1;
             L_v2=2*n+1;
             if(labelOf_biggerOne){
                 v1=delta_data2>>L_v2;
                 v2=delta_data2;
                 vs=delta_data1;
                 Zcompress_2(payload_needToBe_process,vs,v1,0,L_v1);
             }
             else {
                 v1=delta_data1>>L_v2;
                 v2=delta_data1;
                 vs=delta_data2;
                 Zcompress_2(payload_needToBe_process,v1,vs,0,L_v1);
             }
             cascade_data_toPayload(payload_needToBe_process,v2, L_v2);

        }

    }
}

void cascade_data_toPayload(preprocessedPayload *payload_needToBe_process,uint16_t taget_data, uint8_t bits_needTo_cascade){

    uint8_t i;
    for(i=bits_needTo_cascade;i>0;i--){
        uint16_t check_bit=1<<(i-1);
        if(taget_data&check_bit)push_one(payload_needToBe_process);
        else push_zero(payload_needToBe_process);
    }
}

void decode_naive_ZCompression(preprocessedPayload *payload_needToBe_process, decodedData *multi_dimensional_data){
    uint8_t numberOf_data=multi_dimensional_data->size;
    uint16_t total_bit_of_compressed_data;
    uint16_t itertor_bits_of_data;
    uint8_t itertor_nums_of_data;
    uint8_t last_bits_of_compressed_data;
    uint8_t maximum_bits_of_data;
    total_bit_of_compressed_data=get_bits_of_compressed_data(payload_needToBe_process)-1;

    maximum_bits_of_data=total_bit_of_compressed_data/numberOf_data;
    
    for(itertor_bits_of_data=0;itertor_bits_of_data<total_bit_of_compressed_data;itertor_bits_of_data++){
        last_bits_of_compressed_data=pop(payload_needToBe_process);
        if(last_bits_of_compressed_data==1)
            insert_one(&multi_dimensional_data->buf[numberOf_data-itertor_bits_of_data%numberOf_data-1]);
        else
            insert_zero(&multi_dimensional_data->buf[numberOf_data-itertor_bits_of_data%numberOf_data-1]);
    }
    for(itertor_nums_of_data=0;itertor_nums_of_data<numberOf_data;itertor_nums_of_data++){
        for(itertor_bits_of_data=0;itertor_bits_of_data<16-maximum_bits_of_data;itertor_bits_of_data++){
            insert_zero(&multi_dimensional_data->buf[itertor_nums_of_data]);
        }
    }
}



void Zcompression_optimized_setRatio(preprocessedPayload *payload_needToBe_process,float bit_ratio_of_larger_over_smaller,uint16_t delta_data1,uint16_t delta_data2){
    uint16_t integer_part_of_ratio;
    uint8_t decimal_part_of_ratio;
    


}

