/**
 * Copyright (c) 2013 minglin. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sync.server.http;

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
