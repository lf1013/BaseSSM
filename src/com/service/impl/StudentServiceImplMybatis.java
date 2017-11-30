package com.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dao.mybatis.BaseMapper;
import com.mode.Page;
import com.service.StudentService;

import util.MapListHelp;
import util.SQLHelp;
import util.Tools;

@Service("studentServiceMybatis")
public class StudentServiceImplMybatis implements StudentService, Serializable {

	private static final long serialVersionUID = 8304941820771045214L;
 
	/**
	 * mybatis入口
	 */
	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	@Autowired
	protected BaseMapper baseMapper;

	@Override
	public List<Map> list(String id, String name, String timefrom, String timeto, Page page) {
		if(!Tools.isNull(name)) name = "";
		else name = SQLHelp.like(name);
		if(!Tools.isNull(id)) id = "";
		else  id = SQLHelp.like(id);
		
		Map map = MapListHelp.getMap()
				.put("name", name)
				.put("id", id)
				.put("timefrom", timefrom)
				.put("timeto", timeto)
				.put("pagestart", page.getStart())
				.put("pagestop", page.getStop())
				.build();  
		page.setNum(baseMapper.count(map));
		return baseMapper.find(map);
	}

	@Override
	public int update(String id, String name, String time) {
		Map map = MapListHelp.getMap().put("name", name)
				.put("id", id)
				.put("time", time)
				.build(); 
		return baseMapper.update(map);
	}

	@Override
	public int delete(String id) {
		Map map = MapListHelp.getMap() 
				.put("id", id)
				.build();
		return baseMapper.delete(map);
	}

	@Override
	public Map get(String id) {
		Map map = MapListHelp.getMap() 
				.put("id", id)
				.build();
		List<Map> list = baseMapper.find(map);
		Map res = null;
		if(list != null && list.size() > 0){
			res = list.get(0);
		}else{
			res = new HashMap();
		}
		return res;
	}

	@Override
	public int add(String name, String time) {
		Map map = MapListHelp.getMap() 
				.put("name", name)
				.put("time", time)
				.build();
		return baseMapper.add(map);
	}

}