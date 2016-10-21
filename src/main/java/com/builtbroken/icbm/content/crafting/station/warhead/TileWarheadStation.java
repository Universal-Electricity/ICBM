package com.builtbroken.icbm.content.crafting.station.warhead;

import com.builtbroken.icbm.ICBM;
import com.builtbroken.icbm.api.warhead.IWarheadItem;
import com.builtbroken.icbm.api.modules.IWarhead;
import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.lib.world.explosive.ExplosiveRegistry;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.TileModuleMachine;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Handles crafting and configuration of warheads
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/6/2016.
 */
public class TileWarheadStation extends TileModuleMachine implements IPacketIDReceiver, IGuiTile
{
    public static final int WARHEAD_SLOT = 0;
    public static final int EXPLOSIVE_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int TRIGGER_SLOT = 3;

    //TODO add tabs, crafting, configuration, reverse crafting
    //TODO add option to limit number of inserted explosives per craft (1-64 scale, all)

    public TileWarheadStation()
    {
        super("warheadStation", Material.iron);
        this.resistance = 10f;
        this.hardness = 10f;
        this.renderTileEntity = true;
        this.renderNormalBlock = false;
    }

    @Override
    public Tile newTile()
    {
        return new TileWarheadStation();
    }

    /** Called to process a crafting request */
    public void doCrafting()
    {
        final ItemStack warheadStack = getWarheadStack();
        final ItemStack explosiveStack = getExplosiveStack();
        final ItemStack outputStack = getOutputStack();

        if (warheadStack != null && warheadStack.getItem() instanceof IWarheadItem && ExplosiveRegistry.get(explosiveStack) != null && (outputStack == null || outputStack.stackSize < outputStack.getMaxStackSize()))
        {
            final IWarhead warhead = ((IWarheadItem) warheadStack.getItem()).getModule(warheadStack);
            if (warhead != null && warhead.hasSpaceForExplosives(explosiveStack))
            {
                int insert = Math.min(explosiveStack.stackSize, warhead.getSpaceForExplosives());
                //Update warhead's explosive stack
                if (warhead.getExplosiveStack() == null)
                {
                    ItemStack insertStack = explosiveStack.copy();
                    insertStack.stackSize = insert;
                    warhead.setExplosiveStack(insertStack);
                }
                else
                {
                    //Increase explosive stack
                    warhead.getExplosiveStack().stackSize += insert;
                    //Trigger any events for warhead change
                    warhead.setExplosiveStack(warhead.getExplosiveStack());
                }
                final ItemStack newWarhead = warhead.toStack();

                if (outputStack == null || InventoryUtility.stacksMatch(newWarhead, outputStack))
                {
                    //Decrease explosive stack
                    explosiveStack.stackSize -= insert;
                    if (explosiveStack.stackSize <= 0)
                    {
                        getInventory().setInventorySlotContents(EXPLOSIVE_SLOT, null);
                    }
                    else
                    {
                        //Update inventory
                        getInventory().setInventorySlotContents(EXPLOSIVE_SLOT, explosiveStack);
                    }

                    //Decrease warhead stack (INPUT)
                    warheadStack.stackSize--;
                    if (warheadStack.stackSize <= 0)
                    {
                        getInventory().setInventorySlotContents(WARHEAD_SLOT, null);
                    }
                    else
                    {
                        //Update inventory
                        getInventory().setInventorySlotContents(WARHEAD_SLOT, warheadStack);
                    }

                    //Increase output
                    if (getOutputStack() == null)
                    {
                        getInventory().setInventorySlotContents(OUTPUT_SLOT, newWarhead);
                    }
                    else
                    {
                        outputStack.stackSize += 1;
                        getInventory().setInventorySlotContents(OUTPUT_SLOT, outputStack);
                    }
                }
            }
        }
    }

    /**
     * Checks if it is possible to process a crafting recipe
     *
     * @return true if possible
     */
    public boolean canCraft()
    {
        return getOutputStack() == null || InventoryUtility.stacksMatch(getCraftResult(), getOutputStack());
    }

    /**
     * Gets the expected output of a crafting recipe
     *
     * @return ItemStack, or null if not possible to craft
     */
    public ItemStack getCraftResult()
    {
        final ItemStack warheadStack = getWarheadStack();
        final ItemStack explosiveStack = getExplosiveStack();
        final ItemStack outputStack = getOutputStack();

        if (warheadStack != null && warheadStack.getItem() instanceof IWarheadItem && ExplosiveRegistry.get(explosiveStack) != null && (outputStack == null || outputStack.stackSize < outputStack.getMaxStackSize()))
        {
            final IWarhead warhead = ((IWarheadItem) warheadStack.getItem()).getModule(warheadStack);
            if (warhead != null && warhead.hasSpaceForExplosives(explosiveStack))
            {
                int insert = Math.min(explosiveStack.stackSize, warhead.getSpaceForExplosives());
                //Update warhead's explosive stack
                if (warhead.getExplosiveStack() == null)
                {
                    ItemStack insertStack = explosiveStack.copy();
                    insertStack.stackSize = insert;
                    warhead.setExplosiveStack(insertStack);
                }
                else
                {
                    //Increase explosive stack
                    warhead.getExplosiveStack().stackSize += insert;
                    //Trigger any events for warhead change
                    warhead.setExplosiveStack(warhead.getExplosiveStack());
                }
                return warhead.toStack();
            }
        }
        return null;
    }

    @Override
    protected boolean onPlayerRightClick(EntityPlayer player, int side, Pos hit)
    {
        if (isServer())
        {
            openGui(player, ICBM.INSTANCE);
        }
        return true;
    }


    @Override
    public TileModuleInventory getInventory()
    {
        if (super.getInventory() == null)
        {
            //lazy init of inventory
            addInventoryModule(4);
        }
        return (TileModuleInventory) super.getInventory();
    }

    protected ItemStack getWarheadStack()
    {
        return getInventory().getStackInSlot(WARHEAD_SLOT);
    }

    protected ItemStack getExplosiveStack()
    {
        return getInventory().getStackInSlot(EXPLOSIVE_SLOT);
    }

    protected ItemStack getOutputStack()
    {
        return getInventory().getStackInSlot(OUTPUT_SLOT);
    }

    protected ItemStack getTriggerStack()
    {
        return getInventory().getStackInSlot(TRIGGER_SLOT);
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        super.doUpdateGuiUsers();
        if (!super.read(buf, id, player, type))
        {
            if (isServer())
            {
                if (id == 1)
                {
                    doCrafting();
                    return true;
                }
            }
            else
            {

            }
            return false;
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
    }

    @Override
    public void doUpdateGuiUsers()
    {
        if (ticks % 5 == 0)
        {
            //PacketTile packet = new PacketTile(this, 5, );
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerWarheadStation(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return null;
    }
}
