/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package reborncore.common.util;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import reborncore.api.recipe.IRecipeCrafterProvider;
import reborncore.api.tile.IUpgradeable;
import reborncore.common.recipes.RecipeCrafter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//From OpenBlocksLib: https://github.com/OpenMods/OpenModsLib
public class InventoryHelper {

	public static void tryInsertStack(IInventory targetInventory, int slot, ItemStack stack, boolean canMerge) {
		if (targetInventory.isItemValidForSlot(slot, stack)) {
			ItemStack targetStack = targetInventory.getStackInSlot(slot);
			if (targetStack.isEmpty()) {
				int space = targetInventory.getInventoryStackLimit();
				int mergeAmount = Math.min(space, stack.getCount());

				ItemStack copy = stack.copy();
				copy.setCount(mergeAmount);
				targetInventory.setInventorySlotContents(slot, copy);
				stack.shrink(mergeAmount);
			} else if (canMerge) {
				if (targetInventory.isItemValidForSlot(slot, stack) && areMergeCandidates(stack, targetStack)) {
					int space = Math.min(targetInventory.getInventoryStackLimit(), targetStack.getMaxStackSize())
						- targetStack.getCount();
					int mergeAmount = Math.min(space, stack.getCount());

					ItemStack copy = targetStack.copy();
					copy.grow(mergeAmount);
					targetInventory.setInventorySlotContents(slot, copy);
					stack.shrink(mergeAmount);
				}
			}
		}
	}

	protected static boolean areMergeCandidates(ItemStack source, ItemStack target) {
		return source.isItemEqual(target) && ItemStack.areItemStackTagsEqual(source, target)
			&& target.getCount() < target.getMaxStackSize();
	}

	public static void insertItemIntoInventory(IInventory inventory, ItemStack stack) {
		insertItemIntoInventory(inventory, stack, null, -1);
	}

	public static void insertItemIntoInventory(IInventory inventory, ItemStack stack, EnumFacing side, int intoSlot) {
		insertItemIntoInventory(inventory, stack, side, intoSlot, true);
	}

	public static void insertItemIntoInventory(IInventory inventory, ItemStack stack, EnumFacing side, int intoSlot,
	                                           boolean doMove) {
		insertItemIntoInventory(inventory, stack, side, intoSlot, doMove, true);
	}

	public static void insertItemIntoInventory(IInventory inventory, ItemStack stack, EnumFacing side, int intoSlot,
	                                           boolean doMove, boolean canStack) {
		if (stack.isEmpty())
			return;

		IInventory targetInventory = inventory;

		if (!doMove) {
			targetInventory = new GenericInventory("temporary.inventory", false, targetInventory.getSizeInventory());
			((GenericInventory) targetInventory).copyFrom(inventory);
		}

		int i = 0;
		int[] attemptSlots = new int[0];

		if (inventory instanceof ISidedInventory && side != null) {
			attemptSlots = ((ISidedInventory) inventory).getSlotsForFace(side);
			if (attemptSlots == null)
				attemptSlots = new int[0];
		} else {
			attemptSlots = new int[inventory.getSizeInventory()];
			for (int a = 0; a < inventory.getSizeInventory(); a++)
				attemptSlots[a] = a;
		}
		if (intoSlot > -1) {
			Set<Integer> x = new HashSet<Integer>();
			for (int attemptedSlot : attemptSlots)
				x.add(attemptedSlot);

			if (x.contains(intoSlot))
				attemptSlots = new int[] { intoSlot };
			else
				attemptSlots = new int[0];
		}
		while (stack.getCount() > 0 && i < attemptSlots.length) {
			if (side != null && inventory instanceof ISidedInventory)
				if (!((ISidedInventory) inventory).canInsertItem(attemptSlots[i], stack, side.getOpposite())) {
					i++;
					continue;
				}

			tryInsertStack(targetInventory, attemptSlots[i], stack, canStack);
			i++;
		}
	}

	public static int testInventoryInsertion(IInventory inventory, ItemStack item, EnumFacing side) {
		if (item.isEmpty() || item.getCount() == 0)
			return 0;
		item = item.copy();

		if (inventory == null)
			return 0;

		inventory.getSizeInventory();

		int itemSizeCounter = item.getCount();
		int[] availableSlots = new int[0];

		if (inventory instanceof ISidedInventory && side != null)
			availableSlots = ((ISidedInventory) inventory).getSlotsForFace(side);
		else if(inventory instanceof IRecipeCrafterProvider) { //This is horrible, and shoudnlt need to be a thing. Remove when side configs are added
			RecipeCrafter recipeCrafter = ((IRecipeCrafterProvider) inventory).getRecipeCrafter();
			availableSlots = recipeCrafter.inputSlots;
		} else {
			availableSlots = buildSlotsForLinearInventory(inventory);
		}

		for (int i : availableSlots) {
			if (itemSizeCounter <= 0)
				break;

			if (!inventory.isItemValidForSlot(i, item))
				continue;

			if (side != null && inventory instanceof ISidedInventory)
				if (!((ISidedInventory) inventory).canInsertItem(i, item, side.getOpposite()))
					continue;

			ItemStack inventorySlot = inventory.getStackInSlot(i);
			if (inventorySlot.isEmpty())
				itemSizeCounter -= Math.min(Math.min(itemSizeCounter, inventory.getInventoryStackLimit()),
					item.getMaxStackSize());
			else if (areMergeCandidates(item, inventorySlot)) {
				int space = Math.min(inventory.getInventoryStackLimit(), inventorySlot.getMaxStackSize())
					- inventorySlot.getCount();
				itemSizeCounter -= Math.min(itemSizeCounter, space);
			}
		}

		if (itemSizeCounter != item.getCount()) {
			itemSizeCounter = Math.max(itemSizeCounter, 0);
			return item.getCount() - itemSizeCounter;
		}

		return 0;
	}

	public static IInventory getInventory(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) tileEntity;

			TileEntityChest adjacent = null;

			if (chest.adjacentChestXNeg != null) {
				adjacent = chest.adjacentChestXNeg;
			}

			if (chest.adjacentChestXPos != null) {
				adjacent = chest.adjacentChestXPos;
			}

			if (chest.adjacentChestZNeg != null) {
				adjacent = chest.adjacentChestZNeg;
			}

			if (chest.adjacentChestZPos != null) {
				adjacent = chest.adjacentChestZPos;
			}

			if (adjacent != null) {
				return new InventoryLargeChest("", chest, adjacent);
			}
			return chest;
		}
		return tileEntity instanceof IInventory ? (IInventory) tileEntity : null;
	}

	public static IInventory getInventory(World world, int x, int y, int z, EnumFacing direction) {
		if (direction != null) {
			x += direction.getXOffset();
			y += direction.getYOffset();
			z += direction.getZOffset();
		}
		return getInventory(world, x, y, z);

	}

	public static IInventory getInventory(IInventory inventory) {
		if (inventory instanceof TileEntityChest) {
			TileEntity te = (TileEntity) inventory;
			return getInventory(te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
		}
		return inventory;
	}

	public static int[] buildSlotsForLinearInventory(IInventory inv) {
		int[] slots = new int[inv.getSizeInventory()];
		for (int i = 0; i < slots.length; i++)
			slots[i] = i;

		return slots;
	}
	
	public static void dropInventoryItems(World world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity == null) {
			return;
		}
		if (tileEntity instanceof IInventory) {
			IInventory inventory = (IInventory) tileEntity;

			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				ItemStack itemStack = inventory.getStackInSlot(i);

				if (itemStack.isEmpty()) {
					continue;
				}
				if (itemStack.getCount() > 0) {
					if (itemStack.getItem() instanceof ItemBlock) {
						if (((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockFluidBase
								|| ((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockStaticLiquid
								|| ((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockDynamicLiquid) {
							continue;
						}
					}
				}
				net.minecraft.inventory.InventoryHelper.spawnItemStack(world, (double) pos.getX(), (double) pos.getY(),
						(double) pos.getZ(), itemStack);
			}
		}
		if (tileEntity instanceof IUpgradeable) {
			net.minecraft.inventory.InventoryHelper.dropInventoryItems(world, pos, ((IUpgradeable) tileEntity).getUpgradeInventory());
		}
	}

	public static class GenericInventory implements IInventory {

		protected String inventoryTitle;
		protected int slotsCount;
		protected ItemStack[] inventoryContents;
		protected boolean isInvNameLocalized;

		public GenericInventory(String name, boolean isInvNameLocalized, int size) {
			this.isInvNameLocalized = isInvNameLocalized;
			slotsCount = size;
			inventoryTitle = name;
			inventoryContents = new ItemStack[size];
		}

		@Override
		public ItemStack decrStackSize(int par1, int par2) {
			if (!inventoryContents[par1].isEmpty()) {
				ItemStack itemstack;

				if (inventoryContents[par1].getCount() <= par2) {
					itemstack = inventoryContents[par1];
					inventoryContents[par1] = ItemStack.EMPTY;
					return itemstack;
				}

				itemstack = inventoryContents[par1].splitStack(par2);
				if (inventoryContents[par1].getCount() == 0)
					inventoryContents[par1] = ItemStack.EMPTY;

				return itemstack;
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public int getSizeInventory() {
			return slotsCount;
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack itemstack : inventoryContents) {
				if (!itemstack.isEmpty()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public ItemStack getStackInSlot(int i) {
			return inventoryContents[i];
		}

		public ItemStack getStackInSlot(Enum<?> i) {
			return getStackInSlot(i.ordinal());
		}

		@Override
		public ItemStack removeStackFromSlot(int i) {
			if (i >= inventoryContents.length)
				return ItemStack.EMPTY;

			if (!inventoryContents[i].isEmpty()) {
				ItemStack itemstack = inventoryContents[i];
				inventoryContents[i] = ItemStack.EMPTY;
				return itemstack;
			}

			return ItemStack.EMPTY;
		}

		public boolean isItem(int slot, Item item) {
			return !inventoryContents[slot].isEmpty() && inventoryContents[slot].getItem() == item;
		}

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemstack) {
			return true;
		}

		@Override
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {

		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public void clear() {

		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer entityplayer) {
			return true;
		}

		@Override
		public void openInventory(EntityPlayer player) {

		}

		@Override
		public void closeInventory(EntityPlayer player) {

		}

		public void clearAndSetSlotCount(int amount) {
			slotsCount = amount;
			inventoryContents = new ItemStack[amount];
		}

		public void readFromNBT(NBTTagCompound tag) {
			if (tag.hasKey("size"))
				slotsCount = tag.getInteger("size");

			NBTTagList nbttaglist = tag.getTagList("Items", 10);
			inventoryContents = new ItemStack[slotsCount];
			for (int i = 0; i < nbttaglist.tagCount(); i++) {
				NBTTagCompound stacktag = nbttaglist.getCompoundTagAt(i);
				int j = stacktag.getByte("Slot");
				if (j >= 0 && j < inventoryContents.length)
					inventoryContents[j] = new ItemStack(stacktag);
			}
		}

		@Override
		public void setInventorySlotContents(int i, ItemStack itemstack) {
			inventoryContents[i] = itemstack;

			if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit())
				itemstack.setCount(getInventoryStackLimit());
		}

		public void writeToNBT(NBTTagCompound tag) {
			tag.setInteger("size", getSizeInventory());
			NBTTagList nbttaglist = new NBTTagList();
			for (int i = 0; i < inventoryContents.length; i++) {
				if (!inventoryContents[i].isEmpty()) {
					NBTTagCompound stacktag = new NBTTagCompound();
					stacktag.setByte("Slot", (byte) i);
					inventoryContents[i].writeToNBT(stacktag);
					nbttaglist.appendTag(stacktag);
				}
			}
			tag.setTag("Items", nbttaglist);
		}

		public void copyFrom(IInventory inventory) {
			for (int i = 0; i < inventory.getSizeInventory(); i++)
				if (i < getSizeInventory()) {
					ItemStack stack = inventory.getStackInSlot(i);
					if (!stack.isEmpty())
						setInventorySlotContents(i, stack.copy());
					else
						setInventorySlotContents(i, ItemStack.EMPTY);
				}
		}

		public List<ItemStack> contents() {
			return Arrays.asList(inventoryContents);
		}

		@Override
		public void markDirty() {
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}

		@Override
		public ITextComponent getDisplayName() {
			return null;
		}
	}
}
