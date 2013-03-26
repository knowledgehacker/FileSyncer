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

package main.sync.ssh;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import com.jcraft.jsch.UserInfo;

public class SpecificUserInfo implements UserInfo {
	private String passphrase;
	private String password;

	public String getPassphrase() {
		return passphrase;
	}

	public String getPassword() {
		return password;
	}

	public boolean promptPassword(String message) {
		Console console = System.console();
		password = new String(console.readPassword(message + ": "));

		return true;
	}

	public boolean promptPassphrase(String message) {
		Console console = System.console();
		passphrase = new String(console.readPassword(message + ": "));

		return true;
	}

	public boolean promptYesNo(String message) {
		System.out.print(message);

		String yesNo = null;
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		try{
			yesNo = br.readLine();
		}catch(IOException ioe) {
			System.out.println("Read yes/no from standard input failed.");
			return false;
		}

		return yesNo.equals("yes");
	}

	public void showMessage(String message) {
		System.out.println(message);
	}
}
