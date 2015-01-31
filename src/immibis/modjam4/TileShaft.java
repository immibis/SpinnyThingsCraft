package immibis.modjam4;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import immibis.modjam4.shaftnet.ShaftNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileShaft extends TileMachine {
	
	ShaftNode shaftNode = createShaftNode();
	
	public int clientNetID;
	
	protected ShaftNode createShaftNode() {
		return new ShaftNode(this);
	}
	
	@Override
	public ShaftNode getShaftNode(int side) {
		return (side & 6) == (getBlockMetadata() & 6) ? shaftNode : null;
	}
	
	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		shaftNode.setSideMask(getSideMask());
	}
	
	@Override
	public String toString() {
		return xCoord+"/"+yCoord+"/"+zCoord;
	}
	
	private boolean firstTick = true;
	@Override
	public void updateEntity() {
		if(firstTick) {
			firstTick = false;
			shaftNode.setSideMask(getSideMask());
			updateNeighbourConnections();
		}
		if(!worldObj.isRemote) {
			int netID = shaftNode.getNetwork().netID;
			if(netID != clientNetID) {
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				clientNetID = netID;
			}
		}
		super.updateEntity();
	}
	
	protected void onUnload() {
		firstTick = true;
		shaftNode.deleteNode();
		super.onUnload();
	}
	
	protected int getSideMask() {
		return 3 << (getBlockMetadata() & 6);
	}

	@Override
	protected void updateNeighbourConnections() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		shaftNode.updateNeighbours();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		clientNetID = pkt.func_148857_g().getInteger("netID");
		super.onDataPacket(net, pkt);
	}
	
	@Override
	public S35PacketUpdateTileEntity getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("netID", shaftNode.getNetwork().netID);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}

	public ShaftNode getShaftNode() {
		return shaftNode;
	}
}
