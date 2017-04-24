package wftp.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

import wftp.model.ConnNode;


public class StandaloneRule implements ISchedulingRule{
	
	private ConnNode node;
	
	public StandaloneRule(ConnNode node) {
		this.node = node;
	}
	
	public boolean contains(ISchedulingRule rule) {
		return this.equals(rule);
	}
	public boolean isConflicting(ISchedulingRule rule) {
		return this.equals(rule);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof StandaloneRule)) return false;
		StandaloneRule castOther = (StandaloneRule) obj;
		return castOther.getNode().equals(node);
	}

	public ConnNode getNode() {
		return node;
	}

	public void setNode(ConnNode node) {
		this.node = node;
	}
	
	
};
