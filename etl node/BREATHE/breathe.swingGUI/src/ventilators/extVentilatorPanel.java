package ventilators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import app.App;

public class extVentilatorPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private JLabel pressure, volume;
    
	// SEMechanicalVentilation (for external ventilators)
	public extVentilatorPanel(App app) {
		
		this.setLayout(new GridBagLayout());
		this.setBackground(Color.LIGHT_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;       
        
        addLabelAndField("Pressure", pressure = new JLabel(""), this, gbc);
        addLabelAndField("Volume", volume = new JLabel(""), this, gbc);
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
    

    public void setPressureLabel(Double pressure) {
    	this.pressure.setText(""+pressure);
    }
    
    public void setVolumeLabel(Double volume) {
    	this.volume.setText(""+volume);
    }
}
