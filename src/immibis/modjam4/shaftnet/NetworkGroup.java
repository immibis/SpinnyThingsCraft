package immibis.modjam4.shaftnet;

import immibis.modjam4.shaftnet.MatrixMath.SingularMatrixException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkGroup {
	
	private boolean deleted = false;
	void markDeleted() {deleted = true;}
	boolean isDeleted() {return deleted;}
	
	final ShaftPhysicsUniverse universe;
	NetworkGroup(ShaftPhysicsUniverse universe) {
		this.universe = universe;
	}
	
	
	List<ShaftNetwork> networks = new ArrayList<ShaftNetwork>();
	boolean needVelocityRecalc = false;
	
	/**
	 * Set if the network must be stationary (all velocities 0)
	 * This happens for example if you connect both sides of a non-1:1 gearbox together.
	 */
	boolean noValidVelocities = false;
	
	long groupAngVel;
	
	void recalcVelocity() {
		if(isDeleted()) throw new AssertionError("group was deleted");
		
		List<NetworkLink> links = new ArrayList<NetworkLink>();
		for(ShaftNetwork n : networks)
			for(NetworkLink l : n.links)
				if(l.netA == n)
					links.add(l);
		
		if(links.size() < networks.size() - 1) {
			System.out.println("[ImmibisMJ4 Debug/Warning] "+links.size()+" links for "+networks.size()+" networks! Shaft network group is corrupted?");
			noValidVelocities = true;
			return;
		}
		
		if(networks.size() == 1) {
			groupAngVel *= networks.get(0).relativeVelocity;
			networks.get(0).relativeVelocity = 1;
			return;
		}
		
		for(NetworkLink l : links)
			if(!networks.contains(l.netA) || !networks.contains(l.netB)) {
				System.out.println("[ImmibisMJ4 Debug/Warning] Group "+Integer.toHexString(hashCode())+" has a link to a network not in the group. Link: "+l+". Networks in group: "+networks);
				noValidVelocities = true;
				return;
			}
		
		// construct a matrix for a system of linear equations
		// one equation for each link, one variable for each network's relativeVelocity
		double[][] matrix = new double[links.size()][networks.size()]; // [row][col]
		int linkIndex = 0;
		for(NetworkLink l : links) {
			// netAspeed*vm = netBspeed
			// -> netAspeed*vm - netBspeed = 0
			matrix[linkIndex][networks.indexOf(l.netA)] = l.velocityMultiplier;
			matrix[linkIndex][networks.indexOf(l.netB)] = -1;
			linkIndex++;
		}
		
		boolean previousNVV = noValidVelocities;
		double previousLastNetworkRelativeVelocity = networks.get(networks.size()-1).relativeVelocity;
		
		try {
			MatrixMath.toReducedRowEchelonForm(matrix);
		} catch (SingularMatrixException e) {
			noValidVelocities = true;
			return;
		}
		
		for(int net = 0; net < networks.size() - 1; net++) {
			if(matrix[net][net] != 1) {
				noValidVelocities = true;
				return;
			}
		}
		
		for(int net = 0; net < networks.size() - 1; net++) {
			networks.get(net).relativeVelocity = -matrix[net][networks.size()-1];
		}
		networks.get(networks.size()-1).relativeVelocity = 1;
		noValidVelocities = false;
		
		if(previousNVV) {
			groupAngVel = 0;
		} else {
			double mult = previousLastNetworkRelativeVelocity / networks.get(networks.size()-1).relativeVelocity;
			groupAngVel *= mult;
		}
		
		for(ShaftNetwork n : networks)
			n.angvel = (long)(groupAngVel * n.relativeVelocity);
	}

	void add(ShaftNetwork net) {
		if(isDeleted()) throw new AssertionError("group was deleted");
		if(net.group != this) throw new AssertionError();
		if(networks.contains(net)) throw new AssertionError("network already added to group");
		networks.add(net);
		needVelocityRecalc = true;
	}

	void mergeInto(NetworkGroup group) {
		if(isDeleted() || group.isDeleted()) throw new AssertionError("group was deleted");
		
		if(this == group)
			return;
		
		if(group.needVelocityRecalc) {
			group.calcInertia();
		}
		
		
		for(ShaftNetwork n : networks)
			n.angvel = (long)(n.relativeVelocity * groupAngVel); 
		for(ShaftNetwork n : group.networks)
			n.angvel = (long)(n.relativeVelocity * group.groupAngVel);
		
		ShaftNetwork referenceNetwork = networks.get(0);
		
		//double referenceNetworkOldRelativeVelocity = referenceNetwork.relativeVelocity;
		
		//double thisInertia = calcInertia();
		//double otherInertia = group.calcInertia();
		//System.out.println(thisInertia+" * "+groupAngVel+" + "+otherInertia+" * "+group.groupAngVel);
		//System.out.println("/ "+(thisInertia + otherInertia)+" = "+(long)((thisInertia * groupAngVel + otherInertia * group.groupAngVel) / (thisInertia + otherInertia)));
		//group.groupAngVel = (long)((thisInertia * groupAngVel + otherInertia * group.groupAngVel) / (thisInertia + otherInertia));
		
		for(ShaftNetwork n : networks) {
			n.group = group;
			group.add(n);
		}
		
		//group.recalcVelocity();
		
		//double groupRelativeVelocity = referenceNetwork.relativeVelocity / referenceNetworkOldRelativeVelocity;
		
		// if groupRelativeVelocity is 2, then the R.V. of all networks previously in this group just doubled
		
		//groupAngVel /= groupRelativeVelocity;
		groupAngVel = (long)(referenceNetwork.angvel / referenceNetwork.relativeVelocity);
		
		double thisInertia = calcInertia();
		double otherInertia = group.calcInertia();
		//System.out.println(thisInertia+" * "+groupAngVel+" + "+otherInertia+" * "+group.groupAngVel+"   (GRV is "+groupRelativeVelocity+")");
		//System.out.println("/ "+(thisInertia + otherInertia)+" = "+(long)((thisInertia * groupAngVel + otherInertia * group.groupAngVel) / (thisInertia + otherInertia)));
		//group.groupAngVel = (long)((thisInertia * groupAngVel + otherInertia * group.groupAngVel) / (thisInertia + otherInertia));
		
		
		// this is wrong. TODO fix the above code somehow
		double thisMomentum = thisInertia * groupAngVel;
		double otherMomentum = otherInertia * group.groupAngVel;
		group.groupAngVel = (long)(((thisMomentum < 0) != (otherMomentum < 0) ? otherMomentum + thisMomentum : Math.abs(thisMomentum) > Math.abs(otherMomentum) ? thisMomentum : otherMomentum) / (thisInertia + otherInertia));
		
		networks.clear();
		getUniverse().deleteGroup(this);
	}

	public double calcInertia() {
		if(isDeleted()) throw new AssertionError("group was deleted");
		
		double inertia = 0;
		for(ShaftNetwork sn : networks)
			inertia += sn.relativeVelocity*sn.relativeVelocity*sn.calcNetworkInertia();
		return inertia;
	}

	public ShaftPhysicsUniverse getUniverse() {
		return universe;
	}
	
	/** Checks some invariants. */
	public void validate() {
		if(networks.size() == 0)
			throw new AssertionError("group with no networks");
		for(ShaftNetwork n : networks) {
			if(n.isDeleted())
				throw new AssertionError("group contains deleted network");
			if(n.group != this)
				throw new AssertionError("group contains network in different group");
		}
	}
	
	private static AtomicInteger nextGroupID = new AtomicInteger();
	public final int groupID = nextGroupID.incrementAndGet();
}
