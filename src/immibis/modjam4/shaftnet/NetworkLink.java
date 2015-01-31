package immibis.modjam4.shaftnet;

public class NetworkLink {
	ShaftNetwork netA;
	ShaftNetwork netB;
	public final double velocityMultiplier; // netA.angvel * velocityMultiplier = netB.angvel. Must never be zero.
	
	private boolean deleted = false;
	public boolean isDeleted() {return deleted;}
	void markDeleted() {deleted = true;}
	
	NetworkLink(ShaftNetwork a, ShaftNetwork b, double m) {
		netA = a;
		netB = b;
		velocityMultiplier = m;
	}

	public void unlink() {
		if(isDeleted()) {
			new Throwable("warning: tried to delete link that is already deleted").printStackTrace();
			return;
		}
		
		if(!netA.links.remove(this) || !netB.links.remove(this))
			throw new AssertionError("Link wasn't referenced by network?");
		
		if(netA.group != netB.group)
			throw new AssertionError("Nets were previously linked but in different groups??");
		
		if(!ShaftPhysicsUniverse.findPathBetweenNetworks(netA, netB)) {
			NetworkGroup newNetAGroup = netA.getUniverse().createGroup();
			netA.propagateGroup(newNetAGroup);
			if(netA.group != newNetAGroup)
				throw new AssertionError();
		}
		
		markDeleted();
	}
	
	@Override
	public String toString() {
		if(isDeleted())
			return "<deleted link>";
		return "Link("+netA+" * "+velocityMultiplier+" = "+netB+")";
	}

	/** Checks some invariants. */
	public void validate() {
		if(netA == null || netB == null)
			throw new AssertionError("link contains null net reference");
		if(netA.isDeleted() || netB.isDeleted())
			throw new AssertionError("link contains deleted net reference");
		if(!netA.links.contains(this) || !netB.links.contains(this))
			throw new AssertionError("net does not refer back to link");
		if(velocityMultiplier == 0)
			throw new AssertionError("link has 0 velocity multiplier");
	}

	public ShaftNetwork getOther(ShaftNetwork n) {
		if(n == netA) return netB;
		if(n == netB) return netA;
		throw new AssertionError("not passed either network (passed "+n+", link is "+this+")");
	}
}
