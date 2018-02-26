package com.service;

import java.util.List;
import java.util.Map;

import com.mode.Page;


/**
 * 案例：student表的hibernate和mybatis两种方式实现操作
 * @author Walker
 *
 */
public interface StudentService  {
	public List<Map>  list(String id, String name, String timefrom, String timeto, Page page) ;
    
	public int update(String id, String name, String time);
	public int delete(String id);
	public int add( String name, String time);

	public Map get(String id);
	
}