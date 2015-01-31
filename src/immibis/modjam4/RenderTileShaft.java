package immibis.modjam4;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderTileShaft extends TileEntitySpecialRenderer  {
	
	public void renderAttachment() {}
	public void renderStatic() {}

	@Override
	public void renderTileEntityAt(TileEntity te_, double renderX, double renderY, double renderZ, float partialTick) {
		int meta = te_.getBlockMetadata();
		TileShaft te = (TileShaft)te_;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		Tessellator t = Tessellator.instance;
		
		RenderHelper.disableStandardItemLighting();
		
		int angle_r = te.shaftNode.getNetwork().angle;
		long angvel = te.shaftNode.getNetwork().angvel;
		float angle = (float)((angle_r + angvel * partialTick) / (4294967296.0 / 360.0));
		
		GL11.glPushMatrix();
		GL11.glTranslated(renderX+0.5, renderY+0.5, renderZ+0.5);
		/*if(meta == 2) {
			// Z -> Y
			GL11.glRotatef(90, 1, 0, 0);
		}
		if(meta == 4) {
			// X -> Y
			GL11.glRotatef(90, 0, 0, 1);
		}*/
		if((meta & 6) == 2) {
			// Z -> Y
			GL11.glRotatef(90, 1, 0, 0);
		}
		if((meta & 6) == 4) {
			// X -> Y
			GL11.glRotatef(-90, 0, 0, 1);
			angle = -angle;
			angvel = -angvel;
		}
		if((meta & 1) == 1) {
			// Y <-> -Y
			GL11.glRotatef(180, 1, 0, 0);
			angle = -angle;
		}
		
		renderStatic();
		
		GL11.glRotatef(angle, 0, 1, 0);
		
		t.startDrawingQuads();
		renderAttachment();
		renderShaft(false);
		t.draw();
		
		GL11.glPopMatrix();
	}
	
	public void renderShaft(boolean useNormal) {
		Tessellator t = Tessellator.instance;
		
		IIcon icon = Blocks.log.getIcon(2, 0);
		if(useNormal) t.setNormal(-1, 0, 0);
		t.addVertexWithUV(-0.25, -0.51,-0.25, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV(-0.25, -0.51, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-0.25,  0.51, 0.25, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-0.25,  0.51,-0.25, icon.getMinU(), icon.getMaxV());
		
		if(useNormal) t.setNormal(1, 0, 0);
		t.addVertexWithUV( 0.25,  0.51,-0.25, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( 0.25,  0.51, 0.25, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( 0.25, -0.51, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( 0.25, -0.51,-0.25, icon.getMinU(), icon.getMinV());
		
		if(useNormal) t.setNormal(0, 0, -1);
		t.addVertexWithUV(-0.25,  0.51,-0.25, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( 0.25,  0.51,-0.25, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( 0.25, -0.51,-0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-0.25, -0.51,-0.25, icon.getMinU(), icon.getMinV());
		
		if(useNormal) t.setNormal(0, 0, 1);
		t.addVertexWithUV(-0.25, -0.51, 0.25, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV( 0.25, -0.51, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( 0.25,  0.51, 0.25, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-0.25,  0.51, 0.25, icon.getMinU(), icon.getMaxV());
		
		icon = Blocks.log.getIcon(0, 0);
		if(useNormal) t.setNormal(0, -1, 0);
		t.addVertexWithUV( 0.25, -0.51,-0.25, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( 0.25, -0.51, 0.25, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-0.25, -0.51, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-0.25, -0.51,-0.25, icon.getMinU(), icon.getMinV());
		
		if(useNormal) t.setNormal(0, 1, 0);
		t.addVertexWithUV(-0.25,  0.51,-0.25, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV(-0.25,  0.51, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( 0.25,  0.51, 0.25, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( 0.25,  0.51,-0.25, icon.getMinU(), icon.getMaxV());
	}

}
