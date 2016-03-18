package com.builtbroken.icbm.content.ams;

import com.builtbroken.icbm.client.Assets;
import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.GroupObject;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/5/2016.
 */
public class TileAMSClient extends TileAMS implements ISimpleItemRenderer
{
    private static GroupObject turret;
    private static GroupObject base;

    @Override
    public Tile newTile()
    {
        return new TileAMSClient();
    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        if (turret == null || base == null)
        {
            for (GroupObject object : Assets.AMS_MODEL.groupObjects)
            {
                if (object.name.equals("TurretBase_Cylinder"))
                {
                    base = object;
                }
                else if (object.name.equals("Gun_Cube.002"))
                {
                    turret = object;
                }
            }
        }

        GL11.glTranslatef(0.5f, 0, 0.5f);

        GL11.glRotatef(90, 0, 1, 0);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(Assets.AMS_TEXTURE);
        base.render();

        GL11.glTranslatef(0, 0.5f, 0);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(Assets.AMS_TOP_TEXTURE);
        turret.render();
    }

    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        //Lerp rotation values so not to have snapping effects(looks bad)
        if (!currentAim.isWithin(aim, 1))
        {
            currentAim.lerp(aim, (System.nanoTime() - lastRotationUpdate) / ROTATION_TIME);
            currentAim.clampTo360();
        }
        lastRotationUpdate = System.nanoTime();

        //Get render pieces if missing
        if (turret == null || base == null)
        {
            for (GroupObject object : Assets.AMS_MODEL.groupObjects)
            {
                if (object.name.equals("TurretBase_Cylinder"))
                {
                    base = object;
                }
                else if (object.name.equals("Gun_Cube.002"))
                {
                    turret = object;
                }
            }
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.xf() + 0.5f, pos.yf(), pos.zf() + 0.5f);

        GL11.glRotatef((float) ((currentAim.yaw() + 90f) % 360), 0, 1, 0);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(Assets.AMS_TEXTURE);
        base.render();

        GL11.glTranslatef(0, 0.5f, 0);
        GL11.glRotatef((float) currentAim.pitch(), 0, 0, 1);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(Assets.AMS_TOP_TEXTURE);
        turret.render();

        GL11.glPopMatrix();
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        if (!super.read(buf, id, player, type))
        {
            if (id == 2)
            {
                fireWeaponEffect();
                return true;
            }
            else if (id == 3)
            {
                aim.readByteBuf(buf);
                return true;
            }
            return false;
        }
        return true;
    }

    protected void fireWeaponEffect()
    {
        //Pos aimVector = aim.toVector();
        //Pos pos = new Pos(x(), y(), z()).add(0.5).add(aimVector);
        //RocketFx fx = new RocketFx(world(), pos.x(), pos.y(), pos.z(), aimVector.x() * 0.2, aimVector.y() * 0.2, aimVector.z() * 0.2);
        //Minecraft.getMinecraft().effectRenderer.addEffect(fx);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        aim.readByteBuf(buf);
        currentAim.readByteBuf(buf);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiAMSTurret(player, this);
    }
}