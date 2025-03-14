package com.synopsys.integration.configuration.property.types.enumallnone.property;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.configuration.property.types.enumallnone.enumeration.AllEnum;
import com.synopsys.integration.configuration.property.types.enumallnone.list.AllEnumList;
import com.synopsys.integration.configuration.property.types.enumextended.ExtendedEnumListPropertyBase;
import com.synopsys.integration.configuration.property.types.enumextended.ExtendedEnumValue;

public class AllEnumListProperty<B extends Enum<B>> extends ExtendedEnumListPropertyBase<AllEnum, B, AllEnumList<B>> {
    public AllEnumListProperty(@NotNull String key, List<ExtendedEnumValue<AllEnum, B>> defaultValue, @NotNull Class<B> eClass) {
        super(key, defaultValue, AllEnum.class, eClass);
    }

    public AllEnumListProperty(@NotNull String key, @NotNull AllEnum allValue, @NotNull Class<B> eClass) {
        super(key, Collections.singletonList(ExtendedEnumValue.ofExtendedValue(allValue)), AllEnum.class, eClass);
    }

    public AllEnumListProperty(@NotNull String key, @NotNull B extendedValue, @NotNull Class<B> eClass) {
        super(key, Collections.singletonList(ExtendedEnumValue.ofBaseValue(extendedValue)), AllEnum.class, eClass);
    }

    public AllEnumList<B> toList(List<ExtendedEnumValue<AllEnum, B>> values) {
        return new AllEnumList<>(values, bClass);
    }

    @Override
    @NotNull
    public AllEnumList<B> convertValue(List<ExtendedEnumValue<AllEnum, B>> value) {
        return new AllEnumList<>(value, bClass);
    }
}
