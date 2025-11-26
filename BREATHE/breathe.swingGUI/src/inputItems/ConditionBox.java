package inputItems;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import app.App;
import data.Condition;

public class ConditionBox {
	
	/*
	 * ITEM to display a button for a single Condition
	 */
    
    private JPanel sectionPanel;
    private JButton applySectionButton;
    private JButton headerButton;
    
    private App app;
    
    private String title;
    private Map<String, JComponent> components;

    private boolean applied = false;
    
    public ConditionBox(App app, String title, Map<String, JComponent> components) {
    	
    	this.app = app;
        this.title = title;
        this.components = components;
        
        sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBackground(Color.LIGHT_GRAY);
        
        //Button with Conditions Name to show text fields
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.LIGHT_GRAY);
        
        headerButton = new JButton(title);
        headerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerButton.setBackground(Color.DARK_GRAY);
        headerButton.setForeground(Color.WHITE);
        headerButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        headerButton.setFocusPainted(false);
        
        // Text fields and Apply Buttons
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fieldsPanel.setBackground(Color.LIGHT_GRAY);
        fieldsPanel.setVisible(false);  
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        int gridX = 0;
        
        // Add Labels and JComponents
        for (Map.Entry<String, JComponent> entry : components.entrySet()) {
            JLabel label = new JLabel(addSpaceBeforeUpperCase(entry.getKey()) + ":");
            gbc.gridx = gridX++;
            fieldsPanel.add(label, gbc);
            
            Dimension minSize = new Dimension(100, 30); // 100px larghezza, 50px altezza
            entry.getValue().setMinimumSize(minSize);
            entry.getValue().setPreferredSize(minSize); 
            gbc.gridx = gridX++;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            fieldsPanel.add(entry.getValue(), gbc);
            
            gbc.gridy++;
            gridX = 0;
        }
        
        // "Apply" button
        applySectionButton = new JButton("Apply");
        applySectionButton.setPreferredSize(new Dimension(120, 30));
        applySectionButton.setFocusPainted(false);
        applySectionButton.setMargin(new Insets(0, 0, 0, 0));
        applySectionButton.setEnabled(true); 
        applySectionButton.addActionListener(buttonAction());
        
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        fieldsPanel.add(applySectionButton, gbc);
        
        //Show/Hide fields
        headerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isVisible = !fieldsPanel.isVisible();
                fieldsPanel.setVisible(isVisible);
                headerButton.setText(isVisible ? title + " (Close)" : title);
            }
        });
                
        headerPanel.add(headerButton, BorderLayout.NORTH);
        headerPanel.add(fieldsPanel, BorderLayout.CENTER);
        sectionPanel.add(headerPanel, BorderLayout.NORTH);
    }
    
    // Action Listener for Apply Button
    private ActionListener buttonAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!applied) {
                    // Adding condition
                    enableFields(false);  
                    applySectionButton.setText("Remove");
                    headerButton.setBackground(new Color(100, 149, 237));
                    
                	Map<String,Double> parameters = new HashMap<>();
                	for (Map.Entry<String, JComponent> entry : components.entrySet()) {
            		   String chiave = entry.getKey();
            		   JComponent component = entry.getValue();
            		    
            		   Double valore = null;
            		   if (component instanceof JSpinner) {
            		       valore = ((JSpinner) component).getValue() instanceof Number ? 
            		                ((Number) ((JSpinner) component).getValue()).doubleValue() : null;
            		   } else {
            		       valore = Double.parseDouble(component.toString());
            		   }
            		    
            		   parameters.put(chiave, valore);
                       app.minilogStringData(title+" applied");
                	}
                    
                    app.applyCondition(new Condition(title,parameters));
                    applied = true;
                } else {
                    // Removing Condition
                    enableFields(true);  
                    applySectionButton.setText("Apply");
                    headerButton.setBackground(Color.DARK_GRAY);
                    app.removeCondition(title);
                    app.minilogStringData(title+" removed");
                    applied = false;
                }
            }
        };
    }
    
    // Enable/Disable fields
    private void enableFields(boolean enable) {
        for (Map.Entry<String, JComponent> entry : components.entrySet()) {
        	entry.getValue().setEnabled(enable);
        }
    }
    
    public JPanel getSectionPanel() {
        return sectionPanel;
    }
    
    public boolean isActive() {
        return applied;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void enableBox(boolean enable) {
    	enableFields(enable);
    	applySectionButton.setEnabled(enable);
    }
    
    //Set all values to 0 and remove conditions
    public void reset() {
        enableFields(true);  
        applySectionButton.setText("Apply");
        headerButton.setBackground(Color.DARK_GRAY);
        for (Map.Entry<String, JComponent> entry : components.entrySet()) {
            if (entry.getValue() instanceof JSpinner) {
                JSpinner spinner = (JSpinner) entry.getValue();
                spinner.setValue(0);  
            }
        }
        applied = false;
    }
    
    //add space to title
    private String addSpaceBeforeUpperCase(String input) {
        if (input == null || input.isEmpty()) {
            return input; 
        }
        return input.replaceAll("(?<!^)([A-Z])", " $1").trim();
    }

    //Set initial Conditions
    public void setComponents(Map<String, Double> parameters) {
        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
            String key = entry.getKey();          
            Double value = entry.getValue();      

            if (components.containsKey(key)) {
            	
                JComponent component = components.get(key);  
                applySectionButton.setText("Remove");
                headerButton.setBackground(new Color(100, 149, 237));
                app.applyCondition(new Condition(title,parameters));
                applied = true;
                if (component instanceof JSpinner) {
                    JSpinner spinner = (JSpinner) component;
                    spinner.setValue(value);  
                }
            }
        }
    }
}
