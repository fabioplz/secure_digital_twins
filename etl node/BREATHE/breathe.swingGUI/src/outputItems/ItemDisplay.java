package outputItems;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;

public abstract class ItemDisplay extends JPanel {
    private static final long serialVersionUID = 1L;
    
    /*
     * Abstract class for output items
     */
    
    protected String title;
    protected String unit;
    protected double currentValue;  

    public ItemDisplay(String title, String unit, Dimension preferredSize) {
        this.title = title;
        this.unit = unit;
        this.currentValue = 0.0;  
        setPreferredSize(preferredSize);
        setBackground(Color.BLACK);  
    }

    public void updateValue(double newValue) {
        this.currentValue = newValue;
        repaint();
    }
    
    public abstract void clear();

    public abstract void addPoint(double x, double y);
}