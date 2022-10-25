/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;

import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.util.Fertilizer;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity.NutrientType;
import static net.dries007.tfc.common.blockentities.FarmlandBlockEntity.NutrientType.*;

/**
 * Implement on block entities that hold nitrogen, phosphorous, and potassium values to allow fertilization and consuming nutrients.
 */
public interface IFarmland
{
    static void addNutrientParticles(ServerLevel level, BlockPos pos, Fertilizer fertilizer)
    {
        final float n = fertilizer.getNitrogen(), p = fertilizer.getPhosphorus(), k = fertilizer.getPotassium();
        for (int i = 0; i < (int) (n > 0 ? Mth.clamp(n * 10, 1, 5) : 0); i++)
        {
            level.sendParticles(TFCParticles.NITROGEN.get(), pos.getX() + level.random.nextFloat(), pos.getY() + level.random.nextFloat() / 5D, pos.getZ() + level.random.nextFloat(), 0, 0D, 0D, 0D, 1D);
        }
        for (int i = 0; i < (int) (p > 0 ? Mth.clamp(p * 10, 1, 5) : 0); i++)
        {
            level.sendParticles(TFCParticles.PHOSPHORUS.get(), pos.getX() + level.random.nextFloat(), pos.getY() + level.random.nextFloat() / 5D, pos.getZ() + level.random.nextFloat(), 0, 0D, 0D, 0D, 1D);
        }
        for (int i = 0; i < (int) (k > 0 ? Mth.clamp(k * 10, 1, 5) : 0); i++)
        {
            level.sendParticles(TFCParticles.POTASSIUM.get(), pos.getX() + level.random.nextFloat(), pos.getY() + level.random.nextFloat() / 5D, pos.getZ() + level.random.nextFloat(), 0, 0D, 0D, 0D, 1D);
        }
    }

    /**
     * @return the amount [0-1] of the nutrient of {@code type}
     */
    float getNutrient(NutrientType type);

    /**
     * Implementations should clamp {@code value} on a range [0, 1]
     *
     * @param type the nutrient type to be set
     * @param value the amount (clamped [0-1]) of the nutrient to set
     */
    void setNutrient(NutrientType type, float value);

    default void addNutrient(NutrientType type, float value)
    {
        setNutrient(type, getNutrient(type) + value);
    }

    default void addNutrients(Fertilizer fertilizer)
    {
        addNutrient(NITROGEN, fertilizer.getNitrogen());
        addNutrient(PHOSPHOROUS,  fertilizer.getPhosphorus());
        addNutrient(POTASSIUM, fertilizer.getPotassium());
    }

    /**
     * @deprecated use resupplyModifier to interact with the crop's consumption/resupply. this is unused
     * @return The fraction [0, 1] of a nutrient consumed to add to non-consumed nutrients
     */
    @Deprecated
    default float resupplyFraction()
    {
        return 1 / 6f;
    }

    /**
     * @return A positive number to multiply the resupply amount of a crop block by.
     */
    default float resupplyModifier()
    {
        return 1f;
    }

    @Deprecated
    default float consumeNutrientAndResupplyOthers(NutrientType type, float amount)
    {
        return consumeNutrientAndResupplyOthers(type, amount, 1 / 6f);
    }

    /**
     * Consume up to {@code amount} of nutrient {@code type}.
     * Resupplies other nutrient by default what.
     * @return The amount of nutrient {@code type} that was actually consumed.
     */
    default float consumeNutrientAndResupplyOthers(NutrientType type, float amount, float resupplyFraction)
    {
        final float startValue = getNutrient(type);
        final float consumed = Math.min(startValue, amount);

        setNutrient(type, startValue - consumed);
        for (NutrientType other : NutrientType.VALUES)
        {
            if (other != type)
            {
                addNutrient(other, consumed * resupplyFraction * resupplyModifier());
            }
        }

        return consumed;
    }

    default boolean isMaxedOut()
    {
        return getNutrient(NITROGEN) == 1 && getNutrient(PHOSPHOROUS) == 1 && getNutrient(POTASSIUM) == 1;
    }

    default void saveNutrients(CompoundTag nbt)
    {
        nbt.putFloat("n", getNutrient(NITROGEN));
        nbt.putFloat("p", getNutrient(PHOSPHOROUS));
        nbt.putFloat("k", getNutrient(POTASSIUM));
    }

    default void loadNutrients(CompoundTag nbt)
    {
        setNutrient(NITROGEN, nbt.getFloat("n"));
        setNutrient(PHOSPHOROUS, nbt.getFloat("p"));
        setNutrient(POTASSIUM, nbt.getFloat("k"));
    }

    default void addTooltipInfo(List<Component> text)
    {
        text.add(Helpers.translatable("tfc.tooltip.farmland.nutrients", format(getNutrient(NutrientType.NITROGEN)), format(getNutrient(NutrientType.PHOSPHOROUS)), format(getNutrient(NutrientType.POTASSIUM))));
    }

    private String format(float value)
    {
        return String.format("%.2f", value * 100);
    }


}
