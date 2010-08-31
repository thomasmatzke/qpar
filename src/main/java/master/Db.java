package main.java.master;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

//import org.apache.commons.dbutils.QueryRunner;
//import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

public class Db {
	
	static Logger logger = Logger.getLogger(MasterDaemon.class);
	static Connection conn; 
	
	//public static Object lock = new Object();
	
//	static public void shutdown() throws SQLException {
//
//		Statement st = conn.createStatement();
//
//		// db writes out to files and performs clean shuts down
//		// otherwise there will be an unclean shutdown
//		// when program ends
//		st.execute("SHUTDOWN");
//		conn.close(); // if there are no other open connection
//	}
//
//	// use for SQL command SELECT
//	static public synchronized List<Map<String,Object>> query(String query) {
//		List<Map<String,Object>> result = null;
//	    try {
//	        QueryRunner qrun = new QueryRunner();
//	        result = qrun.query(conn, query, new MapListHandler());
//	    } catch (Exception ex) {
//	        logger.error(ex);
//	        MasterDaemon.bailOut();
//	    }
//	    return result;
//	}
//
//	// use for SQL commands CREATE, DROP, INSERT and UPDATE
//	static public synchronized void update(String expression) {
//		try {
//			Statement st = null;
//	
//			st = conn.createStatement(); // statements
//	
//			int i = st.executeUpdate(expression); // run the query
//	
//			if (i == -1) {
//				logger.error("db error : " + expression);
//				MasterDaemon.bailOut();
//			}
//	
//			st.close();
//		} catch (SQLException e) {
//			logger.error(e);
//			MasterDaemon.bailOut();
//		}
//	} 

}
