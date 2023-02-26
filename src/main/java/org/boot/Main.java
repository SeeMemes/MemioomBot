package org.boot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.backend.CommandListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/config.properties"));
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

            jda.updateCommands().addCommands(
                    Commands.slash("ping", "Пинг бота"),
                    Commands.slash("addtolist", "Засунуть юзера в список дебилов"),
                    Commands.slash("deletefromlist", "Высунуть юзера из списка дебилов")
            ).queue();
        } catch (IOException e) {
            System.err.println(e + "Укажите bot.token в файле config.properties в src/main/resources/config.properties");
        }
    }
}