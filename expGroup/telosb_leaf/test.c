#include "ZCompression_new.h"
#define PRINT_NUM(token) printf(#token " is %d \n",token)

void pt(uint8_t *res);


void pt(uint8_t *res){

	uint8_t bytes=*res;
	uint8_t i;
	int j;
	//printf("%d",num);
//PRINT_NUM(bytes);
	for(i=0;i<bytes;i++){
		
	    for(j=7;j>=0;j--){

		printf("%d",((res[i+1]&(1<<j))==0?0:1));
	    }
	    printf(" ");
	}
	printf("\n");


}

int main(){

	uint8_t *in1=malloc(32);
	uint8_t *in2=malloc(32);
	uint8_t *res=malloc(32);
        uint8_t *res1=malloc(32);
        uint8_t *res2=malloc(32);
	uint32_t n1;
	uint32_t n2;
	uint32_t max=2;
	uint32_t i=0;
	for(n1=1;n1<max;n1++){
	    for(n2=1;n2<max;n2++){
i++;
//PRINT_NUM(i);


		convertToArray(in1,n1);

		convertToArray(in2,n2);
printf("input ");
//PRINT_NUM(*in1);
		pt(in1);
//PRINT_NUM(*in2);

		pt(in2);
printf("output ");
//PRINT_NUM(n1);
//PRINT_NUM(n2);		


		ZCompress_2(in1,in2,res,0);
		ZDecode_2(res,res1,res2);
//PRINT_NUM(*res);
		pt(res);
//PRINT_NUM(*res1);
        	pt(res1);

//PRINT_NUM(*res2);
        	pt(res2);
		if(equal(res1,in1)!=1||equal(res2,in2)!=1)return 0;

	    }
	}

	*res=2;
	res[1]=134;
	res[2]=18;
	pt(res);
	ZDecode_2(res,res1,res2);


	pt(res2);
	pt(res1);
	ZDecode_2(res2,res1,res);
	pt(res);
	pt(res1);

}




