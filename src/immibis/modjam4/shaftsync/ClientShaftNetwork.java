package immibis.modjam4.shaftsync;

public class ClientShaftNetwork {
	public final int netID;
	public boolean isDeleted;
	public long angvel;
	public int angle;
	
	ClientShaftNetwork(int netID) {
		this.netID = netID;
	}
	
	int lastAccessTick;
}
