package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Scanner;

public final class Application {

	@NotNull
	public static final String APPLICATION_ID;

	@NotNull
	public static final String APPLICATION_SECRET;

	static {
		InputStream inputStream = Application.class.getClassLoader().getResourceAsStream("mccc-application.txt");
		if (inputStream == null)
			throw new IllegalStateException("Could not load application from resources");
		Scanner scanner = new Scanner(inputStream);
		APPLICATION_ID = scanner.next("\n");
		APPLICATION_SECRET = scanner.next("\n");
	}
}
