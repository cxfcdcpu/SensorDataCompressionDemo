/*
 * Copyright (c) 2006 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

import java.util.*;
import java.io.*;
/* Hold all data received from motes */
class Data {
    /* The mote data is stored in a flat array indexed by a mote's identifier.
       A null value indicates no mote with that identifier. */
    private Node[] nodes = new Node[256];
    private Oscilloscope parent;
    public long totalMax;
    private long totalReceive;

    Data(Oscilloscope parent) {
    this.parent = parent;
    totalMax=0;
    }

    void getMax(){
	totalMax=0;
	for(Node n:nodes){
	    if(n!=null)
		totalMax+=n.max;
	}
    }

    void writeToLog(){
	for(Node n:nodes){
	    if(n!=null)
	    {
                File file=new File("./Log/log_"+n.id+".txt");
		try{
		file.createNewFile();
		PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println(Constants.NREADINGS);
                int start[]=new int[Constants.NREADINGS];
                for(int i=0;i<n.max;i++){
		if(n.data[i]==-1)writer.println(""+start[i%Constants.NREADINGS]);
		else {
			writer.println(""+n.data[i]);
			start[i%Constants.NREADINGS]=n.data[i];
		}
		}
		writer.close();
		}catch(Exception except){
			System.out.println("can't find");		
		}

            }
	}


    }

    public double getRate(){

	return (double)totalReceive/totalMax;
       
    }

    public double getStream(){
        if(totalReceive==10)this.parent.firstTime=System.currentTimeMillis();
	long currentTime=System.currentTimeMillis();
        if(totalReceive>20)return 1000*(totalReceive-10)/(currentTime-this.parent.firstTime);
	else return 0;
       
    }

    /* Data received from mote nodeId containing NREADINGS samples from
       messageId * NREADINGS onwards. Tell parent if this is a new node. */
    void update(int nodeId, int messageId, int interval,int readings[]) {
    
    if (nodeId >= nodes.length) {
        int newLength = nodes.length * 2;
        if (nodeId >= newLength) {
            newLength = nodeId + 1;
        }

        Node newNodes[] = new Node[newLength];
        System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
        nodes = newNodes;
    }
    Node node = nodes[nodeId];
    if (node == null) {
        nodes[nodeId] = node = new Node(nodeId);
        parent.newNode(nodeId,interval);
    }
    node.update(messageId, readings);
    getMax();
    totalReceive++;
    totalReceive=Math.min(totalMax,totalReceive);
    }

    /* Return value of sample x for mote nodeId, or -1 for missing data */
    int getData(int nodeId, int x) {
    if (nodeId >= nodes.length || nodes[nodeId] == null)
        return -1;
    return nodes[nodeId].getData(x);
    }

    /* Return number of last known sample on mote nodeId. Returns 0 for
       unknown motes. */
    int maxX(int nodeId) {
    if (nodeId >= nodes.length || nodes[nodeId] == null)
        return 0;
    return nodes[nodeId].maxX();
    }

    /* Return number of largest known sample on all motes (0 if there are no
       motes) */
    int maxX() {
    int max = 0;

    for (int i = 0; i < nodes.length; i++) {
        if (nodes[i] != null) {
        int nmax = nodes[i].maxX();

        if (nmax > max)
            max = nmax;
        }
    }

    return max/Constants.NREADINGS;
    }
}
