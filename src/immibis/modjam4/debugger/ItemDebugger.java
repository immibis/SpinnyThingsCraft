package immibis.modjam4.debugger;

import immibis.modjam4.ShaftUtils;
import immibis.modjam4.TileShaft;
import immibis.modjam4.shaftnet.ShaftNetwork;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemDebugger extends Item {
	{
		setMaxStackSize(1);
		setTextureName("spinnycraft:debugger");
		setCreativeTab(CreativeTabs.tabAllSearch);
		setUnlocalizedName("immibis.spinnycraft.debugger");
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
			return true;
		
		TileEntity te = world.getTileEntity(x, y, z);
		
		if(te instanceof TileShaft) {
			TileShaft ts = (TileShaft)te;
			
			debugNetwork(ts.getShaftNode().getNetwork(), player);
		}
		
		return true;
	}

	private void debugNetwork(ShaftNetwork network, EntityPlayer player) {
		String message = "NET"+network.netID+": ";
		
		message += ShaftUtils.toDegreesPerSecond(network.angvel)+" deg/s";
		message += ", rv="+network.getRelativeVelocity();
		
		if(network.getGroup() != null) { // should be always
			
			message += ", part of G"+network.getGroup().groupID;
			message += ", apparent inertia="+network.getApparentInertia();
		
		} else {
			message += ", NO GROUP!";
		}
		
		player.addChatMessage(new ChatComponentText(message));
	}
}
