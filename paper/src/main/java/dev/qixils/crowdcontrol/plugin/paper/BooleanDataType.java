package dev.qixils.crowdcontrol.plugin.paper;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

final class BooleanDataType implements PersistentDataType<Byte, Boolean> {
	private static final byte TRUE = 1;
	private static final byte FALSE = 0;

	@NotNull
	public Class<Byte> getPrimitiveType() {
		return Byte.class;
	}

	@NotNull
	public Class<Boolean> getComplexType() {
		return Boolean.class;
	}

	@NotNull
	public Byte toPrimitive(@NotNull Boolean complex, @NotNull PersistentDataAdapterContext context) {
		return complex ? TRUE : FALSE;
	}

	@NotNull
	public Boolean fromPrimitive(@NotNull Byte primitive, @NotNull PersistentDataAdapterContext context) {
		return primitive != FALSE;
	}
}
