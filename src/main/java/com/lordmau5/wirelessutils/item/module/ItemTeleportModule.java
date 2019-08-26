package com.lordmau5.wirelessutils.item.module;

import com.lordmau5.wirelessutils.gui.client.modules.ElementTeleportModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.TeleportUtils;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTeleportModule extends ItemFilteringModule {

    public ItemTeleportModule() {
        super();
        setName("teleport_module");
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.vaporizers.modules.teleport.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.teleport.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.teleport.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.teleport.energyDrain;
    }

    @Nullable
    @Override
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new TeleportBehavior(vaporizer, stack);
    }

    public static class TeleportBehavior extends FilteredBehavior {

        private final ItemStack INPUT_GHOST;
        private final ItemStack GHOST;

        private BlockPosDimension target;
        private boolean isInterdimensional = false;
        private int range = 0;
        private int cost = 0;
        private int fuel = 0;

        public TeleportBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack stack) {
            super(vaporizer);
            INPUT_GHOST = new ItemStack(ModItems.itemRangeAugment);
            GHOST = new ItemStack(ModItems.itemAbsolutePositionalCard);

            allowPlayers = ModConfig.vaporizers.modules.teleport.targetPlayers;
            allowCreative = true;

            requireAttackable = false;
            obeyItemTags = true;

            updateModule(stack);
            updateRange();
            updateModifier(vaporizer.getModifier());
        }

        public boolean isInterdimensional() {
            return isInterdimensional;
        }

        public int getRange() {
            return range;
        }

        public int getFuel() {
            return fuel;
        }

        public double getEnergyMultiplier() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 1;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyMultiplier(stack, vaporizer);
        }

        public int getEnergyAddition() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 0;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyAddition(stack, vaporizer);
        }

        public int getEnergyDrain() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 0;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEneryDrain(stack, vaporizer);
        }

        public void updateRange() {
            ItemStack stack = vaporizer.getInput().getStackInSlot(0);

            isInterdimensional = ModItems.itemRangeAugment.isInterdimensional(stack);
            range = ModItems.itemRangeAugment.getPositionalRange(stack);
        }

        public void updateModifier(@Nonnull ItemStack stack) {
            // We don't want to have a target if we don't have our own position.
            BlockPosDimension pos = vaporizer.getPosition();
            if ( pos == null )
                target = null;
            else {
                if ( isValidModifier(stack) ) {
                    ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
                    target = card.getTarget(stack, pos);
                } else
                    target = pos.offset(vaporizer.getEnumFacing().getOpposite(), 1);
            }

            double fuel = ModConfig.vaporizers.modules.teleport.fuelInterdimensional;
            cost = ModConfig.vaporizers.modules.teleport.costInterdimensional;
            if ( target != null && target.getDimension() == pos.getDimension() ) {
                int dimCost = 0;
                double fuelCost = 0;
                double distance = pos.distanceSq(target);
                if ( distance > 0 ) {
                    dimCost = (int) Math.floor(
                            (ModConfig.vaporizers.modules.teleport.costPerBlock * Math.sqrt(distance)) +
                                    (ModConfig.vaporizers.modules.teleport.costPerBlockSquared * distance)
                    );

                    fuelCost = (ModConfig.vaporizers.modules.teleport.fuelPerBlock * Math.sqrt(distance)) +
                            (ModConfig.vaporizers.modules.teleport.fuelPerBlockSquared * distance);
                }

                if ( dimCost < cost )
                    cost = dimCost;

                if ( fuelCost < fuel || fuel == 0 )
                    fuel = fuelCost;
            }

            cost += ModConfig.vaporizers.modules.teleport.baseEnergy;
            this.fuel = (int) Math.ceil(fuel + ModConfig.vaporizers.modules.teleport.baseFuel);
        }

        @Nullable
        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementTeleportModule(gui, this);
        }

        @Override
        public Class<? extends Entity> getEntityClass() {
            if ( ModConfig.vaporizers.modules.teleport.livingOnly )
                return EntityLivingBase.class;

            return Entity.class;
        }

        public boolean isPositionalCardValid(@Nonnull ItemStack stack) {
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemBasePositionalCard) )
                return false;

            ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
            return card.isCardConfigured(stack);
        }

        public boolean isTargetInRange(BlockPosDimension target) {
            return isTargetInRange(target, range, isInterdimensional);
        }

        public boolean isTargetInRange(BlockPosDimension target, int range, boolean interdimensional) {
            if ( interdimensional )
                return true;

            BlockPosDimension origin = vaporizer.getPosition();
            if ( origin == null || origin.getDimension() != target.getDimension() )
                return false;

            return Math.floor(Math.sqrt(origin.distanceSq(target))) <= range;
        }

        public boolean isTargetInRange(@Nonnull ItemStack stack) {
            return isTargetInRange(stack, range, isInterdimensional);
        }

        public boolean isTargetInRange(@Nonnull ItemStack stack, int range, boolean interdimensional) {
            ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
            if ( card.shouldIgnoreDistance(stack) )
                return true;

            // We need a world~
            BlockPosDimension pos = vaporizer.getPosition();
            if ( pos == null )
                return false;

            return isTargetInRange(card.getTarget(stack, pos), range, interdimensional);
        }

        public boolean isInputUnlocked(int slot) {
            if ( slot == 0 ) {
                ItemStack modifier = vaporizer.getModifier();
                if ( isPositionalCardValid(modifier) )
                    return isTargetInRange(modifier, ModItems.itemRangeAugment.getPositionalRange(ItemStack.EMPTY), false);
            }

            return usesFuel();
        }

        @Override
        public int getInputLimit(int slot) {
            if ( slot == 0 )
                return 1;

            return 64;
        }

        public boolean isValidBall(@Nonnull ItemStack stack) {
            if ( !EntityUtilities.isFilledEntityBall(stack) )
                return false;

            int value = EntityUtilities.getBaseExperience(stack, vaporizer.getWorld());
            if ( EntityUtilities.containsBabyEntity(stack) )
                value = (int) Math.floor(value * ModConfig.vaporizers.babyMultiplier);

            return value > 0;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, int slot) {
            if ( slot == 0 ) {
                if ( stack.getItem() != ModItems.itemRangeAugment )
                    return false;

                return vaporizer.getLevel().toInt() >=
                        ModItems.itemRangeAugment.getRequiredLevel(stack).toInt();
            }

            if ( !usesFuel() )
                return false;

            return EntityUtilities.canEmptyBall(stack) && isValidBall(stack);
        }

        @Override
        public void updateInput(int slot) {
            if ( slot == 0 )
                updateRange();
        }

        @Nonnull
        @Override
        public ItemStack getInputGhost(int slot) {
            if ( slot == 0 )
                return INPUT_GHOST;

            return ItemStack.EMPTY;
        }

        public boolean isModifierUnlocked() {
            return true;
        }

        @Nonnull
        public ItemStack getModifierGhost() {
            return GHOST;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            return isPositionalCardValid(stack) && isTargetInRange(stack);
        }

        public boolean wantsFluid() {
            return usesFuel();
        }

        public static boolean usesFuel() {
            return ModConfig.vaporizers.modules.teleport.baseFuel > 0 ||
                    ModConfig.vaporizers.modules.teleport.fuelPerBlockSquared > 0 ||
                    ModConfig.vaporizers.modules.teleport.fuelPerBlock > 0 ||
                    ModConfig.vaporizers.modules.teleport.fuelInterdimensional > 0;
        }

        public int getExperienceMode() {
            return 0;
        }

        public int getDropMode() {
            return 0;
        }

        @Override
        public boolean canRun(boolean ignorePower) {
            if ( !super.canRun(ignorePower) )
                return false;

            // This should only happen when the machine is loading.
            if ( target == null ) {
                updateModifier(vaporizer.getModifier());
                if ( target == null )
                    return false;
            }

            return ignorePower || vaporizer.getEnergyStored() >= cost;
        }

        public int getBlockEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return cost;
        }

        public int getEntityEnergyCost(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return 0;
        }

        public int getMaxEntityEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return 0;
        }

        public int getActionCost() {
            return ModConfig.vaporizers.modules.teleport.budget;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget vaporizerTarget) {
            World world = entity.world;
            if ( world == null || entity.timeUntilPortal > 0 || target == null )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            int removed = fuel == 0 ? 0 : vaporizer.removeFuel(fuel);
            if ( removed < fuel ) {
                if ( removed > 0 )
                    vaporizer.addFuel(fuel);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            Entity newEntity = TeleportUtils.teleportEntity(entity, target.getDimension(), target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            if ( newEntity == null ) {
                if ( removed > 0 )
                    vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_REMOVE;
            }

            if ( removed > fuel )
                vaporizer.addFuel(removed - fuel);

            newEntity.timeUntilPortal = newEntity.getPortalCooldown();
            newEntity.fallDistance = 0;
            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}
