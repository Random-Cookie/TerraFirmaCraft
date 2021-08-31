/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import net.dries007.tfc.common.tileentity.SnowPileTileEntity;
import net.dries007.tfc.common.tileentity.TFCTileEntities;
import net.dries007.tfc.util.Helpers;

/**
 * This block is a snow layer block that hides / covers a block underneath
 * When it melts, it will transform into the underlying block, with one level of snow active
 */
public class SnowPileBlock extends SnowLayerBlock implements IForgeBlockExtension, EntityBlockExtension
{
    /**
     * Converts an existing block state to a snow pile consisting of that block state
     *
     * @param world      The world
     * @param pos        The position
     * @param state      The original state
     */
    public static void convertToPile(LevelAccessor world, BlockPos pos, BlockState state)
    {
        world.setBlock(pos, TFCBlocks.SNOW_PILE.get().defaultBlockState(), 3);
        world.getBlockEntity(pos, TFCTileEntities.SNOW_PILE.get()).ifPresent(entity -> entity.setInternalState(state));
    }

    private final ForgeBlockProperties properties;

    public SnowPileBlock(ForgeBlockProperties properties)
    {
        super(properties.properties());

        this.properties = properties;
    }

    @Override
    public ForgeBlockProperties getForgeProperties()
    {
        return properties;
    }

    /**
     * This allows two things:
     * - Snow piles are removed one layer at a time, same as snow blocks (modified via mixin)
     * - Once removed enough, they convert to the underlying block state.
     */
    @Override
    public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
    {
        playerWillDestroy(world, pos, state, player);
        return world.getBlockEntity(pos, TFCTileEntities.SNOW_PILE.get()).map(entity -> {
            BlockState newState = entity.getDestroyedState(state);
            return world.setBlock(pos, newState, world.isClientSide ? 11 : 3);
        }).orElse(false);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
    {
        return new ItemStack(Blocks.SNOW);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SnowPileTileEntity(pos, state);
    }
}