package dev.qixils.crowdcontrol.plugin.paper;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

final class EnumDataType<C extends Enum<C>> implements PersistentDataType<String, C> {
	private final Class<C> complexType;
	private final Function<Optional<C>, C> resolver;

	EnumDataType(Class<C> complexType, Function<Optional<C>, C> resolver) {
		this.complexType = complexType;
		this.resolver = resolver;
	}

	@Override
	public @NotNull Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public @NotNull Class<C> getComplexType() {
		return complexType;
	}

	@Override
	public @NotNull String toPrimitive(@NotNull C complex, @NotNull PersistentDataAdapterContext context) {
		return complex.name();
	}

	@Override
	public @NotNull C fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
		return resolver.apply(Arrays.stream(complexType.getEnumConstants()).filter(c -> c.name().equals(primitive)).findAny());
	}
}
