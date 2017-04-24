package wftp.model;

public interface ConnectionListener {
	
	public void onConnect(ConnNode node);
	public void onDisconnect(ConnNode node);

}
