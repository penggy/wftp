package wftp.views;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class LocalViewerSorter extends ViewerSorter {

	public static final int NAME = 1;
	public static final int TIME = 2;
	public static final int SIZE = 3;
	public static final int TYPE = 4;

	public static final ViewerSorter NAME_ASC = new LocalViewerSorter(NAME);
	public static final ViewerSorter NAME_DESC = new LocalViewerSorter(-NAME);
	public static final ViewerSorter TIME_ASC = new LocalViewerSorter(TIME);
	public static final ViewerSorter TIME_DESC = new LocalViewerSorter(-TIME);
	public static final ViewerSorter SIZE_ASC = new LocalViewerSorter(SIZE);
	public static final ViewerSorter SIZE_DESC = new LocalViewerSorter(-SIZE);
	public static final ViewerSorter TYPE_ASC = new LocalViewerSorter(TYPE);
	public static final ViewerSorter TYPE_DESC = new LocalViewerSorter(-TYPE);

	private int sortType;// 当前所要排序的列，取自上面的ID、USERID两值或其相反数

	// 构造函数用private，表示不能在外部创建ArchiveEditorSorter对象
	private LocalViewerSorter(int sortType) {
		this.sortType = sortType;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		File f1 = (File) e1;
		File f2 = (File) e2;
		String type1 = FileSystemView.getFileSystemView().getSystemTypeDescription(f1);
		if(type1==null || type1.trim().equals("")) {
			type1 = new JFileChooser().getTypeDescription(f1);
			type1 = type1==null ? "" : type1;
		}
		String type2 = FileSystemView.getFileSystemView().getSystemTypeDescription(f2);
		if(type2==null || type1.trim().equals("")) {
			type2 = new JFileChooser().getTypeDescription(f2);
			type2 = type2==null ? "" : type2;
		}
		switch (sortType) {
		case NAME:
			return f1.getName().compareTo(f2.getName());
		case -NAME:
			return f2.getName().compareTo(f1.getName());
		case TIME:
			return new Long(f1.lastModified()).compareTo(new Long(f2
					.lastModified()));
		case -TIME:
			return new Long(f2.lastModified()).compareTo(new Long(f1
					.lastModified()));
		case SIZE:
			return new Long(f1.length()).compareTo(new Long(f2.length()));
		case -SIZE:
			return new Long(f2.length()).compareTo(new Long(f1.length()));
		case TYPE:
			return type1.compareTo(type2);
		case -TYPE:
			return type2.compareTo(type1);
		}
		return super.compare(viewer, e1, e2);
	}

}
