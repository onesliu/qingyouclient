package com.qingyou.businesslogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Order {

	public long order_id;
	public int order_status;
	public String order_createtime;
	public int customer_id;
	public String customer_name;
	public String customer_phone;
	public String shipping_name;
	public String shipping_phone;
	public String shipping_addr;
	public String shipping_time;
	public String comment;
	public int iscash;
	
	public int order_type;
	public int order_status_orign;
	public String productSubject;
	public boolean is_delete = false;

	public List<Product> products;
	
	public Order() {
		products = new ArrayList<Product>();
	}
	
	public void add_product(Product p) {
		products.add(p);
	}
	
	public Product get(int productid) {
		Iterator<Product> itr = products.iterator();
		Product p = null, ret = null;
		while(itr.hasNext()) {
			p = itr.next();
			if (p.product_id == productid) {
				ret = p;
				break;
			}
		}
		return ret;
	}
	
	public Product get(String barcode) {
		Iterator<Product> itr = products.iterator();
		ParseBarCode pbc = new ParseBarCode();
		Product p = null, ret = null;
		int id1, id2;

		if (pbc.parseWeightCode(barcode) == false)
			return null;
		id1 = pbc.productId;
		
		while(itr.hasNext()) {
			p = itr.next();
			if (pbc.parseWeightCode(p.ean) == false)
				continue;
			id2 = pbc.productId;
			
			if (id1 == id2) {
				ret = p;
				break;
			}
		}
		return ret;
	}
	
	public int product_size() {
		return products.size();
	}
	
	public void initStatus(int status) {
		order_status = order_status_orign = status;
	}
	
	public void resetStatus() {
		order_status = order_status_orign;
	}
	
	public boolean hasChanged() {
		if (order_status != order_status_orign)
			return true;
		return false;
	}
	
	public void commit() {
		order_status_orign = order_status;
	}
	
	public boolean hasScanedOver() {
		if (order_status >= OrderStatus.ORDER_STATUS_FINISHED)
			return true;
		
		Iterator<Product> itr = products.iterator();
		while(itr.hasNext()) {
			if (itr.next().hasScanned() == false)
				return false;
		}
		
		if (order_type == 0)
			order_status = OrderStatus.ORDER_STATUS_SCALED;
		else
			order_status = OrderStatus.ORDER_STATUS_PAYING;

		return true;
	}
	
	public void setDelivered() {
		order_status = OrderStatus.ORDER_STATUS_FINISHED;
	}
	
	public double getOrderTotal() {
		Iterator<Product> itr = products.iterator();
		double total = 0.0;
		while(itr.hasNext()) {
			total += itr.next().total;
		}
		return total;	
	}
	
	public double getOrderRealTotal() {
		Iterator<Product> itr = products.iterator();
		double total = 0.0;
		while(itr.hasNext()) {
			total += itr.next().realtotal;
		}
		return total;	
	}
}
