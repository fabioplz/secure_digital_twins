package panels;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import app.App;
import data.Condition;
import inputItems.ConditionBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("conditions-panel")
public class ConditionsPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
	/*
	 * PANEL WITH ALL CONDITION BOXES
	 */
	
	private List<ConditionBox> boxes = new ArrayList<>();
    private List<Condition> activeConditions = new ArrayList<>();
    private Button resetButton;

    public ConditionsPanel(App app) {
    	
		getStyle().set("margin","0px" );
		getStyle().set("padding","2px" );

        setSpacing(false);

        Div fixedSizeDiv = new Div();
        fixedSizeDiv.getStyle().set("box-sizing", "border-box"); 

        // Panels containing all conditions
        VerticalLayout conditionLayout = new VerticalLayout();
        conditionLayout.setPadding(false);
        conditionLayout.setSpacing(false);
        
        /*
         * ADD CONDITIONS
         */
        addConditionBox(app, "Chronic Anemia", new String[]{"ReductionFactor"}, conditionLayout);
        addConditionBox(app, "ARDS", new String[]{"LeftLungSeverity", "RightLungSeverity"}, conditionLayout);
        addConditionBox(app, "COPD", new String[]{"BronchitisSeverity", "LeftLungEmphysemaSeverity", "RightLungEmphysemaSeverity"}, conditionLayout);
        addConditionBox(app, "Pericardial Effusion", new String[]{"AccumulatedVolume"}, conditionLayout, 100);
        addConditionBox(app, "Renal Stenosis", new String[]{"LeftKidneySeverity", "RightKidneySeverity"}, conditionLayout);
        addConditionBox(app, "Pneumonia", new String[]{"LeftLungSeverity", "RightLungSeverity"}, conditionLayout);
        addConditionBox(app, "Pulmonary Fibrosis", new String[]{"Severity"}, conditionLayout);
        addConditionBox(app, "Pulmonary Shunt", new String[]{"Severity"}, conditionLayout);

        Div scrollableDiv = new Div();
        scrollableDiv.getStyle().set("overflow-y", "auto");  
        scrollableDiv.getStyle().set("scrollbar-width", "none");
        
        scrollableDiv.setHeight("60vh"); 
        scrollableDiv.add(conditionLayout);
        fixedSizeDiv.add(scrollableDiv);
        fixedSizeDiv.setHeight("60vh");
        scrollableDiv.getStyle().set("border-bottom", "2px solid #ccc"); 

        add(fixedSizeDiv);

        // Reset Button
        resetButton = new Button("Reset Conditions", e -> resetConditions());
        HorizontalLayout buttonLayout = new HorizontalLayout(resetButton);
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); 
		buttonLayout.setWidthFull(); 
        add(buttonLayout);
    }

    private void addConditionBox(App app, String title, String[] fields, VerticalLayout container) {
        addConditionBox(app, title, fields, container, 1.0);
    }

    private void addConditionBox(App app, String title, String[] fields, VerticalLayout container, double max) {
        Map<String, Component> components = new HashMap<>();
        for (String field : fields) {
            NumberField spinner = new NumberField(field);
            spinner.setMin(0);
            spinner.setMax(max);
            spinner.setStep(0.01);
            components.put(field, spinner);
        }
        ConditionBox conditionBox = new ConditionBox(app, title, components);
        boxes.add(conditionBox);
        container.add(conditionBox);
    }

    private void resetConditions() {
        for (ConditionBox box : boxes) {
            box.reset(); 
        }
        activeConditions.clear(); 
    }

    public void addCondition(Condition c) {
        activeConditions.add(c);
    }

    public void removeCondition(String title) {
        activeConditions.removeIf(c -> c.getName().equals(title));
    }

    public void enableButtons(boolean enable) {
        resetButton.setEnabled(enable);
        for (ConditionBox box : boxes) {
            box.enableBox(enable); 
        }
    }
    
    public List<Condition> getActiveConditions() {
        return activeConditions;
    }

    //Set Conditions to GUI from File
    public void setConditions(List<Condition> list) {
        activeConditions.clear();
        for (ConditionBox box : boxes) {
            boolean found = false;
            for (Condition condition : list) {
                if (box.getTitle().equals(condition.getName())) {
                    found = true;
                    box.setComponents(condition.getParameters()); 
                    break;
                }
            }
            if (!found) {
                box.reset(); 
            }
        }
    }
}
