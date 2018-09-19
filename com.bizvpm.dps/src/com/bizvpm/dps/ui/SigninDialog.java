package com.bizvpm.dps.ui;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bizvpm.dps.Activator;

public class SigninDialog extends Dialog {

	private static final String DEFAULT_MSG = "请输入您的DFS账户信息";

	private Text textHostName;

	private Text textHostPassword;

	private String hostName;

	private String password;

	private String hostIp;

	private int hostPort;

	private Text textHostPasswordRepeat;

	private Label labelHostPasswordRepeat;

	private boolean signupMode;

	private Label message;

	protected SigninDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		message = new Label(composite, SWT.WRAP);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		gd.heightHint = 48;
		message.setLayoutData(gd);
		message.setText(DEFAULT_MSG);

		FocusAdapter focusAdapter = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				message.setText(DEFAULT_MSG);
			}
		};

		new Label(composite, SWT.NONE).setText("输入您的登录账户");
		textHostName = new Text(composite, SWT.BORDER);
		textHostName.addFocusListener(focusAdapter);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = 200;
		textHostName.setLayoutData(gd);

		new Label(composite, SWT.NONE).setText("输入您的登录密码");
		textHostPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		textHostPassword.addFocusListener(focusAdapter);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = 200;
		textHostPassword.setLayoutData(gd);

		labelHostPasswordRepeat = new Label(composite, SWT.NONE);
		labelHostPasswordRepeat.setText("请再次输入登录密码");
		textHostPasswordRepeat = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		textHostPasswordRepeat.addFocusListener(focusAdapter);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = 200;
		textHostPasswordRepeat.setLayoutData(gd);

		setSignupMode(false);

		loadDefaultValue();
		return composite;
	}

	private void setSignupMode(boolean b) {
		labelHostPasswordRepeat.setVisible(b);
		textHostPasswordRepeat.setVisible(b);

		textHostName.setText("");
		textHostPassword.setText("");
		textHostPasswordRepeat.setText("");

		Button button = getButton(IDialogConstants.OPEN_ID);
		if (button != null) {
			button.setVisible(!b);
		}
		getShell().setText(b ? "注册DPS账户" : "连接DPS服务器");
		this.signupMode = b;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OPEN_ID, "注册", false);
		createButton(parent, IDialogConstants.OK_ID, "确认", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
	}

	private void loadDefaultValue() {
		IPreferenceStore store = Activator.getStore();
		String hostName = store.getString("hostName");
		if (!hostName.isEmpty()) {
			textHostName.setText(hostName);
		}
		String password = store.getString("password");
		if (!password.isEmpty()) {
			textHostPassword.setText(password);
		}
		int hostPort = store.getInt("port");
		if (hostPort != 0) {
			this.hostPort = hostPort;
		}
	}

	private void saveDefaultValue() {
		IPreferenceStore store = Activator.getStore();
		store.setValue("hostName", hostName);
		store.setValue("password", password);
		store.setValue("port", hostPort);
	}

	@Override
	protected void okPressed() {
		try {
			if (signupMode) {
				signUp();
			} else {
				signIn();
			}
			saveDefaultValue();
			super.okPressed();
		} catch (Exception e) {
			message.setText(e.getMessage());
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OPEN_ID == buttonId) {
			setSignupMode(true);
		} else if (IDialogConstants.CANCEL_ID == buttonId && signupMode) {
			setSignupMode(false);
		} else {
			super.buttonPressed(buttonId);
		}
	}

	private void signIn() throws Exception {
		hostName = textHostName.getText();
		password = textHostPassword.getText();
		if (hostName.trim().isEmpty()) {
			throw new Exception("您必须输入账户名称");
		}
		if (password.isEmpty()) {
			throw new Exception("您必须输入账户登录密码");
		}

		checkIpAddressAndPort();

		boolean b = Activator.getServer().signin(hostName, password, hostIp,
				hostPort);
		if (!b) {
			throw new Exception("服务器无法验证您的账户，请检查账户名称和密码");
		}

	}

	private void signUp() throws Exception {
		hostName = textHostName.getText();
		password = textHostPassword.getText();
		String password2 = textHostPasswordRepeat.getText();
		if (hostName.trim().isEmpty()) {
			throw new Exception("您必须设置账户名称");
		}
		if (password.isEmpty()) {
			throw new Exception("您必须设置账户登录密码");
		}
		if (!password.equals(password2)) {
			throw new Exception("您两次输入的密码不一致");
		}

		checkIpAddressAndPort();

		boolean b = Activator.getServer().signup(hostName, password, hostIp,
				hostPort);
		if (!b) {
			throw new Exception("注册失败，您的账户名称已被占用。");
		}
	}

	private void checkIpAddressAndPort() {
		hostIp = Activator.getServer().ping();
		hostPort = createPort(26000);
	}

	private int createPort(int defaultPort) {
		int port = defaultPort;
		Socket s = new Socket();
		while (true) {
			try {
				s.bind(new InetSocketAddress(hostIp, port));
				s.close();
				return port;
			} catch (IOException e) {
				port++;
			}
		}
	}

	public String getHostName() {
		return hostName;
	}

	public String getHostIp() {
		return hostIp;
	}

	public int getHostPort() {
		return hostPort;
	}

	public String getPassword() {
		return password;
	}

}
