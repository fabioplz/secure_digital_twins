package panels;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;

import app.App;
import data.Ventilator;
import utils.VentilationMode;
import ventilators.cpapVentilatorPanel;
import ventilators.pcVentilatorPanel;
import ventilators.vcVentilatorPanel;

@Route("ventilators")
public class VentilatorsPanel extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
	private pcVentilatorPanel pcPanel;
    private cpapVentilatorPanel cpapPanel;
    private vcVentilatorPanel vcPanel;

    private VentilationMode activeMode = VentilationMode.PC; // Default mode is PC
    private VentilationMode selectedMode = VentilationMode.PC; // Default mode is PC
    
    private Button pcButton, cpapButton, vcButton;
    private Button connectButton, disconnectButton;
    private FlexLayout ventilatorLayout;
    
    App app;

    public VentilatorsPanel(App app) {
    	this.app = app;
        setSpacing(false);
        getStyle().set("margin","0px" );
        getStyle().set("padding","0px" );
        
        pcButton = new Button("PC");
        cpapButton = new Button("CPAP");
        vcButton = new Button("VC");
        
        pcButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Create the panels for each ventilation mode
        pcPanel = new pcVentilatorPanel(app);
        cpapPanel = new cpapVentilatorPanel(app);
        vcPanel = new vcVentilatorPanel(app);

        ventilatorLayout = new FlexLayout();

        // Initially show only the PC panel
        ventilatorLayout.add(pcPanel);
        
        // Listener per il pulsante "Patient"
        pcButton.addClickListener(event -> {
        	updateVentilatorView(VentilationMode.PC);
            pcButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            cpapButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            vcButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });

        // Listener per il pulsante "Conditions"
        cpapButton.addClickListener(event -> {
        	updateVentilatorView(VentilationMode.CPAP);
            pcButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            cpapButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            vcButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });
        
        vcButton.addClickListener(event -> {
        	updateVentilatorView(VentilationMode.VC);
            pcButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            cpapButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            vcButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });

        // Aggiungi i pulsanti e il layout del contenuto al layout principale
        HorizontalLayout topArea = new HorizontalLayout();
        topArea.setWidthFull(); 
        topArea.setJustifyContentMode(JustifyContentMode.CENTER); 
        
        pcButton.setMaxWidth("20%");
        vcButton.setMaxWidth("20%");
        cpapButton.setMaxWidth("20%");

        topArea.add(pcButton,cpapButton ,vcButton);

        // Connect and disconnect buttons
        connectButton = new Button("Connect", e -> connectVentilator());
        disconnectButton = new Button("Disconnect", e -> disconnectVentilator());
        
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(false);
        
        connectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        disconnectButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        // Buttons layout
        HorizontalLayout buttonLayout = new HorizontalLayout(connectButton, disconnectButton);
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); 
		buttonLayout.setWidthFull(); 
		
        add(topArea, ventilatorLayout, buttonLayout);
    }

    private void updateVentilatorView(VentilationMode mode) {
        selectedMode = mode;
        ventilatorLayout.removeAll(); // Remove all panels
        switch (mode) {
            case PC:
                ventilatorLayout.add(pcPanel); // Show PC panel
                break;
            case CPAP:
                ventilatorLayout.add(cpapPanel); // Show CPAP panel
                break;
            case VC:
                ventilatorLayout.add(vcPanel); // Show VC panel
                break;
			case EXT:
				break;
			default:
				break;
	    }
    }

    private void connectVentilator() {
        if (selectedMode != null) {
        	app.connectVentilator();
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            activeMode = selectedMode;
            setEnableApplyButton(selectedMode, true);
            disconnectButton.setText("Disconnect "+ selectedMode);
        }
    }

	private void disconnectVentilator() {
		app.disconnectVentilator();
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        setEnableApplyButton(activeMode, false);
        disconnectButton.setText("Disconnect");
    }
    

    public Ventilator getCurrentVentilator() {
        switch (activeMode) {
            case PC:
                return new Ventilator(VentilationMode.PC, pcPanel.getData());
            case CPAP:
                return new Ventilator(VentilationMode.CPAP, cpapPanel.getData());
            case VC:
                return new Ventilator(VentilationMode.VC, vcPanel.getData());
            default:
                return null;
        }
    }
    
    private void setEnableApplyButton(VentilationMode mode, boolean enable) {
        switch (mode) {
            case PC:
                pcPanel.setEnableApplyButton(enable);
                break;
            case CPAP:
                cpapPanel.setEnableApplyButton(enable);
                break;
            case VC:
                vcPanel.setEnableApplyButton(enable);
                break;
            default:
                break;
        }
    }
    
    public void resetButton() {
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(false);
        setEnableApplyButton(activeMode, false);
        disconnectButton.setText("Disconnect");
    }
    
    public void manageConnectButton(boolean b) {
    	connectButton.setEnabled(b);
    }

	public void setEnableConnectButton(boolean b) {
		connectButton.setEnabled(b);
	}

    //load Ventilator Data from File (if connected i.e. after an export)
    public void setVentilatorsData(Ventilator ventilator) {
    	switch (ventilator.getMode()) {
        case PC:
            pcPanel.setVentilator(ventilator);
            activeMode = VentilationMode.PC;
            break;
        case CPAP:
            cpapPanel.setVentilator(ventilator);
            activeMode = VentilationMode.VC;
            break;
        case VC:
            vcPanel.setVentilator(ventilator);
            activeMode = VentilationMode.VC;
            break;
        default:
            break;
    	}
    	setEnableApplyButton(activeMode, true);
        disconnectButton.setEnabled(true);
        disconnectButton.setText("Disconnect " + activeMode);
    }
    
}
