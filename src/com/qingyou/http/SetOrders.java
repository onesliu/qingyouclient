package com.qingyou.http;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.qingyou.businesslogic.Order;
import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.Product;
import com.qingyou.qingyouclient.Log;

public class SetOrders extends HttpPacket {

	@Override
	public int makeSendBuffer(Bundle input) {
		
		params.clear();
		if (input.getParcelable("order") == null)
			return -1;
		if (input.getInt("order_status", 0) == 0)
			return -1;
		String ojson = makeOrderList((Order)input.getParcelable("order"), input.getInt("order_status"));
		if (ojson.equals(""))
			return -1;
		params.add("orders", ojson);
		url = HttpPacket.SERVER_URL + "?route=qingyou/order_query/commit&token=" + input.getString("token");
		
		return ERR_NONE;
	}

	@Override
	protected void processResult(String response) throws IOException, ProtocolException, JSONException {
		
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(response);
			int status = jsonObj.getInt("status");
			data.putInt("status", status);
			if (status == 0) {
				OrderList.getGlobal().commitAllOrders();
				Log.v("PROTO:(SetOrders) OK");
			}
			else {
				errNo = status;
				Log.v("PROTO:(SetOrders) Error, status != 0");
			}

			data.putInt("session", HttpUtility.cookieSize());
		} catch(JSONException e) {
			errNo = ERR_RESPONSE_INVALID;
			Log.v("PROTO:(SetOrders) Error");
			e.printStackTrace();
			System.out.println(response);
		}
	}
	
	public static String makeOrderList(Order oorder, int order_status) {
		JSONArray orderArr, productArr;
		JSONObject order, product;

		if (oorder == null) return "";
	
		try {
			
			orderArr = new JSONArray();
			
			order = new JSONObject();
			order.put("order_id", oorder.order_id);
			order.put("order_status", order_status);
			order.put("total", oorder.getOrderTotal());
			order.put("realtotal", oorder.getOrderRealTotal());
			order.put("order_type", oorder.order_type);
			order.put("order_createtime", oorder.order_createtime);
			order.put("productSubject", oorder.productSubject);
			order.put("costpay", oorder.costpay);
			order.put("cashpay", oorder.cashpay);
			order.put("iscash", oorder.iscash);
			
			if (oorder.product_size() > 0) {
				productArr = new JSONArray();
				for(int j = 0; j < oorder.product_size(); j++) {
					Product oproduct = oorder.products.get(j);
					product = new JSONObject();
					product.put("product_id", oproduct.product_id);
					product.put("realweight", oproduct.realweight);
					product.put("realtotal", oproduct.realtotal);
					
					productArr.put(product);
				}
				
				order.put("products", productArr);
			}
			
			orderArr.put(order);

			return orderArr.toString();
		} catch(JSONException e) {
			e.printStackTrace();
		}

		return "";
	}
	
}
