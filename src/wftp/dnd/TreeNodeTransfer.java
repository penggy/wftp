package wftp.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;

public class TreeNodeTransfer extends LocalSelectionTransfer {

	private static final String TYPE_NAME = "treenode-selection-transfer-format" + (new Long(System.currentTimeMillis())).toString(); //$NON-NLS-1$;

	private static final int TYPEID = registerType(TYPE_NAME);

	private static final TreeNodeTransfer INSTANCE = new TreeNodeTransfer();

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
