package wftp.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.progress.UIJob;

import wftp.ImageContext;
import wftp.console.ConsoleFactory;
import wftp.jobs.ConnectJob;

public class ConnNode extends TreeNode {

	public static final int DISCONNECT = 0;
	public static final int DOCONNECT = 1;
	public static final int CONNECTED = 2;
	public static final Image connImage = ImageContext
			.getImage(ImageContext.CONNECT);
	public static final Image disConnImage = ImageContext
			.getImage(ImageContext.DISCONNECT);
	public static final String DEFAULT_ENCODE = "GBK";
	public static final String DEFAULT_USER = "anonymous";
	public static final int DEFAULT_PORT = 21;

	private List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();
	private int status = ConnNode.DISCONNECT;
	private FTPClient ftpClient;
	private IOConsoleOutputStream consoleMessageOutputStream;
	private IOConsoleOutputStream consoleErrorOutputStream;
	private PrintWriter consoleMessageWriter;
	private PrintWriter consoleErrorWriter;
	private String homeDir;
	private String curDir;

	private String host;
	private int port;
	private String username;
	private String password;
	private String encode;

	public List<ConnectionListener> getConnectionListeners() {
		return connectionListeners;
	}

	public void setConnectionListeners(
			List<ConnectionListener> connectionListeners) {
		this.connectionListeners = connectionListeners;
	}

	/**
	 * 异步连接
	 */
	public void aysconnect() {
		new ConnectJob(this).schedule();
	}

	public void setConsoleMessage(String msg) {
		if (consoleMessageWriter != null
				&& !consoleMessageOutputStream.isClosed()) {
			consoleMessageWriter.println(msg);
			consoleMessageWriter.flush();
		}
	}

	public void setConsoleError(String msg) {
		if (consoleErrorWriter != null && !consoleErrorOutputStream.isClosed()) {
			consoleErrorWriter.println(msg);
			consoleErrorWriter.flush();
		}
	}

	/**
	 * 同步连接
	 */
	public boolean connect() throws SocketException, IOException {
		if (status != DISCONNECT) {
			throw new RuntimeException("当前状态不可连接");
		}
		status = DOCONNECT;
		ftpClient = FTPClientFactory.createFTPClient(this);
		InetAddress addr = InetAddress.getByName(host);
		ftpClient.connect(addr.getHostAddress(), port);
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(username, password)) {
				homeDir = ftpClient.printWorkingDirectory();
				new UIJob("登录成功注册到消息台") {
					
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						ConsoleFactory.regist(ConnNode.this);
						try {
							setConsoleMessage("登录成功,当前目录 : "
									+ ftpClient.printWorkingDirectory());
						} catch (IOException e) {
							e.printStackTrace();
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				}.schedule();
				return true;
			}
		}
		return false;
	}

	public boolean reconnect() {
		try {
			if (ftpClient != null) {
				ftpClient.disconnect();
				ftpClient = null;
			}
			ConsoleFactory.unregist(this);
			ftpClient = FTPClientFactory.createFTPClient(this);
			InetAddress addr = InetAddress.getByName(host);
			ftpClient.connect(addr.getHostAddress(), port);
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				if (ftpClient.login(username, password)) {
					ftpClient.changeWorkingDirectory(curDir);
					new UIJob("重新登录后注册到消息台") {
						
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							ConsoleFactory.regist(ConnNode.this);
							try {
								setConsoleMessage("重新登录成功,当前目录 : "
										+ ftpClient.printWorkingDirectory());
							} catch (IOException e) {
								e.printStackTrace();
							}
							monitor.done();
							return Status.OK_STATUS;
						}
					}.schedule();
					return true;
				}
			}
		} catch (IOException e) {
			return false;
		}
		return false;
	}

	public void deleteFile(String filename) throws IOException {
		try {
			ftpClient.deleteFile(filename);
		} catch (FTPConnectionClosedException e) {
			if (reconnect()) {
				ftpClient.deleteFile(filename);
			}
		}
	}

	public void changeWorkingDirectory(String dir) throws IOException {
		try {
			ftpClient.changeWorkingDirectory(dir);
		} catch (FTPConnectionClosedException ex) {
			if (reconnect())
				ftpClient.changeWorkingDirectory(dir);
		}
	}

	public void removeDirectory(String dirname) throws IOException {
		try {
			ftpClient.removeDirectory(dirname);
		} catch (FTPConnectionClosedException e) {
			if (reconnect()) {
				ftpClient.removeDirectory(dirname);
			}
		}
	}

	public void makeDirectory(String name) throws IOException {
		try {
			ftpClient.makeDirectory(name);
		} catch (FTPConnectionClosedException e) {
			if (reconnect()) {
				ftpClient.makeDirectory(name);
			}
		}
	}

	public void rename(String from, String to) throws IOException {
		try {
			ftpClient.rename(from, to);
		} catch (FTPConnectionClosedException e) {
			if (reconnect()) {
				ftpClient.rename(from, to);
			}
		}
	}

	public String printWorkingDirectory() throws IOException {
		try {
			return ftpClient.printWorkingDirectory();
		} catch (FTPConnectionClosedException e) {
			if (reconnect()) {
				return ftpClient.printWorkingDirectory();
			}
		}
		return "";
	}

	public FTPFile[] listFiles() throws IOException {
		try {
			ftpClient.enterLocalPassiveMode();
			return ftpClient.listFiles();
		} catch (FTPConnectionClosedException e) {
			if (reconnect()) {
				ftpClient.enterLocalPassiveMode();
				return ftpClient.listFiles();
			}
		}
		return null;
	}

	public void changeToParentDirectory() throws IOException {
		try {
			ftpClient.changeToParentDirectory();
		} catch (FTPConnectionClosedException e) {
			if (reconnect()) {
				ftpClient.changeToParentDirectory();
			}
		}
	}

	public Image getImage() {
		return status == CONNECTED ? connImage : disConnImage;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void addConnectionListener(ConnectionListener listner) {
		connectionListeners.add(listner);
	}

	public void removeConnectionListener(ConnectionListener listner) {
		connectionListeners.remove(listner);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}

	public PrintWriter getConsoleWriter() {
		return consoleMessageWriter;
	}

	public void setConsoleWriter(PrintWriter consoleWriter) {
		this.consoleMessageWriter = consoleWriter;
	}

	public String getHomeDir() {
		return homeDir;
	}

	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}

	public static String getFTPFileType(FTPFile file) {
		String result = null;
		if (file.isDirectory())
			result = "文件夹";
		if (file.isFile()) {
			int dotIndex = file.getName().lastIndexOf('.');
			if (dotIndex != -1) {
				Program program = Program.findProgram(file.getName().substring(
						dotIndex));
				if (program != null) {
					result = program.getName();
				}
			}
		}
		if (file.isSymbolicLink())
			result = "链接";
		return result == null ? "" : result;
	}

	@Override
	public void disConnect() {
		try {
			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.disconnect();
				ftpClient = null;
			}
		} catch (IOException e) {
			setConsoleError(e.getMessage());
		} finally {
			status = DISCONNECT;
			for (ConnectionListener adapter : connectionListeners) {
				adapter.onDisconnect(this);
			}
			connectionListeners.clear();
			ConsoleFactory.unregist(this);
		}

	}

	public String getCurDir() {
		return curDir;
	}

	public void setCurDir(String curDir) {
		this.curDir = curDir;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	@Override
	public String toString() {
		return username + "@" + host;
	}

	public IOConsoleOutputStream getConsoleMessageOutputStream() {
		return consoleMessageOutputStream;
	}

	public void setConsoleMessageOutputStream(
			IOConsoleOutputStream consoleMessageOutputStream) {
		this.consoleMessageOutputStream = consoleMessageOutputStream;
	}

	public IOConsoleOutputStream getConsoleErrorOutputStream() {
		return consoleErrorOutputStream;
	}

	public void setConsoleErrorOutputStream(
			IOConsoleOutputStream consoleErrorOutputStream) {
		this.consoleErrorOutputStream = consoleErrorOutputStream;
	}

	public PrintWriter getConsoleMessageWriter() {
		return consoleMessageWriter;
	}

	public void setConsoleMessageWriter(PrintWriter consoleMessageWriter) {
		this.consoleMessageWriter = consoleMessageWriter;
	}

	public PrintWriter getConsoleErrorWriter() {
		return consoleErrorWriter;
	}

	public void setConsoleErrorWriter(PrintWriter consoleErrorWriter) {
		this.consoleErrorWriter = consoleErrorWriter;
	}

}
