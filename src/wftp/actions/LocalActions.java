package wftp.actions;

import javax.swing.filechooser.FileSystemView;

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
import wftp.views.LocalView;

public class LocalActions extends ActionGroup implements
		ISelectionChangedListener {
	private LocalView lv;
	private HomeAction homeAction = new HomeAction();
	private UpwardAction upwardAction = new UpwardAction();
	private RefreshAction refreshAction = new RefreshAction();
	private DeleteAction deleteAction = new DeleteAction();
	private MakeDirAction makeDirAction = new MakeDirAction();
	private RenameFileAction renameFileAction = new RenameFileAction();

	private UploadAction uploadAction = new UploadAction();
	private IHandlerActivation upwardHandlerAction;
	private IHandlerActivation refreshHandlerAction;
	private IHandlerActivation deleteHandlerAction;
	private IHandlerActivation renameHandlerAction;

	public LocalActions(LocalView lv) {
		this.lv = lv;
		IHandlerService handlerService = (IHandlerService) lv.getViewSite()
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
		toolBarMgr.add(uploadAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		MenuManager menuManager = (MenuManager) menu;
		menuManager.add(uploadAction);
		menuManager.add(new Separator());
		menuManager.add(makeDirAction);
		menuManager.add(deleteAction);
		menuManager.add(renameFileAction);
		menuManager.add(new Separator());
		menuManager.add(refreshAction);
		Table table = lv.getTableViewer().getTable();
		table.setMenu(menuManager.createContextMenu(table));
	}

	private class RefreshAction extends Action {

		public RefreshAction() {
			setText("刷新");
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.REFRESH));
		}

		@Override
		public void run() {
			lv.refresh();
		}

	}

	private class MakeDirAction extends Action {

		public MakeDirAction() {
			setText("创建目录");
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.FOLDER_ADD));
		}

		@Override
		public void run() {
			lv.makeDir();
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
			lv.renameSelection();
		}

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
				lv.deleteSelections();
			}
		}

	}

	private class UploadAction extends Action {

		public UploadAction() {
			setText("上传");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.EXPORT_WIZ));
		}

		@Override
		public void run() {
			lv.uploadSelections();
		}

	}

	private class UpwardAction extends Action {

		public UpwardAction() {
			setText("上级");
			setImageDescriptor(ImageContext
					.getImageDescriptor(ISharedImages.IMG_TOOL_UP));
		}

		@Override
		public void run() {
			lv.upward();
		}

	}

	private class HomeAction extends Action {

		public HomeAction() {
			setText("首页");
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.HOME_NAV));
		}

		@Override
		public void run() {
			lv.goHome();
		}

	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		refresh();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void refresh() {
		Object[] sels = ((IStructuredSelection) lv.getTableViewer()
				.getSelection()).toArray();
		if (sels.length > 0) {
			uploadAction.setEnabled(lv.isUploadable());
			deleteAction.setEnabled(true);
		} else {
			uploadAction.setEnabled(false);
			deleteAction.setEnabled(false);
		}
		if (sels.length == 1) {
			renameFileAction.setEnabled(FileSystemView.getFileSystemView()
					.isFileSystem(lv.getSelection()));
		} else {
			renameFileAction.setEnabled(false);
		}
	}

	public LocalView getLv() {
		return lv;
	}

	public void setLv(LocalView lv) {
		this.lv = lv;
	}

	public HomeAction getHomeAction() {
		return homeAction;
	}

	public void setHomeAction(HomeAction homeAction) {
		this.homeAction = homeAction;
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

	public UploadAction getUploadAction() {
		return uploadAction;
	}

	public void setUploadAction(UploadAction uploadAction) {
		this.uploadAction = uploadAction;
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

	public MakeDirAction getMakeDirAction() {
		return makeDirAction;
	}

	public void setMakeDirAction(MakeDirAction makeDirAction) {
		this.makeDirAction = makeDirAction;
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
