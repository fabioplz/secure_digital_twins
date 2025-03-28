package outputItems;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.page.Push;

@Route
@Push
public class LineChart extends ItemDisplay {
	/*
	 * Item connected to javascript to display dynamic line charts
	 */
    private static final long serialVersionUID = 1L;

    private Div chartContainer; 

    public LineChart(String title, String unit) {
        super(title, unit);
        initializeChart(title, unit); 
    }

    private void initializeChart(String title, String unit) {
    	getElement().getStyle().set("padding", "0");
    	getElement().getStyle().set("margin", "0");
    	
        chartContainer = new Div();
        chartContainer.setId(title); 
        chartContainer.getElement().getStyle().set("width", "35vw");
        chartContainer.getElement().getStyle().set("height", "34vh");
        chartContainer.getElement().getStyle().set("box-shadow", "inset 5px 5px 5px 5px #03070a");
        
        add(chartContainer);
        
        getElement().executeJs("initLineChart($0,$1)", title, unit);
    }

    @Override
    public void addPoint(double x, double y) {
        getElement().executeJs("updateLineChart($0, $1, $2)", 
            title, 
            x, 
            y);
    }

    @Override
    public void clear() {
    	getElement().executeJs("clearLineChart($0)", title);
    }
}
