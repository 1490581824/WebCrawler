package com.yuli.netspider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class mainPro {

	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//连接数据库
		String frontpage = utils.URLS;
		Connection conn = null;
		
        
        
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dburl = "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8";
			conn = DriverManager.getConnection(dburl, utils.USER, utils.PASSWORD);
			
			if(!conn.isClosed()) {
				System.out.println("数据库连接成功！");
			}
			else {
				System.out.println("数据库连接失败！");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//创建数据库，完成初始化
		String sql = null;
        String url = frontpage;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
		if(conn != null) {
			try{
				sql = "CREATE DATABASE IF NOT EXISTS crawler";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				sql = "USE crawler";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				sql = "create table if not exists record (recordID int(5) not null auto_increment, URL text not null, crawled tinyint(1) not null, primary key (recordID)) engine=InnoDB DEFAULT CHARSET=utf8";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				sql = "create table if not exists webcontent (contentID int(5) not null auto_increment, URL2 text not null, webContent text, primary key (contentID)) engine=InnoDB DEFAULT CHARSET=utf8";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				sql = "create table if not exists tags (tagnum int(4) not null auto_increment, tagname text not null, primary key (tagnum)) engine=InnoDB DEFAULT CHARSET=utf8";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				sql = "INSERT INTO record (URL, crawled) VALUES ('" + frontpage + "',0)";
    			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    			pstmt.execute();
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			//开始爬
			while(true) {
				//获取网页的连接
				httpGet.getByString(url,conn);
				count++;
				
				//set boolean value "crawled" to true after crawling this page
				sql = "UPDATE record SET crawled = 1 WHERE URL = '" + url + "'";
				stmt = conn.createStatement();
				
				if(stmt.executeUpdate(sql)>0) {
					//获取下一个页面
					sql = "SELECT * FROM record WHERE crawled = 0";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if(rs.next()) {
						url = rs.getString(2);
					}else {
						//如果到达表尾，则停止爬
						break;
					}
					
					//设置爬取的数量限制
					if(count>1000 || url == null) {
						break;
					}
				}
			}
			conn.close();
			conn = null;
			
			System.out.println("完成");
			System.out.println("爬取的网页数量: "+(count-1));
		}
	}

}
