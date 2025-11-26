package outputItems;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Dimension;

public class InfoBox extends ItemDisplay {
	//Item to display last value received
	
    private static final long serialVersionUID = 1L;
    
    private static final Color TEXT_COLOR = Color.WHITE;   

    public InfoBox(String title, String unit) {
        super(title, unit, new Dimension(150, 150));  
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // Add title
        if (title != null && !title.isEmpty()) {
            g2.setFont(g2.getFont().deriveFont(16f));
            g2.setColor(TEXT_COLOR);
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (getWidth() - titleWidth) / 2, 25);  // title
        }

        // Add value
        g2.setFont(g2.getFont().deriveFont(24f));
        g2.setColor(TEXT_COLOR);
        String valueStr = String.format("%.2f", currentValue);
        int valueWidth = g2.getFontMetrics().stringWidth(valueStr);
        g2.drawString(valueStr, (getWidth() - valueWidth) / 2, getHeight() / 2);  // value

        // Add unit
        if (unit != null) {
            g2.setFont(g2.getFont().deriveFont(14f));
            String unitStr = "(" + unit.toString() + ")";
            int unitWidth = g2.getFontMetrics().stringWidth(unitStr);
            g2.drawString(unitStr, (getWidth() - unitWidth) / 2, getHeight() / 2 + 30);  // unity
        }
    }

    @Override
    public void addPoint(double x, double y) {
    	if(unit.equals("%")) y = y*100;
    	currentValue = y;  
        repaint(); 
    }

    @Override
    public void clear() {
        currentValue = 0;  
        repaint(); 
    }
}
