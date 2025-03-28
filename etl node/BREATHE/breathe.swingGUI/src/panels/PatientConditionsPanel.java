package panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import app.App;

public class PatientConditionsPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	/*
	 * Panel to group Patient and Conditions Panel
	 */
	
	public PatientPanel patientPanel;
	 public ConditionsPanel conditionsPanel;
	 
	 private JPanel patientConditionPanel;
	 private CardLayout cardLayout;  
	 
	 public PatientConditionsPanel(App app) {
		 
		   patientPanel = new PatientPanel(app);
		   conditionsPanel = new ConditionsPanel(app);
		 	
		   JToggleButton patientButton = new JToggleButton("Patient");
	       JToggleButton conditionsButton = new JToggleButton("Conditions");
	       patientButton.setPreferredSize(new Dimension(150, 30));
	       conditionsButton.setPreferredSize(new Dimension(150, 30));
	        
	       ButtonGroup group = new ButtonGroup();
	       group.add(patientButton);
	       group.add(conditionsButton);
	       patientButton.setSelected(true);  
	        
	       //Switch between the two panels
	       JPanel radioPanel = new JPanel();
	       radioPanel.setBackground(Color.LIGHT_GRAY); 
	       radioPanel.add(patientButton);
	       radioPanel.add(conditionsButton);
	
	       cardLayout = new CardLayout();
	       patientConditionPanel = new JPanel(cardLayout);
	       patientConditionPanel.add(patientPanel, "Patient");
	       patientConditionPanel.add(conditionsPanel, "Condition");
	
	       patientButton.addActionListener(e -> cardLayout.show(patientConditionPanel, "Patient"));
	       conditionsButton.addActionListener(e -> cardLayout.show(patientConditionPanel, "Condition"));
	       
	       setPreferredSize(new Dimension(550, 20));
	       setBackground(Color.LIGHT_GRAY);
	       add(radioPanel, BorderLayout.NORTH);
	       add(patientConditionPanel, BorderLayout.CENTER);

	 }
	 
	 public PatientPanel getPatientPanel() {
		 return patientPanel;
	 }
	 
	 public ConditionsPanel getConditionsPanel() {
		 return conditionsPanel;
	 }
}
