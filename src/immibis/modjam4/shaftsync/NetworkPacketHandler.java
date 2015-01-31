package immibis.modjam4.shaftsync;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.network.NetHandlerPlayServer;

public class NetworkPacketHandler extends SimpleNetworkWrapper {
	public static NetworkPacketHandler instance;
	public static void init() {instance = new NetworkPacketHandler();}
	private NetworkPacketHandler() {
		super("SpinnyCraft");
		
		registerMessage(new IMessageHandler<PacketSubscribe, IMessage>() {
			@Override
			public IMessage onMessage(PacketSubscribe message, MessageContext ctx) {
				if(ctx.netHandler instanceof NetHandlerPlayServer) {
					NetHandlerPlayServer handler = ((NetHandlerPlayServer)ctx.netHandler);
					ShaftSyncManagerServer.instance.onNetworkSubscribePacket(message.id, message.status, handler);
				}
				return null;
			}
		}, PacketSubscribe.class, 0, Side.SERVER);
		
		registerMessage(new IMessageHandler<PacketUnsubscribeS2C, IMessage>() {
			@Override
			public IMessage onMessage(PacketUnsubscribeS2C message, MessageContext ctx) {
				ShaftSyncManagerClient.instance.onServerForcesUnsubscribe(message.id);
				return null;
			}
		}, PacketUnsubscribeS2C.class, 1, Side.CLIENT);
		
		registerMessage(new IMessageHandler<PacketShaftNetworkUpdate, IMessage>() {
			@Override
			public IMessage onMessage(PacketShaftNetworkUpdate message, MessageContext ctx) {
				ShaftSyncManagerClient.instance.onUpdateFromServer(message);
				return null;
			}
		}, PacketShaftNetworkUpdate.class, 2, Side.CLIENT);
	}
}
