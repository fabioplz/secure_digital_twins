package outputItems;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class InfoBox extends ItemDisplay {
	
	/*
	 * ITEM TO DISPLAY A SINGLE OUTPUT VALUE AND NAME
	 */
    private static final long serialVersionUID = 1L;
    
    protected Span titleLabel;
    protected Span valueLabel;

    public InfoBox(String title, String unit) {
        super(title, unit);
        
        getStyle().set("padding", "0px");
        setWidth("15vw");
        
        titleLabel = new Span(title + " (" + unit + ") ");
        titleLabel.getStyle().set("font-size", "16px");
        titleLabel.getStyle().set("color", "white"); 

        valueLabel = new Span();
        valueLabel.getStyle().set("font-size", "20px");
        valueLabel.getStyle().set("color", "white");
        
        VerticalLayout layout = new VerticalLayout(titleLabel, valueLabel);
        
        layout.getStyle().set("border", "1px solid #14dffa"); 
        layout.getStyle().set("border-radius", "8px"); 
        layout.getStyle().set("background-color", "#041e25"); 
        layout.getStyle().set("padding", "2px"); 
        layout.getStyle().set("margin", "2px");

        layout.setAlignItems(Alignment.CENTER); 
        layout.setSpacing(false);
        
        addPoint(0.00, 0.00);
        add(layout);
    }

    @Override
    public void addPoint(double x, double y) {
        if(unit!= null && unit.equals("%")) y = y*100;
        y = Math.round(y * 100.0) / 100.0; 
        valueLabel.setText(y + "");
        valueLabel.getElement().getStyle().set("font-style", "italic");  
    }

    @Override
    public void clear() {
        valueLabel.setText("0.0");
    }
}
