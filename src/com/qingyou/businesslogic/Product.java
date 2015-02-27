package com.qingyou.businesslogic;

import java.math.BigDecimal;

public class Product {
	public int product_id;
	public String product_name;
	public int product_type;
	public String ean;
	public double price;
	public int quantity;
	public double total;
	public double realweight;
	public double realtotal;
	
	public int scancount = 0;

	public boolean hasScanned() {
		if (scancount < quantity) return false;
		return true;
	}
	
	public void resetScan() {
		scancount = 0;
	}
	
	public void finishScan() {
		scancount = quantity;
	}
	
	public void addScan(double weight) {
		if (scancount == 0) {
			realweight = realtotal = 0;
		}
		else if (scancount >= quantity) {
			return;
		}
		
		realweight += weight;
		BigDecimal bg = new BigDecimal(realweight * price);
		realtotal = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); //金额2位小数四舍五入
		scancount++;
	}
}