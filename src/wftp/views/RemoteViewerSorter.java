package wftp.views;

import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import wftp.model.ConnNode;

public class RemoteViewerSorter extends ViewerSorter {

	public static final int NAME = 1;
	public static final int TIME = 2;
	public static final int SIZE = 3;
	public static final int TYPE = 4;
	public static final int ATTR = 5;

	public static final ViewerSorter NAME_ASC = new RemoteViewerSorter(NAME);
	public static final ViewerSorter NAME_DESC = new RemoteViewerSorter(-NAME);
	public static final ViewerSorter TIME_ASC = new RemoteViewerSorter(TIME);
	public static final ViewerSorter TIME_DESC = new RemoteViewerSorter(-TIME);
	public static final ViewerSorter SIZE_ASC = new RemoteViewerSorter(SIZE);
	public static final ViewerSorter SIZE_DESC = new RemoteViewerSorter(-SIZE);
	public static final ViewerSorter TYPE_ASC = new RemoteViewerSorter(TYPE);
	public static final ViewerSorter TYPE_DESC = new RemoteViewerSorter(-TYPE);
	public static final ViewerSorter ATTR_ASC = new RemoteViewerSorter(ATTR);
	public static final ViewerSorter ATTR_DESC = new RemoteViewerSorter(-ATTR);

	private int sortType;// 当前所要排序的列，取自上面的ID、USERID两值或其相反数

	// 构造函数用private，表示不能在外部创建ArchiveEditorSorter对象
	private RemoteViewerSorter(int sortType) {
		this.sortType = sortType;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		FTPFile f1 = (FTPFile) e1;
		FTPFile f2 = (FTPFile) e2;
		switch (sortType) {
		case NAME:
			return f1.getName().compareTo(f2.getName());
		case -NAME:
			return f2.getName().compareTo(f1.getName());
		case TIME:
			return f1.getTimestamp().compareTo(f2.getTimestamp());
		case -TIME:
			return f2.getTimestamp().compareTo(f1.getTimestamp());
		case SIZE:
			return new Long(f1.getSize()).compareTo(new Long(f2.getSize()));
		case -SIZE:
			return new Long(f2.getSize()).compareTo(new Long(f1.getSize()));
		case TYPE:
			return ConnNode.getFTPFileType(f1).compareTo(
					ConnNode.getFTPFileType(f2));
		case -TYPE:
			return ConnNode.getFTPFileType(f2).compareTo(
					ConnNode.getFTPFileType(f1));
		case ATTR:
			return f1.getRawListing().compareTo(f2.getRawListing());
		case -ATTR:
			return f2.getRawListing().compareTo(f1.getRawListing());
		}
		return super.compare(viewer, e1, e2);
	}

}
