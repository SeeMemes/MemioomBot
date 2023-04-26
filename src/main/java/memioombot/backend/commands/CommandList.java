package memioombot.backend.commands;

import memioombot.backend.database.repositories.UserRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import memioombot.backend.database.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class CommandList {

    @Autowired private UserRepository userRepository;

    private ArrayList<UserEntity> userEntities;

    public CommandList() {}

    @PostConstruct
    private void setUserEntities () {
        userEntities = (ArrayList<UserEntity>) userRepository.findAll();
    }

    protected void addUserToBlackList(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        User user = userOption.getAsUser();
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            boolean isUserInBlacklist = false;
            for (UserEntity userEntity : userEntities) {
                if (user.getIdLong() == userEntity.getUId()) {
                    System.err.println("User is already in BlackList");
                    event.reply("User is already in BlackList").setEphemeral(true).queue();
                    isUserInBlacklist = true;
                    break;
                }
            }
            if (!isUserInBlacklist) {
                UserEntity userToAdd = new UserEntity(user);
                userEntities.add(userToAdd);
                userRepository.save(userToAdd);
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
            boolean isUserInBlacklist = false;
            for (UserEntity userEntity : userEntities) {
                if (user.getIdLong() == userEntity.getUId()) {
                    userEntities.remove(userEntity);
                    userRepository.deleteById(userEntity.getUId());
                    isUserInBlacklist = true;
                    event.reply("Success!").setEphemeral(false).queue();
                    break;
                }
            }
            if (!isUserInBlacklist) {
                event.reply("User is not in blacklist").setEphemeral(true).queue();
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
        for (UserEntity userEntity : userEntities) {
            if (user.getIdLong() == userEntity.getUId()) {
                event.getMessage().delete().queue();
                event.getChannel().sendMessage(user.getName() + " today is not your day").queue();
                break;
            }
        }
    }
}
