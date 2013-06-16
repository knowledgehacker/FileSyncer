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

package sync.client.spdy;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import spdy.api.client.SPDYClientHelper;

public class LoginUI implements ActionListener {
	private String userName;
	private String password;
	private String deviceId;
	
	private final LoginUtils loginUtils;
	
	private final JFrame frame;
	private final MyKeyListener unListener;
	private final MyKeyListener pwdListener;
	private final MyKeyListener diListener;
	
	public LoginUI(SPDYClientHelper helper, short spdyVersion, String hostname, int port) {
		loginUtils = new LoginUtils(helper, spdyVersion, hostname, port);

		// label and textfield for user name
		unListener = new MyKeyListener();
		JPanel unPanel = createAJPanel("user name: ", unListener);
	
		// label and textfield for user password
		pwdListener = new MyKeyListener();
		JPanel pwdPanel = createAJPanel("password: ", pwdListener);
		
		// label and textfield for device id
		diListener = new MyKeyListener();
		JPanel diPanel = createAJPanel("device id: ", diListener);	

		// buttons: "Submit" and "Cancel"	
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setPreferredSize(new Dimension(120, 80));
		BoxLayout buttonsLayout = new BoxLayout(buttonsPanel, BoxLayout.X_AXIS);	
		buttonsPanel.setLayout(buttonsLayout);
		JButton submitButton = new JButton("Submit");
		submitButton.setActionCommand("Submit");
		submitButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		buttonsPanel.add(submitButton);
		buttonsPanel.add(cancelButton);

        frame = new JFrame("login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addNotify();
        //frame.setSize(frame.getInsets().left + 405, frame.getInsets().top + 324);
        frame.setSize(120, 200);
        frame.add(diPanel, BorderLayout.NORTH);
        frame.add(unPanel, BorderLayout.WEST);
        frame.add(pwdPanel, BorderLayout.EAST);
        frame.add(buttonsPanel, BorderLayout.SOUTH);
	}

	public void show() {
		frame.setSize(250, 250);
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
    	if ("Submit".equals(e.getActionCommand())) {
			System.out.println("Submit button is clicked");

			userName = unListener.getText();
			password = pwdListener.getText();
			deviceId = diListener.getText();
			loginUtils.auth(userName, password, deviceId);
    	} else {
			System.out.println("Cancel button is clicked");
    	}
	}

	private class MyKeyListener implements KeyListener {
	 	private String text = "";
		
		public void keyPressed(KeyEvent e) {

		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
			text += e.getKeyChar();
		}

		public String getText() {
			return text;
		}
	}
	
	private JPanel createAJPanel(String labelName, MyKeyListener listener) {
		JPanel jPanel = new JPanel();
		jPanel.setPreferredSize(new Dimension(120, 60));
		BoxLayout layout = new BoxLayout(jPanel, BoxLayout.X_AXIS);
		jPanel.setLayout(layout);
		JLabel jLabel = new JLabel(labelName, SwingConstants.LEFT);
		JTextField jTextField = new JTextField();
		jTextField.addKeyListener(listener);
		jPanel.add(jLabel);	
		jPanel.add(jTextField);
		
		return jPanel;
	}
	
	public final String getUserName() {
		return userName;
	}

	public final String getDeviceId() {
		return deviceId;
	}
	
	/*
	public static void main(String[] args) {
		LoginUI lui = new LoginUI();
		lui.show();
	}
	*/
}
