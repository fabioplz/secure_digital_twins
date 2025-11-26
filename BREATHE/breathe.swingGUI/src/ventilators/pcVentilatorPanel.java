package ventilators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.App;
import data.Ventilator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

public class pcVentilatorPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JSpinner fractionInspOxygen, inspiratoryPeriod, inspiratoryPressure, positiveEndExpPres, respirationRate, slope;
    private JComboBox<String> assistedMode = new JComboBox<>(new String[]{"AC", "CMV"});

    private JButton applyButton;
    
    
    // MechanicalVentilatorContinuousPositiveAirwayPressure (PC)
    public pcVentilatorPanel(App app) {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.LIGHT_GRAY);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.LIGHT_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addLabelAndField("Fraction Inspired Oxygen - FiO2", fractionInspOxygen = new JSpinner(new SpinnerNumberModel(0.21, 0, 1, 0.01)), inputPanel, gbc);
        addLabelAndField("Inspiratory Period - Ti", inspiratoryPeriod = new JSpinner(new SpinnerNumberModel(1, 0, 10, 0.1)), inputPanel, gbc);
        addLabelAndField("Inspiratory Pressure - Pinsp", inspiratoryPressure = new JSpinner(new SpinnerNumberModel(19, 0, 100, 1)), inputPanel, gbc);
        addLabelAndField("Positive End Expiratory Pressure - PEEP", positiveEndExpPres = new JSpinner(new SpinnerNumberModel(5, 0, 20, 1)), inputPanel, gbc);
        addLabelAndField("Respiration Rate - RR", respirationRate = new JSpinner(new SpinnerNumberModel(12, 0, 60, 1)), inputPanel, gbc);
        addLabelAndField("Slope", slope = new JSpinner(new SpinnerNumberModel(0.2, 0, 2, 0.1)), inputPanel, gbc);
        addLabelAndField("Assisted Mode", assistedMode, inputPanel, gbc);

        // Create the Apply button
        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        applyButton.setFocusPainted(false);
        applyButton.setPreferredSize(new Dimension(150, 30));
        applyButton.addActionListener(e -> {
            app.connectVentilator();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        buttonPanel.setBackground(Color.LIGHT_GRAY); 
        buttonPanel.add(applyButton);
        
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.add(inputPanel, BorderLayout.CENTER);
    }

    // Method to add visual to panel
    private void addLabelAndField(String labelText, JComponent component, JPanel panel, GridBagConstraints gbc) {
        component.setPreferredSize(new Dimension(65, 25));
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
        gbc.gridy++;
    }

    // Method to get all ventilator data in a Map (The name has to be equal to that of the engine)
    public Map<String, Number> getData() {
        Map<String, Number> dataMap = new HashMap<>();
        dataMap.put("FractionInspiredOxygen", (double) fractionInspOxygen.getValue());
        dataMap.put("InspiratoryPeriod", (double) inspiratoryPeriod.getValue());
        dataMap.put("InspiratoryPressure", (int) inspiratoryPressure.getValue());
        dataMap.put("PositiveEndExpiratoryPressure", (int) positiveEndExpPres.getValue());
        dataMap.put("RespirationRate", (int) respirationRate.getValue());
        dataMap.put("Slope", (double) slope.getValue());
        // Include the Assisted Mode as a String value
        if (assistedMode.getSelectedItem().toString().equals("AC"))
            dataMap.put("AssistedMode", 0);
        else
            dataMap.put("AssistedMode", 1);
        return dataMap;
    }
    
	//setting ventilator (if on in the state file)
	public void setVentilator(Ventilator ventilator) {
		fractionInspOxygen.setValue(ventilator.getParameters().get("FractionInspiredOxygen"));
		inspiratoryPeriod.setValue(ventilator.getParameters().get("InspiratoryPeriod"));
		inspiratoryPressure.setValue(ventilator.getParameters().get("InspiratoryPressure").intValue());
		positiveEndExpPres.setValue(ventilator.getParameters().get("PositiveEndExpiratoryPressure").intValue());
		respirationRate.setValue(ventilator.getParameters().get("RespirationRate").intValue());
		slope.setValue(ventilator.getParameters().get("Slope"));
		if((int) ventilator.getParameters().get("AssistedMode") == 0) assistedMode.setSelectedItem("AC");
		else assistedMode.setSelectedItem("CMV");
		
		applyButton.setEnabled(true);
	}
    
    public void setEnableApplyButton(boolean enable) {
    	applyButton.setEnabled(enable);
    }


}
