package ca.pfv.spmf.gui.patternvizualizer;

import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import ca.pfv.spmf.gui.PathsManager;
import ca.pfv.spmf.gui.patternvizualizer.filters.AbstractFilter;
import ca.pfv.spmf.test.MainTestApriori_saveToFile;

//Visualize the patterns found by algorithms
public class PatternVizualizer extends JFrame
{
	
	//Generated serial ID*/
	private static final long serialVersionUID = -2012129335077139428L;
	
	//The table for showing the patterns to the user
	JTable table;
	
	//The label indicating the number of patterns currently shown in the Jtable
	private JLabel labelNumberOfPatterns;
	
	//Variables for storing the data from the TableModel used in the Jtable
	Vector<List<Object>> data = null;
	
	//List of table column names
	Vector<String> columnNames = null;
	
	//List of table column classes (Integer, Double, String)
	Vector<Class>  columnClasses = null;
	
	//The JList showing the current filters that are applied on the Jtable
	private JList listFilters;
	
	//The list model for the JList showing the filters
	private DefaultListModel<String> listModelFilters;

	//The "Remove selected filter" button
	private JButton btnRemoveFilter;
	
	//The "Remove all filters" button
	private JButton btnRemoveAllFilters;

	//The list of current filters, used for filtering the JTable
	PatternTableRowFilters  rowFilters = new PatternTableRowFilters();

	//The TableRowSorter used by the JTable
    private TableRowSorter<PatternTableModel> sorter;

    //The TableModel used by the JTable
    private PatternTableModel model;
    private JTextField textFieldSearch;

	private JComboBox comboBoxExport;
	
	//Initialize the windows for vizualizing patterns, and diplay patterns from a file in SPMF format.
	public PatternVizualizer(String patternFilePath) throws ParseException, IOException
	{
		//Set the size of the window, and make it non-resizeable
		setSize(800, 600);
		setResizable(false);
		
		//Get the file name 
		File file = new File(patternFilePath);
		String fileName = file.getName();
		
		//Get the file last modification date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		//printing value of Date System.out.println("current Date: " + currentDate); DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss"); //formatted value of current Date System.out.println("Milliseconds to Date: " + df.format(currentDate)); - See more at: http://javac.in/?p=402#sthash.ziDUnonk.dpuf
		String modificationDate =  sdf.format(new Date(file.lastModified()));
		
		//Set the window title and layout
		setTitle("SPMF - Pattern vizualization tool");
		getContentPane().setLayout(null);
		
		//Add the scroll pane for the Jtable
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(22, 41, 572, 462);
		getContentPane().add(scrollPane);
		
		//Create the Jtable for showing patterns
		table = new JTable();
		
		//let the user sort the columns in the table by clicking on the column headers
		table.setAutoCreateRowSorter(true);
		table.setCellSelectionEnabled(true);
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		scrollPane.setViewportView(table);
		
		//Set the horizontal and vertical scrollbars for the table
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Create the label for showing the number of patterns currently displayed
		labelNumberOfPatterns = new JLabel();
		labelNumberOfPatterns.setBounds(22, 514, 189, 14);
		getContentPane().add(labelNumberOfPatterns);
		
		//Show the patern file size
		double size = file.length() / 1024d / 1024d;
		String fileSize = String.format("%.4f", size);
		JLabel lblFileSizemb = new JLabel("File size (MB): " + fileSize);
		lblFileSizemb.setBounds(221, 535, 174, 14);
		getContentPane().add(lblFileSizemb);
		
		//Show the file name
		JLabel lblFileName = new JLabel("File name: " + fileName);
		lblFileName.setBounds(22, 535, 224, 14);
		getContentPane().add(lblFileName);
		
		//Show the date of last modification
		JLabel lblLastModified = new JLabel("Last modified: " + modificationDate);
		lblLastModified.setBounds(405, 535, 217, 14);
		getContentPane().add(lblLastModified);
		
		//Label "Patterns:"
		JLabel lblPatterns = new JLabel("Patterns:");
		lblPatterns.setBounds(22, 16, 77, 14);
		getContentPane().add(lblPatterns);
		
		//Label "Filters"
		JLabel lblApplyFilters = new JLabel("Apply filter(s):");
		lblApplyFilters.setBounds(604, 104, 138, 14);
		getContentPane().add(lblApplyFilters);
		
		//Create the JList for showing the current filters and create the underlying ListModel
		listModelFilters = new DefaultListModel();
		
		//Read the file containing patterns to fill the JTable
		readFile(patternFilePath);
		
		//Set the window as modal
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		//Create the button for adding a filter
		JButton btnAddAFilter = new JButton("Add a filter");
		btnAddAFilter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) //When the user click on the button
			{ 
				// A window is created for letting the user create the filter
				FilterSelectionWindow window = new FilterSelectionWindow(columnNames, columnClasses, rowFilters, PatternVizualizer.this);
			}
		});
		btnAddAFilter.setBounds(609, 284, 174, 23);
		getContentPane().add(btnAddAFilter);
		
		//Create the "remove selected filter" button
		btnRemoveFilter = new JButton("Remove selected filter");
		btnRemoveFilter.setBounds(609, 308, 174, 23);
		btnRemoveFilter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) //Add action to the remove button
			{
				removeSelectedFilter();
			}
		});
		btnRemoveFilter.setEnabled(false);
		getContentPane().add(btnRemoveFilter);

		//Create the "remove all filters" button
		btnRemoveAllFilters = new JButton("Remove all filters");
		btnRemoveAllFilters.setBounds(609, 333, 174, 23);
		btnRemoveAllFilters.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				removeAllFilters();
			}

		});
		btnRemoveAllFilters.setEnabled(false);
		getContentPane().add(btnRemoveAllFilters);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(609, 124, 174, 155);
		getContentPane().add(scrollPane_1);
		listFilters = new JList(listModelFilters);
		scrollPane_1.setViewportView(listFilters);
		
		JLabel lblNewLabel = new JLabel("Search:");
		lblNewLabel.setBounds(604, 39, 69, 20);
		getContentPane().add(lblNewLabel);
		
		textFieldSearch = new JTextField();
		textFieldSearch.setBounds(609, 59, 128, 26);
		getContentPane().add(textFieldSearch);
		textFieldSearch.setColumns(10);
		
		//Search button
		JButton btnSearch = new JButton("");
		btnSearch.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				search();
			}
		});
		
		btnSearch.setIcon(new ImageIcon(PatternVizualizer.class.getResource("/ca/pfv/spmf/gui/patternvizualizer/find.gif")));
		btnSearch.setBounds(738, 59, 30, 26);
		getContentPane().add(btnSearch);
		
		//Export label
		JLabel lblExportTo = new JLabel("Export current view to:");
		lblExportTo.setBounds(604, 372, 138, 20);
		getContentPane().add(lblExportTo);
		
		comboBoxExport = new JComboBox();
		comboBoxExport.setBounds(609, 396, 138, 26);
		getContentPane().add(comboBoxExport);
		comboBoxExport.addItem("SPMF format");
		comboBoxExport.addItem("TSV format");
		comboBoxExport.addItem("CSV format");
		
		//Export Button
		JButton buttonExport = new JButton("");
		buttonExport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) //Add action to the button to export the result
			{
				export(); //Call export funct�on
			}
		});
		buttonExport.setIcon(new ImageIcon(PatternVizualizer.class.getResource("/ca/pfv/spmf/gui/patternvizualizer/save.gif")));
		buttonExport.setBounds(753, 396, 30, 26);
		getContentPane().add(buttonExport);
		listFilters.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent arg0)
			{
				selectFilter(arg0);
			}
		});
		
		setVisible(true); //Set this window as visible
	}
	
	//Read a file containing patterns and show the patterns in the table
	private void readFile(String patternFilePath) throws IOException
	{
		//Variable to count the number of patterns
		int numberOfPatterns = 0;
		
		//Initialize the variables used by the JTable
		data = new Vector<List<Object>>();
		columnNames = new Vector<String>();
		columnClasses = new Vector<Class>();
		columnClasses.add(String.class);
		columnNames.add("Pattern"); //Add a first column named "Pattern" to the list of columns
		
		//Create a buffered reader for reading the file containing the patterns
		BufferedReader br = new BufferedReader(new FileReader(patternFilePath));
        String line;
        while((line = br.readLine()) != null)
        {
			// if the line is  a comment, is  empty or is  metadata
			if (line.isEmpty() == true ||line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@')
			{
				//Do nothing
			}
			else
			{
				//Create a list of Object for storing the values
				List<Object> lineData = new ArrayList<Object>();
				
				//Find the position of the next "#" symbol indicating the end of the first attribute value
				int positionFirstDelimiter = line.indexOf(" #");
				
				//If there is no "#", we will consider that the line has a single attribute
				if(positionFirstDelimiter == -1)
				{
					//Add the first attribute value to the data of this line
					lineData.add(line.substring(0, line.length()));
				}
				else //Otherwise, we will break down the line into several attributes using the "#"
				{
					//Add the first attribute value to the data of this line
					lineData.add(line.substring(0, positionFirstDelimiter));
	
					//Split the rest of the line using the " " delimiter
					String[] split = line.substring(positionFirstDelimiter+1,line.length()).split(" ");
					
					for(String token: split) //For each token
					{
						//If it is an attribute name
						if(token.length() > 0 && token.charAt(0) == '#')
						{
							//If it is the first line
							if(numberOfPatterns == 0)
							{
								columnNames.add(token); //Add that attribute name to the list of column
							}
						}
						else //If the token is  an attribute value, we first check if it is a double value
						{
							Double doubleValue = isDouble(token);
							if(doubleValue != null)
							{
								//If first time, we remember the class for this column
								if(numberOfPatterns == 0)
								{
									columnClasses.add(Double.class);
								}
								lineData.add(doubleValue); //If it is a double value, we add that value the list of attribute value
								continue; 
							}
							
							// We check if it is an integer value
							Integer integerValue = isInteger(token);
							if(integerValue != null)
							{
								//If first time, we remember the class for this column
								if(numberOfPatterns == 0)
								{
									columnClasses.add(Double.class);
								}
								//If it is an integer value, we add that value the list of attribute value
								columnClasses.add(Integer.class);
								lineData.add(integerValue);
								continue;
							}
							
							//We check if it is a boolean value
							Boolean booleanValue = isBoolean(token);
							if(booleanValue != null)
							{
								//If first time, we remember the class for this column
								if(numberOfPatterns == 0)
								{
									columnClasses.add(Boolean.class);
								}
								//If it is a boolean value, we add that value the list of attribute value
								columnClasses.add(Boolean.class);
								lineData.add(booleanValue);
								continue;
							}
							
							//Else we assume that it is a string value
							columnClasses.add(String.class);
							lineData.add(token);
						}
					}
				}
				data.add(lineData); //Add the line to the model
				numberOfPatterns++; //Count the number of patterns
			}
        }
        br.close(); //Close the file
        
        if(numberOfPatterns == 0) //If the file is empty, do nothing
        {
        	return;
        }

        //We have filled the table model, so now we set it as the table model for the JTable
        model = new PatternTableModel(data, columnNames, columnClasses);
        table.setModel(model);
        
		//We set the table sorter of the JTable
        sorter = new TableRowSorter<PatternTableModel>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(rowFilters);
        
        //We auto adjust column widths of tables so that the values are fully displayed
        TableColumnAdjuster tca = new TableColumnAdjuster(table);
        tca.adjustColumns();
		
        //We update the number of patterns shown in the window
        refreshNumberOfPatternsDisplayed();
	}
	
	//Check if a string value is a doule value
    private Double isDouble(String token)
    {
    	Double result = null;
    	try
    	{
    		result = Double.valueOf(token);
    	}
    	catch(Exception e)
    	{ 	
    		
    	}
		return result;
	}
    
    //Check if a string value is an integer value
    private Integer isInteger(String token)
    {
    	Integer result = null;
    	try
    	{
    		result = Integer.valueOf(token);
    	}
    	catch(Exception e)
    	{
    		
    	}
		return result;
	}
    
    //Check if a string value is a boolean value
    private Boolean isBoolean(String token)
    {
    	Boolean result = null;
    	try
    	{
    		result = Boolean.valueOf(token);
    	}
    	catch(Exception e)
    	{
    		
    	}
		return result;
	}

    /**
     * 
     * 
     */
    //This method is called when the user has added a new filter. It updates the list of filters, the buttons and also the JTable.
	public void filtersHaveBeenUpdated()
	{
		
		listModelFilters.clear(); //Remove all filters
		
		//Fill the JList of filters
		for(int i =0; i < rowFilters.filters.size(); i++)
		{
			AbstractFilter filter = (AbstractFilter) rowFilters.filters.get(i);
			listModelFilters.addElement(filter.getFilterWithParameterName());				 
		}
		
		//If there are some filters, then enable the bubtton "Remove all filters"
		if(rowFilters.filters.size() >=0)
		{
			btnRemoveAllFilters.setEnabled(true);
		}
		
		//Notifiy all listeners that the filters have been updated. This will refresh the Jtable using the new filters
		for(TableModelListener listener: model.listeners)
		{
			listener.tableChanged(new TableModelEvent(model));
		}
		refreshNumberOfPatternsDisplayed(); //Refresh the number of patterns displayed in the window
	}
	
	//This method is called when the user selects a filter from the list of filters
	protected void selectFilter(ListSelectionEvent arg0)
	{
		//If a filter is selected
		if(listFilters.getSelectedIndex()> -1)
		{
			btnRemoveFilter.setEnabled(true); //Enable the button "Remove selected filter"
		}
	}
	
	//Method to remove the current selected filter in the list of filters.
	private void removeSelectedFilter()
	{
		//Get the index of the selected filter in the list of filter
		int index = listFilters.getSelectedIndex();
		
		//Remove the filter from the JList model
		listModelFilters.remove(index);
		
		//Remove the filter from the filters used by the JTable
		rowFilters.filters.remove(index);
		
		//Disable the button for removing the selected filter
		btnRemoveFilter.setEnabled(false);
		
		//If there are no filters left, then we also disable the "remove all filters" button
		if(rowFilters.filters.size() == 0){
			btnRemoveAllFilters.setEnabled(false);
		}
		
		// Notifiy all listeners that the filters have been updated. This will refresh the Jtable using the new filters 
		for(TableModelListener listener: model.listeners)
		{
			listener.tableChanged(new TableModelEvent(model));
		}
		refreshNumberOfPatternsDisplayed(); //Refresh the number of patterns displayed in the window
	}

	/**
	 * 
	 * 
	 * 
	 */
	//This method is called when the user removes all filters. It clear the JList of filters and also removes all filters from the Jtable, and update the user interface accordingly.
	private void removeAllFilters()
	{
		//Delete all the filters in the JList of filters
		listModelFilters.clear();
		
		//Delete all the filters in the Jtable
		rowFilters.filters.clear();
		
		//Disable the button for removing filters
		btnRemoveAllFilters.setEnabled(false);
		btnRemoveFilter.setEnabled(false);
		
		// Notifiy all listeners that the filters have been updated. This will refresh the Jtable using the new filters
		for(TableModelListener listener: model.listeners)
		{
			listener.tableChanged(new TableModelEvent(model));
		}
		
		refreshNumberOfPatternsDisplayed(); //Refresh the number of patterns displayed in the window
	}
	
	//Refresh the number of patterns displayed in the window
	private void refreshNumberOfPatternsDisplayed()
	{
		labelNumberOfPatterns.setText("Number of patterns: " + table.getRowCount());
	}
	
	//This method is called when the user click on the button to export the current patters to a file format.
	protected void export()
	{
		String selection = (String)comboBoxExport.getSelectedItem();
		
		//Ask the user to choose the filename and path
		String outputFilePath = null;
		try
		{
		    File path;
		    
		    //Get the last path used by the user, if there is one
		    String previousPath = PathsManager.getInstance().getOutputFilePath();
		    
		    //If there is no previous path (first time user), show the files in the "examples" package of the spmf distribution.
		    if (previousPath == null)
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
		    else //Otherwise, use the last path used by the user.
		    {
		        path = new File(previousPath);
		    }
		    
		    //Ask the user to choose a file
		    final JFileChooser fc;
		    if (path != null)
		    {
		        fc = new JFileChooser(path.getAbsolutePath());
		    }
		    else
		    {
		        fc = new JFileChooser();
		    }
		    int returnVal = fc.showSaveDialog(PatternVizualizer.this);

		    //If the user chose a file
		    if (returnVal == JFileChooser.APPROVE_OPTION)
		    {
		        File file = fc.getSelectedFile();
		        outputFilePath = file.getPath(); //Save the file path
		        
		        // save the path of this folder for next time.
		        if (fc.getSelectedFile() != null)
		        {
		            PathsManager.getInstance().setOutputFilePath(fc.getSelectedFile().getParent());
		        }
		    }
		    else //The user did not choose so we return
		    {
		    	return;
		    }

		}
		catch (Exception e)
		{
		    JOptionPane.showMessageDialog(null,
		            "An error occured while opening the output file dialog. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}

		try
		{
			//If the user wants to save in CSV format
			if("CSV format".equals(selection))
			{
				exportToCSV(table, outputFilePath);
			}
			//If the user wants to save in TSV format
			else if("TSV format".equals(selection))
			{
				exportToTSV(table, outputFilePath);
			}
			//If the user wants to save in SPMF format
			else if("SPMF format".equals(selection))
			{
				exportToSPMFFormat(table, outputFilePath);
			}
		}
		catch (Exception e)
		{
		    JOptionPane.showMessageDialog(null,
		            "An error occured while attempting to save the file. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//Save content of JTable to the SPMF format
	private void exportToSPMFFormat(JTable table2, String outputFilePath) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

        //For each row
        for(int i=0; i< table.getRowCount(); i++)
        {
        	// Write the data in that row for each column
            for(int j=0; j < table.getColumnCount(); j++)
            {
            	//If the first column
            	if(j==0)
            	{
            		writer.write(table.getValueAt(i,j).toString()); //Write the value
            	}
            	//If not the first column
            	else 
            	{
            		//Write the column name + space
            		writer.write(columnNames.get(j));
            		writer.write(' ');
            		
            		//Then write the value
            		writer.write(table.getValueAt(i,j).toString());
            	}
            	//If not the last element on this line, we put a "," after
            	if(j != table.getColumnCount() -1)
            	{
            		writer.write(' ');
            	}
            }
            writer.newLine(); //Write the end of line
        }
        writer.close(); //Close the file
	}
	
	//Save content of JTable to tab-separated format compatible with Excel and other software
	public void exportToTSV(JTable table, String filepath) throws IOException
	{
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
        //For each column
        for(int i = 0; i < table.getColumnCount(); i++)
        {
        	//Write the column name followed by a tab
        	writer.write(table.getColumnName(i));
        	
        	//If not the last element on this line, we put a ","
        	if(i!= table.getColumnCount() -1)
        	{
        		writer.write('\t');
        	}
        }
        writer.newLine(); //Then write the end of line

        //For each row
        for(int i=0; i< table.getRowCount(); i++)
        {
        	//Write the data in that row for each column
            for(int j=0; j < table.getColumnCount(); j++)
            {
            	writer.write(table.getValueAt(i,j).toString());
            	//If not the last element on this line, we put a "," after
            	if(j != table.getColumnCount() -1)
            	{
            		writer.write('\t');
            	}
            }
            writer.newLine(); //Write the end of line
        }
        writer.close(); //Close the file
	}
	
	//Save content of JTable to comma-separated format compatible with Excel and other software
	public void exportToCSV(JTable table, String filepath) throws IOException
	{
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
        //For each column
        for(int i = 0; i < table.getColumnCount(); i++)
        {
        	//Write the column name followed by a tab
        	String string = table.getColumnName(i);
        	
        	//Ff the , character appears, we should add quotes according to CSV format
        	if(string.indexOf(',') != -1)
        	{
            	string = '\"' + string + '\"';
        	}
        	writer.write(string);
        	//If not the last element on this line, we put a ","
        	if(i!= table.getColumnCount() -1)
        	{
        		writer.write(',');
        	}
        }
        //Then write the end of line
        writer.newLine();

        //For each row
        for(int i=0; i< table.getRowCount(); i++)
        {
        	
        	//Write the data in that row for each column
            for(int j=0; j < table.getColumnCount(); j++)
            {
            	String string = table.getValueAt(i,j).toString();
            	
            	//If the , character appears, we should add quotes according to CSV format
            	if(string.indexOf(',') != -1)
            	{
                	string = '\"' + string + '\"';
            	}
            	writer.write(string);
            	
            	//If not the last element on this line, we put a "," after
            	if(j != table.getColumnCount() -1)
            	{
            		writer.write(',');
            	}
            }
            writer.newLine(); //Write the end of line
        }
        writer.close(); //Close the file
	}
	
	//This method is called when the user click on the "search" button
	protected void search()
	{
		String text = textFieldSearch.getText();
		
		//If the user did not enter any text, then we do nothing
		if(text.length() == 0)
		{
			return;
		}
		// We search from the position that is next to the current selected position in the table
		// except if nothing is selected, then we will search from the current position (0,0).
		int currentRow = table.getSelectedRow();
		int currentColumn = table.getSelectedColumn();
		currentColumn++;
		
		if(currentRow == -1)
		{
			currentRow = 0;
		}
		
		//For each row
        for(; currentRow< table.getRowCount(); currentRow++)
        {
        	//Write the data in that row for each column
            for(; currentColumn < table.getColumnCount(); currentColumn++)
            {
            	//If we have found the searched text in the current cell
            	if(table.getValueAt(currentRow, currentColumn).toString().contains(text))
            	{
            		//Select that cell
            		table.changeSelection(currentRow, currentColumn, false, false);
            		return;
            	}
            }
            currentColumn = 0;
        }
	}
}
