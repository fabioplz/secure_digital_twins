package breathe.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import app.Initializer;
import app.SimulationWorker;
import data.Action;
import data.Condition;
import data.Patient;
import data.Ventilator;
import interfaces.GuiCallback;
import utils.Pair;
import utils.VentilationMode;

public class TestEngine {

    @Test
    public void testStandardSimulation() {
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);

        /*
         * PATIENT
         */
        Map < String, Double > patientParameters = new HashMap < > ();
        patientParameters.put("Age", 30.0);
        patientParameters.put("BodyFatFraction", 0.18);
        patientParameters.put("HeartRateBaseline", 70.0);
        patientParameters.put("DiastolicArterialPressureBaseline", 80.0);
        patientParameters.put("SystolicArterialPressureBaseline", 120.0);
        patientParameters.put("RespirationRateBaseline", 16.0);
        patientParameters.put("BasalMetabolicRate", 1800.0);
        patientParameters.put("Weight", 75.0);
        patientParameters.put("Height", 175.0);

        //Add a condition
        Map < String, Double > anemiaParams = new HashMap < > ();
        anemiaParams.put("ReductionFactor", 0.5);
        Condition anemiaCondition = new Condition("Chronic Anemia", anemiaParams);


        List < Condition > conditions = new ArrayList < > ();
        conditions.add(anemiaCondition);
        Patient patient = new Patient("Testing", 'F', patientParameters, conditions);
        //Create also a male patient for covarage
        new Patient("Testing", 'M', patientParameters, conditions);
        patient.getPatient();
        patient.getName();


        //Start simulation (standard)
        sim.simulation(patient);
        while (!sim.isStable()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sim.stopSimulation();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Start another simulation for other test
        sim.simulation(patient);
        while (!sim.isStable()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sim.stopSimulation();

    }

    @Test //load a male patient file
    public void testPatientMale() {
        Initializer.initilizeJNI();
        new Patient("../breathe.engine/states/StandardMale@0s.json");
    }

    @Test //load a female patient file
    public void testPatientFemale() {
        Initializer.initilizeJNI();
        new Patient("../breathe.engine/states/StandardFemale@0s.json");
    }

    @Test //load wrong file
    public void testPatientError() {
        Initializer.initilizeJNI();
        try {
            new Patient("../breathe.engine/scenario/ErrorFile.json");
        } catch (Exception e) {}
    }


    @Test
    public void testSimulationFromFile() {
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);
        //test state file with PC_AC ventilator connected
        sim.simulationFromFile("../breathe.engine/states/exported/PC_AC.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();

        //test state file with PC_CMV ventilator connected
        sim.simulationFromFile("../breathe.engine/states/exported/PC_CMV.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();

        //test state file with VC_AC ventilator connected
        sim.simulationFromFile("../breathe.engine/states/exported/VC_AC.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();

        //test state file with VC_CMV ventilator connected
        sim.simulationFromFile("../breathe.engine/states/exported/VC_CMV.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();

        //test state file with CPAP ventilator connected
        sim.simulationFromFile("../breathe.engine/states/exported/CPAP.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();

        //test state file with CPAP ventilator connected
        sim.simulationFromFile("../breathe.engine/states/exported/EXT.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();
        
        //test state file with female patient
        sim.simulationFromFile("../breathe.engine/states/StandardFemale@0s.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();

        //test state file with a patient with all the condition applied
        sim.simulationFromFile("../breathe.engine/states/exported/Malannus.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();
    }

    @Test
    public void testExportSimulation() {
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);
        sim.simulationFromFile("../breathe.engine/states/StandardMale@0s.json");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.exportSimulation(" ");
        sim.exportSimulation("../breathe.engine/states/exported/ExportTest.json");
    }

    @Test 
    public void testSimulationFromScenario() {
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);
        
        //test scenario file with PC_AC ventilator connected in the state file
        sim.simulationFromScenario("../breathe.engine/scenario/exported/TestPC_AC.json");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();
        
        //test scenario file with EXT ventilator connected in the state file
        sim.simulationFromScenario("../breathe.engine/scenario/exported/TestEXT.json");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();
        
        //test scenario file with all condition applied in the state file
        sim.simulationFromScenario("../breathe.engine/scenario/exported/TestMalannus.json");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sim.stopSimulation();
        
        //load wrong file
        try {
            sim.simulationFromScenario("../breathe.engine/scenario/exported/ErrorFile.json");
        } catch (Exception e) {}
    }

    @Test
    public void testCreateScenario() {
        new Initializer();
        Initializer.initilizeJNI();
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);
        //add an action to scenario
        Map < String, Double > actionParameters = new HashMap < > ();
        actionParameters.put("BronchitisSeverity", 0.5);
        actionParameters.put("LeftLungEmphysemaSeverity", 0.6);
        actionParameters.put("RightLungEmphysemaSeverity", 0.4);
        Action copdExacerbationAction = new Action("COPD Exacerbation", actionParameters);
        ArrayList < Pair < Action, Integer >> actions = new ArrayList < > ();
        Pair < Action, Integer > p = new Pair < > (copdExacerbationAction, 1);
        actions.add(p);
        //create scenario
        sim.createScenario("../breathe.engine/states/StandardMale@0s", "TestScenario", actions);
        //recreate the same scenario
        sim.createScenario("../breathe.engine/states/StandardMale@0s", "TestScenario", actions);
    }

    @Test
    public void testVentilator() {
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);
        sim.simulationFromFile("../breathe.engine/states/exported/Malannus.json");

        // VC - Volume Control
        Map < String, Number > vcParameters = new HashMap < > ();
        vcParameters.put("AssistedMode", 1);
        vcParameters.put("Flow", 60);
        vcParameters.put("FractionInspiredOxygen", 0.5);
        vcParameters.put("InspiratoryPeriod", 1.2);
        vcParameters.put("PositiveEndExpiratoryPressure", 5);
        vcParameters.put("RespirationRate", 14);
        vcParameters.put("TidalVolume", 500);

        Ventilator ventilatorVC = new Ventilator(VentilationMode.VC, vcParameters);
        ventilatorVC.getParameters();
        sim.connectVentilator(ventilatorVC);
        sim.connectVentilator(ventilatorVC);
        sim.disconnectVentilator(ventilatorVC);

        // PC - Pressure Control
        Map < String, Number > pcParameters = new HashMap < > ();
        pcParameters.put("AssistedMode", 1);
        pcParameters.put("InspiratoryPressure", 15);
        pcParameters.put("FractionInspiredOxygen", 0.5);
        pcParameters.put("InspiratoryPeriod", 1.2);
        pcParameters.put("PositiveEndExpiratoryPressure", 5);
        pcParameters.put("RespirationRate", 14);
        pcParameters.put("Slope", 0.3);

        Ventilator ventilatorPC = new Ventilator(VentilationMode.PC, pcParameters);
        sim.connectVentilator(ventilatorPC);
        sim.connectVentilator(ventilatorPC);
        sim.disconnectVentilator(ventilatorPC);

        // CPAP - Continuous Positive Airway Pressure
        Map < String, Number > cpapParameters = new HashMap < > ();
        cpapParameters.put("FractionInspiredOxygen", 0.5);
        cpapParameters.put("DeltaPressureSupport", 8);
        cpapParameters.put("PositiveEndExpiratoryPressure", 5);
        cpapParameters.put("Slope", 0.3);

        Ventilator ventilatorCPAP = new Ventilator(VentilationMode.CPAP, cpapParameters);
        sim.connectVentilator(ventilatorCPAP);
        sim.connectVentilator(ventilatorCPAP);
        sim.disconnectVentilator(ventilatorCPAP);


        // EXTERNAL
        Ventilator externalVentilator = new Ventilator(VentilationMode.EXT);
        sim.connectVentilator(externalVentilator);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TempClient client = new TempClient();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.changetoVolume();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.disconnectFromServer();
        sim.disconnectVentilator(externalVentilator);
        
       //wrong constructor
       new Ventilator(VentilationMode.CPAP);
    }

    @Test
    public void testErrorsPC() {
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);
        sim.simulationFromFile("../breathe.engine/states/StandardMale@0s.json");

        // PC - Pressure Control
        Map < String, Number > pcParameters = new HashMap < > ();
        pcParameters.put("AssistedMode", 1);
        pcParameters.put("InspiratoryPressure", 15);
        pcParameters.put("FractionInspiredOxygen", 0.5);
        pcParameters.put("InspiratoryPeriod", 1.2);
        pcParameters.put("PositiveEndExpiratoryPressure", 25); //PEEP is too much high
        pcParameters.put("RespirationRate", 14);
        pcParameters.put("Slope", 0.3);

        Ventilator ventilatorPC = new Ventilator(VentilationMode.PC, pcParameters);
        sim.connectVentilator(ventilatorPC);
        sim.disconnectVentilator(ventilatorPC);
    }

    @Test //test apply action at the simulation
    public void testAction() {
        GuiCallback gui = new TempGUI();
        SimulationWorker sim = new SimulationWorker(gui);
        sim.simulationFromFile("../breathe.engine/states/StandardMale@0s.json");

        Map < String, Double > actionParameters = new HashMap < > ();
        actionParameters.put("BronchitisSeverity", 0.5);
        actionParameters.put("LeftLungEmphysemaSeverity", 0.6);
        actionParameters.put("RightLungEmphysemaSeverity", 0.4);

        Action copdExacerbationAction = new Action("COPD Exacerbation", actionParameters);
        sim.applyAction(copdExacerbationAction);
        sim.stopSimulation();
    }

    @Test
    public void testActions() {
        // ARDS Exacerbation
        Map < String, Double > ardsParams = new HashMap < > ();
        ardsParams.put("LeftLungSeverity", 0.7);
        ardsParams.put("RightLungSeverity", 0.8);
        new Action("ARDS Exacerbation", ardsParams);

        // Acute Stress
        Map < String, Double > stressParams = new HashMap < > ();
        stressParams.put("Severity", 0.5);
        new Action("Acute Stress", stressParams);

        // Airway Obstruction
        Map < String, Double > obstructionParams = new HashMap < > ();
        obstructionParams.put("Severity", 0.6);
        new Action("Airway Obstruction", obstructionParams);

        // Asthma Attack
        Map < String, Double > asthmaParams = new HashMap < > ();
        asthmaParams.put("Severity", 0.4);
        new Action("Asthma Attack", asthmaParams);

        // Brain Injury
        Map < String, Double > brainInjuryParams = new HashMap < > ();
        brainInjuryParams.put("Severity", 0.3);
        new Action("Brain Injury", brainInjuryParams);

        // Bronchoconstriction
        Map < String, Double > bronchoconstrictionParams = new HashMap < > ();
        bronchoconstrictionParams.put("Severity", 0.2);
        new Action("Bronchoconstriction", bronchoconstrictionParams);

        // COPD Exacerbation
        Map < String, Double > copdParams = new HashMap < > ();
        copdParams.put("BronchitisSeverity", 0.5);
        copdParams.put("LeftLungEmphysemaSeverity", 0.6);
        copdParams.put("RightLungEmphysemaSeverity", 0.4);
        new Action("COPD Exacerbation", copdParams);

        // Dyspnea
        Map < String, Double > dyspneaParams = new HashMap < > ();
        dyspneaParams.put("RespirationRateSeverity", 0.7);
        new Action("Dyspnea", dyspneaParams);

        // Exercise
        Map < String, Double > exerciseParams = new HashMap < > ();
        exerciseParams.put("Intensity", 0.8);
        new Action("Exercise", exerciseParams);

        // Pericardial Effusion
        Map < String, Double > effusionParams = new HashMap < > ();
        effusionParams.put("EffusionRate ml/s", 1.5);
        new Action("Pericardial Effusion", effusionParams);

        // Pneumonia Exacerbation
        Map < String, Double > pneumoniaParams = new HashMap < > ();
        pneumoniaParams.put("LeftLungSeverity", 0.6);
        pneumoniaParams.put("RightLungSeverity", 0.7);
        new Action("Pneumonia Exacerbation", pneumoniaParams);

        // Pulmonary Shunt Exacerbation
        Map < String, Double > shuntParams = new HashMap < > ();
        shuntParams.put("Severity", 0.4);
        new Action("Pulmonary Shunt Exacerbation", shuntParams);

        // Respiratory Fatigue
        Map < String, Double > fatigueParams = new HashMap < > ();
        fatigueParams.put("Severity", 0.5);
        new Action("Respiratory Fatigue", fatigueParams);

        // Urinate
        new Action("Urinate", new HashMap < > ());

        // Ventilator Leak
        Map < String, Double > leakParams = new HashMap < > ();
        leakParams.put("Severity", 0.3);
        Action leakAction = new Action("Ventilator Leak", leakParams);

        leakAction.getName();
        leakAction.getParameters();
        leakAction.toString();
    }

    @Test
    public void testAllConditions() {
        // Chronic Anemia
        Map < String, Double > anemiaParams = new HashMap < > ();
        anemiaParams.put("ReductionFactor", 0.5);
        new Condition("Chronic Anemia", anemiaParams);

        // ARDS
        Map < String, Double > ardsParams = new HashMap < > ();
        ardsParams.put("LeftLungSeverity", 0.7);
        ardsParams.put("RightLungSeverity", 0.8);
        new Condition("ARDS", ardsParams);

        // COPD
        Map < String, Double > copdParams = new HashMap < > ();
        copdParams.put("BronchitisSeverity", 0.6);
        copdParams.put("LeftLungEmphysemaSeverity", 0.5);
        copdParams.put("RightLungEmphysemaSeverity", 0.4);
        new Condition("COPD", copdParams);

        // Pericardial Effusion
        Map < String, Double > effusionParams = new HashMap < > ();
        effusionParams.put("AccumulatedVolume", 200.0);
        new Condition("Pericardial Effusion", effusionParams);

        // Renal Stenosis
        Map < String, Double > stenosisParams = new HashMap < > ();
        stenosisParams.put("LeftKidneySeverity", 0.6);
        stenosisParams.put("RightKidneySeverity", 0.7);
        new Condition("Renal Stenosis", stenosisParams);

        // Chronic Ventricular Systolic Dysfunction
        new Condition("Chronic Ventricular Systolic Disfunction", new HashMap < > ());

        // Pneumonia
        Map < String, Double > pneumoniaParams = new HashMap < > ();
        pneumoniaParams.put("LeftLungSeverity", 0.4);
        pneumoniaParams.put("RightLungSeverity", 0.5);
        new Condition("Pneumonia", pneumoniaParams);

        // Pulmonary Fibrosis
        Map < String, Double > fibrosisParams = new HashMap < > ();
        fibrosisParams.put("Severity", 0.7);
        new Condition("Pulmonary Fibrosis", fibrosisParams);

        // Pulmonary Shunt
        Map < String, Double > shuntParams = new HashMap < > ();
        shuntParams.put("Severity", 0.6);
        Condition shuntCondition = new Condition("Pulmonary Shunt", shuntParams);

        shuntCondition.getName();
        shuntCondition.getParameters();
        shuntCondition.toString();
    }

}