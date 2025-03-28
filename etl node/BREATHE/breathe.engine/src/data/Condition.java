package data;

import java.util.HashMap;
import java.util.Map;

import com.kitware.pulse.cdm.bind.Physiology.eLungCompartment;
import com.kitware.pulse.cdm.conditions.SECondition;
import com.kitware.pulse.cdm.patient.conditions.*;
import com.kitware.pulse.cdm.properties.CommonUnits.VolumeUnit;

public class Condition {
	
	/*
	 * To add a new condition:
	 * - add it when creating the Condition object client side
	 * - add the proper line in "generateSECondition"
	 */
	
	private String name;
	private Map<String, Double> parameters = new HashMap<>(); 
	private SECondition condition; 
	
	/*
	 * Constructor from Parameters
	 */
	public Condition(String name, Map<String, Double> pairs) {
		
	    this.name = name;

	    //Receive a list of parameters as pairs String Double
	    //Doesn't check for duplicates cause it is completely handled client side
	    this.parameters = new HashMap<>(pairs); 
	    
	    generateSECondition(); //generate and save SECondition object
	}
	
	public Condition(SECondition condition) {
		this.condition = condition;
		extractSECondition();
	}
	
	/*
	 * Create the SECondition object
	 */
	private void generateSECondition() {

		switch (this.name) {
		    case "Chronic Anemia":
		        SEChronicAnemia condition = new SEChronicAnemia();	                        
		        condition.getReductionFactor().setValue(parameters.get("ReductionFactor"));	 
		        this.condition = condition;
		        break;
		        
		    case "ARDS":
		        SEAcuteRespiratoryDistressSyndrome ARDS = new SEAcuteRespiratoryDistressSyndrome();   
		        ARDS.getSeverity(eLungCompartment.LeftLung).setValue(parameters.get("LeftLungSeverity"));
		        ARDS.getSeverity(eLungCompartment.RightLung).setValue(parameters.get("RightLungSeverity"));	                        
		        this.condition = ARDS;
		        break;
		        
		    case "COPD":
		        SEChronicObstructivePulmonaryDisease COPD = new SEChronicObstructivePulmonaryDisease();
		        COPD.getBronchitisSeverity().setValue(parameters.get("BronchitisSeverity"));
		        COPD.getEmphysemaSeverity(eLungCompartment.LeftLung).setValue(parameters.get("LeftLungEmphysemaSeverity"));
		        COPD.getEmphysemaSeverity(eLungCompartment.RightLung).setValue(parameters.get("RightLungEmphysemaSeverity"));                        
		        this.condition = COPD;
		        break;   
		        
		    case "Pericardial Effusion":
		        SEChronicPericardialEffusion CPE = new SEChronicPericardialEffusion();	                        
		        CPE.getAccumulatedVolume().setValue(parameters.get("AccumulatedVolume"), VolumeUnit.mL);                        
		        this.condition = CPE;
		        break; 
		        
		    case "Renal Stenosis":
		        SEChronicRenalStenosis Stenosis = new SEChronicRenalStenosis();   
		        Stenosis.getLeftKidneySeverity().setValue(parameters.get("LeftKidneySeverity"));
		        Stenosis.getRightKidneySeverity().setValue(parameters.get("RightKidneySeverity"));                  
		        this.condition = Stenosis;
		        break;	  
		        
		    case "Chronic Ventricular Systolic Disfunction":
		        SEChronicVentricularSystolicDysfunction VSD = new SEChronicVentricularSystolicDysfunction();   
		        this.condition = VSD;
		        break;		      	  
	
		    case "Pneumonia":
		        SEPneumonia Pneumonia = new SEPneumonia();   
		        Pneumonia.getSeverity(eLungCompartment.LeftLung).setValue(parameters.get("LeftLungSeverity"));
		        Pneumonia.getSeverity(eLungCompartment.RightLung).setValue(parameters.get("RightLungSeverity"));                        
		        this.condition = Pneumonia;
		        break;
		        
		    case "Pulmonary Fibrosis":
		        SEPulmonaryFibrosis fibrosis = new SEPulmonaryFibrosis();	                        
		        fibrosis.getSeverity().setValue(parameters.get("Severity"));	                        
		        this.condition = fibrosis;
		        break;
		        
		    case "Pulmonary Shunt":
		        SEPulmonaryShunt shunt = new SEPulmonaryShunt();	                        
		        shunt.getSeverity().setValue(parameters.get("Severity"));                      
		        this.condition = shunt;
		        break;	                        
        }
	}
	
	/*
	 * extract from SECondition object
	 */
	private void extractSECondition() {

	    if (this.condition instanceof SEChronicAnemia) {
	        this.name = "Chronic Anemia";
	        SEChronicAnemia condition = (SEChronicAnemia) this.condition;
	        parameters.put("ReductionFactor", condition.getReductionFactor().getValue());

	    } else if (this.condition instanceof SEAcuteRespiratoryDistressSyndrome) {
	        this.name = "ARDS";
	        SEAcuteRespiratoryDistressSyndrome ARDS = (SEAcuteRespiratoryDistressSyndrome) this.condition;
	        parameters.put("LeftLungSeverity", ARDS.getSeverity(eLungCompartment.LeftLung).getValue());
	        parameters.put("RightLungSeverity", ARDS.getSeverity(eLungCompartment.RightLung).getValue());

	    } else if (this.condition instanceof SEChronicObstructivePulmonaryDisease) {
	        this.name = "COPD";
	        SEChronicObstructivePulmonaryDisease COPD = (SEChronicObstructivePulmonaryDisease) this.condition;
	        parameters.put("BronchitisSeverity", COPD.getBronchitisSeverity().getValue());
	        parameters.put("LeftLungEmphysemaSeverity", COPD.getEmphysemaSeverity(eLungCompartment.LeftLung).getValue());
	        parameters.put("RightLungEmphysemaSeverity", COPD.getEmphysemaSeverity(eLungCompartment.RightLung).getValue());

	    } else if (this.condition instanceof SEChronicPericardialEffusion) {
	        this.name = "Pericardial Effusion";
	        SEChronicPericardialEffusion CPE = (SEChronicPericardialEffusion) this.condition;
	        parameters.put("AccumulatedVolume", CPE.getAccumulatedVolume().getValue(VolumeUnit.mL));

	    } else if (this.condition instanceof SEChronicRenalStenosis) {
	        this.name = "Renal Stenosis";
	        SEChronicRenalStenosis Stenosis = (SEChronicRenalStenosis) this.condition;
	        parameters.put("LeftKidneySeverity", Stenosis.getLeftKidneySeverity().getValue());
	        parameters.put("RightKidneySeverity", Stenosis.getRightKidneySeverity().getValue());

	    } else if (this.condition instanceof SEChronicVentricularSystolicDysfunction) {
	        this.name = "Chronic Ventricular Systolic Disfunction";	       
	        
	    } else if (this.condition instanceof SEPneumonia) {
	        this.name = "Pneumonia";
	        SEPneumonia Pneumonia = (SEPneumonia) this.condition;
	        parameters.put("LeftLungSeverity", Pneumonia.getSeverity(eLungCompartment.LeftLung).getValue());
	        parameters.put("RightLungSeverity", Pneumonia.getSeverity(eLungCompartment.RightLung).getValue());

	    } else if (this.condition instanceof SEPulmonaryFibrosis) {
	        this.name = "Pulmonary Fibrosis";
	        SEPulmonaryFibrosis fibrosis = (SEPulmonaryFibrosis) this.condition;
	        parameters.put("Severity", fibrosis.getSeverity().getValue());

	    } else if (this.condition instanceof SEPulmonaryShunt) {
	        this.name = "Pulmonary Shunt";
	        SEPulmonaryShunt shunt = (SEPulmonaryShunt) this.condition;
	        parameters.put("Severity", shunt.getSeverity().getValue());
	    }
	}
	
	/*
	 * Return SECondition object
	 */
	public SECondition getCondition(){
		return condition;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, Double> getParameters() {
		return parameters;
	}
	
	public String toString(){
		return condition.toString();
	}

}
