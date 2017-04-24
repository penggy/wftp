package wftp.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;

public class LocalFileTransfer extends LocalSelectionTransfer {

	private static final String TYPE_NAME = "file-selection-transfer-format" + (new Long(System.currentTimeMillis())).toString(); //$NON-NLS-1$;

	private static final int TYPEID = registerType(TYPE_NAME);

	private static final LocalFileTransfer INSTANCE = new LocalFileTransfer();
	
	public static LocalSelectionTransfer getTransfer() {
		return INSTANCE;
	}

	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}
	
	

}
