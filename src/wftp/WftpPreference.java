package wftp;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class WftpPreference extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static final String DIR_KEY = "$DIR_KEY";
	public static final String TIMEOUT_KEY = "$TIMEOUT_KEY";

	public static final int DEFAULT_TIMEOUT = 10;
	public static final String DEFAULT_LOCALDIR = FileSystemView
			.getFileSystemView().getHomeDirectory().getAbsolutePath();

	private IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
	private Text localDirText;
	private Button dirSelect;
	private Text timeoutText;

	@Override
	protected Control createContents(Composite parent) {
		Composite topComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		topComp.setLayout(layout);
		CLabel brand = new CLabel(topComp,SWT.NONE);
		brand.setImage(ImageContext.getImage(ImageContext.ABOUT));
		brand.setText("欢迎使用wftp.\n"+
				"wftp以eclipse rcp的形式开发.\n"+
				"历时: 2010/11/05 - 2010/12/16.\n"+
				"仅以此纪念我在甘肃兰州出差的日子.\n"+
				"您可以自由使用wftp,希望将您在使用\n"+
				"中发现的bug或者一些好的建议告知我.\n"+
				"谢谢!\n\n"+
				"联系我:\n"+
				"email : peng.wu@foxmail.com\n"+
				"qq : 378150060\n\n"+
				"----------------------华丽的分割线---------------------\n"+
				"至高的SUN,伟大的IBM. All right reserved.");
		GridData brandData = new GridData(GridData.FILL_BOTH);
		brandData.horizontalSpan = 3;
		brand.setLayoutData(brandData);
		new Label(topComp, SWT.NONE).setText("选择默认本地目录 : ");
		localDirText = new Text(topComp, SWT.BORDER);
		localDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirSelect = new Button(topComp, SWT.NONE);
		dirSelect.setText("选 择 ...");
		dirSelect.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDlg = new DirectoryDialog(getShell());
				String dir = dirDlg.open();
				localDirText.setText(dir);
			}

		});
		new Label(topComp, SWT.NONE).setText("设置连接超时(s) : ");
		timeoutText = new Text(topComp, SWT.BORDER);
		timeoutText.setLayoutData(new GridData(40, -1));
		timeoutText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				Pattern pattern = Pattern.compile("[0-9]+");
				Matcher matcher = pattern.matcher(e.text);
				if (matcher.matches()) // 处理数字
					e.doit = true;
				else if (e.text.length() > 0) // 有字符情况,包含中文、空格
					e.doit = false;
				else
					e.doit = true;
			}
		});

		String dir = ps.getString(DIR_KEY);
		if (dir == null || dir.trim().equals("")) {
			localDirText.setText(DEFAULT_LOCALDIR);
		} else {
			localDirText.setText(dir);
		}
		int timeout = ps.getInt(TIMEOUT_KEY);
		if (timeout == 0) {
			timeoutText.setText(String.valueOf(DEFAULT_TIMEOUT));
		} else {
			timeoutText.setText(String.valueOf(timeout));
		}
		return topComp;
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	private void doSave() {
		ps.setValue(DIR_KEY, localDirText.getText());
		ps.setValue(TIMEOUT_KEY, Integer.parseInt(timeoutText.getText()));
	}

	private boolean check() {
		File dir = new File(localDirText.getText());
		if (!dir.exists() || !dir.isDirectory()
				|| !FileSystemView.getFileSystemView().isFileSystem(dir)) {
			setErrorMessage("目录无效");
			return false;
		}
		try {
			int timeout = Integer.parseInt(timeoutText.getText());
			if (timeout <= 0) {
				setErrorMessage("超时时间必须大于0");
				return false;
			}
		} catch (NumberFormatException e) {
			setErrorMessage("超时设置无效");
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		localDirText.setText(DEFAULT_LOCALDIR);
		timeoutText.setText(String.valueOf(DEFAULT_TIMEOUT));
	}

	@Override
	public boolean performOk() {
		if (!check()) {
			return false;
		}
		doSave();
		return super.performOk();
	}

	@Override
	protected void performApply() {
		if (!check()) {
			return;
		}
		doSave();
		super.performApply();
	}

}
