package panels;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

import app.App;

@Route("patient-condition-panel")
public class PatientConditionPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
	/*
	 * Panel to group Patient and Conditions Panel
	 */
	
	private final PatientPanel patientPanel; 
    private final ConditionsPanel conditionsPanel; 

    public PatientConditionPanel(App app) {
        this.patientPanel = new PatientPanel(app);
        this.conditionsPanel = new ConditionsPanel(app);
        setSpacing(false);
        getStyle().set("margin","0px" );
        getStyle().set("padding","0px" );

	    //Buttons to switch between the two panels
        Button patientButton = new Button(VaadinIcon.USER.create());
        patientButton.setTooltipText("Patients info");
        patientButton.setWidth("9vw");
        Button conditionsButton = new Button(VaadinIcon.CLIPBOARD_HEART.create());
        conditionsButton.setTooltipText("Conditions");
        conditionsButton.setWidth("9vw");

        patientButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Div contentLayout = new Div();
        contentLayout.getStyle().set("margin", "0").set("padding", "0"); 
        contentLayout.add(patientPanel); 
        
        patientButton.addClickListener(event -> {
            contentLayout.removeAll();
            contentLayout.add(patientPanel);
            patientButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            conditionsButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });

        conditionsButton.addClickListener(event -> {
            contentLayout.removeAll();
            contentLayout.add(conditionsPanel);
            conditionsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            patientButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });

        HorizontalLayout topArea = new HorizontalLayout();
        topArea.setWidthFull(); 
        topArea.setJustifyContentMode(JustifyContentMode.CENTER); 

        topArea.add(patientButton, conditionsButton);
        add(topArea, contentLayout);
        add(topArea, contentLayout);
    }
    
    public PatientPanel getPatientPanel() {
    	return patientPanel;
    }
    
    public ConditionsPanel getConditionsPanel() {
    	return conditionsPanel;
    }
}
