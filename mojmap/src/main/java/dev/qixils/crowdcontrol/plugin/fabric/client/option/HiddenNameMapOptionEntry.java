package dev.qixils.crowdcontrol.plugin.fabric.client.option;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

public class HiddenNameMapOptionEntry<K, V> implements MapOptionEntry<K, V> {
    private final MapOptionEntry<K, V> option;

    public HiddenNameMapOptionEntry(MapOptionEntry<K, V> option) {
        this.option = option;
    }

    @Override
    public K getKey() {
        return option.getKey();
    }

    @Override
    public void setKey(K key) {
        option.setKey(key);
    }

    @Override
    public V getValue() {
        return option.getValue();
    }

    @Override
    public void setValue(V value) {
        option.setValue(value);
    }

    @Override
    public @NotNull Component name() {
        return Component.empty();
    }

    @Override
    public @NotNull OptionDescription description() {
        return option.description();
    }

    @Override
    @Deprecated
    public @NotNull Component tooltip() {
        return option.tooltip();
    }

    @Override
    public @NotNull StateManager<Map.Entry<K, V>> stateManager() {
        return option.stateManager();
    }

    @Override
    public @NotNull Controller<Map.Entry<K, V>> controller() {
        return option.controller();
    }

    @Override
    public @NotNull Binding<Map.Entry<K, V>> binding() {
        return option.binding();
    }

    @Override
    public boolean available() {
        return option.available();
    }

    @Override
    public void setAvailable(boolean available) {
        option.setAvailable(available);
    }

    @Override
    public MapOption<K, V> parentGroup() {
        return option.parentGroup();
    }

    @Override
    public @NotNull ImmutableSet<OptionFlag> flags() {
        return option.flags();
    }

    @Override
    public boolean changed() {
        return option.changed();
    }

    @Override
    public @NotNull Map.Entry<K, V> pendingValue() {
        return option.pendingValue();
    }

    @Override
    public void requestSet(@NotNull Map.Entry<K, V> value) {
        option.requestSet(value);
    }

    @Override
    public boolean applyValue() {
        return option.applyValue();
    }

    @Override
    public void forgetPendingValue() {
        option.forgetPendingValue();
    }

    @Override
    public void requestSetDefault() {
        option.requestSetDefault();
    }

    @Override
    public boolean isPendingValueDefault() {
        return option.isPendingValueDefault();
    }

    @Override
    public boolean canResetToDefault() {
        return option.canResetToDefault();
    }

    @Override
    @Deprecated
    public void addListener(BiConsumer<Option<Map.Entry<K, V>>, Map.Entry<K, V>> changedListener) {
        option.addListener(changedListener);
    }

    @Override
    public void addEventListener(OptionEventListener<Map.Entry<K, V>> listener) {
        option.addEventListener(listener);
    }
}
