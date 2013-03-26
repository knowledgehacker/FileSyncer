package main.sync.server.http;

/*
import java.sql.*;
import javax.sql.*;
*/

import java.util.Hashtable;

/**
 * UserDatabase stores user information in a database, such as user name, password, gender, etc.
 * and access to the underlying database via JDBC API.
 * We don't need to synchronize the operations to the underlying database,
 * since "exists" and "add" operations inovked by the same user will not interleave at any time.
 */
/**
 * Currently, to find problems in the prototype as soon as possible, we use a HashTable to simulate
 * the underlying database, the HashMap contains only user name and password.
 * Note the differences between HashMap and HashTable are: 1) HashTable is thread-safe, while HashMap
 * isn't. 2) HashMap permits null keys and null values, while HashTable doesn't.
 */
public class UserDatabase {
/*
	private final String dbUrl;

	public UserDatabase() {
		dbUrl = ServerSetting.getProperty("server.user.database.url");
	}

	public boolean exists(String userName) {
		Connection dbConn = DriverManager.getConnection(dbUrl);
		// To implement ...

		return false;
	}

	public void add(String userName) {
		Connection dbConn = DriverManager.getConnection(dbUrl);
		// To implement ...
	}
*/

	private Hashtable<String, String> userInfos;
	
	public UserDatabase() {
		userInfos = new Hashtable<String, String>();
	}

	public boolean exists(String user) {
		return userInfos.containsKey(user);
	}

	public void add(String user, String password) {
		userInfos.put(user, password);
	}
}
