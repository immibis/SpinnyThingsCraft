/**
 * A {@link ShaftNode} is effectively a block in the shaft physics system.
 * 
 * If a block has two shafts that cross over each other, or otherwise aren't connected,
 * it would have two ShaftNodes.
 * 
 * For example, a speed-doubling gearbox has two ShaftNodes (one for each end of the gearbox).
 * A plain gearbox (the sort that lets you change shaft direction) has one ShaftNode,
 * which is used on all 6 sides.
 * An axle has one ShaftNode, which is used on both ends.
 * 
 * ShaftNodes are combined into {@link ShaftNetwork}s, which are collections of nodes rotating
 * at the same speed (including sign).
 */
package immibis.modjam4.shaftnet;