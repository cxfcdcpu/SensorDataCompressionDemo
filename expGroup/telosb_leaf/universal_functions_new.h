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





uint8_t getByteFromBits(uint16_t bits);

uint16_t findLength_byteArray(uint8_t* in1);

uint8_t findLength_bitHelper(uint8_t byt);

void convertToArray(uint8_t* in,uint32_t input);

void divide1(uint8_t* in, uint8_t* res,uint16_t tail);

void divide2(uint8_t* in, uint8_t* res,uint16_t tail);

void innerCascade(uint8_t* in1, uint8_t* in2,uint8_t* res, uint16_t len2);

uint8_t getMask(uint8_t value, uint8_t bit);

uint8_t equal(uint8_t* in1, uint8_t* in2);

void addZeros(uint8_t* in,uint8_t numberOfZeros);

void deletFirstZero(uint8_t* in);

void shrink(uint8_t* in);

uint16_t findNumberOfBits(uint32_t value);



//the delta value is the difference of current_value and previous_value. If there is no change using '1' to represent.
uint16_t get_delta_value(uint16_t previous_value,uint16_t current_value);
//the current value is the previous_value adding the delta_value
uint16_t get_current_value(uint16_t previous_value,uint16_t delta_value);

void setUIntBEElement(uint8_t* compressedData, uint16_t offset, uint16_t length, uint32_t value);

uint32_t getUIntBEElement(uint8_t* compressedData, uint16_t offset, uint16_t length);



uint16_t findNumberOfBits(uint32_t value){
	uint16_t res=0;

	while((value>>res)>0){
		res++;
	
	}
	return res;

}

void setUIntBEElement(uint8_t* compressedData, uint16_t offset, uint16_t length, uint32_t value) {


    uint16_t byteOffset = offset >> 3;
    uint16_t bitOffset = offset & 7;
    uint16_t base_offset = 1;
    uint16_t mask;

    // all in one byte case
    if (length + bitOffset <= 8) {
       mask = ((1 << length) - 1) << (8 - bitOffset - length);

      compressedData[base_offset + byteOffset] = (uint8_t)( (compressedData[byteOffset+1] & ~mask) | (value << (8 - bitOffset - length)));
      return;
    }

    // set some high order bits
    if (bitOffset > 0) {
       mask = (1 << (8 - bitOffset)) - 1;

      length -= 8 - bitOffset;
      compressedData[base_offset + byteOffset] = (uint8_t) ((compressedData[byteOffset+1] & ~mask) |( value >> length));
      byteOffset++;
    }

    while (length >= 8) {
      length -= 8;
      compressedData[base_offset + (byteOffset++)] = (uint8_t) (value >> length);
    }

    // compressedData for last byte
    if (length > 0) {
      mask = (1 << (8 - length)) - 1;

      compressedData[base_offset + byteOffset] = (uint8_t) ((compressedData[byteOffset+1] & mask) | (value << (8 - length)));
    }
  }

uint32_t getUIntBEElement(uint8_t* compressedData, uint16_t offset, uint16_t length) {

    uint16_t byteOffset = offset >> 3;
    uint16_t bitOffset = offset & 7;
    uint32_t value = 0;

    // All in one byte case
    if (length + bitOffset <= 8)
      return (compressedData[byteOffset+1] >> (8 - bitOffset - length))
          & ((1 << length) - 1);

    // get some high order bits
    if (bitOffset > 0) {
      length -= 8 - bitOffset;
      value = (uint32_t) (compressedData[byteOffset+1] & ((1 << (8 - bitOffset)) - 1)) << length;
      byteOffset++;
    }

    while (length >= 8) {
      length -= 8;
      value |= (uint32_t) compressedData[1+byteOffset++] << length;
    }

    // compressedData from last byte
    if (length > 0)
      value |= compressedData[1+byteOffset] >> (8 - length);

    return value;
  }


void convertToArray(uint8_t* in,uint32_t input){

    in[0]=4;
    in[1]=(input>>24)&0x000000FF;
    in[2]=(input>>16)&0x000000FF;
    in[3]=(input>>8)&0x000000FF;
    in[4]=(input)&0x000000FF;
    shrink(in);
}



void shrink(uint8_t* in){
    uint16_t bits=findLength_byteArray(in);
    uint8_t bt=getByteFromBits(bits);
    uint8_t buf=*in;

     
    //in=realloc(in, (1+bt)*sizeof *in);

    memcpy(in+1,in+1+buf-bt,bt);
    *in=bt;


}

uint8_t equal(uint8_t* in1, uint8_t* in2){
    uint8_t i;
    shrink(in1);
    shrink(in2);

    if(*in1!=*in2)return 0;
    for(i=1;i<=*in1;i++){
	if(in1[i]!=in2[i])return 0;

    }
    return 1;
}


void addZeros(uint8_t* in,uint8_t numberOfZeros){
    uint16_t bits=findLength_byteArray(in);
    uint16_t residule=bits+numberOfZeros;
    uint8_t bt=getByteFromBits(residule);
    uint8_t Mask=1;
    uint8_t max=255;
    uint16_t i;
    uint8_t head;

    Mask=max^(Mask<<(bits%8==0?7:(bits%8-1)));
    head=in[1]&Mask;
    //in=realloc(in,(1+bt)*sizeof *in);
    for(i=bt;i>bt-*in;i--){
	in[i]=in[i-bt+*in];
    }
    in[1]=0;
    in[bt-*in+1]=head;

    in[1]+=(1<<(residule%8==0?7:(residule%8-1)));
    *in=bt;
}

void deletFirstZero(uint8_t* in){
    uint8_t Mask;
    uint16_t len=findLength_byteArray(in);
    Mask=1<<(len%8==0?7:(len%8-1));
    in[1]-=Mask;
}

uint8_t getMask(uint8_t value, uint8_t bit){
    if((value&(1<<bit))!=0)return 1;
    else return 0;
}

uint8_t getByteFromBits(uint16_t bits){

    return bits%8==0?bits/8:(bits/8+1);

}


void divide1(uint8_t* in, uint8_t* res,uint16_t tail){
    uint16_t bits=findLength_byteArray(in);
    uint16_t residule=bits-tail;
    //uint8_t mask;
    uint8_t bt=getByteFromBits(residule);

    uint16_t i;
    //res=realloc(res,(1+bt)*sizeof *res);
    memset(res,0,1+bt);
    *res=bt;
    for(i=0;i<residule;i++){
        
	res[bt-i/8]+=(getMask(in[*in-(tail+i)/8],(tail+i)%8)<<(i%8));

    }


}



void divide2(uint8_t* in, uint8_t* res,uint16_t tail){
    uint8_t bt=getByteFromBits(tail);

    uint16_t i;
    //res=realloc(res,(1+bt)*sizeof *res);
    memset(res,0,1+bt);
    *res=bt;
    for(i=0;i<bt-1;i++){
	res[bt-i]=in[*in-i];

    }
    for(i=0;i<tail%8;i++){
	res[1]+=(getMask(in[*in-bt+1],i%8)<<(i%8));

    }


}


void innerCascade(uint8_t* in1, uint8_t* in2, uint8_t* res, uint16_t len2){

    uint16_t bits=findLength_byteArray(in1);
    uint16_t totalBits=bits+len2;
    uint8_t bt=getByteFromBits(totalBits);

    uint16_t i;

//PRINT_NUM(totalBits);
    //res=realloc(res,(1+bt)*sizeof *res);
    memset(res,0,1+bt);
    *res=bt;
    for(i=0;i<getByteFromBits(len2);i++){
	res[bt-i]=in2[*in2-i];
    }
    for(i=0;i<bits;i++){
        
	res[bt-(len2+i)/8]+=(getMask(in1[*in1-(i)/8],i%8)<<((i+len2)%8));

    }

}

uint16_t findLength_byteArray(uint8_t* in1){
    uint8_t bt=*in1;
    uint8_t i=1;
    uint16_t min=0;
    while(*(in1+i)==0){
        i++;
    }
    min=8*(bt-i);
    return min+findLength_bitHelper(*(in1+i));

}


uint8_t findLength_bitHelper(uint8_t byt){ 
    uint8_t bit=0;
    while((byt>>bit)>0){
	bit++;

    }
    return bit;
}



uint16_t get_delta_value(uint16_t previous_value,uint16_t current_value){
    uint16_t delta;
    if(previous_value==current_value)delta=1;
    else if(previous_value<current_value){
        delta=current_value-previous_value;
        delta<<=1;
    }
    else{
        delta=previous_value-current_value;
        delta<<=1;
        delta+=1;
    }
    return delta;
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
