package ventilators;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import app.App;
import data.Ventilator;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.HashMap;
import java.util.Map;

public class cpapVentilatorPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private NumberField fractionInspOxygen;
	private NumberField deltaPressureSup;
	private NumberField positiveEndExpPres;
	private NumberField slope;

	private Button applyButton;

	private App app;

	// MechanicalVentilatorContinuousPositiveAirwayPressure (CPAP)
	public cpapVentilatorPanel(App app) {
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

		deltaPressureSup = new NumberField("Delta Pressure Support - deltaPsupp");
		deltaPressureSup.setValue(10.0);
		deltaPressureSup.setMin(0);
		deltaPressureSup.setMax(50);
		deltaPressureSup.setStep(1);
		deltaPressureSup.setWidth("26vw");
		deltaPressureSup.setStepButtonsVisible(true);

		positiveEndExpPres = new NumberField("Positive End Expiratory Pressure - PEEP");
		positiveEndExpPres.setValue(5.0);
		positiveEndExpPres.setMin(0);
		positiveEndExpPres.setMax(20);
		positiveEndExpPres.setStep(1);
		positiveEndExpPres.setWidth("26vw");
		positiveEndExpPres.setStepButtonsVisible(true);

		slope = new NumberField("Slope");
		slope.setValue(0.2);
		slope.setMin(0);
		slope.setMax(2);
		slope.setWidth("26vw");
		slope.setStep(0.1);
		slope.setStepButtonsVisible(true);

		fieldLayout.add(fractionInspOxygen, deltaPressureSup, positiveEndExpPres, slope);

		applyButton = new Button("Apply");
		applyButton.setEnabled(false);
		applyButton.addClickListener(e -> applySettings());
		HorizontalLayout buttonLayout = new HorizontalLayout(applyButton);
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); 
		buttonLayout.setWidthFull(); 

		Div scrollableDiv = new Div();
		scrollableDiv.getStyle().set("overflow-y", "auto");  
		scrollableDiv.getStyle().set("scrollbar-width", "none");

	    scrollableDiv.setHeight("55vh");  
        scrollableDiv.add(fieldLayout);
        fixedSizeDiv.add(scrollableDiv);
        fixedSizeDiv.setHeight("55vh");

        add(fixedSizeDiv);
        add(buttonLayout);
	}

	private void applySettings() {
		app.connectVentilator();
	}

	//Get ventilator data 
	public Map<String, Number> getData() {
		Map<String, Number> dataMap = new HashMap<>();
		dataMap.put("FractionInspiredOxygen", fractionInspOxygen.getValue());
		dataMap.put("DeltaPressureSupport", deltaPressureSup.getValue().intValue());
		dataMap.put("PositiveEndExpiratoryPressure", positiveEndExpPres.getValue().intValue());
		dataMap.put("Slope", slope.getValue());
		return dataMap;
	}

	//setting ventilator (if on in the state file)
	public void setVentilator(Ventilator ventilator) {
		fractionInspOxygen.setValue(ventilator.getParameters().get("FractionInspiredOxygen").doubleValue());
		deltaPressureSup.setValue(ventilator.getParameters().get("DeltaPressureSup").doubleValue());
		slope.setValue(ventilator.getParameters().get("Slope").doubleValue());
		positiveEndExpPres.setValue(ventilator.getParameters().get("PositiveEndExpiratoryPressure").doubleValue());
		
		applyButton.setEnabled(true);
	}
	
	public void setEnableApplyButton(boolean enable) {
		applyButton.setEnabled(enable);
	}
}
