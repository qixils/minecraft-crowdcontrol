package dev.qixils.crowdcontrol.plugin.paper.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class ReflectionUtil {
	public static final @Nullable Class<?> FEATURE_ELEMENT_CLAZZ = getClazz("net.minecraft.world.flag.FeatureElement").orElse(null);
	public static final @Nullable Class<?> FEATURE_FLAG_SET_CLAZZ = getClazz("net.minecraft.world.flag.FeatureFlagSet").orElse(null);
	public static final @NotNull String CB_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

	public static @NotNull String cbClass(@NotNull String name) {
		return CB_PACKAGE + '.' + name;
	}

	public static boolean isInstance(@Nullable Class<?> clazz, @Nullable Object object) {
		return clazz != null && clazz.isInstance(object);
	}


	public static <T> Optional<T> getAsClass(@Nullable Object object, @NotNull Class<T> clazz) {
		if (object == null)
			return Optional.empty();
		if (clazz.isInstance(object)) {
			return Optional.of(clazz.cast(object));
		}
		return Optional.empty();
	}

	public static Optional<Class<?>> getClazz(@NotNull String name) {
		try {
			return Optional.of(Class.forName(name));
		} catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	public static Optional<Method> getMethod(@NotNull Class<?> clazz, @NotNull String name, Class<?>... parameterTypes) {
		for (Class<?> parameterType : parameterTypes) {
			if (parameterType == null) {
				return Optional.empty();
			}
		}
		try {
			return Optional.of(clazz.getMethod(name, parameterTypes));
		} catch (NoSuchMethodException e) {
			return Optional.empty();
		}
	}

	private static Class<?>[] guessParameterTypes(Object... args) {
		Class<?>[] parameterTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return parameterTypes;
	}

	public static boolean invokeVoidMethod(@NotNull Method method, @Nullable Object object, Object... args) {
		try {
			method.invoke(object, args);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static <T> Optional<T> invokeMethod(@NotNull Method method, @Nullable Object object, Object... args) {
		try {
			return Optional.ofNullable((T) method.invoke(object, args));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static boolean invokeVoidMethod(@Nullable Object instance, @NotNull Class<?> clazz, @NotNull String name, Class<?>[] parameterTypes, Object... args) {
		return getMethod(clazz, name, parameterTypes).map(method -> invokeVoidMethod(method, instance, args)).orElse(false);
	}

	public static <T> Optional<T> invokeMethod(@Nullable Object instance, @NotNull Class<?> clazz, @NotNull String name, Class<?>[] parameterTypes, Object... args) {
		return getMethod(clazz, name, parameterTypes).flatMap(method -> invokeMethod(method, instance, args));
	}

	public static boolean invokeVoidMethod(@Nullable Object instance, @NotNull Class<?> clazz, @NotNull String name, Object... args) {
		return invokeVoidMethod(instance, clazz, name, guessParameterTypes(args), args);
	}

	public static <T> Optional<T> invokeMethod(@Nullable Object instance, @NotNull Class<?> clazz, @NotNull String name, Object... args) {
		return invokeMethod(instance, clazz, name, guessParameterTypes(args), args);
	}

	public static boolean invokeVoidMethod(@NotNull Object instance, @NotNull String name, Class<?>[] parameterTypes, Object... args) {
		return invokeVoidMethod(instance, instance.getClass(), name, parameterTypes, args);
	}

	public static <T> Optional<T> invokeMethod(@NotNull Object instance, @NotNull String name, Class<?>[] parameterTypes, Object... args) {
		return invokeMethod(instance, instance.getClass(), name, parameterTypes, args);
	}

	public static boolean invokeVoidMethod(@NotNull Object instance, @NotNull String name, Object... args) {
		return invokeVoidMethod(instance, instance.getClass(), name, args);
	}

	public static <T> Optional<T> invokeMethod(@NotNull Object instance, @NotNull String name, Object... args) {
		return invokeMethod(instance, instance.getClass(), name, args);
	}

	public static Optional<Field> getField(@NotNull Class<?> clazz, @NotNull String name) {
		try {
			return Optional.of(clazz.getField(name));
		} catch (NoSuchFieldException e) {
			return Optional.empty();
		}
	}

	public static <T> Optional<T> getFieldValue(@NotNull Field field, @Nullable Object object) {
		try {
			return Optional.of((T) field.get(object));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static <T> Optional<T> getFieldValue(@Nullable Object instance, @NotNull Class<?> clazz, @NotNull String name) {
		return getField(clazz, name).flatMap(field -> getFieldValue(field, instance));
	}

	public static <T> Optional<T> getFieldValue(@NotNull Object instance, @NotNull String name) {
		return getFieldValue(instance, instance.getClass(), name);
	}
}
