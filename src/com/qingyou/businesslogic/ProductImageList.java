package com.qingyou.businesslogic;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.qingyou.http.HttpUtility;

public class ProductImageList implements Parcelable {

	HashMap<String, Bitmap> imagemap;
	
	private static ProductImageList _self = null;
	public static ProductImageList instance() {
		if (_self == null) {
			_self = new ProductImageList();
		}
		return _self;
	}
	
	//=========================Parcel======================================
	
	public ProductImageList(Parcel in) {
		imagemap = new HashMap<String, Bitmap>();
		in.readMap(imagemap, HashMap.class.getClassLoader());
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeMap(imagemap);
	}
	
	public static final Parcelable.Creator<ProductImageList> CREATOR = new Creator<ProductImageList>() {

		@Override
		public ProductImageList createFromParcel(Parcel source) {
			ProductImageList ol = new ProductImageList(source);
			ProductImageList._self = ol;
			return ol;
		}

		@Override
		public ProductImageList[] newArray(int size) {
			return new ProductImageList[size];
		}
	
	};
	
	//===============================================================

	private ProductImageList() {
		imagemap = new HashMap<String, Bitmap>();
	}
	
	public Bitmap getImage(String url) {
		return imagemap.get(url);
	}
	
	public void putImage(final String url) {
		if (getImage(url) != null) return;
		
		new Thread() {
			public Bitmap image = null;
			@Override
			public void run() {
				image = HttpUtility.getHttpBitmap(url);
				if (image != null) {
					imagemap.put(url, image);
				}
			}
		}.start();
	}
	
}
