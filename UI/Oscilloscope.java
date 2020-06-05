
import net.tinyos.message.*;
import net.tinyos.util.*;
import java.io.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;



import javax.swing.JScrollBar;
import javax.swing.JInternalFrame;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/* The "Oscilloscope" demo app. Displays graphs showing data received from
   the Oscilloscope mote application, and allows the user to:
   - zoom in or out on the X axis
   - set the scale on the Y axis
   - change the sampling period
   - change the color of each mote's graph
   - clear all data

   This application is in three parts:
   - the Node and Data objects store data received from the motes and support
     simple queries
   - the Window and Graph and miscellaneous support objects implement the
     GUI and graph drawing
   - the Oscilloscope object talks to the motes and coordinates the other
     objects

   Synchronization is handled through the Oscilloscope object. Any operation
   that reads or writes the mote data must be synchronized on Oscilloscope.
   Note that the messageReceived method below is synchronized, so no further
   synchronization is needed when updating state based on received messages.
*/
public class Oscilloscope implements MessageListener
{
    MoteIF mote;
    Data data;
    Window window;
    /* The current sampling period. If we receive a message from a mote
       with a newer version, we update our interval. If we receive a message
       with an older version, we broadcast a message with the current interval
       and version. If the user changes the interval, we increment the
       version and broadcast the new interval and version. */
    int interval = Constants.DEFAULT_INTERVAL;
    int version = 100;
    int mode=-1;
    short mmm;
    public long firstTime=0;

    /* Main entry point */
    void run() {
    data = new Data(this);
    
    window = new Window(this);
    window.setup();
       mote = new MoteIF(PrintStreamMessenger.err);
    }

    synchronized boolean setMode(int modes){

      if(modes==1){
        mode=1;
	mmm=127;
        return true;
      }else if(modes==0){
        mode=0;
	mmm=33;
        return true;
      }else if(modes==2){
        mode=2;
        return true;
      }else if(modes==3){
	mode=3;	
	return true;
      }
       else return false;

    }

    synchronized void setInterval(int intervals){

      this.interval=intervals;

    }



    synchronized boolean RegisterMote(){

      if(mode==1){
    	mote.registerListener(new CompressedMsg(), this);
	return true;
      }else if(mode==0){
        
	mote.registerListener(new OscilloscopeMsg(), this);
        return true;
      }else{
        window.error("Please set the mode first ('0' for uncompressed, '1' for compressed)");
	return false;
      }
    }



    /* The data object has informed us that nodeId is a previously unknown
       mote. Update the GUI. */
    void newNode(int nodeId,int period) {
    window.newNode(nodeId,period);
    }

    public synchronized void messageReceived(int dest_addr, 
            Message msg) {
    if(mode==1){
    if (msg instanceof CompressedMsg) {
        CompressedMsg cmsg = (CompressedMsg)msg;


        for(int i=0;i<Constants.ITEM;i++){
          int[] tmp=new int[Constants.NREADINGS];
	  for(int j=0;j<Constants.NREADINGS;j++){
          	tmp[0]=cmsg.get_data()[(Constants.NREADINGS+2)*i+2+j];
	  }    
          data.update(cmsg.get_data()[8*i+0], cmsg.get_data()[8*i+1],100, tmp);  
        
        }
        window.newData();
        window.updateRate(data.getRate());
        window.updateStream(data.getStream());
      }
     }
     else if(mode==0){
    if (msg instanceof OscilloscopeMsg) {
        OscilloscopeMsg omsg = (OscilloscopeMsg)msg;
        

        data.update(omsg.get_id(), omsg.get_count(),omsg.get_interval(), omsg.get_readings());

        /* Inform the GUI that new data showed up */
        window.newData();
        window.updateRate(data.getRate());
        window.updateStream(data.getStream());
    }
     }

    }



    /* The user wants to set the interval to newPeriod. Refuse bogus values
       and return false, or accept the change, broadcast it, and return
       true */

/*
    synchronized boolean setInterval(int newPeriod) {
    if (newPeriod < 1 || newPeriod > 65535) {
        return false;
    }
    interval = newPeriod;
    version++;
    sendInterval();
    return true;
    }
*/
    /* Broadcast a version+interval message. */

/*
    void sendInterval() {
    OscilloscopeMsg omsg = new OscilloscopeMsg();

    omsg.set_version(version);
    omsg.set_interval(interval);
    try {
        mote.send(MoteIF.TOS_BCAST_ADDR, omsg);
    }
    catch (IOException e) {
        window.error("Cannot send message to mote");
    }
    }

*/

    void Control(int start){
    baseControlMsg bcmsg=new baseControlMsg();

    bcmsg.set_version(version);
    bcmsg.set_settingOrControl((short)(1&0xff));
    bcmsg.set_start((short)(start&0xff));
    bcmsg.set_interval(100);
    bcmsg.set_mode(this.mmm);
    mote.registerListener(new baseControlMsg(), this);
    try {
        mote.send(MoteIF.TOS_BCAST_ADDR, bcmsg);
 	version++;
    }
    catch (IOException e) {
        window.error("Cannot send message to mote");
    }
 

    }


    void SetMote(){
    baseControlMsg bcmsg=new baseControlMsg();

    bcmsg.set_version(version);
    bcmsg.set_settingOrControl((short)(0&0xff));
    bcmsg.set_start((short)(1&0xff));
    bcmsg.set_interval(this.interval);
    bcmsg.set_mode(this.mmm);
    mote.registerListener(new baseControlMsg(), this);
    try {
        mote.send(MoteIF.TOS_BCAST_ADDR, bcmsg);
 	version++;
    }
    catch (IOException e) {
        window.error("Cannot send message to mote");
    }
 

    }


    void superReset(){
	superResetMsg srmsg=new superResetMsg();
	
	srmsg.set_key1((short)233);
	srmsg.set_key2((short)55);
	srmsg.set_key3((short)156);
	srmsg.set_key4((short)3);
        mote.registerListener(new superResetMsg(), this);
	version=100;
        try {
            mote.send(MoteIF.TOS_BCAST_ADDR, srmsg);
        }
        catch (IOException e) {
            window.error("Cannot send message to mote");
        }
    }


    /* User wants to clear all data. */
    void clear() {
    data = new Data(this);
    }

    public static void main(String[] args) {
    Oscilloscope me = new Oscilloscope();
    me.run();
    }
}
