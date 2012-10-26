package icbm;

import icbm.zhapin.ZhaPin.ZhaPinType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import universalelectricity.core.UEConfig;
import universalelectricity.core.Vector2;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;

public class BaoHu
{
	/**
	 * File Structure: Tag: DimData -Tag: DimID
	 * -boolean: globalBan -Tag: RegionName -int:
	 * X -int: Z -int: R -int: TYPE
	 */
	public static NBTTagCompound nbtData;

	public static final String FIELD_GLOBAL_BAN = "globalBan";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_X = "X";
	public static final String FIELD_Z = "Z";
	public static final String FIELD_R = "R";

	public static final boolean DEFAULT_PROTECITON = UEConfig.getConfigData(ZhuYao.CONFIGURATION, "Protect Worlds by Default", false);

	public static boolean shiWeiZhiBaoHu(World worldObj, Vector2 position, ZhaPinType type)
	{
		if (!worldObj.isRemote)
		{
			NBTTagCompound dimData = nbtData.getCompoundTag("dim" + worldObj.getWorldInfo().getDimension());

			if (shiQuanQiuBaoHu(dimData)) { return true; }

			// Regions check
			Iterator i = dimData.getTags().iterator();
			while (i.hasNext())
			{
				try
				{
					NBTTagCompound region = (NBTTagCompound) i.next();

					if (Vector2.distance(position, new Vector2(region.getInteger(FIELD_X), region.getInteger(FIELD_Z))) <= region.getInteger(FIELD_R)) { return (ZhaPinType.get(region.getInteger(FIELD_TYPE)) == ZhaPinType.ALL || ZhaPinType.get(region.getInteger(FIELD_TYPE)) == type); }
				}
				catch (Exception e)
				{
				}
			}
		}

		return false;
	}

	public static boolean shiZhaDanBaoHu(World worldObj, Vector2 position)
	{
		return !shiWeiZhiBaoHu(worldObj, position, ZhaPinType.BLOCK);
	}

	public static boolean shiShouLiuDanBaoHu(World worldObj, Vector2 position)
	{
		return !shiWeiZhiBaoHu(worldObj, position, ZhaPinType.GRENADE);
	}

	public static boolean shiDaoDanBaoHu(World worldObj, Vector2 position)
	{
		return !shiWeiZhiBaoHu(worldObj, position, ZhaPinType.MISSILE);
	}

	public static boolean shiQuanQiuBaoHu(NBTTagCompound dimData)
	{
		return ((!dimData.hasKey(FIELD_GLOBAL_BAN) && DEFAULT_PROTECITON) || dimData.getBoolean(FIELD_GLOBAL_BAN));
	}

	public static boolean saveData(NBTTagCompound data, String filename)
	{
		String folder;

		if (FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
			folder = FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
		else
			folder = "saves" + File.separator + FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
		try
		{
			File var3 = new File(folder + File.separator, filename + "_tmp_.dat");
			File var4 = new File(folder + File.separator, filename + ".dat");

			CompressedStreamTools.writeCompressed(data, new FileOutputStream(var3));

			if (var4.exists())
			{
				var4.delete();
			}

			var3.renameTo(var4);
			return true;
		}
		catch (Exception var5)
		{
			FMLLog.severe("Failed to save " + filename + ".dat!");
			return false;
		}
	}

	public static NBTTagCompound loadData(String filename)
	{
		String folder;
		if (FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
			folder = FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
		else
			folder = "saves" + File.separator + FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
		try
		{
			File var2 = new File(folder + File.separator, filename + ".dat");

			if (var2.exists())
			{
				return CompressedStreamTools.readCompressed(new FileInputStream(var2));
			}
			else
			{
				return new NBTTagCompound();
			}
		}
		catch (Exception var3)
		{
			FMLLog.severe("Failed to load " + filename + ".dat!");
			return null;
		}
	}
}
