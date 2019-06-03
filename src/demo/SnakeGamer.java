package demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
//import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.alibaba.fastjson.JSONObject;
/**
 * 贪吃蛇客户端(局域网版)
 * 本类没有入口
 *
 */
public class SnakeGamer {
	private static Socket socket;
//	private static BufferedReader read;
	private static PrintStream write;
	private static String name;
	private static String ip;
	private static JFrame frame;
	private static JPanel panel;
	private static JFrame otherframe;
	private static JPanel otherpanel;
	/** 显示玩家蛇身长度容器*/
	private static JDialog showCount;
	/** 显示玩家蛇身长度内容体*/
	private static JLabel showSnakeCount;
	/** 是否被淘汰*/
	private static boolean isout = false;
	/** 蛇头*/
	private static JDialog head;
	/** 蛇整体构造(流传输用)*/
	private static List<Snake> snake_all = new ArrayList<Snake>();
	/** 蛇整体构造*/
	private static List<JDialog> snake_all_dj = new ArrayList<JDialog>();
	/** 关闭食物显示*/
	private static boolean shutFood = false;
	/** 其他蛇构造*/
	private static Map<String,List<JDialog>> otherSnakeMap = new HashMap<String,List<JDialog>>();
	/** 食物集合*/
	private static List<JDialog> foodList = Collections.synchronizedList(new ArrayList<JDialog>());
	/** 屏幕大小*/
	private static Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
	//'建','房','i','骄','傲','考','虑','过','技','术','开','发','吉','林','省','附','近','都','是','分','开','两','地','哦',
	/** 随机玩家名字*/
	private static char[] chars = {
			'1','2','3','4','5','6','7','8','9','0','a','s','d','f','g','h','j','k','e','r','t','y','u','i','o','p','z','x','c'};

	public static void connect() throws Exception {
		// 要连接的服务端IP地址和端口
		String host = "127.0.0.1";
		int port = 55533;
		// 与服务端建立连接
		socket = new Socket(host, port);
//		read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		write = new PrintStream(socket.getOutputStream());
		// 建立连接后获得输出流
		Message message = new Message(Instruction.name, "fz");
		name = "fz";
		sendMessage(message);
		listenMessage();
		addGamer(panel, name);
	}

	/** 加入房间 */
	public static boolean addHome(String host) {
		int port = 55533;
		// 与服务端建立连接
		try {
			socket = new Socket(host, port);
//			read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			write = new PrintStream(socket.getOutputStream());
			listenMessage();
			String gamer_name = "";
			for(int i =0; i<3;i++){
				gamer_name += chars[new Random().nextInt(chars.length)] ;
			}
			name = gamer_name;
			Message message = new Message(Instruction.other,gamer_name);
			sendMessage(message);
		} catch (Exception e) {
			socket = null;
			return false;
		}
		return true;
	}

	/**
	 * 发送指令
	 * 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static synchronized void sendMessage(Message message){
		String json = JSONObject.toJSONString(message);
		write.print(json);
		write.flush();
		System.out.println("客户端发送消息" + json);
	}

	/** 监听消息 */
	public static void listenMessage() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						byte[] bytes = new byte[10240];
						int len;
						String sb = "";
						while ((len = socket.getInputStream().read(bytes)) != -1) {
							sb = (new String(bytes, 0, len, "UTF-8"));
							doTask(sb);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	/** 业务处理 */
	public static void doTask(String str) {
		System.out.println("客户端接收消息" + str);
		Message message = null;
		try {
			message = JSONObject.parseObject(str, Message.class);
		} catch (Exception e) {
			return;
		}
		/** 进入房间 */
		if (message.getName().equals(Instruction.ip)) {
			ip = message.getIns();
			JFrame.setDefaultLookAndFeelDecorated(true);
			otherframe = new JFrame("GAME===不服你咬我===GAME");
			otherframe.setVisible(false);
			otherframe.setLayout(null);
			// 设置窗口关闭按钮为程序退出(默认不退出程序,只隐藏窗口)
			otherframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// 设置窗口大小
			otherframe.setSize(500, 500);
			// 设置窗口位置，null为中心
			otherframe.setLocationRelativeTo(null);
			otherframe.setVisible(true);

			JLabel text = new JLabel("您以进入本房间");
			JLabel text1 = null;
			if (!str.equals("") && str != null) {
				text1 = new JLabel("本房间ip为:" + ip);
			}
			JPanel pan = new JPanel();
			pan.setBounds(150, 30, 200, 50);
			pan.add(text);
			pan.add(text1);
			pan.setVisible(true);
			otherframe.add(pan);
			otherframe.setVisible(true);
			Message m = new Message(Instruction.getIp, "");
			sendMessage(m);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			head = new JDialog();
			SnakePanel pann = new SnakePanel();
			pann.add(new JLabel("玩家"+name));
			head.add(pann);
			head.setLocationRelativeTo(null);
			head.setUndecorated(true);
			head.setVisible(true);
			head.setSize(50, 50);
			Random r = new Random();
			head.setLocation(r.nextInt(windowSize.width-20), r.nextInt(windowSize.height-20));
			//添加蛇头到蛇构造集合
			Snake sn = new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height);
			snake_all.add(sn);
			//添加到所有玩家蛇构造集合
//			List<JDialog> li = new ArrayList<JDialog>();
//			li.add(head);
//			otherSnakeMap.put(name, li);
			snake_all_dj.add(head);
			
			
			Message mess = new Message(Instruction.snake_head, "");
			List<Snake> list = new ArrayList<Snake>();
			Snake snake = new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height);
			list.add(snake);
			mess.setSnake(list);
			//发送蛇头名称
			mess.setId(name);
			//发送蛇头信息给服务器同步
			sendMessage(mess);
			
		}
		if (message.getName().equals(Instruction.gamers)) {
			otherpanel = new JPanel();
			otherframe.add(otherpanel);
			otherpanel.setVisible(true);
			otherpanel.setBounds(10, 100, 450, 300);
			for (String s : message.getGamers()) {
				if(s!="" || !s.equals("")){
					addGamer(otherpanel, s);
				}
			}
		}
		/** 添加玩家图标*/
		if (message.getName().equals(Instruction.addGamer)){
			if(otherpanel!=null){
				addGamer(otherpanel, message.getGamers().get(0));
			}
			if(panel!=null){
				addGamer(panel, message.getGamers().get(0));
			}
		}
		//准备就绪,同步位置
		if(message.getName().equals(Instruction.start)){
			Map<String, List<Snake>> map = message.getMap();
			Set<String> keySet = map.keySet();
			List<JDialog> other = new ArrayList<JDialog>();
			for(String str1 : keySet){
				List<Snake> list = map.get(str1);
				Snake snake = list.get(0);
				if(str1.equals(name)) {
					continue;
				}
				JDialog j = new JDialog();
				SnakePanel pan = new SnakePanel();
				JLabel name_label = new JLabel(str1);
				pan.add(name_label);
				name_label.setForeground(Color.red);
				j.add(pan);
				j.setLocationRelativeTo(null);
				j.setUndecorated(true);
				j.setFocusable(false);
//				j.setSize(50, 50);
//				j.setBackground(snake.getColor());
				j.setVisible(true);
				j.setBounds(snake.getX(), snake.getY(), snake.getWidth(), snake.getHeight());
				other.add(j);
				otherSnakeMap.put(str1, other);
			}
			if(frame!=null){
				frame.setVisible(false);
			}
			if(otherframe!=null){
				otherframe.setVisible(false);
			}
			//操作
			operation();
			initShowCount();
//			sendLocation();
		}
		
		//接收到服务器发来的玩家移动信息
		if(message.getName().equals(Instruction.move)){
			//同步其他玩家位置
			Map<String, List<Snake>> map = message.getMap();
			Set<String> keySet = map.keySet();
			for(String str1 : keySet){
				if(str1.equals(name)) {
					continue;
				}
				List<Snake> server = map.get(str1);
				List<JDialog> local = otherSnakeMap.get(str1);
//				if(local.size()<=server.size()){
//					for(int i = 0;i<server.size();i++){
//						try {
//							local.get(i).setBounds(server.get(i).getX(), server.get(i).getY(), server.get(i).getWidth(), server.get(i).getHeight());
//						} catch (Exception e) {
//							//TODO
//							JDialog jd = new JDialog();
//							local.add(jd);
//						}
//					}
//					
//				}
				int i=0;
				for(; i<server.size(); i++) {
					if(i > local.size()-1) {
						JDialog j = new JDialog();
						SnakePanel bp = new SnakePanel();
						j.add(bp);
						j.setUndecorated(true);
						j.setFocusable(false);
						j.setSize(50, 50);
						j.setVisible(true);
						local.add(j);
					}
					local.get(i).setBounds(server.get(i).getX(), server.get(i).getY(), server.get(i).getWidth(), server.get(i).getHeight());
				}
				for(; i<local.size(); ){
					JDialog jd = local.remove(i);
					jd.setVisible(false);
					jd.dispose();
				}
			}
			
			
			
			
//			//先处理本地存储的其他玩家信息
//			Set<String> key = otherSnakeMap.keySet();
//			for(String s : key){
//				List<JDialog> list = otherSnakeMap.get(s);
//				for(JDialog jd : list){
//					jd.setVisible(false);
//					jd.dispose();
//				}
//			}
//			
//			Map<String, List<Snake>> map = message.getMap();
//			//处理服务器发来的最新的其他玩家信息
//			List<JDialog> other = new ArrayList<JDialog>();
//			Set<String> keySet = map.keySet();
//			for(String str1 : keySet){
//				if(str1.equals(name)) {
//					continue;
//				}
//				
//				List<Snake> list = map.get(str1);
//				for(Snake snake:list){
//					JDialog j = new JDialog();
//					SnakePanel pan = new SnakePanel();
//					pan.add(new JLabel("玩家"+str1));
//					j.add(pan);
//					j.setUndecorated(true);
//					j.setFocusable(false);
////					j.setSize(50, 50);
////					j.setBackground(snake.getColor());
//					j.setVisible(true);
//					j.setBounds(snake.getX(), snake.getY(), snake.getWidth(), snake.getHeight());
//					other.add(j);
//				}
//				//添加其他玩家信息
//				otherSnakeMap.put(str1, other);
//			}
		}
		//展示食物
		if(message.getName().equals(Instruction.showFood)){
			if(shutFood){
				return;
			}
			Snake food = message.getFood();
			FoodPanel pan = new FoodPanel();
			JDialog jd = new JDialog();
			jd.setSize(50,50);
			jd.setLocation(food.getX(), food.getY());
			jd.add(pan);
			jd.setBackground(Color.green);
			jd.setUndecorated(true);
			jd.setFocusable(false);
			jd.setVisible(true);
			foodList.add(jd);
		}
		
		//同步食物位置
		if(message.getName().equals(Instruction.SynchronizeFood)){
			List<Snake> snakes = message.getSnake();
			int i = 0;
			for(; i<snakes.size(); i++) {
				if(foodList.size() <= i) {
					JDialog jd = new JDialog();
					FoodPanel pan = new FoodPanel();
					jd.setSize(50,50);
					jd.add(pan);
					jd.setUndecorated(true);
					jd.setFocusable(false);
					jd.setVisible(true);
					foodList.add(jd);
				}
				foodList.get(i).setBounds(snakes.get(i).getRect());
			}
			
			while(i < foodList.size()) {
				JDialog jd = foodList.remove(i);
				jd.setVisible(false);
				jd.dispose();
			}
			
//			//将服务器食物位置与本地比较
////			for(int i=0;i<snakes.size();i++){
////				Snake snake = snakes.get(0);
////				JDialog jd = new JDialog();
////				jd.setBounds(snake.getX(), snake.getY(), snake.getWidth(), snake.getHeight());
////				//是否包含相同食物位置
////				boolean has = false;
////				for(int z = 0;z<foodList.size();z++){
////					JDialog jdi = foodList.get(z);
////					//如果包含和服务器位置相同的食物,调整位置
////					if(jdi.getBounds().getX() == jd.getBounds().getX() && jdi.getBounds().getY() == jd.getBounds().getY()){
////						JDialog copy = foodList.get(z);
////						foodList.add(i, jdi);
////						foodList.add(z,copy);
////						has = true;
////						break;
////					}
////				}
////				if(!has){
////					//如果不包含，则新增加一个
////					RedPanel pan = new RedPanel();
////					jd.setSize(50,50);
////					jd.add(pan);
////					jd.setUndecorated(true);
////					jd.setFocusable(false);
////					jd.setVisible(true);
////					foodList.add(foodList.size(), foodList.get(i));
////					foodList.add(i, jd);
////				}
////				
////			}
////			//去除本地有但服务器上没有的食物
////			for(int i= snakes.size()-1;i<foodList.size();i++){
////				foodList.get(i).setVisible(false);
////				foodList.get(i).dispose();
////			}
//			
//			
//			
//			
//			
//			
//			List<JDialog> old = new ArrayList<JDialog>();
//			
//			
//			for(Snake s : snakes){
//				JDialog jd = new JDialog();
//				jd.setBounds(s.getX(), s.getY(), s.getWidth(), s.getHeight());
//				RedPanel pan = new RedPanel();
//				jd.setSize(50,50);
//				jd.add(pan);
//				jd.setUndecorated(true);
//				jd.setFocusable(false);
//				jd.setVisible(true);
//				old.add(jd);
//				
////				for(JDialog jdi : foodList){
////					if(jdi.getBounds().getX() == jd.getBounds().getX() && jdi.getBounds().getY() == jd.getBounds().getY()){
////						old.add(jdi);
////					}
////				}
////				RedPanel pan = new RedPanel();
////				jd.setSize(50,50);
////				jd.add(pan);
////				jd.setUndecorated(true);
////				jd.setFocusable(false);
////				jd.setVisible(true);
////				old.add(jd);
//				
//			}
//			for(JDialog jdi : foodList){
//				jdi.setVisible(false);
//				jdi.dispose();
//			}
//			foodList.removeAll(foodList);
//			foodList.addAll(old);
		}
		
		
		//有玩家吃到食物
		if(message.getName().equals(Instruction.eatFood)){
			if(message.getIns().equals(name)){
				//获取最后一节
				Snake snake = snake_all.get(snake_all.size()-1);
				JDialog end = new JDialog();
				end.setBounds(snake.getX(), snake.getY(), snake.getWidth(), snake.getHeight());
				JDialog jd = new JDialog();
				SnakePanel pann = new SnakePanel();
				jd.add(pann);
				jd.setBackground(head.getBackground());
				jd.setLocationRelativeTo(null);
				jd.setUndecorated(true);
				jd.setVisible(true);
				
				Rectangle endBounds = end.getBounds();
				int x = endBounds.x;
				int y = endBounds.y;
				jd.setBounds(x-endBounds.width, y, endBounds.width, endBounds.height);
				jd.setVisible(true);
				snake_all.add(new Snake(jd.getBounds().x, jd.getBounds().y, jd.getBounds().width, jd.getBounds().height));
				snake_all_dj.add(jd);
				updateShowCount();
//				return;
			} 
			else {
				//为吃到食物玩家的蛇构造添加一节
				addSnake(message.getIns());
				updateShowCount();
			}
			//被吃的食物位置
			Snake food = message.getFood();
			for(int i = foodList.size()-1;i>=0;i--){
				JDialog j = foodList.get(i);
				if(j.getBounds().x == food.getX() && j.getBounds().y == food.getY()){
					j.setVisible(false);
					j.dispose();
					foodList.remove(j);
					break;
				}
			}
		}
		//有玩家out
		if(message.getName().equals(Instruction.checkOtherGamer)){
			String ins = message.getIns();
			//如果是自己淘汰了
			if(ins.equals(name)){
				//弹出提示框
				JDialog out = new JDialog();
				FoodPanel pan = new FoodPanel();
				out.add(pan);
				out.setBounds(windowSize.width/2, windowSize.height/2, 120, 60);
				out.add(new JLabel("玩家"+ins+"被淘汰!!!!"));
				out.setUndecorated(true);
				out.setFocusable(false);
				out.setVisible(true);
				return;
			}
			//如果是别人淘汰了
			Set<String> keySet = otherSnakeMap.keySet();
			for(String s : keySet){
				if(s.equals(ins)){
					List<JDialog> list = otherSnakeMap.get(s);
					for(JDialog jd : list){
						jd.setVisible(false);
						jd.dispose();
					}
					//弹出提示框
					JDialog out = new JDialog();
					FoodPanel pan = new FoodPanel();
					out.add(pan);
					out.setBounds(windowSize.width/2, windowSize.height/2, 120, 60);
					out.add(new JLabel("玩家"+ins+"被淘汰!!!!"));
					out.setUndecorated(true);
					out.setFocusable(false);
					out.setVisible(true);
					list.removeAll(list);
				}
			}
		}
		//有玩家被淘汰(上面的那个有点问题)
		if(message.getName().equals(Instruction.gamer_out)){
			String ins = message.getIns();
			Set<String> keySet = otherSnakeMap.keySet();
			for(String key : keySet){
				if(key.equals(name)){
					return;
				}
				if(key.equals(ins)){
					List<JDialog> list = otherSnakeMap.get(ins);
					for(JDialog jd : list){
						jd.setVisible(false);
						jd.dispose();
					}
				}
			}
			otherSnakeMap.remove(ins);
			new Thread(new Runnable() {
				@Override
				public void run() {
					JDialog jd = new JDialog();
					jd.setBounds(windowSize.width/2, windowSize.height/2, 250, 30);
					jd.add(new JLabel("玩家:"+ins+"被淘汰"));
					jd.setUndecorated(true);
					jd.setFocusable(false);
					jd.setVisible(true);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					jd.setVisible(false);
					jd.dispose();
				}
			}).start();
		}
		
		//胜利者
		if(message.getName().equals(Instruction.winner)){
			String winnerStr = "玩家:"+message.getIns()+"胜利!!!!";
			if(message.getIns().equals(name)){
				winnerStr = "你没看错，我们赢了";
			}
			isout = true;
			shutFood = true;
			JDialog jd = new JDialog();
			jd.setBounds(windowSize.width/3, windowSize.height-800, 500, 100);
			Font font = new Font("宋体", Font.BOLD, 50);
			JLabel lable = new JLabel(winnerStr);
			lable.setFont(font);
			jd.add(lable);
			jd.setUndecorated(true);
			jd.setFocusable(false);
			jd.setVisible(true);
			shutDownFood();
		}
	}
	/** 为吃到食物的玩家增加一节*/
	public static void addSnake(String gamer_name){
		List<JDialog> list = otherSnakeMap.get(gamer_name);
		//最后一节的位置
		JDialog last = list.get(list.size()-1);
		FoodPanel pan = new FoodPanel();
		JDialog jd = new JDialog();
		jd.setSize(50,50);
		jd.setUndecorated(true);
		Rectangle endBounds = last.getBounds();
		int x = endBounds.x;
		int y = endBounds.y;
		jd.setBounds(x-endBounds.width, y, endBounds.width, endBounds.height);
		jd.setVisible(true);
		jd.add(pan);
		list.add(jd);
	}
	
	/** 最后一次自身位置 */
	private static Message message = null;
	
	/** 定时发送蛇构造位置*/
	public static void sendLocation(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					//定时15毫秒
					try {
						Thread.sleep(12);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(message != null) {
						Message temp = message;
						message = null;
						sendMessage(temp);
					}
				}
			}
		}).start();
	}
	/** 开始游戏 */
	public static void start() throws Exception {
		connect();
	}

	/**
	 * 创建房间操作
	 * 
	 * @throws Exception
	 */
	public static void creatHome() throws Exception {
		JFrame.setDefaultLookAndFeelDecorated(true);
		//创建一个带标题的窗口
		frame = new JFrame("GAME===不服你咬我===GAME");
		frame.setLayout(null);
		//设置窗口关闭按钮为程序退出(默认不退出程序,只隐藏窗口)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		//设置窗口位置，null为中心
		frame.setLocationRelativeTo(null);
		//设置图标，不起作用
		Toolkit tk=Toolkit.getDefaultToolkit();
		Image image=tk.createImage("logo.png");
		//显示窗口
		frame.setIconImage(image);
		frame.setVisible(true);
		
		
		
		JLabel text = new JLabel("等待玩家进入...");
		InetAddress add = null;
		try {
			add = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String str = add.getHostAddress();
		JLabel text1 = null;
		if (!str.equals("") && str != null) {
			text1 = new JLabel("本房间ip为:" + str);
		}
		JPanel pan = new JPanel();
		pan.setBounds(150, 30, 200, 50);
		pan.add(text);
		pan.add(text1);
		pan.setVisible(true);
		frame.add(pan);
		// 开始游戏
		JPanel start = new JPanel();
		start.setBounds(380, 400, 120, 30);
		start.setVisible(true);
		JPanel back = new JPanel();
		back.setBounds(10, 400, 120, 30);
		back.setVisible(true);
		JButton st = new JButton("开始游戏");
		st.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Message message = new Message(Instruction.start, "");
				sendMessage(message);
			}
		});
		JButton st1 = new JButton("返回");
		st1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				try {
					Message message = new Message(name, Instruction.back);
					write.print(JSONObject.toJSONString(message));
					write.flush();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		
		back.add(st1);
		start.add(st);
		frame.add(start);
		frame.add(back);
		// 玩家展示
		panel = new JPanel();
		panel.setBounds(10, 100, 450, 300);
		frame.add(panel);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					SnakeManager.creatServer();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}).start();
		SnakeGamer.start();
		Message message = new Message(Instruction.ip, str);
		sendMessage(message);
		Thread.sleep(1000);
		//生成蛇头
		head = new JDialog();
		SnakePanel pann = new SnakePanel();
		JLabel name_label = new JLabel("玩家:"+name);
		name_label.setForeground(Color.red);
		pann.add(name_label);
		head.add(pann);
		head.setLocationRelativeTo(null);
		head.setUndecorated(true);
		head.setSize(50, 50);
		Random r = new Random();
		//随机蛇头位置
		head.setLocation(r.nextInt(windowSize.width-20), r.nextInt(windowSize.height-20));
		//添加蛇头到蛇构造集合
		Snake sn = new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height);
		snake_all.add(sn);
		head.setVisible(true);
		Message mess = new Message(Instruction.snake_head, "");
		List<Snake> list = new ArrayList<Snake>();
		Snake snake = new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height);
		list.add(snake);
		mess.setSnake(list);
		//发送蛇头名称
		mess.setId(name);
		//发送蛇头信息给服务器同步
		sendMessage(mess);
		snake_all_dj.add(head);
		
		
		//添加到所有玩家蛇构造集合
//		List<JDialog> li = new ArrayList<JDialog>();
//		li.add(head);
//		otherSnakeMap.put(name, li);
	}

	/** 添加玩家 */
	public static void addGamer(JPanel panel, String gamer) {
		JLabel name = new JLabel("玩家:" + gamer);
		Component coms[] = panel.getComponents();
		int num = coms.length;
		int row = panel.getHeight() / 30;
		panel.add(name);
		name.setBounds((num / row) * 60, (num % row) * 30, 120, 20);
	}
	/** 玩家操作*/
	public static void operation() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//始终获取焦点
				head.addWindowFocusListener(new WindowFocusListener() {
					@Override
					public void windowLostFocus(WindowEvent e) {
						head.requestFocus();
					}
					@Override
					public void windowGainedFocus(WindowEvent e) {}
				});
				
				//操作
				head.addKeyListener(new KeyListener() {
					
					@Override
					public void keyTyped(KeyEvent e) {
					}
					
					@Override
					public void keyReleased(KeyEvent e) {
					}
					
					@Override
					public void keyPressed(KeyEvent e) {
						Rectangle old = head.getBounds();
						System.out.println(e.getKeyChar());
						switch(e.getKeyCode()){
						case KeyEvent.VK_UP:
						case KeyEvent.VK_W:
							if(isout){
								break;
							}
							JDialog jd = new JDialog();
							jd.setBounds((int)old.getX(), (int)old.getY()-50, (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jd)){
								jd.dispose();
								break;
							}
							jd.dispose();
							move();
							head.setBounds((int)old.getX(), (int)old.getY()-50, (int)old.getWidth(), (int)old.getHeight());
//							Message mw = new Message(Instruction.checkOtherGamer, name);
//							mw.setFood(new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height));
//							sendMessage(mw);
							checkCollision();
							checkFood();
							break;
						case KeyEvent.VK_DOWN:
						case KeyEvent.VK_S:
							if(isout){
								break;
							}
							JDialog jdi = new JDialog();
							jdi.setBounds((int)old.getX(), (int)old.getY()+50, (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jdi)){
								jdi.dispose();
								break;
							}
							jdi.dispose();
							move();
							head.setBounds((int)old.getX(), (int)old.getY()+50, (int)old.getWidth(), (int)old.getHeight());
//							Message ms = new Message(Instruction.checkOtherGamer, name);
//							ms.setFood(new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height));
//							sendMessage(ms);
							checkCollision();
							checkFood();
							break;
						case KeyEvent.VK_LEFT:
						case KeyEvent.VK_A:
							if(isout){
								break;
							}
							JDialog jdia = new JDialog();
							jdia.setBounds((int)old.getX()-50, (int)old.getY(), (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jdia)){
								jdia.dispose();
								break;
							}
							jdia.dispose();
							move();
							head.setBounds((int)old.getX()-50, (int)old.getY(), (int)old.getWidth(), (int)old.getHeight());
//							Message ma = new Message(Instruction.checkOtherGamer, name);
//							ma.setFood(new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height));
//							sendMessage(ma);
							checkCollision();
							checkFood();
							break;
						case KeyEvent.VK_RIGHT:
						case KeyEvent.VK_D:
							if(isout){
								break;
							}
							JDialog jdial = new JDialog();
							jdial.setBounds((int)old.getX()+50, (int)old.getY(), (int)old.getWidth(), (int)old.getHeight());
							if(checkWindow(jdial)){
								jdial.dispose();
								break;
							}
							jdial.dispose();
							move();
							head.setBounds((int)old.getX()+50, (int)old.getY(), (int)old.getWidth(), (int)old.getHeight());
//							Message md = new Message(Instruction.checkOtherGamer, name);
//							md.setFood(new Snake(head.getBounds().x, head.getBounds().y, head.getBounds().width, head.getBounds().height));
//							sendMessage(md);
							checkCollision();
							checkFood();
							break;
						//ESC按键
						case KeyEvent.VK_ESCAPE:
							head.setVisible(false);
//							shutFood = true;
							if(frame!=null){
								frame.setVisible(true);
							}
							if(otherframe!=null){
								otherframe.setVisible(true);
							}
//							for(JDialog jd : snake){
//								jd.setVisible(false);
//							}
//							snake.removeAll(snake);
							break;
						default:
							break;
						}
						
						Message message = new Message(Instruction.move,name);
						snake_all.get(0).setX((int)head.getBounds().getX());
						snake_all.get(0).setY((int)head.getBounds().getY());
						snake_all.get(0).setWidth((int)head.getBounds().getWidth());
						snake_all.get(0).setHeight((int)head.getBounds().getHeight());
//						snake_all.get(0).setColor(head.getBackground());
						message.setSnake(snake_all);
						sendMessage(message);
					}
				});
				
			}
		}).start();
	}
	/** 检查是否碰到食物*/
	public static void checkFood(){
		for(int i = foodList.size()-1;i>=0;i--){
			JDialog jd = foodList.get(i);
			Rectangle foodBounds = jd.getBounds();
			Rectangle bounds = head.getBounds();
			boolean b = bounds.intersects(foodBounds);
			if(b){
				jd.setVisible(false);
				Message message = new Message(Instruction.eatFood, name);
				message.setFood(new Snake(jd.getBounds().x, jd.getBounds().y, jd.getBounds().width, jd.getBounds().height));
				sendMessage(message);
			}
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
	/** 移动*/
	public static void move(){
		System.out.println("当前蛇长:"+name+"="+snake_all.size());
		if(snake_all.size()<=1){
			return;
		}
		for(int i = snake_all_dj.size()-1 ; i>0; i--){
			snake_all_dj.get(i).setBounds(snake_all_dj.get(i-1).getBounds());
		}
		for(int i = snake_all.size()-1 ; i>0; i--){
			snake_all.get(i).setX(snake_all.get(i-1).getX());
			snake_all.get(i).setY(snake_all.get(i-1).getY());
			snake_all.get(i).setWidth(snake_all.get(i-1).getWidth());
			snake_all.get(i).setHeight(snake_all.get(i-1).getHeight());
		}
	}
	/** 本地碰撞检测*/
	public static void checkCollision(){
		Set<String> keySet = otherSnakeMap.keySet();
		for(String other : keySet){
			List<JDialog> list = otherSnakeMap.get(other);
			for(JDialog jd : list){
				if(head.getBounds().intersects(jd.getBounds())){
					//弹出提示框
					JDialog out = new JDialog();
					FoodPanel pan = new FoodPanel();
					out.add(pan);
					out.setBounds(windowSize.width/2, windowSize.height/2, 250, 30);
					out.add(new JLabel("您碰撞到了--"+other+",按ESC退出"));
					out.setUndecorated(true);
					out.setFocusable(false);
					out.requestFocus();
					out.setVisible(true);
					shutDownFood();
					Message message = new Message(Instruction.gamer_out, name);
					sendMessage(message);
				}
			}
		}
	}
	/** 关闭食物*/
	public static void shutDownFood(){
		shutFood = true;
		isout = true;
		for(JDialog jd : foodList){
			jd.setVisible(false);
			jd.dispose();
		}
	}
	/** 初始化showcount容器*/
	public static void initShowCount(){
		showCount = new JDialog();
		showCount.setBounds(800, 400, 200, 300);
		showCount.setUndecorated(true);
		//设置不可移动
		showCount.setEnabled(false);
		showCount.setFocusable(false);
		
		
		//设置透明
		showCount.getRootPane ().setOpaque (false);
		showCount.getContentPane ().setBackground (new Color (0, 0, 0, 0));
		showCount.setBackground (new Color (0, 0, 0, 0));
//		+str+"<br>佛挡杀佛"+"</html></body>"
		StringBuffer text = new StringBuffer("<html><body>玩家吃到食物的数量:<br>");
		//添加自己
		text.append(name+":"+snake_all.size()+"<br>");
		Set<String> keySet = otherSnakeMap.keySet();
		for(String gamer : keySet){
			if(gamer.equals(name)){
				continue;
			}
			text.append(gamer+":"+otherSnakeMap.get(gamer).size()+"<br>");
		}
		showSnakeCount = new JLabel(text.toString());
		showSnakeCount.setForeground(Color.red);
		showSnakeCount.setBounds(0, 10, 120, 30);
		showCount.add(showSnakeCount);
		showCount.setVisible(true);
	}
	/** 更新自己及其他玩家的蛇长度*/
	public static void updateShowCount(){
		StringBuffer text = new StringBuffer("<html><body>玩家吃到食物的数量:<br>");
		//添加自己
		text.append(name+":"+snake_all.size()+"<br>");
		Set<String> keySet = otherSnakeMap.keySet();
		for(String gamer : keySet){
			if(gamer.equals(name)){
				continue;
			}
			text.append(gamer+":"+otherSnakeMap.get(gamer).size()+"<br>");
		}
		showSnakeCount.setText(text.toString());
	}
	
}
