package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import app.App;
import data.Ventilator;
import utils.VentilationMode;
import ventilators.*;

public class VentilatorsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private pcVentilatorPanel pcPanel;
    private cpapVentilatorPanel cpapPanel;
    private vcVentilatorPanel vcPanel;
    private extVentilatorPanel extPanel;

    private VentilationMode activeMode = null;

    JToggleButton pcToggleButton, cpapToggleButton, vcToggleButton, extToggleButton;
    JButton connectButton, disconnectButton;
    JPanel buttonPanel;

    public VentilatorsPanel(App app) {
        this.setBackground(Color.LIGHT_GRAY);
        this.setPreferredSize(new Dimension(550, 650));

        CardLayout ventilatorCardLayout = new CardLayout();
        JPanel ventilatorsCardPanel = new JPanel(ventilatorCardLayout);

        pcPanel = new pcVentilatorPanel(app);
        cpapPanel = new cpapVentilatorPanel(app);
        vcPanel = new vcVentilatorPanel(app);
        extPanel = new extVentilatorPanel(app);

        ventilatorsCardPanel.add(pcPanel, "PC");
        ventilatorsCardPanel.add(cpapPanel, "CPAP");
        ventilatorsCardPanel.add(vcPanel, "VC");
        ventilatorsCardPanel.add(extPanel, "EXT");

        pcToggleButton = new JToggleButton("PC");
        cpapToggleButton = new JToggleButton("CPAP");
        vcToggleButton = new JToggleButton("VC");
        extToggleButton = new JToggleButton("EXT");
        Dimension buttonSize = new Dimension(125, 30);
        
        pcToggleButton.setPreferredSize(buttonSize);
        cpapToggleButton.setPreferredSize(buttonSize);
        vcToggleButton.setPreferredSize(buttonSize);
        extToggleButton.setPreferredSize(buttonSize);
        
        pcToggleButton.setSelected(true);

        ButtonGroup ventilatorGroup = new ButtonGroup();
        ventilatorGroup.add(pcToggleButton);
        ventilatorGroup.add(cpapToggleButton);
        ventilatorGroup.add(vcToggleButton);
        ventilatorGroup.add(extToggleButton);

        JPanel ventilatorsRadioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,0, 10));
        
        ventilatorsRadioPanel.setBackground(Color.LIGHT_GRAY);
        ventilatorsRadioPanel.add(pcToggleButton);
        ventilatorsRadioPanel.add(cpapToggleButton);
        ventilatorsRadioPanel.add(vcToggleButton);
        ventilatorsRadioPanel.add(extToggleButton);

        pcToggleButton.addActionListener(e -> {
            ventilatorCardLayout.show(ventilatorsCardPanel, "PC");
        });
        cpapToggleButton.addActionListener(e -> {
            ventilatorCardLayout.show(ventilatorsCardPanel, "CPAP");
        });
        vcToggleButton.addActionListener(e -> {
            ventilatorCardLayout.show(ventilatorsCardPanel, "VC");
        });
        extToggleButton.addActionListener(e -> {
            ventilatorCardLayout.show(ventilatorsCardPanel, "EXT");
        });

        connectButton = new JButton("Connect");
        connectButton.setEnabled(false);
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(e -> {
            activeMode = getCurrentMode();
            app.connectVentilator();
            connectButton.setEnabled(false);
            setEnableApplyButton(getCurrentMode(), true);
            disconnectButton.setEnabled(true);
            disconnectButton.setText("Disconnect " + getCurrentMode());
            buttonPanel.revalidate();
            buttonPanel.repaint();
        });

        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        disconnectButton.setFocusPainted(false);
        disconnectButton.setBackground(new Color(255, 59, 48));
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.addActionListener(e -> {
            app.disconnectVentilator();
            connectButton.setEnabled(true);
            setEnableApplyButton(activeMode, false);
            disconnectButton.setEnabled(false);
            disconnectButton.setText("Disconnect");
            activeMode = null;
        });

        
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        
        connectButton.setPreferredSize(buttonSize);
        buttonPanel.add(connectButton);
        
        disconnectButton.setPreferredSize(buttonSize);
        buttonPanel.add(disconnectButton);

        
        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BorderLayout());
        borderPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        borderPanel.add(ventilatorsCardPanel, BorderLayout.CENTER);
        borderPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(ventilatorsRadioPanel, BorderLayout.NORTH);
        this.add(borderPanel, BorderLayout.CENTER);
    }

    private VentilationMode getCurrentMode() {
        if (pcToggleButton.isSelected()) {
            return VentilationMode.PC;
        } else if (cpapToggleButton.isSelected()) {
            return VentilationMode.CPAP;
        } else if (vcToggleButton.isSelected()) {
            return VentilationMode.VC;
        } else if (extToggleButton.isSelected()) {
            return VentilationMode.EXT;
        }
        return null;
    }

    public Ventilator getCurrentVentilator() {
        switch (activeMode) {
            case PC:
                return new Ventilator(VentilationMode.PC, pcPanel.getData());
            case CPAP:
                return new Ventilator(VentilationMode.CPAP, cpapPanel.getData());
            case VC:
                return new Ventilator(VentilationMode.VC, vcPanel.getData());
            case EXT:
                return new Ventilator(VentilationMode.EXT);
            default:
                return null; 
        }
    }

    public void resetButton() {
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(false);
        if(activeMode!=null) setEnableApplyButton(activeMode, false);
        disconnectButton.setText("Disconnect");
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


    public void manageConnectButton() {
    	if(activeMode == null)
    		connectButton.setEnabled(true);
    	else
    		connectButton.setEnabled(false);
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
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    
    public void setEnableConnectButton(boolean enable) {
        connectButton.setEnabled(enable);
    }
    
    public void setEnableDisconnectButton(boolean enable) {
        disconnectButton.setEnabled(enable);
    }

    public void setEXTPressureLabel(Double pressure) {
        extPanel.setPressureLabel(pressure);
    }

    public void setEXTVolumeLabel(Double volume) {
        extPanel.setVolumeLabel(volume);
    }

}
