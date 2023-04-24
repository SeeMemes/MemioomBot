package memioombot.backend.commands;

import memioombot.backend.database.repositories.UserRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import memioombot.backend.database.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class CommandList {

    @Autowired private UserRepository userRepository;

    private HashMap<User, Long> userHashMap = new HashMap<>();

    public CommandList() {}

    protected void addUserToBlackList(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        User user = userOption.getAsUser();

        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (userHashMap.containsKey(user)) {
                System.err.println("User is already in BlackList");
                event.reply("User is already in BlackList").setEphemeral(true).queue();
            } else {
                UserEntity userToAdd = new UserEntity(user);
                userRepository.save(userToAdd);
                userHashMap.put(user, 1L/*userToAdd.getId()*/);
                event.reply("Success!").setEphemeral(false).queue();
            }
        } else {
            System.err.println("No rights to add user to blacklist");
            event.reply("No rights to add user to blacklist").setEphemeral(false).queue();
        }
    }

    protected void deleteUserFromList(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        User user = userOption.getAsUser();

        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (userHashMap.containsKey(user)) {
                Long id = userHashMap.get(user);
                userRepository.deleteById(id);
                userHashMap.remove(user);
                event.reply(event.getName() + " has been completed").setEphemeral(false).queue();
            } else {
                event.reply("Something went wrong try again later").setEphemeral(true).queue();
            }
        } else {
            event.reply("No rights to add user to blacklist").setEphemeral(true).queue();
        }
    }

    protected void ping(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(false) // reply or acknowledge
                .flatMap(v ->
                        event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time) // then edit original
                ).queue(); // Queue both reply and edit
    }

    protected void deleteUserMessage(MessageReceivedEvent event, User user) {
        if (userHashMap.containsKey(user)) {
            event.getMessage().delete().queue();
            event.getChannel().sendMessage(user.getName() + " today is not your day").queue();
        }
    }
}
