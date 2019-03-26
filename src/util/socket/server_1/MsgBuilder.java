package util.socket.server_1;

import util.Bean;
import util.socket.server_1.session.Session;

/**
 * 构造各种消息格式
 * @author walker
 *
 */
public class MsgBuilder {
	
	
	
	
	public static <T> Object makeException(Session<T> session, MsgUp msg, Exception e) {
		return new MsgDown().setType("exception").setData(new Bean().set("msg", msg).set("info", e.toString()));
	}

	public static <T> Object makeOnLogin(Session<T> session, Bean bean) {
//		return new Bean().set(TYPE, TYPE_EVENT).set(DATA, bean.set("type", "onlogin"));
		return new MsgDown().setType("onlogin").setData(bean.set("session", session));
	}

	public static <T> Object makeOnUnLogin(Session<T> session, Bean bean) {
//		return new Bean().set(TYPE, TYPE_EVENT).set(DATA, bean.set("type", "onunlogin"));
		return new MsgDown().setType("onunlogin").setData(bean.set("session", session));
	}

	
	
	
}
