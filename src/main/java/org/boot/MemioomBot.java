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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemioomBot {
    @Value("${token}")
    private String token;

    @Bean
    public JDA runApp() throws InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT);

        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setActivity(Activity.playing("Life"));

        JDA jda = builder.build();
        jda.addEventListener(new CommandListener(jda));

        jda.updateCommands().addCommands(
                Commands.slash("ping", "Пинг бота"),
                Commands.slash("addtolist", "Insert user to blacklist")
                        .addOptions(
                                new OptionData(OptionType.USER, "user", "user to be blacklisted")
                                        .setRequired(true)
                        ),
                Commands.slash("deletefromlist", "Delete user from blacklist")
                        .addOptions(
                                new OptionData(OptionType.USER, "user", "user to be freed from blacklist")
                                        .setRequired(true)
                        )
        ).queue();
        return jda;
    }
}
