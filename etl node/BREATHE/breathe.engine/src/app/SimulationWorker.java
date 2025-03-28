package app;

import data.*;
import data.Action;
import interfaces.GuiCallback;
import server.ZeroServer;
import utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;

import javax.swing.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kitware.pulse.cdm.engine.SEDataRequestManager;
import com.kitware.pulse.cdm.properties.CommonUnits.*;
import com.kitware.pulse.engine.PulseEngine;
import com.kitware.pulse.cdm.engine.SEPatientConfiguration;
import com.kitware.pulse.cdm.patient.SEPatient;
import com.kitware.pulse.cdm.patient.actions.SEMechanicalVentilation;
import com.kitware.pulse.cdm.conditions.SECondition;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kitware.pulse.cdm.actions.SEAction;
import com.kitware.pulse.cdm.actions.SEAdvanceTime;
import com.kitware.pulse.cdm.bind.Enums.eSwitch;
import com.kitware.pulse.cdm.properties.SEScalarTime;
import com.kitware.pulse.cdm.scenario.SEScenario;
import com.kitware.pulse.cdm.system.equipment.SEEquipmentAction;
import com.kitware.pulse.cdm.system.equipment.mechanical_ventilator.actions.SEMechanicalVentilatorConfiguration;
import com.kitware.pulse.cdm.system.equipment.mechanical_ventilator.actions.SEMechanicalVentilatorContinuousPositiveAirwayPressure;
import com.kitware.pulse.cdm.system.equipment.mechanical_ventilator.actions.SEMechanicalVentilatorPressureControl;
import com.kitware.pulse.cdm.system.equipment.mechanical_ventilator.actions.SEMechanicalVentilatorVolumeControl;

public class SimulationWorker extends SwingWorker<Void, String>{
	
    private PulseEngine pe;
    private String initializeMode;
    
    private SEDataRequestManager dataRequests;
    private String[] requestList, unitList;
    
    private GuiCallback gui;
    
    private boolean stopRequest = false;
    private boolean stabilized = false;

    private SEScalarTime stime = new SEScalarTime(0, TimeUnit.s);
    private SEPatientConfiguration patient_configuration = new SEPatientConfiguration();
    
    //Data for scenario
    private String scenarioFilePath = null;
    private String patientFilePath = null;
    
    //Data for external ventilator
    private ZeroServer zmqServer;
    private SEMechanicalVentilation ventilator_ext = new SEMechanicalVentilation();
    private boolean standardVent_running = false;
    private boolean extVent_running = false;
    private boolean firstEXTConnection = true;
    
    private String patientID = null;
    private String inputPatient = null;
      
    public SimulationWorker(GuiCallback guiCallback) {
    	this.gui = guiCallback;
    }
    
	/*
	 * STARTING NORMAL SIMULATION
	 */
    public void simulation(Patient patient) {
    	initializeMode = "standard";
        pe = new PulseEngine();
		
        dataRequests = new SEDataRequestManager();
        setDataRequests(dataRequests);
        patient_configuration = patient.getPatientConfiguration();
        patient_configuration.setDataRootDir("../breathe.engine/resources");
        
        for(Condition any : patient.getConditions())
        {
          patient_configuration.getConditions().add(any.getCondition());
        }
        gui.minilogStringData("Loading...");
    	this.execute();
    }
    
	/*
	 * STARTING FILE SIMULATION
	 */
    public void simulationFromFile(String filePath) {
    	initializeMode = "file";
    	pe = new PulseEngine();
    	
        dataRequests = new SEDataRequestManager();
        setDataRequests(dataRequests);
        
		gui.minilogStringData("Loading state file " + filePath);
		pe.serializeFromFile(filePath, dataRequests);
		patientFilePath = filePath;

		//check that patient has loaded
		SEPatient initialPatient = new SEPatient();
		pe.getInitialPatient(initialPatient);
		
		//get conditions
		List<SECondition> listCondition = new ArrayList<>();
		List<Condition> temp_listCondition = new ArrayList<>();
        pe.getConditions(listCondition);
        for(SECondition c : listCondition) {
        	Condition temp = new Condition(c);
        	temp_listCondition.add(temp);
        }
        gui.setCondition(temp_listCondition);
        
     
		//get ventilators data (if connected)
        List<SEAction> listAction = new ArrayList<SEAction>();
		pe.getActiveActions(listAction);
		Ventilator temp_ventilator;
        for(SEAction a : listAction) {
        	
        	if ((a instanceof SEEquipmentAction) && !(a instanceof SEMechanicalVentilatorConfiguration)) {
        		temp_ventilator = new Ventilator(a);
        		gui.setVentilator(temp_ventilator);
        		break;
        	}
        	
        	if((a instanceof SEMechanicalVentilation)){
        		SEMechanicalVentilation vent = (SEMechanicalVentilation) a;
        		vent.setState(eSwitch.Off);
    		    pe.processAction(vent);
        	}
        }
        
    	this.execute();
    }
    
	/*
	 * STARTING SCENARIO
	 */
    public void simulationFromScenario(String scenarioFilePath) {
    	initializeMode = "scenario";
    	
    	//METHOD TO SEND DATA TO GUI (conditions and ventilators)
    	PulseEngine pe1 = new PulseEngine();
		
        dataRequests = new SEDataRequestManager();
        setDataRequests(dataRequests);
    	
        this.scenarioFilePath = scenarioFilePath;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode_scenario = null;
		try {
			rootNode_scenario = mapper.readTree(new File(scenarioFilePath));
		} catch (IOException e) {		
			e.printStackTrace();
		}
        patientFilePath = rootNode_scenario.path("EngineStateFile").asText();
        
        
        gui.minilogStringData("Loading Scenario " + scenarioFilePath);
        pe1.serializeFromFile(patientFilePath, dataRequests);

		//check that patient has loaded
		SEPatient initialPatient = new SEPatient();
		pe1.getInitialPatient(initialPatient);
		
		gui.minilogStringData("Load state ("+patientFilePath+")");
		List<SECondition> list = new ArrayList<>();
		List<Condition> temp_list = new ArrayList<>();
		pe1.getConditions(list);
        for(SECondition c : list) {
        	Condition temp = new Condition(c);
        	temp_list.add(temp);
        }
        gui.setCondition(temp_list);

      //get ventilators data (if connected)
        List<SEAction> listAction = new ArrayList<SEAction>();
        pe1.getActiveActions(listAction);
		Ventilator temp_ventilator;
        for(SEAction a : listAction) {
        	
        	if ((a instanceof SEEquipmentAction) && !(a instanceof SEMechanicalVentilatorConfiguration)) {
        		temp_ventilator = new Ventilator(a);
        		gui.setVentilator(temp_ventilator);
        		break;
        	}
        }
        pe1.clear();
        pe1.cleanUp();
        
    	this.execute();
    }

    public void stopSimulation() {
    	stopRequest = true;
    	zmqServer.close();
    }
    
	@Override
	protected Void doInBackground() throws Exception {
		zmqServer = new ZeroServer();
       	try {
			zmqServer.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
       	Thread.sleep(1000);
		if(initializeMode.equals("standard")) {
	        pe.initializeEngine(patient_configuration, dataRequests); 
	        exportInitialPatient(patient_configuration.getPatient());	
	        
	        //Advice for stabilization completed
	        stabilized = true;
			gui.stabilizationComplete(true);
			gui.minilogStringData("\nSimulation Started");
			sendInputPatient();
		}else if(initializeMode.equals("file")) {
			gui.stabilizationComplete(true);
			gui.minilogStringData("\nSimulation Started");
			sendInputPatient();
		}else if(initializeMode.equals("scenario")) {
			run_scenario();
		}
	
		while (!stopRequest) {
        	if(!simulationLoop()) return null;
        }
		
		stopRequest = false;
        pe.clear();
        pe.cleanUp();
		gui.minilogStringData("\nSimulation has been stopped");
        return null;
	}
	
    private void setDataRequests(SEDataRequestManager dataRequests) {
    	//list of data requests.
    	//SimTime is mandatory, since it is always retrieved
    	//order is important
    	String[] requestList = {"SimTime",
				"HeartRate",
				"TotalLungVolume",
				"RespirationRate",
				"Lead3ElectricPotential",
				"CarbonDioxide",
				"ArterialPressure",
				"AirwayPressure",
				"OxygenSaturation"
				};
    	String[] unitList = {"s",                
                "per_min",          
                "mL",               
                "per_min",          
                "mV",               
                "mmHg",             
                "mmHg",             
                "mmHg",             
                ""                  
               };

		this.requestList = requestList;
		this.unitList = unitList; 
    	
    	this.requestList = requestList;
    	
    	//create the requests
    	dataRequests.createPhysiologyDataRequest(requestList[1], FrequencyUnit.Per_min);
        dataRequests.createPhysiologyDataRequest(requestList[2], VolumeUnit.mL);
        dataRequests.createPhysiologyDataRequest(requestList[3], FrequencyUnit.Per_min);
        dataRequests.createECGDataRequest(requestList[4], ElectricPotentialUnit.mV);
        dataRequests.createGasCompartmentDataRequest("Carina", "CarbonDioxide", "PartialPressure", PressureUnit.mmHg);
        dataRequests.createPhysiologyDataRequest(requestList[6], PressureUnit.mmHg);
        dataRequests.createPhysiologyDataRequest(requestList[7], PressureUnit.mmHg);
        dataRequests.createPhysiologyDataRequest(requestList[8]);
    }
    
    
    private void run_scenario() {
    	
    	//Load scenario
    	pe = new PulseEngine();
    	SEScenario sce = new SEScenario();
		try {
			sce.readFile(scenarioFilePath);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		if(sce.hasEngineState()) {
			if(!pe.serializeFromFile(patientFilePath, dataRequests));
		} else if(sce.hasPatientConfiguration()) {
			if(!pe.initializeEngine(sce.getPatientConfiguration(), dataRequests));
		}
		
		SEPatient initialPatient = new SEPatient();
		pe.getInitialPatient(initialPatient);
		
		//Advice for stabilaztion completed
		gui.stabilizationComplete(true);
		gui.minilogStringData("\nSimulation Started");
		sendInputPatient();
		for (SEAction a : sce.getActions()) {
			if(stopRequest)
		    	return;
			
		    if (a instanceof SEAdvanceTime) {
		        for(int i = 0; i<50; i++){	
		        	if(!simulationLoop()) return;
		        }

		    } else {
		        pe.processAction(a);
		        gui.minilogStringData("\nApplying " +  a.toString());
		        sendInputAction(a);
		    }
		}
	}
    
    private boolean simulationLoop() {
        long startTime = System.nanoTime(); 
        
        if (!pe.advanceTime(stime)) {
            gui.minilogStringData("\nError!");
            gui.minilogStringData("Simulation stopped!");
            return false;
        }

        if (extVent_running) 
            manage_ext();
        zmqServer.publishData(sendData());
        

        stime.setValue(0.02, TimeUnit.s);

        long elapsedTime = System.nanoTime() - startTime;
        long sleepTime =  (2 * 10000000) - elapsedTime; 
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime / 1000000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return true;
    }

    
    private void exportInitialPatient(SEPatient patient) {
        String basePath = "../breathe.engine/states/exported/";
        String baseFileName = patient.getName() + "@0s.json";
        String filePath = basePath + baseFileName;

        int counter = 1;
        while (new File(filePath).exists()) {
            filePath = basePath + patient.getName() + "@0 (" + counter+ ").json";
            counter++;
        }
        if( pe.serializeToFile(filePath) ) {
        	gui.minilogStringData("\nExported Patient File to " + filePath);
        }
        
        patientFilePath = filePath;
    }
    
    
    private String sendData() {
    	//ArrayList<String> data = new ArrayList<String>();
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode rootNode = objectMapper.createObjectNode(); 
        ObjectNode patientDataNode = objectMapper.createObjectNode();
        ObjectNode conditionsNode = objectMapper.createObjectNode();
        ObjectNode actionsNode = objectMapper.createObjectNode();
    	String currentCondition = null; 
    	ObjectNode currentConditionNode = null; 
	    rootNode.put("id", patientID);
	    
        //print requested data
    	List<Double> dataValues = pe.pullData();
        gui.logStringData("---------------------------\n");
        for(int i = 0; i < (dataValues.size()); i++ ) {
        	ObjectNode dataNode = objectMapper.createObjectNode(); 
    	    dataNode.put("value", dataValues.get(i));
    	    dataNode.put("unit", unitList[i]);   
    	    patientDataNode.set(requestList[i], dataNode);

            gui.logStringData(requestList[i] + ": " + dataValues.get(i) + "\n");
        }
        rootNode.set("Patient Data", patientDataNode);
        
    	//print conditions
        pe.getConditions(patient_configuration.getConditions());
        for(SECondition any : patient_configuration.getConditions())
        {
        	String[] dataLines = any.toString().split("\n");
        	for (int i = 0; i < dataLines.length; i++) {
        	    dataLines[i] = dataLines[i].trim();
        	}
        	
        	for (String line : dataLines) {
        	    line = line.trim();

        	    if (line.contains(":")) {
        	        String[] keyValue = line.split(":");
        	        String key = keyValue[0].trim();
        	        String value = keyValue[1].trim();

        	        if (isNumeric(value)) {
                        currentConditionNode.put(key, Double.parseDouble(value));
                    } else {
                        currentConditionNode.put(key, value); 
                    }
        	    } else if (!line.isEmpty()) {
        	        currentCondition = line; 
        	        currentConditionNode = objectMapper.createObjectNode(); 

        	        conditionsNode.set(currentCondition, currentConditionNode);
        	    }
        	}
        	
        	gui.logStringData(any.toString()+ "\n");
        }
        rootNode.set("Conditions", conditionsNode);

        //print actions
        List<SEAction> actions = new ArrayList<SEAction>();
        pe.getActiveActions(actions);
        for(SEAction any : actions)
        {	
        	String[] dataLines = any.toString().split("\n");
        	for (int i = 0; i < dataLines.length; i++) {
        	    dataLines[i] = dataLines[i].trim();
        	}
        	
        	for (String line : dataLines) {
        	    line = line.trim();
        	    if (line.contains(":")) {
        	        String[] keyValue = line.split(":");
        	        String key = keyValue[0].trim();
        	        String value = keyValue[1].trim();
        	        if (isNumeric(value)) {
                        currentConditionNode.put(key, Double.parseDouble(value));
                    } else {
                        currentConditionNode.put(key, value); 
                    }
        	    } else if (!line.isEmpty()) {
        	        currentCondition = line; 
        	        currentConditionNode = objectMapper.createObjectNode(); 

        	        actionsNode.set(currentCondition, currentConditionNode);
        	    }
        	}
        	
        	gui.logStringData(any.toString()+ "\n");
        }
        rootNode.set("Actions", actionsNode);
        
        //send data to graphs to be printed
        double x = dataValues.get(0);
        double y = 0;
        for (int i = 1; i < (dataValues.size()); i++) {
        	y = dataValues.get(i);
            gui.logItemDisplayData(requestList[i],x, y);
        }
        

        
        String jsonString = "";
		try {
			jsonString = objectMapper.writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
        return jsonString;
    }
    
    public void connectVentilator(Ventilator v) {
        switch (v.getMode()) {
        case PC:
        	SEMechanicalVentilatorPressureControl ventilator_PC = (SEMechanicalVentilatorPressureControl) v.getVentilator();
        	ventilator_PC.setConnection(eSwitch.On);
            if(pe.processAction(ventilator_PC)) {
	        	if(!standardVent_running) {
	        		gui.minilogStringData("\nPC Ventilator Connected ");
	        		standardVent_running = true;
	        	} else
	        		gui.minilogStringData("\nVentilator modify applied ");
            } else
            	gui.minilogStringData("\nPC Ventilator error!!! ");
            break;
            
        case CPAP:
        	SEMechanicalVentilatorContinuousPositiveAirwayPressure ventilator_CPAP = (SEMechanicalVentilatorContinuousPositiveAirwayPressure) v.getVentilator();
        	ventilator_CPAP.setConnection(eSwitch.On);
        	if(pe.processAction(ventilator_CPAP)) {
	        	if(!standardVent_running) {
	        		gui.minilogStringData("\nCPAP Ventilator Connected ");
	        		standardVent_running = true;
	        	} else
	        		gui.minilogStringData("\nVentilator modify applied ");
            } else
            	gui.minilogStringData("\nCPAP Ventilator error!!! ");
            break;

        case VC:
        	SEMechanicalVentilatorVolumeControl ventilator_VC = (SEMechanicalVentilatorVolumeControl) v.getVentilator();
        	ventilator_VC.setConnection(eSwitch.On);
        	if(pe.processAction(ventilator_VC)) {
	        	if(!standardVent_running) {
	        		gui.minilogStringData("\nVC Ventilator Connected ");
	        		standardVent_running = true;
	        	} else
	        		gui.minilogStringData("\nVentilator modify applied ");
            } else
            	gui.minilogStringData("\nVC Ventilator error!!! ");
            break;

        case EXT:
        	ventilator_ext = (SEMechanicalVentilation) v.getVentilator_External();
        	//Server wait for data
        	gui.minilogStringData("\nSearching for EXTERNAL ventilators...\n");
    		zmqServer.startReceiving();
    		extVent_running = true;
            break;
        }
    }
    
    public void disconnectVentilator(Ventilator v) {
        switch (v.getMode()) {
        case PC:
        	SEMechanicalVentilatorPressureControl ventilator_PC = (SEMechanicalVentilatorPressureControl) v.getVentilator();
        	ventilator_PC.setConnection(eSwitch.Off);
            pe.processAction(ventilator_PC);
            gui.minilogStringData("\nPC Ventilator Disconnected");
            break;
            
        case CPAP:
        	SEMechanicalVentilatorContinuousPositiveAirwayPressure ventilator_CPAP = (SEMechanicalVentilatorContinuousPositiveAirwayPressure) v.getVentilator();
        	ventilator_CPAP.setConnection(eSwitch.Off);
            pe.processAction(ventilator_CPAP);
            gui.minilogStringData("\nCPAP Ventilator Disconnected");
            break;

        case VC:
        	SEMechanicalVentilatorVolumeControl ventilator_VC = (SEMechanicalVentilatorVolumeControl) v.getVentilator();
        	ventilator_VC.setConnection(eSwitch.Off);
            pe.processAction(ventilator_VC);
            gui.minilogStringData("\nVC Ventilator Disconnected");
            break;

        case EXT:
	        ventilator_ext = (SEMechanicalVentilation) v.getVentilator_External();
			ventilator_ext.setState(eSwitch.Off);
		    pe.processAction(ventilator_ext);
	        zmqServer.stopReceiving();
        	gui.minilogStringData("EXTERNAL Ventilator server closed");
        	extVent_running = false;
        	firstEXTConnection = true;
        	resetLogExtVentilator();
        	break;
        }
        standardVent_running = false;
    }
    
    //Methods for external ventilator
	private void manage_ext(){
		if(zmqServer.isConnectionStable() && zmqServer.getSelectedMode() != null) {
			if(firstEXTConnection) {
				ventilator_ext.setState(eSwitch.On);
				gui.minilogStringData("EXTERNAL Ventilator connected");
				firstEXTConnection = false;
			}
			if (zmqServer.getSelectedMode().equals("Volume")) {
	            setExtVolume();
	        } else {
	            setExtPressure();
	        }
	        if(!pe.processAction(ventilator_ext)) {
	        	gui.minilogStringData("\nEXTERNAL Ventilator error!!!");
	        }

		} else {
			if(zmqServer.isDisconnecting()) {	
				ventilator_ext.setState(eSwitch.Off);
			    gui.minilogStringData("\nEXTERNAL Ventilator disconnected");
			    gui.minilogStringData("Searching for EXTERNAL ventilators...\n");
		        pe.processAction(ventilator_ext);
		        resetLogExtVentilator();
		        firstEXTConnection = true;
			}
		}
    }
    
	private void setExtVolume() {
		double volume = zmqServer.getVolume();
		ventilator_ext.getFlow().setValue(volume, VolumePerTimeUnit.mL_Per_s);
    	gui.logVolumeExternalVentilatorData(volume);
	}
	
	private void setExtPressure() {
		double pressure = zmqServer.getPressure();
		ventilator_ext.getPressure().setValue(pressure,PressureUnit.mmHg);
		gui.logPressureExternalVentilatorData(pressure);
	}
	
	private void resetLogExtVentilator() {
		gui.logPressureExternalVentilatorData(Double.NaN);
		gui.logVolumeExternalVentilatorData(Double.NaN);
	}
	
	public void applyAction(Action action) {
	    gui.minilogStringData("\nApplying " + action.getAction().toString());
	    pe.processAction(action.getAction());
	    sendInputAction(action.getAction());
	}

	
	public void exportSimulation(String exportFilePath) {
		if (pe.serializeToFile(exportFilePath))  
			gui.minilogStringData("\nExported Patient File to " + exportFilePath);
		else
			gui.minilogStringData("\nExported Failed to " + exportFilePath);
	}

	
    public void createScenario(String patientFile ,String scenarioName, ArrayList<Pair<Action, Integer>> actions) {
        SEScenario sce = new SEScenario();

        sce.setName(scenarioName);
        sce.setEngineState(patientFile);

        int seconds = 0;
        SEAdvanceTime adv = new SEAdvanceTime();
        adv.getTime().setValue(1, TimeUnit.s);
        if(actions != null) {
	        for (Pair<Action, Integer> action : actions) {
	            int target = action.getValue();
	
	            while (seconds < target) {
	                sce.getActions().add(adv);
	                seconds++;
	            }
	
	            sce.getActions().add(action.getKey().getAction());
	        }
        }
        String filePath = "../breathe.engine/scenario/exported/" + scenarioName + ".json";
        int counter = 1;
        while (new File(filePath).exists()) {
            filePath = "../breathe.engine/scenario/exported/"+ scenarioName + " (" + counter+ ").json";
            counter++;
        }
        sce.writeFile(filePath);
        gui.minilogStringData("\nScenario exported to: " + ".../breathe.engine/scenario/exported/" + scenarioName + ".json");
    }
    
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isStable() {
    	return stabilized;
    }
    
    public void sendInputAction(SEAction action) {

	    ObjectMapper objectMapper = new ObjectMapper();
	    ObjectNode rootNode = objectMapper.createObjectNode();
	    ObjectNode actionsNode = objectMapper.createObjectNode();
	    String currentCondition = null;
	    ObjectNode currentConditionNode = null;

	    String[] dataLines = action.toString().split("\n");
	    for (int i = 0; i < dataLines.length; i++) {
	        dataLines[i] = dataLines[i].trim();
	    }	    

	    for (String line : dataLines) {
	        line = line.trim();
	        if (line.contains(":")) {
	            String[] keyValue = line.split(":");
	            String key = keyValue[0].trim();
	            String value = keyValue[1].trim();
	            if (isNumeric(value)) {
	                currentConditionNode.put(key, Double.parseDouble(value));
	            } else {
	                currentConditionNode.put(key, value);
	            }
	        } else if (!line.isEmpty()) {
	            currentCondition = line;
	            currentConditionNode = objectMapper.createObjectNode();

	            actionsNode.set(currentCondition, currentConditionNode);
	        }
	    }
	    rootNode.put("id", patientID);
	    rootNode.set("Action", actionsNode);
	          
	    String actionData = "";
	    try {
	        actionData = objectMapper.writeValueAsString(rootNode);
		    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	    }
	    zmqServer.publishInputData(actionData);
    }
    
    public void sendInputPatient() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(patientFilePath));
            inputPatient = mapper.writeValueAsString(rootNode);
            String fileName = Paths.get(patientFilePath).getFileName().toString();
            String parentDir = Paths.get(patientFilePath).getParent().getFileName().toString();
            patientID = parentDir + "_" + fileName;
            ObjectNode rootNodeJson = mapper.createObjectNode();
            rootNodeJson.put("id", patientID);
            rootNodeJson.set("InputPatient", rootNode);
            String finalJson = mapper.writeValueAsString(rootNodeJson);
            zmqServer.publishInputData(finalJson);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading JSON file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

}
