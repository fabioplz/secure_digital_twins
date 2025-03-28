package panels;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import app.App;
import data.Action;
import utils.Pair;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

public class ScenarioPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	/*
	 * Panel to create scenario
	 */
    private App app;
    
    private JComboBox<String> patientFileComboBox;
    private JTextField scenarioNameField;
    
    private DefaultTableModel tableModel;
    private JTable actionsTable;
    
    private ArrayList<Pair<Action, Integer>> actions = new ArrayList<>();
    
    public ScenarioPanel(App app) {
    	
    	this.app = app;
    	
    	//Main panel
    	this.setLayout(new GridBagLayout());
    	this.setBackground(Color.LIGHT_GRAY);
    	
        GridBagConstraints gbc = new GridBagConstraints();
        
        //PATIENT NAME SETUP
        patientFileComboBox = new JComboBox<>();
        String[] directories = {"../breathe.engine/states/", "../breathe.engine/states/exported/"};
        updatePatientFiles(directories);
        addLabelAndField("Patient:", patientFileComboBox, this, gbc, 0);
        
        //SCENARIO NAME SETUP
        scenarioNameField = new JTextField(25);
        scenarioNameField.setPreferredSize(new Dimension(300, 30)); 
        addLabelAndField("Scenario Name:", scenarioNameField, this, gbc, 1);

        
        //TABLE SETUP
        tableModel = new DefaultTableModel(new Object[]{"Action", "Time"}, 0) {

			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        actionsTable = new JTable(tableModel);
        
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel() {

			private static final long serialVersionUID = 1L;

			@Override
            public void setSelectionInterval(int index0, int index1) {
                //Can only select lines not starting with "	  ", so only action names
                String action = (String) tableModel.getValueAt(index0, 0);
                if (!action.startsWith("    ")) {
                    super.setSelectionInterval(index0, index0);
                } else {
                    clearSelection();
                }
            }
        };

        // Assign logic to table
        actionsTable.setSelectionModel(selectionModel);
        updateActionsDisplay();
        JScrollPane actionsScrollPane = new JScrollPane(actionsTable);
        actionsScrollPane.setPreferredSize(new Dimension(450, 350)); 
        addLabelAndField("", actionsScrollPane, this, gbc, 2);


        //BUTTON SETUP
        JButton createScenarioButton = new JButton("Create Scenario");;
        createScenarioButton.setBackground(new Color(0, 122, 255)); 
        createScenarioButton.setForeground(Color.WHITE);
        
        JButton removeActionButton = new JButton("Remove Selected Actions");
        removeActionButton.setBackground(new Color(255, 59, 48));
        removeActionButton.setForeground(Color.WHITE);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(removeActionButton, gbc); 
        
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(createScenarioButton, gbc);

        createScenarioButton.addActionListener(e -> {
        	createScenario();
        });

        removeActionButton.addActionListener(e -> {
        	removeAction();
        });
    }

    
    //Add visuals to panel
    private void addLabelAndField(String labelText, JComponent textField, JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }
    
    //Add action to table
    public void addAction(Action action, int seconds) {
        Pair<Action, Integer> newAction = new Pair<>(action, seconds);
        actions.add(newAction);
        actions.sort((pair1, pair2) -> pair1.getValue().compareTo(pair2.getValue()));
        updateActionsDisplay();
    }

    // get all patients from folder 
	public void updatePatientFiles(String[] directories) {
        for (String dirPath : directories) {
            File dir = new File(dirPath);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".json")) { // Pick only files .json
                            patientFileComboBox.addItem(file.getName());
                        }
                    }
                }
            }
        }
    }
	
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    //Update Table
    private void updateActionsDisplay() {
        tableModel.setRowCount(0);

        for (Pair<Action, Integer> action : actions) {
            String actionString = action.getKey().toString();
            String timeString = formatTime(action.getValue());

            String actionName = actionString.split("\n")[0];
            tableModel.addRow(new Object[]{actionName, timeString});

            String[] lines = actionString.split("\n");
            for (int i = 1; i < lines.length; i++) {
                tableModel.addRow(new Object[]{"    " + lines[i], ""});
            }
            tableModel.addRow(new Object[]{"    ", "    "});
        }
    }
    
    //Export scenario
    private void createScenario() {
        String scenarioName = scenarioNameField.getText();

        if (scenarioName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a name for the scenario.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File scenarioFile = new File("./scenario/" + scenarioName + ".json");
        if (scenarioFile.exists()) {
            int confirm = JOptionPane.showConfirmDialog(null,
                    "A file named \"" + scenarioName + ".json\" already exists. Do you want to overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String patientFile = (String) patientFileComboBox.getSelectedItem();
        File patientTempFile = new File("../breathe.engine/states/" + patientFile);
        if (patientTempFile.exists())
            patientFile = "../breathe.engine/states/" + patientFile;
        else
        	patientFile = "../breathe.engine/states/exported/" + patientFile;
        
        app.createScenario(patientFile, scenarioName, actions);
        
    }
    
    private void removeAction() {
        int[] selectedRows = actionsTable.getSelectedRows(); 

      //Check if at least one actions is selected
        if (selectedRows.length > 0) {
            StringBuilder confirmationMessage = new StringBuilder("Are you sure you want to remove the following actions?\n");

            //Check if all the actions selected are not empty and create message
            for (int row : selectedRows) {
                if (isEmptyRow(row)) {
                    JOptionPane.showMessageDialog(null, "Please select a valid action to remove.", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Pair<Action, Integer> actionToRemove = getActionFromRow(row);
                confirmationMessage.append("\n"+actionToRemove.getKey().toString()+"\nTime: "+formatTime(actionToRemove.getValue())+"\n");
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    confirmationMessage.toString(),
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
               
            	 // Remove the selected actions, iterating backward
                for (int i = selectedRows.length - 1; i >= 0; i--) { 
                    int row = selectedRows[i]; 
                    Pair<Action, Integer> actionToRemove = getActionFromRow(row);
                    removeAction(actionToRemove.getKey());
                }
                
                updateActionsDisplay(); 
                actionsTable.clearSelection();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select actions to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    
    private boolean isEmptyRow(int row) {
        Object actionValue = tableModel.getValueAt(row, 0);
        return actionValue == null || actionValue.toString().trim().isEmpty();
    }
    
    //Select whole action
    private Pair<Action, Integer> getActionFromRow(int row) {
        int rowCount = 0;

        for (Pair<Action, Integer> action : actions) {
            int lines = action.getKey().toString().split("\n").length + 1; 

            if (rowCount == row) {
                return action; 
            }
            if (rowCount + lines > row) {
                return action; 
            }
            rowCount += lines; 
        }
        return null; 
    }


    private void removeAction(Action actionToRemove) {
        actions.removeIf(pair -> pair.getKey().equals(actionToRemove));
    }
}
