package immibis.modjam4;

import immibis.modjam4.shaftnet.ShaftNode;

public class TileGearboxDirectional extends TileMachine {
	
	private ShaftNode shaftNode = new ShaftNode(this);
	{shaftNode.setSideMask(63);}
	
	@Override
	public ShaftNode getShaftNode(int side) {
		return shaftNode;
	}
	
	@Override
	protected void updateNeighbourConnections() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		shaftNode.updateNeighbours();
	}
	
	@Override
	public void updateEntity() {
		shaftNode.tick();
		super.updateEntity();
	}
}
