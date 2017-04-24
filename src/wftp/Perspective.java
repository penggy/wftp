package wftp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;

import wftp.views.ConnView;
import wftp.views.LocalView;
import wftp.views.RemoteView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		IFolderLayout connFolder = layout.createFolder("left",
				IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		connFolder.addView(ConnView.class.getName());
		IPlaceholderFolderLayout bottomHolder = layout.createPlaceholderFolder(
				"bottomFolder", IPageLayout.BOTTOM, 0.7f, IPageLayout.ID_EDITOR_AREA);
		bottomHolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		bottomHolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW+":*");
		bottomHolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID+":*");
		IPlaceholderFolderLayout remoteHolder = layout.createPlaceholderFolder(
				"remoteFolder", IPageLayout.LEFT, 0.5f,
				IPageLayout.ID_EDITOR_AREA);
		remoteHolder.addPlaceholder(RemoteView.class.getName() + ":*");
		remoteHolder.addPlaceholder(RemoteView.class.getName());
		layout.addStandaloneView(LocalView.class.getName(), true,
				IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.getViewLayout(RemoteView.class.getName()).setCloseable(true);
		layout.getViewLayout(LocalView.class.getName()).setCloseable(false);
	}
	
}
