package dev.qixils.crowdcontrol.plugin.fabric.client.option;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@ApiStatus.Internal
public final class MapOptionEntryImpl<K, V> implements MapOptionEntry<K, V> {
    private final MapOptionImpl<K, V> group;
    
    private K key;
    private V value;
    private final java.util.List<OptionEventListener<Map.Entry<K, V>>> listeners = new java.util.ArrayList<>();

    private final VirtualOption<K> keyOption;
    private final VirtualOption<V> valueOption;
    
    private final Controller<K> keyController;
    private final Controller<V> valueController;

    private final Controller<Map.Entry<K, V>> controller;

    MapOptionEntryImpl(MapOptionImpl<K, V> group, K initialKey, V initialValue, Function<Option<K>, Controller<K>> keyControlGetter, Function<Option<V>, Controller<V>> valueControlGetter) {
        this.group = group;
        this.key = initialKey;
        this.value = initialValue;

        this.keyOption = new VirtualOption<>(
            () -> this.key,
            (newKey) -> {
                if (group.hasKey(newKey) && group.getEntryForKey(newKey) != this) {
                    // Reject change
                    return;
                }
                this.key = newKey;
                for (OptionEventListener<Map.Entry<K, V>> listener : listeners) {
                    listener.onEvent(MapOptionEntryImpl.this, OptionEventListener.Event.STATE_CHANGE);
                }
                group.triggerListener(OptionEventListener.Event.STATE_CHANGE, true);
            }
        );

        this.valueOption = new VirtualOption<>(
            () -> this.value,
            (newValue) -> {
                this.value = newValue;
                for (OptionEventListener<Map.Entry<K, V>> listener : listeners) {
                    listener.onEvent(MapOptionEntryImpl.this, OptionEventListener.Event.STATE_CHANGE);
                }
                group.triggerListener(OptionEventListener.Event.STATE_CHANGE, true);
            }
        );

        this.keyController = keyControlGetter.apply(new HiddenNameVirtualOption<>(keyOption));
        this.valueController = valueControlGetter.apply(new HiddenNameVirtualOption<>(valueOption));
        
        this.controller = new EntryController<>(this);
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public void setKey(K key) {
        keyOption.requestSet(key);
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public void setValue(V value) {
        valueOption.requestSet(value);
    }

    @Override
    public @NotNull Component name() {
        return group.name();
    }

    @Override
    public @NotNull OptionDescription description() {
        return group.description();
    }

    @Override
    public @NotNull Component tooltip() {
        return group.tooltip();
    }

    @Override
    public @NotNull Controller<Map.Entry<K, V>> controller() {
        return controller;
    }

    @Override
    public @NotNull StateManager<Map.Entry<K, V>> stateManager() {
        throw new UnsupportedOperationException("MapOptionEntryImpl does not support state managers");
    }

    @Override
    public @NotNull Binding<Map.Entry<K, V>> binding() {
        return new Binding<Map.Entry<K, V>>() {
            @Override
            public void setValue(Map.Entry<K, V> value) {
                setKey(value.getKey());
                MapOptionEntryImpl.this.setValue(value.getValue());
            }

            @Override
            public Map.Entry<K, V> getValue() {
                return Map.entry(key, value);
            }

            @Override
            public Map.Entry<K, V> defaultValue() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean available() {
        return parentGroup().available();
    }

    @Override
    public void setAvailable(boolean available) {
    }

    @Override
    public MapOption<K, V> parentGroup() {
        return group;
    }

    @Override
    public boolean changed() {
        return group.changed();
    }

    @Override
    public @NotNull Map.Entry<K, V> pendingValue() {
        return Map.entry(key, value);
    }

    @Override
    public void requestSet(@NotNull Map.Entry<K, V> value) {
        binding().setValue(value);
    }

    @Override
    public boolean applyValue() {
        return group.applyValue();
    }

    @Override
    public void forgetPendingValue() {
    }

    @Override
    public void requestSetDefault() {
    }

    @Override
    public boolean isPendingValueDefault() {
        return false;
    }

    @Override
    public boolean canResetToDefault() {
        return false;
    }

    @Override
    public void addEventListener(OptionEventListener<Map.Entry<K, V>> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void addListener(BiConsumer<Option<Map.Entry<K, V>>, Map.Entry<K, V>> changedListener) {
    }

    @ApiStatus.Internal
    public record EntryController<K, V>(MapOptionEntryImpl<K, V> entry) implements Controller<Map.Entry<K, V>> {
        @Override
        public Option<Map.Entry<K, V>> option() {
            return entry;
        }

        @Override
        public Component formatValue() {
            return Component.empty();
        }

        @Override
        public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
            return new MapEntryWidget<>(screen, entry, entry.keyController.provideWidget(screen, widgetDimension.clone()), entry.valueController.provideWidget(screen, widgetDimension.clone()));
        }
    }

    private class VirtualOption<T> implements Option<T> {
        private final Binding<T> binding;
        
        VirtualOption(java.util.function.Supplier<T> getter, java.util.function.Consumer<T> setter) {
            this.binding = new Binding<T>() {
                @Override
                public void setValue(T value) {
                    setter.accept(value);
                }

                @Override
                public T getValue() {
                    return getter.get();
                }

                @Override
                public T defaultValue() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public @NotNull Component name() {
            return group.name();
        }

        @Override
        public @NotNull OptionDescription description() {
            return group.description();
        }

        @Override
        public @NotNull Component tooltip() {
            return group.tooltip();
        }

        @Override
        public @NotNull Controller<T> controller() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull StateManager<T> stateManager() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Binding<T> binding() {
            return binding;
        }

        @Override
        public boolean available() {
            return group.available();
        }

        @Override
        public void setAvailable(boolean available) {
        }



        @Override
        public @NotNull ImmutableSet<OptionFlag> flags() {
            return group.flags();
        }

        @Override
        public boolean changed() {
            return false;
        }

        @Override
        public @NotNull T pendingValue() {
            return binding.getValue();
        }

        @Override
        public void requestSet(@NotNull T value) {
            binding.setValue(value);
        }

        @Override
        public boolean applyValue() {
            return false;
        }

        @Override
        public void forgetPendingValue() {
        }

        @Override
        public void requestSetDefault() {
        }

        @Override
        public boolean isPendingValueDefault() {
            return false;
        }

        @Override
        public boolean canResetToDefault() {
            return false;
        }

        @Override
        public void addEventListener(OptionEventListener<T> listener) {
        }

        @Override
        public void addListener(BiConsumer<Option<T>, T> changedListener) {
        }
    }
    
    private static class HiddenNameVirtualOption<T> implements Option<T> {
        private final Option<T> option;

        public HiddenNameVirtualOption(Option<T> option) {
            this.option = option;
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
        public @NotNull Component tooltip() {
            return option.tooltip();
        }

        @Override
        public @NotNull Controller<T> controller() {
            return option.controller();
        }

        @Override
        public @NotNull StateManager<T> stateManager() {
            return option.stateManager();
        }

        @Override
        public @NotNull Binding<T> binding() {
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
        public @NotNull ImmutableSet<OptionFlag> flags() {
            return option.flags();
        }

        @Override
        public boolean changed() {
            return option.changed();
        }

        @Override
        public @NotNull T pendingValue() {
            return option.pendingValue();
        }

        @Override
        public void requestSet(@NotNull T value) {
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
        public void addEventListener(OptionEventListener<T> listener) {
            option.addEventListener(listener);
        }

        @Override
        public void addListener(BiConsumer<Option<T>, T> changedListener) {
            option.addListener(changedListener);
        }
    }
}
