package memioombot.backend.commands;

import memioombot.backend.database.repositories.UserRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import org.backend.database.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Controller
public class CommandListener extends ListenerAdapter {

    @Autowired private CommandList commandList;

    public CommandListener() {}

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping":
                commandList.ping(event);
                break;
            case "addtolist":
                commandList.addUserToBlackList(event);
                break;
            case "deletefromlist":
                commandList.deleteUserFromList(event);
                break;
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        User user = event.getAuthor();

        commandList.deleteUserMessage(event, user);
    }
}