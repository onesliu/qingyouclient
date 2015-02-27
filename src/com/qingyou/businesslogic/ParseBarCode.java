package com.qingyou.businesslogic;

public class ParseBarCode {

	public int precode;
	public int productId;
	public double weight;
	
	public boolean parseWeightCode(String code) {
		if (code.length() < 12) return false;
		try {
			precode = Integer.parseInt(code.substring(0, 2));
			productId = Integer.parseInt(code.substring(2, 7));
			weight = Double.parseDouble(code.substring(7, 12)) / 1000;
		} catch(Exception e) {
			return false;
		}
		return true;
	}
}
