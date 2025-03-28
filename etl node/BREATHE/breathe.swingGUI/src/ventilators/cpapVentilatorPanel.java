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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.App;
import data.Ventilator;

public class cpapVentilatorPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private JSpinner fractionInspOxygen, deltaPressureSup, positiveEndExpPres, slope;
    
	private JButton applyButton;
	
	// MechanicalVentilatorContinuousPositiveAirwayPressure (CPAP)
	public cpapVentilatorPanel(App app) {
		
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
        addLabelAndField("Delta Pressure Support - deltaPsupp", deltaPressureSup = new JSpinner(new SpinnerNumberModel(10, 0, 50, 1)), inputPanel, gbc);
        addLabelAndField("Positive End Expiratory Pressure - PEEP", positiveEndExpPres = new JSpinner(new SpinnerNumberModel(5, 0, 20, 1)), inputPanel, gbc);
        addLabelAndField("Slope", slope = new JSpinner(new SpinnerNumberModel(0.2, 0, 2, 0.1)), inputPanel, gbc);
        
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
        dataMap.put("FractionInspiredOxygen", (double) fractionInspOxygen.getValue());
        dataMap.put("DeltaPressureSupport", (int) deltaPressureSup.getValue()); 
        dataMap.put("PositiveEndExpiratoryPressure", (int) positiveEndExpPres.getValue());
        dataMap.put("Slope", (double) slope.getValue()); 
        return dataMap;
    }
    
    //setting ventilator (if on in the state file)
	public void setVentilator(Ventilator ventilator) {
		fractionInspOxygen.setValue(ventilator.getParameters().get("FractionInspiredOxygen"));
		deltaPressureSup.setValue(ventilator.getParameters().get("DeltaPressureSup").intValue());
		slope.setValue(ventilator.getParameters().get("Slope"));
		positiveEndExpPres.setValue(ventilator.getParameters().get("PositiveEndExpiratoryPressure").intValue());
		
		applyButton.setEnabled(true);
	}
    
    public void setEnableApplyButton(boolean enable) {
    	applyButton.setEnabled(enable);
    }


}
