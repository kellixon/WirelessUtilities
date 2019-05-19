package com.lordmau5.wirelessutils.item.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

public interface IExplainableItem {

    default void addExplanation(@Nonnull List<String> tooltip, @Nonnull String name, Object... args) {
        String path = name + ".0";
        if ( StringHelper.canLocalize(path) ) {
            if ( StringHelper.isShiftKeyDown() )
                addLocalizedLines(tooltip, name, args);
            else
                tooltip.add(StringHelper.shiftForDetails());
        }
    }

    default void addLocalizedLines(@Nonnull List<String> tooltip, @Nonnull String name, Object... args) {
        int i = 0;
        String path = name + "." + i;
        int blank = 0;

        while ( StringHelper.canLocalize(path) ) {
            ITextComponent component = new TextComponentTranslation(path, args).setStyle(TextHelpers.GRAY);
            if ( component.getUnformattedText().isEmpty() )
                blank++;
            else {
                while ( blank > 0 ) {
                    tooltip.add("");
                    blank--;
                }

                tooltip.add(component.getFormattedText());
            }

            i++;
            path = name + "." + i;
        }
    }

    default void addLocalizedLines(@Nonnull List<String> tooltip, @Nonnull String name, Style style, Object... args) {
        int i = 0;
        int blank = 0;
        String path = name + "." + i;
        while ( StringHelper.canLocalize(path) ) {
            ITextComponent component = new TextComponentTranslation(path, args).setStyle(style);
            if ( component.getUnformattedText().isEmpty() )
                blank++;
            else {
                while ( blank > 0 ) {
                    tooltip.add("");
                    blank--;
                }

                tooltip.add(component.getFormattedText());
            }

            i++;
            path = name + "." + i;
        }
    }

}
