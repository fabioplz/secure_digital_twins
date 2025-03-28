package panels;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import app.App;
import inputItems.ActionBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("actions-panel")
public class ActionsPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
	/*
	 * PANEL CONTAINING ALL ACTIONS
	 */
	private List<ActionBox> boxes = new ArrayList<>();

    public ActionsPanel(App app, boolean forScenario) {
		getStyle().set("margin","0px" );
		getStyle().set("padding","2px" );

        setSpacing(false);

        Div fixedSizeDiv = new Div();
        fixedSizeDiv.getStyle().set("box-sizing", "border-box"); 
        
        // Panels with all actions
        VerticalLayout actionLayout = new VerticalLayout();
        actionLayout.setPadding(false);
        actionLayout.setSpacing(false);
        
        /*
         * ADD ACTIONS
         */
        addActionBox(app, "ARDS Exacerbation", new String[]{"LeftLungSeverity", "RightLungSeverity"}, actionLayout, forScenario);
        addActionBox(app, "Acute Stress", new String[]{"Severity"}, actionLayout, forScenario);
        addActionBox(app, "Airway Obstruction", new String[]{"Severity"}, actionLayout, forScenario);
        addActionBox(app, "Asthma Attack", new String[]{"Severity"}, actionLayout, forScenario);
        addActionBox(app, "Brain Injury", new String[]{"Severity"}, actionLayout, forScenario);
        addActionBox(app, "Bronchoconstriction", new String[]{"Severity"}, actionLayout, forScenario);
        addActionBox(app, "COPD Exacerbation", new String[]{
                "BronchitisSeverity", "LeftLungEmphysemaSeverity", "RightLungEmphysemaSeverity"}, actionLayout, forScenario);
        addActionBox(app, "Dyspnea", new String[]{"RespirationRateSeverity"}, actionLayout, forScenario);
        addActionBox(app, "Exercise", new String[]{"Intensity"}, actionLayout, forScenario);
        addActionBox(app, "Pericardial Effusion", new String[]{"EffusionRate ml/s"}, actionLayout, 1000, forScenario);
        addActionBox(app, "Pneumonia Exacerbation", new String[]{"LeftLungSeverity", "RightLungSeverity"}, actionLayout, forScenario);
        addActionBox(app, "Pulmonary Shunt Exacerbation", new String[]{"Severity"}, actionLayout, forScenario);
        addActionBox(app, "Respiratory Fatigue", new String[]{"Severity"}, actionLayout, forScenario);
        addActionBox(app, "Urinate", new String[]{}, actionLayout, forScenario);
        addActionBox(app, "Ventilator Leak", new String[]{"Severity"}, actionLayout, forScenario);

        Div scrollableDiv = new Div();
        scrollableDiv.getStyle().set("overflow-y", "auto");  
        scrollableDiv.getStyle().set("scrollbar-width", "none");
        
        scrollableDiv.setHeight("75vh"); 
        scrollableDiv.add(actionLayout);
        fixedSizeDiv.add(scrollableDiv);
        fixedSizeDiv.setHeight("75vh");
        scrollableDiv.getStyle().set("border-bottom", "2px solid #ccc"); 

        add(fixedSizeDiv);

    }

    private void addActionBox(App app, String title, String[] fields, VerticalLayout container, boolean fs) {
        addActionBox(app, title, fields, container, 1.0, fs); 
    }

    private void addActionBox(App app, String title, String[] fields, VerticalLayout container, double max, boolean fs) {
        Map<String, Component> components = new HashMap<>();
        for (String field : fields) {
            NumberField spinner = new NumberField(field);
            spinner.setMin(0);
            spinner.setMax(max);
            spinner.setStep(0.01);
            components.put(field, spinner);
        }
        ActionBox actionBox = new ActionBox(app, title, components, fs);
        boxes.add(actionBox);
        container.add(actionBox);
    }

    public void enableButtons(boolean enable) {
        for (ActionBox box : boxes) {
            box.enableBox(enable);
        }
    }
}
