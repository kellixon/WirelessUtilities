package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.item.ItemStack;

public interface IPositionalMachine {

    boolean isInterdimensional();

    int getRange();

    BlockPosDimension getPosition();

    default boolean isPositionalCardValid(ItemStack stack) {
        if ( stack.isEmpty() || !(stack.getItem() instanceof ItemBasePositionalCard) )
            return false;

        ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
        return card.isCardConfigured(stack);
    }

    default boolean isTargetInRange(BlockPosDimension target) {
        return isTargetInRange(target, getRange(), isInterdimensional());
    }

    default boolean isTargetInRange(BlockPosDimension target, int range, boolean interdimensional) {
        if ( interdimensional )
            return true;

        BlockPosDimension origin = getPosition();
        if ( origin.getDimension() != target.getDimension() )
            return false;

        return Math.floor(Math.sqrt(target.distanceSq(target))) <= range;
    }

}
