package panels;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import app.App;

public class OutputButtonPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	/*
	 * CLASS WITH BUTTONS TO DECIDE WHAT TO DISPLAY IN OUTPUT PANEL
	 */

    private List<JToggleButton> buttons;
    private App app;

    public OutputButtonPanel(App app) {
        this.app = app;
        this.buttons = new ArrayList<>();

        this.setBackground(Color.LIGHT_GRAY);
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));

        JScrollPane scrollablePanel = new JScrollPane(this);
        scrollablePanel.setPreferredSize(new Dimension(250, 120));
        scrollablePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollablePanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.setBorder(null);
    }

    public void addOutputButton(String outputName) {
        JToggleButton button = new JToggleButton(outputName);
        button.setSelected(true);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.updateOutputDisplay(buttons);
            }
        });

        button.setToolTipText(outputName);
        buttons.add(button);
        this.add(button);
        this.revalidate();
        this.repaint();
    }
}
