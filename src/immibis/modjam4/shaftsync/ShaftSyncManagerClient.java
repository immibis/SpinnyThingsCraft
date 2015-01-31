package immibis.modjam4.shaftsync;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.CLIENT)
public class ShaftSyncManagerClient {
	public static final ShaftSyncManagerClient instance = new ShaftSyncManagerClient();
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}
	
	private int currentTick = 0; // not aligned with world ticks; just a number that increments
	
	@SubscribeEvent
	/** Must be public or FML crashes. */
	public void onTick(TickEvent.ClientTickEvent evt) {
		if(evt.phase != TickEvent.Phase.END)
			return;
		
		currentTick++;
		
		Iterator<ClientShaftNetwork> csn_it = subscribedNets.values().iterator();
		while(csn_it.hasNext()) {
			ClientShaftNetwork csn = csn_it.next();
			
			if((currentTick - csn.lastAccessTick) > 100) {
				// not used in the last 5 seconds; unsubscribe
				sendSubscribePacket(csn.netID, false);
				csn_it.remove();
			}
			
			csn.angle += (int)csn.angvel;
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	/** Must be public or FML crashes. */
	public void onDisconnect(ClientDisconnectionFromServerEvent evt) {
		subscribedNets.clear();
	}
	
	
	
	private Map<Integer, ClientShaftNetwork> subscribedNets = new HashMap<>();
	
	public ClientShaftNetwork getClientNetwork(int ID) {
		ClientShaftNetwork csn = subscribedNets.get(ID);
		if(csn != null) {
			csn.lastAccessTick = currentTick;
			return csn;
		}
		
		csn = new ClientShaftNetwork(ID);
		csn.lastAccessTick = currentTick;
		subscribedNets.put(ID, csn);
		sendSubscribePacket(ID, true);
		return csn;
	}

	private void sendSubscribePacket(int id, boolean status) {
		PacketSubscribe ps = new PacketSubscribe();
		ps.id = id;
		ps.status = status;
		NetworkPacketHandler.instance.sendToServer(ps);
	}

	void onServerForcesUnsubscribe(int id) {
		ClientShaftNetwork csn = subscribedNets.get(id);
		if(csn != null) {
			csn.isDeleted = true;
			csn.angvel = 0;
			csn.angle = 0;
		}
	}

	void onUpdateFromServer(PacketShaftNetworkUpdate message) {
		ClientShaftNetwork csn = subscribedNets.get(message.netID);
		if(csn != null) {
			csn.angvel = message.angvel;
		}
	}
	
}
