package immibis.modjam4.shaftsync;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Must be public or FML crashes. */
public class PacketUnsubscribeS2C implements IMessage {

	public int id;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
	}

}
