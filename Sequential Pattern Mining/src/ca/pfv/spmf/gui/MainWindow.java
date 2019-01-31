package ca.pfv.spmf.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import ca.pfv.spmf.gui.patternvizualizer.PatternVizualizer;
import ca.pfv.spmf.test.MainTestApriori_saveToFile;

public class MainWindow extends JFrame implements ThreadCompleteListener, UncaughtExceptionHandler
{
    private String inputFile = null; //Current input file
    private String outputFile = null; //Current output file
    private static final long serialVersionUID = 1L;
    
    //Components of the user interface
    private JPanel contentPane;
    private JTextField textFieldParam1;
    private JTextField textFieldParam2;
    private JTextField textFieldParam3;
    private JLabel labelParam1;
    private JLabel labelParam2;
    private JLabel labelParam3;
    private JLabel lbHelp1;
    private JLabel lbHelp2;
    private JLabel lbHelp3;
    private JTextField textFieldInput;
    private JTextField textFieldOutput;
    private JComboBox<String> comboBox;
    private JTextArea textArea;
    private JButton buttonRun;
    private JCheckBox checkboxOpenOutputText;
    private JButton buttonExample;
    private JLabel lblSetOutputFile;
    private JButton buttonOutput;
    private JButton buttonInput;
    private JLabel lblChooseInputFile;
    private JProgressBar progressBar;
    
    //  VARIABLES USED TO RUN AN ALGORITHM IN A SEPARATED THREAD
    // The current data mining task
    private static NotifyingThread currentRunningAlgorithmThread = null;
	private JCheckBox checkboxOpenOutputPatterViewer;

	//Create frame
    public MainWindow()
    {
        setResizable(false);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosed(WindowEvent arg0)
            {
                System.exit(0);
            }
        }); 
        setTitle("SPMF"); //Set the title of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //When the user clicks the "x" the software will close.
        setBounds(100, 100, 706, 621); //Size of the window
        contentPane = new JPanel(); //Create panel
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5)); //Set the border of the panel
        setContentPane(contentPane); //Assign the panel
        contentPane.setLayout(null);

        //ComboBox to store the list of algorithms.
        comboBox = new JComboBox<String>(new Vector<String>());
        comboBox.addItem("");
        comboBox.addItem("PrefixSpan");
        comboBox.addItem("PrefixSpan_AGP");
        comboBox.addItem("PrefixSpan_PostProcessingClosed");
        comboBox.addItem("PrefixSpan_with_strings");
        comboBox.addItem("SPADE");
        comboBox.addItem("SPADE_Parallelized");

        //What to do when the user choose an algorithm : 
        comboBox.addItemListener(new ItemListener()
        {
			@Override
			public void itemStateChanged(ItemEvent evt)
			{
				// We need to update the user interface:
				updateUserInterfaceAfterAlgorithmSelection(evt.getItem().toString(), evt.getStateChange() == ItemEvent.SELECTED);		
			}
        });
        comboBox.setBounds(263, 130, 312, 20);
        contentPane.add(comboBox); //Add selected item to the combobox

        //Run algorithm
        buttonRun = new JButton("Run Algorithm"); //Create the button
        buttonRun.setEnabled(false); //Make the button visible (active)
        buttonRun.addActionListener(new ActionListener() //Define event for the button
        {
            public void actionPerformed(ActionEvent arg0)
            {
                processRunAlgorithmCommandFromGUI(); // When the user clicks "run"
            }
        });
        buttonRun.setBounds(263, 381, 157, 23);
        contentPane.add(buttonRun); //Add the button to the content panel
        
        //Create "Choose an Algorithm" label
        JLabel lblChooseAnAlgorithm = new JLabel("Choose an Algorithm:");
        lblChooseAnAlgorithm.setBounds(12, 130, 204, 20);
        contentPane.add(lblChooseAnAlgorithm);
        
        //Create "University" label
        JLabel UniversityLabel = new JLabel("University label");
        UniversityLabel.addMouseListener(new MouseAdapter() //Add event to the label when pressed
        {
            //When the label is pressed
            public void mousePressed(MouseEvent arg0)
            {
            	//Show the web page of DEU
                openWebPage("http://www.deu.edu.tr/deuwebv2/Anasayfa.php?");
            }
        });
        UniversityLabel.setIcon(new ImageIcon(MainWindow.class.getResource("universite.jpg"))); //Assign visual icon to the label
        UniversityLabel.setBounds(12, 11, 100, 108);
        contentPane.add(UniversityLabel);
        
        //Create "Faculty" label
        JLabel FacultyLabel = new JLabel("Faculty label");
        FacultyLabel.addMouseListener(new MouseAdapter() //Add event to the label when pressed
        {
        	//When the label is pressed
            public void mousePressed(MouseEvent arg0)
            {
            	//Show the web page of DEU
                openWebPage("http://www.eng.deu.edu.tr/");
            }
        });
        FacultyLabel.setIcon(new ImageIcon(MainWindow.class.getResource("fakulte.png"))); //Assign visual icon to the label
        FacultyLabel.setBounds(590, 11, 100, 108);
        contentPane.add(FacultyLabel);
        
        //Create "Developer" label
        JLabel DeveloperLabel = new JLabel("Faculty label");
        DeveloperLabel.addMouseListener(new MouseAdapter() //Add event to the label when pressed
        {
        	//When the label is pressed
            public void mousePressed(MouseEvent arg0)
            {
            	//Show the web page of DEU
                openWebPage("https://www.linkedin.com/in/erdem-akbaba-5528a5113?trk=nav_responsive_tab_profile");
            }
        });
        DeveloperLabel.setIcon(new ImageIcon(MainWindow.class.getResource("developer.jpg"))); //Assign visual icon to the label
        DeveloperLabel.setBounds(298, 11, 100, 108);
        contentPane.add(DeveloperLabel);
        
        //Set the content of the textFieldParam1 with buffer
        textFieldParam1 = new JTextField();
        textFieldParam1.setBounds(263, 224, 157, 20);
        contentPane.add(textFieldParam1);
        textFieldParam1.setColumns(10);
        {
        	String buffer = new String(new byte[]{83,80,77,70});
	        if(getTitle().startsWith(buffer) != true)
	        {
	        	setTitle(buffer);
	        }
        }
        
        //Open input file button
        buttonInput = new JButton("Open");
        buttonInput.addActionListener(new ActionListener() //Add event for the "Open" button
        {
            public void actionPerformed(ActionEvent arg0)
            {
                askUserToChooseInputFile(); //Call the function to open input file
            }
        });        
        buttonInput.setBounds(509, 161, 66, 20);
        contentPane.add(buttonInput);
        
        //Open output file button
        buttonOutput = new JButton("Open");
        buttonOutput.addActionListener(new ActionListener() //Add event for the "Open" button
        {
            public void actionPerformed(ActionEvent arg0)
            {
                askUserToChooseOutputFile(); //Call the function to open output file
            }
        });
        buttonOutput.setBounds(509, 193, 66, 20);
        contentPane.add(buttonOutput);
        
        ///Other label parameters
        labelParam1 = new JLabel("Parameter 1:");
        labelParam1.setBounds(12, 227, 204, 14);
        contentPane.add(labelParam1);

        labelParam2 = new JLabel("Parameter 2:");
        labelParam2.setBounds(12, 258, 204, 14);
        contentPane.add(labelParam2);

        labelParam3 = new JLabel("Parameter 3:");
        labelParam3.setBounds(12, 293, 204, 14);
        contentPane.add(labelParam3);

        textFieldParam2 = new JTextField();
        textFieldParam2.setColumns(10);
        textFieldParam2.setBounds(263, 255, 157, 20);
        contentPane.add(textFieldParam2);

        textFieldParam3 = new JTextField();
        textFieldParam3.setColumns(10);
        textFieldParam3.setBounds(263, 290, 157, 20);
        contentPane.add(textFieldParam3);
        
        //Scrollpane for filling the textArea
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 415, 681, 170);
        contentPane.add(scrollPane);
        
        //Text area to write statistical values
        textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        System.setOut(new PrintStream(new TextAreaOutputStream(textArea)));
        
        //Text field for input file
        textFieldInput = new JTextField();
        textFieldInput.setEditable(false);
        textFieldInput.setBounds(263, 161, 236, 20);
        contentPane.add(textFieldInput);
        textFieldInput.setColumns(10);
        
        //Text field for output file
        textFieldOutput = new JTextField();
        textFieldOutput.setEditable(false);
        textFieldOutput.setColumns(10);
        textFieldOutput.setBounds(263, 193, 236, 20);
        contentPane.add(textFieldOutput);
        
        //Check box to open the output file with using text editor
        checkboxOpenOutputText = new JCheckBox("Using Text Editor");
        checkboxOpenOutputText.addActionListener(new ActionListener() //Add event for selection this check box
        {
        	public void actionPerformed(ActionEvent arg0)
        	{
        		if (checkboxOpenOutputText.isSelected()) //When this option selected
        		{
        			checkboxOpenOutputPatterViewer.setSelected(false); //Set the other option unselected
                }
        	}
        });
        checkboxOpenOutputText.setSelected(true);
        checkboxOpenOutputText.setBounds(215, 351, 150, 23);
        contentPane.add(checkboxOpenOutputText);
        
        //Check box to open the output file with using SPMF viewer
        checkboxOpenOutputPatterViewer = new JCheckBox("Using SPMF Viewer");
        checkboxOpenOutputPatterViewer.addActionListener(new ActionListener() //Add event for selection this check box
        {
        	public void actionPerformed(ActionEvent arg0)
        	{
        		if (checkboxOpenOutputPatterViewer.isSelected()) //When this option selected
        		{
        			checkboxOpenOutputText.setSelected(false); //Set the other option unselected
                }
        	}
        });
        checkboxOpenOutputPatterViewer.setBounds(49, 345, 164, 29);
        contentPane.add(checkboxOpenOutputPatterViewer);
        
        //Help button
        buttonExample = new JButton("?");
        buttonExample.setEnabled(false);
        buttonExample.addActionListener(new ActionListener() //Add event to open the web page
        {
            public void actionPerformed(ActionEvent arg0)
            {
            	//Open web page corresponding to the algorithm selected
                String choice = (String) comboBox.getSelectedItem();
                openHelpWebPageForAlgorithm(choice);
            }
        });
        buttonExample.setBounds(585, 129, 49, 23);
        contentPane.add(buttonExample);
        
        //Label to choose input file
        lblChooseInputFile = new JLabel("Choose Input File");
        lblChooseInputFile.setBounds(12, 164, 97, 14);
        contentPane.add(lblChooseInputFile);
        
        //Label to set output file
        lblSetOutputFile = new JLabel("Set Output File");
        lblSetOutputFile.setBounds(12, 196, 97, 14);
        contentPane.add(lblSetOutputFile);
        
        //Other labels for hint (help)
        lbHelp1 = new JLabel("help1");
        lbHelp1.setBounds(430, 227, 157, 14);
        contentPane.add(lbHelp1);

        lbHelp2 = new JLabel("help2");
        lbHelp2.setBounds(430, 258, 157, 14);
        contentPane.add(lbHelp2);

        lbHelp3 = new JLabel("help3");
        lbHelp3.setBounds(430, 293, 157, 14);
        contentPane.add(lbHelp3);
        
        //Label for opening output file with two selection
        JLabel lblOpenOutputFile = new JLabel("Open Output File With: ");
        lblOpenOutputFile.setBounds(12, 318, 157, 20);
        contentPane.add(lblOpenOutputFile);

        hideAllParams(); //Hide all parameters when the user change algorithm
    }
    
    /*
    This method updates the user interface according to what the user has selected or unselected
    in the list of algorithms. For example, if the user choose the "PrefixSpan" algorithm
    the parameters of the PrefixSpan algorithm will be shown in the user interface.
    */
    private void updateUserInterfaceAfterAlgorithmSelection(String algorithmName, boolean isSelected)
	{
        if (isSelected) //If the user selected any algorithm
        {
            buttonRun.setEnabled(true); //Make the run button active
            buttonExample.setEnabled(true); //Make the help button active
            
            //If the user selected "PRefixSpan" algorithm
            if ("PrefixSpan".equals(algorithmName))
            {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose Min. Support (%):", labelParam1, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam2, "Max. Pattern Length (Optional):", labelParam2, "(e.g. 4 items)");
                setParam(textFieldParam3, "Show Sequence IDs (Optional):", labelParam3, "(default: false)");

            }
            
            //If the user selected "PrefixSpan_with_strings" algorithm
            else if ("PrefixSpan_with_strings".equals(algorithmName))
            {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose Min. Support (%):", labelParam1, "(e.g. 0.4 or 40%)");
            }
            
            //If the user selected any type of SPADE or any type of PrefixSpan algorithms
            else if ("SPADE".equals(algorithmName)
            		|| "SPADE_Parallelized".equals(algorithmName)
            		|| "PrefixSpan_AGP".equals(algorithmName)
            		|| "PrefixSpan_PostProcessingClosed".equals(algorithmName))
            {
            	//Show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose Min. Support (%):", labelParam1, "(e.g. 0.4 or 40%)");
                setParam(textFieldParam2, "Show Sequence IDs (Optional):", labelParam2, "(default: false)");
            }
        }
        else //If no algorithm is selected, then hide all paremeters
        {
            
            hideAllParams(); //Hide parameters
            buttonRun.setEnabled(false); //Make the run button passive
            buttonExample.setEnabled(false); //Make the help button passive
        }
	}
    
    //Set parameters according to algorithms
    private  void setParam(JTextField textfield, String name, JLabel label, String helpText)
    {
        label.setText(name);
        textfield.setEnabled(true);
        textfield.setVisible(true);
        label.setVisible(true);
        
        if (textfield == textFieldParam1)
        {
            lbHelp1.setText(helpText);
            lbHelp1.setVisible(true);
        }
        else if (textfield == textFieldParam2)
        {
            lbHelp2.setText(helpText);
            lbHelp2.setVisible(true);
        }
        else if (textfield == textFieldParam3)
        {
            lbHelp3.setText(helpText);
            lbHelp3.setVisible(true);
        }
    }

    //Hide all parameters
    private  void hideAllParams()
    {
        labelParam1.setVisible(false);
        labelParam2.setVisible(false);
        labelParam3.setVisible(false);
        
        lbHelp1.setVisible(false);
        lbHelp2.setVisible(false);
        lbHelp3.setVisible(false);
        
        textFieldParam1.setVisible(false);
        textFieldParam2.setVisible(false);
        textFieldParam3.setVisible(false);

        lblSetOutputFile.setVisible(true);
        buttonOutput.setVisible(true);
        
        textFieldOutput.setVisible(true);
        textFieldInput.setVisible(true);
        
        lblChooseInputFile.setVisible(true);
        
        buttonInput.setVisible(true);
        
        checkboxOpenOutputText.setVisible(true);
    }
    
    //Text area for statistics of running algorithm
    static class TextAreaOutputStream extends OutputStream
    {
        JTextArea textArea;
        public TextAreaOutputStream(JTextArea textArea)
        {
            this.textArea = textArea;
        }
        
        //Flush the result
        public void flush()
        {
            textArea.repaint();
        }
        
        //Wrþte the result
        public void write(int b)
        {
            textArea.append(new String(new byte[]{(byte) b}));
        }
    }
    
    //Open a URL in the default browser
    private void openWebPage(String url)
    {
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (java.io.IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
    
    //Open the help page
    private void openHelpWebPageForAlgorithm(String choice)
    {
		if ("PrefixSpan".equals(choice) || "PrefixSpan_AGP".equals(choice) || "PrefixSpan_with_strings".equals(choice) || "PrefixSpan_PostProcessingClosed".equals(choice))
		{
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#examplePrefixSpan");
        }
        else if ("SPADE".equals(choice) || "SPADE_Parallelized".equals(choice))
        {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmspade");
        }
	}
    
    //Choose input file
	private void askUserToChooseInputFile()
	{
		try
		{
			//When the user click to choose the input file
		    File path;
		    // Get the last path used by the user, if there is one
		    String previousPath = PathsManager.getInstance().getInputFilePath();
		    if (previousPath == null) //If there is no previous file
		    {
		        URL main = MainTestApriori_saveToFile.class.getResource("MainTestApriori_saveToFile.class");
		        if (!"file".equalsIgnoreCase(main.getProtocol()))
		        {
		            path = null;
		        }
		        else
		        {
		            path = new File(main.getPath());
		        }
		    }
		    else //If there is any previous file, show the last path
		    {
		        path = new File(previousPath);
		    }
		    
		    //Create a file chooser to let the user select the file
		    final JFileChooser fc = new JFileChooser(path);
		    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		    int returnVal = fc.showOpenDialog(MainWindow.this);
		    
		    //If any file has been chosen
		    if (returnVal == JFileChooser.APPROVE_OPTION)
		    {
		        File file = fc.getSelectedFile();
		        textFieldInput.setText(file.getName());
		        inputFile = file.getPath(); // remember the file he chose
		    }
		    //Remember this folder for next time.
		    if (fc.getSelectedFile() != null)
		    {
		        PathsManager.getInstance().setInputFilePath(fc.getSelectedFile().getParent());
		    }
		}
		catch (Exception e)
		{
		    JOptionPane.showMessageDialog(null, "An error occured while opening the input file dialog. ERROR MESSAGE = " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);           
		}
	}
	
	//Choose output file
	private void askUserToChooseOutputFile()
	{
		try
		{
			//When the user click to choose the input file
		    File path;
		    // Get the last path used by the user, if there is one
		    String previousPath = PathsManager.getInstance().getOutputFilePath();
		    if (previousPath == null) //If there is any previous file, show the last path
		    {
		        URL main = MainTestApriori_saveToFile.class.getResource("MainTestApriori_saveToFile.class");
		        if (!"file".equalsIgnoreCase(main.getProtocol()))
		        {
		            path = null;
		        }
		        else
		        {
		            path = new File(main.getPath());
		        }
		    }
		    else //If there is any previous file, show the last path
		    {
		        path = new File(previousPath);
		    }

		    //Create a file chooser to let the user select the file
		    final JFileChooser fc;
		    if (path != null)
		    {
		        fc = new JFileChooser(path.getAbsolutePath());
		    }
		    else
		    {
		        fc = new JFileChooser();
		    }
		    int returnVal = fc.showSaveDialog(MainWindow.this);

		    //If any file has been chosen
		    if (returnVal == JFileChooser.APPROVE_OPTION)
		    {
		        File file = fc.getSelectedFile();
		        textFieldOutput.setText(file.getName());
		        outputFile = file.getPath(); //Save the file path
		        
		        //Remember this folder for next time.
		        if (fc.getSelectedFile() != null)
		        {
		            PathsManager.getInstance().setOutputFilePath(fc.getSelectedFile().getParent());
		        }
		    }

		}
		catch (Exception e)
		{
		    JOptionPane.showMessageDialog(null, "An error occured while opening the output file dialog. ERROR MESSAGE = " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);    
		}
		
        {
        	String buffer = new String(new byte[]{83,80,77,70});
	        if(getTitle().startsWith(buffer) != true)
	        {
	        	setTitle(buffer);
	        }
        }
	}
	
	//Receive a notification when when an algorithm terminates that was launched by the user by clicking "Run algorithm"
	@Override
	public void notifyOfThreadComplete(Thread thread, boolean succeed)
	{
		//If the algorithm terminates, there is an output file
		if (succeed  && lblSetOutputFile.isVisible())
		{
			//If user wants to use the text editor to open the file
			if(checkboxOpenOutputText.isSelected())
			{
			    Desktop desktop = Desktop.getDesktop(); //Open the output file if the CheckBox is checked 
			    if (desktop.isSupported(Desktop.Action.OPEN)) //Check first if we can open it on this operating system:
			    {
			        try
			        {
			            desktop.open(new File(outputFile)); //If yes, open it
			        }
			        catch (IOException e)
			        {
			            JOptionPane.showMessageDialog(null,
			                    "The output file failed to open with the default application. "
			                    + "To fix the problem, consider changing the extension of the output file to .txt."
			                    + "\n\n ERROR MESSAGE = " + e.toString(), "Error",
			                    JOptionPane.ERROR_MESSAGE);
			        }
			        catch (SecurityException e)
			        {
			            JOptionPane.showMessageDialog(null,
			                    "A security error occured while trying to open the output file. ERROR MESSAGE = " + e.toString(), "Error",
			                    JOptionPane.ERROR_MESSAGE);
			        }
			        catch (Throwable e)
			        {
			            JOptionPane.showMessageDialog(null,
			                    "An error occured while opening the output file. ERROR MESSAGE = " + e.toString(), "Error",
			                    JOptionPane.ERROR_MESSAGE);
			        }
			    }
			}
			//If user wants to use the patter viewer to open the file
			else if(checkboxOpenOutputPatterViewer.isSelected())
			{
			    //Open the output file if the CheckBox is checked 
				try
				{
				    PatternVizualizer patternViz = new PatternVizualizer(outputFile);
				}
				catch (SecurityException e)
				{
		            JOptionPane.showMessageDialog(null,
		                    "A security error occured while trying to open the output file. ERROR MESSAGE = " + e.toString(), "Error",
		                    JOptionPane.ERROR_MESSAGE);
		        }
				catch (Throwable e)
				{
		            JOptionPane.showMessageDialog(null,
		                    "An error occured while opening the output file. ERROR MESSAGE = " + e.toString(), "Error",
		                    JOptionPane.ERROR_MESSAGE);
		        }
			}
		}

		buttonRun.setText("Run algorithm");
        comboBox.setEnabled(true);
	}
	
	//This method receives the notifications when an algorithm launched by the user throw an exception
	@Override
	public void uncaughtException(Thread thread, Throwable e)
	{
		//If the thread just die because the user click on the "Stop algorithm" button
		if(e instanceof ThreadDeath)
		{
			//We just let the thread die.
		}
		else if(e instanceof NumberFormatException)
		{
			//If it is a number format exception, meaning that the user enter a string as a parameter instead of an integer or double value.
			JOptionPane.showMessageDialog(null,
                    "Error. Please check the parameters of the algorithm.  The format for numbers is incorrect. \n"
                    + "\n ERROR MESSAGE = " + e.toString(), "Error",
                    JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			//If another kind of error occurred while running the algorithm, show the error.
            JOptionPane.showMessageDialog(null,
                    "An error occurred while trying to run the algorithm. \n ERROR MESSAGE = " + e.toString(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
		textArea.setText("");
	}

	/**
	 * 
	 * 
	 */
	//This method is called when the user click the "Run" or "Stop" button of the user interface, to launch the chosen algorithm and thereafter catch exception if one occurs.
	private void processRunAlgorithmCommandFromGUI()
	{
		//If a thread is already running (the user click on the stop Button
		if(currentRunningAlgorithmThread != null && currentRunningAlgorithmThread.isAlive())
		{
			//Stop that thread
			currentRunningAlgorithmThread.stop();
			
			textArea.setText("Algorithm stopped. \n");
			buttonRun.setText("Run algorithm");
	        progressBar.setIndeterminate(false);
	        comboBox.setEnabled(true);
			return;
		}
		
		//Get the parameters
		final String choice = (String) comboBox.getSelectedItem();
		final String parameters[] = new String[6];
		parameters[0] = textFieldParam1.getText();
		parameters[1] = textFieldParam2.getText();
		parameters[2] = textFieldParam3.getText();
		textArea.setText("Algorithm is running...\n");
		
        buttonRun.setText("Stop algorithm");
        comboBox.setEnabled(false);
        
        //Run the selected algorithm in a new thread, create a thread to execute the algorithm
		currentRunningAlgorithmThread = new NotifyingThread()
		{
			@Override
			public void doRun() throws Exception
			{
				CommandProcessor.runAlgorithm(choice, inputFile, outputFile, parameters);
			}
		};
		currentRunningAlgorithmThread.addListener(this); //The main thread will listen for the completion of the algorithm
		currentRunningAlgorithmThread.setUncaughtExceptionHandler(this); //The main thread will also listen for exception generated by the algorithm
		currentRunningAlgorithmThread.start(); //Run the thread
	}
}
