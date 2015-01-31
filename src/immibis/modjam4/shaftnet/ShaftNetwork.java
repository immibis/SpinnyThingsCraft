package immibis.modjam4.shaftnet;

import immibis.modjam4.ShaftUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A network is a bunch of shaft machines rotating at the same speed.
 */
public class ShaftNetwork {
	
	private boolean deleted = false;
	void markDeleted() {deleted = true;}
	public boolean isDeleted() {return deleted;}
	
	List<ShaftNode> nodes = new ArrayList<ShaftNode>();
	Collection<NetworkLink> links = new HashSet<NetworkLink>();
	
	NetworkGroup group;
	
	double relativeVelocity = 1; // relative to group velocity
	
	public int angle;
	public long angvel;
	
	long lastUpdate;

	public void add(ShaftNode node) {
		if(isDeleted()) throw new AssertionError("object was deleted");
		if(node.network != this) throw new AssertionError();
		if(nodes.contains(node)) throw new AssertionError("node already added to network");
		nodes.add(node);
	}

	void tick() {
		if(isDeleted()) throw new IllegalStateException("object was deleted");
		if(group.needVelocityRecalc) {
			group.needVelocityRecalc = false;
			group.recalcVelocity();
		}
		
		if(group.noValidVelocities) {
			angvel = 0;
			return;
		}
		
		angvel = (long)(group.groupAngVel * relativeVelocity);
		angle += angvel;
		
		//angvel *= 0.95;
		
		double inertia = group.calcInertia() * (relativeVelocity * relativeVelocity);
		
		long sumtorque = 0;
		for(ShaftNode sn : nodes) {
			SpeedTorqueCurve stc = sn.getSpeedTorqueCurve();
			if(stc != null) {
				sumtorque += stc.getTorqueAtSpeed(angvel) / inertia;
			}
		}
		
		//System.out.println("angvel "+angvel+", sumtorque "+sumtorque+", inertia "+inertia);
		
		group.groupAngVel += sumtorque;
	}

	// TODO what does this even do?
	ShaftNetwork createSplitNetwork() {
		if(isDeleted()) throw new IllegalStateException("object was deleted");
		ShaftNetwork n = getUniverse().createNetwork(null);
		
		n.angle = angle;
		n.angvel = angvel;
		//n.group.groupAngVel = group.groupAngVel;
		//n.relativeVelocity = relativeVelocity;
		n.group.needVelocityRecalc = true;
		//System.out.println("createSplitNetwork "+this+" -> "+n);
		return n;
	}
	
	void propagateGroup(NetworkGroup g) {
		if(isDeleted()) throw new IllegalStateException("network "+hashCode()+" was deleted");
		if(group == g)
			return;
		
		if(!group.networks.remove(this)) throw new AssertionError();
		if(group.networks.size() == 0) group.getUniverse().deleteGroup(group);
		group = g;
		g.add(this);
		
		for(NetworkLink l : links) {
			l.netA.propagateGroup(g);
			l.netB.propagateGroup(g);
		}
	}
	
	@Override
	public String toString() {
		return "NET"+netID+", group="+Integer.toHexString(group == null ? 0 : group.hashCode())+", rv="+relativeVelocity+", angvel="+ShaftUtils.toDegreesPerSecond((int)angvel)
			+", inertia="+(group == null ? 0 : group.calcInertia() * (relativeVelocity * relativeVelocity));
	}

	double calcNetworkInertia() {
		return nodes.size();
	}

	public void forceAngVel(long angvel) {
		group.groupAngVel = (long)(angvel / relativeVelocity);
	}

	/** Checks some invariants. */
	public void validate() {
		if(isDeleted())
			throw new IllegalStateException("object was deleted");
		if(nodes.size() == 0)
			throw new AssertionError("empty network");
		if(group == null)
			throw new AssertionError("no group");
		if(!group.networks.contains(this))
			throw new AssertionError("no reference back from group");
		for(ShaftNode n : nodes) {
			if(n.network != this)
				throw new AssertionError("node in network is actually in different network");
			n.validate();
		}
		for(NetworkLink link : links) {
			link.validate();
			if(link.netA != this && link.netB != this)
				throw new AssertionError("network contains link which is not for this network");
		}
	}
	
	public ShaftPhysicsUniverse getUniverse() {
		return group.getUniverse();
	}
	
	
	private static AtomicInteger nextNetID = new AtomicInteger(0);
	public final int netID = nextNetID.incrementAndGet();
}
