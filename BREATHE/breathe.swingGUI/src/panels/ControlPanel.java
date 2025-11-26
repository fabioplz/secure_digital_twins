package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import app.App;

public class ControlPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	/*
	 * BUTTONS TO CONTROL THE SIMULATION
	 */

    JButton startFromFileButton,startFromScenarioButton,startButton,stopButton,exportButton;
 
    App app;

    public ControlPanel(App app) {
    	this.app = app;

    	//set up main panel
    	this.setBackground(Color.LIGHT_GRAY);
    	this.setPreferredSize(new Dimension(550, 60));
    	this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
    	this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder())); 


        Dimension buttonSize = new Dimension(150, 30); 

        //START FROM FILE BUTTON
        startFromFileButton = new JButton("Start From File");
        startFromFileButton.setToolTipText("Start Simulation from Patient File");
        startFromFileButton.setPreferredSize(buttonSize);
        startFromFileButton.setMaximumSize(buttonSize);
        startFromFileButton.setBackground(new Color(0, 122, 255));
        startFromFileButton.setForeground(Color.WHITE);
        startFromFileButton.setFocusPainted(false);
        
        startFromFileButton.addActionListener(e -> {
        	startingFileSimulation();
        });

        //START FROM SCENARIO BUTTON
        startFromScenarioButton = new JButton("Start From Scenario");
        startFromScenarioButton.setToolTipText("Start a Scenario");
        startFromScenarioButton.setPreferredSize(buttonSize);
        startFromScenarioButton.setMaximumSize(buttonSize);
        startFromScenarioButton.setBackground(new Color(0, 122, 255));
        startFromScenarioButton.setForeground(Color.WHITE);
        startFromScenarioButton.setFocusPainted(false);
        
        startFromScenarioButton.addActionListener(e -> {
        	startingScenarioSimulation();
        });

        //START SIMULATION BUTTON
        startButton = new JButton("Start Simulation");
        startButton.setToolTipText("Start new Simulation");
        startButton.setPreferredSize(buttonSize);
        startButton.setMaximumSize(buttonSize);
        startButton.setBackground(new Color(0, 122, 255));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        
        startButton.addActionListener(e -> {
        	startingStandardSimulation();
        });
        
        //STOP SIMULATION BUTTON
        stopButton = new JButton("Stop Simulation");
        stopButton.setToolTipText("Stop Simulation");
        stopButton.setPreferredSize(buttonSize);
        stopButton.setMaximumSize(buttonSize);
        stopButton.setEnabled(false);
        stopButton.setBackground(new Color(255, 59, 48));
        stopButton.setForeground(Color.WHITE);
        stopButton.setVisible(false);
        stopButton.setFocusPainted(false);
        
        stopButton.addActionListener(e -> {
        	app.stopSimulation();
        	app.resetVentilatorsButton();
        	enableControlStartButton(true);
        	showControlStartButton(true);
        });

        //EXPORT BUTTON
        exportButton = new JButton("Export Simulation");
        exportButton.setToolTipText("Export current patient state");
        exportButton.setPreferredSize(buttonSize);
        exportButton.setMaximumSize(buttonSize);
        exportButton.setEnabled(false);
        exportButton.setBackground(new Color(0, 128, 0));
        exportButton.setForeground(Color.WHITE);
        exportButton.setVisible(false);
        exportButton.setFocusPainted(false);
        
        exportButton.addActionListener(e -> {
            String defaultFileName = "../breathe.engine/states/exported/" + app.getPatientName() + ".json";
            JFileChooser fileChooser = new JFileChooser("../breathe.engine/states/exported/");
            fileChooser.setDialogTitle("Export simulation");
            fileChooser.setSelectedFile(new File(defaultFileName)); // Pre-set default filename
            fileChooser.setApproveButtonText("Export");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            boolean validFileName = false; // Flag to track valid filename

            while (!validFileName) {
                int userSelection = fileChooser.showSaveDialog(null);
                
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    String fileName = fileToSave.getAbsolutePath();
                    
                    // Ensure the file ends with .json
                    if (!fileName.endsWith(".json")) {
                        fileName += ".json";
                    }
                    
                    File file = new File(fileName);
                    
                    // Check if file exists and ask for overwrite confirmation
                    if (file.exists()) {
                        int response = JOptionPane.showConfirmDialog(null, 
                            "File already exists. Do you want to overwrite it?", 
                            "Overwrite Confirmation", 
                            JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                        
                        if (response == JOptionPane.YES_OPTION) {
                            app.exportSimulation(fileName);
                            validFileName = true; // Exit loop
                        }
                    } else {
                    	app.exportSimulation(fileName);
                        validFileName = true; 
                    }
                    
                } else {
                    // User cancelled the operation
                    validFileName = true; 
                }
            }
        });
        
        //Add buttons to buttonPanel
        this.add(startFromScenarioButton);
        this.add(stopButton); 
        this.add(startFromFileButton);
        this.add(startButton);
        this.add(exportButton);
    }
    

	//start simulation
    private void startingStandardSimulation() {
    	clearOutputDisplay();
    	app.startSimulation();
	}

    //start from file
    private void startingFileSimulation() {
    	clearOutputDisplay();
    	JFileChooser fileChooser = new JFileChooser("../breathe.engine/states");
        int returnValue = fileChooser.showOpenDialog(null); // pick a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String patientFilePath = fileChooser.getSelectedFile().getAbsolutePath();   
            
            enableControlStartButton(false);

            if(app.loadPatientData(patientFilePath)) 
            	app.startFromFileSimulation(patientFilePath);
        }
    }   


	//start from scenario simulation
    private void startingScenarioSimulation() {
    	clearOutputDisplay();
    	JFileChooser fileChooser = new JFileChooser("../breathe.engine/scenario/exported");
        int returnValue = fileChooser.showOpenDialog(null); // pick a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String scenarioFilePath = fileChooser.getSelectedFile().getAbsolutePath();
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode_scenario = mapper.readTree(new File(scenarioFilePath));
                String PatientFilePath = rootNode_scenario.path("EngineStateFile").asText();

                if(new File(PatientFilePath).exists() && app.loadPatientData(PatientFilePath)) {
                	enableControlStartButton(false);
                	app.startFromScenarioSimulation(scenarioFilePath);
                }
                else
                	app.minilogStringData("\nPlease upload a valid scenario file.");
                
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error loading scenario JSON file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
	}
    
    
    //From Start to Stop
    public void enableControlStartButton(boolean enable) {
        startButton.setEnabled(enable); 
        startFromFileButton.setEnabled(enable); 
        startFromScenarioButton.setEnabled(enable);
        stopButton.setEnabled(!enable);
        exportButton.setEnabled(!enable);
    }
    
    //From Start to Stop
    public void showControlStartButton(boolean enable) {
        startButton.setVisible(enable); 
        startFromFileButton.setVisible(enable); 
        startFromScenarioButton.setVisible(enable);
        stopButton.setVisible(!enable);
        exportButton.setVisible(!enable);
    }
    
    //Clean outputs graphs
	private void clearOutputDisplay() {
		app.clearOutputDisplay();
	}
	

}