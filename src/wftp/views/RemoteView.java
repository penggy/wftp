package wftp.views;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

import wftp.Context;
import wftp.ImageContext;
import wftp.actions.RemoteActions;
import wftp.dnd.FTPFileTransfer;
import wftp.dnd.LocalFileTransfer;
import wftp.jobs.ChangeDirectoryJob;
import wftp.jobs.DeleteFilesJob;
import wftp.jobs.DownloadFileJob;
import wftp.jobs.MakeDirJob;
import wftp.jobs.RenameFileJob;
import wftp.jobs.UploadFileJob;
import wftp.model.ConnNode;
import wftp.model.ConnectionListener;

public class RemoteView extends ContentView implements ConnectionListener,
		IDoubleClickListener {

	private ConnNode node; // 连接当中的节点
	private ChangeDirectoryJob changeDirJob;
	private RemoteActions remoteActions;
	private MakeDirJob makeDirJob;
	private RenameFileJob renameFileJob;
	private DeleteFilesJob delJob;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		remoteActions = new RemoteActions(this);
		remoteActions.fillActionBars(getViewSite().getActionBars());
		remoteActions.fillContextMenu(getMenuManager());
		initTableViewer();
		initDirCombo();
	}

	private void initDirCombo() {
		dirCombo.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if ((e.keyCode == 13 || e.keyCode == 10)) {
					changeDir(dirCombo.getText());
				}

			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		dirCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String pathname = dirCombo
						.getItem(dirCombo.getSelectionIndex());
				changeDir(pathname);
			}

		});

		dirCombo.addFocusListener(new FocusListener() {
			IHandlerService handlerService = (IHandlerService) getSite()
					.getService(IHandlerService.class);

			@Override
			public void focusLost(FocusEvent e) {
				IHandlerActivation handlerAction = handlerService
						.activateHandler(remoteActions.getUpwardHandlerAction());
				remoteActions.setUpwardHandlerAction(handlerAction);
			}

			@Override
			public void focusGained(FocusEvent e) {
				handlerService.deactivateHandler(remoteActions
						.getUpwardHandlerAction());
			}
		});
	}

	private void initTableViewer() {
		tableViewer.addSelectionChangedListener(remoteActions);
		tableViewer.setSorter(RemoteViewerSorter.TYPE_ASC);
		createColumn(60, "名称").addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = RemoteViewerSorter.NAME_ASC,
					desc = RemoteViewerSorter.NAME_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		createColumn(50, "日期").addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = RemoteViewerSorter.TIME_ASC,
					desc = RemoteViewerSorter.TIME_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		TableColumn sizecol = createColumn(40, "大小");
		sizecol.setAlignment(SWT.RIGHT);
		sizecol.addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = RemoteViewerSorter.SIZE_ASC,
					desc = RemoteViewerSorter.SIZE_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		createColumn(40, "类型").addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = RemoteViewerSorter.TYPE_ASC,
					desc = RemoteViewerSorter.TYPE_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		createColumn(40, "属性").addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = RemoteViewerSorter.ATTR_ASC,
					desc = RemoteViewerSorter.ATTR_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		tableViewer.setContentProvider(new RemoteFileContentProvider());
		tableViewer.setLabelProvider(new RemoteFileLabelProvider());
		tableViewer.addDoubleClickListener(this);
		initDragAndDrop();
	}

	@Override
	public void onConnect(ConnNode node) {
		this.node = node;
		remoteActions.onConnect(node);
		changeDirJob = new ChangeDirectoryJob("切换目录任务", node);
		delJob = new DeleteFilesJob(node);
		makeDirJob = new MakeDirJob(node);
		renameFileJob = new RenameFileJob(node);
		dirCombo.removeAll();
		setPartName(node.toString());
		refresh();
		getSite().getPage().bringToTop(this);
	}

	@Override
	public void onDisconnect(ConnNode node) {
		remoteActions.onDisconnect(node);
		tableViewer.setInput(null);
		dirCombo.removeAll();
		dirCombo.setText("");
		this.node = null;
		getSite().getPage().hideView(this);
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (node != null) {
			FTPFile file = getSelection();
			if (file != null && !file.isDirectory())
				return;
			changeDir(file.getName());
		}

	}

	public FTPFile getSelection() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer
				.getSelection();
		return (FTPFile) sel.getFirstElement();
	}

	public Object[] getSelections() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer
				.getSelection();
		return sel.toArray();
	}

	@Override
	public void dispose() {
		if (node != null) {
			node.removeConnectionListener(this);
			node.disConnect();
			node = null;
			getSite().getPage().hideView(this);
		}
		super.dispose();
	}

	class RemoteFileContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof FTPFile[]) {
				return (FTPFile[]) inputElement;
			}
			if (inputElement instanceof FTPFile) {
				return new FTPFile[] { (FTPFile) inputElement };
			}
			return null;
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
	}

	class RemoteFileLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				FTPFile file = (FTPFile) element;
				if (file.isDirectory()) {
					return ImageContext.getImage(ImageContext.FOLDER);
				}
				if (file.isSymbolicLink()) {
					return ImageContext.getImage(ImageContext.LINK);
				}
				if (file.isFile()) {
					int dotIndex = file.getName().lastIndexOf('.');
					if (dotIndex != -1) {
						Program program = Program.findProgram(file.getName()
								.substring(dotIndex));
						if (program != null &&  program.getImageData() !=null) {
							return new Image(null, program.getImageData());
						}
					}
				}
				return ImageContext.getImage(ImageContext.PAGE_WHITE);
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof FTPFile) {
				FTPFile file = (FTPFile) element;
				switch (columnIndex) {
				case 0:
					return file.getName();
				case 1:
					Calendar cal = file.getTimestamp();
					return cal.get(Calendar.YEAR) + "/"
							+ cal.get(Calendar.MONTH) + "/"
							+ cal.get(Calendar.DAY_OF_MONTH) + " "
							+ cal.get(Calendar.HOUR_OF_DAY) + ":"
							+ cal.get(Calendar.MINUTE);
				case 2:
					if (file.isDirectory())
						return null;
					return NumberFormat.getIntegerInstance().format(
							file.getSize());
				case 3:
					return ConnNode.getFTPFileType(file);
				case 4:
					return file.getRawListing();
				}
			}
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

	}

	public ConnNode getNode() {
		return node;
	}

	public void setNode(ConnNode node) {
		this.node = node;
	}

	public void refresh() {
		if(node != null) {
			String dir = node.getHomeDir();
			try {
				dir = dirCombo.getItem(dirCombo.getSelectionIndex());
			} catch (Exception e) {
				node.setConsoleMessage(e.getMessage());
			}
			changeDir(dir);
		}
	}

	public void changeDir(String pathname) {
		if (node != null) {
			changeDirJob.setTargetDir(pathname);
			changeDirJob.schedule();
		}
	}

	public void upward() {
		if (node != null) {
			changeDirJob.setTargetDir("..");
			changeDirJob.schedule();
		}
	}

	public void goHome() {
		if (node != null) {
			changeDirJob.setTargetDir(node.getHomeDir());
			changeDirJob.schedule();
		}
	}

	public void deleteSelections() {
		if (node != null) {
			FTPFile[] files = new FTPFile[] {};
			List<FTPFile> fileList = new ArrayList<FTPFile>();
			for (Object o : getSelections()) {
				fileList.add((FTPFile) o);
			}
			files = fileList.toArray(files);
			delJob.setFiles(files);
			delJob.schedule();
		}
	}

	public void downloadSelections() {
		if (!isDownloadable()) {
			MessageDialog.openError(null, "提示", "当前无法下载");
			return;
		}
		for (Object o : getSelections()) {
			FTPFile file = (FTPFile) o;
			DownloadFileJob downJob = new DownloadFileJob(node, file, Context
					.getInstance().getLocalView().getLocalDir(), dirCombo
					.getItem(dirCombo.getSelectionIndex()));
			downJob.schedule();
		}
	}

	public void makeDir() {
		if (!isDownloadable()) {
			MessageDialog.openError(null, "提示", "当前无法创建目录");
			return;
		}
		InputDialog input = new InputDialog(null, "创建目录", "输入目录名", "新建文件夹",
				new IInputValidator() {
					@Override
					public String isValid(String newText) {
						if (newText == null || newText.trim().equals("")) {
							return "目录名不能为空";
						}
						return null;
					}
				});
		if (IDialogConstants.OK_ID == input.open()) {
			makeDirJob.setDirname(input.getValue());
			makeDirJob.schedule();
		}

	}

	@Override
	public void addPath(String pathname) {
		if (node != null) {
			node.setCurDir(pathname);
		}
		super.addPath(pathname);
	}

	private void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		final Transfer[] dragTransfers = new Transfer[] { FTPFileTransfer
				.getTransfer() };
		final Transfer[] dropTransfers = new Transfer[] { LocalFileTransfer
				.getTransfer() };
		tableViewer.addDragSupport(ops, dragTransfers, new DragSourceAdapter() {

			@Override
			public void dragSetData(DragSourceEvent event) {
				((FTPFileTransfer) dragTransfers[0]).setSelection(tableViewer
						.getSelection());
				((FTPFileTransfer) dragTransfers[0]).setRv(RemoteView.this);
				event.doit = true;
			}

		});
		tableViewer.addDropSupport(ops, dropTransfers, new ViewerDropAdapter(
				tableViewer) {

			@Override
			public boolean validateDrop(Object target, int operation,
					TransferData transferType) {
				if (LocalFileTransfer.getTransfer().isSupportedType(
						transferType)
						&& isDownloadable()) {
					return true;
				}
				return false;
			}

			@Override
			public boolean performDrop(Object data) {
				if (!isDownloadable()) {
					MessageDialog.openError(null, "提示", "当前无法上传");
					return false;
				}
				IStructuredSelection sel = (IStructuredSelection) data;
				for (Object o : sel.toArray()) {
					File file = (File) o;
					UploadFileJob job = new UploadFileJob(RemoteView.this,
							file, node.getCurDir());
					job.schedule();
				}
				return true;
			}
		});
	}

	public boolean isDownloadable() {
		return node != null && node.getStatus() == ConnNode.CONNECTED;
	}

	public void renameSelection() {
		if (!isDownloadable()) {
			MessageDialog.openError(null, "提示", "当前无法重命名");
			return;
		}
		InputDialog input = new InputDialog(null, "重命名", "输入文件名", getSelection().getName(),
				new IInputValidator() {
					@Override
					public String isValid(String newText) {
						if (newText == null || newText.trim().equals("")) {
							return "文件名不能为空";
						}
						return null;
					}
				});
		if (IDialogConstants.OK_ID == input.open()) {
			renameFileJob.setFrom(getSelection().getName());
			renameFileJob.setTo(input.getValue());
			renameFileJob.schedule();
		}
	}

	@Override
	public void saveState(IMemento memento) {
		return;
	}
	
	

}