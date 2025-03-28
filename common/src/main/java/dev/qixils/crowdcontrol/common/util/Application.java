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
		String[] tokens = new Scanner(inputStream).next().split(":");
		APPLICATION_ID = tokens[0];
		APPLICATION_SECRET = tokens[1];
	}
}
