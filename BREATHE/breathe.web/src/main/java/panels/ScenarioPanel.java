package panels;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import app.App;
import data.Action;
import files.DownloadLinksArea;
import files.UploadArea;
import utils.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScenarioPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
	/*
	 * Panel to create scenario
	 */
	private App app;

	private ComboBox<String> patientFileComboBox;
	private TextField scenarioNameField;

	private Grid<Pair<Action, Integer>> actionsGrid;
	private ListDataProvider<Pair<Action, Integer>> dataProvider;

	private ArrayList<Pair<Action, Integer>> actions = new ArrayList<>();

	public ScenarioPanel(App app) {
		this.app = app;
		setSpacing(false);
		setWidth("33vw");
		getStyle().set("margin", "0px");
		getStyle().set("padding", "0px");

		//SCENARIO NAME SETUP
		scenarioNameField = new TextField("Scenario Name");
		scenarioNameField.setWidth("70%");
		
		//PATIENT NAME SETUP
		patientFileComboBox = new ComboBox<>("Patient");
		patientFileComboBox.setWidth("70%");
		String[] directories = { "../breathe.engine/states/exported/", "../breathe.engine/states/", "../breathe.engine/states/uploaded/"};
		updatePatientFiles(directories);

		Button newButton = new Button(VaadinIcon.PLUS.create(), e -> uploadNewPatient()); 

		HorizontalLayout patientLayout = new HorizontalLayout();
		patientLayout.setAlignItems(Alignment.BASELINE);
		patientLayout.add(patientFileComboBox, newButton);
		patientLayout.setWidth("70%");

	    Div fixedSizeDiv = new Div();
        fixedSizeDiv.getStyle().set("box-sizing", "border-box"); 

        VerticalLayout tableLayout = new VerticalLayout();
        tableLayout.setPadding(false);
        tableLayout.setSpacing(false);

        //TABLE SETUP
		actionsGrid = new Grid<>();
		actionsGrid.addColumn(pair -> pair.getKey().toString().split("\n")[0]).setHeader("Action")
				.setTooltipGenerator(pair -> pair.getKey().toString());

		actionsGrid.addColumn(pair -> formatTime(pair.getValue())).setHeader("Time");

		actionsGrid.setSelectionMode(Grid.SelectionMode.MULTI);

		dataProvider = new ListDataProvider<>(actions);
		actionsGrid.setDataProvider(dataProvider);
        actionsGrid.setWidth("30vw");
        actionsGrid.setHeight("50vh");
		
		Div scrollableDiv = new Div();
        scrollableDiv.getStyle().set("overflow-y", "auto"); 
        scrollableDiv.getStyle().set("scrollbar-width", "none");
        
        scrollableDiv.setHeight("50vh"); 
        scrollableDiv.add(actionsGrid); 
        fixedSizeDiv.add(scrollableDiv);
        fixedSizeDiv.setHeight("50vh");

        
        //BUTTON SETUP
		Button createScenarioButton = new Button("Create Scenario", e -> createScenario());

		Button removeActionButton = new Button(VaadinIcon.TRASH.create(), e -> removeSelectedActions());
		removeActionButton.setTooltipText("Remove Selected Actions");
		removeActionButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		
		HorizontalLayout removeButtonLayout = new HorizontalLayout();
		removeButtonLayout.add(removeActionButton);
		removeButtonLayout.setWidth("100%");
		removeButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

		HorizontalLayout createButtonLayout = new HorizontalLayout();
		createButtonLayout.add(createScenarioButton);
		createButtonLayout.setWidth("100%");
		createButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

		VerticalLayout buttonsLayout = new VerticalLayout();
		buttonsLayout.add(removeButtonLayout, createButtonLayout);
		buttonsLayout.setWidth("100%");

        VerticalLayout fieldLayout = new VerticalLayout();
        fieldLayout.setAlignItems(Alignment.CENTER);
        fieldLayout.setPadding(false);
        fieldLayout.setSpacing(false);
        fieldLayout.add(scenarioNameField, patientLayout);
        add(fieldLayout);
        add(fixedSizeDiv);
		add(buttonsLayout);
	}

	// get all patients from folder 
	private void updatePatientFiles(String[] directories) {
		List<String> patientFiles = new ArrayList<>();

		for (String dirPath : directories) {
			File dir = new File(dirPath);
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				if (files != null) {
					for (File file : files) {
						if (file.isFile() && file.getName().endsWith(".json")) {
							patientFiles.add(file.getName()); 
						}
					}
				}
			}
		}

		patientFileComboBox.setItems(patientFiles); 
	}

	private String formatTime(int seconds) {
		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		int remainingSeconds = seconds % 60;

		return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
	}

	//Add action to table
	public void addAction(Action action, int seconds) {
		Pair<Action, Integer> newAction = new Pair<>(action, seconds);
		actions.add(newAction);
		actions.sort((pair1, pair2) -> pair1.getValue().compareTo(pair2.getValue()));
		dataProvider.refreshAll();
	}
	
	//load new state file
	private void uploadNewPatient() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Select New State File");
        
        File uploadFolder = app.getFolder("states/uploaded");
        
        UploadArea upload = new UploadArea(uploadFolder);
        upload.getUploadField().addSucceededListener(e -> {
            upload.hideErrorField();
    		String[] directories = { "../breathe.engine/states/exported/", "../breathe.engine/states/", "../breathe.engine/states/uploaded/"  };
        	updatePatientFiles(directories);
        });
        
        VerticalLayout dialogLayout = new VerticalLayout(upload);

        dialog.add(dialogLayout);
        dialog.open();
	}

	 //Export scenario
	private void createScenario() {
		String scenarioName = scenarioNameField.getValue();

		if (scenarioName.isEmpty()) {
			Notification.show("Please enter a name for the scenario.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
			return;
		}

		String patientFile = patientFileComboBox.getValue();
		if (patientFile == null || patientFile.isEmpty()) {
			Notification.show("Please select a patient file.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
			return;
		}

		if (new File("../breathe.engine/states/" + patientFile).exists()) {
		    patientFile = "../breathe.engine/states/" + patientFile;
		} else if (new File("../breathe.engine/states/exported/" + patientFile).exists()) {
		    patientFile = "../breathe.engine/states/exported/" + patientFile;
		} else {
		    patientFile = "../breathe.engine/states/uploaded/" + patientFile;
		}
		
		app.createScenario(patientFile, scenarioName, actions);
		Notification.show("Scenario \"" + scenarioName + "\" created successfully.",3000,Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
		Dialog dialog = new Dialog();

		File uploadFolder = app.getFolder("scenario/exported/");
		DownloadLinksArea linksArea = new DownloadLinksArea(uploadFolder);
		VerticalLayout dialogLayout = new VerticalLayout(linksArea);

		Button closeButton = new Button("Close", e -> {
			dialog.close();
		});

		dialog.setHeaderTitle("Select File Option");
		dialog.add(dialogLayout, closeButton);

		dialog.open();
	}

	private void removeSelectedActions() {
		List<Pair<Action, Integer>> selectedActions = new ArrayList<>(actionsGrid.getSelectedItems());

		if (selectedActions.isEmpty()) {
			Notification.show("Please select actions to remove.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
			return;
		}

		actions.removeAll(selectedActions);
		dataProvider.refreshAll();
		Notification.show("Selected actions removed.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);;
	}
}
