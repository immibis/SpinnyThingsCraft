package immibis.modjam4.shaftnet;

import cpw.mods.fml.common.FMLCommonHandler;
import immibis.modjam4.IShaft;
import immibis.modjam4.Modjam4Mod;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;


public class ShaftNode {
	
	private boolean deleted = false;
	void markDeleted() {deleted = true;}
	boolean isDeleted() {return deleted;}
	
	private TileEntity te;
	private int sideMask = 0;
	private ShaftNode adjNodes[] = new ShaftNode[6];
	
	public SpeedTorqueCurve getSpeedTorqueCurve() {
		return null;
	}
	
	ShaftNetwork network;
	
	public ShaftNode(TileEntity te) {
		this.te = te;
		if(FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			network = Modjam4Mod.universe.createNetwork(this);
		}
	}
	
	private static ShaftNetwork DUMMY_NETWORK = new ShaftNetwork();
	public ShaftNetwork getNetwork() {
		if(network == null) return DUMMY_NETWORK; // TODO remove
		
		return network;
	}
	
	public void updateNeighbours() {
		if(network == null)
			return;
		
		boolean newNetwork = false;
		for(int k = 0; k < 6; k++)
			newNetwork |= updateNeighbour(k);
		
		if(newNetwork) {
			
			ShaftNetwork newnet = network.createSplitNetwork();
			
			//System.out.println("propagate split from "+this);
			propagateNetwork(newnet);
		}
	}

	private void propagateNetwork(ShaftNetwork newNetwork) {
		if(network == newNetwork)
			return;
		
		if(!network.nodes.remove(this))
			throw new AssertionError();
		if(network.nodes.size() == 0) {
			
			// XXX HACK
			while(network.links.size() > 0)
				network.links.iterator().next().unlink();
			
			network.getUniverse().deleteNetwork(network);
		}
		network = newNetwork;
		newNetwork.add(this);
		
		//System.out.println("propagate "+te+" "+network+" -> "+Arrays.toString(adjNodes));
		
		for(ShaftNode neighbour : adjNodes)
			if(neighbour != null)
				neighbour.propagateNetwork(newNetwork);
	}
	
	@Override
	public String toString() {
		return "ShaftNode("+te.toString()+")";
	}

	private boolean updateNeighbour(int dir) {
		int x = te.xCoord+Facing.offsetsXForSide[dir];
		int y = te.yCoord+Facing.offsetsYForSide[dir];
		int z = te.zCoord+Facing.offsetsZForSide[dir];
		ShaftNode neighbour = null;
		if(0 != (sideMask & (1 << dir))) {
			if(te.getWorldObj().blockExists(x, y, z)) {
				TileEntity ote = te.getWorldObj().getTileEntity(x, y, z);
				if(ote instanceof IShaft)
					neighbour = ((IShaft)ote).getShaftNode(dir^1);
			}
		}
		
		if(neighbour != null && neighbour.network != network) {
			ShaftPhysicsUniverse.mergeNetworks(network, neighbour.network);
			if(neighbour.network != network)
				throw new AssertionError();
		}
		
		if(neighbour != adjNodes[dir]) {
			boolean newNetwork = (adjNodes[dir] != null);
			//if(newNetwork) System.out.println(this+" "+dir+" "+adjNodes[dir]+" -> "+neighbour);
			adjNodes[dir] = neighbour;
			return newNetwork;
		}
		
		return false;
	}

	public void setSideMask(int i) {
		sideMask = i;
	}
	
	/** Checks some invariants. */
	public void validate() {
		//if(sideMask == 0)
		//	throw new AssertionError("sideMask is 0; tile is "+te);
		if(te.isInvalid())
			throw new AssertionError("tile is invalid");
		if(!(te instanceof IShaft))
			throw new AssertionError("tile does not implement IShaft");
		if(te.getWorldObj().getTileEntity(te.xCoord, te.yCoord, te.zCoord) != te)
			throw new AssertionError("tile is stale");
		for(int k = 0; k < 6; k++)
			if((sideMask & (1 << k)) != 0)
				if(((IShaft)te).getShaftNode(k) != this)
					throw new AssertionError("tile.getShaftNode("+k+") != this");
	}
	
	ShaftPhysicsUniverse getUniverse() {
		return network.getUniverse();
	}

	public void deleteNode() {
		if(network != null)
			getUniverse().deleteNode(this);
	}

}
