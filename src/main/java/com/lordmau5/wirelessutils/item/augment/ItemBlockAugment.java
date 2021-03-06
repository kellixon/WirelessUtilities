package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBlockAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICropAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IWorldAugmentable;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockAugment extends ItemAugment {
    public ItemBlockAugment() {
        super();
        setName("block_augment");
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getItemEnchantability() {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if ( ModConfig.augments.block.allowSilkTouch && enchantment == Enchantments.SILK_TOUCH )
            return true;

        if ( ModConfig.augments.block.allowFortune && enchantment == Enchantments.FORTUNE )
            return true;

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        boolean silky = false;
        int fortune = 0;
        if ( !stack.isEmpty() ) {
            silky = ModConfig.augments.block.allowSilkTouch && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
            fortune = ModConfig.augments.block.allowFortune ? EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) : 0;
        }

        if ( augmentable instanceof IBlockAugmentable )
            ((IBlockAugmentable) augmentable).setBlockAugmented(!stack.isEmpty(), silky, fortune);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IBlockAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ICropAugmentable && ((ICropAugmentable) augmentable).isCropAugmented() )
            return false;

        if ( augmentable instanceof IWorldAugmentable && ((IWorldAugmentable) augmentable).isWorldAugmented() )
            return false;

        return augmentable instanceof IBlockAugmentable;
    }
}
