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

package sync.client;

import java.util.Vector;

/**
 * FileDoubleBuffer here is not thread-safe.
 * So the classes has FileDoubleBuffer component should provide mechanism to ensure 
 * thread-safety if the Pair component will be accessed by multiple threads.
 */
public class FileDoubleBuffer {
	private Vector<FileOp> inFops;
	private Vector<FileOp> outFops;

	public FileDoubleBuffer() {
		inFops = new Vector<FileOp>();
		outFops = new Vector<FileOp>();
	}

	/**
	 * The following methods operate on inFiles before inFiles and outFiles are swapped each run.
	 */
	public final void add(FileOp fop) {
		inFops.add(fop);
	}	

	public final boolean empty() {
		return inFops.isEmpty();
	}

	public final void swap() {
		Vector<FileOp> tmp = inFops;
		inFops = outFops;
		outFops = tmp;

		// Why clear inFops here doesn't work?
		//inFops.clear();
	}

	/**
	 * The following methods operate on outFiles after inFiles and outFiles are swapped each run.
	 * We return the private Vector "outFiles" directly, it is dangerous to do so. We should return a copy of it.
	 * But it will introduce copy overhead, since we it is used internally, we choose to return reference here.
	 */
	public final Vector<FileOp> get() {
		return outFops;
	}
}
