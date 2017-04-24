package wftp.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

import wftp.ImageContext;
import wftp.controls.ConnDialog;
import wftp.controls.ConnTreeViewer;
import wftp.model.ConnNode;
import wftp.model.ConnNodeFactory;
import wftp.model.FolderNode;
import wftp.model.TreeNode;
import wftp.views.ConnView;

public class ConnectionActionGroup extends ActionGroup implements
		ISelectionChangedListener {

	private Action doConnAction = new DoConnAction();
	private Action disConnAction = new DisConnAction();
	private Action addConnAction = new AddConnAction();
	private Action editConnAction = new EditConnAction();
	private Action delAction = new DelAction();
	private Action addFolderAction = new AddFolderAction();
	private Action editLabelAction = new EditLabelAction();

	private IHandlerActivation deleteHandlerAction;
	private IHandlerActivation editHandlerAction;

	private ConnTreeViewer tv;
	private ConnView cv;

	public ConnectionActionGroup(ConnView cv) {
		this.cv = cv;
		this.tv = cv.getConnTree();
		IHandlerService handlerService = (IHandlerService) cv.getViewSite()
				.getService(IHandlerService.class);
		deleteHandlerAction = handlerService.activateHandler("org.eclipse.ui.edit.delete",
				new ActionHandler(delAction));
		editHandlerAction = handlerService.activateHandler("org.eclipse.ui.edit.rename",
				new ActionHandler(editLabelAction));
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		IToolBarManager toolBarMgr = actionBars.getToolBarManager();
		toolBarMgr.add(doConnAction);
		toolBarMgr.add(disConnAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(addConnAction);
		toolBarMgr.add(editConnAction);
		toolBarMgr.add(addFolderAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(editLabelAction);
		toolBarMgr.add(delAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		MenuManager menuManager = (MenuManager) menu;
		menuManager.add(addConnAction);
		menuManager.add(addFolderAction);
		Tree tree = tv.getTree();
		tree.setMenu(menuManager.createContextMenu(tree));
	}

	private class DoConnAction extends Action {

		public DoConnAction() {
			setText("连接");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.CONNECT));
		}

		@Override
		public void run() {
			ConnNode connNode = (ConnNode) tv.getSelectNode();
			connNode.aysconnect();
			setEnabled(false);
		}

	}

	private class DisConnAction extends Action {

		public DisConnAction() {
			setText("断开");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.DISCONNECT));
		}

		@Override
		public void run() {
			ConnNode connNode = (ConnNode) tv.getSelectNode();
			connNode.disConnect();
		}

	}

	private class AddConnAction extends Action {

		public AddConnAction() {
			setText("添加连接");
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.SERVER_ADD));
		}

		@Override
		public void run() {
			TreeNode node = tv.getSelectNode();
			ConnDialog dialog = new ConnDialog(null, node, false);
			dialog.open();
			tv.refresh();
		}

	}

	private class EditConnAction extends Action {
		public EditConnAction() {
			setText("编辑连接");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.SERVER_EDIT));
		}

		@Override
		public void run() {
			TreeNode node = tv.getSelectNode();
			if (node instanceof ConnNode) {
				ConnDialog dialog = new ConnDialog(null, node, true);
				dialog.open();
				tv.refresh(node);
			}
		}
	}

	private class DelAction extends Action {
		public DelAction() {
			setText("删除");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		}

		@Override
		public void run() {
			Object[] sels = tv.getSelectNodes();
			if (sels.length == 1) {
				TreeNode node = tv.getSelectNode();
				if (MessageDialog.openConfirm(null, "提示", "确定要删除["
						+ node.getLabel() + "]吗?")) {
					ConnNodeFactory.getInstance().delete(node);
				}
			} else {
				if (MessageDialog.openConfirm(null, "提示", "确定要删除这["
						+ sels.length + "]个节点吗?"))
					for (Object o : sels) {
						TreeNode node = (TreeNode) o;
						ConnNodeFactory.getInstance().delete(node);
					}
			}
			tv.refresh();
		}
	}

	private class AddFolderAction extends Action {
		public AddFolderAction() {
			setText("添加收藏夹");
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.FOLDER_ADD));
		}

		@Override
		public void run() {
			TreeNode node = tv.getSelectNode();
			InputDialog input = new InputDialog(null, "添加收藏夹", "输入收藏夹名",
					FolderNode.DEFAULT_LABEL, new IInputValidator() {
						@Override
						public String isValid(String newText) {
							if (newText == null || newText.trim().equals("")) {
								return "收藏夹名不能为空";
							}
							return null;
						}
					});
			if (IDialogConstants.OK_ID == input.open()) {
				FolderNode folderNode = new FolderNode();
				folderNode.setLabel(input.getValue());
				ConnNodeFactory.getInstance().save(node, folderNode);
			}
			tv.refresh();
		}
	}

	private class EditLabelAction extends Action {
		public EditLabelAction() {
			setText("编辑标签");
			setEnabled(false);
			setImageDescriptor(ImageContext
					.getImageDescriptor(ImageContext.TEXTFIELD_RENAME));
		}

		@Override
		public void run() {
			TreeNode node = tv.getSelectNode();
			InputDialog input = new InputDialog(null, "编辑标签", "输入标签名", node
					.getLabel(), new IInputValidator() {
				@Override
				public String isValid(String newText) {
					if (newText == null || newText.trim().equals("")) {
						return "标签名不能为空";
					}
					return null;
				}
			});
			if (IDialogConstants.OK_ID == input.open()) {
				node.setLabel(input.getValue());
				ConnNodeFactory.getInstance().update(node);
			}
			tv.refresh(node);
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		tv.getMenuManager().removeAll();
		Object[] sels = ((IStructuredSelection) event.getSelection()).toArray();
		TreeNode node = sels.length == 1 ? (TreeNode) sels[0] : null;
		if (node instanceof ConnNode) {
			ConnNode connNode = (ConnNode) node;
			doConnAction
					.setEnabled(connNode.getStatus() == ConnNode.DISCONNECT);
			disConnAction
					.setEnabled(connNode.getStatus() == ConnNode.CONNECTED);
			editConnAction
					.setEnabled(connNode.getStatus() == ConnNode.DISCONNECT);
			editLabelAction.setEnabled(true);
			delAction.setEnabled(true);

			tv.getMenuManager().add(doConnAction);
			tv.getMenuManager().add(disConnAction);
			tv.getMenuManager().add(new Separator());
			tv.getMenuManager().add(editLabelAction);
			tv.getMenuManager().add(editConnAction);
			tv.getMenuManager().add(addConnAction);
			tv.getMenuManager().add(delAction);
			tv.getMenuManager().add(new Separator());
			tv.getMenuManager().add(addFolderAction);
		} else if (node instanceof FolderNode) {
			editLabelAction.setEnabled(true);
			delAction.setEnabled(true);
			
			doConnAction.setEnabled(false);
			disConnAction.setEnabled(false);
			editConnAction.setEnabled(false);

			tv.getMenuManager().add(editLabelAction);
			tv.getMenuManager().add(addConnAction);
			tv.getMenuManager().add(addFolderAction);
			tv.getMenuManager().add(delAction);
		} else if(node == null){
			doConnAction.setEnabled(false);
			disConnAction.setEnabled(false);
			editConnAction.setEnabled(false);
			editLabelAction.setEnabled(false);
			tv.getMenuManager().add(addConnAction);
			tv.getMenuManager().add(addFolderAction);
			
			if(sels.length==0) {  //选择0个
				delAction.setEnabled(false);
			}else {   //选择多个
				delAction.setEnabled(true);
				tv.getMenuManager().add(new Separator());
				tv.getMenuManager().add(delAction);
			}

		}
	}

	public void onConnect(ConnNode node) {
		TreeNode selNode = tv.getSelectNode();
		if (selNode == node) {
			editConnAction.setEnabled(false);
			doConnAction.setEnabled(false);
			disConnAction.setEnabled(true);
		}
	}

	public void onDisconnect(ConnNode node) {
		TreeNode selNode = tv.getSelectNode();
		if (selNode == node) {
			editConnAction.setEnabled(true);
			doConnAction.setEnabled(true);
			disConnAction.setEnabled(false);
		}
	}

	public IHandlerActivation getDeleteHandlerAction() {
		return deleteHandlerAction;
	}

	public void setDeleteHandlerAction(IHandlerActivation deleteHandlerAction) {
		this.deleteHandlerAction = deleteHandlerAction;
	}

	public IHandlerActivation getEditHandlerAction() {
		return editHandlerAction;
	}

	public void setEditHandlerAction(IHandlerActivation editHandlerAction) {
		this.editHandlerAction = editHandlerAction;
	}

	public ConnTreeViewer getTv() {
		return tv;
	}

	public void setTv(ConnTreeViewer tv) {
		this.tv = tv;
	}

	public ConnView getCv() {
		return cv;
	}

	public void setCv(ConnView cv) {
		this.cv = cv;
	}

	public Action getDoConnAction() {
		return doConnAction;
	}

	public void setDoConnAction(Action doConnAction) {
		this.doConnAction = doConnAction;
	}

	public Action getDisConnAction() {
		return disConnAction;
	}

	public void setDisConnAction(Action disConnAction) {
		this.disConnAction = disConnAction;
	}

	public Action getAddConnAction() {
		return addConnAction;
	}

	public void setAddConnAction(Action addConnAction) {
		this.addConnAction = addConnAction;
	}

	public Action getEditConnAction() {
		return editConnAction;
	}

	public void setEditConnAction(Action editConnAction) {
		this.editConnAction = editConnAction;
	}

	public Action getDelAction() {
		return delAction;
	}

	public void setDelAction(Action delAction) {
		this.delAction = delAction;
	}

	public Action getAddFolderAction() {
		return addFolderAction;
	}

	public void setAddFolderAction(Action addFolderAction) {
		this.addFolderAction = addFolderAction;
	}

	public Action getEditLabelAction() {
		return editLabelAction;
	}

	public void setEditLabelAction(Action editLabelAction) {
		this.editLabelAction = editLabelAction;
	}

}
