package com.builtbroken.test.icbm.content.warhead;

import com.builtbroken.icbm.ICBM;
import com.builtbroken.icbm.api.ICBM_API;
import com.builtbroken.icbm.content.items.ItemExplosive;
import com.builtbroken.icbm.content.missile.parts.MissileModuleBuilder;
import com.builtbroken.icbm.content.missile.parts.warhead.Warhead;
import com.builtbroken.icbm.content.missile.parts.warhead.WarheadCasings;
import com.builtbroken.icbm.content.warhead.TileWarhead;
import com.builtbroken.mc.core.content.blast.tnt.ExplosiveHandlerTNT;
import com.builtbroken.mc.framework.explosive.ExplosiveRegistry;
import com.builtbroken.mc.lib.data.item.ItemStackWrapper;
import com.builtbroken.mc.testing.junit.VoltzTestRunner;
import com.builtbroken.mc.testing.tile.AbstractTileTest;
import com.builtbroken.test.icbm.content.crafting.TestExplosiveItem;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/28/2016.
 */
@RunWith(VoltzTestRunner.class)
public class TestWarhead extends AbstractTileTest<TileWarhead>
{
    public TestWarhead() throws IllegalAccessException, InstantiationException
    {
        super("TileTestWarhead", TileWarhead.class);
        ICBM.registerExplosives();
        WarheadCasings.register();
        ICBM_API.blockWarhead = this.block;
    }

    @Override
    public void setUpForEntireClass()
    {
        super.setUpForEntireClass();

        if (TestExplosiveItem.item == null)
        {
            TestExplosiveItem.item = new ItemExplosive();
            GameRegistry.registerItem(TestExplosiveItem.item, "testExplosiveItemII");
        }
        ICBM_API.itemExplosive = TestExplosiveItem.item;
        for (ItemExplosive.ExplosiveItems exItem : ItemExplosive.ExplosiveItems.values())
        {
            ExplosiveRegistry.unregisterExplosiveItem(exItem.newItem());
            if (exItem.ex_name != null && exItem != ItemExplosive.ExplosiveItems.EMP)
            {
                ExplosiveRegistry.registerExplosive("mod", exItem.ex_name, new ExplosiveHandlerTNT());
                assertTrue(ExplosiveRegistry.registerExplosiveItem(exItem.newItem()));
                List<ItemStackWrapper> list = ExplosiveRegistry.getItems(exItem.getExplosive());
                assertTrue(list.contains(new ItemStackWrapper(exItem.newItem())));
            }
        }
    }

    @Override
    public void tearDownForEntireClass()
    {
        super.tearDownForEntireClass();
        for (ItemExplosive.ExplosiveItems exItem : ItemExplosive.ExplosiveItems.values())
        {
            ExplosiveRegistry.unregisterExplosiveItem(exItem.newItem());
            ExplosiveRegistry.unregisterExplosive(exItem.ex_name);
        }
        ICBM_API.blockWarhead = null;
        ICBM_API.itemExplosive = null;
    }

    @Test
    public void testWarheadCasings()
    {
        for (WarheadCasings casing : WarheadCasings.values())
        {
            //Build empty warhead
            Warhead warhead = MissileModuleBuilder.INSTANCE.buildWarhead(casing, (ItemStack) null);

            //Test default casing creation & init values
            assertNotNull("Warhead failed to create", warhead);
            assertNotNull("Warhead failed to with stack", warhead.toStack());
            assertNotNull("Warhead was created with invalid stack item", warhead.toStack().getItem());
            assertNull("Warhead should have generated with no explosive instance", warhead.getExplosive());
            assertNull("Warhead should have generated with no explosive instance", warhead.explosive);
            assertTrue("Warhead should have generated with no NBT data",
                    warhead.getAdditionalExplosiveData() == null || warhead.getAdditionalExplosiveData().hasNoTags());

            //Test warhead creation with explosive items
            for (ItemExplosive.ExplosiveItems exItem : ItemExplosive.ExplosiveItems.values())
            {
                if (exItem.ex_name != null && exItem != ItemExplosive.ExplosiveItems.EMP)
                {
                    //Check that explosive data exists
                    assertTrue("Failed to get explosive: " + exItem.ex_name, exItem.getExplosive() != null);

                    //Build warhead
                    warhead = MissileModuleBuilder.INSTANCE.buildWarhead(casing, exItem.newItem());

                    //Test data
                    assertNotNull("Warhead failed to create, explosive: " + exItem.ex_name, warhead);
                    assertNotNull("Warhead failed to create with stack, explosive: " + exItem.ex_name, warhead.toStack());
                    assertNotNull("Warhead failed to create with stack item, explosive: " + exItem.ex_name, warhead.toStack().getItem());
                    assertEquals("Warhead failed to create with right explosive instance, explosive: " + exItem.ex_name,
                            warhead.getExplosive(), exItem.getExplosive());

                    //Test save, we do this per explosive to catch edge cases if explosive data conflicts or fails
                    NBTTagCompound tag = warhead.save(new NBTTagCompound());
                    assertNotNull("Warhead failed to save, explosive: " + exItem.ex_name, tag);
                    assertFalse("Warhead saved empty, explosive: " + exItem.ex_name, tag.hasNoTags());
                    assertTrue("Warhead doesn't contain exItem, explosive: " + exItem.ex_name, tag.hasKey(Warhead.NBT_EXPLOSIVE_ITEMSTACK));

                    //Test load
                    warhead = MissileModuleBuilder.INSTANCE.buildWarhead(casing, (ItemStack) null);
                    warhead.load(tag);

                    assertEquals("Warhead failed to load with right explosive instance, explosive: " + exItem.ex_name,
                            warhead.getExplosive(), exItem.getExplosive());
                }
            }
        }
    }

    @Test
    public void testRecipes()
    {
        assertTrue(new ItemStack(block).getItem() != null);

        List<IRecipe> recipes = new ArrayList();
        TileWarhead.getRecipes(recipes);
        for (IRecipe recipe : recipes)
        {
            assertNotNull(recipe);
            assertNotNull(recipe.getRecipeOutput());
        }
    }
}
