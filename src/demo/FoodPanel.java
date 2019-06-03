package demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;
/** 食物特效*/
public class FoodPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.green);
		Rectangle rect = this.getBounds();
		g.drawRect(rect.x, rect.y, rect.width-2, rect.height-2);
		this.setBackground(Color.green);
	}
}
