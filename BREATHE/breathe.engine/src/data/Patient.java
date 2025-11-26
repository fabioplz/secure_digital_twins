package data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitware.pulse.cdm.bind.Patient.PatientData.eSex;
import com.kitware.pulse.cdm.engine.SEPatientConfiguration;
import com.kitware.pulse.cdm.patient.SEPatient;
import com.kitware.pulse.cdm.properties.CommonUnits.FrequencyUnit;
import com.kitware.pulse.cdm.properties.CommonUnits.LengthUnit;
import com.kitware.pulse.cdm.properties.CommonUnits.MassUnit;
import com.kitware.pulse.cdm.properties.CommonUnits.PowerUnit;
import com.kitware.pulse.cdm.properties.CommonUnits.PressureUnit;
import com.kitware.pulse.cdm.properties.CommonUnits.TimeUnit;

public class Patient {
	
	/*
	 * To add a new customizable parameter to patient:
	 * - add it when creating the Patient object client side
	 * - add the proper line in "generateSEPatient"
	 * - add the proper line in "loadPatientData"
	 */
	
	/*
	 * Add list of Actions and Conditions to patient?
	 */
	
	private String name; 
	private eSex sex; 
	private Map<String, Double> parameters; //Parameter name and Parameter value
	private SEPatient patient = new SEPatient(); //SEPatient object for simulation
	private SEPatientConfiguration patient_configuration;
	private List<Condition> conditions;
	
	/*
	 * Constructor from Parameters
	 */
	public Patient(String name, char sex, Map<String, Double> parameters, List<Condition> conditions) {
		
	    this.name = name;
	    this.conditions = conditions;
	    
	    //set it up already for simulation
	    if(sex == 'M') this.sex = eSex.Male; 
	    else this.sex = eSex.Female; 

	    //Receive a list of parameters as pairs String Double
	    //Doesn't check for duplicates cause it is completely handled client side
	    this.parameters = parameters; 
	    
	    generateSEPatient(); //generate and save SEPatient object
	}
	
	/*
	 * Constructor from File
	 */
	public Patient(String file) {	
		this.parameters = new HashMap<String, Double>();
		loadPatientData(file);
	}
	
	/*
	 * Create the SEPatient object
	 */
	private void generateSEPatient() {
		//create new patient
		patient_configuration = new SEPatientConfiguration();
		patient = patient_configuration.getPatient();
			
    	//SET UP DATA
		//the name sent by the client must be the same as the ones here specified
		//the values must be calculated already adapted to the unit here specified
		patient.setName(name);
		patient.getAge().setValue(parameters.get("Age"), TimeUnit.yr);
		patient.getBodyFatFraction().setValue(parameters.get("BodyFatFraction"));
		patient.getHeartRateBaseline().setValue(parameters.get("HeartRateBaseline"), FrequencyUnit.Per_min);
		patient.getDiastolicArterialPressureBaseline().setValue(parameters.get("DiastolicArterialPressureBaseline"), PressureUnit.mmHg);
		patient.getSystolicArterialPressureBaseline().setValue(parameters.get("SystolicArterialPressureBaseline"), PressureUnit.mmHg);
		patient.getRespirationRateBaseline().setValue(parameters.get("RespirationRateBaseline"), FrequencyUnit.Per_min);
		patient.getBasalMetabolicRate().setValue(parameters.get("BasalMetabolicRate"), PowerUnit.kcal_Per_day);
		patient.setSex(sex);
    	patient.getWeight().setValue(parameters.get("Weight"), MassUnit.kg);
    	patient.getHeight().setValue(parameters.get("Height"), LengthUnit.cm);
    	
    	for(Condition c : conditions) {
    		patient_configuration.getConditions().add(c.getCondition());
    	}
	}
	
	/*
	 * Get Info from File
	 */
	private void loadPatientData(String patientFilePath) {
        try { 
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(patientFilePath));

            // Retrieve patient data from the selected file
            String name = rootNode.path("InitialPatient").path("Name").asText();
            String sex = rootNode.path("InitialPatient").path("Sex").asText();
            if (sex.isBlank()) sex = "Male";
            double age = rootNode.path("InitialPatient").path("Age").path("ScalarTime").path("Value").asInt();
            double bodyFat = rootNode.path("InitialPatient").path("BodyFatFraction").path("Scalar0To1").path("Value").asDouble();
            double heartRate = rootNode.path("InitialPatient").path("HeartRateBaseline").path("ScalarFrequency").path("Value").asDouble();
            double diastolicPressure = rootNode.path("InitialPatient").path("DiastolicArterialPressureBaseline").path("ScalarPressure").path("Value").asDouble();
            double systolicPressure = rootNode.path("InitialPatient").path("SystolicArterialPressureBaseline").path("ScalarPressure").path("Value").asDouble();
            double respirationRate = rootNode.path("InitialPatient").path("RespirationRateBaseline").path("ScalarFrequency").path("Value").asDouble();
            double basalMetabolicRate = rootNode.path("InitialPatient").path("BasalMetabolicRate").path("ScalarPower").path("Value").asDouble();
            double weight = rootNode.path("InitialPatient").path("Weight").path("ScalarMass").path("Value").asDouble();
            double height = rootNode.path("InitialPatient").path("Height").path("ScalarLength").path("Value").asDouble();
            
            // Add the values to parameters
            this.name = name;
    	    if(sex.equals("Male")) this.sex = eSex.Male; 
    	    else this.sex = eSex.Female; 
    	    this.parameters.put("Age", age);
    	    this.parameters.put("BodyFatFraction", bodyFat);
    	    this.parameters.put("HeartRateBaseline", heartRate);
    	    this.parameters.put("DiastolicArterialPressureBaseline", diastolicPressure);
    	    this.parameters.put("SystolicArterialPressureBaseline", systolicPressure);
    	    this.parameters.put("RespirationRateBaseline", respirationRate);
    	    this.parameters.put("BasalMetabolicRate", basalMetabolicRate);
    	    this.parameters.put("Weight", weight);
    	    this.parameters.put("Height", height);
            

        } catch (IOException ex) {
            ex.printStackTrace();
        }	
	}
	

	/*
	 * Return SEPatient object
	 */
	public SEPatient getPatient(){
		return patient;
	}
	
	public SEPatientConfiguration getPatientConfiguration(){
		return patient_configuration;
	}
	
	public String getName() {
		return name;
	}
	
	/*
	 * Return list of conditions
	 */
	public List<Condition> getConditions(){
		return conditions;
	}
	
}
