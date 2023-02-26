package org.backend;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public class CommandList {
    private final JDA jda;

    private static ArrayList<User> userBlackList = new ArrayList<>();

    public CommandList (JDA jda) {
        this.jda = jda;
    }

    protected static void addUserToBlackList(SlashCommandInteractionEvent event, User user) {
        System.out.println("addUserToBlackList");
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (userBlackList.contains(user)) {
                System.err.println("User is already in BlackList");
                event.reply("User is already in BlackList").setEphemeral(true).queue();
            } else {
                if (userBlackList.add(user)) {
                    event.reply("Success!").setEphemeral(false).queue();
                } else {
                    System.err.println("Something went wrong try again later");
                    event.reply("Something went wrong try again later").setEphemeral(true).queue();
                }
            }
        } else {
            System.err.println("No rights to add user to blacklist");
            event.reply("No rights to add user to blacklist").setEphemeral(false).queue();
        }
    }

    protected static void deleteUserFromList(SlashCommandInteractionEvent event, User user) {
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (userBlackList.remove(user)){
                event.reply(event.getName() + " has been completed").setEphemeral(false).queue();
            } else {
                event.reply("Something went wrong try again later").setEphemeral(true).queue();
            }
        } else {
            event.reply("No rights to add user to blacklist").setEphemeral(true).queue();
        }
    }

    protected static void ping(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(false) // reply or acknowledge
                .flatMap(v ->
                        event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time) // then edit original
                ).queue(); // Queue both reply and edit
    }

    protected static void deleteUserMessage (MessageReceivedEvent event, User user) {
        if (userBlackList.contains(user)){
            event.getMessage().delete().queue();
            event.getChannel().sendMessage(user.getName() + " иди нахуй").queue();
        }
    }
}
