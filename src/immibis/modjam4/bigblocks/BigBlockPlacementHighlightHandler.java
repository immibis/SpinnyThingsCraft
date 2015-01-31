package immibis.modjam4.bigblocks;

import immibis.modjam4.BlockWatermill;
import immibis.modjam4.BlockWindmill;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import static org.lwjgl.opengl.GL11.*;

/**
 * Draws previews for large objects (watermills, windmills, etc).
 * 
 * If the object can be placed, it is drawn mostly in green (with yellow for any non-collision parts that overlap placed blocks). 
 * If the object can't be placed, it is drawn mostly in blue (with red for any parts that collide with placed blocks). 
 */
public class BigBlockPlacementHighlightHandler {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void handle(DrawBlockHighlightEvent evt) {
		if(evt.target.typeOfHit != MovingObjectType.BLOCK)
			return;
		if(evt.currentItem != null && evt.currentItem.getItem() instanceof ItemBlock) {
			Block block = Block.getBlockFromItem(evt.currentItem.getItem());
			
			if(block instanceof BlockWatermill || block instanceof BlockWindmill) {
				
				ForgeDirection side = ForgeDirection.getOrientation(evt.target.sideHit);
				int cx = evt.target.blockX, cy = evt.target.blockY, cz = evt.target.blockZ;
				cx += side.offsetX; cy += side.offsetY; cz += side.offsetZ;
				
				Entity rve = Minecraft.getMinecraft().renderViewEntity;
				if(rve == null)
					return; // shouldn't happen?
				
				double rvx = rve.lastTickPosX + (rve.posX - rve.lastTickPosX) * evt.partialTicks;
				double rvy = rve.lastTickPosY + (rve.posY - rve.lastTickPosY) * evt.partialTicks;
				double rvz = rve.lastTickPosZ + (rve.posZ - rve.lastTickPosZ) * evt.partialTicks;
				
				World w = Minecraft.getMinecraft().theWorld;
				if(w == null)
					return; // also shouldn't happen
				
				glDisable(GL_LIGHTING);
				glDisable(GL_TEXTURE_2D);
				glEnable(GL_BLEND);
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				glDepthMask(false);
				glDisable(GL_DEPTH_TEST);
				
				glPushMatrix();
				glTranslated(cx - rvx, cy - rvy, cz - rvz);
				glBegin(GL_QUADS);
				
				boolean canPlace = block.canPlaceBlockOnSide(w, cx, cy, cz, side.ordinal());
				
				for(int da = -2; da <= 2; da++)
					for(int db = -2; db <= 2; db++) {
						
						int dx = 0, dy = 0, dz = 0;
						
						switch(side.ordinal()) {
						case 4: case 5: // touching X side; YZ plane
							dy = da; dz = db;
							break;
						case 2: case 3: // touching Z side; XY plane
							dx = da; dy = db;
							break;
						case 0: case 1: // touching Y side; XZ plane
							dx = da; dz = db;
							break;
						}
						
						boolean isBlockOccupied = !w.isAirBlock(cx+dx, cy+dy, cz+dz);
						
						//boolean isNoncollidingBlock = false;
						//if(block instanceof BlockWatermill && (da == -2 || da == 2 || db == -2 || db == 2))
							//isNoncollidingBlock = true;
						
						if(isBlockOccupied)
							if(canPlace) //if(isNoncollidingBlock)
								glColor4f(1, 1, 0, 0.5f); // yellow
							else
								glColor4f(1, 0, 0, 0.5f); // red
						else
							if(canPlace)
								glColor4f(0, 1, 0, 0.5f); // green
							else
								glColor4f(0, 0, 1, 0.5f); // blue
						
						drawPreviewBlock(dx, dy, dz);
					}
				
				
				glEnd();
				glPopMatrix();
				
				glEnable(GL_LIGHTING);
				glEnable(GL_TEXTURE_2D);
				glDisable(GL_BLEND);
				glDepthMask(true);
				glEnable(GL_DEPTH_TEST);
				
				evt.setCanceled(true);
			}
		}
	}
	
	private void drawPreviewBlock(int x, int y, int z) {
		
		glVertex3i(x, y, z);
		glVertex3i(x, y+1, z);
		glVertex3i(x+1, y+1, z);
		glVertex3i(x+1, y, z);
		
		glVertex3i(x, y, z+1);
		glVertex3i(x+1, y, z+1);
		glVertex3i(x+1, y+1, z+1);
		glVertex3i(x, y+1, z+1);
		
		glVertex3i(x, y, z);
		glVertex3i(x, y, z+1);
		glVertex3i(x, y+1, z+1);
		glVertex3i(x, y+1, z);
		
		glVertex3i(x+1, y, z);
		glVertex3i(x+1, y+1, z);
		glVertex3i(x+1, y+1, z+1);
		glVertex3i(x+1, y, z+1);
		
		glVertex3i(x, y, z);
		glVertex3i(x+1, y, z);
		glVertex3i(x+1, y, z+1);
		glVertex3i(x, y, z+1);
		
		glVertex3i(x, y+1, z);
		glVertex3i(x, y+1, z+1);
		glVertex3i(x+1, y+1, z+1);
		glVertex3i(x+1, y+1, z);
		
	}
}
