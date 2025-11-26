package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
	
	/*
	 * MAIN CLASS TO LAUNCH THE APP
	 */
	
    public static void main(String[] args) {
    		
        SwingUtilities.invokeLater(() -> {
        	//GUI design
        	try { 
        		UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        	} catch (Exception e) {
        	    e.printStackTrace();
        	}
        	
            App app = new App();
            app.setVisible(true);
        });
    }
}