package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public abstract class Command {
    public static final Random rand = new Random();

    public abstract @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull Request request);
    @NotNull
    public abstract String getEffectName();

    @NotNull
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        char[] chars = getClass().getSimpleName().replace("Command", "").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char chr = chars[i];
            if (i > 0 && Character.isUpperCase(chr))
                sb.append(' ');
            sb.append(chr);
        }
        return sb.toString();
    }

    public final void executeAndNotify(@NotNull Request request) {
        execute(request).thenAccept(builder -> {
            Response response = builder.id(request.getId()).build();

            CrowdControl cc = plugin.getCrowdControl();
            if (cc != null) cc.dispatchResponse(response);

            if (response.getResultType() == Response.ResultType.SUCCESS)
                Bukkit.getServer().sendMessage(new TextBuilder()
                        .next(request.getViewer(), CrowdControlPlugin.USER_COLOR)
                        .next(" used command ")
                        .next(getDisplayName(), CrowdControlPlugin.CMD_COLOR));
        });
    }

    protected final CrowdControlPlugin plugin;
    public Command(@NotNull CrowdControlPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

}
