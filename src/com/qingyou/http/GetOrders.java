package com.qingyou.http;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.qingyou.businesslogic.Order;
import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.OrderStatus;
import com.qingyou.businesslogic.Product;
import com.qingyou.qingyouclient.Log;

public class GetOrders extends HttpPacket {

	@Override
	public int makeSendBuffer(Bundle input) {
		
		params.add("route", "qingyou/order_query");
		params.add("token", input.getString("token"));
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
			OrderList newlist = parseOrderList(response);
			if (newlist != null) {
				errNo = ERR_NONE;
				data.putSerializable("newlist", newlist);
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
	
	public static OrderList parseOrderList(String response) {
		JSONArray orderArr, productArr;
		JSONObject order, product;
		OrderList olist = null;
		String productSubject = new String();
		try {
			orderArr = new JSONArray(response);
			olist = new OrderList();
			for(int i = 0; i < orderArr.length(); i++) {
				order = orderArr.getJSONObject(i);
				
				Order o = new Order();
				o.order_id = Long.parseLong(order.getString("order_id"));
				o.initStatus(order.getInt("order_status_id"));
				o.order_createtime = order.getString("order_createtime");
				o.customer_id = order.getInt("customer_id");
				o.customer_name = order.getString("customer_name");
				o.customer_phone = order.getString("customer_phone");
				o.shipping_name = order.getString("shipping_name");
				o.shipping_phone = order.getString("shipping_telephone");
				o.shipping_addr = order.getString("shipping_addr");
				o.shipping_time = order.getString("shipping_time");
				o.iscash = order.getInt("iscash");
				o.comment = "";
				
				productArr = order.getJSONArray("products");
				
				productSubject = "";
				int ordertype = 0;
				for(int j = 0; j < productArr.length(); j++) {
					product = productArr.getJSONObject(j);

					Product p = new Product();
					p.product_id = product.getInt("product_id");
					p.product_name = product.getString("product_name");
					p.product_type = product.getInt("product_type");
					p.ean = product.getString("ean");
					p.unit = product.getString("unit");
					p.price = product.getDouble("price");
					p.perprice = product.getDouble("perprice");
					p.perweight = product.getDouble("perweight");
					p.perunit = product.getString("perunit");
					p.weightunit = product.getString("weightunit");
					p.quantity = product.getInt("quantity");
					p.total = product.getDouble("total");
					p.realweight = product.getDouble("realweight");
					p.realtotal = product.getDouble("realtotal");
					
					if (o.order_status > OrderStatus.ORDER_STATUS_WAITING)
						p.finishScan();
					
					if (p.product_type == 0) {
						p.realweight = p.perweight * p.quantity;
						p.realtotal = p.perprice * p.quantity;
						p.finishScan();
					}
					
					ordertype += p.product_type;
					o.add_product(p);
					productSubject += product.getString("product_name") + " ";
				}
				
				if (ordertype == 0)
					o.order_type = 0; //0:固定客单价订单, 1:变客单价订单
				else
					o.order_type = 1;
				
				o.productSubject = productSubject;
				olist.add(o);
			}
			
			//System.out.println(response);

			return olist;
		} catch(JSONException e) {
			Log.v("PROTO:(parseOrders) Error");
			e.printStackTrace();
			System.out.println(response);
		}

		return null;
	}
	
}
