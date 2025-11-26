package interfaces;

import java.util.List;

import data.Condition;
import data.Ventilator;

public interface GuiCallback {
	
	/*
	 * INTERFACE TO LET SIMULATIONWORKER CALL GUI METHODS
	 */
	
    void stabilizationComplete(boolean enable);
    
    void logStringData(String data);
    
    void minilogStringData(String data);
    
    void logItemDisplayData(String data, double x, double y);
    
    void logPressureExternalVentilatorData(double pressure);
    
    void logVolumeExternalVentilatorData(double volume);
    
    void setCondition(List<Condition> list);

	void setVentilator(Ventilator ventilator);
    
}