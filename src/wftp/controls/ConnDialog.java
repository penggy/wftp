package wftp.controls;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import wftp.model.ConnNode;
import wftp.model.ConnNodeFactory;
import wftp.model.TreeNode;

public class ConnDialog extends TitleAreaDialog {
	
	private TreeNode pNode; // ��ǰѡ�еĽڵ�
	private boolean bFix; // �Ƿ��޸Ľڵ�
	private Button anonChk; // ������¼
	private Text label;
	private Text host;
	private Text port;
	private Text username;
	private Text password;
	private Combo encodeCombo;

	public ConnDialog(Shell parent, TreeNode node, boolean bFix) {
		super(parent);
		this.pNode = node;
		this.bFix = bFix;
	}

	public ConnDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		FocusListener focusListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if(e.getSource() == host && label.getText().trim().equals("")) {
					label.setText(host.getText());
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (e.getSource() instanceof Text) {
					Text text = (Text) e.getSource();
					text.selectAll();
				}
			}
		};
		Composite topCompo = new Composite(parent, SWT.NONE);
		topCompo.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.horizontalSpacing = 10;
		topCompo.setLayout(gridLayout);
		
		new Label(topCompo, SWT.NONE).setText("��ǩ: ");
		this.label = new Text(topCompo, SWT.BORDER);
		this.label.addFocusListener(focusListener);
		GridData labelData = new GridData(GridData.FILL_HORIZONTAL);
		labelData.horizontalSpan = 3;
		this.label.setLayoutData(labelData);
		
		new Label(topCompo, SWT.NONE).setText("��ַ: ");
		this.host = new Text(topCompo, SWT.BORDER);
		this.host.addFocusListener(focusListener);
		GridData hostData = new GridData(GridData.FILL_HORIZONTAL);
		hostData.horizontalSpan = 3;
		this.host.setLayoutData(hostData);
		
		new Label(topCompo, SWT.NONE).setText("�˿�: ");
		this.port = new Text(topCompo, SWT.BORDER);
		this.port.setText(String.valueOf(ConnNode.DEFAULT_PORT));
		this.port.addFocusListener(focusListener);
		this.port.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				Pattern pattern = Pattern.compile("[0-9]+");
				Matcher matcher = pattern.matcher(e.text);
				if (matcher.matches()) // ��������
					e.doit = true;
				else if (e.text.length() > 0) // ���ַ����,�������ġ��ո�
					e.doit = false;
				else
					e.doit = true;
			}
		});
		this.port.setLayoutData(new GridData(40, -1));
		
		new Label(topCompo, SWT.NONE).setText("������¼: ");
		this.anonChk = new Button(topCompo, SWT.CHECK);
		this.anonChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				username.setEnabled(!anonChk.getSelection());
				if (!username.isEnabled())
					username.setText(ConnNode.DEFAULT_USER);
				else {
					username.setText("");
					username.forceFocus();
				}
				password.setEnabled(!anonChk.getSelection());
			}
		});
		
		new Label(topCompo, SWT.NONE).setText("��¼��: ");
		this.username = new Text(topCompo, SWT.BORDER);
		this.username.addFocusListener(focusListener);
		GridData usernameData = new GridData(120, -1);
		usernameData.horizontalSpan = 3;
		this.username.setLayoutData(usernameData);
		
		new Label(topCompo, SWT.NONE).setText("����: ");
		this.password = new Text(topCompo, SWT.PASSWORD | SWT.BORDER);
		GridData passwordData = new GridData(120, -1);
		passwordData.horizontalSpan = 3;
		this.password.addFocusListener(focusListener);
		this.password.setLayoutData(passwordData);
		
		new Label(topCompo,SWT.NONE).setText("����: ");
		this.encodeCombo = new Combo(topCompo, SWT.BORDER);
		this.encodeCombo.add("GBK");
		this.encodeCombo.add("ISO-8859-1");
		this.encodeCombo.add("UTF-8");
		this.encodeCombo.setText(ConnNode.DEFAULT_ENCODE);
		GridData encodeData = new GridData(105,-1);
		encodeData.horizontalSpan = 3;
		this.encodeCombo.setLayoutData(encodeData);
		
		
		if (bFix && pNode instanceof ConnNode) {
			ConnNode node = (ConnNode) pNode;
			label.setText(node.getLabel());
			host.setText(node.getHost());
			port.setText(String.valueOf(node.getPort()));
			username.setText(node.getUsername());
			if(ConnNode.DEFAULT_USER.equals(node.getUsername())) {
				this.anonChk.setSelection(true);
				username.setEnabled(false);
				password.setEnabled(false);
			}else {
				password.setText(node.getPassword());
			}
			encodeCombo.setText(node.getEncode());
		}
		return topCompo;
	}

	private void storeConn() {
		if (!bFix) {
			ConnNode newNode = new ConnNode();
			newNode.setLabel(label.getText());
			newNode.setHost(host.getText());
			try{
				newNode.setPort(Integer.parseInt(port.getText()));
			}catch (NumberFormatException e) {
				newNode.setPort(ConnNode.DEFAULT_PORT);
			}
			newNode.setUsername(username.getText());
			newNode.setPassword(password.getText());
			newNode.setEncode(encodeCombo.getText());
			ConnNodeFactory.getInstance().save(pNode, newNode);
		} else {
			ConnNode node = (ConnNode) pNode;
			node.setLabel(label.getText());
			node.setHost(host.getText());
			try{
				node.setPort(Integer.parseInt(port.getText()));
			}catch (NumberFormatException e) {
				node.setPort(ConnNode.DEFAULT_PORT);
			}
			node.setUsername(username.getText());
			node.setPassword(password.getText());
			node.setEncode(encodeCombo.getText());
			ConnNodeFactory.getInstance().update(node);
		}
	}

	private boolean validate() {
		if (host.getText().trim().equals("")) {
			setErrorMessage("��ַ����");
			host.setFocus();
			return false;
		}
		if (port.getText().trim().equals("")) {
			setErrorMessage("�˿ڱ���");
			port.setFocus();
			return false;
		}
		if (username.getText().trim().equals("")) {
			setErrorMessage("��¼������");
			username.setFocus();
			return false;
		}
		try{
			new String("".getBytes(),encodeCombo.getText());
		}catch (UnsupportedEncodingException e) {
			setErrorMessage("��Ч����");
			encodeCombo.setFocus();
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	protected void buttonPressed(int buttonId) {

		if (buttonId == IDialogConstants.OK_ID) {
			if (!validate())
				return;
			storeConn();
		}

		super.buttonPressed(buttonId);
	}

	@Override
	protected Control createContents(Composite parent) {
		super.createContents(parent);
		getShell().setText("�༭����");
		setTitle("�༭����");
		setMessage("����FTP���ӱ�Ҫ�Ĳ���");
		return parent;
	}

	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		p.x = 320;
		return p;
	}

}
