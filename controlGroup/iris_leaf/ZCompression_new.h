#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include "Configuration.h"
#include "universal_functions_new.h"






/*define funtion ZCompress_2
 *import two byte array 'in1' and 'in2'
 *the length of input array is indicate by the first bytes
 *the output is another byte array. 
 *the length of output array is indicate by the first bytes
 *Function 00001
*/
void ZCompress_2(uint8_t* in1, uint8_t* in2,uint8_t* res, uint8_t mode);

void ZDecode_2(uint8_t* Z,uint8_t* in1, uint8_t* in2);

uint8_t countZeros(uint8_t* Z);

uint8_t oddEvenCheck(uint8_t* Z);

void compress_LEC(uint16_t* in, uint8_t* res);

void compress_TinyPack(uint16_t* in, uint8_t* res);

void decode_LEC(uint8_t* compressed, uint16_t* res, uint16_t dataNum);

void decode_TinyPack(uint8_t* compressed, uint16_t* res, uint16_t dataNum);


void compress_LEC(uint16_t* in, uint8_t* res){

    uint16_t num=*in;
    uint16_t i;
    uint16_t pointer=0;

    uint16_t value=0;
    uint16_t valLen=0;
    uint16_t prefix=0;
    uint16_t prefixLen=0;
    for( i=1;i<=num;i++){
	
	if(in[i]==1||in[i]==0){
		value=0;
		valLen=0;
		prefix=0;
		prefixLen=2;

	}
	else if(in[i]>=2&&in[i]<=3){
		value=in[i]-2;
		valLen=1;
		prefix=2;
		prefixLen=3;

	}
	else if(in[i]>=4&&in[i]<=7){
		value=in[i]-4;
		valLen=2;
		prefix=3;
		prefixLen=3;

	}
	else if(in[i]>=8&&in[i]<=15){
		value=in[i]-8;
		valLen=3;
		prefix=4;
		prefixLen=3;

	}
	else if(in[i]>=16&&in[i]<=31){
		value=in[i]-16;
		valLen=4;
		prefix=5;
		prefixLen=3;

	}
	else {
		valLen=findNumberOfBits(in[i])-1;
		value=in[i]-(1<<(valLen));
		prefixLen=valLen-2;
		prefix=(1<<prefixLen)-2;
	}
	setUIntBEElement(res,pointer,prefixLen,(uint32_t)prefix);
	if(valLen!=0)setUIntBEElement(res,pointer+prefixLen,valLen,(uint32_t)value);
	pointer+=prefixLen+valLen;

    }

	*res=getByteFromBits(pointer);

}

void compress_TinyPack(uint16_t* in, uint8_t* res){

    uint16_t num=*in;
    uint16_t i;
    uint16_t pointer=0;

    uint16_t value=0;
    uint16_t valLen=0;
    uint16_t prefix=0;
    uint16_t prefixLen=0;
    for( i=1;i<=num;i++){
	
	if(in[i]==1||in[i]==0){
		value=0;
		valLen=0;
		prefix=1;
		prefixLen=1;

	}
	else if(in[i]>=2&&in[i]<=3){
		value=in[i]-2;
		valLen=1;
		prefix=1;
		prefixLen=2;

	}
	else if(in[i]>=4&&in[i]<=7){
		value=in[i]-4;
		valLen=2;
		prefix=1;
		prefixLen=3;

	}
	else if(in[i]>=8&&in[i]<=15){
		value=in[i]-8;
		valLen=3;
		prefix=1;
		prefixLen=4;

	}
	else if(in[i]>=16&&in[i]<=31){
		value=in[i]-16;
		valLen=4;
		prefix=1;
		prefixLen=5;

	}
	else {
		valLen=findNumberOfBits(in[i])-1;
		value=in[i]-(1<<(valLen));
		prefixLen=valLen+1;
		prefix=1;
	}
	setUIntBEElement(res,pointer,prefixLen,(uint32_t)prefix);
	if(valLen!=0)setUIntBEElement(res,pointer+prefixLen,valLen,(uint32_t)value);
	pointer+=prefixLen+valLen;

    }

	*res=getByteFromBits(pointer);

}



void decode_LEC(uint8_t* compressed, uint16_t* res,uint16_t dataNum){
    uint16_t pointer=0;
    uint16_t i;
    uint32_t prePrefix;
    uint16_t prefix;
    uint16_t prefixLen;
    uint16_t value;
    uint16_t offset=0;
    uint16_t valueLen;

    *res=dataNum;
//no possible prefix==1;
    for(i=1;i<=dataNum;i++){
	prePrefix=getUIntBEElement(compressed,offset,2);
	if(prePrefix==0){
	    valueLen=0;
	    prefixLen=2;
	}
	else{
	    prefix=getUIntBEElement(compressed,offset,3);
	    if(prefix==7){
		pointer=0;
		while(getUIntBEElement(compressed,offset+3+pointer,1)==1){
			pointer++;
		}
		prefixLen=4+pointer;
		valueLen=prefixLen+2;
	    }
	    else if(prefix==6){
		valueLen=5;
		prefixLen=3;
	    }
	    else if(prefix==5){
		valueLen=4;
		prefixLen=3;

	    }
	    else if(prefix==4){
		valueLen=3;
		prefixLen=3;

	    }
	    else if(prefix==3){
		valueLen=2;
		prefixLen=3;

	    }
//prefix==2
	    else{
		valueLen=1;
		prefixLen=3;
	    }
	}
	
	    if(valueLen!=0)value=getUIntBEElement(compressed,offset+prefixLen,valueLen);
	    else value=0;
	    offset+=prefixLen+valueLen;
	    res[i]=value+(1<<valueLen);
    }

}


void decode_TinyPack(uint8_t* compressed, uint16_t* res,uint16_t dataNum){
    uint16_t pointer=0;
    uint16_t i;
    uint32_t prePrefix;
    uint16_t prefix;
    uint16_t prefixLen;
    uint16_t value;
    uint16_t offset=0;
    uint16_t valueLen;

    *res=dataNum;
//no possible prefix==1;
    for(i=1;i<=dataNum;i++){
	prePrefix=getUIntBEElement(compressed,offset,1);
	if(prePrefix==1){
	    valueLen=0;
	    prefixLen=1;
	}
	else{
	    
		pointer=0;
		while(getUIntBEElement(compressed,offset+1+pointer,1)==0){
			pointer++;
		}
		prefixLen=2+pointer;
		valueLen=prefixLen-1;
	    
	}
	    if(valueLen!=0)value=getUIntBEElement(compressed,offset+prefixLen,valueLen);
	    else value=0;
	    offset+=prefixLen+valueLen;
	    res[i]=value+(1<<valueLen);
    }

}

//1 is not skew, 0 is skew which is even with first 1 or odd without first 1.
uint8_t oddEvenCheck(uint8_t* Z){
    uint8_t start=0;
    uint8_t buf=128;
    uint8_t mask;
    uint8_t count=1;
    uint16_t i;
    for(i=0;i<8 * (*Z);i++){
	mask=buf>>(i%8);
	count=(count+1)%2;
        if(start==0&&((Z[1+i/8]&mask)!=0)){
	    start=1;
	    break;
	}
        
    }
    return count;



}

uint8_t countZeros(uint8_t* Z){
    uint8_t start=0;
    uint8_t buf=128;
    uint8_t mask;
    uint8_t count=0;
    uint16_t i;

    for (i=0;i<8 * (*Z);i++){
	mask=buf>>(i%8);
//PRINT_NUM(start);
        if(start==0&&((Z[1+i/8]&mask)!=0)){start=1;
	}
        else if(start==1&&((Z[1+i/8]&mask)==0)){
	    count++;

        }
	else if(start==1&&((Z[1+i/8]&mask)!=0)){break;
	}
    }
//PRINT_NUM(*Z);
    return count;

}

void ZDecode_2(uint8_t* Z,uint8_t* in1, uint8_t* in2){
    uint16_t len=findLength_byteArray(Z);
    uint8_t bt;
    uint8_t n;
    uint16_t tail;
    uint16_t zLen=findLength_byteArray(Z)-1;
    uint8_t *buf1;
    uint8_t *buf2;
    uint8_t *buf3;
    uint8_t countZ=countZeros(Z);
    uint16_t i;
    //small library
    if(*Z==1&&Z[1]==1){
	//in1=realloc(in1,2*sizeof *in1);
        *in1=1;
        in1[1]=1;
	//in2=realloc(in2,2*sizeof *in2);
        *in2=1;
        in2[1]=1;
    }
    else if(*Z==1&&Z[1]==3){
	//in1=realloc(in1,2*sizeof *in1);
        *in1=1;
        in1[1]=1;
	//in2=realloc(in2,2*sizeof *in2);
        *in2=1;
        in2[1]=2;
    }
    else if(*Z==1&&Z[1]==2){
	//in1=realloc(in1,2*sizeof *in1);
        *in1=1;
        in1[1]=2;
	//in2=realloc(in2,2*sizeof *in2);
        *in2=1;
        in2[1]=1;
    }

    else if(*Z==1&&Z[1]==7){
	//in1=realloc(in1,2*sizeof *in1);
        *in1=1;
        in1[1]=1;
	//in2=realloc(in2,2*sizeof *in2);
        *in2=1;
        in2[1]=3;
    }

    else if(*Z==1&&Z[1]==6){
	//in1=realloc(in1,2*sizeof *in1);
        *in1=1;
        in1[1]=3;
	//in2=realloc(in2,2*sizeof *in2);
        *in2=1;
        in2[1]=1;
    }
    else if(countZ==4){
	//in1=realloc(in1,2*sizeof *in1);
        *in1=1;
        in1[1]=1;
	bt=getByteFromBits(len-5);
	//in2=realloc(in2,(1+bt)*sizeof *in2);
        memset(in2,0,1+bt);
        *in2=bt;
        deletFirstZero(Z);
        memcpy(in2+1,Z+*Z-bt+1,bt);

    }
    else if(countZ==5){
	//in2=realloc(in2,2*sizeof *in1);
        *in2=1;
        in2[1]=1;
	bt=getByteFromBits(len-6);
	//in1=realloc(in1,(1+bt)*sizeof *in1);
        memset(in1,0,1+bt);
        *in1=bt;
        deletFirstZero(Z);
        memcpy(in1+1,Z+*Z-bt+1,bt);

    }
    //skewed
    else if(oddEvenCheck(Z)==0){
//PRINT_NUM(oddEvenCheck(Z));
	n=(zLen)/6;
        if(zLen%6==1)tail=2*n-1;
	else tail=2*n+1;
        buf1=malloc(32);
        buf2=malloc(32);
        divide1(Z,buf1,tail);
        divide2(Z,buf2,tail);
        buf3=malloc(32);
//pt(buf1);
	if(countZ%2==0){
            ZDecode_2(buf1,buf3,in2);
            innerCascade(buf3,buf2,in1,tail);

        }
        else{
	    ZDecode_2(buf1,in1,buf3);
	    innerCascade(buf3,buf2,in2,tail);
;
        }


        free(buf1);
	free(buf2);
	free(buf3);
    }
    //nomal
    else if(oddEvenCheck(Z)==1){
//PRINT_NUM(countZ);
	bt=getByteFromBits((zLen)/2);
	//in1=realloc(in1,(1+bt)*sizeof *in1);
        memset(in1,0,1+bt);
	*in1=bt;
	//in2=realloc(in2,(1+bt)*sizeof *in2);
        memset(in2,0,1+bt);
	*in2=bt;
	for(i=0;i<zLen;i++){
	    if(i%2==0){
		in2[bt-i/2/8]+=(getMask(Z[*Z-i/8],i%8)<<(i/2%8));

	    }
	    else{
		in1[bt-i/2/8]+=(getMask(Z[*Z-i/8],i%8)<<(i/2%8));
	    }

	}
	shrink(in1);
	shrink(in2);
    }
    else{
	memset(in1,0,2);
        memset(in2,0,2);
	*in1=1;
        *in2=1;
        in1[1]=1;
        in2[1]=1;

    }
    


}









//implement function 00001
void ZCompress_2(uint8_t* in1, uint8_t* in2,uint8_t* res, uint8_t mode){
    //find the length of in1 and in2;
    uint16_t len1=findLength_byteArray(in1);
    uint16_t len2=findLength_byteArray(in2);
	uint16_t size1=len1/8+(len1%8==0?0:1);
	uint16_t size2=len2/8+(len2%8==0?0:1);
    uint16_t start1=*in1-size1;
    uint16_t start2=*in2-size2;

    uint16_t lens=len1>len2?len2:len1;
    uint16_t lenl=len1>len2?len1:len2;
    uint16_t len=2*lenl+1;
    
    uint16_t lenB=len/8+(len%8==0?0:1);

    uint8_t n=0;
    uint16_t Lvl1=0;
    uint16_t Lvl2=0;
    uint16_t iterator=0;
	uint16_t size;
	uint8_t mask;

	uint8_t *buf1;
        uint8_t *buf2;
        uint8_t *buf3;
        uint8_t *buf4;
    //CHECK SMALL LIBRARY FIRST
	/*
		0 0 1
		0 1 11
		1 0 10
		0 -1 111
		-1 0 110
		0 2^5 10000+
		2^5 0 100000+


	*/
	//0 0

    if(len1==1&&len2==1&&mode==0){
	



                	memset(res,0,2);
			*res=1;
			*(res+1)=1;
		
	
    }
	//0 1
    else if(len1==1&&len2==2&&in2[1+start2]==2&&mode==0){



                	memset(res,0,2);
			*res=1;
			*(res+1)=3;

	

    }
	//1 0
    else if(len2==1&&len1==2&&in1[1+start1]==2&&mode==0){


                	memset(res,0,2);
			*res=1;
			*(res+1)=2;
		
	


    }
	//0 -1
    else if(len1==1&&len2==2&&in2[1+start2]==3&&mode==0){


                	memset(res,0,2);
			*res=1;
			*(res+1)=7;
		
	


    }
	//-1 0
    else if(len2==1&&len1==2&&in1[1+start1]==3&&mode==0){


                	memset(res,0,2);
			*res=1;
			*(res+1)=6;
		
	

    }

    else if(len1==1&&len2>6&&mode==0){


		size=1+(len2+5)/8+((len2+5)%8==0?0:1);

                	memset(res,0,size);
			res[0]=size-1;
			for(iterator=1;iterator<=size2;iterator++){
				res[size-size2+iterator-1]=in2[iterator+start2];
				
								
			}

			res[1]+=(len2+5)%8==0?(1<<7):(1<<((len2+5)%8-1));
		
	
    }
 
    else if(len2==1&&len1>6&&mode==0){
		
		size=1+(len1+6)/8+((len1+6)%8==0?0:1);

                	memset(res,0,size);
			res[0]=size-1;
			for(iterator=1;iterator<=size1;iterator++){
				res[size-size1+iterator-1]=in1[iterator+start1];
								
			}
			
			res[1]+=(len1+6)%8==0?(1<<7):(1<<((len1+6)%8-1));
		

    }
	//skewed
	///*
    else if(lens<lenl/2&&mode==0){

//PRINT_NUM(lens);
	n=lenl/4;
	if(lenl%4==0){
		Lvl1=2*n+1;
		Lvl2=2*n-1;

	}
	else if(lenl%4==2){;
		Lvl1=2*n+1;
		Lvl2=2*n+1;
	}
	else if(lenl%4==3){
		Lvl1=2*n+2;
		Lvl2=2*n+1;
	}
	else{
		Lvl1=2*n+1;
		Lvl2=2*n+1;

	}

	if(len1>len2){
		buf1=malloc(32);
		buf2=malloc(32);
		divide1(in1,buf1,Lvl2);
		divide2(in1,buf2,Lvl2);
		buf3=malloc((1+*in2)*sizeof *buf3);
		buf4=malloc(32);
                memcpy(buf3,in2,1+*in2);
		ZCompress_2(buf1,buf3,buf4,1);
	}
	else{
		buf1=malloc(32);
		buf2=malloc(32);
		divide1(in2,buf1,Lvl2);
		divide2(in2,buf2,Lvl2);
		buf3=malloc(32);
                memcpy(buf3,in1,1+*in1);
		buf4=malloc(32);
		ZCompress_2(buf3,buf1,buf4,1);

	}

	innerCascade(buf4,buf2,res,Lvl2);

//PRINT_NUM(*res);
	if(lenl%4==1){
//PRINT_NUM(lenl);
		addZeros(res,2);
        }
	free(buf1);
	free(buf2);
	free(buf3);
	free(buf4);

    }
//*/
	//normal
    else{
	

        		memset(res,0,lenB+1);
			*res=lenB;
			*(res+1)=1<<(len%8==0?7:len%8-1);

//PRINT_NUM(*(res+1));
//PRINT_NUM(len2);

			for(iterator=1;iterator<len;iterator++){
				mask=(lenl-(iterator-1)/2)%8==0?(1<<7):(1<<((lenl-(iterator-1)/2)%8-1));				

				if(iterator%2==1){


					
					if(iterator/2>=lenl-len1&&(mask&in1[size1-(lenl-iterator/2-1)/8])!=0){

						res[lenB-(len-iterator)/8-((len-iterator)%8==0?0:1)+1]+=(len-iterator)%8==0?(1<<7):(1<<((len-iterator)%8-1));


					}
					
					
				}
				else{
				

											
					if((iterator-1)/2>=lenl-len2&&(mask&in2[size2-(lenl-(iterator-1)/2-1)/8])!=0){



						res[lenB-(len-iterator)/8-((len-iterator)%8==0?0:1)+1]+=(len-iterator)%8==0?(1<<7):(1<<((len-iterator)%8-1));
					}
					
				
				}
			}
			
		



    }



}




