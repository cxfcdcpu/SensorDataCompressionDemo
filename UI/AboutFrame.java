

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

public class AboutFrame extends JFrame {

	JDesktopPane desktop;

	/**
	 * Create the frame.
	 */
	public AboutFrame() {
		super("About");

        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 150;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                  400,300);
 
        //Set up the GUI.
        desktop = new JDesktopPane(); //a specialized layered pane
        setContentPane(desktop);
 
        //Make dragging a little faster but perhaps uglier.
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        ImageIcon icon = createImageIcon("/sampleData/About.gif","About");
        
        JLabel lblNewLabel = new JLabel(icon);
        
        lblNewLabel.setBounds(10, 11, 364, 240);
        desktop.add(lblNewLabel);
		
	}
	
	protected  ImageIcon createImageIcon(String path,
            String description) {
			java.net.URL imgURL =getClass().getResource(path);
				if (imgURL != null) {
						return new ImageIcon(imgURL, description);
					} else {
						System.err.println("Couldn't find file: " + path);
						return null;
					}
				}
}
