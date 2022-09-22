package org.multipvp.serverbot;


import com.google.common.collect.Lists;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Collection;


public class Bot {
    public static Guild mainGuild;
    public static Category category;
    public static Role verifiedRole;
    public static void start()
    {
        JDA api = JDABuilder.createDefault("MTAyMTc3MTE5OTk0NjE3MDM4OQ.GBVH55.dy3QnCVYvE_AAUATmoqf-FliRTRWtcxpNGYLjU",
                        Lists.asList(GatewayIntent.GUILD_BANS,GatewayIntent.values())
                )
                .addEventListeners(new EventListener())
                .addEventListeners(new JoinListener())
                .build();



    }
}