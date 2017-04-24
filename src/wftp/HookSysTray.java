package wftp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class HookSysTray {
	private Shell shell;
	private TrayItem trayItem;
	private ToolTip tip;
	private Menu menu;

	private HookSysTray(final Shell shell) {
		this.shell = shell;
		final Tray tray = shell.getDisplay().getSystemTray();
		tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
		tip.setAutoHide(true);
		tip.setText("wftp系统托盘");
		tip.setMessage("右键单击图标, \n可以选择菜单");
		trayItem = new TrayItem(tray, SWT.NONE);
		trayItem.setVisible(false);
		trayItem.setText("wftp");
		trayItem.setImage(ImageContext.getImage(ImageContext.WFTP_16));
		trayItem.setToolTip(tip);
		menu = new Menu(shell, SWT.POP_UP);
		fillMenuItems();
	}

	private void fillMenuItems() {
		final MenuItem showItem = new MenuItem(menu, SWT.PUSH);
		showItem.setText(shell.isVisible() ? "隐藏(&Hide)" : "显示(&Show)");
		showItem.setImage(ImageContext
				.getImage(ImageContext.APPLICATION_LIGHTNING));
		showItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.setVisible(!shell.isVisible());
				showItem.setText(shell.isVisible() ? "隐藏(&Hide)" : "显示(&Show)");
			}

		});
		/**
		 * 这一段为了让程序最小化到系统托盘
		 */
//		shell.addShellListener(new ShellAdapter() {
//
//			@Override
//			public void shellIconified(ShellEvent e) {
//				windowClose();
//			}
//
//		});
		MenuItem exitItem = new MenuItem(menu, SWT.PUSH);
		exitItem.setText("退出(E&xit)");
		exitItem.setImage(ImageContext.getImage(ImageContext.EXIT));
		exitItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				HookSysTray.this.dispose();
			}

		});
		trayItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				shell.setVisible(true);
				shell.setMinimized(false);
				shell.setActive();
			}
		});
		trayItem.addListener(SWT.DefaultSelection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				shell.setVisible(true);
				shell.setMinimized(false);
				shell.setActive();

			}
		});
		trayItem.addListener(SWT.MenuDetect, new Listener() {

			@Override
			public void handleEvent(Event event) {
				showItem.setText(shell.isVisible() ? "隐藏(&Hide)" : "显示(&Show)");
				menu.setVisible(true);
			}
		});
	}

	public void setVisible(boolean visible) {
		trayItem.setVisible(visible);
	}

	public static HookSysTray getInstance(final Shell shell) {
		final Tray tray = shell.getDisplay().getSystemTray();
		if(tray == null) {
			return null;
		}
		return new HookSysTray(shell);
	}

	public void windowClose() {
		shell.setVisible(false);
		tip.setVisible(true);
	}
	
	public void dispose() {
		Activator.getDefault().getWorkbench().close();
	}

}
