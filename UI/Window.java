/*
 * Copyright (c) 2006 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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


/* The main GUI object. Build the GUI and coordinate all user activities */
class Window {
    Oscilloscope parent;
    Graph graph;

    Font smallFont = new Font("Dialog", Font.PLAIN, 8);
    Font boldFont = new Font("Dialog", Font.BOLD, 12);
    Font normalFont = new Font("Dialog", Font.PLAIN, 12);
    MoteTableModel moteListModel; // GUI view of mote list
    JLabel xLabel; // Label displaying X axis range
    JTextField sampleText,modeText, yText,sr,stream; // inputs for sample period and Y axis range
    JTextField intervalText;
    JFrame frame;
    JButton btnStart;


    JFileChooser fc;
    ButtonGroup group = new ButtonGroup();
    File inputFile;
    int inputDiameter=2;
    public static int part=6;
    public static int packetSize=114;
    int inputMode=0;
    int run=0;
    int round=0;
    int startOrPause=0;

    Reader inReader;
    File externalFile;


    Deque<String> inputQueue=new ArrayDeque<>();
    Deque<byte[]> inputDeltaQueue=new ArrayDeque<byte[]>();
    Deque<byte[]> outputDeltaQueue=new ArrayDeque<byte[]>();
    Deque<String> outputQueue=new ArrayDeque<>();

	ArrayDeque<byte[]> round1Result=new ArrayDeque<byte[]>();
	ArrayDeque<byte[]> round2Result=new ArrayDeque<byte[]>();
	ArrayDeque<byte[]> round3Result=new ArrayDeque<byte[]>();
	ArrayDeque<byte[]> decode3Result=new ArrayDeque<byte[]>();
	ArrayDeque<byte[]> decode2Result=new ArrayDeque<byte[]>();
	ArrayDeque<byte[]> decode1Result=new ArrayDeque<byte[]>();


    Window(Oscilloscope parent) {
	this.parent = parent;
    }

    /* A model for the mote table, and general utility operations on the mote
       list */
    class MoteTableModel extends AbstractTableModel {
	private ArrayList<Integer> motes = new ArrayList<Integer>();
        private ArrayList<Integer> intervals=new ArrayList<Integer>();
	private ArrayList<Color> colors = new ArrayList<Color>();

	/* Initial mote colors cycle through this list. Add more colors if
	   you want. */
	private Color[] cycle = {
	    Color.WHITE, Color.RED,  Color.GREEN, Color.MAGENTA,Color.PINK,Color.LIGHT_GRAY,
	    Color.YELLOW, Color.GRAY,Color.BLUE,Color.ORANGE,Color.CYAN
	};
	int cycleIndex;
	
	/* TableModel methods for achieving our table appearance */
	public String getColumnName(int col) {
	    if (col == 0) {
		return "Mote";
	    }
	    else if(col==1){
  		return "Interval";
	    } 
	    else {
		return "Color";
	    }
	}

	public int getColumnCount() { return 3; }

	public synchronized int getRowCount() { return motes.size(); }
	
	public synchronized Object getValueAt(int row, int col) {
	    if (col == 0) {
		return motes.get(row);
	    } 
	    else if(col==1){
  		return intervals.get(row);
	    } 
	    else {
		return colors.get(row);
	    }
	}

        public Class getColumnClass(int col) {
            return getValueAt(0, col).getClass();
        }

	public boolean isCellEditable(int row, int col) {
	    return col == 2;
	}

	public synchronized void setValueAt(Object value, int row, int col) {
	    colors.set(row, (Color)value);
            fireTableCellUpdated(row, col);
	    graph.repaint();
        }

	/* Return mote id of i'th mote */
	int get(int i) { return (motes.get(i)).intValue(); }
	



	/* Return color of i'th mote */
	Color getColor(int i)  { return colors.get(i); }
	
	/* Return number of motes */
	int size() { return motes.size(); }
	
	/* Add a new mote */
	synchronized void newNode(int nodeId,int interval) {
	    /* Shock, horror. No binary search. */
	    int i, len = motes.size();
	    
	    for (i = 0; ; i++) {
		if (i == len || nodeId < get(i)) {
		    motes.add(i, new Integer(nodeId));
		    intervals.add(i,new Integer(interval));
		    // Cycle through a set of initial colors
		    colors.add(i, cycle[cycleIndex++ % cycle.length]);
		    break;
		}
	    }
	    fireTableRowsInserted(i, i);
	}
	
	/* Remove all motes */
	void clear() {
	    motes = new ArrayList<Integer>();
            intervals=new ArrayList<Integer>();
	    colors = new ArrayList<Color>();
	    fireTableDataChanged();
	}
    } /* End of MoteTableModel */



    /* A simple full-color cell */
    static class MoteColor extends JLabel implements TableCellRenderer {
	public MoteColor() { setOpaque(true); }
	public Component getTableCellRendererComponent
	    (JTable table, Object color,
	     boolean isSelected, boolean hasFocus, 
	     int row, int column) {
	    setBackground((Color)color);
	    return this;
	}
    }

    /* Convenience methods for making buttons, labels and textfields.
       Simplifies code and ensures a consistent style. */

    JButton makeButton(String label, ActionListener action) {
	JButton button = new JButton();
        button.setText(label);
        button.setFont(boldFont);
	button.addActionListener(action);
	return button;
    }

    JLabel makeLabel(String txt, int alignment) {
	JLabel label = new JLabel(txt, alignment);
	label.setFont(boldFont);
	return label;
    }
    
    JLabel makeSmallLabel(String txt, int alignment) {
	JLabel label = new JLabel(txt, alignment);
	label.setFont(smallFont);
	return label;
    }
    
    JTextField makeTextField(int columns, ActionListener action) {
	JTextField tf = new JTextField(columns);
	tf.setFont(normalFont);
	tf.setMaximumSize(tf.getPreferredSize());
	tf.addActionListener(action);
	return tf;
    }

    /* Build the GUI */
    void setup() {
	JPanel main = new JPanel(new BorderLayout());

	main.setMinimumSize(new Dimension(500, 250));
	main.setPreferredSize(new Dimension(1100, 700));
	
	// Four panels: mote list, graph, controls, menu
	moteListModel = new  MoteTableModel();
	JTable moteList = new JTable(moteListModel);
	moteList.setDefaultRenderer(Color.class, new MoteColor());
	moteList.setDefaultEditor(Color.class, 
				  new ColorCellEditor("Pick Mote Color"));
	moteList.setPreferredScrollableViewportSize(new Dimension(150, 650));
	JScrollPane motePanel = new JScrollPane();
	motePanel.getViewport().add(moteList, null);
	main.add(motePanel, BorderLayout.WEST);
	
	graph = new Graph(this);
	main.add(graph, BorderLayout.CENTER);
	
//**********************************************************************************//



	// Sample period.
	JLabel modeLabel = makeLabel("Choose Mode:", JLabel.RIGHT);
	modeText = makeTextField(3, new ActionListener() {
		public void actionPerformed(ActionEvent e) { setMode(); 
		btnStart.setEnabled(true);
		}
	    } );

        //success rate;
        JLabel successLabel=	makeLabel("Receive rate:", JLabel.RIGHT);
        sr = new JTextField(6);
	sr.setFont(normalFont);
	sr.setMaximumSize(sr.getPreferredSize());

        //success rate;
        JLabel streamLabel=	makeLabel("stream rate:", JLabel.RIGHT);
        stream = new JTextField(6);
	stream.setFont(normalFont);
	stream.setMaximumSize(sr.getPreferredSize());


	JButton logButton = makeButton("Record", new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			parent.data.writeToLog();

		 }
	    } );


	Box menu = new Box(BoxLayout.X_AXIS);
	menu.add(modeLabel);
        menu.add(modeText);
	menu.add(successLabel);
	menu.add(sr);
	menu.add(streamLabel);
	menu.add(stream);
	menu.add(logButton);



	main.add(menu, BorderLayout.NORTH);

//********************************************************************************************//

	// Controls. Organised using box layouts.
	

        

	// Sample period.
/*
	JLabel sampleLabel = makeLabel("Sample period (ms):", JLabel.RIGHT);
	sampleText = makeTextField(6, new ActionListener() {
		public void actionPerformed(ActionEvent e) { setSamplePeriod(); }
	    } );
	updateSamplePeriod();
*/
	// Clear data.
	JButton clearButton = makeButton("Clear data", new ActionListener() {
		public void actionPerformed(ActionEvent e) { clearData(); }
	    } );
	
	// Adjust X-axis zoom.
	Box xControl = new Box(BoxLayout.Y_AXIS);
	xLabel = makeLabel("", JLabel.CENTER);
	final JSlider xSlider = new JSlider(JSlider.HORIZONTAL, 0, 8, graph.scale);
	Hashtable<Integer, JLabel> xTable = new Hashtable<Integer, JLabel>();
	for (int i = 0; i <= 8; i += 2) {
	    xTable.put(new Integer(i),
		       makeSmallLabel("" + (Graph.MIN_WIDTH << i),
				      JLabel.CENTER));
	}
	xSlider.setLabelTable(xTable);
	xSlider.setPaintLabels(true);
	graph.updateXLabel();
	graph.setScale(graph.scale,parent.mode);
	xSlider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    //if (!xSlider.getValueIsAdjusting())
		    graph.setScale((int)xSlider.getValue(),parent.mode);
		}
	    });
	xControl.add(xLabel);
	xControl.add(xSlider);
	
	// Adjust Y-axis range.
	JLabel yLabel = makeLabel("Y:", JLabel.RIGHT);
	yText = makeTextField(12, new ActionListener() {
		public void actionPerformed(ActionEvent e) { setYAxis(); }
	    } );
	yText.setText(graph.gy0 + " - " + graph.gy1);
	
	JLabel intervalLabel = makeLabel("Choose interval:", JLabel.RIGHT);
	intervalText = makeTextField(5, new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
		setInterval();
		parent.SetMote();
		}
	    } );



	Box controls = new Box(BoxLayout.X_AXIS);
	controls.add(clearButton);
	controls.add(intervalLabel);
	controls.add(intervalText);
	controls.add(Box.createHorizontalGlue());
	controls.add(Box.createRigidArea(new Dimension(20, 0)));

	//controls.add(sampleLabel);
	//controls.add(sampleText);
	//controls.add(Box.createHorizontalGlue());
	//controls.add(Box.createRigidArea(new Dimension(20, 0)));
	controls.add(xControl);
	controls.add(yLabel);
	controls.add(yText);
	main.add(controls, BorderLayout.SOUTH);

        

	// The frame part
	frame = new JFrame("Z compression Demo");
//*************************************************//
	JMenuBar menuBar = new JMenuBar();
	JMenu mnHelp = new JMenu("Help");
	menuBar.add(mnHelp);
		
	frame.setJMenuBar(menuBar);

JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				AboutFrame aboutFrame=new AboutFrame();
				aboutFrame.setVisible(true);
				try{
					frame.add(aboutFrame);
				}catch(Exception except){
					
				}
			}
		});
		

		mnHelp.add(mntmAbout);
		
		JMenuItem mntmReadme = new JMenuItem("ReadMe");
		mntmReadme.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				ReadMeFrame readMeFrame=new ReadMeFrame();
				readMeFrame.setVisible(true);
				try{
					frame.add(readMeFrame);
				}catch(Exception except){
					
				}
			}
		});
		mnHelp.add(mntmReadme);



		fc = new JFileChooser();
JMenu mnInputChoose = new JMenu("Input choose");
		menuBar.add(mnInputChoose);
		
		JMenuItem mntmInputYourOwn = new JMenuItem("Input your own data");
		mntmInputYourOwn.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		mnInputChoose.add(mntmInputYourOwn);
		
		JMenuItem mntmReadExistingData = new JMenuItem("Read existing data");
		mntmReadExistingData.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				inputMode=2;
				int returnVal = fc.showOpenDialog(null);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                externalFile = fc.getSelectedFile();
	                //This is where a real application would open the file.
	                //log.append("Opening: " + file.getName() + "." + newline);
	            } else {
	                //log.append("Open command cancelled by user." + newline);
	            }
	            //log.setCaretPosition(log.getDocument().getLength());
				
			}
		});
		mnInputChoose.add(mntmReadExistingData);
		
		JMenu mnUseSampleData = new JMenu("Use sample Data");
		mnInputChoose.add(mnUseSampleData);
		
		JRadioButton rdbtnZebranet = new JRadioButton("ZebraNet");
		rdbtnZebranet.setSelected(true);
		rdbtnZebranet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inputMode=3;
			}
		});
		mnUseSampleData.add(rdbtnZebranet);
		
		JRadioButton rdbtnAccelerometer = new JRadioButton("Accelerometer");
		rdbtnAccelerometer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inputMode=4;
			}
		});
		mnUseSampleData.add(rdbtnAccelerometer);
		
		JRadioButton rdbtnVehicalTrace = new JRadioButton("VehicalTrace");
		rdbtnVehicalTrace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inputMode=5;
			}
		});
		mnUseSampleData.add(rdbtnVehicalTrace);
		
		JRadioButton rdbtnIntelLab = new JRadioButton("IntelLab");
		rdbtnIntelLab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inputMode=6;
			}
		});
		mnUseSampleData.add(rdbtnIntelLab);
		
		
        group = new ButtonGroup();
        group.add(rdbtnZebranet);
        group.add(rdbtnAccelerometer);
        group.add(rdbtnVehicalTrace);
        group.add(rdbtnIntelLab);

		JButton btnRun = new JButton("Local");
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
		  	    if(parent.mode==2){
				compress();
				run++;
			    }
			}
		});
		menuBar.add(btnRun);
		
		btnStart = new JButton("Start");
		btnStart.setEnabled(false);
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
		  	    if(startOrPause==0){
				parent.SetMote();
				parent.Control(1);
				if(parent.RegisterMote()){
				
				startOrPause=1;
				btnStart.setText("Pause");
				}

			    }	
			    else{
				try {
				parent.Control(0);
				Thread.sleep(100);
				parent.Control(0);
				Thread.sleep(100);
				parent.Control(0);
				Thread.sleep(100);
				parent.Control(0);
				}catch(Exception ee){
					error("Thread error");
				}
				if(parent.RegisterMote()){

				startOrPause=0;
				btnStart.setText("Start");
				}
			    }
			}
		});
		menuBar.add(btnStart);

		JButton btnReset = new JButton("Reset");
		btnReset.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
				parent.superReset();
				Thread.sleep(100);
				parent.superReset();
				Thread.sleep(100);
				parent.superReset();
				Thread.sleep(100);
				parent.superReset();
				}catch(Exception ee){
					error("Thread error");
				}
		                btnStart.setEnabled(false);
				startOrPause=0;
				btnStart.setText("Start");
			}
		});
		menuBar.add(btnReset);



//////////////////*//////////////////////////////
	frame.setSize(main.getPreferredSize());
	frame.getContentPane().add(main);
	frame.setVisible(true);
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) { System.exit(0); }
	    });
    }


    void compress(){
        if(run%7==0){
	    initialInput();
            graph.newDataDemo();		
        }
	else if(run%7==1){
            		round1Result=optimizedZCompression.optimizedCompress_round1((ArrayDeque<byte[]>) inputDeltaQueue);
                	int i=0;            		
            		int max=getLargestNumberOfBits(round1Result);
                	ArrayDeque<byte[]> drawBuf=new ArrayDeque<byte[]>(round1Result);
                	while(!drawBuf.isEmpty()){
                                int[] readings=new int[Constants.NREADINGS];
                                readings[0]=getNumberOfBits(drawBuf.poll());
                                parent.data.update(run,i,0,readings);                		
                		i++;
                	}
            graph.gy1=max+10;
	    graph.repaint();			
        }
	else if(run%7==2){
            		round2Result=optimizedZCompression.optimizedCompress_round2((ArrayDeque<byte[]>) round1Result,part,packetSize-1);
                	int i=0;            		
            		int max=getLargestNumberOfBits(round2Result);
                	ArrayDeque<byte[]> drawBuf=new ArrayDeque<byte[]>(round2Result);
                	while(!drawBuf.isEmpty()){
                                int[] readings=new int[Constants.NREADINGS];
                                readings[0]=getNumberOfBits(drawBuf.poll());
                                parent.data.update(run,i,0,readings);                		
                		i++;
                	}
            graph.gy1=max+10;
	    graph.repaint();

        }
	else if(run%7==3){
            		round3Result=optimizedZCompression.optimizedCompress_round2((ArrayDeque<byte[]>) round2Result,1,packetSize-1);
                	int i=0;            		
            		int max=getLargestNumberOfBits(round3Result);
                	ArrayDeque<byte[]> drawBuf=new ArrayDeque<byte[]>(round3Result);
                	while(!drawBuf.isEmpty()){
                                int[] readings=new int[Constants.NREADINGS];
                                readings[0]=getNumberOfBits(drawBuf.poll());
                                parent.data.update(run,i,0,readings);                		
                		i++;
                	}
            graph.gy1=max+10;
	    graph.repaint();

        }
	else if(run%7==4){
            		int i=0;
            		while(!round3Result.isEmpty()){
            			byte[] bb=round3Result.poll();
            			i++;
            			ArrayDeque<byte[]> current=optimizedZCompression.decode_1(bb);
            			for(byte[] b:current){
            				decode3Result.add(b);
            			}
            		}
                	ArrayDeque<byte[]> drawBuf=new ArrayDeque<byte[]>(decode3Result);
			i=0;
                	while(!drawBuf.isEmpty()){
                                int[] readings=new int[Constants.NREADINGS];
                                readings[0]=getNumberOfBits(drawBuf.poll())+run;
                                parent.data.update(run,i,0,readings);                		
                		i++;
                	}
	    graph.repaint();

        }
	else if(run%7==5){
            		int i=0;
            		while(!decode3Result.isEmpty()){
            			byte[] bb=decode3Result.poll();
            			i++;
            			ArrayDeque<byte[]> current=optimizedZCompression.decode_1(bb);
            			for(byte[] b:current){
            				decode2Result.add(b);
            			}
            		}
                	ArrayDeque<byte[]> drawBuf=new ArrayDeque<byte[]>(decode2Result);
			i=0;
                	while(!drawBuf.isEmpty()){
                                int[] readings=new int[Constants.NREADINGS];
                                readings[0]=getNumberOfBits(drawBuf.poll())+run;
                                parent.data.update(run,i,0,readings);                		
                		i++;
                	}
	    graph.repaint();

        }
	else if(run%7==6){
            		int i=0;
            		while(!decode2Result.isEmpty()){
            			byte[] bb=decode2Result.poll();
            			i++;
            			ArrayDeque<byte[]> current=optimizedZCompression.decode_1(bb);
            			for(byte[] b:current){
            				decode1Result.add(b);
            			}
            		}
                	ArrayDeque<byte[]> drawBuf=new ArrayDeque<byte[]>(decode1Result);
			i=0;
                	while(!drawBuf.isEmpty()){
                                int[] readings=new int[Constants.NREADINGS];
                                readings[0]=getNumberOfBits(drawBuf.poll())+run;
                                parent.data.update(run,i,0,readings);                		
                		i++;
                	}
	    graph.repaint();

        }

    }

    /* User operation: clear data */
    void clearData() {
	run=0;
        parent.data.totalMax=0;
	synchronized (parent) {
	    moteListModel.clear();
	    parent.clear();
	    if(parent.mode!=2){graph.newData();
		graph.gy1=10000;
		graph.gx1=200;
	    }
            else graph.newDataDemo();
            updateRate(0);
	}
    }

    /* User operation: set Y-axis range. */
    void setYAxis() {
	String val = yText.getText();

	try {
	    int dash = val.indexOf('-');
	    if (dash >= 0) {
		String min = val.substring(0, dash).trim();
		String max = val.substring(dash + 1).trim();

		if (!graph.setYAxis(Integer.parseInt(min), Integer.parseInt(max))) {
		    error("Invalid range " 
			  + min 
			  + " - " 
			  + max 
			  + " (expected values between 0 and 65535)");
		}
		return;
	    }
	}
	catch (NumberFormatException e) { }
	error("Invalid range " + val + " (expected NN-MM)");
    }

    /* User operation: set sample period. */
/*
    void setSamplePeriod() {
	String periodS = sampleText.getText().trim();
        parent.data.totalMax=0;
	try {
	    int newPeriod = Integer.parseInt(periodS);
	    if (parent.setInterval(newPeriod)) {
		return;
	    }
	}
	catch (NumberFormatException e) { }
	error("Invalid sample period " + periodS);
    }
*/
    /* Notification: sample period changed. */
/*
    void updateSamplePeriod() {
	sampleText.setText("" + parent.interval);
    }
*/
    /* Notification: new node. */
    void newNode(int nodeId,int period) {
	moteListModel.newNode(nodeId,period);
    }

    /* Notification: new data. */
    void newData() {
	graph.newData();
    }

    void setMode(){
	String modes = modeText.getText().trim();
	try {
	    int mode = Integer.parseInt(modes);
	    if (parent.setMode(mode)) {
		return;
	    }
	}
	catch (NumberFormatException e) { }
	error("Invalid Mode " + modes);
    }

    void setInterval(){
	String intervals = intervalText.getText().trim();
	try {
	    int interval = Integer.parseInt(intervals);
	    parent.setInterval(interval);

	}
	catch (NumberFormatException e) {	error("Invalid interval " + intervals); }

    }

    
    void updateRate(double rate){
        sr.setText(""+rate);
    }

    void updateStream(double str){
        stream.setText(""+str);
    }

    void error(String msg) {
	JOptionPane.showMessageDialog(frame, msg, "Error",
				      JOptionPane.ERROR_MESSAGE);
    }

//********************************************************************//
	public byte[] getDeltaValue(BigInteger previous, BigInteger current){
		
		if(previous.equals(current)){
			return new byte[]{1};
		}
		else if(previous.compareTo(current)<0){
			return current.subtract(previous).shiftLeft(1).add(new BigInteger("1")).toByteArray();
		}
		else return previous.subtract(current).shiftLeft(1).toByteArray();
		
	}

	public static int getNumberOfBits(byte[] b){
		
		if(b==null||b.length==0)return 0;
		
		return new BigInteger(b).bitLength();
	}

public boolean initialInput(){
		
	    inputQueue.clear();
	    inputDeltaQueue.clear();
	    outputDeltaQueue.clear();
	    outputQueue.clear();
	round1Result=new ArrayDeque<byte[]>();
	round2Result=new ArrayDeque<byte[]>();
	round3Result=new ArrayDeque<byte[]>();
	decode3Result=new ArrayDeque<byte[]>();
	decode2Result=new ArrayDeque<byte[]>();
	decode1Result=new ArrayDeque<byte[]>();    	
    	
		if(inputMode==2){
			try {
				inReader= new FileReader(externalFile);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(inputMode==3){
			InputStream  in = getClass().getResourceAsStream("/sampleData/ZebraNetData.in"); 

			inReader=new InputStreamReader(in);
		}
		else if(inputMode==4){
			InputStream  in = getClass().getResourceAsStream("/sampleData/accData.in"); 

			inReader=new InputStreamReader(in);
		}
		else if(inputMode==5){
			InputStream  in = getClass().getResourceAsStream("/sampleData/vehicalTrace.in"); 

			inReader=new InputStreamReader(in);
		}
		else if(inputMode==6){
			InputStream  in = getClass().getResourceAsStream("/sampleData/intelLab.in"); 

			inReader=new InputStreamReader(in);
		}else{

			inputMode=3;
			return initialInput();
			
		}
		
		BufferedReader br = null;

		try {

			String sCurrentLine;
			br = new BufferedReader(inReader);
			sCurrentLine = br.readLine();
			int diameter=Integer.parseInt(sCurrentLine);
            if(diameter>10)diameter=1;
            BigInteger[] previousValue=new BigInteger[diameter];
            BigInteger[] currentValue=new BigInteger[diameter];
            for(int i=0;i<diameter;i++){
            	previousValue[i]=new BigInteger("0");
            }
            
            int i=0;
            
			while ((sCurrentLine = br.readLine()) != null) {
				
				
				inputQueue.add(sCurrentLine);
				currentValue[i%diameter]=new BigInteger(sCurrentLine);
				byte[] inputDelta=getDeltaValue(previousValue[i%diameter],currentValue[i%diameter]);
                                int[] readings=new int[Constants.NREADINGS];
                                readings[0]=getNumberOfBits(inputDelta);
                                parent.data.update(0,i,0,readings);

				inputDeltaQueue.add(inputDelta);
				previousValue[i%diameter]=new BigInteger(sCurrentLine);
				i++;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
	error("initiation error");
			return false;
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		
	}


	public static int getLargestNumberOfBits(Deque<byte[]> inputDeltaQueue2){
		int max=0;
		for(byte[] b:inputDeltaQueue2){
			max=getNumberOfBits(b)>max?getNumberOfBits(b):max;
		}
		
		return max;
	}






















//********************************************************************//

}
