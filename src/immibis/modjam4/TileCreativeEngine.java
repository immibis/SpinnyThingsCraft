package immibis.modjam4;

import immibis.modjam4.shaftnet.ShaftNode;

public class TileCreativeEngine extends TileMachine {
	
	private ShaftNode shaftNode = new ShaftNode(this);
	{shaftNode.setSideMask(63);}

	@Override
	public ShaftNode getShaftNode(int side) {
		return shaftNode;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!worldObj.isRemote)
			shaftNode.getNetwork().angvel = 3 << 24;
	}
	
	@Override
	protected void updateNeighbourConnections() {
		shaftNode.updateNeighbours();
	}
}
