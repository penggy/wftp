package wftp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private int width, height;
	private int x, y;
	private HookSysTray tray;

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		Rectangle clientArea = Display.getCurrent().getClientArea();
		width = clientArea.width * 9 / 10;
		height = clientArea.height * 9 / 10;
		x = (clientArea.width - width) / 2;
		y = (clientArea.height - height) / 2;
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(width, height));
		configurer.setShowMenuBar(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowPerspectiveBar(false);
		configurer.setShowCoolBar(false);
		configurer.setTitle("WFTP Application");
		configurer.setShowFastViewBars(true);
		configurer.setShowStatusLine(true);
	}

	@Override
	public void postWindowCreate() {
		super.postWindowCreate();
		final Shell shell = getWindowConfigurer().getWindow().getShell();
		shell.setLocation(x, y);
		tray = HookSysTray.getInstance(shell);
		if (tray != null) {
			tray.setVisible(true);
		}
	}

	@Override
	public boolean preWindowShellClose() {
		if (tray != null) {
			tray.windowClose();
			return false;
		} else {
			return super.preWindowShellClose();
		}
	}

}
