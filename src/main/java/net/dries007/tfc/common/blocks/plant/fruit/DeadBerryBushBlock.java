/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.plant.fruit;


import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ForgeBlockProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.tileentity.TickCounterTileEntity;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.ICalendar;

public class DeadBerryBushBlock extends SeasonalPlantBlock implements IFluidLoggable, EntityBlockExtension
{
    public static final FluidProperty FLUID = TFCBlockStateProperties.FRESH_WATER;

    public DeadBerryBushBlock(ForgeBlockProperties properties)
    {
        super(properties, () -> Items.AIR, new Lifecycle[12]);

        registerDefaultState(getStateDefinition().any().setValue(getFluidProperty(), getFluidProperty().keyFor(Fluids.EMPTY)).setValue(STAGE, 0));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if (!stateIn.canSurvive(worldIn, currentPos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            final Fluid containedFluid = stateIn.getValue(getFluidProperty()).getFluid();
            if (containedFluid != Fluids.EMPTY)
            {
                worldIn.getLiquidTicks().scheduleTick(currentPos, containedFluid, containedFluid.getTickDelay(worldIn));
            }
            return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
    {
        return InteractionResult.FAIL;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
        if (random.nextInt(15) == 0 && !world.getBlockState(pos.above()).is(TFCTags.Blocks.ANY_SPREADING_BUSH))
        {
            TickCounterTileEntity te = Helpers.getTileEntity(world, pos, TickCounterTileEntity.class);
            if (te != null)
            {
                if (te.getTicksSinceUpdate() > ICalendar.TICKS_IN_DAY * 80)
                {
                    te.setRemoved();
                    world.destroyBlock(pos, true);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(getFluidProperty(), STAGE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state)
    {
        return IFluidLoggable.super.getFluidState(state);
    }

    @Override
    public FluidProperty getFluidProperty()
    {
        return FLUID;
    }
}