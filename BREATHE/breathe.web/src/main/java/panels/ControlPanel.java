package panels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Div;

import java.io.File;
import java.io.IOException;


import files.DownloadLinksArea;
import files.UploadArea;
import app.App;

public class ControlPanel extends HorizontalLayout {
    private static final long serialVersionUID = 1L;

	/*
	 * BUTTONS TO CONTROL THE SIMULATION
	 */
    
    private Button startButton, stopButton, exportButton, scenarioButton;
    private String uploadedFileName;
    
    private final ActionsPanel actionsPanel; 
    private final ScenarioPanel scenarioPanel;
    
    App app;

    public ControlPanel(App app) {
        this.app = app;      
        
        actionsPanel = new ActionsPanel(app, true); 
        scenarioPanel = new ScenarioPanel(app);
       
        //START SIMULATION
        startButton = new Button(VaadinIcon.PLAY.create(), e -> showStartOptions());
        startButton.setTooltipText("Start simulation");
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); 
        
        //STOP SIMULATION
        stopButton = new Button(VaadinIcon.STOP.create());
        stopButton.setTooltipText("Stop simulation");
        stopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopButton.setEnabled(false);
        stopButton.addClickListener(e -> {
            app.stopSimulation();
            enableControlStartButton(true);
        });
        
        //EXPORT SIMULATION
        exportButton = new Button(LumoIcon.DOWNLOAD.create());
        exportButton.setTooltipText("Export simulation");
        exportButton.setEnabled(false);
        exportButton.addClickListener(e -> {
        	exportSimulation();
        });
        
        //CREATE SCENARIO
        scenarioButton = new Button(VaadinIcon.CLIPBOARD_TEXT.create());
        scenarioButton.setTooltipText("Create Scenario");
        scenarioButton.addClickListener(e -> {
        	openScenarioDialog();
        });
        
        //CHANGE THEME
        Button themeButton = new Button(VaadinIcon.MOON.create());
        themeButton.getStyle().set("margin-right", "0px");
        themeButton.addClickListener(e -> {
            if (UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK)) {
                UI.getCurrent().getElement().getThemeList().remove(Lumo.DARK);
                UI.getCurrent().getElement().getThemeList().add(Lumo.LIGHT);
            } else {
                UI.getCurrent().getElement().getThemeList().remove(Lumo.LIGHT);
                UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);
            }
        });     
        
        setWidth("100%");
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");

        add(startButton, stopButton, exportButton, scenarioButton, spacer, themeButton);
    }

    //Panel to create scenario
    private void openScenarioDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Scenario");

        HorizontalLayout hl = new HorizontalLayout();
        hl.add(actionsPanel, scenarioPanel);
        dialog.add(hl);

        dialog.open();
    }

    //All options to start a simulation
    private void showStartOptions() {
    	uploadedFileName = null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Select Start Option");
        
        Button startSimulationButton = new Button("Start Simulation", e -> {
            dialog.close();
            startingSimulation();
        });

        Button startFromFileButton = new Button("Start from File", e -> {
            dialog.close();
            startingFileSimulation(); 
        });

        Button startScenarioButton = new Button("Start Scenario", e -> {
            dialog.close();
            startingScenario();
        });

        startSimulationButton.setWidth("100%");
        startFromFileButton.setWidth("100%");
        startScenarioButton.setWidth("100%");
        
        VerticalLayout dialogLayout = new VerticalLayout(startSimulationButton, startFromFileButton, startScenarioButton);
        dialog.add(dialogLayout);

        dialog.open();
    }
    
    //Standard Simulation
    private void startingSimulation() {
    	app.clearOutputDisplay();
    	app.startSimulation();
    }

    //Starting file Simulation
    private void startingFileSimulation() {
        
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Select File Option");
        
        File uploadFolder = app.getFolder("states/uploaded");
        
        UploadArea upload = new UploadArea(uploadFolder);
        upload.getUploadField().addSucceededListener(e -> {
            upload.hideErrorField();
            uploadedFileName = e.getFileName();
        });
        
        VerticalLayout dialogLayout = new VerticalLayout(upload);
        
        Button startSimulationButton = new Button("Start Simulation", e -> {
            if (uploadedFileName != null) {
                String patientFilePath = "../breathe.engine/states/uploaded/" + uploadedFileName;
                dialog.close();
                if(app.loadPatientData(patientFilePath)) 
                	app.startFromFileSimulation(patientFilePath);
            } else {
                Notification.show("Please upload a file before starting the simulation.",3000,Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
            }
        });

        dialog.add(dialogLayout, startSimulationButton);
        dialog.open();
    }
    
    //Starting from Simulation
    private void startingScenario() {
        
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Select File Option");
        
        File uploadFolder = app.getFolder("scenario/uploaded");
        
        UploadArea upload = new UploadArea(uploadFolder);
        upload.getUploadField().addSucceededListener(e -> {
            upload.hideErrorField();
            uploadedFileName = e.getFileName();
        });
        
        VerticalLayout dialogLayout = new VerticalLayout(upload);
        
        Button startScenarioButton = new Button("Start Scenario", e -> {
            if (uploadedFileName != null) {
            	try {
            		String filePath = "../breathe.engine/scenario/uploaded/" + uploadedFileName;
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode_scenario = mapper.readTree(new File(filePath));
                    String PatientFilePath = rootNode_scenario.path("EngineStateFile").asText();
                    
                    if(new File(PatientFilePath).exists() && app.loadPatientData(PatientFilePath)) {
                    	dialog.close();
                    	enableControlStartButton(false);
                    	app.startFromScenarioSimulation(filePath);
                    }
                    else
                    	Notification.show("Please upload a valid scenario file.",3000,Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                    
            	} catch (IOException ex) {
	                ex.printStackTrace();
            	}
            } else {
                Notification.show("Please upload a file before starting the simulation.",3000,Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
            }
        });

        dialog.add(dialogLayout, startScenarioButton);
        dialog.open();
    	
    }
    
    //Export Simulation
    private void exportSimulation() {
    	 String filePath = "../breathe.engine/states/exported/"+ app.getPatientName()+ ".json";
    	 
    	 int counter = 1;
         while (new File(filePath).exists()) {
             filePath = "../breathe.engine/states/exported/"+ app.getPatientName()+ " (" + counter+ ").json";
             counter++;
         }
         app.exportSimulation(filePath);
         
         Dialog dialog = new Dialog();
         
         File uploadFolder = app.getFolder("states/exported");
         DownloadLinksArea linksArea = new DownloadLinksArea(uploadFolder);
         VerticalLayout dialogLayout = new VerticalLayout(linksArea);

         Button closeButton = new Button("Close", e -> {
             dialog.close();
         });
       
         dialog.setHeaderTitle("Select File Option");
         dialog.add(dialogLayout, closeButton);
         
         dialog.open();
    }
    
    public void enableControlStartButton(boolean enable) {
        startButton.setEnabled(enable); 
        stopButton.setEnabled(!enable);
        exportButton.setEnabled(!enable);
    }
    
    public ScenarioPanel getScenarioPanel() {
    	return scenarioPanel;
    }
    
}
