package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.Bukkit;

import java.util.Random;

public abstract class ChatCommand {
    public static final Random rand = new Random();

    public abstract Response.Result execute(Request request);
    public abstract String getEffectName();

    public final Response.Result executeAndNotify(Request request) {
        Response.Result result = execute(request);
        if (result.getType() == Response.ResultType.SUCCESS)
            Bukkit.getServer().sendMessage(new TextBuilder()
                .next(request.getViewer(), CrowdControlPlugin.USER_COLOR)
                .next(" used command ")
                .next(getDisplayName(), CrowdControlPlugin.CMD_COLOR));
        return result;
    }

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

    protected final CrowdControlPlugin plugin;
    public ChatCommand(CrowdControlPlugin plugin) {
        this.plugin = plugin;
    }

}
