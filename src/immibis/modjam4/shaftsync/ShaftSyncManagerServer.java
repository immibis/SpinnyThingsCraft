package immibis.modjam4.shaftsync;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import immibis.modjam4.Modjam4Mod;
import immibis.modjam4.shaftnet.ShaftNetwork;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.network.NetHandlerPlayServer;

public class ShaftSyncManagerServer {
	public static ShaftSyncManagerServer instance;
	
	public static void init() {
		instance = new ShaftSyncManagerServer();
		FMLCommonHandler.instance().bus().register(instance);
	}
	
	private static class Subscription implements Comparable<Subscription> {
		ShaftNetwork network;
		NetHandlerPlayServer handler;
		
		private static AtomicLong nextUniqueID = new AtomicLong();
		long uniqueID = nextUniqueID.getAndIncrement();
		
		@Override
		public int hashCode() {
			return handler.hashCode() + network.netID;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Subscription))
				return false;
			Subscription o = (Subscription)obj;
			return network == o.network && handler == o.handler;
		}

		@Override
		public int compareTo(Subscription o) {
			int i;
			i = Integer.compare(network.netID, o.network.netID);
			if(i != 0) return i;
			i = Integer.compare(handler.hashCode(), o.handler.hashCode());
			if(i != 0) return i;
			return Long.compare(uniqueID, o.uniqueID);
		}
	}
	
	@SubscribeEvent
	/** Must be public or FML crashes. */
	public void onTick(TickEvent.ServerTickEvent evt) {
		if(evt.phase != TickEvent.Phase.END)
			return;
		
		//currentTick++;
		ticksUntilNextSendStart--;
		
		if(send_iterator != null) {
			int numSubscriptionsPerTick = (numSubscriptions + TARGET_FULL_SEND_TIME_TICKS - 1) / TARGET_FULL_SEND_TIME_TICKS;
			for(int k = 0; k < numSubscriptionsPerTick; k++) {
				if(!send_iterator.hasNext()) {
					send_iterator = null;
					break;
				}
				sendSubscription(send_iterator.next());
			}
			
		} else if(ticksUntilNextSendStart <= 0) {
			ticksUntilNextSendStart = TARGET_FULL_SEND_TIME_TICKS;
			send_iterator = subscriptions.iterator();
		}
	}
	
	private void sendSubscription(Subscription s) {
		if(s.network.isDeleted() || !s.handler.netManager.isChannelOpen()) {
			send_iterator.remove();
			return;
		}
		
		PacketShaftNetworkUpdate p = new PacketShaftNetworkUpdate();
		p.angvel = s.network.angvel;
		p.netID = s.network.netID;
		NetworkPacketHandler.instance.sendTo(p, s.handler.playerEntity);
	}

	private final static int TARGET_FULL_SEND_TIME_TICKS = 20; // one update per second
	//private int currentTick = 0;
	private int ticksUntilNextSendStart = 0;
	
	private int numSubscriptions = 0;
	private ConcurrentSkipListSet<Subscription> subscriptions = new ConcurrentSkipListSet<>();
	private Iterator<Subscription> send_iterator = null;

	void onNetworkSubscribePacket(int id, boolean status, NetHandlerPlayServer handler) {
		if(Modjam4Mod.universe == null)
			return; // shouldn't happen
		
		System.out.println((status ? "Subscribed" : "Unsubscribed")+" to NET"+id);
		
		ShaftNetwork net = Modjam4Mod.universe.getNetworkByID(id);
		if(net == null)
			return;
		
		Subscription subscription = new Subscription();
		subscription.network = net;
		subscription.handler = handler;
		if(status) {
			if(subscriptions.add(subscription))
				numSubscriptions++;
		} else {
			if(subscriptions.remove(subscription))
				numSubscriptions--;
		}
	}

}
