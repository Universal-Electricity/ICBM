package icbm.zhapin.render.tile;

import icbm.core.ZhuYaoICBM;
import icbm.zhapin.jiqi.TLeiDaTai;
import icbm.zhapin.muoxing.jiqi.MLeiDa;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RLeiDaTai extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ZhuYaoICBM.DOMAIN, ZhuYaoICBM.MODEL_PATH + "radar.png");

	public static final MLeiDa MODEL = new MLeiDa();

	public void renderAModelAt(TLeiDaTai tileEntity, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		this.func_110628_a(TEXTURE_FILE);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		MODEL.render(tileEntity.xuanZhuan, 0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
	{
		renderAModelAt((TLeiDaTai) tileentity, d, d1, d2, f);
	}
}