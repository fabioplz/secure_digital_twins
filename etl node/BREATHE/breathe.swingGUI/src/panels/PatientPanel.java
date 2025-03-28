package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.App;
import data.*;

public class PatientPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	/*
	 * PANEL TO SET UP PATIENT DATA
	 */
	
	/*
	 * Fields
	 */
	private Map<String, JTextField> fieldMap = new HashMap<>();
    private JComboBox<String> sexComboBox_Patient = new JComboBox<>(new String[]{"Male", "Female"});
    private JComboBox<String> weightUnitComboBox, heightUnitComboBox;
	
	/*
	 * Inner Panels
	 */
	private JScrollPane dataPanel;
    
    public PatientPanel(App app) {
    	
    	//set up main panel
    	this.setBackground(Color.LIGHT_GRAY);
    	
    	//set up dataPanel
    	dataPanel = new JScrollPane();
        JPanel innerPanel = new JPanel(); 
        innerPanel.setLayout(new GridBagLayout());
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        innerPanel.setBackground(Color.LIGHT_GRAY);
        
        //Grid
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        //fill up map
        fieldMap.put("Name", new JTextField("Standard", 20));
        fieldMap.put("Age", new JTextField("44"));
        fieldMap.put("Weight", new JTextField("77"));
        fieldMap.put("Height", new JTextField("180"));
        fieldMap.put("BodyFatFraction", new JTextField("0.21"));
        fieldMap.put("HeartRateBaseline", new JTextField("72"));
        fieldMap.put("DiastolicArterialPressureBaseline", new JTextField("72"));
        fieldMap.put("SystolicArterialPressureBaseline", new JTextField("114"));
        fieldMap.put("RespirationRateBaseline", new JTextField("16"));
        fieldMap.put("BasalMetabolicRate", new JTextField("1600"));
        
        // Selectors for patient data
        addLabelAndField("Name:", fieldMap.get("Name"), innerPanel, gbc);
        addLabelAndField("Sex:", sexComboBox_Patient, innerPanel, gbc);
        addLabelFieldAndUnit("Age:", fieldMap.get("Age"), new JLabel("yr"), innerPanel, gbc);
        weightUnitComboBox = new JComboBox<>(new String[]{"kg", "lb"});
        addLabelFieldAndUnit("Weight:", fieldMap.get("Weight"), weightUnitComboBox, innerPanel, gbc);
        heightUnitComboBox = new JComboBox<>(new String[]{"cm", "m", "in", "ft"});
        addLabelFieldAndUnit("Height:", fieldMap.get("Height"), heightUnitComboBox, innerPanel, gbc);
        addLabelFieldAndUnit("Body Fat Fraction:", fieldMap.get("BodyFatFraction"), new JLabel("%"), innerPanel, gbc);
        addLabelFieldAndUnit("Heart Rate Baseline:", fieldMap.get("HeartRateBaseline"), new JLabel("heartbeats/min"), innerPanel, gbc);
        addLabelFieldAndUnit("Diastolic Pressure:", fieldMap.get("DiastolicArterialPressureBaseline"), new JLabel("mmHg"), innerPanel, gbc);
        addLabelFieldAndUnit("Systolic Pressure:", fieldMap.get("SystolicArterialPressureBaseline"), new JLabel("mmHg"), innerPanel, gbc);
        addLabelFieldAndUnit("Respiration Rate Baseline:", fieldMap.get("RespirationRateBaseline"), new JLabel("breaths/min"), innerPanel, gbc);
        addLabelFieldAndUnit("Basal Metabolic Rate:", fieldMap.get("BasalMetabolicRate"), new JLabel("kcal/day"), innerPanel, gbc);
        
        //Add labels
        fieldMap.get("Age").setToolTipText("Value must be between 18 and 65");
        fieldMap.get("Height").setToolTipText("Value must be between 163cm and 190cm for male patients and between 151cm and 175cm for female patients");
        fieldMap.get("BodyFatFraction").setToolTipText("Value must be between 0.02% and 0.25% for male patients and between 0.1% and 0.32% for female patients");
        fieldMap.get("HeartRateBaseline").setToolTipText("Value must be between 50bpm and 110bpm");
        fieldMap.get("DiastolicArterialPressureBaseline").setToolTipText("Value must be between 60mmHg and 80mmHg");
        fieldMap.get("SystolicArterialPressureBaseline").setToolTipText("Value must be between 90mmHg and 120mmHg");
        fieldMap.get("RespirationRateBaseline").setToolTipText("Value must be between 8bpm and 20bpm");
        
        //Add the new panel to the scrollable View
        dataPanel.setViewportView(innerPanel);
        dataPanel.setPreferredSize(new Dimension(517, 432)); 
        dataPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        dataPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        //Include everything to the main Panel
        this.add(dataPanel, BorderLayout.CENTER);
    }
    
    //method to add visual to panel
    private void addLabelAndField(String labelText, JComponent component, JPanel innerPanel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0; 
        innerPanel.add(new JLabel(labelText), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER; 
        gbc.weightx = 1.0; 
        innerPanel.add(component, gbc);
        
        gbc.gridy++;
    }
  
    //method to add visual to panel
    private void addLabelFieldAndUnit(String labelText, JComponent component, JComponent unitComponent, JPanel innerPanel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        innerPanel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        innerPanel.add(component, gbc);
        gbc.gridx = 2;
        innerPanel.add(unitComponent, gbc);
        gbc.gridy++;
    }
    
    //Generate Patient Data from text Values and Conditions
    public Patient generateInitialPatient(List<Condition> conditions) {
    	if(checkFieldsNumeric()){
        	String name = fieldMap.get("Name").getText();
        	Map<String,Double> parameters = new HashMap<>();
        	
        	char sex = 'F';
        	for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
        	    String chiave = entry.getKey();
        	    
        	    if(!chiave.equals("Name")) {
            	    Double valore = Double.parseDouble(entry.getValue().getText());
        	    	parameters.put(chiave, valore);
        	    }
        	}
        	
    		if (sexComboBox_Patient.getSelectedItem().equals("Male")) 
    		    sex = 'M';
    		
    		//methods to convert weight and height (kg and cm)
    		checkUnits(parameters);
    		
        	return new Patient(name,sex,parameters,conditions); 	
    	}else{
    		JOptionPane.showMessageDialog(null, 
    			    "One or more fields contain invalid characters.\nPlease ensure all numeric fields contain only valid numbers.", 
    			    "Invalid Input", 
    			    JOptionPane.WARNING_MESSAGE);
    		return null;
    	}
    }
    
	//Check that all values are numeric
    private boolean checkFieldsNumeric() {
        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            JTextField field = entry.getValue();

            if (key.equals("Name")) {
                continue;
            }

            String fieldValue = field.getText().replace(",", ".");
            field.setText(fieldValue);

            if (!isValidNumber(fieldValue)) {
                return false;
            }
        }
        return true;
    }
    
   //Check that a number is a valid number
    public boolean isValidNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        int commaCount = str.length() - str.replace(",", "").length();
        commaCount += str.length() - str.replace(".", "").length();
        
        if (commaCount > 1) {
            return false;
        }
        
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) {
                return false;
            }
        }

        try {
            Double.parseDouble(str.replace(",", "."));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    //load Patient Data from File
    public boolean loadPatientData(String patientFilePath) {
        try { 
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(patientFilePath));

            // Retrieve patient data from the selected file
            String name = rootNode.path("InitialPatient").path("Name").asText();
            String sex = rootNode.path("InitialPatient").path("Sex").asText();
            if (sex.isBlank()) sex = "Male";
            int age = rootNode.path("InitialPatient").path("Age").path("ScalarTime").path("Value").asInt();
            double weight = rootNode.path("InitialPatient").path("Weight").path("ScalarMass").path("Value").asDouble();
            String weightUnit = rootNode.path("InitialPatient").path("Weight").path("ScalarMass").path("Unit").asText();
            int height = rootNode.path("InitialPatient").path("Height").path("ScalarLength").path("Value").asInt();
            String heightUnit = rootNode.path("InitialPatient").path("Height").path("ScalarLength").path("Unit").asText();
            double bodyFat = rootNode.path("InitialPatient").path("BodyFatFraction").path("Scalar0To1").path("Value").asDouble();
            double heartRate = rootNode.path("InitialPatient").path("HeartRateBaseline").path("ScalarFrequency").path("Value").asDouble();
            double diastolicPressure = rootNode.path("InitialPatient").path("DiastolicArterialPressureBaseline").path("ScalarPressure").path("Value").asDouble();
            double systolicPressure = rootNode.path("InitialPatient").path("SystolicArterialPressureBaseline").path("ScalarPressure").path("Value").asDouble();
            int respirationRate = rootNode.path("InitialPatient").path("RespirationRateBaseline").path("ScalarFrequency").path("Value").asInt();
            double basalMetabolicRate = rootNode.path("InitialPatient").path("BasalMetabolicRate").path("ScalarPower").path("Value").asDouble();

            // Set the values to the appropriate fields
            fieldMap.get("Name").setText(name);
            sexComboBox_Patient.setSelectedItem(sex);
            fieldMap.get("Age").setText(String.valueOf(age));
            fieldMap.get("Weight").setText(String.format("%.2f", weight));
            weightUnitComboBox.setSelectedItem(weightUnit);
            fieldMap.get("Height").setText(String.valueOf(height));
            heightUnitComboBox.setSelectedItem(heightUnit);
            fieldMap.get("BodyFatFraction").setText(String.format("%.2f", bodyFat));
            fieldMap.get("HeartRateBaseline").setText(String.format("%.2f", heartRate));
            fieldMap.get("DiastolicArterialPressureBaseline").setText(String.format("%.2f", diastolicPressure));
            fieldMap.get("SystolicArterialPressureBaseline").setText(String.format("%.2f", systolicPressure));
            fieldMap.get("RespirationRateBaseline").setText(String.valueOf(respirationRate));
            fieldMap.get("BasalMetabolicRate").setText(String.format("%.2f", basalMetabolicRate));
            
 
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading JSON file.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
	private void checkUnits(Map<String, Double> parameters) {
		
	    // weight conversion (if necessary)
	    if(weightUnitComboBox.getSelectedItem().equals("lb")) {
	    	parameters.put("Weight", Double.parseDouble(fieldMap.get("Weight").getText()) * 0.453592); 
	    }

	    // height conversion (if necessary)
	    switch ((String) heightUnitComboBox.getSelectedItem()) {
	        case "m":
	        	parameters.put("Height", Double.parseDouble(fieldMap.get("Height").getText()) * 100); // m to cm
	            break;
	        case "in":
	        	parameters.put("Height", Double.parseDouble(fieldMap.get("Height").getText()) * 2.54); // in to cm
	            break;
	        case "ft":
	        	parameters.put("Height", Double.parseDouble(fieldMap.get("Height").getText()) * 30.48); // feat to cm
	            break;
	    }
	}

	public String getPatientName() {
		return fieldMap.get("Name").getText();
	}
	
	//Enable/disable during simulation
	public void enableComponents(boolean enabled) {
	    for (JTextField field : fieldMap.values()) {
	        field.setEnabled(enabled);
	    }

	    sexComboBox_Patient.setEnabled(enabled);
	    if (weightUnitComboBox != null) {
	        weightUnitComboBox.setEnabled(enabled);
	    }
	    if (heightUnitComboBox != null) {
	        heightUnitComboBox.setEnabled(enabled);
	    }
	}
	
}
