package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.IUpgradeable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ItemBaseUpgrade extends ItemBase {

    public void onUpgradeInstalled(@Nullable EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IUpgradeable tile, @Nullable EnumFacing side, @Nonnull ItemStack stack) {

    }

    @Override
    @Nonnull
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity tile = world.getTileEntity(pos);
        if ( tile == null )
            return EnumActionResult.PASS;

        if ( tile instanceof IUpgradeable ) {
            IUpgradeable upgradeable = (IUpgradeable) tile;
            if ( !upgradeable.canUpgrade(stack) )
                return EnumActionResult.PASS;

            if ( !world.isRemote ) {
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                ITextComponent blockName = new TextComponentTranslation(block.getTranslationKey() + ".name");

                ItemStack clone = stack.copy();
                if ( clone.getCount() > 1 )
                    clone.setCount(1);

                if ( upgradeable.installUpgrade(clone) ) {
                    if ( !player.capabilities.isCreativeMode )
                        stack.shrink(1);

                    onUpgradeInstalled(player, world, pos, upgradeable, side, stack);

                    player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.6F, 1.0F);
                    player.sendMessage(new TextComponentTranslation(
                            "chat." + WirelessUtils.MODID + ".upgrade.success",
                            clone.getTextComponent(),
                            blockName
                    ));
                } else
                    player.sendMessage(new TextComponentTranslation(
                            "chat." + WirelessUtils.MODID + ".upgrade.failed",
                            clone.getTextComponent(),
                            blockName
                    ));
            }

            return EnumActionResult.SUCCESS;
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }
}
