package com.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mode.Page;
import com.service.StudentService;

import util.MapListHelp;
import util.Tools;
import util.WebHelp;

/** 
 * 测试AngularJs的后台
 * @author Walker
 *
 */
@Controller
@RequestMapping("/angular")
public class AngularControll extends BaseControll{   
	@Autowired
	@Qualifier("studentServiceHibernate") 
	StudentService studentServiceHibernate;

	

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("/statis.do") 
	public void statis(HttpServletRequest request, HttpServletResponse response) throws IOException { 
		  
//		        legend: {      data: ['线条1', '线条2']   },
//		        xAxis: {  data: xNames  }, 
//		        series: [   
//		                 { name: lineName,    type: 'line',   data: lineValues,  },
//		                 { name: lineName,    type: 'line',   data: lineValues,  }, 
		
		//x1, x2, x3, x4
		List listXs = MapListHelp.toArrayAndTurn(baseService.find("select lpad(level, 2, '0') lev from dual connect by level <=12")).get(0);
		List listLineNames = MapListHelp.array().build();
		int yearFrom = Tools.parseInt(request.getParameter("TIMEFROM"));
		int yearTo = Tools.parseInt(request.getParameter("TIMETO")); 
		List listSeries = MapListHelp.array().build(); 
		for(int i = yearFrom; i <= yearTo; i++){
			//yi-1, yi-2, yi-3, yi-4
			listSeries = MapListHelp.listAdd(listSeries,  MapListHelp.toArrayAndTurn(baseService.find(
					"SELECT nvl(t1.y, '0') y FROM ( SELECT t.xs x,count(*) y FROM ( SELECT s.*,to_char(s.time, 'MM') xs FROM student s where to_char(s.time, 'yyyy')=?  ) t group by t.xs ) t1,(select lpad(level, 2, '0') lev from dual connect by level <=12   ) t2 where t1.x(+) = t2.lev order by t2.lev  "
					, i)) ); 
			listLineNames.add(i + ""); 
		}
		//共x轴 多线数据 
		//x1, x2, x3, x4
		//y1, y2, y3, y4 
		//y5, y6, y7, y8   
		String type = "bar";	
		Map title = MapListHelp.map().put("text", "注册统计").build();		//标题
		Map legend = MapListHelp.map().put("data", listLineNames).build();   //线条名字集合
		Map xAxis = MapListHelp.map().put("data", listXs).build();  	//x坐标集合 多线条共x轴
		List series = MapListHelp.array().build();
		for(int i = 0; i < listSeries.size(); i++){
			//type = i / 2 == 0 ? "bar" : "line"; 
			series.add(MapListHelp.map()
					.put("name", listLineNames.get(i))	//该线条的名字
					.put("type", type)					//该线条的显示方式line bar pie
					.put("data", listSeries.get(i))			//该线条的y值集合
					.build()
				);
		} 
		Map option = MapListHelp.map()
				.put("title", title)  
				.put("legend", legend) 
				.put("tooltip", new Object()) //若无则不能预览
				.put("xAxis", xAxis) 
				.put("yAxis", new Object()) //若无则报错YAxis 0 not found
				.put("series", series) 
				.build();
		 

		Map res = MapListHelp.getMap()
				.put("res", "true")
				.put("option", option) 
				.put("info", WebHelp.getRequestMap(request)).build(); 
		log(res);
		writeJson(response, res);
	}	
	
	
	@RequestMapping("/login.do") 
	public void login(HttpServletRequest request, HttpServletResponse response) throws IOException { 
		Map res = MapListHelp.getMap().put("res", "true").put("info",WebHelp.getRequestMap(request)).build(); 
		log(res);
		writeJson(response, res);
	}	
	 
	@RequestMapping("/update.do")
	public void update(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String id = request.getParameter("ID"); 
		String name = request.getParameter("NAME");
		String time = request.getParameter("TIME");
	    
	    Map res = null;
	    if(studentServiceHibernate.get(id).isEmpty()){
	    	res = MapListHelp.getMap().put("res",studentServiceHibernate.add(name, time)).build();
	    }else{
	    	res = MapListHelp.getMap().put("res",studentServiceHibernate.update(id, name, time)).build();
	    }
		writeJson(response, res);
	}
	@RequestMapping("/delete.do")
	public void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String id = request.getParameter("ID");
	    
	    Map res = MapListHelp.getMap().put("res",studentServiceHibernate.delete(id)).build();
		writeJson(response, res);
	}	
	@RequestMapping("/get.do")
	public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//Map res = MapListHelp.getMap().put("id", "id-001").put("key", "key-001").put("username", "username-001").build();
		String id = request.getParameter("id");   
		Map res = studentServiceHibernate.get(id );
		log(res); 
		writeJson(response, res);
	}	
	@RequestMapping("/list.do")
	public void list(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String id = request.getParameter("ID");
		String name = request.getParameter("NAME");
		String timefrom = request.getParameter("TIMEFORM");
		String timeto = request.getParameter("TIMETO");
		
		Page page = getPage(request);
		List<Map> list = studentServiceHibernate.list(id, name, timefrom, timeto, page);
		log(list, page);
		writeJson(response, list, page);
	}
	@RequestMapping("/listrecent.do")
	public void listRecent(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String count = request.getParameter("count"); 
		 
		List<Map> list = baseService.find("select * from (select s.*, rownum num from student s) where num < ? order by time desc ", count);
		writeJson(response, list);
	} 
	
	static public Logger logger = LoggerFactory.getLogger(AngularControll.class); 

	@Override
	public void log(Object... objs) {
		 logger.info(Tools.getString(objs));
	}
    
}