package org.boot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.backend.commands.CommandListener;
import org.springframework.boot.SpringApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/application.properties"));
            //SpringApplication.run(Main.class);
            JDABuilder builder = JDABuilder.createDefault(properties.getProperty("bot.token"))
                    .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT);

            // Disable parts of the cache
            builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
            // Enable the bulk delete event
            builder.setBulkDeleteSplittingEnabled(false);
            // Set activity (like "playing Something")
            builder.setActivity(Activity.watching("TV"));

            JDA jda = builder.build();

            CommandListener commandListener = new CommandListener(jda);

            jda.addEventListener(commandListener);

            OptionData addToListOption
                    = new OptionData(OptionType.USER, "user", "user to be blacklisted")
                    .setRequired(true);
            OptionData deleteFromListOption
                    = new OptionData(OptionType.USER, "user", "user to be freed from blacklist")
                    .setRequired(true);

            jda.updateCommands().addCommands(
                    Commands.slash("ping", "Пинг бота"),
                    Commands.slash("addtolist", "Insert user to blacklist")
                            .addOptions(addToListOption),
                    Commands.slash("deletefromlist", "Delete user from blacklist")
                            .addOptions(deleteFromListOption)
            ).queue();
        } catch (IOException e) {
            System.err.println(e + "Укажите bot.token в файле application.properties в src/main/resources/application.properties");
        }
    }
}