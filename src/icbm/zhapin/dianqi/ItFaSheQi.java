package icbm.zhapin.dianqi;

import icbm.api.explosion.ExplosiveType;
import icbm.core.di.ItElectricICBM;
import icbm.zhapin.ZhuYaoZhaPin;
import icbm.zhapin.zhapin.daodan.DaoDan;
import icbm.zhapin.zhapin.daodan.EDaoDan;
import icbm.zhapin.zhapin.daodan.ItDaoDan;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;

/**
 * Rocket Launcher
 * 
 * @author Calclavia
 * 
 */

public class ItFaSheQi extends ItElectricICBM
{
	private static final int YONG_DIAN_LIANG = 5000;

	public ItFaSheQi(int par1)
	{
		super(par1, "rocketLauncher");
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.bow;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if (!world.isRemote)
		{

			if (this.getElectricityStored(itemStack) >= YONG_DIAN_LIANG)
			{
				// Check the player's inventory and look for missiles.
				for (int i = 0; i < player.inventory.getSizeInventory(); i++)
				{
					ItemStack inventoryStack = player.inventory.getStackInSlot(i);

					if (inventoryStack != null)
					{
						if (inventoryStack.getItem() instanceof ItDaoDan)
						{
							int haoMa = inventoryStack.getItemDamage();

							if (inventoryStack.getItem() instanceof ItTeBieDaoDan)
							{
								haoMa += 100;
							}

							DaoDan daoDan = DaoDan.list[haoMa];

							if (daoDan != null && !ZhuYaoZhaPin.shiBaoHu(world, new Vector3(player), ExplosiveType.AIR, haoMa))
							{
								// Limit the missile to tier two.
								if (daoDan.getTier() <= 2 && daoDan.isCruise())
								{
									double dist = 5000;
									Vector3 diDian = Vector3.add(new Vector3(player), new Vector3(0, 0.5, 0));
									Vector3 kan = new Vector3(player.getLook(1));
									Vector3 kaiShiDiDian = Vector3.add(diDian, Vector3.multiply(kan, 1.1));
									Vector3 muBiao = Vector3.add(diDian, Vector3.multiply(kan, 100));

									EDaoDan eDaoDan = new EDaoDan(world, kaiShiDiDian, daoDan.getID(), player.rotationYaw, player.rotationPitch);
									world.spawnEntityInWorld(eDaoDan);
									eDaoDan.launch(muBiao);

									if (!player.capabilities.isCreativeMode)
									{
										player.inventory.setInventorySlotContents(i, null);
									}

									this.onProvide(ElectricityPack.getFromWatts(YONG_DIAN_LIANG, this.getElectricityStored(itemStack)), itemStack);

									return itemStack;
								}
							}
							else
							{
								player.sendChatToPlayer("Region being is protected.");
							}

						}
					}
				}
			}
		}

		return itemStack;
	}

	@Override
	public float getVoltage(ItemStack itemStack)
	{
		return 25;
	}

	@Override
	public double getMaxElectricityStored(ItemStack itemStack)
	{
		return 100000;
	}
}
