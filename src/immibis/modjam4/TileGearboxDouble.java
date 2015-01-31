package immibis.modjam4;

import immibis.modjam4.shaftnet.NetworkLink;
import immibis.modjam4.shaftnet.ShaftNetwork;
import immibis.modjam4.shaftnet.ShaftNode;
import immibis.modjam4.shaftnet.ShaftPhysicsUniverse;

public class TileGearboxDouble extends TileMachine {
	
	ShaftNode hsNode = new ShaftNode(this);
	ShaftNode lsNode = new ShaftNode(this);
	
	NetworkLink networkLink;
	
	
	ShaftNetwork lsNetwork = lsNode.getNetwork();
	ShaftNetwork hsNetwork = hsNode.getNetwork();
	
	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		hsNode.setSideMask(1 << getBlockMetadata());
		lsNode.setSideMask(1 << (getBlockMetadata() ^ 1));
	}
	
	private boolean firstTick = true;
	public void updateEntity() {
		if(firstTick) {
			firstTick = false;
			hsNode.setSideMask(1 << getBlockMetadata());
			lsNode.setSideMask(1 << (getBlockMetadata() ^ 1));
			updateNeighbourConnections();
		}
		
		if(worldObj.isRemote)
			return;
		
		ShaftNetwork lsNetwork_new = lsNode.getNetwork();
		ShaftNetwork hsNetwork_new = hsNode.getNetwork();
		
		if(networkLink == null || lsNetwork_new != lsNetwork || hsNetwork_new != hsNetwork) {
			lsNetwork = lsNetwork_new;
			hsNetwork = hsNetwork_new;
			
			if(networkLink != null) {
				//System.out.println("unlinking old gearbox link");
				networkLink.unlink();
			}
			networkLink = ShaftPhysicsUniverse.linkNetworks(lsNetwork_new, hsNetwork_new, 2);
		}
	}
	
	@Override
	protected void onUnload() {
		if(networkLink != null) {
			networkLink.unlink();
			networkLink = null;
		}
		lsNode.deleteNode();
		hsNode.deleteNode();
	}
	
	@Override
	protected void updateNeighbourConnections() {
		lsNode.updateNeighbours();
		hsNode.updateNeighbours();
		
		
		
		if(!worldObj.isRemote)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public ShaftNode getShaftNode(int side) {
		int meta = getBlockMetadata();
		if(side == meta)
			return hsNode;
		if(side == (meta ^ 1))
			return lsNode;
		return null;
	}
}
