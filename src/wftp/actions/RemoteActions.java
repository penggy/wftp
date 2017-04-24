package wftp.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

import wftp.ImageContext;
import wftp.model.ConnNode;
import wftp.views.RemoteView;

public class RemoteActions extends ActionGroup implements
		ISelectionChangedListener {
	private RemoteView rv;
	private HomeAction homeAction = new HomeAction();
	private DownloadAction downloadAction = new DownloadAction();
	private UpwardAction upwardAction = new UpwardAction();
	private RefreshAction refreshAction = new RefreshAction();
	private DeleteAction deleteAction = new DeleteAction();
	private MakeDirAction makeDirAction = new MakeDirAction();
	private RenameFileAction renameFileAction = new RenameFileAction();
	private IHandlerActivation upwardHandlerAction;
	private IHandlerActivation refreshHandlerAction;
	private IHandlerActivation deleteHandlerAction;
	private IHandlerActivation renameHandlerAction;

	public RemoteActions(RemoteView rv) {
		this.rv = rv;
		IHandlerService handlerService = (IHandlerService) rv.getViewSite()
				.getService(IHandlerService.class);
		upwardHandlerAction = handlerService.activateHandler("wftp.upward",
				new ActionHandler(upwardAction));
		refreshHandlerAction = handlerService.activateHandler("org.eclipse.ui.file.refresh",
				new ActionHandler(refreshAction));
		deleteHandlerAction = handlerService.activateHandler("org.eclipse.ui.edit.delete",
				new ActionHandler(deleteAction));
		renameHandlerAction = handlerService.activateHandler("org.eclipse.ui.edit.rename",
				new ActionHandler(renameFileAction));
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		IToolBarManager toolBarMgr = actionBars.getToolBarManager();
		toolBarMgr.add(homeAction);
		toolBarMgr.add(upwardAction);
		toolBarMgr.add(refreshAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(makeDirAction);
		toolBarMgr.add(deleteAction);
		toolBarMgr.add(renameFileAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(downloadAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		MenuManager menuManager = (MenuManager) menu;
		menuManager.add(downloadAction);
		menuManager.add(new Separator());
		menuManager.add(makeDirAction);
		menuManager.add(deleteAction);
		menuManager.add(renameFileAction);
		menuManager.add(new Separator());
		menuManager.add(refreshAction);
		Table table = rv.getTableViewer().getTable();
		table.setMenu(menuManager.createContextMenu(table));
	}

	private class DeleteAction extends Action {

		public DeleteAction() {
			setText("删除");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		}

		@Override
		public void run() {
			if (MessageDialog.openConfirm(null, "删除确认", "确定删除选定的文件吗")) {
				rv.deleteSelections();
			}
		}

	}

	private class MakeDirAction extends Action {

		public MakeDirAction() {
			setText("创建目录");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.FOLDER_ADD));
		}

		@Override
		public void run() {
			rv.makeDir();
		}

	}

	private class RenameFileAction extends Action {

		public RenameFileAction() {
			setText("重命名");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.TEXTFIELD_RENAME));
		}

		@Override
		public void run() {
			rv.renameSelection();
		}

	}

	private class RefreshAction extends Action {

		public RefreshAction() {
			setText("刷新");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.REFRESH));
		}

		@Override
		public void run() {
			rv.refresh();
		}

	}

	private class DownloadAction extends Action {

		public DownloadAction() {
			setText("下载");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.IMPORT_WIZ));
		}

		@Override
		public void run() {
			rv.downloadSelections();
		}

	}

	private class UpwardAction extends Action {

		public UpwardAction() {
			setText("上级");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ISharedImages.IMG_TOOL_UP));
		}

		@Override
		public void run() {
			rv.upward();
		}

	}

	private class HomeAction extends Action {

		public HomeAction() {
			setText("首页");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.HOME_NAV));
		}

		@Override
		public void run() {
			rv.goHome();
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object[] sels = ((IStructuredSelection) event.getSelection()).toArray();
		if (sels.length > 0) {
			downloadAction.setEnabled(true);
			deleteAction.setEnabled(true);
		} else {
			downloadAction.setEnabled(false);
			deleteAction.setEnabled(false);
		}
		if(sels.length == 1) {
			renameFileAction.setEnabled(rv.isDownloadable());
		}else {
			renameFileAction.setEnabled(false);
		}
	}

	public void onConnect(ConnNode node) {
		homeAction.setEnabled(true);
		refreshAction.setEnabled(true);
		upwardAction.setEnabled(true);
		makeDirAction.setEnabled(true);
	}

	public void onDisconnect(ConnNode node) {
		homeAction.setEnabled(false);
		downloadAction.setEnabled(false);
		refreshAction.setEnabled(false);
		upwardAction.setEnabled(false);
		deleteAction.setEnabled(false);
		makeDirAction.setEnabled(false);
		renameFileAction.setEnabled(false);
	}

	public RemoteView getRv() {
		return rv;
	}

	public void setRv(RemoteView rv) {
		this.rv = rv;
	}

	public HomeAction getHomeAction() {
		return homeAction;
	}

	public void setHomeAction(HomeAction homeAction) {
		this.homeAction = homeAction;
	}

	public DownloadAction getDownloadAction() {
		return downloadAction;
	}

	public void setDownloadAction(DownloadAction downloadAction) {
		this.downloadAction = downloadAction;
	}

	public UpwardAction getUpwardAction() {
		return upwardAction;
	}

	public void setUpwardAction(UpwardAction upwardAction) {
		this.upwardAction = upwardAction;
	}

	public RefreshAction getRefreshAction() {
		return refreshAction;
	}

	public void setRefreshAction(RefreshAction refreshAction) {
		this.refreshAction = refreshAction;
	}

	public DeleteAction getDeleteAction() {
		return deleteAction;
	}

	public void setDeleteAction(DeleteAction deleteAction) {
		this.deleteAction = deleteAction;
	}

	public MakeDirAction getMakeDirAction() {
		return makeDirAction;
	}

	public void setMakeDirAction(MakeDirAction makeDirAction) {
		this.makeDirAction = makeDirAction;
	}

	public IHandlerActivation getUpwardHandlerAction() {
		return upwardHandlerAction;
	}

	public void setUpwardHandlerAction(IHandlerActivation upwardHandlerAction) {
		this.upwardHandlerAction = upwardHandlerAction;
	}

	public IHandlerActivation getRefreshHandlerAction() {
		return refreshHandlerAction;
	}

	public void setRefreshHandlerAction(IHandlerActivation refreshHandlerAction) {
		this.refreshHandlerAction = refreshHandlerAction;
	}

	public IHandlerActivation getDeleteHandlerAction() {
		return deleteHandlerAction;
	}

	public void setDeleteHandlerAction(IHandlerActivation deleteHandlerAction) {
		this.deleteHandlerAction = deleteHandlerAction;
	}

	public RenameFileAction getRenameFileAction() {
		return renameFileAction;
	}

	public void setRenameFileAction(RenameFileAction renameFileAction) {
		this.renameFileAction = renameFileAction;
	}

	public IHandlerActivation getRenameHandlerAction() {
		return renameHandlerAction;
	}

	public void setRenameHandlerAction(IHandlerActivation renameHandlerAction) {
		this.renameHandlerAction = renameHandlerAction;
	}

}
