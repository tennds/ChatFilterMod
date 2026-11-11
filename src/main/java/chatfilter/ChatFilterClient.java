package chatfilter;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

/**
 * Client-side mod that hides chat messages from specified players.
 *
 * Commands (client-side, work on any server):
 *   /addignore <nick>     — hide messages from a player
 *   /removeignore <nick>  — show messages again
 *   /listignore           — list ignored players
 *   /chatfilter           — show help
 *
 * Tab-completion suggests online player names for /addignore
 * and ignored names for /removeignore.
 */
public class ChatFilterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ChatFilterStore.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("addignore")
                    .executes(context -> {
                        send(context.getSource(), "Usage: /addignore <nick>  (Tab — online player names)");
                        return 1;
                    })
                    .then(ClientCommands.argument("nick", StringArgumentType.string())
                            .suggests((context, builder) ->
                                    SharedSuggestionProvider.suggest(onlinePlayerNames(), builder))
                            .executes(context -> {
                                String nick = StringArgumentType.getString(context, "nick").trim();
                                if (nick.isEmpty()) {
                                    send(context.getSource(), "Usage: /addignore <nick>");
                                    return 0;
                                }
                                boolean added = ChatFilterStore.add(nick);
                                send(context.getSource(), added
                                        ? nick + " added to ignore — their messages are hidden."
                                        : nick + " is already ignored.");
                                return 1;
                            })));

            dispatcher.register(ClientCommands.literal("removeignore")
                    .executes(context -> {
                        send(context.getSource(), "Usage: /removeignore <nick>  (Tab — ignored names)");
                        return 1;
                    })
                    .then(ClientCommands.argument("nick", StringArgumentType.string())
                            .suggests((context, builder) ->
                                    SharedSuggestionProvider.suggest(ChatFilterStore.names(), builder))
                            .executes(context -> {
                                String nick = StringArgumentType.getString(context, "nick").trim();
                                if (nick.isEmpty()) {
                                    send(context.getSource(), "Usage: /removeignore <nick>");
                                    return 0;
                                }
                                boolean removed = ChatFilterStore.remove(nick);
                                send(context.getSource(), removed
                                        ? nick + " removed from ignore — their messages are visible again."
                                        : nick + " was not ignored. List: /listignore");
                                return 1;
                            })));

            dispatcher.register(ClientCommands.literal("listignore")
                    .executes(context -> {
                        sendList(context.getSource());
                        return 1;
                    }));

            dispatcher.register(ClientCommands.literal("chatfilter")
                    .executes(context -> {
                        send(context.getSource(), "Commands: /addignore <nick>, /removeignore <nick>, /listignore");
                        return 1;
                    }));
        });
    }

    /** Nicknames of players currently online (for /addignore tab-completion). */
    private static List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return names;
        }
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            String name = info.getProfile().name();
            if (name != null && !name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    private static void send(FabricClientCommandSource source, String text) {
        source.sendFeedback(Component.literal(ChatFilterStore.PREFIX + " " + text));
    }

    private static void sendList(FabricClientCommandSource source) {
        Collection<String> names = ChatFilterStore.names();
        if (names.isEmpty()) {
            send(source, "List is empty. Add a player: /addignore <nick>");
            return;
        }
        send(source, "Hidden (" + names.size() + "): " + String.join(", ", names));
    }
}