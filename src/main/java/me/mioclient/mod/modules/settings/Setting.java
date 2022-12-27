package me.mioclient.mod.modules.settings;

import me.mioclient.api.events.impl.ClientEvent;
import me.mioclient.mod.Mod;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;
import java.util.function.Predicate;

public class Setting<T> {

    private final String name;
    private Predicate<T> visibility;
    private Mod mod;

    private final T defaultValue;
    private T value, plannedValue, minValue, maxValue;

    private boolean restriction;
    public boolean open, parent, hideAlpha, hasBoolean, booleanValue;

    //Settings

    public Setting(String nameIn, T defaultValueIn, T minValueIn, T maxValueIn, Predicate<T> visibilityIn) {
        name = nameIn;
        defaultValue = defaultValueIn;
        value = defaultValueIn;
        minValue = minValueIn;
        maxValue = maxValueIn;
        plannedValue = defaultValueIn;
        visibility = visibilityIn;

        restriction = true;
    }

    public Setting(String nameIn, T defaultValueIn, Predicate<T> visibilityIn) {
        name = nameIn;
        defaultValue = defaultValueIn;
        value = defaultValueIn;
        plannedValue = defaultValueIn;
        visibility = visibilityIn;
    }

    public Setting(String nameIn, T defaultValueIn, T minValueIn, T maxValueIn) {
        name = nameIn;
        defaultValue = defaultValueIn;
        value = defaultValueIn;
        minValue = minValueIn;
        maxValue = maxValueIn;
        plannedValue = defaultValueIn;

        restriction = true;
    }

    public Setting(String nameIn, T defaultValueIn) {
        name = nameIn;
        defaultValue = defaultValueIn;
        value = defaultValueIn;
        plannedValue = defaultValueIn;
    }

    //Getters

    public T getValue() {
        return value;
    }

    public T getPlannedValue() {
        return plannedValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getMinValue() {
        return minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public String getName() {
        return name;
    }

    public Mod getMod() {
        return mod;
    }

    public String getCurrentEnumName() {
        return EnumConverter.getProperName((Enum) value);
    }

    public <T> String getClassName(T value) {
        return value.getClass().getSimpleName();
    }

    public String getType() {
        if (isEnumSetting()) {
            return "Enum";
        }

        return getClassName(defaultValue);
    }

    //General setters

    public void setValue(T valueIn) {
        setPlannedValue(valueIn);

        if (restriction) {

            if (((Number) minValue).floatValue() > ((Number) valueIn).floatValue()) {
                setPlannedValue(minValue);
            }

            if (((Number) maxValue).floatValue() < ((Number) valueIn).floatValue()) {
                setPlannedValue(maxValue);
            }
        }

        ClientEvent event = new ClientEvent(this);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            value = plannedValue;

        } else {
            plannedValue = value;
        }
    }

    public void setPlannedValue(T valueIn) {
        plannedValue = valueIn;
    }

    public Setting<T> setParent() {
        parent = true;

        return this;
    }

    public void setMod(Mod modIn) {
        mod = modIn;
    }

    //Enum setters

    public void setEnumValue(String value) {
        for (Enum e : ((Enum) this.value).getClass().getEnumConstants()) {

            if (!e.name().equalsIgnoreCase(value)) continue;

            this.value = (T) e;
        }
    }

    public void increaseEnum() {
        plannedValue = (T) EnumConverter.increaseEnum((Enum) value);

        ClientEvent event = new ClientEvent(this);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            value = plannedValue;

        } else {
            plannedValue = value;
        }
    }

    //Color picker setters

    /**
     * injectBoolean() is for adding a boolean value to a color picker (i.e. to add an option to enable/disable a color).
     * @param valueIn is the boolean value that'll be set by default.
     */

    public Setting<T> injectBoolean(boolean valueIn) {
        if (value instanceof Color) {
            hasBoolean = true;
            booleanValue = valueIn;
        }

        return this;
    }

    /**
     * hideAlpha() is for disabling the alpha slider in color pickers which don't need it.
     */

    public Setting<T> hideAlpha() {
        hideAlpha = true;

        return this;
    }

    //Checkers

    public boolean isNumberSetting() {
        return value instanceof Double
                || value instanceof Integer
                || value instanceof Short
                || value instanceof Long
                || value instanceof Float;
    }

    public boolean isEnumSetting() {
        return !isNumberSetting()
                && !(value instanceof String)
                && !(value instanceof Bind)
                && !(value instanceof Character)
                && !(value instanceof Boolean)
                && !(value instanceof Color);
    }

    public boolean isStringSetting() {
        return value instanceof String;
    }

    public boolean isVisible() {
        if (visibility == null) {
            return true;
        }

        return visibility.test(getValue());
    }

    public boolean isOpen() {
        return (open && parent);
    }

    public boolean hasRestriction() {
        return restriction;
    }
}