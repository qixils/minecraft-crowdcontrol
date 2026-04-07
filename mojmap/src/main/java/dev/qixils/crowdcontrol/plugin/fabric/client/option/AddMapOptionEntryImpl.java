package dev.qixils.crowdcontrol.plugin.fabric.client.option;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.TooltipButtonWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

public class AddMapOptionEntryImpl<K, V> implements MapOptionEntry<K, V> {
    private final MapOptionImpl<K, V> group;

    public AddMapOptionEntryImpl(MapOptionImpl<K, V> group) {
        this.group = group;
    }

    @Override
    public K getKey() {
        return null; // Not meant to be used for fetching data
    }

    @Override
    public V getValue() {
        return null;
    }

    @Override
    public void setKey(K key) {}

    @Override
    public void setValue(V value) {}

    @Override
    public @NotNull MapOption<K, V> parentGroup() {
        return group;
    }

    @Override
    public @NotNull Component name() {
        return Component.empty();
    }

    @Override
    public @NotNull OptionDescription description() {
        return OptionDescription.EMPTY;
    }

    @Override
    public @NotNull Component tooltip() {
        return Component.empty();
    }

    @Override
    public @NotNull Controller<Map.Entry<K, V>> controller() {
        return new Controller<Map.Entry<K, V>>() {
            @Override
            public Option<Map.Entry<K, V>> option() {
                return AddMapOptionEntryImpl.this;
            }

            @Override
            public Component formatValue() {
                return Component.empty();
            }

            @Override
            public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> dim) {
                return new AbstractWidget(dim) {
                    private final TooltipButtonWidget button = new TooltipButtonWidget(
                            screen, dim.x(), dim.y(), dim.width(), 20,
                            Component.literal("\u2795 ").append(Component.translatable("yacl.list.add_top")), null,
                            btn -> {
                                group.insertNewEntry();
                                MapOptionImpl.attemptRefresh(screen);
                            }
                    );

                    @Override
                    public void extractRenderState(@NotNull net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pt) {
                        button.active = available();
                        button.setX(getDimension().x());
                        button.setY(getDimension().y());
                        button.setWidth(getDimension().width());
                        button.extractRenderState(graphics, mouseX, mouseY, pt);
                    }

                    private boolean focused = false;

                    @Override
                    public boolean isFocused() {
                        return focused;
                    }

                    @Override
                    public void setFocused(boolean focused) {
                        this.focused = focused;
                    }

                    @Override
                    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                        return button.mouseClicked(event, doubleClick);
                    }

                    @Override
                    public boolean mouseReleased(MouseButtonEvent event) {
                        return button.mouseReleased(event);
                    }

                    @Override
                    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
                        return button.mouseDragged(event, deltaX, deltaY);
                    }

                    public void updateNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
                        // ignored
                    }
                };
            }
        };
    }

    @Override
    public @NotNull StateManager<Map.Entry<K, V>> stateManager() {
        return StateManager.createSimple(binding());
    }

    @Override
    public @NotNull Binding<Map.Entry<K, V>> binding() {
        return new Binding<Map.Entry<K, V>>() {
            @Override
            public void setValue(Map.Entry<K, V> value) {}

            @Override
            public Map.Entry<K, V> getValue() { return null; }

            @Override
            public Map.Entry<K, V> defaultValue() { return null; }
        };
    }

    @Override
    public void requestSet(@NotNull Map.Entry<K, V> value) {}

    @Override
    public boolean changed() {
        return false;
    }

    @Override
    public boolean applyValue() {
        return false;
    }

    @Override
    public void forgetPendingValue() {}

    @Override
    public void requestSetDefault() {}

    @Override
    public boolean isPendingValueDefault() {
        return true;
    }

    @Override
    public boolean available() {
        return group.canInsertNewEntry();
    }

    @Override
    public void setAvailable(boolean available) {}

    @Override
    public @NotNull ImmutableSet<OptionFlag> flags() {
        return ImmutableSet.of();
    }

    @Override
    public @NotNull Map.Entry<K, V> pendingValue() {
        return null;
    }

    @Override
    public void addListener(BiConsumer<Option<Map.Entry<K, V>>, Map.Entry<K, V>> changedListener) {}

    @Override
    public void addEventListener(OptionEventListener<Map.Entry<K, V>> listener) {}
}
