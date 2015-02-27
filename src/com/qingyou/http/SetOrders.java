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
		
		params.add("orders", makeOrderList());
		//System.out.println(params.getValue("orders"));
		url = HttpPacket.SERVER_URL + "?route=qingyou/order_query/commit&token=" + input.getString("token");
		
		return 0;
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
	
	public static String makeOrderList() {
		JSONArray orderArr, productArr;
		JSONObject order, product;
		OrderList olist = OrderList.getGlobal();
	
		try {
			
			orderArr = new JSONArray();
			for(int i = 0; i < olist.size(); i++) {
				Order oorder = olist.orders.get(i);
				if (oorder.hasChanged() == false) continue;
				
				order = new JSONObject();
				order.put("order_id", oorder.order_id);
				order.put("order_status", oorder.order_status);
				order.put("total", oorder.getOrderTotal());
				order.put("realtotal", oorder.getOrderRealTotal());
				order.put("order_type", oorder.order_type);
				order.put("order_createtime", oorder.order_createtime);
				order.put("productSubject", oorder.productSubject);
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
			}

			return orderArr.toString();
		} catch(JSONException e) {
			e.printStackTrace();
		}

		return "";
	}
	
}
