package com.qingyou.http;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.qingyou.businesslogic.OrderList;
import com.qingyou.qingyouclient.ActivityMain;

public class HttpThread {
	
	public static final int NET_ERROR = -1;
	public static final int NET_NORMAL = 0;
	public static final int NET_SEND = 1;
	public static final int LOGIN_OK = 2;
	public static final int LOGIN_FAIL = 3;
	public static final int NET_ORDER_REFRESH = 4;

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
	
	public OrderList GetOrders() {
		return (OrderList) orders.data.getSerializable("orderlist");
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
						OrderList nlist = (OrderList)orders.data.getSerializable("newlist");
						olist.mergeOrderList(nlist);
						olist.sortByTime();
						sendMsg(NET_ORDER_REFRESH, "订单获取成功");
					}
				}
				
				for(int i = 0; i < 10; i++) {
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
