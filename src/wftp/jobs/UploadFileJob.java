package wftp.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

import wftp.Activator;
import wftp.model.FTPClientFactory;
import wftp.views.RemoteView;

public class UploadFileJob extends Job implements IJobChangeListener {

	private RemoteView rv;
	private File localFile;
	private String remoteDir;

	public UploadFileJob(RemoteView rv, File localFile, String remoteDir) {
		super("上传任务");
		this.localFile = localFile;
		this.addJobChangeListener(this);
		this.remoteDir = remoteDir;
		this.rv = rv;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		FTPClient ftpClient = null;
		try {
			if (!rv.isDownloadable()) {
				throw new Exception("当前没有目标服务器上传");
			}
			ftpClient = FTPClientFactory.createFTPClient(rv.getNode());
			ftpClient.connect(rv.getNode().getHost(), rv.getNode().getPort());
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				if (!ftpClient.login(rv.getNode().getUsername(), rv.getNode()
						.getPassword())) {
					throw new Exception("连接失败...");
				}
				ftpClient.changeWorkingDirectory(remoteDir);
				uploadFile(ftpClient, localFile, monitor);
				return status;
			}
			throw new Exception("连接失败...");
		} catch (Exception e) {
			rv.getNode().setConsoleError(e.getMessage());
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, "上传失败", e);
		} finally {
			try {
				if (ftpClient != null) {
					ftpClient.disconnect();
					ftpClient = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			new RefreshRemoteJob(rv.getNode()).schedule();
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

	public File getLocalDir() {
		return localFile;
	}

	public void setLocalDir(File localDir) {
		this.localFile = localDir;
	}

	private void uploadFile(FTPClient ftpClient, File localFile,
			IProgressMonitor monitor) throws Exception {
		if (monitor.isCanceled())
			return;
		if (localFile.exists()
				&& FileSystemView.getFileSystemView().isFileSystem(localFile)) {
			ftpClient.enterLocalPassiveMode();
			for (FTPFile rf : ftpClient.listFiles()) {
				if (rf.getName().equals(localFile.getName())) {
					throw new Exception("文件或目录已存在 : " + rf.getName());
				}
			}
			if (localFile.isDirectory()) {
				ftpClient.makeDirectory(localFile.getName());
				ftpClient.changeWorkingDirectory(localFile.getName());
				for (File f : localFile.listFiles()) {
					uploadFile(ftpClient, f, monitor);
					if (monitor.isCanceled())
						break;
				}
				ftpClient.changeToParentDirectory();
			} else {
				ftpClient.enterLocalPassiveMode();
		        ftpClient.setFileType(FTP.BINARY_FILE_TYPE); 
				monitor.beginTask("正在上传 : " + localFile.getName(),
						(int) (localFile.length() / 1024));
				OutputStream out = ftpClient.appendFileStream(localFile
						.getName());
				if (out == null) {
					throw new Exception("无法写入");
				}
				InputStream in = new FileInputStream(localFile);
				byte[] buf = new byte[10240];
				int count = 0;
				int tail = 0;
				while ((count = in.read(buf)) > 0) {
					out.write(buf, 0, count);
					out.flush();
					tail += count % 1024;
					monitor.worked(count / 1024 + tail / 1024);
					tail = tail % 1024;
					if (monitor.isCanceled())
						break;
				}
				in.close();
				out.close();
				ftpClient.completePendingCommand();
			}
		} else {
			throw new Exception("文件不是有效上传对像 : " + localFile.getName());
		}
	}

}
