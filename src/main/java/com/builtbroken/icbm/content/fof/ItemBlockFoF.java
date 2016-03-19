package com.builtbroken.icbm.content.fof;

import com.builtbroken.icbm.content.prefab.ItemBlockICBM;
import net.minecraft.block.Block;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2016.
 */
public class ItemBlockFoF extends ItemBlockICBM
{
    public ItemBlockFoF(Block block)
    {
        super(block);
        additionalHeight = 1;
    }
}
