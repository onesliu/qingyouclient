package com.qingyou.http;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;

import com.qingyou.businesslogic.OrderList;
import com.qingyou.qingyouclient.Log;

public class QueryOrders extends HttpPacket {

	@Override
	public int makeSendBuffer(Bundle input) {
		
		params.clear();
		params.add("route", "qingyou/order_query/search");
		params.add("token", input.getString("token"));

		String sDate = input.getString("date");
		if (sDate != null)
			params.add("date", sDate);
		
		String sStatus = input.getString("status");
		if (sStatus != null)
			params.add("status", sStatus);
		
		String sPid = input.getString("special_id");
		if (sPid != null)
			params.add("special_id", sPid);
		
		url = HttpPacket.SERVER_URL;

		return 0;
	}

	@Override
	protected void processResult(String response) throws IOException, ProtocolException, JSONException {
		
		JSONArray orderArr;
		try {
			orderArr = new JSONArray(response);
			data.putInt("order_count", orderArr.length());
			data.putString("orderjson", response);
			OrderList newlist = GetOrders.parseOrderList(response);
			if (newlist != null) {
				errNo = ERR_NONE;
				data.putParcelable("querylist", newlist);
			}
			else {
				errNo = ERR_RESPONSE_INVALID;
				errMsg = "订单解析出错";
			}
		} catch(JSONException e) {
			errNo = ERR_RESPONSE_INVALID;
			errMsg = "订单解析出错";
			Log.v("订单解析出错");
			e.printStackTrace();
			System.out.println(response);
		}
	}
	
}
