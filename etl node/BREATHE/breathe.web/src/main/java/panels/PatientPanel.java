package panels;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;

import app.App;
import data.Condition;
import data.Patient;

import com.vaadin.flow.component.html.Div;

public class PatientPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	/*
	 * PANEL TO SET UP PATIENT DATA
	 */
	
	/*
	 * Fields
	 */
	private TextField nameField;
	private ComboBox<String> sexComboBox, weightUnitComboBox, heightUnitComboBox;
	
	/*
	 * Inner Panels
	 */
	private NumberField ageField, heightField, weightField, bodyFatField,heartRateField, diastolicPressureField, systolicPressureField, respirationRateField, basalMetabolicRateField;
	private Map<String, NumberField> fieldMap = new HashMap<>();

	public PatientPanel(App app) {
		getStyle().set("margin", "0px");
		getStyle().set("padding", "2px");

		setSpacing(false);

		Div fixedSizeDiv = new Div();
		fixedSizeDiv.getStyle().set("box-sizing", "border-box"); 

    	//set up dataPanel
		FormLayout formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1)); 

		/*
		 * All Fields
		 */
		nameField = new TextField("Name");
		nameField.setValue("Standard");

		ageField = new NumberField("Age");
		ageField.setValue(44.0);
		ageField.setMin(18);
		ageField.setMax(65);
		ageField.setStepButtonsVisible(true);
		ageField.setWidth("10vw");
		fieldMap.put("Age", ageField);
		ageField.setTooltipText(
				"Value must be between 18 and 65");

		weightField = new NumberField("Weight");
		weightField.setValue(77.0);
		weightField.setStepButtonsVisible(true);
		weightField.setWidth("10vw");
		fieldMap.put("Weight", weightField);
		weightField.setTooltipText(
				"No upper and lower limit");

		heightField = new NumberField("Height");
		heightField.setValue(180.0);
		heightField.setStepButtonsVisible(true);
		heightField.setWidth("10vw");
		fieldMap.put("Height", heightField);
		heightField.setTooltipText(
				"Value must be between 163cm and 190cm for male patients and between 151cm and 175cm for female patients");

		bodyFatField = new NumberField("Body Fat Fraction");
		bodyFatField.setValue(0.21);
		bodyFatField.setStepButtonsVisible(true);
		bodyFatField.setWidth("10vw");
		fieldMap.put("BodyFatFraction", bodyFatField);
		bodyFatField.setTooltipText(
				"Value must be between 0.02% and 0.25% for male patients and between 0.1% and 0.32% for female patients");
		
		heartRateField = new NumberField("Heart Rate Baseline");
		heartRateField.setValue(72.0);
		heartRateField.setMin(50);
		heartRateField.setMax(110);
		heartRateField.setWidth("10vw");
		heartRateField.setStepButtonsVisible(true);
		fieldMap.put("HeartRateBaseline", heartRateField);
		heartRateField.setTooltipText(
				"Value must be between 50bpm and 110bpm");

		diastolicPressureField = new NumberField("Diastolic Pressure");
		diastolicPressureField.setValue(72.0);
		diastolicPressureField.setMin(60);
		diastolicPressureField.setMax(80);
		diastolicPressureField.setWidth("10vw");
		diastolicPressureField.setStepButtonsVisible(true);
		fieldMap.put("DiastolicArterialPressureBaseline", diastolicPressureField);
		diastolicPressureField.setTooltipText(
				"Value must be between 60mmHg and 80mmHg");

		systolicPressureField = new NumberField("Systolic Pressure");
		systolicPressureField.setValue(114.0);
		systolicPressureField.setMin(90);
		systolicPressureField.setMax(120);
		systolicPressureField.setWidth("10vw");
		systolicPressureField.setStepButtonsVisible(true);
		fieldMap.put("SystolicArterialPressureBaseline", systolicPressureField);
		systolicPressureField.setTooltipText(
				"Value must be between 90mmHg and 120mmHg");
		
		respirationRateField = new NumberField("Respiration Rate Baseline");
		respirationRateField.setValue(16.0);
		respirationRateField.setMin(8);
		respirationRateField.setMax(20);
		respirationRateField.setWidth("10vw");
		respirationRateField.setStepButtonsVisible(true);
		fieldMap.put("RespirationRateBaseline", respirationRateField);
		respirationRateField.setTooltipText(
				"Value must be between 8bpm and 20bpm");

		basalMetabolicRateField = new NumberField("Basal Metabolic Rate");
		basalMetabolicRateField.setValue(1600.0);
		basalMetabolicRateField.setStepButtonsVisible(true);
		basalMetabolicRateField.setWidth("10vw");
		fieldMap.put("BasalMetabolicRate", basalMetabolicRateField);
		basalMetabolicRateField.setTooltipText(
				"No upper and lower limit");

		sexComboBox = new ComboBox<>("Sex");
		sexComboBox.setItems("Male", "Female");
		sexComboBox.setValue("Male");
		sexComboBox.setWidth("10vw");
		sexComboBox.addValueChangeListener(event -> updateFieldRange());

		weightUnitComboBox = new ComboBox<>("   ");
		weightUnitComboBox.setItems("kg", "lb");
		weightUnitComboBox.setValue("kg");
		weightUnitComboBox.setWidth("5vw");
		weightUnitComboBox.addValueChangeListener(event -> updateFieldRange());
		
		heightUnitComboBox = new ComboBox<>("   ");
		heightUnitComboBox.setItems("cm", "m", "in", "ft");
		heightUnitComboBox.setValue("cm");
		heightUnitComboBox.setWidth("5vw");
		heightUnitComboBox.addValueChangeListener(event -> updateFieldRange());

		//Adding fields to panel
		formLayout.add(nameField);
		HorizontalLayout user = new HorizontalLayout(ageField, sexComboBox);
		formLayout.add(user);
		HorizontalLayout weight = new HorizontalLayout(weightField, weightUnitComboBox);
		weight.setAlignItems(Alignment.BASELINE);
		formLayout.add(weight);
		HorizontalLayout height = new HorizontalLayout(heightField, heightUnitComboBox);
		height.setAlignItems(Alignment.BASELINE);
		formLayout.add(height);
		formLayout.add(bodyFatField);
		formLayout.add(heartRateField);
		formLayout.add(diastolicPressureField);
		formLayout.add(systolicPressureField);
		formLayout.add(respirationRateField);
		formLayout.add(basalMetabolicRateField);

		Div scrollableDiv = new Div();
		scrollableDiv.getStyle().set("overflow-y", "auto");
		scrollableDiv.getStyle().set("scrollbar-width", "none");
		scrollableDiv.getStyle().set("border-bottom", "2px solid #ccc"); 

		scrollableDiv.setHeight("70vh"); 
		scrollableDiv.add(formLayout);

		fixedSizeDiv.add(scrollableDiv);
		fixedSizeDiv.setHeight("70vh");
		
		add(fixedSizeDiv);

	}


	// Convert the value to kg and cm
	private void checkUnits(Map<String, Double> parameters) {

		if (weightUnitComboBox.getValue().equals("lb")) {
			parameters.put("Weight", fieldMap.get("Weight").getValue() * 0.453592);
		}

		switch ((String) heightUnitComboBox.getValue()) {
		case "m":
			parameters.put("Height", fieldMap.get("Height").getValue() * 100); 
			break;
		case "in":
			parameters.put("Height", fieldMap.get("Height").getValue() * 2.54); 
			break;
		case "ft":
			parameters.put("Height", fieldMap.get("Height").getValue() * 30.48);
			break;
		}
	}
	
	//adapt field range depending on unit
	private void updateFieldRange() {
	    String selectedUnit = heightUnitComboBox.getValue();
	    String selectedSex = sexComboBox.getValue();

	    double minHeight, maxHeight;
	    if ("Male".equals(selectedSex)) {
	        switch (selectedUnit) {
	            case "cm":
	                minHeight = 163.0;
	                maxHeight = 190.0;
	                break;
	            case "m":
	                minHeight = 1.63;
	                maxHeight = 1.90;
	                break;
	            case "in":
	                minHeight = 64.2; // 163 cm to inches
	                maxHeight = 74.8; // 190 cm to inches
	                break;
	            case "ft":
	                minHeight = 5.35; // 163 cm to feet
	                maxHeight = 6.23; // 190 cm to feet
	                break;
	            default:
	                minHeight = 0;
	                maxHeight = 0;
	                break;
	        }
	    } else {
	        switch (selectedUnit) {
	            case "cm":
	                minHeight = 151.0;
	                maxHeight = 175.0;
	                break;
	            case "m":
	                minHeight = 1.51;
	                maxHeight = 1.75;
	                break;
	            case "in":
	                minHeight = 59.4; // 151 cm to inches
	                maxHeight = 68.9; // 175 cm to inches
	                break;
	            case "ft":
	                minHeight = 4.95; // 151 cm to feet
	                maxHeight = 5.74; // 175 cm to feet
	                break;
	            default:
	                minHeight = 0;
	                maxHeight = 0;
	                break;
	        }
	    }
	    heightField.setMin(minHeight);
	    heightField.setMax(maxHeight);
	    
	    if ("Male".equals(selectedSex)) {
	        bodyFatField.setMin(0.02);
	        bodyFatField.setMax(0.25);
	    } else {
	        bodyFatField.setMin(0.1);
	        bodyFatField.setMax(0.32);
	    }
	    
		for (Map.Entry<String, NumberField> entry : fieldMap.entrySet()) {
			if (!entry.getKey().equals("Name")) {
				NumberField numberField = (NumberField) entry.getValue();
				Double value = numberField.getValue();
				Double minValue = numberField.getMin();
				Double maxValue = numberField.getMax();
				if (value < minValue || value > maxValue) {
					numberField.setInvalid(true);
					numberField.setErrorMessage("Value must be between " + minValue + " and " + maxValue);
				}
				else
					numberField.setInvalid(false);
			}
		}
	}

	public void enableComponents(boolean enabled) {
		nameField.setEnabled(enabled);

		sexComboBox.setEnabled(enabled);
		weightUnitComboBox.setEnabled(enabled);
		heightUnitComboBox.setEnabled(enabled);

		for (NumberField field : fieldMap.values()) {
			field.setEnabled(enabled);
		}
	}

	public String getPatientName() {
		return nameField.getValue();
	}
	
    //Generate Patient Data from text Values and Conditions
	public Patient generateInitialPatient(List<Condition> conditions) {
		String name = nameField.getValue();
		Map<String, Double> parameters = new HashMap<>();

		char sex = 'F';
		for (Map.Entry<String, NumberField> entry : fieldMap.entrySet()) {

			if (!entry.getKey().equals("Name")) {
				NumberField numberField = (NumberField) entry.getValue();
				Double value = numberField.getValue();
				Double minValue = numberField.getMin();
				Double maxValue = numberField.getMax();

				if (value < minValue || value > maxValue) {
					numberField.setInvalid(true);
					numberField.setErrorMessage("Value must be between " + minValue + " and " + maxValue);
					return null;
				}

				numberField.setInvalid(false);
				parameters.put(entry.getKey(), value);
			}
		}

		if (sexComboBox.getValue().equals("Male"))
			sex = 'M';

		checkUnits(parameters);

		return new Patient(name, sex, parameters, conditions);
	}

	//take patient data from file (start from file/scenario)
    public boolean loadPatientData(String patientFilePath) {
        try { 
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(patientFilePath));

            // Retrieve patient data from the selected file
            String name = rootNode.path("InitialPatient").path("Name").asText();
            String sex = rootNode.path("InitialPatient").path("Sex").asText();
            if (sex.isBlank()) sex = "Male";
            // Use Math.round to control decimal places directly
            double age =rootNode.path("InitialPatient").path("Age").path("ScalarTime").path("Value").asDouble();
            double weight = rootNode.path("InitialPatient").path("Weight").path("ScalarMass").path("Value").asDouble(); // 2 decimal places
            String weightUnit = rootNode.path("InitialPatient").path("Weight").path("ScalarMass").path("Unit").asText();
            double height =rootNode.path("InitialPatient").path("Height").path("ScalarLength").path("Value").asDouble();
            String heightUnit = rootNode.path("InitialPatient").path("Height").path("ScalarLength").path("Unit").asText();
            double bodyFat = rootNode.path("InitialPatient").path("BodyFatFraction").path("Scalar0To1").path("Value").asDouble(); // 2 decimal places
            double heartRate = rootNode.path("InitialPatient").path("HeartRateBaseline").path("ScalarFrequency").path("Value").asDouble();
            double diastolicPressure = rootNode.path("InitialPatient").path("DiastolicArterialPressureBaseline").path("ScalarPressure").path("Value").asDouble();
            double systolicPressure = rootNode.path("InitialPatient").path("SystolicArterialPressureBaseline").path("ScalarPressure").path("Value").asDouble();
            double respirationRate = rootNode.path("InitialPatient").path("RespirationRateBaseline").path("ScalarFrequency").path("Value").asDouble();
            double basalMetabolicRate = rootNode.path("InitialPatient").path("BasalMetabolicRate").path("ScalarPower").path("Value").asDouble();

            // Set the values to the appropriate fields
            nameField.setValue(name);
            sexComboBox.setValue(sex);
            fieldMap.get("Age").setValue(age);
            fieldMap.get("Weight").setValue(weight);
            weightUnitComboBox.setValue(weightUnit);
            fieldMap.get("Height").setValue(height);
            heightUnitComboBox.setValue(heightUnit);
            fieldMap.get("BodyFatFraction").setValue(bodyFat);
            fieldMap.get("HeartRateBaseline").setValue(heartRate);
            fieldMap.get("DiastolicArterialPressureBaseline").setValue(diastolicPressure);
            fieldMap.get("SystolicArterialPressureBaseline").setValue(systolicPressure);
            fieldMap.get("RespirationRateBaseline").setValue(respirationRate);
            fieldMap.get("BasalMetabolicRate").setValue(basalMetabolicRate);
 
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    

}
