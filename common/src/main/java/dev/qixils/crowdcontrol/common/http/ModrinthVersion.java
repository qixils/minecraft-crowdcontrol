package dev.qixils.crowdcontrol.common.http;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModrinthVersion {
	@SerializedName("game_versions")
	private final @NotNull List<String> gameVersions;
	private final @NotNull List<String> loaders;
	private final @NotNull String id;
	@SerializedName("project_id")
	private final @NotNull String projectId;
	@SerializedName("author_id")
	private final @NotNull String authorId;
	private final boolean featured;
	@SerializedName("version_number")
	private final @NotNull String versionNumber;
	// etc

	public ModrinthVersion(@NotNull List<String> gameVersions,
						   @NotNull List<String> loaders,
						   @NotNull String id,
						   @NotNull String projectId,
						   @NotNull String authorId,
						   boolean featured,
						   @NotNull String versionNumber) {
		this.gameVersions = gameVersions;
		this.loaders = loaders;
		this.id = id;
		this.projectId = projectId;
		this.authorId = authorId;
		this.featured = featured;
		this.versionNumber = versionNumber;
	}

	public @NotNull List<String> getGameVersions() {
		return gameVersions;
	}

	public @NotNull List<String> getLoaders() {
		return loaders;
	}

	public @NotNull String getId() {
		return id;
	}

	public @NotNull String getProjectId() {
		return projectId;
	}

	public @NotNull String getAuthorId() {
		return authorId;
	}

	public boolean isFeatured() {
		return featured;
	}

	public @NotNull String getVersionNumber() {
		return versionNumber;
	}
}
