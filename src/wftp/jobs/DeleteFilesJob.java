package wftp.jobs;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

import wftp.Activator;
import wftp.model.ConnNode;

public class DeleteFilesJob extends Job implements IJobChangeListener {

	private ConnNode node;
	private FTPFile[] files;
	private RefreshRemoteJob refreshJob;

	public DeleteFilesJob(ConnNode node) {
		super("ɾ������");
		this.addJobChangeListener(this);
		this.node = node;
		refreshJob = new RefreshRemoteJob(node);
		this.setRule(new StandaloneRule(node));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("����ɾ�� ... ", files.length);
		IStatus status = Status.OK_STATUS;
		try {
			for (FTPFile file : files) {
				monitor.setTaskName("����ɾ�� : " + file.getName());
				deleteFile(file, monitor);
				monitor.worked(1);
				if (monitor.isCanceled())
					break;
			}
		} catch (Exception e) {
			node.setConsoleError(e.getMessage());
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, "ɾ���ļ�ʧ��", e);
		} finally {
			monitor.done();
		}
		return status;
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void done(IJobChangeEvent event) {
		if (event.getResult() == Status.OK_STATUS) {
			refreshJob.schedule();
		}
	}

	@Override
	public void running(IJobChangeEvent event) {
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sleeping(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public FTPFile[] getFiles() {
		return files;
	}

	public void setFiles(FTPFile[] files) {
		this.files = files;
	}

	private void deleteFile(FTPFile file, IProgressMonitor monitor)
			throws IOException {
		if (monitor.isCanceled())
			return;
		if (file.isFile()) {
			node.deleteFile(file.getName());
		}
		if (file.isDirectory()) {
			node.changeWorkingDirectory(file.getName());
			FTPFile[] files = node.listFiles();
			for (FTPFile f : files) {
				deleteFile(f, monitor);
			}
			node.changeToParentDirectory();
			node.removeDirectory(file.getName());
		}
	}

}
