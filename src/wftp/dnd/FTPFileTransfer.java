package wftp.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.swt.dnd.TransferData;

import wftp.views.RemoteView;

public class FTPFileTransfer extends LocalSelectionTransfer {

	private static final String TYPE_NAME = "ftpfile-selection-transfer-format" + (new Long(System.currentTimeMillis())).toString(); //$NON-NLS-1$;

	private static final int TYPEID = registerType(TYPE_NAME);

	private static final FTPFileTransfer INSTANCE = new FTPFileTransfer();

	private RemoteView rv;

	public static LocalSelectionTransfer getTransfer() {
		return INSTANCE;
	}

	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	public RemoteView getRv() {
		return rv;
	}

	public void setRv(RemoteView rv) {
		this.rv = rv;
	}

	@Override
	public Object nativeToJava(TransferData transferData) {
		super.nativeToJava(transferData);
		return new Object[] { rv, getSelection() };
	}

}
