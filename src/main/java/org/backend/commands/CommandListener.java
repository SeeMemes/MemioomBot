package org.backend.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.backend.commands.CommandList;
import org.backend.database.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;

public class CommandListener extends ListenerAdapter {
    private final JDA jda;

    public CommandListener(JDA jda) {
        this.jda = jda;
    }

    @Bean
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event/*, UserRepository userRepository*/) {
        User user = event.getUser();
        switch (event.getName()) {
            case "ping":
                CommandList.ping(event);
                break;
            case "addtolist":
                CommandList.addUserToBlackList(event/*, userRepository*/);
                break;
            case "deletefromlist":
                CommandList.deleteUserFromList(event/*, userRepository*/);
                break;
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        User user = event.getAuthor();

        CommandList.deleteUserMessage(event, user);
    }
}
