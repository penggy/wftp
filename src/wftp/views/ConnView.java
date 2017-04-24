package wftp.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import wftp.Context;
import wftp.actions.ConnectionActionGroup;
import wftp.controls.ConnTreeViewer;
import wftp.model.ConnNode;
import wftp.model.ConnNodeFactory;
import wftp.model.ConnectionListener;
import wftp.model.FolderNode;
import wftp.model.TreeNode;

public class ConnView extends ViewPart implements ConnectionListener{
	
	private ConnectionActionGroup connActions;
	private ConnTreeViewer connTree;

	public ConnectionActionGroup getConnActions() {
		return connActions;
	}

	public void setConnActions(ConnectionActionGroup connActions) {
		this.connActions = connActions;
	}

	public ConnTreeViewer getConnTree() {
		return connTree;
	}

	public void setConnTree(ConnTreeViewer connTree) {
		this.connTree = connTree;
	}

	@Override
	public void createPartControl(Composite compo) {
		connTree = new ConnTreeViewer(compo,this);
		connActions = new ConnectionActionGroup(this);
		connTree.addSelectionChangedListener(connActions);
		connActions.fillActionBars(getViewSite().getActionBars());
		connActions.fillContextMenu(connTree.getMenuManager());
		mountTreeNode(ConnNodeFactory.getInstance().getRoot());
		Context.getInstance().setConnView(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		Context.getInstance().setConnView(null);
		unmountTreeNode(ConnNodeFactory.getInstance().getRoot());
	}

	@Override
	public void setFocus() {
		TreeNode node = connTree.getSelectNode();
		connTree.setSelection((node instanceof FolderNode)?null:connTree.getSelection());
	}

	@Override
	public void onConnect(ConnNode node) {
		connActions.onConnect(node);
		connTree.onConnect(node);
	}

	@Override
	public void onDisconnect(ConnNode node) {
		connActions.onDisconnect(node);
		connTree.onDisconnect(node);
	}
	
	private void mountTreeNode(TreeNode node) {
		connTree.setInput(ConnNodeFactory.getInstance().getRoot());
		if(node instanceof ConnNode) {
			ConnNode conn = (ConnNode) node;
			if(conn.getStatus() == ConnNode.CONNECTED) {
				conn.addConnectionListener(this);
			}
		}
		if(node instanceof FolderNode) {
			FolderNode folder = (FolderNode)node;
			for(TreeNode tn :folder.getChildren()) {
				mountTreeNode(tn);
			}
		}
	}
	
	private void unmountTreeNode(TreeNode node) {
		if(node instanceof ConnNode) {
			ConnNode conn = (ConnNode) node;
			if(conn.getStatus() == ConnNode.CONNECTED) {
				conn.removeConnectionListener(this);
			}
		}
		if(node instanceof FolderNode) {
			FolderNode folder = (FolderNode)node;
			for(TreeNode tn :folder.getChildren()) {
				unmountTreeNode(tn);
			}
		}
	}

}
