package wftp.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.PrintCommandListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

import wftp.model.ConnNode;

public class ConsoleFactory implements IConsoleFactory {

	private static MessageConsole console;
	public static final IConsoleManager conmgr = ConsolePlugin.getDefault()
			.getConsoleManager();
	public static List<ConnNode> nodes = new ArrayList<ConnNode>();

	@Override
	public void openConsole() {
		conmgr.addConsoles(new IConsole[] { console });
		console.activate();
	}

	public static void closeConsole() {
		conmgr.removeConsoles(new IConsole[] { console });
	}

	public static MessageConsole regist(final ConnNode node) {
		if (node != null && !nodes.contains(node)) {
			if (nodes.size() == 0) {
				console = new MessageConsole("WFTPÏûÏ¢Ì¨", null);
				conmgr.addConsoles(new IConsole[] { console });
			}
			console.activate();
			nodes.add(node);
			if (node.getConsoleMessageOutputStream() == null
					|| node.getConsoleMessageOutputStream().isClosed()) {
				IOConsoleOutputStream msg = console.newOutputStream();
				node.setConsoleMessageOutputStream(msg);
				node.setConsoleMessageWriter(new PrintWriter(msg) {

					@Override
					public void print(String s) {
						s = "[" + node.toString() + "] " + s;
						super.print(s);
					}

				});
				node.getFtpClient().addProtocolCommandListener(
						new PrintCommandListener(new PrintWriter(msg) {

							@Override
							public void print(String s) {
								s = "[" + node.toString() + "] " + s;
								super.print(s);
							}
						}));
			}
			if (node.getConsoleErrorOutputStream() == null
					|| node.getConsoleErrorOutputStream().isClosed()) {
				IOConsoleOutputStream error = console.newOutputStream();
				error.setColor(new Color(null, 255, 0, 0));
				node.setConsoleErrorOutputStream(error);
				node.setConsoleErrorWriter(new PrintWriter(error) {

					@Override
					public void print(String s) {
						s = "[" + node.toString() + "] " + s;
						super.print(s);
					}

				});
			}
			return console;
		} else {
			return null;
		}
	}

	public static void unregist(ConnNode node) {
		if (node != null) {
			nodes.remove(node);
			if (nodes.size() == 0) {
				conmgr.removeConsoles(new IConsole[] { console });
			}
			if (node.getConsoleMessageOutputStream() != null
					&& !node.getConsoleMessageOutputStream().isClosed()) {
				try {
					node.getConsoleMessageOutputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				node.setConsoleMessageOutputStream(null);
			}
			if (node.getConsoleErrorOutputStream() != null
					&& !node.getConsoleErrorOutputStream().isClosed()) {
				try {
					node.getConsoleErrorOutputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				node.setConsoleErrorOutputStream(null);
			}
		}
	}

}
