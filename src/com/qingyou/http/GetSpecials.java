package com.qingyou.http;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.qingyou.qingyouclient.Log;

public class GetSpecials extends HttpPacket {

	@Override
	public int makeSendBuffer(Bundle input) {
		
		params.clear();
		params.add("route", "qingyou/order_query/specials");
		params.add("token", input.getString("token"));
		url = HttpPacket.SERVER_URL;
		
		return 0;
	}

	@Override
	protected void processResult(String response) throws IOException, ProtocolException, JSONException {
		
		try {
			Bundle specials = parseSpecials(response);
			if (specials != null) {
				errNo = ERR_NONE;
				data.putBundle("specials", specials);
			}
			else {
				errNo = ERR_RESPONSE_INVALID;
				errMsg = "预订商品解析出错";
			}
		} catch(Exception e) {
			errNo = ERR_RESPONSE_INVALID;
			errMsg = "预订商品解析出错";
			Log.v("预订商品解析出错");
			e.printStackTrace();
			System.out.println(response);
		}
	}
	
	public Bundle parseSpecials(String resp) {
		JSONArray specials;
		JSONObject product;
		Bundle mpSpecials = new Bundle();
		
		try {
			specials = new JSONArray(resp);
			for(int i = 0; i < specials.length(); i++) {
				product = specials.getJSONObject(i);
				
				int pid = product.getInt("product_id");
				String name = product.getString("name");
				mpSpecials.putInt(name, pid);
			}
			
			return mpSpecials;
		} catch(JSONException e) {
			Log.v("PROTO:(parseSpecials) Error");
			e.printStackTrace();
			System.out.println(resp);
		}
		
		return null;
	}
}
