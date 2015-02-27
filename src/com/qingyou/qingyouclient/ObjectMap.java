package com.qingyou.qingyouclient;

import java.util.HashMap;

public class ObjectMap {
	
	private static ObjectMap _self = null;
	private HashMap<String, Object> _objs;

	private ObjectMap()
	{
		_objs = new HashMap<String, Object>();
	}
	
	public static ObjectMap getInstance()
	{
		if (_self == null)
		{
			_self = new ObjectMap();
		}
		
		return _self;
	}
	
	public void put(String key, Object value)
	{
		_objs.put(key, value);
	}
	
	public Object get(String key)
	{
		return _objs.get(key);
	}
	
	public String getString(String key)
	{
		return (String) get(key);
	}
	
	public Integer getInt(String key)
	{
		return (Integer) get(key);
	}
	
	public void remove(String key)
	{
		_objs.remove(key);
	}
	
	public void clear()
	{
		_objs.clear();
	}
}
