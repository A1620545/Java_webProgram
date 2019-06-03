package demo;

/** 指令列表*/
public interface Instruction {
	/** 玩家名称*/
	public static String name = "name" ;
	/** 移动指令*/
	public static String move = "move";
	/** 退出游戏*/
	public static String exit = "exit";
	/** 返回*/
	public static String back = "back";
	/** 房间ip*/
	public static String ip = "ip";
	/** 获取房间ip*/
	public static String getIp = "getIp";
	/** 玩家*/
	public static String gamers = "gamers";
	/** 房主外的其他玩家*/
	public static String other = "other";
	/** 添加玩家*/
	public static String addGamer = "addGamer";
	/** 开始游戏*/
	public static String start = "start";
	/** 蛇头位置*/
	public static String snake_head = "snake_head";
	/** 停止食物生产*/
	public static String shutFood = "shutFood";
	/** 生产食物*/
	public static String showFood = "showFood";
	/** 有玩家吃到食物*/
	public static String eatFood = "eatFood";
	/** 同步食物位置*/
	public static String SynchronizeFood = "SynchronizeFood";
	/** 检查是否碰到其他玩家*/
	public static String checkOtherGamer = "checkOtherGamer";
	/** 玩家被淘汰*/
	public static String gamer_out = "gamer_out";
	/** 最终胜利者*/
	public static String winner = "winner";
}
