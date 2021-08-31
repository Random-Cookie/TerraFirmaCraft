/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.dries007.tfc.common.TFCTags;


import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ThinSpikeBlock extends Block
{
    public static final VoxelShape PILLAR_SHAPE = Shapes.or(
        box(9.5, 0, 12.5, 11.5, 16, 14.5),
        box(8, 0, 1, 11, 16, 4),
        box(3.5, 0, 1.5, 5.5, 16, 3.5),
        box(4, 0, 11, 7, 16, 14),
        box(2.5, 0, 8.5, 4.5, 16, 10.5),
        box(9.5, 0, 4.5, 11.5, 16, 6.5),
        box(11, 0, 8, 14, 16, 11),
        box(4, 0, 4, 8, 16, 8)
    );

    public static final VoxelShape TIP_SHAPE = Shapes.or(
        box(5, 4, 12, 6, 8, 13),
        box(4, 12, 11, 7, 16, 14),
        box(4.5, 8, 11.5, 6.5, 12, 13.5),
        box(9, 4, 2, 10, 8, 3),
        box(8, 12, 1, 11, 16, 4),
        box(8.5, 8, 1.5, 10.5, 12, 3.5),
        box(5, 2, 5, 7, 7, 7),
        box(4, 11, 4, 8, 16, 8),
        box(4.5, 6, 4.5, 7.5, 11, 7.5),
        box(12, 5, 9, 13, 9, 10),
        box(11, 13, 8, 14, 16, 11),
        box(11.5, 9, 8.5, 13.5, 13, 10.5),
        box(10, 6, 5, 11, 12, 6),
        box(9.5, 12, 4.5, 11.5, 16, 6.5),
        box(3, 10, 9, 4, 14, 10),
        box(2.5, 14, 8.5, 4.5, 16, 10.5),
        box(4, 10, 2, 5, 13, 3),
        box(3.5, 13, 1.5, 5.5, 16, 3.5),
        box(10, 9, 13, 11, 14, 14),
        box(9.5, 14, 12.5, 11.5, 16, 14.5)
    );

    public static final BooleanProperty TIP = TFCBlockStateProperties.TIP;

    public ThinSpikeBlock(Properties properties)
    {
        super(properties);

        registerDefaultState(getStateDefinition().any().setValue(TIP, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        boolean flag = context.getLevel().getBlockState(context.getClickedPos().above()).is(TFCTags.Blocks.SMALL_SPIKE);
        return defaultBlockState().setValue(TIP, flag);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TIP);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if (facing == Direction.DOWN && !facingState.is(this))
        {
            return stateIn.setValue(TIP, true);
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        if (!canSurvive(state, worldIn, pos))
        {
            worldIn.destroyBlock(pos, false);
        }
        if (TFCTags.Blocks.SMALL_SPIKE.contains(blockIn))
        {
            worldIn.setBlock(pos, state.setValue(TIP, false), 2);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        BlockPos posDown = pos.below();
        BlockState otherState = worldIn.getBlockState(posDown);
        if (otherState.getBlock() == this)
        {
            worldIn.getBlockTicks().scheduleTick(posDown, this, 0);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
    {
        BlockPos abovePos = pos.above();
        BlockState aboveState = worldIn.getBlockState(abovePos);
        return (aboveState.getBlock() == this && !aboveState.getValue(TIP)) || aboveState.isFaceSturdy(worldIn, abovePos, Direction.DOWN);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return state.getValue(TIP) ? TIP_SHAPE : PILLAR_SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand)
    {
        worldIn.destroyBlock(pos, false);
    }
}