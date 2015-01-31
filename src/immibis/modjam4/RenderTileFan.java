package immibis.modjam4;

import immibis.modjam4.shaftsync.ClientShaftNetwork;
import immibis.modjam4.shaftsync.ShaftSyncManagerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public class RenderTileFan extends TileEntitySpecialRenderer {
	public void renderAttachment() {}
	public void renderStatic() {}

	@Override
	public void renderTileEntityAt(TileEntity te_, double renderX, double renderY, double renderZ, float partialTick) {
		int meta = te_.getBlockMetadata();
		TileFan te = (TileFan)te_;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		Tessellator t = Tessellator.instance;
		
		RenderHelper.disableStandardItemLighting();
		
		ClientShaftNetwork csn = ShaftSyncManagerClient.instance.getClientNetwork(te.clientNetID);
		long angvel = csn.angvel;
		float angle = (float)((csn.angle + angvel * partialTick) / (4294967296.0 / 360.0));
		
		GL11.glPushMatrix();
		GL11.glTranslated(renderX+0.5, renderY+0.5, renderZ+0.5);
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
		renderShaft(false, ((meta & 1) == 0) ^ /*((meta & 6) == 2) ^*/ (angvel < 0));
		t.draw();
		
		GL11.glPopMatrix();
	}
	
	public void renderShaft(boolean useNormal, boolean flipFanDir) {
		Tessellator t = Tessellator.instance;
		
		double MAXY = 2/16f;
		final double A = 2/16f;
		
		IIcon icon = Blocks.log.getIcon(2, 0);
		if(useNormal) t.setNormal(-1, 0, 0);
		t.addVertexWithUV(-0.25, -0.5,-0.25, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV(-0.25, -0.5, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-A   , MAXY, A   , icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-A   , MAXY,-A   , icon.getMinU(), icon.getMaxV());
		
		if(useNormal) t.setNormal(1, 0, 0);
		t.addVertexWithUV( A   , MAXY,-A   , icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( A   , MAXY, A   , icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( 0.25, -0.5, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( 0.25, -0.5,-0.25, icon.getMinU(), icon.getMinV());
		
		if(useNormal) t.setNormal(0, 0, -1);
		t.addVertexWithUV(-A   , MAXY,-A   , icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( A   , MAXY,-A   , icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( 0.25, -0.5,-0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-0.25, -0.5,-0.25, icon.getMinU(), icon.getMinV());
		
		if(useNormal) t.setNormal(0, 0, 1);
		t.addVertexWithUV(-0.25, -0.5, 0.25, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV( 0.25, -0.5, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( A   , MAXY, A   , icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-A   , MAXY, A   , icon.getMinU(), icon.getMaxV());
		
		//icon = Blocks.log.getIcon(0, 0);
		if(useNormal) t.setNormal(0, -1, 0);
		t.addVertexWithUV( 0.25, -0.5,-0.25, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( 0.25, -0.5, 0.25, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-0.25, -0.5, 0.25, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-0.25, -0.5,-0.25, icon.getMinU(), icon.getMinV());
		
		
		
		
		if(useNormal) t.setNormal(0, 1, 0);
		t.addVertexWithUV(-A, MAXY,-A, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV(-A, MAXY, A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( A, MAXY, A, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( A, MAXY,-A, icon.getMinU(), icon.getMaxV());
		
		icon = Blocks.iron_block.getIcon(0, 0);
		
		
		final double B = 0.5;
		double BACK = MAXY;
		
		if(flipFanDir)
			MAXY -= 2/16f;
		else
			BACK -= 2/16f;
		
		t.addVertexWithUV(-A, MAXY, -A, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV( A, BACK, -A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( A, BACK, -B, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-A, MAXY, -B, icon.getMinU(), icon.getMaxV());
		
		t.addVertexWithUV(-A, BACK, -A, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV(-B, BACK, -A, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-B, MAXY,  A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-A, MAXY,  A, icon.getMinU(), icon.getMinV());
	
		t.addVertexWithUV( A, MAXY,  A, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV(-A, BACK,  A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-A, BACK,  B, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( A, MAXY,  B, icon.getMinU(), icon.getMaxV());
		
		t.addVertexWithUV( A, BACK,  A, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( B, BACK,  A, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( B, MAXY, -A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( A, MAXY, -A, icon.getMinU(), icon.getMinV());
		
		
		t.addVertexWithUV(-A, MAXY, -B, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV( A, BACK, -B, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( A, BACK, -A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-A, MAXY, -A, icon.getMinU(), icon.getMinV());
		
		t.addVertexWithUV(-A, MAXY,  A, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV(-B, MAXY,  A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV(-B, BACK, -A, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-A, BACK, -A, icon.getMinU(), icon.getMaxV());
		
		t.addVertexWithUV( A, MAXY,  B, icon.getMinU(), icon.getMaxV());
		t.addVertexWithUV(-A, BACK,  B, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV(-A, BACK,  A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( A, MAXY,  A, icon.getMinU(), icon.getMinV());
		
		t.addVertexWithUV( A, MAXY, -A, icon.getMinU(), icon.getMinV());
		t.addVertexWithUV( B, MAXY, -A, icon.getMaxU(), icon.getMinV());
		t.addVertexWithUV( B, BACK,  A, icon.getMaxU(), icon.getMaxV());
		t.addVertexWithUV( A, BACK,  A, icon.getMinU(), icon.getMaxV());
		
	}
}
