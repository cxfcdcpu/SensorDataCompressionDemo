
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JScrollBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class ReadMeFrame extends JFrame {

	JDesktopPane desktop;

	/**
	 * Create the frame.
	 */
	public ReadMeFrame() {
		super("ReadMe");

        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 150;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                  550,350);
 
        //Set up the GUI.
        desktop = new JDesktopPane(); //a specialized layered pane
        setContentPane(desktop);
        JTextArea textArea = new JTextArea();
        textArea.setBounds(10, 11, 552, 300);
        String newline="\n";
        textArea.append("How to use this Demo: "+newline);
        textArea.append("1. Choose a dataset"+newline);
        textArea.append("2. Choose one or multiple compression algorithm"+newline);
        textArea.append("3. Press Run"+newline);
        textArea.append("*****************************************************************"+newline);
        textArea.append("Graph of the Demo: "+newline);
        textArea.append("The X axis is the data item id. Data item are ranged by time. "+newline);
        textArea.append("One pixel of the X axis represent a data item"+newline);
        textArea.append("The Y axis is the number of bits of the data item. It is normalized in graph. "+newline);
        textArea.append("The Maximum value is Y axis is the largest number of bits in the dataset. "+newline);
        textArea.append("******************************************************************"+newline);
        textArea.append("Result of the Demo:  "+newline);
        textArea.append("Every compression Algorithm has its color."+newline);
        textArea.append("The result is the average bits after compressing one data item "+newline);
        textArea.append("Result of LEC And TinyPack will also show the encoded data's bits of each data item"+newline);
        
        desktop.add(textArea);
		
	}
	

}
