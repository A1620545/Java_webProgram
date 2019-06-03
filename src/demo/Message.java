package demo;

import java.util.List;
import java.util.Map;
/**客户端与服务器端通信*/
public class Message {

	//玩家标识
	private String id;
	//指令名称
	private String name;
	//指令附加信息(玩家名称之类的)
	private String ins;
	//玩家组
	private List<String> gamers;
	//蛇
	private List<Snake> snake;
	
	private Map<String,List<Snake>> map;
	//食物或蛇头位置
	private Snake food;
	
	
	
	public Message(String name, String ins) {
		super();
		this.name = name;
		this.ins = ins;
	}
	public Message(){}
	
	
	
	public Snake getFood() {
		return food;
	}
	public void setFood(Snake food) {
		this.food = food;
	}
	public Map<String, List<Snake>> getMap() {
		return map;
	}
	public void setMap(Map<String, List<Snake>> map) {
		this.map = map;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIns() {
		return ins;
	}
	public void setIns(String ins) {
		this.ins = ins;
	}
	public List<String> getGamers() {
		return gamers;
	}
	public void setGamers(List<String> gamers) {
		this.gamers = gamers;
	}
	public List<Snake> getSnake() {
		return snake;
	}
	public void setSnake(List<Snake> snake) {
		this.snake = snake;
	};
	
}
