package demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.alibaba.fastjson.JSONObject;

/**
 * 贪吃蛇服务端(局域网版)
 * 贪吃蛇联网版启动入口
 * main启动
 *
 */
public class SnakeManager {
	/** 存放多个用户的容器*/
	private static ConcurrentHashMap<String,Socket> clientList = new ConcurrentHashMap<String, Socket>();
//	private static Map<String,Socket> clientList = new HashMap<String, Socket>();
	/** 玩家位置信息map*/
	private static Map<String,List<Snake>> location_map = new HashMap<String, List<Snake>>();
	/** 食物集合*/
	private static List<Snake> foodList = new ArrayList<Snake>();
	/** 关闭食物生产, 默认生产*/
	private static boolean shutFood = false;
	/** 关闭食物同步*/
	private static boolean syFood = false;
	private static List<String> gamerList = new ArrayList<String>();
	private static ServerSocket server = null;
	/** 记录房间ip*/
	private static String ip = null;
	/** 创建房间*/
	public static void creatServer()throws Exception{
		 // 监听指定的端口
	    int port = 55533;
	    server = new ServerSocket(port);
	    boolean b = false;
	    while(!b){
	    	Socket socket = server.accept();
	    	new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						listenMessage(socket);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
	    }
	}
	
	private static void doTask(String str, Socket socket) throws IOException {
		// 建立好连接后，从socket中获取输入流，并建立缓冲区进行读取
		Message message = null;
		try {
			message = JSONObject.parseObject(str, Message.class);
		} catch (Exception e) {
			return;
		}
    	System.out.println("服务器收到消息:"+JSONObject.toJSONString(message));
    	if(message.getName().equals(Instruction.name)){
    		clientList.put(message.getIns(), socket);
    		gamerList.add(message.getIns());
    		System.out.println("玩家"+message.getIns()+"进入房间");
    		System.out.println("当前玩家个数为:"+checkGamerCount());
    	}
    	/** 其他玩家加入*/
    	if(message.getName().equals(Instruction.other)){
    		System.out.println("玩家"+message.getIns()+"进入房间");
    		Enumeration<Socket> elements = clientList.elements();
    		//分发消息给其他人展示
    		while(elements.hasMoreElements()){
    			Socket next = elements.nextElement();
    			List<String> list = new ArrayList<String>();
    			list.add(message.getIns());
    			Message mess = new Message(Instruction.addGamer, "");
    			mess.setGamers(list);
    			sendMessage(next, JSONObject.toJSONString(mess));
    		}
    		
    		
    		clientList.put(message.getIns(), socket);
    		gamerList.add(message.getIns());
    		Message me = new Message(Instruction.ip, ip);
    		sendMessage(socket, JSONObject.toJSONString(me));
    		System.out.println("当前玩家个数为:"+checkGamerCount());
    	}
    	/** 获取房间ip*/
    	if(message.getName().equals(Instruction.ip)){
    		ip = message.getIns();
    	}
    	/** 发送房间ip*/
    	if(message.getName().equals(Instruction.getIp)){
    		Enumeration<String> keys = clientList.keys();
    		Message mess = new Message(Instruction.gamers, "");
    		List<String> list = new ArrayList<String>();
    		while(keys.hasMoreElements()){
    			String ele = keys.nextElement();
    			list.add(ele);
    		}
    		mess.setGamers(list);
    		sendMessage(socket, JSONObject.toJSONString(mess));
    	}
    	//保存玩家蛇的位置
    	if(message.getName().equals(Instruction.snake_head)){
    		location_map.put(message.getId(), message.getSnake());
    	}
    	
    	//正式开始游戏
    	if(message.getName().equals(Instruction.start)){
    		Enumeration<Socket> elements = clientList.elements();
    		//分发消息给其他人展示蛇位置
    		while(elements.hasMoreElements()){
    			Socket next = elements.nextElement();
    			Message mess = new Message(Instruction.start, "");
    			mess.setMap(location_map);
    			sendMessage(next, JSONObject.toJSONString(mess));
    		}
    		try {
				Thread.sleep(1000);
				showFood();
				//如果参与人数大于1，则检测胜负
				if(checkGamerCount()>1){
					winner();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	//接收玩家发来的定时移动命令
    	if(message.getName().equals(Instruction.move)){
    		//保存玩家蛇构造位置
    		location_map.put(message.getIns(), message.getSnake());
    		Enumeration<Socket> elements = clientList.elements();
    		//分发消息给其他人展示蛇位置
    		while(elements.hasMoreElements()){
    			Socket next = elements.nextElement();
    			Message mess = new Message(Instruction.move, "");
    			mess.setMap(location_map);
    			sendMessage(next, JSONObject.toJSONString(mess));
    		}
    	}
    	//有玩家吃到食物
    	if(message.getName().equals(Instruction.eatFood)){
    		checkLocation(message.getFood(), message.getIns());
    	}
    	//检查是否碰到其他玩家
//    	if(message.getName().equals(Instruction.checkOtherGamer)){
//    		Set<String> keySet = location_map.keySet();
//    		//玩家蛇头位置
//    		Snake head = message.getFood();
//    		JDialog jhead = new JDialog();
//    		jhead.setBounds(head.getX(),head.getY(),head.getWidth(),head.getHeight());
//    		for(String gamer : keySet){
//    			for(Snake sn : location_map.get(gamer)){
//    				JDialog jd = new JDialog();
//    				jd.setBounds(sn.getX(),sn.getY(),sn.getWidth(),sn.getHeight());
//    				boolean b = jhead.getBounds().intersects(jd.getBounds());
//    				if(b){
//    					Message me = new Message(Instruction.checkOtherGamer, message.getIns());
//    					Enumeration<Socket> elements = clientList.elements();
//    		    		//分发消息给其他人有玩家out了
//    		    		while(elements.hasMoreElements()){
//    		    			Socket next = elements.nextElement();
//    		    			sendMessage(next, JSONObject.toJSONString(me));
//    		    		}
//    		    		//清除该玩家
//    		    		location_map.put(gamer, null);
//    				}
//    			}
//    		}
//    		jhead.dispose();
//    	}
    	
    	
    	
    	//有玩家被淘汰
    	if(message.getName().equals(Instruction.gamer_out)){
    		Enumeration<Socket> elements = clientList.elements();
    		//分发消息给其他人有玩家淘汰了
    		while(elements.hasMoreElements()){
    			Socket next = elements.nextElement();
    			Message mess = new Message(Instruction.gamer_out, message.getIns());
    			sendMessage(next, JSONObject.toJSONString(mess));
    		}
    		gamerList.remove(message.getIns());
    	}
    	//玩家退出
    	if(str .equals(Instruction.exit)){
    		exitGamer("");
    	}
    	
    	//取消房间
    	if(message.getIns().equals(Instruction.back)){
    		show();
    		close(server);
    		System.out.println("房间关闭");
    	}
	}
	/** 发送指令*/
	public static void sendMessage(Socket socket,String text){
		try {
			text = text.trim();
			 socket.getOutputStream().write(text.getBytes("UTF-8"));
			 socket.getOutputStream().flush();
			 System.out.println("服务器发出消息:"+text);
		} catch (IOException e) {
			e.printStackTrace();
			Set<Entry<String, Socket>> set = clientList.entrySet();
			Iterator<Entry<String, Socket>> iterator = set.iterator();
			while(iterator.hasNext()){
				Entry<String, Socket> next = iterator.next();
				if(next.getValue().equals(socket)){
					clientList.remove(next.getKey());
				}
			}
		}
	}
	/** 监听指令*/
	public static  void listenMessage(Socket socket) throws Exception{
		byte[] bytes = new byte[102400];
		int len;
		String sb = "";
		while ((len = socket.getInputStream().read(bytes)) != -1) {
			sb = (new String(bytes, 0, len, "UTF-8"));
			String[] sbs = sb.split("\\}\\{");
			if(sbs.length >= 2) {
				sbs[0] += "}";
				sbs[sbs.length-1] = "{"+sbs[sbs.length-1];
				for(int i=1; i<sbs.length-1; i++) {
					sbs[i] = "{"+sbs[i]+"}";
				}
			}
			
			for(String json : sbs) {
				doTask(json, socket);
			}
		}
	}
	/** 游戏界面*/
	public static void show(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		//创建一个带标题的窗口
		JFrame frame = new JFrame("GAME===不服你咬我===GAME");
		frame.setLayout(null);
		//设置窗口关闭按钮为程序退出(默认不退出程序,只隐藏窗口)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//div
		JPanel pan = new JPanel();
		pan.setSize(200, 200);
		pan.setBounds(200, 100, 120, 50);
		//建房按钮
		JButton button = new JButton("创建房间");
		JPanel pan1 = new JPanel();
		pan1.setSize(200, 200);
		pan1.setBounds(50, 220, 400, 50);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pan.setVisible(false);
				pan1.setVisible(false);
				try {
					frame.setVisible(false);
					SnakeGamer.creatHome();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		JPanel pan3 = new JPanel();
		JLabel la = new JLabel("ip错误,无此房间");
		pan3.add(la);
		pan3.setVisible(false);
		frame.add(pan3);
		pan3.setBounds(145, 200, 120, 23);
		
		
		//加入别的房间按钮
		JButton button1 = new JButton("加入房间");
		JTextField text = new JTextField("192.168.1.186");
		text.setLayout(null);
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip_text = text.getText();
				System.out.println(ip_text);
				frame.setVisible(false);
				if(!SnakeGamer.addHome(ip_text)){
					frame.setVisible(true);
					pan3.setVisible(true);
				}
			}
		});
		text.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				pan3.setVisible(false);
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		text.setBounds(0, 50, 120, 40);
		button1.setBounds(0, 150, 120, 40);
		//用于游戏规则相关
		JPanel pan_read = new JPanel();
		pan_read.setBounds(50, 300, 400, 100);
		//测试
//		pan1.setBackground(Color.blue);
//		pan.setBackground(Color.orange);
//		pan_read.setBackground(Color.green);
		JTextField read = new JTextField("游戏控制键: w↑ s↓ a← d→");
		read.setBackground(Color.red);
//		read.setLayout(null);
		read.setEnabled(false);
		read.setBounds(0, 100, 300, 40);
		JTextField read1 = new JTextField("蛇身率先达到50胜出,或场上仅剩1个玩家时胜出");
//		read1.setLayout(null);
		read1.setBackground(Color.red);
		read1.setEnabled(false);
		read1.setBounds(0, 140, 300, 40);
		//添加按钮
		pan1.add(text);
		pan.add(button);
		pan1.add(button1);
		pan_read.add(read);
		pan_read.add(read1);
		frame.add(pan);
		frame.add(pan1);
		frame.add(pan_read);
		pan.setVisible(true);
		//设置窗口大小
		frame.setSize(500, 500);
		//设置窗口位置，null为中心
		frame.setLocationRelativeTo(null);
		//设置图标，不起作用
		Toolkit tk=Toolkit.getDefaultToolkit();
		Image image=tk.createImage("logo.png");
		//显示窗口
		frame.setIconImage(image);
		frame.setVisible(true);
	}
	
	/** 关闭房间*/
	public static void close(ServerSocket server) throws IOException{
		server.close();
	}
	/** 生产食物*/
	public static void showFood(){
		//同步食物位置
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!syFood){
					try {
						Thread.sleep(1000);
						//每生产一个食物，同步给其他玩家
						Message m = new Message(Instruction.SynchronizeFood, "");
						m.setSnake(foodList);
						//分发消息给其他人展示食物位置
						Collection<Socket> coll = clientList.values();
						for(Socket so : coll){
							sendMessage(so, JSONObject.toJSONString(m));
						}
						System.out.println("同步食物位置中");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		
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
							dia.setLocation(r.nextInt(windowSize.width-80), r.nextInt(windowSize.height-80));
							dia.setSize(50, 50);
							Snake food = new Snake(dia.getBounds().x, dia.getBounds().y, dia.getBounds().width, dia.getBounds().height);
							foodList.add(food);
							Message message = new Message(Instruction.showFood,"");
							message.setFood(food);
							//分发消息给其他人展示食物位置
							Collection<Socket> values = clientList.values();
							for(Socket so : values){
								sendMessage(so, JSONObject.toJSONString(message));
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}).start();
	}
	
	
	/**检查是否吃到食物 */
	public static void checkLocation(Snake food,String name){
		for(int i = foodList.size()-1;i>=0;i--){
			Snake sn = foodList.get(i);
//			JDialog jd = new JDialog();
//			jd.setBounds(sn.getX(), sn.getY(), sn.getWidth(), sn.getHeight());
//			Rectangle foodBounds = jd.getBounds();
//			JDialog dialog = new JDialog();
//			dialog.setBounds(food.getX(), food.getY(), food.getWidth(), food.getHeight());
//			Rectangle bounds = dialog.getBounds();
//			boolean b = bounds.intersects(foodBounds);
			
			if(sn.getX() == food.getX() && sn.getY() == food.getY()){
				System.out.println("---------------"+name+"吃到食物--------------------");
				System.out.println("当前食物个数---------------------"+foodList.size());
				//将食物添加到对应玩家的蛇构造
				Set<String> keySet = location_map.keySet();
				for(String gamer_name : keySet){
					if(gamer_name.equals(name)){
						List<Snake> list = location_map.get(gamer_name);
						list.add(sn);
					}
				}
				
				
				foodList.remove(sn);
				Message message = new Message(Instruction.eatFood, name);
				message.setFood(food);
				Collection<Socket> values = clientList.values();
				for(Socket so : values){
					sendMessage(so, JSONObject.toJSONString(message));
				}
//				eatFood(jd);
			}
		}
	}
	
	
	/** 检查玩家个数*/
	public static int checkGamerCount(){
		int size = gamerList.size();
		return size;
	}
	/** 检测是否产生胜利者*/
	public static void winner(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(checkGamerCount() == 1 ){
						Enumeration<Socket> elements = clientList.elements();
						long now = System.currentTimeMillis();
						while(elements.hasMoreElements()){
							Socket next = elements.nextElement();
							Message mess = new Message(Instruction.winner, gamerList.get(0));
							sendMessage(next, JSONObject.toJSONString(mess));
							shutFood = true;
							syFood = true;
							System.out.println("胜负已分，关闭食物生产");
							if(System.currentTimeMillis() - now > 10000){
								return;
							}
						}
					}
					//如果其中一个玩家的蛇构造达先到50，则胜出
					Set<String> keySet = location_map.keySet();
					for(String winner : keySet){
						if(location_map.get(winner).size()>=50){
							Enumeration<Socket> elements = clientList.elements();
							long now = System.currentTimeMillis();
							while(elements.hasMoreElements()){
								Socket next = elements.nextElement();
								Message mess = new Message(Instruction.winner, winner);
								sendMessage(next, JSONObject.toJSONString(mess));
								shutFood = true;
								syFood = true;
								System.out.println("玩家"+winner+":蛇身率先达到50，赢得胜利");
								if(System.currentTimeMillis() - now > 10000){
									return;
								}
							}
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	/** 玩家退出*/
	public static void exitGamer(String gamer){
		
	}
	public static void main(String[] args) throws Exception {
		SnakeManager.show();
	}
}
