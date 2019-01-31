package ca.pfv.spmf.gui.patternvizualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ca.pfv.spmf.gui.patternvizualizer.filters.AbstractFilter;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterEqualBoolean;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterEqualDouble;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterEqualInteger;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterGreaterThanDouble;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterGreaterThanInteger;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterLessThanDouble;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterLessThanInteger;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterStringContains;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterStringNotContains;

//Windows for creating filter
public class FilterSelectionWindow extends JDialog
{

	//The text field for entering a value for the current filter
	private JTextField textField;

	//List of table column names
	Vector<String> columnNames = null;

	//List of table column classes (Integer, Double, String)
	Vector<Class> columnClasses = null;

	//List of current filters
	PatternTableRowFilters rowfilters;

	//The list of available filters for showing in the combo box
	List<Class> listComboBoxFilters = new ArrayList<Class>();

	//Combo box to let the user choose a filter
	private JComboBox comboBoxFilters;

	//Combo box for letting the user choose a column
	private JComboBox comboBoxColumns; 
	
	//The pattern vizualizer window that has created that filter section window
	private PatternVizualizer patternVisualizer;
	
	//Constructor of the filter selection window
	public FilterSelectionWindow(Vector<String> columnNames,
			Vector<Class> columnClasses, PatternTableRowFilters rowFilters, PatternVizualizer patternVisualizer)
	{
		super(patternVisualizer); //Use patternVisualizer class

		//Save the variables
		this.columnNames = columnNames;
		this.columnClasses = columnClasses;
		this.rowfilters = rowFilters;
		this.patternVisualizer = patternVisualizer;
		
		setVisible(true); //Set this window as visible
		setModal(true); //Show this window as modal
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Add a filter..."); //Set title and layout
		getContentPane().setLayout(null);

		//Create the Cancel button
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				setVisible(false);
				dispose();
			}
		});
		btnCancel.setBounds(500, 143, 89, 23);
		getContentPane().add(btnCancel);

		//Create the Add Filter button
		JButton btnAddFilter = new JButton("Add filter");
		btnAddFilter.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addFilter();
			}
		});
		btnAddFilter.setBounds(401, 143, 89, 23);
		getContentPane().add(btnAddFilter);
		btnAddFilter.setEnabled(false);


		//Create the labels
		JLabel lblSelectAColumn = new JLabel("Choose a column:");
		lblSelectAColumn.setBounds(10, 22, 258, 14);
		getContentPane().add(lblSelectAColumn);

		JLabel lblSelectAConstraint = new JLabel("Choose a constraint:");
		lblSelectAConstraint.setBounds(10, 74, 149, 14);
		getContentPane().add(lblSelectAConstraint);

		//Create the combo boxes for filters
		comboBoxFilters = new JComboBox();
		comboBoxFilters.setBounds(68, 99, 323, 23);
		getContentPane().add(comboBoxFilters);

		//Create the combo boxes for columns
		comboBoxColumns = new JComboBox(columnNames);
		comboBoxColumns.setBounds(68, 42, 200, 23);
		getContentPane().add(comboBoxColumns);

		//Add a listener to detect when the user select a column
		comboBoxColumns.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent evt)
			{
				//Refresh the combo box of filters
				if (evt.getStateChange() == ItemEvent.SELECTED)
				{
					refreshComboBoxFilters(columnNames, columnClasses,
							comboBoxFilters, comboBoxColumns);
				}
				else
				{
					//Do nothing
				}
			}
		});

		// Fill the combo box of filters  for the first time
		refreshComboBoxFilters(columnNames, columnClasses, comboBoxFilters, comboBoxColumns);

		//Create the text field to enter the value for the filter
		textField = new JTextField();
		DocumentListener documentListener = new DocumentListener()
		{
			public void changedUpdate(DocumentEvent documentEvent)
			{
				printIt(documentEvent);
			}

			public void insertUpdate(DocumentEvent documentEvent)
			{
				printIt(documentEvent);
			}

			public void removeUpdate(DocumentEvent documentEvent)
			{
				printIt(documentEvent);
			}

			private void printIt(DocumentEvent documentEvent)
			{
				// When the user type in the jtext field, if the text is not empty we will show the button "add filter" as enabled.
				if (textField.getText().isEmpty() == false)
				{
					btnAddFilter.setEnabled(true);
				}
				//Otherwise, the button is disabled
				else
				{
					btnAddFilter.setEnabled(false);
				}
			}
		};
		textField.getDocument().addDocumentListener(documentListener);
		textField.setBounds(401, 100, 200, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		textField.setEnabled(true);

		
		setSize(632, 228); //Set the size of the filter selection window
	} //End of the constructor
	
	//Add filter function
	private void addFilter()
	{
		//Get the class corresponding to the chosen filter
		Class selectedFilterClass = listComboBoxFilters.get(comboBoxFilters.getSelectedIndex());
		
		//Get the value to be used for the filter
		String valueString = textField.getText();
		
		//Get the  index of the column to be used by the filter
		int columnID = comboBoxColumns.getSelectedIndex();
		
		//Get the column  to be used by the filter
		String columnName = (String)comboBoxColumns.getSelectedItem();
		
		// Create the filter
		try
		{
			AbstractFilter filter = null;
			if(selectedFilterClass.equals(FilterEqualBoolean.class))
			{
				boolean value = Boolean.parseBoolean(valueString);
				filter = new FilterEqualBoolean(value, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterEqualDouble.class))
			{
				double value = Double.parseDouble(valueString);
				filter = new FilterEqualDouble(value, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterEqualInteger.class))
			{
				int value = Integer.parseInt(valueString);
				filter = new FilterEqualInteger(value, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterGreaterThanDouble.class))
			{
				double value = Double.parseDouble(valueString);
				filter = new FilterGreaterThanDouble(value, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterGreaterThanInteger.class))
			{
				int value = Integer.parseInt(valueString);
				filter = new FilterGreaterThanInteger(value, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterLessThanDouble.class))
			{
				double value = Double.parseDouble(valueString);
				filter = new FilterLessThanDouble(value, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterLessThanInteger.class))
			{
				int value = Integer.parseInt(valueString);
				filter = new FilterLessThanInteger(value, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterStringContains.class))
			{
				filter = new FilterStringContains(valueString, columnName, columnID);
			}
			else if (selectedFilterClass.equals(FilterStringNotContains.class))
			{
				filter = new FilterStringNotContains(valueString, columnName, columnID);
			}
			rowfilters.filters.add(filter); //Add the filter to the list of current filters
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Invalid value in text field. ERROR MESSAGE = " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);    
			return;
		}
		setVisible(false); // hide this window
		
		/*Notify the pattern vizualizer window that the filters have been updated
		so that the table can be refreshed using the new filters*/
		patternVisualizer.filtersHaveBeenUpdated();
	}
	
	//Fill the list of available filters for the chosen column in the combo box for filters
	private void refreshComboBoxFilters(Vector<String> columnNames, Vector<Class> columnClasses, JComboBox comboBoxFilters, JComboBox comboBoxColumns)	
	{
		//Get the selected column
		String selectedColumnName = (String) comboBoxColumns.getSelectedItem();

		//Clear comboBox filters
		comboBoxFilters.removeAllItems();
		listComboBoxFilters.clear();

		//Find the class of the chosen column
		Class selectedColumnClass = null;
		for (int i = 0; i < columnNames.size(); i++)
		{
			if (columnNames.get(i).equals(selectedColumnName))
			{
				selectedColumnClass = columnClasses.get(i);
				break;
			}
		}
		
		//If the selected column is of type integer, then make available the filters for integer values in the combo box.
		if (selectedColumnClass.equals(Integer.class))
		{
			listComboBoxFilters.add(FilterEqualInteger.class);
			comboBoxFilters.addItem(FilterEqualInteger.getFilterGenericName());
			listComboBoxFilters.add(FilterGreaterThanInteger.class);
			comboBoxFilters.addItem(FilterGreaterThanInteger.getFilterGenericName());
			listComboBoxFilters.add(FilterLessThanInteger.class);
			comboBoxFilters.addItem(FilterLessThanInteger.getFilterGenericName());
		}
		//If the selected column is of type double, then make available the filters for doule values in the combo box.
		else if (selectedColumnClass.equals(Double.class))
		{
			listComboBoxFilters.add(FilterEqualDouble.class);
			comboBoxFilters.addItem(FilterEqualDouble.getFilterGenericName());
			listComboBoxFilters.add(FilterGreaterThanDouble.class);
			comboBoxFilters.addItem(FilterGreaterThanDouble.getFilterGenericName());
			listComboBoxFilters.add(FilterLessThanDouble.class);
			comboBoxFilters.addItem(FilterLessThanDouble.getFilterGenericName());
		}
		//If the selected column is of type string, then make available the filters for string values in the combo box.
		else if (selectedColumnClass.equals(String.class))
		{
			listComboBoxFilters.add(FilterStringContains.class);
			comboBoxFilters.addItem(FilterStringContains.getFilterGenericName());
			listComboBoxFilters.add(FilterStringNotContains.class);
			comboBoxFilters.addItem(FilterStringNotContains.getFilterGenericName());
		}
		//If the selected column is of type boolean, then make available the filters for boolean values in the combo box.
		else if (selectedColumnClass.equals(Boolean.class))
		{
			listComboBoxFilters.add(FilterEqualBoolean.class);
			comboBoxFilters.addItem(FilterEqualBoolean.getFilterGenericName());
		}
	}
}
