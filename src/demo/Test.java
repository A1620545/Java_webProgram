package demo;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
/**
 * 贪吃蛇单机版
 *
 */
public class Test {
	/** 食物集合*/
	private static List<JDialog> foodList = new ArrayList<JDialog>();
	/** true:关闭食物生产,并隐藏所有食物*/
	private static Boolean shutFood = false;
	/** 蛇构造*/
	private static List<JDialog> snake = new ArrayList<JDialog>();
	/** 屏幕大小*/
	private static Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
	public static void show(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		//创建一个带标题的窗口
		JFrame frame = new JFrame("welcome");
		frame.setLayout(null);
		//设置窗口关闭按钮为程序退出(默认不退出程序,只隐藏窗口)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//div
		JPanel pan = new JPanel();
		pan.setSize(50, 50);
		pan.setBounds(220, 400, 50, 30);
		//新建按钮
		JButton button = new JButton("开始");
		button.setLocation(20, 30);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start(frame);
				shutFood = false;
				showFood();
				
			}
		});
		//添加按钮
		pan.add(button);
		frame.add(pan);
		pan.setVisible(true);
		//设置窗口大小
		frame.setSize(500, 500);
		//设置窗口位置，null为中心
		frame.setLocationRelativeTo(null);
		//显示窗口
		frame.setVisible(true);
		
	}
	
	public static void start(JFrame frame){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//先最小化frame窗口
				frame.setVisible(false);
				JDialog dialog = new JDialog();
				SnakePanel pan = new SnakePanel();
				dialog.add(pan);
				dialog.add(new JLabel("...蛇头."));
				dialog.setLocationRelativeTo(null);
				dialog.setUndecorated(true);
				dialog.setVisible(true);
				dialog.setSize(50, 50);
				//添加蛇头
				snake.add(dialog);
				dialog.addWindowListener(new WindowListener() {
					
					@Override
					public void windowOpened(WindowEvent e) {}
					
					@Override
					public void windowIconified(WindowEvent e) {}
					
					@Override
					public void windowDeiconified(WindowEvent e) {}
					
					@Override
					public void windowDeactivated(WindowEvent e) {}
					
					@Override
					public void windowClosing(WindowEvent e) {
						dialog.setVisible(false);
						frame.setVisible(true);
						shutFood = true;
						for(JDialog jd : snake){
							jd.setVisible(false);
						}
						snake.removeAll(snake);
					}
					
					@Override
					public void windowClosed(WindowEvent e) {}
					
					@Override
					public void windowActivated(WindowEvent e) {}
				});
				dialog.addWindowFocusListener(new WindowFocusListener() {
					
					@Override
					public void windowLostFocus(WindowEvent e) {
						dialog.requestFocus();
						
					}
					
					@Override
					public void windowGainedFocus(WindowEvent e) {}
				});
				
				dialog.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e) {
						Rectangle old = dialog.getBounds();
						switch(e.getKeyChar()){
						case 'w':
							JDialog jd = new JDialog();
							jd.setBounds((int)old.getX(), (int)old.getY()-50, (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jd)){
								jd.dispose();
								break;
							}
							jd.dispose();
							move();
							dialog.setBounds((int)old.getX(), (int)old.getY()-50, (int)old.getWidth(), (int)old.getHeight());
							checkLocation(dialog);
							break;
						case 's':
							JDialog jdi = new JDialog();
							jdi.setBounds((int)old.getX(), (int)old.getY()+50, (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jdi)){
								jdi.dispose();
								break;
							}
							jdi.dispose();
							move();
							dialog.setBounds((int)old.getX(), (int)old.getY()+50, (int)old.getWidth(), (int)old.getHeight());
							checkLocation(dialog);
							break;
						case 'a':
							JDialog jdia = new JDialog();
							jdia.setBounds((int)old.getX()-50, (int)old.getY(), (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jdia)){
								jdia.dispose();
								break;
							}
							jdia.dispose();
							move();
							dialog.setBounds((int)old.getX()-50, (int)old.getY(), (int)old.getWidth(), (int)old.getHeight());
							checkLocation(dialog);
							break;
						case 'd':
							JDialog jdial= new JDialog();
							jdial.setBounds((int)old.getX(), (int)old.getY()-50, (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jdial)){
								jdial.dispose();
								break;
							}
							jdial.dispose();
							move();
							dialog.setBounds((int)old.getX()+50, (int)old.getY(), (int)old.getWidth(), (int)old.getHeight());
							checkLocation(dialog);
							break;
						case KeyEvent.VK_ESCAPE:
							dialog.setVisible(false);
							shutFood = true;
							frame.setVisible(true);
							for(JDialog j : snake){
								j.setVisible(false);
							}
							snake.removeAll(snake);
							break;
						default:
							break;
						}
					}

					@Override
					public void keyTyped(KeyEvent e) {}

					@Override
					public void keyReleased(KeyEvent e) {}
				});
			}
		}).start();
	}
	
	/** 显示食物给贪吃蛇*/
	public static void showFood(){
		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(!shutFood){
						System.out.println("检查当前食物个数为："+foodList.size());
						if(foodList.size()<10){
							System.out.println("创建食物");
							JDialog dia = new JDialog();
							Random r = new Random();
							SnakePanel pan = new SnakePanel();
							dia.add(pan);
							dia.setLocation(r.nextInt(windowSize.width-20), r.nextInt(windowSize.height-20));
							dia.setUndecorated(true);
							dia.setSize(50, 50);
							dia.setFocusable(false);
							foodList.add(dia);
							dia.addWindowListener(new WindowListener() {
								@Override
								public void windowOpened(WindowEvent e) {}
								
								@Override
								public void windowIconified(WindowEvent e) {}
								
								@Override
								public void windowDeiconified(WindowEvent e) {}
								
								@Override
								public void windowDeactivated(WindowEvent e) {}
								
								@Override
								public void windowClosing(WindowEvent e) {
									System.out.println("食物消失");
									boolean b = foodList.remove(dia);
									System.out.println("集合中除掉食物:"+(b==true?"成功":"失败")+"当前食物个数为:"+foodList.size());
								}
								
								@Override
								public void windowClosed(WindowEvent e) {}
								
								@Override
								public void windowActivated(WindowEvent e) {}
							});
							dia.setVisible(true);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					System.out.println("游戏结束,清空食物");
					for(JDialog jd : foodList){
						jd.setVisible(false);
					}
				}
			}).start();
	}
	/**检查是否吃到食物 */
	public static void checkLocation(JDialog dialog){
		for(int i = foodList.size()-1;i>=0;i--){
			JDialog jd = foodList.get(i);
			Rectangle foodBounds = jd.getBounds();
			Rectangle bounds = dialog.getBounds();
			boolean b = bounds.intersects(foodBounds);
			if(b){
				System.out.println("---------------吃到食物--------------------");
				jd.setVisible(false);
				foodList.remove(jd);
				eatFood(jd);
			}
			
		}
	}
	
	/** 吃到食物，连接蛇尾*/
	public static void eatFood(JDialog jd){
		//取蛇尾的最后一节
		JDialog end = snake.get(snake.size()-1);
		//取位置
		Rectangle endBounds = end.getBounds();
		int x = endBounds.x;
		int y = endBounds.y;
		jd.setBounds(x-endBounds.width, y, endBounds.width, endBounds.height);
		jd.setVisible(true);
		snake.add(jd);
	}
	
	/** 移动*/
	public static void move(){
		System.out.println("当前蛇长"+snake.size());
		if(snake.size()<=1){
			return;
		}
		for(int i = snake.size()-1 ; i>0; i--){
			snake.get(i).setBounds(snake.get(i-1).getBounds());
		}
	}
	/** 检测是否到达屏幕边缘*/
	public static boolean checkWindow(JDialog  jd){
		int height = windowSize.height;
		int width = windowSize.width;
		if(jd.getLocation().x >= width-10){
			return true;
		}
		if(jd.getLocation().x <= -10){
			return true;
		}
		if(jd.getLocation().y <= -10){
			return true;
		}
		if(jd.getLocation().y >= height-10){
			return true;
		}
		return false;
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				show();
			}
		});
	}
}
