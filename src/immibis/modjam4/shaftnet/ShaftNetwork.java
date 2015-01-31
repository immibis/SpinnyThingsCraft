package immibis.modjam4.shaftnet;

import immibis.modjam4.CableNetwork;
import immibis.modjam4.ICable;
import immibis.modjam4.ShaftUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


/**
 * A network is a bunch of shaft machines rotating at the same speed.
 */
public class ShaftNetwork {
	private List<ShaftNode> nodes = new ArrayList<ShaftNode>();
	private List<SpeedTorqueCurve> machineCurves = new ArrayList<SpeedTorqueCurve>();
	Collection<NetworkLink> links = new HashSet<NetworkLink>();
	
	double relativeVelocity = 1; // relative to group velocity
	
	NetworkGroup group = new NetworkGroup();
	{group.add(this);}
	
	public int angle;
	public long angvel;
	
	long lastUpdate;
	
	public void mergeInto(ShaftNetwork network) {
		if(network == this)
			return;
		
		network.angle += (int)(((double)angle - network.angle) * nodes.size() / (network.nodes.size() + nodes.size()));

		//System.out.println(relativeVelocity+" "+network.relativeVelocity);
		
		long myAngvel = (long)(angvel * relativeVelocity);
		long otherAngvel = (long)(network.angvel*network.relativeVelocity);
		
		double myInertia = calcNetworkInertia();
		double otherInertia = network.calcNetworkInertia();
		
		//network.angvel = (long)((angvel*myInertia + network.angvel*otherInertia) / (myInertia * otherInertia));
		
		//System.out.println("merging "+this+" into "+network);
		for(NetworkLink link : new ArrayList<NetworkLink>(links)) {
			if(link.netA == this) {
				link.netA = network;
				
			} else if(link.netB == this) {
				link.netB = network;
				
			} else
				throw new AssertionError();
		}
		links.clear();
		network.links.addAll(links);
		group.networks.remove(this);
		//group.mergeInto(network.group);
		
		for(ShaftNode c : nodes) {
			c.network = network;
			network.add(c);
		}
		nodes.clear();
	}

	public void add(ShaftNode node) {
		nodes.add(node);
		SpeedTorqueCurve curve = node.getSpeedTorqueCurve();
		if(curve != null)
			machineCurves.add(curve);
	}
	
	void addLink(NetworkLink link) {
		if(this == link.netA)
			addLink(link, link.netB);
		else if(this == link.netB)
			addLink(link, link.netA);
		else
			throw new AssertionError("invalid link");
	}

	private void addLink(NetworkLink link, ShaftNetwork other) {
		if(link.netA != this && link.netB != this)
			throw new AssertionError();
		links.add(link);
		group.mergeInto(other.group);
	}

	void tick() {
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
		for(SpeedTorqueCurve stc : machineCurves)
			sumtorque += stc.getTorqueAtSpeed(angvel) / inertia;
		
		//System.out.println("angvel "+angvel+", sumtorque "+sumtorque+", inertia "+inertia);
		
		group.groupAngVel += sumtorque;
	}

	ShaftNetwork createSplitNetwork() {
		ShaftNetwork n = new ShaftNetwork();
		
		n.angle = angle;
		n.angvel = angvel;
		//n.group.groupAngVel = group.groupAngVel;
		//n.relativeVelocity = relativeVelocity;
		n.group.needVelocityRecalc = true;
		//System.out.println("createSplitNetwork "+this+" -> "+n);
		return n;
	}

	void propagateNewGroup() {
		//System.out.println("propagateNewGroup "+this);
		NetworkGroup ng = new NetworkGroup();
		ng.groupAngVel = group.groupAngVel;
		ng.needVelocityRecalc = true;
		ng.noValidVelocities = group.noValidVelocities;
		propagateGroup(ng);
	}
	
	private void propagateGroup(NetworkGroup g) {
		if(group == g)
			return;
		
		group = g;
		g.add(this);
		
		for(NetworkLink l : links) {
			l.netA.propagateGroup(g);
			l.netB.propagateGroup(g);
		}
	}
	
	@Override
	public String toString() {
		return Integer.toHexString(hashCode())+", group="+Integer.toHexString(group.hashCode())+", rv="+relativeVelocity+", angvel="+ShaftUtils.toDegreesPerSecond((int)angvel)
				+", inertia="+(group.calcInertia() * (relativeVelocity * relativeVelocity));
	}

	void removeLink(NetworkLink link) {
		links.remove(link);
	}

	double calcNetworkInertia() {
		return nodes.size();
	}

	public void forceAngVel(long angvel) {
		group.groupAngVel = (long)(angvel / relativeVelocity);
	}
}
