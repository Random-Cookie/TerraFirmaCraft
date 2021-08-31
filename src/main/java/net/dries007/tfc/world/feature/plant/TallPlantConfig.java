/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature.plant;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dries007.tfc.world.Codecs;

public record TallPlantConfig(BlockState bodyState, BlockState headState, int tries, int radius, int minHeight, int maxHeight) implements FeatureConfiguration
{
    public static final Codec<TallPlantConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codecs.LENIENT_BLOCKSTATE.fieldOf("body").forGetter(c -> c.bodyState),
        Codecs.LENIENT_BLOCKSTATE.fieldOf("head").forGetter(c -> c.bodyState),
        Codec.intRange(1, 128).fieldOf("tries").forGetter(c -> c.tries),
        Codec.intRange(1, 16).fieldOf("radius").forGetter(c -> c.radius),
        Codec.intRange(1, 100).fieldOf("minHeight").forGetter(c -> c.minHeight),
        Codec.intRange(1, 100).fieldOf("maxHeight").forGetter(c -> c.maxHeight)
    ).apply(instance, TallPlantConfig::new));

}