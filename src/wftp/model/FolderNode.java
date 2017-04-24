package wftp.model;

import org.eclipse.swt.graphics.Image;

import wftp.ImageContext;

public class FolderNode extends TreeNode{
	
	public static String DEFAULT_LABEL = "ÐÂ½¨ÊÕ²Ø¼Ð";
	private static Image image = ImageContext.getImage(ImageContext.FOLDER);

	public Image getImage() {
		return image;
	}

	@Override
	public void disConnect() {
		for(TreeNode node : this.getChildren()) {
			node.disConnect();
		}
	}

}
