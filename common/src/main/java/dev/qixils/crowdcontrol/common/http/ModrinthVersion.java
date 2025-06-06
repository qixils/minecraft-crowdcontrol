package dev.qixils.crowdcontrol.common.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModrinthVersion {
	private final @NotNull List<String> game_versions;
	private final @NotNull List<String> loaders;
	private final @NotNull String id;
	private final @NotNull String project_id;
	private final @NotNull String author_id;
	private final boolean featured;
	private final @NotNull String version_number;
	// etc

	@JsonCreator
	public ModrinthVersion(@JsonProperty("game_versions") @NotNull List<String> game_versions,
						   @JsonProperty("loaders") @NotNull List<String> loaders,
						   @JsonProperty("id") @NotNull String id,
						   @JsonProperty("project_id") @NotNull String project_id,
						   @JsonProperty("author_id") @NotNull String author_id,
						   @JsonProperty("featured") boolean featured,
						   @JsonProperty("version_number") @NotNull String version_number) {
		this.game_versions = game_versions;
		this.loaders = loaders;
		this.id = id;
		this.project_id = project_id;
		this.author_id = author_id;
		this.featured = featured;
		this.version_number = version_number;
	}

	@JsonProperty("game_versions")
	public @NotNull List<String> getGameVersions() {
		return game_versions;
	}

	public @NotNull List<String> getLoaders() {
		return loaders;
	}

	public @NotNull String getId() {
		return id;
	}

	@JsonProperty("project_id")
	public @NotNull String getProjectId() {
		return project_id;
	}

	@JsonProperty("author_id")
	public @NotNull String getAuthorId() {
		return author_id;
	}

	public boolean isFeatured() {
		return featured;
	}

	@JsonProperty("version_number")
	public @NotNull String getVersionNumber() {
		return version_number;
	}
}
