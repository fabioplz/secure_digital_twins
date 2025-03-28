package ventilators;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import app.App;
import data.Ventilator;

import java.util.HashMap;
import java.util.Map;

public class pcVentilatorPanel extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    private NumberField fractionInspOxygen;
    private NumberField inspiratoryPeriod;
    private NumberField inspiratoryPressure;
    private NumberField positiveEndExpPres;
    private NumberField respirationRate;
    private NumberField slope;
    private ComboBox<String> assistedMode;

    private Button applyButton;

    private App app;

    // MechanicalVentilatorContinuousPositiveAirwayPressure (PC)
    public pcVentilatorPanel(App app) {
        this.app = app;

        getStyle().set("margin", "0px");
        getStyle().set("padding", "2px");
        getStyle().set("border-bottom", "2px solid #ccc"); 

        setSpacing(false);

        Div fixedSizeDiv = new Div();
        fixedSizeDiv.getStyle().set("box-sizing", "border-box"); 

        VerticalLayout fieldLayout = new VerticalLayout();
        fieldLayout.setPadding(false);
        fieldLayout.setSpacing(false);

        fractionInspOxygen = new NumberField("Fraction Inspired Oxygen - FiO2");
        fractionInspOxygen.setValue(0.21);
        fractionInspOxygen.setMin(0);
        fractionInspOxygen.setMax(1);
        fractionInspOxygen.setStep(0.01);
        fractionInspOxygen.setWidth("26vw"); 
        fractionInspOxygen.setStepButtonsVisible(true);

        inspiratoryPeriod = new NumberField("Inspiratory Period - Ti");
        inspiratoryPeriod.setValue(1.0);
        inspiratoryPeriod.setMin(0);
        inspiratoryPeriod.setMax(10);
        inspiratoryPeriod.setStep(0.1);
        inspiratoryPeriod.setWidth("26vw");
        inspiratoryPeriod.setStepButtonsVisible(true);

        inspiratoryPressure = new NumberField("Inspiratory Pressure - Pinsp");
        inspiratoryPressure.setValue(19.0);
        inspiratoryPressure.setMin(0);
        inspiratoryPressure.setMax(100);
        inspiratoryPressure.setStep(1);
        inspiratoryPressure.setWidth("26vw");
        inspiratoryPressure.setStepButtonsVisible(true);

        positiveEndExpPres = new NumberField("Positive End Expiratory Pressure - PEEP");
        positiveEndExpPres.setValue(5.0);
        positiveEndExpPres.setMin(0);
        positiveEndExpPres.setMax(20);
        positiveEndExpPres.setStep(1);
        positiveEndExpPres.setWidth("26vw");
        positiveEndExpPres.setStepButtonsVisible(true);

        respirationRate = new NumberField("Respiration Rate - RR");
        respirationRate.setValue(12.0);
        respirationRate.setMin(0);
        respirationRate.setMax(60);
        respirationRate.setStep(1);
        respirationRate.setWidth("26vw");
        respirationRate.setStepButtonsVisible(true);

        slope = new NumberField("Slope");
        slope.setValue(0.2);
        slope.setMin(0);
        slope.setMax(2);
        slope.setStep(0.1);
        slope.setWidth("26vw");
        slope.setStepButtonsVisible(true);

        assistedMode = new ComboBox<>("Assisted Mode");
        assistedMode.setItems("AC", "CMV");
        assistedMode.setValue("AC");
        assistedMode.setWidth("26vw");

        fieldLayout.add(fractionInspOxygen, inspiratoryPeriod, inspiratoryPressure,
                positiveEndExpPres, respirationRate, slope, assistedMode);

        Div scrollableDiv = new Div();
        scrollableDiv.getStyle().set("overflow-y", "auto"); 
        scrollableDiv.getStyle().set("scrollbar-width", "none");
        scrollableDiv.setHeight("55vh"); 
        scrollableDiv.add(fieldLayout);
        fixedSizeDiv.add(scrollableDiv);
        fixedSizeDiv.setHeight("55vh");

        applyButton = new Button("Apply");
        applyButton.setEnabled(false);
        applyButton.addClickListener(e -> applySettings());
        HorizontalLayout buttonLayout = new HorizontalLayout(applyButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); 
        buttonLayout.setWidthFull();

        add(fixedSizeDiv);
        add(buttonLayout);
    }

    private void applySettings() {
        app.connectVentilator();
        Notification.show("Settings updated",3000,Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
    }

    // Method to get all ventilator data in a Map (The name has to be equal to that of the engine)
    public Map<String, Number> getData() {
        Map<String, Number> dataMap = new HashMap<>();
        dataMap.put("FractionInspiredOxygen", fractionInspOxygen.getValue());
        dataMap.put("InspiratoryPeriod", inspiratoryPeriod.getValue());
        dataMap.put("InspiratoryPressure", inspiratoryPressure.getValue().intValue());
        dataMap.put("PositiveEndExpiratoryPressure", positiveEndExpPres.getValue().intValue());
        dataMap.put("RespirationRate", respirationRate.getValue().intValue());
        dataMap.put("Slope", slope.getValue());
        // Include the Assisted Mode as a String value
        dataMap.put("AssistedMode", assistedMode.getValue().equals("AC") ? 0 : 1);  
        return dataMap;
    }

	//setting ventilator (if on in the state file)
	public void setVentilator(Ventilator ventilator) {
		fractionInspOxygen.setValue(ventilator.getParameters().get("FractionInspiredOxygen").doubleValue());
		inspiratoryPeriod.setValue(ventilator.getParameters().get("InspiratoryPeriod").doubleValue());
		inspiratoryPressure.setValue(ventilator.getParameters().get("InspiratoryPressure").doubleValue());
		positiveEndExpPres.setValue(ventilator.getParameters().get("PositiveEndExpiratoryPressure").doubleValue());
		respirationRate.setValue(ventilator.getParameters().get("RespirationRate").doubleValue());
		slope.setValue(ventilator.getParameters().get("Slope").doubleValue());
		if((int) ventilator.getParameters().get("AssistedMode") == 0) assistedMode.setValue("AC");
		else assistedMode.setValue("CMV");
		
		applyButton.setEnabled(true);
	}
	
    public void setEnableApplyButton(boolean enable) {
        applyButton.setEnabled(enable);
    }
}
