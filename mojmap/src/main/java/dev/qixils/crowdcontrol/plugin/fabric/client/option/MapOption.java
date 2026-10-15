package dev.qixils.crowdcontrol.plugin.fabric.client.option;

import com.google.common.collect.ImmutableList;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MapOption<K, V> extends OptionGroup, Option<Map<K, V>> {
    @Override
    @NotNull ImmutableList<MapOptionEntry<K, V>> options();

    @ApiStatus.Internal
    int numberOfEntries();

    @ApiStatus.Internal
    int maximumNumberOfEntries();

    @ApiStatus.Internal
    int minimumNumberOfEntries();

    @ApiStatus.Internal
    MapOptionEntry<K, V> insertNewEntry();

    @ApiStatus.Internal
    void insertEntry(int index, MapOptionEntry<?, ?> entry);

    @ApiStatus.Internal
    int indexOf(MapOptionEntry<?, ?> entry);

    @ApiStatus.Internal
    void removeEntry(MapOptionEntry<?, ?> entry);

    @ApiStatus.Internal
    void addRefreshListener(Runnable changedListener);

    static <K, V> Builder<K, V> createBuilder() {
        return new MapOptionImpl.BuilderImpl<>();
    }

    interface Builder<K, V> {
        Builder<K, V> name(@NotNull Component name);

        Builder<K, V> description(@NotNull OptionDescription description);

        Builder<K, V> initialKey(@NotNull Supplier<K> initialValue);
        Builder<K, V> initialKey(@NotNull K initialValue);

        Builder<K, V> initialValue(@NotNull Supplier<V> initialValue);
        Builder<K, V> initialValue(@NotNull V initialValue);

        Builder<K, V> keyController(@NotNull Function<Option<K>, ControllerBuilder<K>> controller);
        Builder<K, V> valueController(@NotNull Function<Option<V>, ControllerBuilder<V>> controller);

        Builder<K, V> customKeyController(@NotNull Function<Option<K>, Controller<K>> control);
        Builder<K, V> customValueController(@NotNull Function<Option<V>, Controller<V>> control);

        Builder<K, V> state(@NotNull StateManager<Map<K, V>> stateManager);

        Builder<K, V> binding(@NotNull Binding<Map<K, V>> binding);
        Builder<K, V> binding(@NotNull Map<K, V> def, @NotNull Supplier<@NotNull Map<K, V>> getter, @NotNull Consumer<@NotNull Map<K, V>> setter);

        Builder<K, V> available(boolean available);

        Builder<K, V> minimumNumberOfEntries(int number);
        Builder<K, V> maximumNumberOfEntries(int number);
        Builder<K, V> insertEntriesAtEnd(boolean insertAtEnd);

        Builder<K, V> flag(@NotNull OptionFlag... flag);
        Builder<K, V> flags(@NotNull Collection<OptionFlag> flags);

        Builder<K, V> collapsed(boolean collapsible);

        MapOption.Builder<K, V> addListener(@NotNull OptionEventListener<Map<K, V>> listener);
        MapOption.Builder<K, V> addListeners(@NotNull Collection<OptionEventListener<Map<K, V>>> listeners);

        MapOption.Builder<K, V> listener(@NotNull BiConsumer<Option<Map<K, V>>, Map<K, V>> listener);
        MapOption.Builder<K, V> listeners(@NotNull Collection<BiConsumer<Option<Map<K, V>>, Map<K, V>>> listeners);

        MapOption<K, V> build();
    }
}
