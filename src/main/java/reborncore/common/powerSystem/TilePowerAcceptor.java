package reborncore.common.powerSystem;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import reborncore.RebornCore;
import reborncore.api.IListInfoProvider;
import reborncore.api.power.IEnergyInterfaceTile;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

import com.mojang.realmsclient.gui.ChatFormatting;

import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergySourceInfo;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.Info;
import reborncore.api.power.IPowerConfig;
import reborncore.common.RebornCoreConfig;

@Optional.InterfaceList(value = { @Optional.Interface(iface = "ic2.api.energy.tile.IEnergyTile", modid = "IC2"),
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2") })
public abstract class TilePowerAcceptor extends RFProviderTile implements IEnergyReceiver, IEnergyProvider, // Cofh
		IEnergyInterfaceTile, IListInfoProvider, // TechReborn
		IEnergyTile, IEnergySink, IEnergySource, // Ic2
		IEnergySourceInfo // IC2 Classic //TODO ic2
{
	public int tier;
	private double energy;

	public TilePowerAcceptor(int tier)
	{
		this.tier = tier;
	}

	// IC2

	protected boolean addedToEnet;

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		// onLoaded();
	}

	// public void onLoaded() {
	// if (PowerSystem.EUPOWENET && !addedToEnet &&
	// !FMLCommonHandler.instance().getEffectiveSide().isClient() &&
	// Info.isIc2Available()) {
	// MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
	//
	// addedToEnet = true;
	// }
	// }

	@Override
	public void invalidate()
	{
		super.invalidate();
		onChunkUnload();
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		if (RebornCoreConfig.getRebornPower().eu())
		{
			if (addedToEnet && Info.isIc2Available())
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));

				addedToEnet = false;
			}
		}
	}

	@Override
	public double getDemandedEnergy()
	{
		if (!RebornCoreConfig.getRebornPower().eu())
			return 0;
		return Math.min(getMaxPower() - getEnergy(), getMaxInput());
	}

	@Override
	public int getSinkTier()
	{
		return tier;
	}

	@Override
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage)
	{
		setEnergy(getEnergy() + amount);
		return 0;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, EnumFacing direction)
	{
		if (!RebornCoreConfig.getRebornPower().eu())
			return false;
		return canAcceptEnergy(direction);
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, EnumFacing direction)
	{
		if (!RebornCoreConfig.getRebornPower().eu())
			return false;
		return canProvideEnergy(direction);
	}

	@Override
	public double getOfferedEnergy()
	{
		if (!RebornCoreConfig.getRebornPower().eu())
			return 0;
		return Math.min(getEnergy(), getMaxOutput());
	}

	@Override
	public void drawEnergy(double amount)
	{
		useEnergy((int) amount);
	}

	@Override
	public int getSourceTier()
	{
		return tier;
	}
	// END IC2

	// COFH
	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		if (!RebornCoreConfig.getRebornPower().rf())
			return false;
		return canAcceptEnergy(from) || canProvideEnergy(from);
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if (!RebornCoreConfig.getRebornPower().rf())
			return 0;
		if (!canAcceptEnergy(from))
		{
			return 0;
		}
		maxReceive *= RebornCoreConfig.euPerRF;
		int energyReceived = Math.min(getMaxEnergyStored(null) - getEnergyStored(null),
				Math.min((int) this.getMaxInput() * RebornCoreConfig.euPerRF, maxReceive));

		if (!simulate)
		{
			setEnergy(getEnergy() + energyReceived);
		}
		return energyReceived / RebornCoreConfig.euPerRF;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		if (!RebornCoreConfig.getRebornPower().rf())
			return 0;
		return ((int) getEnergy() / RebornCoreConfig.euPerRF);
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		if (!RebornCoreConfig.getRebornPower().rf())
			return 0;
		return ((int) getMaxPower() / RebornCoreConfig.euPerRF);
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		if (!RebornCoreConfig.getRebornPower().rf())
			return 0;
		if (!canProvideEnergy(from))
		{
			return 0;
		}
		maxExtract *= RebornCoreConfig.euPerRF;
		int energyExtracted = Math.min(getEnergyStored(null), Math.min(maxExtract, maxExtract));

		if (!simulate)
		{
			setEnergy(energy - energyExtracted);
		}
		return energyExtracted / RebornCoreConfig.euPerRF;
	}
	// END COFH

	// TechReborn

	@Override
	public double getEnergy()
	{
		return energy;
	}

	@Override
	public void setEnergy(double energy)
	{
		this.energy = energy;

		if (this.getEnergy() > getMaxPower())
		{
			this.setEnergy(getMaxPower());
		} else if (this.energy < 0)
		{
			this.setEnergy(0);
		}
	}

	@Override
	public double addEnergy(double energy)
	{
		return addEnergy(energy, false);
	}

	@Override
	public double addEnergy(double energy, boolean simulate)
	{
		double energyReceived = Math.min(getMaxPower() - energy, Math.min(this.getMaxPower(), energy));

		if (!simulate)
		{
			setEnergy(getEnergy() + energyReceived);
		}
		return energyReceived;
	}

	@Override
	public boolean canUseEnergy(double input)
	{
		return input <= energy;
	}

	@Override
	public double useEnergy(double energy)
	{
		return useEnergy(energy, false);
	}

	@Override
	public double useEnergy(double extract, boolean simulate)
	{
		if (extract > energy)
		{
			return 0;
		}
		if (!simulate)
		{
			setEnergy(energy - extract);
		}
		return extract;
	}

	@Override
	public boolean canAddEnergy(double energy)
	{
		return this.energy + energy <= getMaxPower();
	}
	// TechReborn END

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		NBTTagCompound data = tag.getCompoundTag("TilePowerAcceptor");
		energy = data.getDouble("energy");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("energy", energy);
		tag.setTag("TilePowerAcceptor", data);
	}

	public void readFromNBTWithoutCoords(NBTTagCompound tag)
	{
		NBTTagCompound data = tag.getCompoundTag("TilePowerAcceptor");
		energy = data.getDouble("energy");
	}

	public void writeToNBTWithoutCoords(NBTTagCompound tag)
	{
		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("energy", energy);
		tag.setTag("TilePowerAcceptor", data);
	}

	@Override
	public void addInfo(List<String> info, boolean isRealTile)
	{
		info.add(ChatFormatting.LIGHT_PURPLE + "Energy buffer Size " + ChatFormatting.GREEN
				+ PowerSystem.getLocaliszedPower(getMaxPower()));
		if (getMaxInput() != 0)
		{
			info.add(ChatFormatting.LIGHT_PURPLE + "Max Input " + ChatFormatting.GREEN
					+ PowerSystem.getLocaliszedPower(getMaxInput()));
		}
		if (getMaxOutput() != 0)
		{
			info.add(ChatFormatting.LIGHT_PURPLE + "Max Output " + ChatFormatting.GREEN
					+ PowerSystem.getLocaliszedPower(getMaxOutput()));
		}
		info.add(ChatFormatting.LIGHT_PURPLE + "Tier " + ChatFormatting.GREEN + getTier());
		// if(isRealTile){ //TODO sync to client
		// info.add(ChatFormatting.LIGHT_PURPLE + "Stored energy " +
		// ChatFormatting.GREEN + getEUString(energy));
		// }
	}

	public double getFreeSpace()
	{
		return getMaxPower() - energy;
	}

	// IC2 Classic

	@Override
	public int getMaxEnergyAmount()
	{
		return (int) getMaxOutput();
	}

	public void charge(int slot)
	{
		// TODO rewrite to use built in power system
		// if(getStackInSlot(slot) != null)
		// {
		// if(getStackInSlot(slot).getItem() instanceof IElectricItem)
		// {
		// if(getEnergy() != getMaxPower())
		// {
		// ItemStack stack = inventory.getStackInSlot(slot);
		// double MaxCharge = ((IElectricItem)
		// stack.getItem()).getMaxCharge(stack);
		// double CurrentCharge = ElectricItem.manager.getCharge(stack);
		// if (CurrentCharge != 0)
		// {
		// ElectricItem.manager.discharge(stack, 5, 4, false, false, false);
		// addEnergy(5);
		// }
		// }
		// }
		// }
	}

	public int getEnergyScaled(int scale)
	{
		return (int) ((energy * scale / getMaxPower()));
	}

	public IPowerConfig getPowerConfig(){
		return RebornCoreConfig.getRebornPower();
	}

}