package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IWorkProvider<T extends TargetInfo> extends ITargetProvider {
    /**
     * Get the location of the work provider, for use in
     * distance calculations.
     */
    BlockPosDimension getPosition();

    /**
     * Get the currently configured iteration mode of the work provider.
     */
    IterationMode getIterationMode();

    /**
     * Set the iteration mode of the work provider.
     *
     * @param mode The new iteration mode.
     */
    void setIterationMode(IterationMode mode);

    /**
     * Whether or not blocks can potentially be handled no
     * matter if they have an associated tile entity or not.
     */
    boolean shouldProcessBlocks();

    /**
     * Whether or not tile entities can potentially be handled.
     */
    boolean shouldProcessTiles();

    /**
     * Whether or not inventory contents can potentially be handled.
     */
    boolean shouldProcessItems();

    /**
     * Create an instance of TargetInfo for the given target.
     * This is called when unable to perform work on a block itself,
     * but able to perform work on an item within that block.
     *
     * @param target The location of the prospective target.
     * @param source The ItemStack of the positional card responsible for this target. EMPTY if not applicable.
     * @param world  The world the target is in.
     * @param block  The block state of the prospective target. May be null if getProcessBlocks returned false.
     * @param tile   A TileEntity found at that location that should be stored. May be null.
     * @return
     */
    T createInfo(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile);

    /**
     * Determine whether or not we can perform work on the provided block.
     *
     * @param target The location of the prospective target.
     * @param source The ItemStack of the positional card responsible for this target. EMPTY if not applicable.
     * @param world  The world the target is in.
     * @param block  The block state of the prospective target.
     * @param tile   The tile entity of the prospective target. May be null.
     */
    boolean canWorkBlock(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState block, @Nullable TileEntity tile);

    /**
     * Determine whether or not we can perform work on the provided tile entity.
     *
     * @param target The location of the prospective target.
     * @param source The ItemStack of the positional card responsible for this target. EMPTY if not applicable.
     * @param world  The world the target is in.
     * @param block  The block state of the prospective target. May be null.
     * @param tile   The tile entity of the prospective target.
     */
    boolean canWorkTile(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nonnull TileEntity tile);

    /**
     * Determine whether or not we can perform work on the provided
     * item stack.
     *
     * @param stack     The item stack.
     * @param slot      Which slot of the inventory it's in.
     * @param inventory The inventory containing the item stack.
     * @param target    The location of the target containing the item stack.
     * @param source    The ItemStack of the positional card responsible for this target. EMPTY if not applicable.
     * @param world     The world the target is in.
     * @param block     The block state of the target containing the item stack. May be null.
     * @param tile      The tile entity of the target containing the item stack.
     * @return True if we should try working on that item stack.
     */
    boolean canWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nonnull TileEntity tile);

    /**
     * Attempt to perform work on the provided block.
     *
     * @param target The TargetInfo returned from canWork.
     * @param world  The world the target is in.
     * @param state  The block state of the target. This may be
     *               null for performance reasons, requiring you
     *               to look up the blockstate yourself.
     * @param tile   The tile entity of the target. May be null.
     * @return
     */
    @Nonnull
    WorkResult performWorkBlock(@Nonnull T target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile);

    /**
     * Attempt to perform work on the provided tile.
     *
     * @param target The TargetInfo returned from canWork.
     * @param world  The world the target is in.
     * @param state  The block state of the target. May be null.
     * @param tile   The tile entity of the target.
     * @return
     */
    @Nonnull
    WorkResult performWorkTile(@Nonnull T target, @Nonnull World world, @Nullable IBlockState state, @Nonnull TileEntity tile);

    /**
     * Attempt to perform work on the provided item stack for
     * inventory processing.
     *
     * @param stack     The item stack to process.
     * @param slot      The slot the item stack is in.
     * @param inventory The inventory containing the item stack.
     * @param target    A TargetInfo instance for the target containing this item.
     * @param world     The world the target is in.
     * @param state     The block state of the target's containing block. May be null.
     * @param tile      The tile entity of the target's containing block.
     * @return
     */
    @Nonnull
    WorkResult performWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull T target, @Nonnull World world, @Nullable IBlockState state, @Nonnull TileEntity tile);


    enum IterationMode {
        ROUND_ROBIN,
        NEAREST_FIRST,
        FURTHEST_FIRST,
        RANDOM;

        public static IterationMode fromInt(int index) {
            IterationMode[] values = values();
            if ( index < 0 )
                return values[0];
            if ( index >= values.length )
                return values[values.length - 1];
            return values[index];
        }

        public IterationMode next() {
            if ( this == ROUND_ROBIN )
                return NEAREST_FIRST;
            else if ( this == NEAREST_FIRST )
                return FURTHEST_FIRST;
            else if ( this == FURTHEST_FIRST )
                return RANDOM;

            return ROUND_ROBIN;
        }

        public IterationMode previous() {
            if ( this == ROUND_ROBIN )
                return RANDOM;
            else if ( this == NEAREST_FIRST )
                return ROUND_ROBIN;
            else if ( this == FURTHEST_FIRST )
                return NEAREST_FIRST;

            return FURTHEST_FIRST;
        }
    }

    enum WorkResult {
        /**
         * This work item was skipped and should not count against the maximum
         * operations per tick.
         */
        SKIPPED(0, false, true, false),

        /**
         * Work was a success. This operation should count against the maximum
         * operations per tick and work processing should continue.
         */
        SUCCESS_CONTINUE(1, true, true, false),

        /**
         * Work was a failure. This operation should count against the maximum
         * operations per tick and work processing should continue.
         */
        FAILURE_CONTINUE(1, false, true, false),

        /**
         * Work was a success. Work can no longer be performed on this target
         * and the target should be removed from the work list.
         */
        SUCCESS_REMOVE(1, true, true, true),

        /**
         * Work was a failure. Work is not currently possible on this target
         * and the target should be removed from the work list.
         */
        FAILURE_REMOVE(1, false, true, true),

        /**
         * Work was a success. We should stop working though, so stop iterating.
         */
        SUCCESS_STOP(1, true, false, false),

        /**
         * Work was a failure. We should also stop working, so stop iterating.
         */
        FAILURE_STOP(1, false, false, false),

        SUCCESS_STOP_REMOVE(1, true, false, true),
        FAILURE_STOP_REMOVE(1, false, false, true);

        public final int cost;
        public final boolean success;
        public final boolean keepProcessing;
        public final boolean remove;

        WorkResult(int cost, boolean success, boolean keepProcessing, boolean remove) {
            this.cost = cost;
            this.success = success;
            this.keepProcessing = keepProcessing;
            this.remove = remove;
        }
    }
}
