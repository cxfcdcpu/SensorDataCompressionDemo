import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

public class optimizedZCompression {

public static ArrayDeque<byte[]> optimizedCompress_round1(ArrayDeque<byte[]> inputDeltaQueue){
		
		ArrayDeque<byte[]> buf=new ArrayDeque<byte[]>();
		ArrayDeque<byte[]> buf0=new ArrayDeque<byte[]>(inputDeltaQueue);
		int count=0;
		int item=0;
		while(!buf0.isEmpty()){
			item++;
			byte[] current=buf0.poll();
			BigInteger bufInt=new BigInteger(current);
			
			if(bufInt.bitLength()!=1){
				if(count==1)buf.add(new BigInteger("7").toByteArray());
				else if(count>1){
					
					//System.out.println("Zerobits: "+zeroBits);
					buf.add(new BigInteger(""+Integer.parseInt(LECCode(count),2)).toByteArray());
					
				}
				
				buf.add(new BigInteger("3").shiftLeft(bufInt.bitLength()).add(bufInt).toByteArray());
				
				count=0;
			}else{
				
				count++;
			}
		}
		if(count==1)buf.add(new BigInteger("7").toByteArray());
		else if(count>1){
			
			//System.out.println("Zerobits: "+zeroBits);
			buf.add(new BigInteger(""+Integer.parseInt(LECCode(count),2)).toByteArray());
			
		}
		return buf;
	}



public static ArrayDeque<byte[]> optimizedCompress_round2(ArrayDeque<byte[]> buf,int part,int packetSize){
	int size=buf.size();
	ArrayDeque<byte[]> buf2=new ArrayDeque<byte[]>();
	ArrayDeque<byte[]> buf3=new ArrayDeque<byte[]>();
	//System.out.println("size:"+size);
	int partSize=packetSize*8/part;
	int maxBits=0;
	int currentBits=0;
	int currentItem=0;
	
	for(int i=0;i<size;i++){
		currentBits=getNumberOfBits(buf.peek());
		
		if((currentItem+1)*Math.max(currentBits, maxBits)>partSize-getLECBits(currentItem+1)-2){
			//System.out.println("currentBits:"+Math.max(currentBits, maxBits));
			//System.out.println("number:"+buf2.size());
			byte[] entry=NaiveCompress(buf2, maxBits);
			//System.out.println("entry: "+MainFrame.getByteArray(entry));
			buf3.add(entry);
			
			currentItem=1;
			maxBits=currentBits;
			buf2.clear();
			buf2.add(buf.poll());
		}else{
			buf2.add(buf.poll());
			maxBits=Math.max(maxBits, currentBits);
			currentItem++;
		}
	}
	if(!buf2.isEmpty()){
		buf3.add(NaiveCompress(buf2, maxBits));
	}
	return  buf3;
}



public static byte[] NaiveCompress(ArrayDeque<byte[]> buf, int max){
	
	int size=buf.size();
	
	int len=max*size/8+((max*size)%8==0?0:1);
	byte[] result=new byte[len+1];
	for(long i=0;i<max*size;i++){
		result[(int)((len*8-max*size+i)/8)+1]|=checkBits(buf,(int)(max-i/size-1))?(1<<((max*size-i-1)%8)):0;
	}
	String prefix=LECCode(buf.size());
	if(size==1)prefix="11";
	//System.out.println("size: "+size);
	//System.out.println("max: "+max);
	//System.out.println("result Length: "+result.length);
	//System.out.println("shift: "+max*size);
	
	//System.out.println("prefix:  "+prefix);
	//System.out.println("prefix shifted: "+MainFrame.getByteArray(new BigInteger(""+Integer.parseInt(prefix,2)).shiftLeft(max*size).toByteArray()));
	//System.out.println("datas: "+MainFrame.getByteArray(new BigInteger(result).toByteArray()));
	//System.out.println("final: "+MainFrame.getByteArray(new BigInteger(""+Integer.parseInt(prefix,2)).shiftLeft(max*size).add(new BigInteger(result)).toByteArray()));
	
	return new BigInteger(""+Integer.parseInt(prefix,2)).shiftLeft(max*size).add(new BigInteger(result)).toByteArray();
}

public static boolean checkBits(ArrayDeque<byte[]> buf, int bitCount){
	byte[] current=buf.poll();
	buf.add(current);
	//System.out.println("current:"+current.length);
	//System.out.println("bitCount:"+bitCount);
	if(current.length*8<bitCount+1)return false;
	else return (current[current.length-1-bitCount/8]&((byte)1<<(bitCount%8)))>0;
	
	
}

public static int getNumberOfBits(byte[] b){
	return new BigInteger(b).bitLength();
}

public static int getLECBits(int in){
	
	int b=0;
	while((in>>b)>0){
		b++;
	}
	if(b==1){
		return 2;
	}
	else if(b==2){
		return 4;
	}
	else if(b==3){
		return 5+1;
	}
	else if(b==4){
		return 6;
	}
	else if(b==5){
		return 7+1;
	}
	else return 2*b-4;
}


public static String LECCode(int in){
	
	String data=Integer.toBinaryString(in).substring(1);
	String lecPrefix="10";
	int b=0;
	while((in>>b)>0){
		b++;
	}
	if(in==2){
		lecPrefix+="00";
		return lecPrefix;
	}
	else if(in==3){
		lecPrefix+="010";
		return lecPrefix;
	}
	else if(b==3){
		lecPrefix+="011";
	}
	else if(b==4){
		lecPrefix+="100";
	}
	else if(b==5){
		lecPrefix+="101";
	}
	else{
		for(int i=0;i<b-4;i++){
			lecPrefix+="1";
		}
		lecPrefix+="0";
		
	}
	
	lecPrefix+=data;
	//int t=Integer.parseInt(lecPrefix, 2);	
	return lecPrefix;
}

public static ArrayDeque<byte[]> decode_1(byte[] input){
	
	int size=input.length*8;
	int index=size;
	if(input[0]==0){
		index=size-8;
	}
	else {
		int i=0;
		
		//System.out.println("input: "+MainFrame.getByteArray(input));
		while(((input[0]>>i)&0xFF)!=0){
			
			i++;
		}
		index=size-8+i;
	}
	
	//System.out.println("size"+size);
	//System.out.println("input: "+MainFrame.getByteArray(input));
	ArrayDeque<byte[]> result=new ArrayDeque<byte[]>();
	if((input[(size-index+1)/8]&(1<<((index-2)%8)))>0){
		input[(size-index)/8]=(byte) (input[(size-index)/8]&getProb((index-1)%8));
		input[(size-index+1)/8]=(byte) (input[(size-index+1)/8]&getProb((index-2)%8));
		result.add(input);
		return result;
	}
	else{
		index=index-2;
		int[] itemInfo=lecDecode(input,index);
		int itemNum=itemInfo[0];
		index=itemInfo[1];
		//System.out.println("index: "+index);
		//System.out.println("itemNum: "+itemNum);
		if(index<=2){
			for(int j=0;j<itemNum;j++){
				result.add(new byte[]{1});
			}
		}else{
			index--;
			int ss=index/itemNum;
			for(int j=0;j<itemNum;j++){
				
				
				byte[] entry=new byte[ss/8+1];
				int op=entry.length*8;
				for(int m=ss;m>0;m--){
					entry[(op-m)/8]|=(input[(size-(m*itemNum-j))/8]&(1<<((m*itemNum-j-1)%8)))==0?0:(1<<((m-1)%8));
				}
				result.add(new BigInteger(entry).toByteArray());
			}
		}
	}
	
	return result;
}




public static int[] lecDecode(byte[] input, int startIndex){
	int size=input.length*8;
	int dataBits=0;
	int result=1;
	if((input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))==0){
		startIndex--;
		if((input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))==0){
			return new int[]{2,startIndex};
		}
		if((input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))>0){
			startIndex--;
			if((input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))==0){
				return new int[]{3,startIndex};
			}
			else{
				dataBits=2;
			}
		}
		
	}else{
		startIndex--;
		if((input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))==0){
			startIndex--;
			if((input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))==0){
				dataBits=3;
			}
			else{
				dataBits=4;
			}
		}
		else{
			dataBits=4;
			while((input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))>0){
				startIndex--;
				dataBits++;
			}
		}
		
	}
	while(dataBits>0){
		dataBits--;
		startIndex--;
		result<<=1;
		result+=(input[(size-startIndex)/8]&(1<<((startIndex-1)%8)))==0?0:1;
	}
	
	
	return new int[]{result,startIndex};
}

public static byte getProb(int shift){
	if(shift>0) return (byte)(1<<(shift-1)|getProb(shift-1));
	else return 0;
}



}
