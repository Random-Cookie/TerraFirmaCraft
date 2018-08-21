/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 *
 */

package net.dries007.tfc.objects.container;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import net.dries007.tfc.objects.inventory.SlotFluidTransfer;
import net.dries007.tfc.util.Helpers;

public class ContainerSmallVesselLiquid extends Container
{
    private final EntityPlayer player;
    private final ItemStack stack;
    private final int itemIndex;

    private final IItemHandlerModifiable inventory;

    public ContainerSmallVesselLiquid(InventoryPlayer playerInv, ItemStack stack)
    {
        this.player = playerInv.player;
        this.stack = stack;
        this.itemIndex = playerInv.currentItem + 31;
        this.inventory = new ItemStackHandler(1);

        addContainerSlots(stack);
        addPlayerInventorySlots(playerInv);
    }

    @Override
    public void detectAndSendChanges()
    {
        // This is where we transfer liquid metal into a mold
        IFluidHandler capFluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (capFluidHandler != null)
        {
            ItemStack outputStack = inventory.getStackInSlot(0);
            if (!outputStack.isEmpty())
            {
                IFluidHandler outFluidHandler = outputStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (outFluidHandler != null)
                {
                    FluidStack fStack = capFluidHandler.drain(1, false);
                    if (fStack != null && outFluidHandler.fill(fStack, false) == 1)
                    {
                        outFluidHandler.fill(capFluidHandler.drain(1, true), true);
                    }

                    stack.setTagCompound(((INBTSerializable<NBTTagCompound>) capFluidHandler).serializeNBT());
                    outputStack.setTagCompound(((INBTSerializable<NBTTagCompound>) outFluidHandler).serializeNBT());
                }
            }
        }
        super.detectAndSendChanges();
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        // Slot that was clicked
        Slot slot = inventorySlots.get(index);

        ItemStack itemstack;

        if (slot == null || !slot.getHasStack())
            return ItemStack.EMPTY;

        if (index == itemIndex)
            return ItemStack.EMPTY;

        ItemStack itemstack1 = slot.getStack();
        itemstack = itemstack1.copy();

        // Begin custom transfer code here

        int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size(); // number of slots in the container

        if (index < containerSlots)
        {
            // Transfer out of the container
            if (!this.mergeItemStack(itemstack1, containerSlots, inventorySlots.size(), true))
            {
                // Don't transfer anything
                return ItemStack.EMPTY;
            }
            //tile.setAndUpdateSlots(index);
        }
        // Transfer into the container
        else
        {
            if (!this.mergeItemStack(itemstack1, 0, 4, false))
            {
                return ItemStack.EMPTY;
            }
        }

        // Required
        if (itemstack1.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }
        if (itemstack1.getCount() == itemstack.getCount())
        {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, itemstack1);
        return itemstack;
    }

    @Override
    @Nonnull
    public ItemStack slotClick(int slotID, int dragType, ClickType clickType, EntityPlayer player)
    {
        if ((clickType == ClickType.QUICK_MOVE || clickType == ClickType.PICKUP || clickType == ClickType.SWAP) && slotID == itemIndex)
        {
            return ItemStack.EMPTY;
        }
        else
        {
            return super.slotClick(slotID, dragType, clickType, player);
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        if (!player.getEntityWorld().isRemote)
        {
            ItemStack stack = inventory.getStackInSlot(0);
            if (!stack.isEmpty())
            {
                Helpers.spawnItemStack(player.getEntityWorld(), player.getPosition(), stack);
            }
        }
        super.onContainerClosed(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    private void addPlayerInventorySlots(InventoryPlayer playerInv)
    {
        // Add Player Inventory Slots
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++)
        {
            addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    private void addContainerSlots(ItemStack stack)
    {
        addSlotToContainer(new SlotFluidTransfer(inventory, 0, 80, 34));
    }

}
