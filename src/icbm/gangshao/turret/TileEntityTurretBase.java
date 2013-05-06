package icbm.gangshao.turret;

import icbm.api.sentry.ISentry;
import icbm.gangshao.ZhuYaoGangShao;
import icbm.gangshao.actions.ActionManager;
import icbm.gangshao.actions.LookHelper;
import icbm.gangshao.platform.TileEntityTurretPlatform;
import icbm.gangshao.render.ITagRender;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.TileEntityAdvanced;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLLog;
import dark.library.DarkMain;

/**
 * Class that handles all the basic movement, and block based updates of a turret.
 * 
 * @author Rseifert
 * 
 */
public abstract class TileEntityTurretBase extends TileEntityAdvanced implements IPacketReceiver, ITagRender, IVoltage, ISentry
{

	public static final float MAX_PITCH = 30f;

	public static final float MIN_PITCH = -30f;

	private ForgeDirection platformDirection = ForgeDirection.DOWN;

	public final ActionManager actionManager = new ActionManager();
	public LookHelper lookHelper;

	/**
	 * The rotation of the arms. In Degrees.
	 */
	public float targetRotationYaw, targetRotationPitch = 0;
	public float rotationYaw, rotationPitch = 0;

	public abstract float getRotationSpeed();

	private int health = 100;

	public TileEntityTurretBase()
	{
		lookHelper = new LookHelper(this);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.isRunning())
		{
			this.onUpdate();
			this.updateRotation();
		}

		// Check to make sure this thing still has hp
		if (this.health <= 0)
		{
			this.destroy(true);
		}

		// Do packet update
		if (!this.worldObj.isRemote && this.ticks % 5 == 0)
		{
			PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 50);
		}

	}

	protected abstract void onUpdate();

	/**
	 * get the turrets control platform
	 */
	@Override
	public TileEntityTurretPlatform getPlatform()
	{
		TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.xCoord + this.platformDirection.offsetX, this.yCoord + this.platformDirection.offsetY, this.zCoord + this.platformDirection.offsetZ);

		if (tileEntity instanceof TileEntityTurretPlatform)
		{
			return (TileEntityTurretPlatform) tileEntity;
		}
		else
		{
			return null;
		}

	}

	/**
	 * Adjusts the turret's rotation to its target rotation over time.
	 */
	public void updateRotation()
	{
		if (Math.abs(this.rotationYaw - this.targetRotationYaw) > 0.001f)
		{
			float speedYaw;
			if (this.rotationYaw > this.targetRotationYaw)
			{
				speedYaw = -this.getRotationSpeed();
			}
			else
			{
				speedYaw = this.getRotationSpeed();
			}

			this.rotationYaw += speedYaw;

			if (Math.abs(this.rotationYaw - this.targetRotationYaw) < this.getRotationSpeed() + 0.1f)
			{
				this.rotationYaw = this.targetRotationYaw;
			}
		}

		if (Math.abs(this.rotationPitch - this.targetRotationPitch) > 0.001f)
		{
			float speedPitch;
			if (this.rotationPitch > this.targetRotationPitch)
			{
				speedPitch = -this.getRotationSpeed();
			}
			else
			{
				speedPitch = this.getRotationSpeed();
			}

			this.rotationPitch += speedPitch;

			if (Math.abs(this.rotationPitch - this.targetRotationPitch) < this.getRotationSpeed() + 0.1f)
			{
				this.rotationPitch = this.targetRotationPitch;
			}
		}

		/**
		 * Wraps all the angels and cleans them up.
		 */
		this.rotationPitch = MathHelper.wrapAngleTo180_float(Math.max(Math.min(this.rotationPitch, 60), -60));
		this.targetRotationYaw = MathHelper.wrapAngleTo180_float(this.targetRotationYaw);
		this.targetRotationPitch = MathHelper.wrapAngleTo180_float(Math.max(Math.min(this.targetRotationPitch, 60), -60));
	}

	/**
	 * is this sentry currently operating
	 */
	public boolean isRunning()
	{
		return this.getPlatform() != null && this.getPlatform().isRunning();
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return PacketManager.getPacket(ZhuYaoGangShao.CHANNEL, this, 1, nbt);
	}

	public void sendShotToClient(Vector3 tar)
	{
		Vector3 vec = this.getMuzzle().clone();
		PacketManager.sendPacketToClients(PacketManager.getPacket(ZhuYaoGangShao.CHANNEL, this, 2, tar.x, tar.y, tar.z), this.worldObj, new Vector3(this), 50);
	}

	/**
	 * Data
	 */
	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		if (this.worldObj.isRemote)
		{
			try
			{
				int pd = dataStream.readInt();
				if (pd == 1)
				{					
					short size = dataStream.readShort();

					if (size > 0)
					{
						byte[] byteCode = new byte[size];
						dataStream.readFully(byteCode);
						this.readFromNBT(CompressedStreamTools.decompress(byteCode));
					}
				}
				else if (pd == 2)
				{
					DarkMain.renderTracer(this.worldObj, this.getMuzzle(), new Vector3(dataStream.readDouble(), dataStream.readDouble(), dataStream.readDouble()));
				}
			}
			catch (IOException e)
			{
				FMLLog.severe("Failed to receive packet for Sentry");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setFloat("yaw", this.targetRotationYaw);
		nbt.setFloat("pitch", this.targetRotationPitch);
		nbt.setInteger("hp", this.health);
		nbt.setInteger("dir", this.platformDirection.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.targetRotationYaw = nbt.getFloat("yaw");
		this.targetRotationPitch = nbt.getFloat("pitch");
		this.health = nbt.getInteger("hp");
		this.platformDirection = ForgeDirection.getOrientation(nbt.getInteger("dir"));
	}

	/**
	 * removes the turret from existence optional create an explosion in the process
	 * 
	 * @param doExplosion - True if the turret is to explode.
	 */
	public boolean destroy(boolean doExplosion)
	{
		if (doExplosion)
		{
			this.worldObj.createExplosion(null, this.xCoord, this.yCoord, this.zCoord, 2f, true);
		}

		if (!this.worldObj.isRemote)
		{
			this.getBlockType().dropBlockAsItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockMetadata(), 0);
		}

		return this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, 0);
	}

	/**
	 * sets the direction by which the turret will deploy too Causes the model to rotate to match
	 * the direction
	 * 
	 * @param side
	 */
	public void setDeploySide(ForgeDirection side)
	{
		this.platformDirection = side.getOpposite();
	}

	@Override
	public float addInformation(HashMap<String, Integer> map, EntityPlayer player)
	{
		map.put(this.getName(), 0x88FF88);
		return 1;
	}

	@Override
	public String getName()
	{
		return new ItemStack(this.getBlockType(), 1, this.getBlockMetadata()).getDisplayName();
	}

	@Override
	public void setRotation(float yaw, float pitch)
	{
		this.targetRotationYaw = yaw;
		this.targetRotationPitch = pitch;
	}

	@Override
	public Vector3 getMuzzle()
	{
		Vector3 position = new Vector3(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5);
		return Vector3.add(position, Vector3.multiply(LookHelper.getDeltaPositionFromRotation(this.rotationYaw, this.rotationPitch - 10), 0.5));
	}
}
