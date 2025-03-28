package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import app.App;

public class LogPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
    JTextArea resultArea = new JTextArea();
    
    public LogPanel(App app) {
    	
    	this.setBackground(Color.LIGHT_GRAY);
    	this.setPreferredSize(new Dimension(550, 600));
    	
		// Text area that updates with every new data retrieved from the engine
	    resultArea.setEditable(false);
	    resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
	    resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    resultArea.setBackground(Color.WHITE);
	    
	    JScrollPane scrollLogPane = new JScrollPane(resultArea);
	    scrollLogPane.setPreferredSize(new Dimension(550, 600));
	    scrollLogPane.setBackground(Color.LIGHT_GRAY);
	    
	    this.add(scrollLogPane);
    }
    
    public void append(String log) {
    	resultArea.append(log);
    	resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }
}
