package wftp.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import wftp.Context;

public class RefreshLocalJob extends UIJob {

	public RefreshLocalJob() {
		super("���±�����ͼ����");
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		Context.getInstance().getLocalView().refresh();
		return Status.OK_STATUS;
	}

}
