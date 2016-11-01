package com.builtbroken.icbm.content.rail.powered;

import com.builtbroken.mc.lib.helper.BlockUtility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/31/2016.
 */
public class ItemBlockPowerRail extends ItemBlock
{
    public ItemBlockPowerRail(Block block)
    {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
        if (world.setBlock(x, y, z, field_150939_a, side, 3))
        {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TilePowerRail)
            {
                if (side == 0 || side == 1)
                {
                    ((TilePowerRail) tile).setFacingDirection(BlockUtility.determineForgeDirection(player));
                }
                //North South
                else if (side == 2 || side == 3)
                {
                    boolean high = hitY >= 0.7;
                    boolean low = hitY <= 0.3;
                    //Left & right are inverted for South
                    boolean left = hitX <= 0.3;
                    boolean right = hitX >= 0.7;

                    if (!left && !right)
                    {
                        if (high)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.UP);
                        }
                        else if (low)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.DOWN);
                        }
                    }
                    else
                    {
                        if (left)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.WEST);
                            player.addChatComponentMessage(new ChatComponentText("West"));
                        }
                        else if (right)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.EAST);
                            player.addChatComponentMessage(new ChatComponentText("East"));
                        }
                    }
                }
                //West East
                else if (side == 4 || side == 5)
                {
                    boolean high = hitY >= 0.7;
                    boolean low = hitY <= 0.3;
                    //Left & right are inverted for East
                    boolean left = hitZ <= 0.3;
                    boolean right = hitZ >= 0.7;

                    if (!left && !right)
                    {
                        if (high)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.UP);
                        }
                        else if (low)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.DOWN);
                        }
                    }
                    else
                    {
                        if (left)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.NORTH);
                        }
                        else if (right)
                        {
                            ((TilePowerRail) tile).setFacingDirection(ForgeDirection.SOUTH);
                        }
                    }
                }
                ((TilePowerRail) tile).type = metadata;
            }
            return true;
        }
        return false;
    }
}
