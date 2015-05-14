package com.qingyou.http;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.qingyou.businesslogic.OrderList;
import com.qingyou.qingyouclient.Log;

public class AlertPay extends HttpPacket {

	@Override
	public int makeSendBuffer(Bundle input) {
		
		params.clear();
		params.add("route", "qingyou/order_query/alertpay");
		params.add("token", input.getString("token"));

		String orderid = input.getString("orderid");
		if (orderid == null)
			return -1;
		params.add("order_id", orderid);
		
		url = HttpPacket.SERVER_URL;
		
		return 0;
	}

	@Override
	protected void processResult(String response) throws IOException, ProtocolException, JSONException {
		
		JSONObject jsonObj;
		try {
			errNo = ERR_NONE;
			jsonObj = new JSONObject(response);
			int status = jsonObj.getInt("status");
			data.putInt("status", status);
			if (status == 0) {
				Log.v("PROTO:(AlertPay) OK");
			}
			else {
				Log.v("PROTO:(AlertPay) Error, status != 0");
			}
		} catch(JSONException e) {
			errNo = ERR_RESPONSE_INVALID;
			Log.v("PROTO:(AlertPay) Error");
			e.printStackTrace();
			System.out.println(response);
		}
	}
	

}
