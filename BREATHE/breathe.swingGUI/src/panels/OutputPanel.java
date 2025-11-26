package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import outputItems.*;
import app.App;

public class OutputPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	/*
	 * PANEL to Display Output Items
	 */
    
    private Map<String, String> chartsMap;
    
	private Map<String, ItemDisplay> chartPanels;
    public JPanel chartsPanel = new JPanel();
    JScrollPane scrollChartPane;
    JPanel infoBoxPanel;
    JScrollPane scrollInfoBoxPane;
    
    public OutputPanel(App app) {
    	this.setBackground(Color.LIGHT_GRAY);
    	this.setPreferredSize(new Dimension(650, 700));
    	
    	chartsMap = new HashMap<>();
        
    	//display ALL vitals
    	chartsMap.put("Total Lung Volume", "mL");
        chartsMap.put("ECG", "mV");
        chartsMap.put("CO2", "mmHg");
        chartsMap.put("Pleth", "mmHg");
        chartsMap.put("Heart Rate", "1/min");
        chartsMap.put("Respiratory Rate", "1/min");
        chartsMap.put("Airway Pressure", "mmHg");
        chartsMap.put("Oxygen Saturation", "%");
        
        chartPanels = new HashMap<>();
             
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        chartsPanel.setBackground(Color.BLACK);

        infoBoxPanel = new JPanel();
        infoBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        infoBoxPanel.setBackground(Color.BLACK);

        scrollInfoBoxPane = new JScrollPane(infoBoxPanel);
        scrollInfoBoxPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollInfoBoxPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollInfoBoxPane.setBorder(null);
        scrollInfoBoxPane.setPreferredSize(new Dimension(150, 300));
        
        //ADDING LINE CHARTS
        String[] chartOrder = {
        	    "Total Lung Volume",
        	    "CO2",
        	    "Pleth",
        	    "ECG"
        	};
        
        for (String chartName : chartOrder) {
            LineChart chart = new LineChart(chartName, chartsMap.get(chartName));
            chartPanels.put(chartName, chart);
            
            app.addOutputButton(chartName);
            
            chartsPanel.add(chartPanels.get(chartName));
        }
        
        //ADDING INFO BOXES
        String[] infoOrder = {
        	    "Heart Rate",
        	    "Respiratory Rate",
        	    "Airway Pressure",
        	    "Oxygen Saturation"
        	};
        
        for (String chartName : infoOrder) {
            InfoBox chart = new InfoBox(chartName, chartsMap.get(chartName));
            chart.setPreferredSize(new Dimension(150, 100));
            chartPanels.put(chartName, chart);
            
            app.addOutputButton(chartName);
            
            infoBoxPanel.add(chartPanels.get(chartName));
        }
        
        //SET UP MAIN PANEL
        scrollChartPane = new JScrollPane(chartsPanel);
        scrollChartPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollChartPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChartPane.setBorder(null);
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(scrollInfoBoxPane);
        this.add(scrollChartPane);
        this.setBackground(Color.BLACK);
    }

    
    //GRAPHIC UPDATE OF THE PANELS
    public void updateItemDisplay(List<JToggleButton> buttons) {
        chartsPanel.removeAll();
        infoBoxPanel.removeAll();

        for (int i = 0; i < buttons.size(); i++) {
            JToggleButton toggleButton = buttons.get(i);
            String chartName = toggleButton.getText();
            if (toggleButton.isSelected()) {
                if (chartPanels.get(chartName) instanceof LineChart) {
                    chartsPanel.add(chartPanels.get(chartName));
                } else if (chartPanels.get(chartName) instanceof InfoBox) {
                    infoBoxPanel.add(chartPanels.get(chartName));
                }
            }
        }

        if (infoBoxPanel.getComponentCount() == 0) {
            scrollInfoBoxPane.setPreferredSize(new Dimension(0, 0));
        } else {
        	scrollInfoBoxPane.setPreferredSize(new Dimension(150, 300));  
        }

        scrollInfoBoxPane.revalidate();
        chartsPanel.revalidate();
        infoBoxPanel.revalidate();

        scrollInfoBoxPane.repaint();
        chartsPanel.repaint();
        infoBoxPanel.repaint();
    }
    
    //ADDING VALUES depending on name
    public void addValueToItemDisplay(String chartName, double x, double y) {
        String mapChartName;

        switch (chartName) {
	        case "HeartRate":
	            mapChartName = "Heart Rate";
	            break;
	        case "AirwayPressure":
	            mapChartName = "Airway Pressure";
	            break;
	        case "RespirationRate":
	            mapChartName = "Respiratory Rate";
	            break;
	        case "OxygenSaturation":
	        	mapChartName = "Oxygen Saturation";
	            break;
	        case "TotalLungVolume":
	            mapChartName = "Total Lung Volume";
	            break;
	        case "Lead3ElectricPotential":
	            mapChartName = "ECG";
	            break;
	        case "CarbonDioxide":
	            mapChartName = "CO2";
	            break;
	        case "ArterialPressure":
	            mapChartName = "Pleth";
	            break;
	        default:
	            mapChartName = null; 
	            break;
	    }
    
        if (mapChartName != null) {
            chartPanels.get(mapChartName).addPoint(x, y);
        }
    }
    
    
    public void clearOutputDisplay() {
    	for (Map.Entry<String, ItemDisplay> chart : chartPanels.entrySet()) {
    	    chart.getValue().clear();
    	}
	}
}
