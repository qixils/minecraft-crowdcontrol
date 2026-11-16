package dev.qixils.crowdcontrol.plugin.fabric.client.option;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface MapOptionEntry<K, V> extends Option<Map.Entry<K, V>> {
    MapOption<K, V> parentGroup();

    K getKey();
    void setKey(K key);

    V getValue();
    void setValue(V value);

    @Override
    default @NotNull ImmutableSet<OptionFlag> flags() {
        return parentGroup().flags();
    }

    @Override
    default boolean available() {
        return parentGroup().available();
    }
}
