package wftp.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

import wftp.Activator;
import wftp.model.ConnNode;

public class MakeDirJob extends Job implements IJobChangeListener {

	private ConnNode node;
	private String dirname;
	private RefreshRemoteJob refreshJob;

	public MakeDirJob(ConnNode node) {
		super("�½�Ŀ¼����");
		this.addJobChangeListener(this);
		this.node = node;
		refreshJob = new RefreshRemoteJob(node);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("���ڴ���Ŀ¼ : " + dirname, IProgressMonitor.UNKNOWN);
		IStatus status = Status.OK_STATUS;
		try {
			node.makeDirectory(dirname);
		} catch (Exception e) {
			node.setConsoleError(e.getMessage());
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, "����Ŀ¼ʧ��", e);
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

	public String getDirname() {
		return dirname;
	}

	public void setDirname(String dirname) {
		this.dirname = dirname;
	}

}
