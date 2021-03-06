package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBlockAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICropAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IWorldAugmentable;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityBaseCondenser;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemWorldAugment extends ItemAugment {
    public ItemWorldAugment() {
        super();
        setName("world_augment");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IWorldAugmentable )
            ((IWorldAugmentable) augmentable).setWorldAugmented(!stack.isEmpty());
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        if ( TileBaseDesublimator.class.isAssignableFrom(klass) ) {
            if ( !ModConfig.desublimators.allowWorldAugment )
                return false;
        } else if ( TileEntityBaseCondenser.class.isAssignableFrom(klass) ) {
            if ( !ModConfig.condensers.allowWorldAugment )
                return false;
        }

        return IWorldAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ICropAugmentable && ((ICropAugmentable) augmentable).isCropAugmented() )
            return false;

        if ( augmentable instanceof IBlockAugmentable && ((IBlockAugmentable) augmentable).isBlockAugmented() )
            return false;

        return augmentable instanceof IWorldAugmentable;
    }
}
