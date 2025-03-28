package app;

import com.kitware.pulse.utilities.JNIBridge;

public class Initializer {
	
    public static void initilizeJNI() {
    	JNIBridge.initialize("../breathe.engine/");
    }

}
