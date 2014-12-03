
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.filechooser.*;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.*;
import javax.swing.*;
/*
 * Fast converter between TrackMate Export XML files
 * and MTrackJ mdf files
 * 
 * Eugene Katrukha 2014
 * 
 */

public class TrackMate2MTrackJ extends JPanel
                             implements ActionListener, 
                             	PropertyChangeListener {
    static private final String newline = "\n";
  
    private JButton openButton;
    private JTextArea log;
    JFileChooser fc;
    private Task task;
    int nFcount = 0; //number of xml files
    boolean bFoundXML = false;
    
    File[] files;

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
                      
            nFcount =0;
            bFoundXML=false;
            //Initialize progress property.
            setProgress(0);
                     	
                for (File file:files)
                {
                	
	                if(file.isFile())
	                {
	                	String nFilename = new String(file.getName());
	                	if(nFilename.endsWith(".xml"))
	                	{
	                		bFoundXML=true;
	                		log.append("Opening: " + file.getName() + "." + newline);
			                try
			                {
			                	int nTrack = 0;
			                	int nFrame;

			                	FileWriter fstream = null; 
			                	BufferedWriter fout = null; 
			                	
			                	XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			                	InputStream in = new FileInputStream(file.getCanonicalPath());
			                	XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);

			                	int nPoint = 0;
			                	while (streamReader.hasNext()) {
			                        if (streamReader.isStartElement()) {
			                            switch (streamReader.getLocalName()) {
			                            case "Tracks": {
			                            	log.append("Total: " + streamReader.getAttributeValue(0) + " tracks... "); 

			                            	fstream = new FileWriter(file.getCanonicalPath()+".mdf");
						                	fout = new BufferedWriter(fstream);
						                	fout.write("MTrackJ 1.2.0 Data File");
						                	fout.newLine();
						                	fout.write("Assembly 1");
						                	fout.newLine();
						                	fout.write("Cluster 1");
						                	fout.newLine();
			                            	 
			                                break;
			                            }
			                            case "particle": {
			                            	nPoint =1;
			                            	nTrack = nTrack + 1;
			                            	fout.write("Track " + Integer.toString(nTrack));
			                            	fout.newLine();
			                            	//log.append("partice\n");
			                            	//log.append(streamReader.getAttributeValue(0));
			                                break;
			                            }
			                            case "detection": {
			                            	//log.append("detection\n");
			                            	nFrame = Integer.parseInt(streamReader.getAttributeValue(0))+1;
			                            	fout.write("Point " + Integer.toString(nPoint) + " " + streamReader.getAttributeValue(1) +  " " + streamReader.getAttributeValue(2) +  " " + streamReader.getAttributeValue(3)+" "+ Integer.toString(nFrame) +  " 1.0");
			                            	fout.newLine();
			                            	nPoint++;
			                            	//log.append(streamReader.getAttributeValue(0));
			                                break;
			                            }
			                            }
			                        }
			     
			                        streamReader.next();
			                    }
			                	//progress = nTrack;
			                	if(fout!=null)
			                	{
			                		fout.write("End of MTrackJ Data File");
			                		fout.close();
			                		fout = null;
			                		log.append("done." + newline);
			                	}
			                	else
			                	{
			                		log.append("Error!" + newline);
			                		log.append("It appears that this XML file is not in proper format." + newline);
			                		log.append("Make sure you used \"Export to XML...\" Trackmate function instead of \"Save\"" + newline);
			                	}
			                	log.setCaretPosition(log.getDocument().getLength());
			                	
			                	Thread.sleep(10);
			                }
			                catch (Exception em)
			                {
			                	 log.append("Error! ");
			                	 log.append(em.getMessage());
			                }

	                	}
	                }
	                nFcount = nFcount+1;
	                setProgress(Math.round(100*nFcount/files.length));

                }
            	            	                       	        
            return null;
        }
 
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
 
            openButton.setEnabled(true);
            setCursor(null); //turn off the wait cursor
            log.setCaretPosition(log.getDocument().getLength());
            if(bFoundXML)
            	log.append("All done!\n");
            else
            	log.append("No XML files found in the folder.\n");
        }
    }
    
    public TrackMate2MTrackJ() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml", "xml");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileFilter(filter);

        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openButton = new JButton("Open a Folder with Trackmate XML EXPORT files...");
        openButton.addActionListener(this);
      

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(TrackMate2MTrackJ.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                files = fc.getSelectedFile().listFiles();

                openButton.setEnabled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                task = new Task();
                task.addPropertyChangeListener(this);
                task.execute();
                
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
       
        }
    }

    
 
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {

           // log.append(String.format(
             //       "Total: %d%% TRACKS.\n", task.getProgress()));
        } 
    }
 
    
    

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TrackMate 2 MTrackJ converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new TrackMate2MTrackJ());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}
