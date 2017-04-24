package wftp.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import wftp.Activator;
import wftp.Context;
import wftp.model.ConnNode;
import wftp.model.ConnectionListener;
import wftp.views.RemoteView;

public class ConnectJob extends Job implements IJobChangeListener {

	private ConnNode node;

	public ConnectJob(ConnNode node) {
		super("连接任务");
		this.addJobChangeListener(this);
		this.node = node;
		this.setRule(new StandaloneRule(node));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("正在连接 " + node.getHost() + ":" + node.getPort()
				+ " ...", IProgressMonitor.UNKNOWN);
		IStatus status = Status.OK_STATUS;
		try {
			if (!node.connect()) {
				status = new Status(Status.ERROR, Activator.PLUGIN_ID,
						"连接服务器超时");
			}
		} catch (Exception e) {
			node.setConsoleError(e.getMessage());
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, "连接服务器失败", e);
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
			node.setStatus(ConnNode.CONNECTED);
			try {
				UIJob job = new UIJob("更新视图") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							IWorkbenchPage page = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage();
							RemoteView rv = (RemoteView) (page.showView(
									RemoteView.class.getName(),
									String.valueOf(node.getNode().hashCode()),
									IWorkbenchPage.VIEW_CREATE));
							node.addConnectionListener(rv);
							node.addConnectionListener(Context.getInstance()
									.getConnView());
							for (ConnectionListener adapter : node
									.getConnectionListeners()) {
								adapter.onConnect(node);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							monitor.done();
						}
						return Status.OK_STATUS;
					}
				};

				job.schedule();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			node.addConnectionListener(Context.getInstance()
					.getConnView());
			UIJob job = new UIJob("更新视图") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					node.disConnect();
					monitor.done();
					return Status.OK_STATUS;
				}

			};
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
