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

package test.sync.ssh;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import main.sync.ssh.Config;
import main.sync.ssh.FileSyncer;

public class TestFileSyncer {
	public static void main(String args[]) {
		String lcwd = "/home/minglin/hadoop/tools";
		String uri = "minglin@datanode";
		FileSyncer syncer = new FileSyncer(lcwd, uri);
		syncer.initialize();

		try{
			Thread.sleep(Long.parseLong(Config.getProperty("check.delay.per.run"))/2);
		}catch(InterruptedException ie) {
			// nothing has to do...
		}

		System.out.println(System.currentTimeMillis() + ": Begin to create ...");
		// Create a new directory and a new file in it.
		File newDir = new File(lcwd, "newDir");
		if(!newDir.exists() && newDir.mkdir()) {
			//System.out.println("Directory \"" + newDir.getAbsolutePath() + "\" created.");
		}
		File newFile = new File(newDir, "newFile.txt");
		try{
			if(!newFile.exists() && newFile.createNewFile()) {
				//System.out.println("File \"" + newFile.getAbsolutePath() + "\" created.");
			}
		}catch(IOException ioe) {
			System.out.println("Create file \"" + newFile.getAbsolutePath() + "\" failed.");
		}

		try{
			Thread.sleep(Long.parseLong(Config.getProperty("check.delay.per.run")));
		}catch(InterruptedException ie) {
			// nothing has to do...
		}

		System.out.println(System.currentTimeMillis() + ": Begin to modify ...");
		// Modifify the new file.
		FileWriter fw = null;
		try{
			fw = new FileWriter(newFile, true);
			fw.write("a new line" + System.getProperty("line.separator"));
		}catch(IOException ioe) {
			System.out.println("main: Write to file \"" + newFile.getAbsolutePath() + "\" failed - " + ioe);
		}finally {
			try{
				fw.close();
			}catch(IOException ioe) {
				System.out.println("main: Close FileWriter of file \"" + newFile.getAbsolutePath() + "\" failed - " + ioe);
			}
		}

		try{
			Thread.sleep(Long.parseLong(Config.getProperty("check.delay.per.run")));
		}catch(InterruptedException ie) {
			// nothing has to do...
		}

		System.out.println(System.currentTimeMillis() + ": Begin to delete ...");
		// Delete the new file and the new directory.
		newFile.delete();
		newDir.delete();	

		try{
			Thread.sleep(Long.parseLong(Config.getProperty("check.delay.per.run"))*5);
		}catch(InterruptedException ie) {
			// nothing has to do...
		}

		syncer.finalize();
	}
}
