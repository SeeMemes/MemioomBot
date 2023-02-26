package org.backend;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    private final JDA jda;

    public CommandListener (JDA jda) {
        this.jda = jda;
    }
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        User user = event.getUser();
        // make sure we handle the right command
        switch (event.getName()) {
            case "ping":
                CommandList.ping(event);
                break;
            case "addtolist":
                CommandList.addUserToBlackList(event, user);
                break;
            case "deletefromlist":
                CommandList.deleteUserFromList(event, user);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        User user = event.getAuthor();

        CommandList.deleteUserMessage(event, user);
    }
}
