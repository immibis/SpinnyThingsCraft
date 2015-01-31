package immibis.modjam4.shaftsync;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Must be public or FML crashes. */
public class PacketShaftNetworkUpdate implements IMessage {
	public int netID;
	public long angvel;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		netID = buf.readInt();
		angvel = buf.readLong();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(netID);
		buf.writeLong(angvel);
	}
	
}
