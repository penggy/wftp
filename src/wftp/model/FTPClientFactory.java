package wftp.model;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.jface.preference.IPreferenceStore;

import wftp.Activator;
import wftp.WftpPreference;

public class FTPClientFactory {

	private static IPreferenceStore ps = Activator.getDefault()
			.getPreferenceStore();

	public static FTPClient createFTPClient() {
		FTPClient client = new FTPClient();
		int timeout = ps.getInt(WftpPreference.TIMEOUT_KEY);
		if (timeout == 0) {
			timeout = WftpPreference.DEFAULT_TIMEOUT;
		}
		//client.setDefaultTimeout(timeout * 1000);
		client.setConnectTimeout(timeout * 1000);
		return client;
	}

	public static FTPClient createFTPClient(final ConnNode node) throws IOException {
		FTPClient client = createFTPClient();
		client.setControlEncoding(node.getEncode());
		return client;
	}

}
