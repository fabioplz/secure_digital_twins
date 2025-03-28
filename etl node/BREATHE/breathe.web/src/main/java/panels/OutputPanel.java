package panels;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import outputItems.InfoBox;
import outputItems.LineChart;
import outputItems.ItemDisplay;
import app.App;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OutputPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
	/*
	 * PANEL to Display Output Items
	 */
	
	private VerticalLayout mainPanel;
    private Map<String, String> chartsMap;
    private Map<String, ItemDisplay> chartPanels;
    private VerticalLayout chartsPanel;
    private FlexLayout infoBoxPanel;
    private Div scrollChartPane;

    public OutputPanel(App app) {
		this.setWidthFull();
		mainPanel = new VerticalLayout();
        mainPanel.getStyle().set("background-color", "#03070a");
		mainPanel.getStyle().set("border-radius", "8px"); 
		mainPanel.getStyle().set("margin","0px" );
		mainPanel.getStyle().set("padding","0px" );
        setSizeFull();
        getStyle().set("margin","5px" );
        getStyle().set("padding","5px" );
        
        setSpacing(false);

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
             
        chartsPanel = new VerticalLayout();
        chartsPanel.getStyle().set("padding", "10px");  

        infoBoxPanel = new FlexLayout();
        infoBoxPanel.setAlignItems(Alignment.CENTER);
        infoBoxPanel.setJustifyContentMode(JustifyContentMode.CENTER);
        
        //ADDING LINE CHARTS
        String[] chartOrder = {
            "Total Lung Volume",
            "CO2",
            "Pleth",
            "ECG"
        };
        
        int count = 0;
        LineChart oldLine = null;
        chartsPanel.setSpacing(false);
        chartsPanel.setWidth("100%");

        for (String chartName : chartOrder) {
            LineChart chart = new LineChart(chartName, chartsMap.get(chartName));
            chartPanels.put(chartName, chart);
            count++;
            if(count == 2) {
            	HorizontalLayout horizontalLayout = new HorizontalLayout(chart, oldLine);
                horizontalLayout.setSpacing(false);
            	chartsPanel.add(horizontalLayout);
            	count = 0;
            }
            oldLine = chart;
        }
        if(count == 1)
        	chartsPanel.add(new HorizontalLayout(oldLine, null));
        
        //ADDING INFO BOXES
        String[] infoOrder = {
            "Heart Rate",
            "Respiratory Rate",
            "Airway Pressure",
            "Oxygen Saturation"
        };
        
        for (String chartName : infoOrder) {
            InfoBox infoBox = new InfoBox(chartName, chartsMap.get(chartName));
            infoBox.getStyle().set("margin", "5px"); 
            chartPanels.put(chartName, infoBox);
            infoBoxPanel.add(infoBox);
        }
        
        //SET UP MAIN PANEL
        scrollChartPane = new Div(chartsPanel);
        scrollChartPane.getStyle().set("overflow-y", "auto").set("overflow-x", "hidden");
        scrollChartPane.setWidthFull();
        
        mainPanel.add(infoBoxPanel, scrollChartPane);
        mainPanel.setAlignItems(Alignment.CENTER);
        mainPanel.setJustifyContentMode(JustifyContentMode.CENTER);
        add(mainPanel);
    }

    //GRAPHIC UPDATE OF THE PANELS
    public void updateItemDisplay(Set<String> selectedCharts) { 
        chartsPanel.removeAll();
        infoBoxPanel.removeAll();

        for (String chartName : selectedCharts) {
            if (chartPanels.containsKey(chartName)) {
                if (chartPanels.get(chartName) instanceof LineChart) {
                    chartsPanel.add(chartPanels.get(chartName));
                } else if (chartPanels.get(chartName) instanceof InfoBox) {
                    infoBoxPanel.add(chartPanels.get(chartName));
                }
            }
        }

        if (infoBoxPanel.getComponentCount() == 0) {
            infoBoxPanel.setWidth("0px");
        } else {
            infoBoxPanel.setWidth("160px");
        }

        scrollChartPane.getElement().executeJs("this.scrollTop = 0");
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
        for (Map.Entry<String, ItemDisplay> entry : chartPanels.entrySet()) {
            entry.getValue().clear();
        }
    }
}
