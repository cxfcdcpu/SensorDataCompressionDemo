/*
 * Copyright (c) 2016-2020 Missouri University Of Science and Technology
 * Computer Science Department
 * 
 * All rights reserved.
 *
 * Author @Xiaofei Cao
*/


#ifndef UNIVERSAL_FUNCTIONS_H
#define UNIVERSAL_FUNCTIONS_H
//find the number of bits of an uint16_t data. Not counting the zero in the head
uint8_t find_length_of_data(uint16_t data);
//print uint16_t data in binary format, ending with \n.
//void print_rawData_inBinary(uint16_t data);
//print uint8_t data in binary format, ending with \n.
//void print_byteData_inBinary(uint8_t data);
uint8_t get_maximum_length_Of_Data_Array(uint16_t *delta_data,uint8_t numberOf_data);
//shift all bits of uint16_t data one bits right and add '1' at the head of the data.
void insert_one(uint16_t *data);
//shift all bits of uint16_t data one bits right.
void insert_zero(uint16_t *data);
//the delta value is the difference of current_value and previous_value. If there is no change using '1' to represent.
uint16_t get_delta_value(uint16_t previous_value,uint16_t current_value);
//the current value is the previous_value adding the delta_value
uint16_t get_current_value(uint16_t previous_value,uint16_t delta_value);

void add_one(uint16_t *data);

void add_zero(uint16_t *data);

uint8_t find_length_of_data(uint16_t data){
    uint8_t length_of_data=0;
    while(data>0){
        data=data>>1;
        length_of_data++;
    }
    return length_of_data;
}
/*
void print_rawData_inBinary(uint16_t data){
    uint16_t most_significant_bit_value;

    for(most_significant_bit_value=1<<15;most_significant_bit_value>0;most_significant_bit_value>>=1){
        if(data&most_significant_bit_value)
            printf("%c", '1');
        else
            printf("%c", '0');
    }
    printf("\n");

}

void print_byteData_inBinary(uint8_t data){
    uint8_t most_significant_bit_value;

    for(most_significant_bit_value=128;most_significant_bit_value>0;most_significant_bit_value>>=1){
        if(data&most_significant_bit_value)
            printf("%c", '1');
        else
            printf("%c", '0');
    }
}
*/

uint8_t get_maximum_length_Of_Data_Array(uint16_t *delta_data,uint8_t numberOf_data){

    uint8_t iterator=0;
    uint16_t delta_value;
    uint16_t maximum_delta_value=0;
    for(iterator=0;iterator<numberOf_data;iterator++){
        delta_data=delta_data+iterator;
        delta_value=*delta_data;
        if(delta_value>maximum_delta_value)maximum_delta_value=delta_value;
    }
    return find_length_of_data(maximum_delta_value);

}


void insert_one(uint16_t *data){
    *data>>=1;
    *data+=1<<15;
}

void insert_zero(uint16_t *data){
    *data>>=1;   
}

void add_one(uint16_t *data){
    *data<<=1;
    *data+=1;
}

void add_zero(uint16_t *data){
    *data<<=1;
} 

uint16_t get_delta_value(uint16_t previous_value,uint16_t current_value){
    uint16_t delta;
    if(previous_value==current_value)delta=1;
    else if(previous_value<current_value){
        delta=current_value-previous_value;
        delta=delta<<1;
    }
    else{
        delta=previous_value-current_value;
        delta=delta<<1;
        delta+=1;
    }
    return delta==0?1:delta;
}

uint16_t get_current_value(uint16_t previous_value,uint16_t delta_value){

    uint16_t current_value;
    if(delta_value==1)current_value=previous_value;
    else if(delta_value&1){
        delta_value>>=1;
        current_value=previous_value-delta_value;
    }
    else{
        delta_value>>=1;
        current_value=previous_value+delta_value;
    }
    return current_value;
}

#endif
