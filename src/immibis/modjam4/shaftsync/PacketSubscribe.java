package immibis.modjam4.shaftsync;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Must be public or FML crashes. */
public class PacketSubscribe implements IMessage {

	public int id;
	public boolean status;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		status = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeBoolean(status);
	}

}
