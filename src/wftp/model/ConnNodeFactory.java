package wftp.model;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import wftp.Context;

public class ConnNodeFactory {

	private static ConnNodeFactory instance = new ConnNodeFactory();
	private TreeNode root = new FolderNode();

	private ConnNodeFactory() {
		init();
	}

	private void init() {
		Document doc = Context.getInstance().getDoc();
		Element rootNode = doc.getRootElement().element("conns");
		if (rootNode == null) {
			rootNode = doc.getRootElement().addElement("conns");
		}
		root.setNode(rootNode);
		readNodes(root);
	}

	public static ConnNodeFactory getInstance() {
		return instance;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void readNodes(TreeNode parent) {
		parent.getChildren().clear();
		Element pNode = parent.getNode();
		List<?> children = pNode.selectNodes("conn");
		if(children != null) {
			for (Object o : children) {
				Element e = (Element) o;
				ConnNode node = new ConnNode();
				Node label = e.selectSingleNode("label");
				Node host = e.selectSingleNode("host");
				Node port = e.selectSingleNode("port");
				Node username = e.selectSingleNode("username");
				Node password = e.selectSingleNode("password");
				Node encode = e.selectSingleNode("encode");
				
				if(host == null) {
					pNode.remove(e);
					continue;
				}
				node.setLabel(label==null?host.getText():label.getText());
				node.setHost(host.getText());
				if(port==null) {
					node.setPort(ConnNode.DEFAULT_PORT);
				}else {
					try {
						node.setPort(Integer.parseInt(port.getText()));
					}catch (Exception ee) {
						node.setPort(ConnNode.DEFAULT_PORT);
					}
				}
				node.setUsername(username==null?ConnNode.DEFAULT_USER:username.getText());
				node.setPassword(password==null?"":password.getText());
				node.setEncode(encode==null?ConnNode.DEFAULT_ENCODE:encode.getText());
				
				node.setNode(e);
				node.setParent(parent);
			}
		}
		children = pNode.selectNodes("folder");
		if(children != null) {
			for (Object o : children) {
				Element e = (Element) o;
				FolderNode node = new FolderNode();
				
				Node label = e.selectSingleNode("label");
				node.setLabel(label==null?FolderNode.DEFAULT_LABEL:label.getText());
				
				node.setNode(e);
				node.setParent(parent);
				readNodes(node);
			}
		}
	}

	public void delete(TreeNode treeNode) {
		TreeNode pTreeNode = treeNode.getParent();
		if (pTreeNode != null) {
			treeNode.disConnect();
			Element pNode = pTreeNode.getNode();
			pTreeNode.getChildren().remove(treeNode);
			pNode.remove(treeNode.getNode());
			treeNode.getNode().setParent(null);
			Context.getInstance().storeConfig();
		}
	}

	public void update(TreeNode treeNode) {
		if (treeNode instanceof FolderNode) {
			FolderNode n = (FolderNode) treeNode;
			n.getNode().element("label").setText(n.getLabel());
		} else if (treeNode instanceof ConnNode) {
			ConnNode n = (ConnNode) treeNode;
			if (n.getLabel() == null || n.getLabel().trim().equals("")) {
				n.setLabel(n.getHost());
			}
			n.getNode().element("label").setText(n.getLabel());
			n.getNode().element("host").setText(n.getHost());
			n.getNode().element("port").setText(String.valueOf(n.getPort()));
			n.getNode().element("username").setText(n.getUsername());
			n.getNode().element("password").setText(n.getPassword());
			n.getNode().element("encode").setText(n.getEncode());
		}
		Context.getInstance().storeConfig();
	}

	public void move(TreeNode source, TreeNode target) {
		if (source == null || source.getParent() == target)
			return;
		if (target == null) {
			target = root;
		}
		while (!(target instanceof FolderNode)) {
			target = target.getParent();
		}
		TreeNode oldParent = source.getParent();
		oldParent.getChildren().remove(source);
		oldParent.getNode().remove(source.getNode());
		source.setParent(target);
		target.getNode().add(source.getNode());
		Context.getInstance().storeConfig();
	}

	public void save(TreeNode pTreeNode, TreeNode treeNode) {
		if (pTreeNode == null) {
			pTreeNode = root;
		}
		while (!(pTreeNode instanceof FolderNode)) {
			pTreeNode = pTreeNode.getParent();
		}
		treeNode.setParent(pTreeNode);
		Element pNode = pTreeNode.getNode();
		if (treeNode instanceof FolderNode) {
			FolderNode n = (FolderNode) treeNode;
			Element e = pNode.addElement("folder");
			e.addElement("label").setText(n.getLabel());
			n.setNode(e);
		} else if (treeNode instanceof ConnNode) {
			ConnNode n = (ConnNode) treeNode;
			if (n.getLabel() == null || n.getLabel().trim().equals("")) {
				n.setLabel(n.getHost());
			}
			Element e = pNode.addElement("conn");
			e.addElement("label").setText(n.getLabel());
			e.addElement("host").setText(n.getHost());
			e.addElement("port").setText(String.valueOf(n.getPort()));
			e.addElement("username").setText(n.getUsername());
			e.addElement("password").setText(n.getPassword());
			e.addElement("encode").setText(n.getEncode());
			n.setNode(e);
		}
		Context.getInstance().storeConfig();
	}

}
