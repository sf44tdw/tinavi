package tainavi;

import javax.swing.tree.DefaultMutableTreeNode;

public class VWListedTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	private boolean used = true;
	
	public final boolean isUsed() { return used; }
	public final boolean isUnUsed() { return ! used; }
	
	public VWListedTreeNode(Object userObject) {
		super(userObject);
		this.used = true;
	}
	
	public VWListedTreeNode(Object userObject, boolean used) {
		super(userObject);
		this.used = used;
	}
}
