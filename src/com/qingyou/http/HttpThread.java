package com.qingyou.http;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.qingyou.businesslogic.Order;
import com.qingyou.businesslogic.OrderList;

public class HttpThread {
	
	public static final int NET_ERROR = -1;
	public static final int NET_NORMAL = 0;
	public static final int NET_SEND = 1;
	public static final int LOGIN_OK = 2;
	public static final int LOGIN_FAIL = 3;
	public static final int NET_ORDER_REFRESH = 4;
	public static final int COMMIT_ORDER = 5;

	private static HttpThread _self = null;
	public boolean isLogin = false;
	public int district_id;
	public int scale_count;
	
	public String user;
	public String password;
	
	private boolean stop = false;
	public boolean conti = false;
	
	private String token;

	private ClientThread connectionThread = null;

	private UserLogin login = new UserLogin();
	private GetOrders orders = new GetOrders();
	private SetOrders setorder = new SetOrders();
	private GetSpecials specials = new GetSpecials();
	private QueryOrders queryorder = new QueryOrders();
	private Bundle data = new Bundle();
	
	private Context c;
	
	private HttpThread(Context c) {
		this.c = c;
	}
	
	public static HttpThread getInstance(Context c)
	{
		if (_self == null)
		{
			_self = new HttpThread(c);
		}
		
		return _self;
	}
	
	public void startDataThread() {
		stop = false;
		connectionThread = new ClientThread();
		connectionThread.start();
	}
	
	public void stopDataThread() {
		stop = true;
		try {
			if (connectionThread != null) {
				connectionThread.interrupt();
				connectionThread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		connectionThread = null;
	}
	
	public boolean login(String user, String pwd) {
		if (isLogin == false) {
			this.user = user;
			this.password = pwd;
			data.clear();
			data.putString("account", user);
			data.putString("passwd", pwd);
			login.makeSendBuffer(data);
			if (login.SyncRequest("POST") != HttpPacket.ERR_NONE) {
				isLogin = false;
				Log.e(login.getClass().getSimpleName(), login.getErrStr());
			}
			else {
				token = login.data.getString("token");
				isLogin = true;
				
				//get order status
				GetStatus status = new GetStatus();
				data.putString("token", token);
				status.makeSendBuffer(data);
				if (status.SyncRequest("GET") != HttpPacket.ERR_NONE) {
					isLogin = false;
				}
			}
		}
		return isLogin;
	}
	
	public boolean commitOrder(Order o, int order_status) {
		data.clear();
		data.putString("token", token);
		data.putParcelable("order", o);
		data.putInt("order_status", order_status);
		if (setorder.makeSendBuffer(data) != HttpPacket.ERR_NONE) {
			sendMsg(NET_ERROR, "订单参数错误");
			Log.e(setorder.getClass().getSimpleName(), setorder.getErrStr());
			return false;
		}
		
		if (setorder.SyncRequest("POST") != HttpPacket.ERR_NONE) {
			sendMsg(NET_ERROR, "提交订单出错");
			Log.e(setorder.getClass().getSimpleName(), setorder.getErrStr());
			return false;
		}

		sendMsg(COMMIT_ORDER, "提交订单成功");
		return true;
	}
	
	public OrderList GetOrders() {
		return (OrderList) orders.data.getSerializable("orderlist");
	}
	
	public OrderList QueryOrders(Bundle params) {
		data.clear();
		data.putString("token", token);
		data.putAll(params);
		queryorder.makeSendBuffer(data);
		if (queryorder.SyncRequest("GET") != HttpPacket.ERR_NONE) {
			isLogin = false;
			sendMsg(NET_ERROR, "订单获取出错");
			Log.e(queryorder.getClass().getSimpleName(), queryorder.getErrStr());
			return null;
		}

		OrderList nlist = (OrderList)queryorder.data.getParcelable("querylist");
		nlist.sortByTime();
		return nlist;
	}
	
	public Bundle GetSpecials() {
		data.clear();
		data.putString("token", token);
		if (specials.makeSendBuffer(data) != HttpPacket.ERR_NONE) {
			sendMsg(NET_ERROR, "取预订商品参数错误");
			Log.e(specials.getClass().getSimpleName(), specials.getErrStr());
			return null;
		}
		
		if (specials.SyncRequest("GET") != HttpPacket.ERR_NONE) {
			sendMsg(NET_ERROR, "取预订商品出错");
			Log.e(specials.getClass().getSimpleName(), specials.getErrStr());
			return null;
		}

		return specials.data.getBundle("specials");
	}

	private void sendMsg(int msgid, String str) {
		Intent intent = new Intent();
		intent.setAction("qingyou.net.trans");
		Bundle b = new Bundle();
		b.putInt("msgid", msgid);
		b.putString("msg", str);
		intent.putExtras(b);
		c.sendBroadcast(intent);
	}

	private class ClientThread extends Thread {
		private void sleep(int seconds) {
			try {
				Log.d(this.getClass().getSimpleName(), "sleep " + seconds + " seconds...");
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException e) {
				Log.e(this.getClass().getSimpleName(), "sleep was interrupted by Exception.");
			}
		}
		
		@Override
		public void run() {
			Log.i(this.getClass().getSimpleName(), "Qingyou Service Thread started.");
			
			while (stop == false) {
				sendMsg(NET_NORMAL, "订单更新中...");
				
				//如果用户未登录状态，重试登录
				if (isLogin == false) {
					if (user != null && password != null && !user.equals("") && !password.equals("")) {
						if (login(user, password) == false) {
							sendMsg(LOGIN_FAIL, "登录失败");
						}
						else {
							sendMsg(LOGIN_OK, "登录成功");
						}
					}
				}
				
				if (isLogin == true) {
					data.putString("token", token);
					orders.makeSendBuffer(data);
					if (orders.SyncRequest("GET") != HttpPacket.ERR_NONE) {
						isLogin = false;
						sendMsg(NET_ERROR, "订单获取出错");
						Log.e(orders.getClass().getSimpleName(), orders.getErrStr());
					}
					else {
						OrderList olist = OrderList.getGlobal();
						OrderList nlist = (OrderList)orders.data.getParcelable("newlist");
						olist.mergeOrderList(nlist);
						olist.sortByTime();
						sendMsg(NET_ORDER_REFRESH, "订单获取成功");
					}
				}
				
				for(int i = 0; i < 20; i++) {
					if (stop == true) break;
					if (conti == true) {
						conti = false;
						break;
					}
					sleep(1);
				}
			}
			
			isLogin = false;
			sendMsg(NET_ERROR, "服务线程停止");
			Log.e(this.getClass().getSimpleName(), "Qingyou Service Thread stoped.");
		}
	}

}
