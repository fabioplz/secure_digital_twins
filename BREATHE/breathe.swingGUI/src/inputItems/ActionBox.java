package inputItems;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.HashMap;

import app.App;
import data.Action;

public class ActionBox {
	
	/*
	 * Item for Single Action Button
	 */
    
    private JPanel sectionPanel;
    private JButton applySectionButton;
    
    private String title;
    private Map<String, JComponent> components;
    
    private JSpinner[] time = new JSpinner[3]; //0 hours, 1 minutes, 2 seconds
    
    private App app;
    
    public ActionBox(App app, String title, Map<String, JComponent> components) {
    	this.app = app;
        this.title = title;
        this.components = components;
        
        sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBackground(Color.LIGHT_GRAY);
        
        // Button with name of Action to display textfields
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.LIGHT_GRAY);
        
        JButton headerButton = new JButton(title);
        headerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerButton.setBackground(Color.DARK_GRAY);
        headerButton.setForeground(Color.WHITE);
        headerButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        headerButton.setFocusPainted(false);
        
        // Fields and Apply button
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fieldsPanel.setBackground(Color.LIGHT_GRAY);
        fieldsPanel.setVisible(false);
        
        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        int gridX = 0;
        
        // Add Labels and components
        for (Map.Entry<String, JComponent> entry : components.entrySet()) {
            JLabel label = new JLabel(addSpaceBeforeUpperCase(entry.getKey()) + ":");
            gbc.gridx = gridX++;
            fieldsPanel.add(label, gbc);
            
            gbc.gridx = gridX++;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            fieldsPanel.add(entry.getValue(), gbc);
            
            gbc.gridy++;
            gridX = 0;
        }
        
        //Time for Scenario
        JLabel timeLabel = new JLabel("Time (hh:mm:ss)");

        time[0] = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1)); 
        time[1] = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1)); 
        time[2] = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1)); 

        for (JComponent spinner : time) {
            spinner.setPreferredSize(new Dimension(60, 30));
        }

        JButton plusButton = new JButton("+");
        plusButton.setPreferredSize(new Dimension(40, 30));
        plusButton.setFocusPainted(false);
        plusButton.setToolTipText("Add to Scenario");
        
        plusButton.addActionListener(e -> {
            int totalSeconds = getTotalTimeInSeconds();
            
            Map<String, Double> parameters = new HashMap<>();

            for (Map.Entry<String, JComponent> entry : components.entrySet()) {
                String key = entry.getKey();
                JComponent component = entry.getValue();

                if (component instanceof JSpinner) {
                    JSpinner spinner = (JSpinner) component;
                    Double value = ((Number) spinner.getValue()).doubleValue(); 
                    parameters.put(key, value);
                }

            }
            
            app.addActiontoScenario(new Action(title, parameters), totalSeconds);
            app.rightTabbedPane.setSelectedIndex(1);
        });

        JPanel timePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTime = new GridBagConstraints();
        gbcTime.insets = new Insets(0, 2, 0, 0);

        gbcTime.gridy = 0;
        gbcTime.anchor = GridBagConstraints.WEST;
        
        gbcTime.gridx = 0; 
        timePanel.add(timeLabel, gbcTime);
        
        gbcTime.gridx++; 
        timePanel.add(time[0], gbcTime);

        gbcTime.gridx++;
        timePanel.add(time[1], gbcTime);

        gbcTime.gridx++;
        timePanel.add(time[2], gbcTime);

        gbcTime.gridx++;
        timePanel.add(plusButton, gbcTime);
        gbcTime.gridx++;
        
        timePanel.setBackground(Color.LIGHT_GRAY);
        gbc.gridx--;
        fieldsPanel.add(timePanel, gbc);
        
        // "Apply" Button
        applySectionButton = new JButton("Apply");
        applySectionButton.setPreferredSize(new Dimension(120, 30));
        applySectionButton.setFocusPainted(false);
        applySectionButton.setMargin(new Insets(0, 0, 0, 0));
        applySectionButton.setEnabled(false); 
        
        applySectionButton.addActionListener(buttonAction());

        gbc.gridx = 0;
        gbc.gridwidth = 2; 
        gbc.gridy++;
        fieldsPanel.add(applySectionButton, gbc);
        
        headerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isVisible = !fieldsPanel.isVisible();
                fieldsPanel.setVisible(isVisible);
                headerButton.setText(isVisible ? title + " (Close)" : title);
                sectionPanel.revalidate();
                sectionPanel.repaint();
            }
        });
        
        headerPanel.add(headerButton, BorderLayout.NORTH);
        headerPanel.add(fieldsPanel, BorderLayout.CENTER);
        sectionPanel.add(headerPanel, BorderLayout.NORTH);
    }
    
    // ActionListener for Apply Button
    private ActionListener buttonAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
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
            	}
                
                app.applyAction(new Action(title,parameters));
            }
        };
    }
    
    //get total time setted as seconds
    public int getTotalTimeInSeconds() {
        int hours = (Integer) time[0].getValue();
        int minutes = (Integer) time[1].getValue();
        int seconds = (Integer) time[2].getValue();
        
        // Convertion in seconds
        return hours * 3600 + minutes * 60 + seconds;
    }
    
    public JPanel getSectionPanel() {
        return sectionPanel;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void enableBox(boolean enable) {
    	applySectionButton.setEnabled(enable);
    }
    
    //add spaces to Title
    private String addSpaceBeforeUpperCase(String input) {
        if (input == null || input.isEmpty()) {
            return input; 
        }
        return input.replaceAll("(?<!^)([A-Z])", " $1").trim();
    }
}