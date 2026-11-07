package dev.qixils.crowdcontrol.plugin.fabric.client.option;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class MapOptionImpl<K, V> implements MapOption<K, V> {
    private final Component name;
    private final OptionDescription description;
    private final Binding<Map<K, V>> binding;
    private final Supplier<? extends Iterable<K>> initialKeys;
    private final Supplier<V> initialValue;
    private final List<MapOptionEntry<K, V>> entries;
    private final boolean collapsed;
    private boolean available;
    private final int minimumNumberOfEntries;
    private final int maximumNumberOfEntries;
    private final boolean insertEntriesAtEnd;
    private final ImmutableSet<OptionFlag> flags;
    private final EntryFactory entryFactory;

    private final List<BiConsumer<Option<Map<K, V>>, Map<K, V>>> listeners;
    private final List<Runnable> refreshListeners;
    private int currentListenerDepth = 0;

    public MapOptionImpl(@NotNull Component name, @NotNull OptionDescription description, @NotNull Binding<Map<K, V>> binding, @NotNull Supplier<? extends Iterable<K>> initialKeys, @NotNull Supplier<V> initialValue, @NotNull Function<Option<K>, Controller<K>> keyControllerFunction, @NotNull Function<Option<V>, Controller<V>> valueControllerFunction, ImmutableSet<OptionFlag> flags, boolean collapsed, boolean available, int minimumNumberOfEntries, int maximumNumberOfEntries, boolean insertEntriesAtEnd, Collection<BiConsumer<Option<Map<K, V>>, Map<K, V>>> listeners) {
        this.name = name;
        this.description = description;
        this.binding = binding;
        this.initialKeys = initialKeys;
        this.initialValue = initialValue;
        this.entryFactory = new EntryFactory(keyControllerFunction, valueControllerFunction);
        this.entries = createEntries(binding().getValue());
        this.collapsed = collapsed;
        this.flags = flags;
        this.available = available;
        this.minimumNumberOfEntries = minimumNumberOfEntries;
        this.maximumNumberOfEntries = maximumNumberOfEntries;
        this.insertEntriesAtEnd = insertEntriesAtEnd;
        this.listeners = new ArrayList<>();
        this.listeners.addAll(listeners);
        this.refreshListeners = new ArrayList<>();
    }

    @Override
    public @NotNull Component name() {
        return this.name;
    }

    @Override
    public @NotNull OptionDescription description() {
        return this.description;
    }

    @Override
    public @NotNull Component tooltip() {
        return description().text();
    }

    @Override
    public @NotNull ImmutableList<MapOptionEntry<K, V>> options() {
        ImmutableList.Builder<MapOptionEntry<K, V>> builder = ImmutableList.builder();
        builder.addAll(entries);
        builder.add(new AddMapOptionEntryImpl<>(this));
        return builder.build();
    }

    public static void attemptRefresh(dev.isxander.yacl3.gui.YACLScreen screen) {
        try {
            for (var child : screen.children()) {
                if (child.getClass().getSimpleName().equals("CategoryTab")) {
                    for (var r : ((net.minecraft.client.gui.components.events.ContainerEventHandler) child).children()) {
                        if (r instanceof dev.isxander.yacl3.gui.OptionListWidget list) {
                            list.refreshOptions();
                            return;
                        }
                    }
                }
                if (child instanceof dev.isxander.yacl3.gui.tab.ListHolderWidget<?> holder && holder.getList() instanceof dev.isxander.yacl3.gui.OptionListWidget list) {
                    list.refreshOptions();
                    return;
                }
                if (child instanceof dev.isxander.yacl3.gui.OptionListWidget list) {
                    list.refreshOptions();
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public @NotNull Controller<Map<K, V>> controller() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Binding<Map<K, V>> binding() {
        return binding;
    }

    @Override
    public boolean collapsed() {
        return collapsed;
    }

    @Override
    public @NotNull ImmutableSet<OptionFlag> flags() {
        return flags;
    }

    @Override
    public @NotNull Map<K, V> pendingValue() {
        Map<K, V> map = new LinkedHashMap<>();
        for (MapOptionEntry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @Override
    public void insertEntry(int index, MapOptionEntry<?, ?> entry) {
        entries.add(index, (MapOptionEntry<K, V>) entry);
        onRefresh();
    }

    @Override
    public MapOptionEntry<K, V> insertNewEntry() {
        K key = nextKey();
        if (key == null) return null;
        MapOptionEntry<K, V> newEntry = entryFactory.create(key, initialValue.get());
        if (insertEntriesAtEnd) {
            entries.add(newEntry);
        } else {
            entries.add(0, newEntry);
        }
        onRefresh();
        return newEntry;
    }

    public boolean hasKey(K key) {
        return entries.stream().anyMatch(e -> Objects.equals(e.getKey(), key));
    }

    public MapOptionEntry<K, V> getEntryForKey(K key) {
        return entries.stream().filter(e -> Objects.equals(e.getKey(), key)).findFirst().orElse(null);
    }

    public boolean canInsertNewEntry() {
        return available() && numberOfEntries() < maximumNumberOfEntries() && nextKey() != null;
    }

    private @Nullable K nextKey() {
        for (K key : initialKeys.get()) {
            if (!hasKey(key)) return key;
        }
        return null;
    }

    @Override
    public void removeEntry(MapOptionEntry<?, ?> entry) {
        if (entries.remove(entry))
            onRefresh();
    }

    @Override
    public int indexOf(MapOptionEntry<?, ?> entry) {
        return entries.indexOf(entry);
    }

    @Override
    public void requestSet(@NotNull Map<K, V> value) {
        entries.clear();
        entries.addAll(createEntries(value));
        onRefresh();
    }

    @Override
    public boolean changed() {
        return !binding().getValue().equals(pendingValue());
    }

    @Override
    public boolean applyValue() {
        if (changed()) {
            binding().setValue(pendingValue());
            return true;
        }
        return false;
    }

    @Override
    public void forgetPendingValue() {
        requestSet(binding().getValue());
    }

    @Override
    public void requestSetDefault() {
        requestSet(binding().defaultValue());
    }

    @Override
    public boolean isPendingValueDefault() {
        return binding().defaultValue().equals(pendingValue());
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        boolean changed = this.available != available;

        this.available = available;

        if (changed) {
            if (!available) {
                forgetPendingValue();
            }
            this.triggerListener(!available);
        }
    }

    @Override
    public int numberOfEntries() {
        return this.entries.size();
    }
    @Override
    public int maximumNumberOfEntries() {
        return this.maximumNumberOfEntries;
    }
    @Override
    public int minimumNumberOfEntries() {
        return this.minimumNumberOfEntries;
    }

    @Override
    public void addListener(BiConsumer<Option<Map<K, V>>, Map<K, V>> changedListener) {
        this.listeners.add(changedListener);
    }

    @Override
    public void addRefreshListener(Runnable changedListener) {
        this.refreshListeners.add(changedListener);
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    private List<MapOptionEntry<K, V>> createEntries(Map<K, V> values) {
        List<MapOptionEntry<K, V>> list = new ArrayList<>();
        for (Map.Entry<K, V> entry : values.entrySet()) {
            list.add(entryFactory.create(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    void triggerListener(boolean allowDepth) {
        if (allowDepth || currentListenerDepth == 0) {
            Validate.isTrue(
                    currentListenerDepth <= 10,
                    "Listener depth exceeded 10! Possible cyclic listener pattern: a listener triggered an event that triggered the initial event etc etc."
            );

            currentListenerDepth++;

            Map<K, V> value = pendingValue();
            for (BiConsumer<Option<Map<K, V>>, Map<K, V>> listener : listeners) {
                listener.accept(this, value);
            }

            currentListenerDepth--;
        }
    }

    private void onRefresh() {
        refreshListeners.forEach(Runnable::run);
        triggerListener(true);
    }

    private class EntryFactory {
        private final Function<Option<K>, Controller<K>> keyControllerFunction;
        private final Function<Option<V>, Controller<V>> valueControllerFunction;

        private EntryFactory(Function<Option<K>, Controller<K>> keyControllerFunction, Function<Option<V>, Controller<V>> valueControllerFunction) {
            this.keyControllerFunction = keyControllerFunction;
            this.valueControllerFunction = valueControllerFunction;
        }

        public MapOptionEntry<K, V> create(K initialKey, V initialValue) {
            return new MapOptionEntryImpl<>(MapOptionImpl.this, initialKey, initialValue, keyControllerFunction, valueControllerFunction);
        }
    }

    @ApiStatus.Internal
    public static final class BuilderImpl<K, V> implements Builder<K, V> {
        private Component name = Component.empty();
        private OptionDescription description = OptionDescription.EMPTY;
        private Function<Option<K>, Controller<K>> keyControllerFunction;
        private Function<Option<V>, Controller<V>> valueControllerFunction;
        private final Set<OptionFlag> flags = new HashSet<>();
        private Supplier<? extends Iterable<K>> initialKeys;
        private Supplier<V> initialValue;
        private boolean collapsed = false;
        private boolean available = true;
        private int minimumNumberOfEntries = 0;
        private int maximumNumberOfEntries = Integer.MAX_VALUE;
        private boolean insertEntriesAtEnd = false;
        private final List<BiConsumer<Option<Map<K, V>>, Map<K, V>>> listeners = new ArrayList<>();

        private Binding<Map<K, V>> binding;

        @Override
        public Builder<K, V> name(@NotNull Component name) {
            Validate.notNull(name, "`name` must not be null");

            this.name = name;
            return this;
        }

        @Override
        public Builder<K, V> description(@NotNull OptionDescription description) {
            Validate.notNull(description, "`description` must not be null");

            this.description = description;
            return this;
        }

        @Override
        public Builder<K, V> initialKey(@NotNull Supplier<K> initialValue) {
            Validate.notNull(initialValue, "`initialValue` cannot be empty");

            this.initialKeys = () -> Collections.singleton(initialValue.get());
            return this;
        }

        @Override
        public Builder<K, V> initialKey(@NotNull K initialValue) {
            Validate.notNull(initialValue, "`initialValue` cannot be empty");

            this.initialKeys = () -> Collections.singleton(initialValue);
            return this;
        }

        @Override
        public Builder<K, V> initialKeys(@NotNull Supplier<? extends Iterable<K>> candidates) {
            Validate.notNull(candidates, "`candidates` cannot be null");

            this.initialKeys = candidates;
            return this;
        }

        @Override
        public Builder<K, V> initialValue(@NotNull Supplier<V> initialValue) {
            Validate.notNull(initialValue, "`initialValue` cannot be empty");

            this.initialValue = initialValue;
            return this;
        }

        @Override
        public Builder<K, V> initialValue(@NotNull V initialValue) {
            Validate.notNull(initialValue, "`initialValue` cannot be empty");

            this.initialValue = () -> initialValue;
            return this;
        }

        @Override
        public Builder<K, V> keyController(@NotNull Function<Option<K>, ControllerBuilder<K>> controller) {
            Validate.notNull(controller, "`controller` cannot be null");

            this.keyControllerFunction = opt -> controller.apply(opt).build();
            return this;
        }

        @Override
        public Builder<K, V> valueController(@NotNull Function<Option<V>, ControllerBuilder<V>> controller) {
            Validate.notNull(controller, "`controller` cannot be null");

            this.valueControllerFunction = opt -> controller.apply(opt).build();
            return this;
        }

        @Override
        public Builder<K, V> customKeyController(@NotNull Function<Option<K>, Controller<K>> control) {
            Validate.notNull(control, "`control` cannot be null");

            this.keyControllerFunction = control;
            return this;
        }

        @Override
        public Builder<K, V> customValueController(@NotNull Function<Option<V>, Controller<V>> control) {
            Validate.notNull(control, "`control` cannot be null");

            this.valueControllerFunction = control;
            return this;
        }

        @Override
        public Builder<K, V> binding(@NotNull Binding<Map<K, V>> binding) {
            Validate.notNull(binding, "`binding` cannot be null");

            this.binding = binding;
            return this;
        }

        @Override
        public Builder<K, V> binding(@NotNull Map<K, V> def, @NotNull Supplier<@NotNull Map<K, V>> getter, @NotNull Consumer<@NotNull Map<K, V>> setter) {
            Validate.notNull(def, "`def` must not be null");
            Validate.notNull(getter, "`getter` must not be null");
            Validate.notNull(setter, "`setter` must not be null");

            this.binding = Binding.generic(def, getter, setter);
            return this;
        }

        @Override
        public Builder<K, V> available(boolean available) {
            this.available = available;
            return this;
        }

        @Override
        public Builder<K, V> minimumNumberOfEntries(int number) {
            this.minimumNumberOfEntries = number;
            return this;
        }

        @Override
        public Builder<K, V> maximumNumberOfEntries(int number) {
            this.maximumNumberOfEntries = number;
            return this;
        }

        @Override
        public Builder<K, V> insertEntriesAtEnd(boolean insertAtEnd) {
            this.insertEntriesAtEnd = insertAtEnd;
            return this;
        }

        @Override
        public Builder<K, V> flag(@NotNull OptionFlag... flag) {
            Validate.notNull(flag, "`flag` must not be null");

            this.flags.addAll(Arrays.asList(flag));
            return this;
        }

        @Override
        public Builder<K, V> flags(@NotNull Collection<OptionFlag> flags) {
            Validate.notNull(flags, "`flags` must not be null");

            this.flags.addAll(flags);
            return this;
        }

        @Override
        public Builder<K, V> collapsed(boolean collapsible) {
            this.collapsed = collapsible;
            return this;
        }

        @Override
        public Builder<K, V> listener(@NotNull BiConsumer<Option<Map<K, V>>, Map<K, V>> listener) {
            Validate.notNull(listener, "`listener` must not be null");

            this.listeners.add(listener);
            return this;
        }

        @Override
        public Builder<K, V> listeners(@NotNull Collection<BiConsumer<Option<Map<K, V>>, Map<K, V>>> listeners) {
            Validate.notNull(listeners, "`listeners` must not be null");

            this.listeners.addAll(listeners);
            return this;
        }

        @Override
        public MapOption<K, V> build() {
            Validate.notNull(keyControllerFunction, "`keyController` must not be null");
            Validate.notNull(valueControllerFunction, "`valueController` must not be null");
            Validate.notNull(initialKeys, "`initialKey` must not be null");
            Validate.notNull(initialValue, "`initialValue` must not be null");
            Validate.notNull(binding, "`binding` must not be null");

            return new MapOptionImpl<>(name, description, binding, initialKeys, initialValue, keyControllerFunction, valueControllerFunction, ImmutableSet.copyOf(flags), collapsed, available, minimumNumberOfEntries, maximumNumberOfEntries, insertEntriesAtEnd, listeners);
        }
    }
}
