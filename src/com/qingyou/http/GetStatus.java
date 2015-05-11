package com.qingyou.http;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.qingyou.businesslogic.OrderStatus;
import com.qingyou.qingyouclient.Log;

public class GetStatus extends HttpPacket {

	@Override
	public int makeSendBuffer(Bundle input) {
		
		params.clear();
		params.add("route", "qingyou/order_query/status");
		params.add("token", input.getString("token"));
		url = HttpPacket.SERVER_URL;
		
		return 0;
	}

	@Override
	protected void processResult(String response) throws IOException, ProtocolException, JSONException {
		
		JSONObject status;
		OrderStatus os = OrderStatus.getInstance();
		try {
			status = new JSONObject(response);
			String key;
			String value;
			Iterator itr = status.keys();
			while(itr.hasNext()) {
				key = (String)itr.next();
				value = (String)status.get(key);
				os.putStatus(Integer.parseInt(key), value);
			}
			
			Log.d("取得订单状态");
		} catch(JSONException e) {
			errNo = ERR_RESPONSE_INVALID;
			errMsg = "订单状态解析出错";
			Log.v("订单状态解析出错");
			e.printStackTrace();
			System.out.println(response);
		}
	}
	

}
