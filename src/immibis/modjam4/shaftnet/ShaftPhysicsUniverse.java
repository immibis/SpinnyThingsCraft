package immibis.modjam4.shaftnet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShaftPhysicsUniverse {
	
	public static final boolean ASSERT_LOTS = true;
	
	private Collection<ShaftNetwork> networks = new HashSet<>();
	private Collection<NetworkGroup> groups = new HashSet<>();
	
	private Map<Integer, ShaftNetwork> networksByID = new HashMap<>();

	public void read(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void write(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	void deleteNetwork(ShaftNetwork n) {
		if(n.getUniverse() != this)
			throw new IllegalArgumentException("wrong universe");
		if(n.isDeleted())
			throw new IllegalArgumentException("network already deleted");
		if(n.links.size() > 0)
			throw new IllegalArgumentException("can't delete a network that still has links "+n.links);
		if(n.nodes.size() > 0)
			throw new IllegalArgumentException("can't delete a network that still has nodes "+n.nodes);
		if(!networks.remove(n))
			throw new AssertionError("node not in networks list?");
		if(networksByID.remove(n.netID) != n)
			throw new AssertionError();
		if(!n.group.networks.remove(n))
			throw new AssertionError("node not in group networks list?");
		if(n.group.networks.size() == 0)
			deleteGroup(n.group);
		n.markDeleted();
	}
	
	void deleteGroup(NetworkGroup group) {
		if(group.getUniverse() != this)
			throw new IllegalArgumentException("group in wrong universe");
		if(group.isDeleted())
			throw new IllegalArgumentException("group already deleted");
		if(group.networks.size() > 0)
			throw new IllegalArgumentException("cannot delete group with networks");
		if(!groups.remove(group))
			throw new AssertionError("group not in groups list?");
		group.markDeleted();
	}

	public void tick() {
		if(ASSERT_LOTS)
			validate();
		
		for(ShaftNetwork n : networks)
			n.tick();
	}
	
	/** Checks some invariants. */
	void validate() {
		for(ShaftNetwork n : networks)
			n.validate();
		for(NetworkGroup g : groups)
			g.validate();
	}
	
	/** Creates a new network, part of a new group, containing only the given node. */
	ShaftNetwork createNetwork(ShaftNode initialNode) {
		if(ASSERT_LOTS && FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER)
			throw new AssertionError("should only be called on the server");
		
		NetworkGroup g = new NetworkGroup(this);
		ShaftNetwork n = new ShaftNetwork();
		
		if(initialNode != null) {
			initialNode.network = n;
			n.add(initialNode);
		}
		n.group = g;
		g.add(n);
		
		groups.add(g);
		networks.add(n);
		if(networksByID.put(n.netID, n) != null)
			throw new AssertionError();
		
		return n;
	}
	
	static void mergeNetworks(ShaftNetwork a, ShaftNetwork b) {
		if(a.isDeleted() || b.isDeleted())
			throw new IllegalStateException("object was deleted");
		if(a == b)
			return;
		
		if(a.group != b.group) {
			linkNetworks(a, b, 1);
			if(a.group != b.group)
				throw new AssertionError();
		}
				
		System.out.println("merging "+a+" into "+b);
		
		for(NetworkLink link : a.links) {
			if(link.netA == a) {
				if(link.netB == b) {
					if(!b.links.remove(link)) throw new AssertionError();
					continue;
				} else {
					link.netA = b;
					b.links.add(link);
				}
				
			} else if(link.netB == a) {
				if(link.netA == b) {
					if(!b.links.remove(link)) throw new AssertionError();
					continue;
				} else {
					link.netB = b;
					b.links.add(link);
				}
				
			} else
				throw new AssertionError();
			
			link.validate();
		}
		a.links.clear();
		
		for(ShaftNode node : a.nodes) {
			node.network = b;
			b.nodes.add(node);
		}
		a.nodes.clear();
		
		a.getUniverse().deleteNetwork(a);
	}

	/** a.angvel * velocityMultiplier = b.angvel */
	public static NetworkLink linkNetworks(ShaftNetwork a, ShaftNetwork b, double velocityMultiplier) {
		if(a == b)
			throw new AssertionError("can't link a network to itself");
		
		NetworkLink link = new NetworkLink(a, b, velocityMultiplier);
		a.links.add(link);
		b.links.add(link);
		if(a.group != b.group)
			a.group.mergeInto(b.group);
		a.group.needVelocityRecalc = true;
		
		link.validate();
		
		return link;
	}

	static boolean findPathBetweenNetworks(ShaftNetwork a, ShaftNetwork b) {
		if(a == b) throw new AssertionError();
		
		Set<ShaftNetwork> closed = new HashSet<>();
		List<ShaftNetwork> open = new ArrayList<>();
		
		open.add(a);
		
		while(open.size() > 0) {
			ShaftNetwork _this = open.remove(open.size() - 1);
			
			for(NetworkLink link : _this.links) {
				ShaftNetwork other = link.getOther(_this);
				if(closed.add(other)) {
					// not already seen
					if(other == b)
						return true;
					open.add(other);
				}
			}
		}
		
		return false;
	}

	public void deleteNode(ShaftNode node) {
		if(!node.network.nodes.remove(node))
			throw new AssertionError();
		
		if(node.network.nodes.size() == 0) {
			deleteNetwork(node.network);
		}
		
		node.markDeleted();
	}

	public NetworkGroup createGroup() {
		if(ASSERT_LOTS && FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER)
			throw new AssertionError("should only be called on the server");
		
		NetworkGroup g = new NetworkGroup(this);
		groups.add(g);
		
		return g;
	}

	public ShaftNetwork getNetworkByID(int id) {
		return networksByID.get(id);
	}
	
}
