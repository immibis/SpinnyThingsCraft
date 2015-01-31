package immibis.modjam4.shaftnet;

import immibis.modjam4.IShaft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;


public class ShaftNode {
	
	private TileEntity te;
	private int sideMask = 0;
	private ShaftNode adjNodes[] = new ShaftNode[6];
	
	public SpeedTorqueCurve getSpeedTorqueCurve() {
		return null;
	}
	
	ShaftNetwork network = new ShaftNetwork();
	{network.add(this);}
	
	public ShaftNode(TileEntity te) {
		this.te = te;
	}
	
	public ShaftNetwork getNetwork() {
		return network;
	}
	
	public void updateNeighbours() {
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
			neighbour.network.mergeInto(network);
		}
		
		if(neighbour != adjNodes[dir]) {
			boolean newNetwork = (adjNodes[dir] != null);
			//if(newNetwork) System.out.println(this+" "+dir+" "+adjNodes[dir]+" -> "+neighbour);
			adjNodes[dir] = neighbour;
			return newNetwork;
		}
		
		return false;
	}

	public void tick() {
		long time = te.getWorldObj().getTotalWorldTime();
		if(time != network.lastUpdate) {
			network.lastUpdate = time;
			network.tick();
		}
	}

	public void setSideMask(int i) {
		sideMask = i;
	}

}
