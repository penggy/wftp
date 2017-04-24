package wftp.views;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

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
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

import wftp.Activator;
import wftp.Context;
import wftp.ImageContext;
import wftp.WftpPreference;
import wftp.actions.LocalActions;
import wftp.dnd.FTPFileTransfer;
import wftp.dnd.LocalFileTransfer;
import wftp.jobs.DownloadFileJob;
import wftp.jobs.UploadFileJob;
import wftp.model.ConnNode;
import wftp.model.ConnNodeFactory;
import wftp.utils.ImageConvertor;

public class LocalView extends ContentView implements IDoubleClickListener,
		IPartListener2 {

	private File localDir;
	private LocalActions localActions;
	private RemoteView uploadTarget;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getSite().getPage().addPartListener(this);
		localDir = initHome();
		localActions = new LocalActions(this);
		localActions.fillActionBars(getViewSite().getActionBars());
		localActions.fillContextMenu(getMenuManager());
		initDirCombo();
		initTableViewer();
		refresh();
		Context.getInstance().setLocalView(this);
	}

	private File initHome() {
		File homeDir = new File(Activator.getDefault().getPreferenceStore()
				.getString(WftpPreference.DIR_KEY));
		if (!isPutable(homeDir)
				|| new File(WftpPreference.DEFAULT_LOCALDIR).equals(homeDir)) {
			homeDir = FileSystemView.getFileSystemView().getHomeDirectory();
		}
		return homeDir;
	}

	private void initTableViewer() {
		tableViewer.addSelectionChangedListener(localActions);
		createColumn(60, "名称").addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = LocalViewerSorter.NAME_ASC,
					desc = LocalViewerSorter.NAME_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		createColumn(50, "日期").addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = LocalViewerSorter.TIME_ASC,
					desc = LocalViewerSorter.TIME_DESC;

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
			private ViewerSorter asc = LocalViewerSorter.SIZE_ASC,
					desc = LocalViewerSorter.SIZE_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		createColumn(40, "类型").addSelectionListener(new SelectionAdapter() {
			private boolean ascFlag = true;
			private ViewerSorter asc = LocalViewerSorter.TYPE_ASC,
					desc = LocalViewerSorter.TYPE_DESC;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(ascFlag ? asc : desc);
				ascFlag = !ascFlag;
			}

		});
		tableViewer.setContentProvider(new LocalFileContentProvider());
		tableViewer.setLabelProvider(new LocalFileLabelProvider());
		tableViewer.addDoubleClickListener(this);
		initDragAndDrop();
	}

	private void initDirCombo() {
		dirCombo.removeAll();
		dirCombo.add(localDir.getAbsolutePath());
		dirCombo.select(0);
		dirCombo.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if ((e.keyCode == 13 || e.keyCode == 10)) {
					String pathname = dirCombo.getText().trim();
					File file = new File(pathname);
					processFile(file);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});
		dirCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String pathname = dirCombo.getItem(dirCombo.getSelectionIndex());
				File file = new File(pathname);
				processFile(file);
			}

		});
		dirCombo.addFocusListener(new FocusListener() {
			IHandlerService handlerService = (IHandlerService) getSite()
					.getService(IHandlerService.class);

			@Override
			public void focusLost(FocusEvent e) {
				IHandlerActivation handlerAction = handlerService
						.activateHandler(localActions.getUpwardHandlerAction());
				localActions.setUpwardHandlerAction(handlerAction);
			}

			@Override
			public void focusGained(FocusEvent e) {
				handlerService.deactivateHandler(localActions
						.getUpwardHandlerAction());
			}
		});
	}

	/**
	 * 本地目录标签提供
	 * 
	 * @author wu.peng
	 * 
	 */
	class LocalFileLabelProvider implements ITableLabelProvider {

		public String getText(Object element) {
			File file = (File) element;
			return FileSystemView.getFileSystemView()
					.getSystemDisplayName(file);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				File file = (File) element;
				Image img = ImageConvertor
						.getSWTImageFromSwing((ImageIcon) FileSystemView
								.getFileSystemView().getSystemIcon(file));
				if (img == null) {
					if (file.isDirectory()) {
						return ImageContext.getImage(ImageContext.FOLDER);
					}
					if (file.isFile()) {
						int dotIndex = file.getName().lastIndexOf('.');
						if (dotIndex != -1) {
							Program program = Program.findProgram(file
									.getName().substring(dotIndex));
							if (program != null
									&& program.getImageData() != null) {
								return new Image(null, program.getImageData());
							}
						}
						return ImageContext.getImage(ImageContext.PAGE_WHITE);
					}
				} else {
					return img;
				}
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof File) {
				File file = (File) element;
				switch (columnIndex) {
				case 0:
					return FileSystemView.getFileSystemView()
							.getSystemDisplayName(file);
				case 1:
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(file.lastModified());
					return cal.get(Calendar.YEAR) + "/"
							+ cal.get(Calendar.MONTH) + "/"
							+ cal.get(Calendar.DAY_OF_MONTH) + " "
							+ cal.get(Calendar.HOUR_OF_DAY) + ":"
							+ cal.get(Calendar.MINUTE);
				case 2:
					if (FileSystemView.getFileSystemView().isFileSystem(file)
							&& file.isFile())
						return NumberFormat.getInstance().format(file.length());
					else
						return null;
				case 3:
					String type = FileSystemView.getFileSystemView()
							.getSystemTypeDescription(file);
					if (type == null || type.trim().equals("")) {
						type = new JFileChooser().getTypeDescription(file);
						type = type == null ? "" : type;
					}
					return type;
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

	/**
	 * 本地目录内容提供
	 * 
	 * @author wu.peng
	 * 
	 */
	class LocalFileContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof File) {
				File file = (File) inputElement;
				if (file.isDirectory())
					return FileSystemView.getFileSystemView().getFiles(file,
							false);
				else
					return FileSystemView.getFileSystemView().getFiles(
							file.getParentFile(), false);
			}
			if (inputElement instanceof File[]) {
				return (File[]) inputElement;
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

	@Override
	public void doubleClick(DoubleClickEvent event) {
		processFile(getSelection());
	}

	public File getSelection() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer
				.getSelection();
		return (File) sel.getFirstElement();
	}

	public Object[] getSelections() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer
				.getSelection();
		return sel.toArray();
	}

	private void processFile(File file) {
		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				localDir = file;
				refresh();
			} else if (FileSystemView.getFileSystemView().isFileSystem(file)) {
				Program.launch(file.getAbsolutePath());
			}
		} else {
			refresh();
		}
	}

	private void updateDircombo() {
		if (isPutable()) {
			addPath(localDir.getAbsolutePath());
		} else {
			dirCombo.setText("");
			dirCombo.clearSelection();
		}
	}

	public boolean isPutable() {
		return localDir != null && localDir.exists()
				&& FileSystemView.getFileSystemView().isFileSystem(localDir)
				&& localDir.isDirectory();
	}

	public boolean isPutable(File dir) {
		return dir != null && dir.exists()
				&& FileSystemView.getFileSystemView().isFileSystem(dir)
				&& dir.isDirectory();
	}

	public void refresh() {
		tableViewer.setInput(localDir);
		updateDircombo();
	}

	public void upward() {
		processFile(localDir.getParentFile() == null ? FileSystemView
				.getFileSystemView().getHomeDirectory() : localDir
				.getParentFile());
	}

	public void goHome() {
		localDir = initHome();
		refresh();
	}

	public File getLocalDir() {
		return localDir;
	}

	public void setLocalDir(File localDir) {
		this.localDir = localDir;
	}

	public LocalActions getLocalActions() {
		return localActions;
	}

	public void setLocalActions(LocalActions localActions) {
		this.localActions = localActions;
	}

	public void deleteSelections() {
		for (Object o : getSelections()) {
			deleteFile((File) o);
		}
		refresh();
	}

	public void deleteFile(File file) {
		if (file != null && file.exists()
				&& FileSystemView.getFileSystemView().isFileSystem(file)) {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					deleteFile(f);
				}
			}
			file.delete();
		}
	}

	public void uploadSelections() {
		if (!isUploadable()) {
			MessageDialog.openError(null, "提示", "当前无法上传");
			return;
		}
		for (Object o : getSelections()) {
			File file = (File) o;
			UploadFileJob job = new UploadFileJob(uploadTarget, file,
					uploadTarget.getNode().getCurDir());
			job.schedule();
		}
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (RemoteView.class.getName().equals(partRef.getId())) {
			RemoteView rv = (RemoteView) partRef.getPart(false);
			uploadTarget = rv;
			localActions.refresh();
		}
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (uploadTarget == partRef.getPart(false)) {
			uploadTarget = null;
			localActions.refresh();
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (RemoteView.class.getName().equals(partRef.getId())) {
			RemoteView rv = (RemoteView) partRef.getPart(false);
			uploadTarget = rv;
			localActions.refresh();
		}
	}

	@Override
	public void dispose() {
		//ConnNodeFactory.getInstance().getRoot().disConnect();
		getSite().getPage().removePartListener(this);
		super.dispose();
	}

	public RemoteView getUploadTarget() {
		return uploadTarget;
	}

	public void setUploadTarget(RemoteView uploadTarget) {
		this.uploadTarget = uploadTarget;
	}

	private void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		final Transfer[] dragTransfers = new Transfer[] { LocalFileTransfer
				.getTransfer() };
		final Transfer[] dropTransfers = new Transfer[] { FTPFileTransfer
				.getTransfer() };
		tableViewer.addDragSupport(ops, dragTransfers, new DragSourceAdapter() {

			@Override
			public void dragSetData(DragSourceEvent event) {
				((LocalFileTransfer) dragTransfers[0]).setSelection(tableViewer
						.getSelection());
				event.doit = true;
			}

		});
		tableViewer.addDropSupport(ops, dropTransfers, new ViewerDropAdapter(
				tableViewer) {

			@Override
			public boolean validateDrop(Object target, int operation,
					TransferData transferType) {
				if (FTPFileTransfer.getTransfer().isSupportedType(transferType)
						&& isPutable()) {
					return true;
				}
				return false;
			}

			@Override
			public boolean performDrop(Object data) {
				Object[] datas = (Object[]) data;
				RemoteView rv = (RemoteView) datas[0];
				if (!rv.isDownloadable()) {
					MessageDialog.openError(null, "提示", "当前无法下载");
					return false;
				}
				IStructuredSelection selection = (IStructuredSelection) datas[1];
				for (Object o : selection.toArray()) {
					FTPFile file = (FTPFile) o;
					DownloadFileJob downJob = new DownloadFileJob(rv.getNode(),
							file, getLocalDir(), rv.getNode().getCurDir());
					downJob.schedule();
				}
				return true;
			}
		});
	}

	public boolean isUploadable() {
		return (uploadTarget != null && uploadTarget.getNode() != null && uploadTarget
				.getNode().getStatus() == ConnNode.CONNECTED);
	}

	public void makeDir() {
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
			if (!isPutable()) {
				MessageDialog.openError(null, "提示", "当前不是有效的目录");
				return;
			}
			File newFile = new File(localDir, input.getValue());
			if (newFile.exists()) {
				MessageDialog.openError(null, "提示", "目录已存在");
				return;
			} else {
				newFile.mkdirs();
				refresh();
			}
		}
	}

	public void renameSelection() {
		if (!FileSystemView.getFileSystemView().isFileSystem(getSelection())) {
			MessageDialog.openError(null, "提示", "当前不是有效的目录");
			return;
		}
		InputDialog input = new InputDialog(null, "重命名", "输入文件名",
				getSelection().getName(), new IInputValidator() {
					@Override
					public String isValid(String newText) {
						if (newText == null || newText.trim().equals("")) {
							return "文件名不能为空";
						}
						return null;
					}
				});
		if (IDialogConstants.OK_ID == input.open()) {
			File newFile = new File(localDir, input.getValue());
			if (newFile.exists() && !newFile.equals(getSelection())) {
				MessageDialog.openError(null, "提示", "目录已存在");
				return;
			} else {
				getSelection().renameTo(newFile);
				refresh();
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		ConnNodeFactory.getInstance().getRoot().disConnect();
	}
	
	
}