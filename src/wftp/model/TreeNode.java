package wftp.model;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.eclipse.swt.graphics.Image;

public abstract class TreeNode {
	private String label;
	private TreeNode parent;
	private Element node;
	private int level = 0;
	
	public Element getNode() {
		return node;
	}

	public void setNode(Element node) {
		this.node = node;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TreeNode)) return false;
		TreeNode castOther = (TreeNode) obj;
		return castOther.node.equals(this.node);
	}

	private List<TreeNode> children = new ArrayList<TreeNode>();
	
	public abstract Image getImage();
	
	public void addChild(TreeNode child) {
		this.children.add(child);
	}
	
	public boolean hasChild() {
		return !this.children.isEmpty();
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
		this.level = parent.level+1;
		parent.addChild(this);
	}
	public List<TreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}
	public abstract void disConnect();
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean isInherit(TreeNode node) {
		if(node instanceof FolderNode) {
			TreeNode cursor = this;
			while(cursor != null) {
				if(cursor.getLevel() < node.getLevel()) {
					return false;
				}
				if(cursor == node) {
					return true;
				}
				cursor = cursor.parent;
			}
		}
		return false;
	}
	
}
