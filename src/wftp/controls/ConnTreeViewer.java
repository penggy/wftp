package wftp.controls;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import wftp.dnd.TreeNodeTransfer;
import wftp.model.ConnNode;
import wftp.model.ConnNodeFactory;
import wftp.model.FolderNode;
import wftp.model.TreeNode;
import wftp.views.ConnView;
import wftp.views.RemoteView;

public class ConnTreeViewer extends TreeViewer implements ITreeContentProvider,
		ILabelProvider, IDoubleClickListener {

	private MenuManager menuManager;
	private ConnView connView;

	public ConnTreeViewer(Composite parent, ConnView connView) {
		super(parent, SWT.NONE | SWT.MULTI);
		this.connView = connView;
		this.setContentProvider(this);
		this.setLabelProvider(this);
		this.addDoubleClickListener(this);
		menuManager = new MenuManager();
		initDragAndDrop();
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	private void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		final Transfer[] transfers = new Transfer[] { TreeNodeTransfer
				.getTransfer() };
		this.addDragSupport(ops, transfers, new DragSourceAdapter() {

			@Override
			public void dragSetData(DragSourceEvent event) {
				((TreeNodeTransfer) transfers[0])
						.setSelection(ConnTreeViewer.this.getSelection());
				event.doit = true;
			}

		});
		this.addDropSupport(ops, transfers, new ViewerDropAdapter(this) {

			@Override
			public boolean validateDrop(Object target, int operation,
					TransferData transferType) {
				if (TreeNodeTransfer.getTransfer()
						.isSupportedType(transferType)) {
					target = target == null ? ConnNodeFactory.getInstance()
							.getRoot() : target;
					if (target instanceof FolderNode) {
						Object[] objects = ((IStructuredSelection) this
								.getViewer().getSelection()).toArray();
						TreeNode toNode = (TreeNode) target;
						for (Object o : objects) {
							if (o instanceof TreeNode) {
								TreeNode selNode = (TreeNode) o;
								if (toNode.isInherit(selNode)) {
									return false;
								}
							}
						}
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean performDrop(Object data) {
				IStructuredSelection selection = (IStructuredSelection) data;
				Object targetObj = getCurrentTarget();
				TreeNode target = targetObj == null
						|| !(targetObj instanceof TreeNode) ? ConnNodeFactory
						.getInstance().getRoot() : (TreeNode) targetObj;
				for (Object o : selection.toArray()) {
					if (o instanceof TreeNode) {
						TreeNode treeNode = (TreeNode) o;
						boolean flag = false;
						for (Object _o : selection.toArray()) {
							if (treeNode.getParent() == _o) {
								flag = true;
								break;
							}
						}
						if (flag)
							continue;
						ConnNodeFactory.getInstance().move(treeNode, target);
					}
				}
				getViewer().refresh();
				return true;
			}
		});
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput instanceof TreeNode && newInput instanceof TreeNode) {

		}
	}

	@Override
	public Image getImage(Object element) {
		return ((TreeNode) element).getImage();
	}

	@Override
	public String getText(Object element) {
		return ((TreeNode) element).getLabel();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		ITreeSelection sel = (ITreeSelection) event.getSelection();
		TreeNode node = (TreeNode) sel.getFirstElement();
		if (node == null)
			return;
		if (isExpandable(node)) {
			if (getExpandedState(node)) {
				collapseToLevel(node, 1);
			} else {
				expandToLevel(node, 1);
			}
		}
		if (node instanceof ConnNode) {
			ConnNode connNode = (ConnNode) node;
			if (connNode.getStatus() == ConnNode.DISCONNECT) {
				connNode.aysconnect();
				connView.getConnActions().getDoConnAction().setEnabled(false);
			}
			if (connNode.getStatus() == ConnNode.CONNECTED) {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					page.showView(RemoteView.class.getName(),
							String.valueOf(connNode.getNode().hashCode()),
							IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return ((TreeNode) parentElement).getChildren().toArray();
	}

	@Override
	public Object getParent(Object element) {
		return ((TreeNode) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((TreeNode) element).hasChild();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			return ((List<?>) inputElement).toArray();
		}
		if (inputElement instanceof FolderNode) {
			return ((FolderNode) inputElement).getChildren().toArray();
		}
		if (inputElement instanceof ConnNode) {
			return new Object[] { inputElement };
		}
		return null;
	}

	public TreeNode getSelectNode() {
		return getSelectNodes().length == 1 ? (TreeNode) ((IStructuredSelection) getSelection())
				.getFirstElement() : null;
	}

	public Object[] getSelectNodes() {
		return (Object[]) ((IStructuredSelection) getSelection()).toArray();
	}

	public void onConnect(ConnNode node) {
		refresh(node);
	}

	public void onDisconnect(ConnNode node) {
		refresh(node);
	}

	public ConnView getConnView() {
		return connView;
	}

	public void setConnView(ConnView connView) {
		this.connView = connView;
	}

}
