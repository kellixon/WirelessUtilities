package com.lordmau5.wirelessutils.gui.client.condenser;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabEnergy;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.SharedState;
import com.lordmau5.wirelessutils.gui.client.elements.*;
import com.lordmau5.wirelessutils.gui.container.condenser.ContainerDirectionalCondenser;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityDirectionalCondenser;
import com.lordmau5.wirelessutils.utils.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class GuiDirectionalCondenser extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");
    public static final ItemStack BUCKET = new ItemStack(Items.BUCKET);

    private final TileEntityDirectionalCondenser condenser;

    private ElementDynamicContainedButton btnMode;
    private ElementRangeControls rangeControls;
    private ElementOffsetControls offsetControls;

    private FluidStack fluid;
    private TabWorkInfo workInfo;

    public GuiDirectionalCondenser(InventoryPlayer player, TileEntityDirectionalCondenser condenser) {
        super(new ContainerDirectionalCondenser(player, condenser), condenser, TEXTURE);
        this.condenser = condenser;

        generateInfo("tab." + WirelessUtils.MODID + ".directional_condenser");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 10, 26, condenser.getEnergyStorage()).setInfinite(condenser.isCreative()));
        addElement(new ElementAreaButton(this, condenser, 152, 69));
        addElement(new ElementModeButton(this, condenser, 134, 69));

        addTab(new TabEnergy(this, condenser, false));
        workInfo = (TabWorkInfo) addTab(new TabWorkInfo(this, condenser).setItem(BUCKET));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, condenser));
        addTab(new TabRoundRobin(this, condenser));

        btnMode = new ElementDynamicContainedButton(this, "Mode", 116, 69, 16, 16, Textures.SIZE);
        addElement(btnMode);

        rangeControls = new ElementRangeControls(this, condenser, 138, 18);
        addElement(rangeControls);

        offsetControls = new ElementOffsetControls(this, condenser, 138, 18);
        addElement(offsetControls);

        addElement(new ElementFluidTankCondenser(this, 34, 22, condenser).setAlwaysShow(true).setSmall().drawTank(true).setInfinite(condenser.isCreative()));

        addElement(new ElementFluidLock(this, condenser, 33, 58));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        switch (buttonName) {
            case "Mode":
                SharedState.offsetMode = !SharedState.offsetMode;
                break;
            default:
                return;
        }

        playClickSound(1F);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        boolean offsetMode = SharedState.offsetMode;

        btnMode.setIcon(offsetMode ? Textures.OFFSET : Textures.SIZE);
        btnMode.setToolTip("btn." + WirelessUtils.MODID + ".mode." + (offsetMode ? "offset" : "range"));
        btnMode.setVisible(condenser.getRange() > 0);

        rangeControls.setVisible(!offsetMode);
        offsetControls.setVisible(offsetMode);

        FluidStack fluid = condenser.getTankFluid();
        if ( (fluid == null && this.fluid == null) || (fluid != null && fluid.isFluidEqual(this.fluid)) )
            return;

        this.fluid = fluid;
        ItemStack stack = fluid == null ? null : FluidUtil.getFilledBucket(fluid);
        if ( stack == null || stack.isEmpty() )
            workInfo.setItem(BUCKET);
        else
            workInfo.setItem(stack);
    }
}
