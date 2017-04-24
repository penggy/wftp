package wftp.jobs;

import java.io.File;
import java.io.FileOutputStream;
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
import wftp.model.ConnNode;
import wftp.model.FTPClientFactory;

public class DownloadFileJob extends Job implements IJobChangeListener {

	private ConnNode node;
	private File localDir;
	private FTPFile file;
	private String remoteDir;

	public DownloadFileJob(ConnNode node, FTPFile file, File localDir,
			String remoteDir) {
		super("下载任务");
		this.file = file;
		this.localDir = localDir;
		this.remoteDir = remoteDir;
		this.addJobChangeListener(this);
		this.node = node;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		FTPClient ftpClient = null;
		try {
			ftpClient = FTPClientFactory.createFTPClient(node);
			ftpClient.connect(node.getHost(), node.getPort());
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				if (!ftpClient.login(node.getUsername(), node.getPassword())) {
					throw new Exception("连接失败...");
				}
				ftpClient.changeWorkingDirectory(remoteDir);
				downloadFile(ftpClient, file, localDir, monitor);
				return status;
			}
			throw new Exception("连接失败...");
		} catch (Exception e) {
			node.setConsoleError(e.getMessage());
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, "下载失败", e);
		} finally {
			try {
				if(ftpClient!=null) {
					ftpClient.disconnect();
					ftpClient=null;
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
			new RefreshLocalJob().schedule();
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

	public FTPFile getFile() {
		return file;
	}

	public void setFile(FTPFile file) {
		this.file = file;
	}

	public File getLocalDir() {
		return localDir;
	}

	public void setLocalDir(File localDir) {
		this.localDir = localDir;
	}

	private void downloadFile(FTPClient ftpClient, FTPFile file, File localDir,
			IProgressMonitor monitor) throws Exception {
		if (monitor.isCanceled())
			return;
		if (localDir.isDirectory()
				&& FileSystemView.getFileSystemView().isFileSystem(localDir)) {
			File newFile = new File(localDir, file.getName());
			if (newFile.exists()) {
				throw new Exception("文件或目录已存在 : " + file.getName());
			}
			if (file.isDirectory()) {
				newFile.mkdirs();
				File local = new File(localDir, file.getName());
				ftpClient.changeWorkingDirectory(file.getName());
				ftpClient.enterLocalPassiveMode();
				for (FTPFile f : ftpClient.listFiles()) {
					downloadFile(ftpClient, f, local, monitor);
					if (monitor.isCanceled())
						break;
				}
				ftpClient.changeToParentDirectory();
			} else {
		        ftpClient.enterLocalPassiveMode();   
		        ftpClient.setFileType(FTP.BINARY_FILE_TYPE); 
				monitor.beginTask("正在下载 : " + file.getName(), (int) (file
						.getSize() / 1024));
				OutputStream out = new FileOutputStream(newFile);
				InputStream in = ftpClient.retrieveFileStream(file.getName());
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
			throw new Exception("当前本地目录不是一个有效的下载目录");
		}
	}

}
