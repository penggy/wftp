package wftp.jobs;

import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import wftp.Activator;
import wftp.model.ConnNode;
import wftp.views.RemoteView;

public class RefreshRemoteJob extends Job implements IJobChangeListener {

	private ConnNode node;
	private FTPFile[] fileList = new FTPFile[]{};
	private String workingDir;
	private ISchedulingRule Schedule_RULE = new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return this.equals(rule);
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return this.equals(rule);
		}
	};

	public RefreshRemoteJob(ConnNode node) {
		super("����Զ����ͼ����");
		this.addJobChangeListener(this);
		this.node = node;
		this.setRule(new StandaloneRule(node));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("����ˢ��Ŀ¼ ... ", IProgressMonitor.UNKNOWN);
		IStatus status = Status.OK_STATUS;
		try {
			workingDir = node.printWorkingDirectory();
			fileList = node.listFiles();
		} catch (Exception e) {
			node.setConsoleError(e.getMessage());
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, "ˢ��Ŀ¼ʧ��", e);
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
			UIJob job = new UIJob("����Զ����ͼ") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						if (node.getStatus() == ConnNode.CONNECTED) {
							IWorkbenchPage page = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage();
							RemoteView rv = (RemoteView) (page.showView(
									RemoteView.class.getName(),
									String.valueOf(node.getNode().hashCode()),
									IWorkbenchPage.VIEW_CREATE));
							rv.getTableViewer().setInput(fileList);
							rv.addPath(workingDir);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			job.setRule(Schedule_RULE);  //�����������߳�ͬʱ�ڸ����б�
			job.schedule();
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
	
}
