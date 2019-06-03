package demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
/** 蛇身特效*/
public class SnakePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static List<Color> colorList = new ArrayList<Color>();
	{
		colorList.add(Color.green);
		colorList.add(Color.black);
		colorList.add(Color.blue);
		colorList.add(Color.cyan);
		colorList.add(Color.lightGray);
		colorList.add(Color.orange);
		colorList.add(Color.yellow);
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.blue);
		Rectangle rect = this.getBounds();
		g.drawRect(rect.x, rect.y, rect.width-2, rect.height-2);
		Random r = new Random();
		this.setBackground(colorList.get(r.nextInt(colorList.size())));
	}
}
