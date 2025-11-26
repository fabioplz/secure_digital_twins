package data;

import java.util.HashMap;
import java.util.Map;

import com.kitware.pulse.cdm.actions.SEAction;
import com.kitware.pulse.cdm.bind.Physiology.eLungCompartment;
import com.kitware.pulse.cdm.patient.actions.*;
import com.kitware.pulse.cdm.system.equipment.mechanical_ventilator.actions.*;
import com.kitware.pulse.cdm.properties.CommonUnits.VolumePerTimeUnit;

public class Action {
	/*
	 * To add a new action:
	 * - add it when creating the Action object client side
	 * - add the proper line in "generateSEAction"
	 */
	
	private String name;
	private Map<String, Double> parameters; 
	private SEAction action; 
	
	/*
	 * Constructor from Parameters
	 */
	public Action(String name, Map<String, Double> pairs) {
		
	    this.name = name;

	    //Receive a list of parameters as pairs String Double
	    //Doesn't check for duplicates cause it is completely handled client side
	    this.parameters = new HashMap<>(pairs); 
	    
	    generateSEAction(); //generate and save SEAction object
	}
	

	/*
	 * Create the SEAction object
	 */
	private void generateSEAction() {
	    switch (this.name) {
	        case "ARDS Exacerbation":
	            SEAcuteRespiratoryDistressSyndromeExacerbation ards = new SEAcuteRespiratoryDistressSyndromeExacerbation();    
	            ards.getSeverity(eLungCompartment.LeftLung).setValue(parameters.get("LeftLungSeverity"));
	            ards.getSeverity(eLungCompartment.RightLung).setValue(parameters.get("RightLungSeverity"));
	            action = ards;
	            break;

	        case "Acute Stress":
	            SEAcuteStress stress = new SEAcuteStress();
	            stress.getSeverity().setValue(parameters.get("Severity"));
	            action = stress;
	            break;

	        case "Airway Obstruction":
	            SEAirwayObstruction obstruction = new SEAirwayObstruction();
	            obstruction.getSeverity().setValue(parameters.get("Severity"));
	            action = obstruction;
	            break;

	        case "Asthma Attack":
	            SEAsthmaAttack asthma = new SEAsthmaAttack();
	            asthma.getSeverity().setValue(parameters.get("Severity"));
	            action = asthma;
	            break;

	        case "Brain Injury":
	            SEBrainInjury brainInjury = new SEBrainInjury();
	            brainInjury.getSeverity().setValue(parameters.get("Severity"));
	            action = brainInjury;
	            break;

	        case "Bronchoconstriction":
	            SEBronchoconstriction bronchoconstriction = new SEBronchoconstriction();
	            bronchoconstriction.getSeverity().setValue(parameters.get("Severity"));
	            action = bronchoconstriction;
	            break;

	        case "COPD Exacerbation":
	            SEChronicObstructivePulmonaryDiseaseExacerbation copd = new SEChronicObstructivePulmonaryDiseaseExacerbation();
	            copd.getBronchitisSeverity().setValue(parameters.get("BronchitisSeverity"));
	            copd.getEmphysemaSeverity(eLungCompartment.LeftLung).setValue(parameters.get("LeftLungEmphysemaSeverity"));
	            copd.getEmphysemaSeverity(eLungCompartment.RightLung).setValue(parameters.get("RightLungEmphysemaSeverity"));
	            action = copd;
	            break;

	        case "Dyspnea":
	            SEDyspnea dyspnea = new SEDyspnea();
	            dyspnea.getRespirationRateSeverity().setValue(parameters.get("RespirationRateSeverity"));
	            action = dyspnea;
	            break;

	        case "Exercise":
	            SEExercise exercise = new SEExercise();
	            exercise.getIntensity().setValue(parameters.get("Intensity"));
	            action = exercise;
	            break;

	        case "Pericardial Effusion":
	            SEPericardialEffusion effusion = new SEPericardialEffusion();
	            effusion.getEffusionRate().setValue(parameters.get("EffusionRate ml/s"), VolumePerTimeUnit.mL_Per_s);
	            action = effusion;
	            break;

	        case "Pneumonia Exacerbation":
	            SEPneumoniaExacerbation pneumonia = new SEPneumoniaExacerbation();
	            pneumonia.getSeverity(eLungCompartment.LeftLung).setValue(parameters.get("LeftLungSeverity"));
	            pneumonia.getSeverity(eLungCompartment.RightLung).setValue(parameters.get("RightLungSeverity"));
	            action = pneumonia;
	            break;

	        case "Pulmonary Shunt Exacerbation":
	            SEPulmonaryShuntExacerbation shunt = new SEPulmonaryShuntExacerbation();
	            shunt.getSeverity().setValue(parameters.get("Severity"));
	            action = shunt;
	            break;

	        case "Respiratory Fatigue":
	            SERespiratoryFatigue fatigue = new SERespiratoryFatigue();
	            fatigue.getSeverity().setValue(parameters.get("Severity"));
	            action = fatigue;
	            break;

	        case "Urinate":
	            SEUrinate urinate = new SEUrinate();
	            action = urinate;
	            break;

	        case "Ventilator Leak":
	            SEMechanicalVentilatorLeak leak = new SEMechanicalVentilatorLeak();
	            leak.getSeverity().setValue(parameters.get("Severity"));
	            action = leak;
	            break;
	    }
	}
	
	/*
	 * Return SEAction object
	 */
	public SEAction getAction(){
		return action;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, Double> getParameters(){
		return parameters;
	}

	public String toString(){
		return action.toString();
	}
}
