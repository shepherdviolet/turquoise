package sviolet.lib.utils;

import java.sql.*;

public class SQLHelper {
	
	//驱动程序名
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    
    Statement statement = null;
    Connection conn = null;
	
    /**
	 *MySQL连接助手<p>
	 *
	 *注意：<p>
	 *1.操作完毕后务必close();<p>
	 *2.结果集使用完毕后close();<p>
	 *3.注意多线程同步<p>
	 *
	 *导入JAR包：<p>
	 *C:\Program Files\MySQL\MySQL Connector J\mysql-connector-java-5.1.20-bin.jar<p>
	 *
     * @param url 指向要访问的数据库名,例："jdbc:mysql://127.0.0.1:3306/bank?useUnicode=true&characterEncoding=utf8"
     * @param user MySQL配置时的用户名
     * @param password MySQL配置时的密码
     * @throws ClassNotFoundException 驱动不存在
     * @throws SQLException 
     */
	public SQLHelper(String url,String user,String password) throws ClassNotFoundException, SQLException{

         // 加载驱动程序
         Class.forName(DRIVER);

         // 连续数据库
         conn = DriverManager.getConnection(url, user, password);

         // statement用来执行SQL语句
         statement = conn.createStatement();
	}
	
	/**
	 * 断开数据库连接
	 */
	public void close(){
		if(conn!=null)
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	/**
	 * 更新操作
	 * 
	 * @param sql SQL语句
	 * @return int 状态
	 * @throws SQLException
	 */
	public int Update(String sql) throws SQLException{
		return statement.executeUpdate(sql);
	}
	
	/**
	 * 查询操作
	 * 
	 * 注意：返回的ResultSet务必close();
	 * 
	 * 例：
	 * while(rs.next()) {
     *     id = rs.getString("id");
     *     id = new String(id.getBytes("ISO-8859-1"),"GB2312");
     * }
	 * 
	 * @param sql SQL语句
	 * @return ResultSet 结果集
	 * @throws SQLException
	 */
	public ResultSet Query(String sql) throws SQLException{
		return statement.executeQuery(sql);
	}
}
