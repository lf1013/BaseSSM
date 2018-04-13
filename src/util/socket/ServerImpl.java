package util.socket;

import java.util.HashMap;

import util.Tools;

/**
 * 一种逻辑管理的服务 实现  抽离转发规则
 * 由底层控制调用 此 又回归底层调用实现
 * 使用HashMap<String, HashMap<String, SOCKET>>管理所有连接和定义转发规则
 * 再由实际的底层调用此规则
 */

public abstract class ServerImpl<Arg> implements Server<Arg>{
		//索引! 系统key       系统子级key  客户端引用
	HashMap<String, HashMap<String, ToClient>> toClients = new HashMap<String, HashMap<String, ToClient>>();
	final static String DEFAULT_SYSKEY = "0";
	final static String DEFAULT_KEY = "0";
	
//	<0, <0, obj>> this default
//	<0, <1001, obj>> this 未登录1
//	<0, <1002, obj>> this 未登录2

//	<10a0, <0,    obj>> 10a0服务器-this 
//	<10a0, <1001, obj>> 10a0服务器-1 
	
	
	 
	/**
	 * 从底层传递上来的 收到的消息 
	 * 此处负责找到该消息 对应的当前管理的 那个ToClient 并调用处理逻辑
	 * Arg -> socket -> ToClient -> sysKey,key
	 * str -> Msg(msgType, toSysKey, toKey, map)
	 */
	@Override
	public void onReceive(Arg obj, String jsonstr){
		Tools.out("<<<<<<<<<<", jsonstr);

		
		ToClient toClient = this.getClient(obj);
		if(toClient == null) return;
		String fromSysKey = toClient.getSysKey();
		String fromKey = toClient.getKey();
		Msg msg = new Msg(fromSysKey, fromKey, jsonstr);
		String toSysKey = msg.getToSysKey();
		String toKey = msg.getToKey();
		Tools.out("onReceive", msg);

		//未登录某系统时 都归属于 this 0/1000+
		if(msg.getMsgType() == Msg.LOGIN_CLIENT){		//客户端登录
			String newSysKey = (String) msg.get("tosyskey", DEFAULT_SYSKEY);
			this.changeClient(fromSysKey, fromKey, newSysKey);
			show();
		}else if(msg.getMsgType() == Msg.LOGIN_SERVER){	//服务器登录
			String newSysKey = (String) msg.get("tosyskey", DEFAULT_SYSKEY);
			String pwd = (String) msg.get("pwd", "0");
			this.changeServer(fromSysKey, fromKey, newSysKey);
			show();
		}else{
			//向发送者回写提示信息
			this.send(obj, msg.getData());
			
		}
		
	}
	/**
	 * 上层调用 
	 * 向某个客户发送消息
	 */
	@Override
	public void send(Arg obj, String jsonstr){
		Tools.out(">>>>", jsonstr);
		this.sendCallback.sendCallback(obj, jsonstr);
	}
	SendCallback sendCallback = null;
	@Override
	public void setSendCallback(SendCallback sendCallback) {
		this.sendCallback = sendCallback;
	}

	/**
	 * 1.java.net.Socket
	 * 
	 */
	@Override
	public void onNewConnection(Arg obj) {
		//新连接 添加为自己掌管的客户 socket
		ToClient toClient = new ToClient<Arg>(obj);
		this.addClient(DEFAULT_SYSKEY, toClient);
		Tools.out("新连接", toClient);
	} 
	@Override
	public void onDisConnection(Arg obj) {
		//新连接 添加为自己掌管的客户 socket
		ToClient toClient = this.getClient(obj);
		Tools.out("断开连接", toClient);
		this.removeClient(toClient.sysKey, toClient.key);
	}
	@Override
	public boolean pause() {
		return true;
	}
 
	 
 

	
	
	
	
	
	
	//获取某系统下一个最小的不重复的编码
	private String getNewKey(String sysKey){
		HashMap<String, ToClient> sys = getSys(sysKey);
		String res = "";
		int start = 1000;
		for(String key : sys.keySet()){
			if(!key.equals(start++ +""))
				break;
		}
		res = start + "";	
		return res;
	}
	
	

	private ToClient changeServer(String sysKey, String key, String toSysKey){
		return this.addServer(toSysKey, this.removeClient(sysKey, key));
	}
	private ToClient changeClient(String sysKey, String key, String toSysKey){
		return this.addClient(toSysKey, this.removeClient(sysKey, key));
	}
	private ToClient removeClient(String sysKey, String key){
		return getSys(sysKey).remove(key);
	} 
	private ToClient addClient(String sysKey, ToClient toClient){
		String key = getNewKey(sysKey);
		toClient.setKey(key);
		toClient.setSysKey(sysKey);
		getSys(sysKey).put(key, toClient);
		return toClient;
	}
	private ToClient addServer(String sysKey, ToClient toClient){
		getSys(sysKey).put(DEFAULT_KEY, toClient);
		return toClient;
	}
	
	private HashMap<String, ToClient> getSys(String sysKey){
		if(toClients.get(sysKey) == null){	//若该系统没有人登录 或者没有初始化 则初始化系统容器
			toClients.put(sysKey, new HashMap<String, ToClient>());
		}
		return toClients.get(sysKey);
	}
	private ToClient getClient(String sysKey, String key){
		return getSys(sysKey).get(key);
	}
	/**
	 * 通过底层socket引用对象来获取hashmap中的toClient(sysKey, key, socket)实例
	 */
	private ToClient getClient(Arg obj){
		ToClient res = null;
		for(String sysKey : toClients.keySet()){
			for(String key : toClients.get(sysKey).keySet()){
				if(toClients.get(sysKey).get(key).like(obj)){
					res = toClients.get(sysKey).get(key);
				}
				
			}
		}
		return res;
	}
	//服务器 路由状态
	private String show(){
		String res = "\n";
		res += "------------------------------------------\n";
		res += "--sysKeys : " + toClients.keySet().size();
		for(String sysKey : toClients.keySet()){
			res += "-----------------\n";
			res += "----" + sysKey + " : " + toClients.get(sysKey).keySet().size();
			for(String key : toClients.get(sysKey).keySet()){
				res += "--" + key + "  " + toClients.get(sysKey).get(key).toString();
				
			}
		}
		res += "\n";
		Tools.out(res);
		return res;
	}
	
} 