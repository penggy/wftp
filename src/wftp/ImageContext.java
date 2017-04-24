package wftp;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

public class ImageContext {

	public static final String EXIT = "exit";
	public static final String CONNECT = "connect";
	public static final String DISCONNECT = "disconnect";
	public static final String ACCEPT = "accept";
	public static final String ADD = "add";
	public static final String SERVER_ADD = "server_add";
	public static final String SERVER_DELETE = "server_delete";
	public static final String SERVER_CONNECT = "server_connect";
	public static final String SERVER_EDIT = "server_edit";
	public static final String SERVER = "server";
	public static final String SERVER_GO = "server_go";
	public static final String SERVER_KEY = "server_key";
	public static final String APPLICATION_XP_TERMINAL = "application_xp_terminal";
	public static final String APPLICATION_LIGHTNING = "application_lightning";
	public static final String HOUSE = "house";
	public static final String APPLICATION_HOME = "application_home";
	public static final String IMPORT_WIZ = "import_wiz";
	public static final String EXPORT_WIZ = "export_wiz";
	public static final String FOLDER = "folder";
	public static final String FOLDER_ADD = "folder_add";
	public static final String FOLDER_EDIT = "folder_edit";
	public static final String FOLDER_DELETE = "folder_delete";
	public static final String REFRESH = "refresh";
	public static final String LINK = "link";
	public static final String PAGE_WHITE = "page_white";
	public static final String COMPUTER = "computer";
	public static final String DRIVE = "drive";
	public static final String DRIVE_NETWORK = "drive_network";
	public static final String FOLDER_USER = "folder_user";
	public static final String TEXTFIELD_RENAME = "textfield_rename";
	public static final String WFTP_16 = "WFTP_16";
	public static final String ABOUT = "about";
	public static final String HOME_NAV = "home_nav";

	private static ImageRegistry imageRegistry;

	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			registImage();
		}
		return imageRegistry;
	}

	private static void registImage() {
		registImage(ACCEPT, "icons/accept.png");
		registImage(ADD, "icons/add.png");
		registImage(APPLICATION_XP_TERMINAL,
				"icons/application_xp_terminal.png");
		registImage(CONNECT, "icons/connect.png");
		registImage(DISCONNECT, "icons/disconnect.png");
		registImage(EXIT, "icons/door_out.png");
		registImage(HOUSE, "icons/house.png");
		registImage(SERVER, "icons/server.png");
		registImage(SERVER_ADD, "icons/server_add.png");
		registImage(SERVER_CONNECT, "icons/server_connect.png");
		registImage(SERVER_DELETE, "icons/server_delete.png");
		registImage(SERVER_EDIT, "icons/server_edit.png");
		registImage(SERVER_GO, "icons/server_go.png");
		registImage(SERVER_KEY, "icons/server_key.png");
		registImage(FOLDER, "icons/folder.png");
		registImage(FOLDER_ADD, "icons/folder_add.png");
		registImage(FOLDER_DELETE, "icons/folder_delete.png");
		registImage(FOLDER_EDIT, "icons/folder_edit.png");
		registImage(REFRESH, "icons/refresh.gif");
		registImage(LINK, "icons/link.png");
		registImage(PAGE_WHITE, "icons/page_white.png");
		registImage(COMPUTER, "icons/computer.png");
		registImage(DRIVE, "icons/drive.png");
		registImage(DRIVE_NETWORK, "icons/drive_network.png");
		registImage(FOLDER_USER, "icons/folder_user.png");
		registImage(APPLICATION_HOME, "icons/application_home.png");
		registImage(APPLICATION_LIGHTNING, "icons/application_lightning.png");
		registImage(IMPORT_WIZ, "icons/import_wiz.gif");
		registImage(EXPORT_WIZ, "icons/export_wiz.gif");
		registImage(APPLICATION_HOME, "icons/application_home.png");
		registImage(TEXTFIELD_RENAME, "icons/textfield_rename.png");
		registImage(WFTP_16, "icons/WFTP_16.gif");
		registImage(ABOUT, "icons/about.png");
		registImage(HOME_NAV, "icons/home_nav.gif");
	}

	private static void registImage(String key, String path) {
		URL url = FileLocator.find(Activator.getDefault().getBundle(),
				new Path(path), null);
		if (url != null) {
			ImageDescriptor imageDescriptor = ImageDescriptor
					.createFromURL(url);
			getImageRegistry().put(key, imageDescriptor);
		}
	}

	public static Image getImage(String key) {
		Image image = PlatformUI.getWorkbench().getSharedImages().getImage(key);
		return image == null ? getImageRegistry().get(key) : image;
	}

	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor imageDescriptor = PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(key);
		return imageDescriptor == null ? getImageRegistry().getDescriptor(key)
				: imageDescriptor;
	}

}
