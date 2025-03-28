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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.App;
import data.Ventilator;

public class vcVentilatorPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private JSpinner flow, fractionInspOxygen, inspiratoryPeriod, positiveEndExpPres, respirationRate, tidalVol;
    private JComboBox<String> assistedMode = new JComboBox<>(new String[]{"AC", "CMV"});
    
    private JButton applyButton;
    
    // SEMechanicalVentilatorVolumeControl (VC)
	public vcVentilatorPanel(App app) {
		this.setBackground(Color.LIGHT_GRAY);
		
		this.setLayout(new BorderLayout());
        
		JPanel inputPanel = new JPanel(new GridBagLayout());
		inputPanel.setBackground(Color.LIGHT_GRAY);
		
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        addLabelAndField("Flow", flow = new JSpinner(new SpinnerNumberModel(60, 0, 120, 1)), inputPanel, gbc);
        addLabelAndField("Fraction Inspired Oxygen - FiO2", fractionInspOxygen = new JSpinner(new SpinnerNumberModel(0.21, 0, 1, 0.01)), inputPanel, gbc);
        addLabelAndField("Positive End Expiratory Pressure - PEEP", positiveEndExpPres = new JSpinner(new SpinnerNumberModel(5, 0, 20, 1)), inputPanel, gbc);
        addLabelAndField("Inspiratory Period", inspiratoryPeriod = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 10.0, 0.1)), inputPanel, gbc);
        addLabelAndField("Respiration Rate - RR", respirationRate = new JSpinner(new SpinnerNumberModel(12, 0, 60, 1)), inputPanel, gbc);
        addLabelAndField("Tidal Volume - VT", tidalVol = new JSpinner(new SpinnerNumberModel(900, 0, 2000, 10)), inputPanel, gbc);
        addLabelAndField("Assisted Mode", assistedMode, inputPanel, gbc);
        
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
	
    //method to add visual to panel
    private void addLabelAndField(String labelText, JComponent component, JPanel panel, GridBagConstraints gbc) {
    	component.setPreferredSize(new Dimension(65, 25));
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
        gbc.gridy++;
    }
    
    
    //Get ventilator data 
    public Map<String, Number> getData() {
        Map<String, Number> dataMap = new HashMap<>();
        dataMap.put("Flow", (int) flow.getValue()); 
        dataMap.put("FractionInspiredOxygen", (double) fractionInspOxygen.getValue());
        dataMap.put("InspiratoryPeriod", (double) inspiratoryPeriod.getValue());
        dataMap.put("PositiveEndExpiratoryPressure", (int) positiveEndExpPres.getValue());
        dataMap.put("RespirationRate", (int) respirationRate.getValue());
        dataMap.put("TidalVolume", (int) tidalVol.getValue()); 
        if(assistedMode.getSelectedItem().toString().equals("AC"))
        	dataMap.put("AssistedMode", 0); 
        else
        	dataMap.put("AssistedMode", 1); 
        return dataMap;
    }
    
	//setting ventilator (if on in the state file)
	public void setVentilator(Ventilator ventilator) {
		fractionInspOxygen.setValue(ventilator.getParameters().get("FractionInspiredOxygen"));
		inspiratoryPeriod.setValue(ventilator.getParameters().get("InspiratoryPeriod"));
		flow.setValue(ventilator.getParameters().get("Flow").intValue());
		positiveEndExpPres.setValue(ventilator.getParameters().get("PositiveEndExpiratoryPressure").intValue());
		respirationRate.setValue(ventilator.getParameters().get("RespirationRate").intValue());
		tidalVol.setValue(ventilator.getParameters().get("TidalVolume").intValue());
		if((int) ventilator.getParameters().get("AssistedMode") == 0) assistedMode.setSelectedItem("AC");
		else assistedMode.setSelectedItem("CMV");
		
    	applyButton.setEnabled(true);
	}
    
    public void setEnableApplyButton(boolean enable) {
    	applyButton.setEnabled(enable);
    }


}
