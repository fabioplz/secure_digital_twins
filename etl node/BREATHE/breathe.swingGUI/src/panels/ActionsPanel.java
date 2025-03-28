package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.App;
import inputItems.ActionBox;

public class ActionsPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	/*
	 * PANEL CONTAINING ALL ACTIONS
	 */
	private List<ActionBox> boxes = new ArrayList<>();

    public ActionsPanel(App app) {
    	 
        this.setBackground(Color.LIGHT_GRAY);
        this.setPreferredSize(new Dimension(550, 650));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); 
        
        // Panels with all actions
        JPanel actionsContainer = new JPanel();
        actionsContainer.setLayout(new BoxLayout(actionsContainer, BoxLayout.Y_AXIS)); 
        actionsContainer.setBackground(Color.LIGHT_GRAY);
        actionsContainer.setBorder(null);
        
        JScrollPane scrollablePanel = new JScrollPane(actionsContainer);  
        scrollablePanel.setPreferredSize(new Dimension(550, 650)); 
        scrollablePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollablePanel.setBorder(null);
        
        /*
         * ADD ACTIONS
         */
        Map<String, JComponent> ardsComponents = new HashMap<>();
        ardsComponents.put("LeftLungSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ardsComponents.put("RightLungSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox ardsBox = new ActionBox(app, "ARDS Exacerbation", ardsComponents);
        boxes.add(ardsBox);

        Map<String, JComponent> acuteStressComponents = new HashMap<>();
        acuteStressComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox acuteStressBox = new ActionBox(app, "Acute Stress", acuteStressComponents);
        boxes.add(acuteStressBox);

        Map<String, JComponent> airwayObstructionComponents = new HashMap<>();
        airwayObstructionComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox airwayObstructionBox = new ActionBox(app, "Airway Obstruction", airwayObstructionComponents);
        boxes.add(airwayObstructionBox);

        Map<String, JComponent> asthmaAttackComponents = new HashMap<>();
        asthmaAttackComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox asthmaAttackBox = new ActionBox(app, "Asthma Attack", asthmaAttackComponents);
        boxes.add(asthmaAttackBox);

        Map<String, JComponent> brainInjuryComponents = new HashMap<>();
        brainInjuryComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox brainInjuryBox = new ActionBox(app, "Brain Injury", brainInjuryComponents);
        boxes.add(brainInjuryBox);

        Map<String, JComponent> bronchoconstrictionComponents = new HashMap<>();
        bronchoconstrictionComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox bronchoconstrictionBox = new ActionBox(app, "Bronchoconstriction", bronchoconstrictionComponents);
        boxes.add(bronchoconstrictionBox);

        Map<String, JComponent> copdExacerbationComponents = new HashMap<>();
        copdExacerbationComponents.put("BronchitisSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        copdExacerbationComponents.put("LeftLungEmphysemaSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        copdExacerbationComponents.put("RightLungEmphysemaSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox copdExacerbationBox = new ActionBox(app, "COPD Exacerbation", copdExacerbationComponents);
        boxes.add(copdExacerbationBox);

        Map<String, JComponent> dyspneaComponents = new HashMap<>();
        dyspneaComponents.put("RespirationRateSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox dyspneaBox = new ActionBox(app, "Dyspnea", dyspneaComponents);
        boxes.add(dyspneaBox);

        Map<String, JComponent> exerciseComponents = new HashMap<>();
        exerciseComponents.put("Intensity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox exerciseBox = new ActionBox(app, "Exercise", exerciseComponents);
        boxes.add(exerciseBox);

        Map<String, JComponent> pericardialEffusionComponents = new HashMap<>();
        pericardialEffusionComponents.put("EffusionRate ml/s", new JSpinner(new SpinnerNumberModel(0, 0, 1000, 0.01)));
        ActionBox pericardialEffusionBox = new ActionBox(app, "Pericardial Effusion", pericardialEffusionComponents);
        boxes.add(pericardialEffusionBox);

        Map<String, JComponent> pneumoniaExacerbationComponents = new HashMap<>();
        pneumoniaExacerbationComponents.put("LeftLungSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        pneumoniaExacerbationComponents.put("RightLungSeverity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox pneumoniaExacerbationBox = new ActionBox(app, "Pneumonia Exacerbation", pneumoniaExacerbationComponents);
        boxes.add(pneumoniaExacerbationBox);

        Map<String, JComponent> shuntExacerbationComponents = new HashMap<>();
        shuntExacerbationComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox shuntExacerbationBox = new ActionBox(app, "Pulmonary Shunt Exacerbation", shuntExacerbationComponents);
        boxes.add(shuntExacerbationBox);

        Map<String, JComponent> respiratoryFatigueComponents = new HashMap<>();
        respiratoryFatigueComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox respiratoryFatigueBox = new ActionBox(app, "Respiratory Fatigue", respiratoryFatigueComponents);
        boxes.add(respiratoryFatigueBox);

        ActionBox urinateBox = new ActionBox(app, "Urinate", new HashMap<>());
        boxes.add(urinateBox);

        Map<String, JComponent> ventilatorLeakComponents = new HashMap<>();
        ventilatorLeakComponents.put("Severity", new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01)));
        ActionBox ventilatorLeakBox = new ActionBox(app, "Ventilator Leak", ventilatorLeakComponents);
        boxes.add(ventilatorLeakBox);

        for(ActionBox box : boxes) {
        	actionsContainer.add(box.getSectionPanel());
        }
        
        this.add(Box.createRigidArea(new Dimension(550, 10)));
        this.add(scrollablePanel);
        this.add(Box.createRigidArea(new Dimension(550, 10)));
        
    }
    
    public void enableButtons(boolean enable) {
        for(ActionBox box : boxes) {
        	box.enableBox(enable);
        }    	
    }

}

