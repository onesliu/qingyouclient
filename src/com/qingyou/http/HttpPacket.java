package com.qingyou.http;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class HttpPacket {

	public static final int ERR_NONE = 0;
	public static final int ERR_NET_CONNECT_FAIL = -1;
	public static final int ERR_PASSWD_INVALID = -2;
	public static final int ERR_RESPONSE_INVALID = -3;
	
	protected static final String SERVER_URL = "http://qy.gz.1251102575.clb.myqcloud.com/admin/index.php";
	
	protected Handler handler;
	protected Context context = null;
	protected Bundle data = null;
	protected int errNo = 0;
	protected String errMsg = null;
	
	protected HttpParameters params;
	protected String url;
	
	public void sendResultMsg() {
		if (handler != null)
		{
			Message msg = new Message();
			msg.what = errNo;
			data.putInt("errno", errNo);
			data.putString("errmsg", errMsg);
			msg.setData(data);
			handler.sendMessage(msg);
		}
	}

	public HttpPacket()
	{
		data = new Bundle();
		params = new HttpParameters();
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	protected String getTime(long time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date(time));
	}
	
	public static HttpPacket clone(Context context,String clsName,Handler handler)
	{
		HttpPacket pkt = null;
		String pkg = HttpPacket.class.getPackage().getName();
		try {
			pkt = (HttpPacket) Class.forName(pkg + "." + clsName).newInstance();
			pkt.setHandler(handler);
			pkt.setContext(context);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return pkt;
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	private static Object lock = new Object();
	protected String request(final Context context, final String url, final HttpParameters params, final String httpMethod) throws ProtocolException {
		
		class SyncThread extends Thread {
			public String rlt;
			@Override
			public void run() {
                try {
            		synchronized (lock) {
            			rlt = HttpUtility.openUrl(context, url, httpMethod, params);
            		}
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
		};
		
		SyncThread t = new SyncThread();
		try {
			t.start();
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        return t.rlt;
    }
	
	protected void fail(Exception e)
	{
		errMsg = e.getClass().getName() + ": " + e.getMessage();
		e.printStackTrace();
	}
	
	protected int SyncRequest(String httpMethod)
	{
		try {
			String resp = request(context, url, params, httpMethod);
			processResult(resp);
        } catch (ProtocolException e) {
			errNo = e.getStatus();
			if (e.getStatus() == ERR_PASSWD_INVALID) {
				errNo = ERR_PASSWD_INVALID;
			}
			else if (errNo == 0) {
				errNo = ERR_NET_CONNECT_FAIL;
			}
        	fail(e);
        } catch(IOException e) {
        	errNo = ERR_NET_CONNECT_FAIL;
			fail(e);
        } catch (JSONException e) {
        	errNo = ERR_RESPONSE_INVALID;
        	fail(e);
		}
		
		return errNo;
	}
	
	public int getErrNo() {
		return errNo;
	}
	
	public String getErrStr() {
		return errMsg;
	}
	
	// set errNo & errMsg in this method
	public abstract int makeSendBuffer(Bundle input);
	
	// set errNo & errMsg in this method
	protected abstract void processResult(String response)
			throws IOException, ProtocolException, JSONException;
}
