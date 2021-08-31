/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.plant;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;

import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class TFCTallGrassBlock extends ShortGrassBlock implements ITallPlant
{
    protected static final EnumProperty<Part> PART = TFCBlockStateProperties.TALL_PLANT_PART;
    protected static final VoxelShape PLANT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape SHORTER_PLANT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);

    public static TFCTallGrassBlock create(IPlant plant, Properties properties)
    {
        return new TFCTallGrassBlock(properties)
        {
            @Override
            public IPlant getPlant()
            {
                return plant;
            }
        };
    }

    protected TFCTallGrassBlock(Properties properties)
    {
        super(properties);

        registerDefaultState(stateDefinition.any().setValue(PART, Part.LOWER));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        Part part = stateIn.getValue(PART);
        if (facing.getAxis() != Direction.Axis.Y || part == Part.LOWER != (facing == Direction.UP) || facingState.getBlock() == this && facingState.getValue(PART) != part)
        {
            return part == Part.LOWER && facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
        else
        {
            return Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
    {
        if (state.getValue(PART) == Part.LOWER)
        {
            return super.canSurvive(state, worldIn, pos);
        }
        else
        {
            BlockState blockstate = worldIn.getBlockState(pos.below());
            if (state.getBlock() != this)
            {
                return super.canSurvive(state, worldIn, pos); //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
            }
            return blockstate.getBlock() == this && blockstate.getValue(PART) == Part.LOWER;
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockPos pos = context.getClickedPos();
        return pos.getY() < 255 && context.getLevel().getBlockState(pos.above()).canBeReplaced(context) ? super.getStateForPlacement(context) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(PART));
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        worldIn.setBlockAndUpdate(pos.above(), defaultBlockState().setValue(PART, Part.UPPER));
    }

    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player)
    {
        if (!worldIn.isClientSide)
        {
            if (player.isCreative())
            {
                if (state.getValue(PART) == Part.UPPER)
                {
                    BlockPos blockpos = pos.below();
                    BlockState blockstate = worldIn.getBlockState(blockpos);
                    if (blockstate.getBlock() == state.getBlock() && blockstate.getValue(PART) == Part.LOWER)
                    {
                        worldIn.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                        worldIn.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
                    }
                }
            }
            else
            {
                dropResources(state, worldIn, pos, null, player, player.getMainHandItem());
            }
        }
    }

    /**
     * See {@link net.minecraft.block.DoublePlantBlock}. We handle drops in playerWillDestroy so we must not drop things here.
     */
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity tile, ItemStack stack)
    {
        super.playerDestroy(level, player, pos, Blocks.AIR.defaultBlockState(), tile, stack);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        Part part = state.getValue(PART);
        if (part == Part.LOWER)
            return PLANT_SHAPE;
        return SHORTER_PLANT_SHAPE;
    }

    @Override
    public OffsetType getOffsetType()
    {
        return OffsetType.XYZ;
    }

    public void placeTwoHalves(LevelAccessor world, BlockPos pos, int flags, Random random)
    {
        int age = random.nextInt(3) + 1;
        world.setBlock(pos, updateStateWithCurrentMonth(defaultBlockState().setValue(TFCBlockStateProperties.TALL_PLANT_PART, Part.LOWER).setValue(TFCBlockStateProperties.AGE_3, age)), flags);
        world.setBlock(pos.above(), updateStateWithCurrentMonth(defaultBlockState().setValue(TFCBlockStateProperties.TALL_PLANT_PART, Part.UPPER).setValue(TFCBlockStateProperties.AGE_3, age)), flags);
    }
}