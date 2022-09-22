package org.multipvp.serverbot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main extends Plugin implements Listener {
    public static ProxyServer proxyServer;
    public static final int PROXIMITY_CHAT_DISTANCE = 115;
    private final int proxChatSqr = PROXIMITY_CHAT_DISTANCE * PROXIMITY_CHAT_DISTANCE;
    public static Gson gson;
    @Override
    public void onEnable() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        getProxy().registerChannel( "multipvp:clusters.updates" );
        proxyServer = getProxy();
        getLogger().info("Yay! It loads!");
        Bot.start();
        getProxy().getPluginManager().registerListener(this,this);


    }
    @Override
    public void onDisable() {
        //make sure to unregister the registered channels in case of a reload
        getProxy().unregisterChannel("multipvp:clusters.updates");
        Bot.category.getChannels().forEach(guildChannel -> {
            if (!guildChannel.getId().equals("1022410179888820224")) {
                guildChannel.delete().queue();
            }
        });
    }
    @EventHandler
    public void onP(PlayerDisconnectEvent e) {
        connections.remove(e.getPlayer().getUniqueId().toString());
        positions.remove(e.getPlayer().getUniqueId().toString());
        // playerUpdate(e.getPlayer().getUniqueId().toString());
    }
    @EventHandler
    public void onPl(ServerSwitchEvent e) {
        //connections.remove(e.getPlayer().getUniqueId().toString());
        //positions.remove(e.getPlayer().getUniqueId().toString());
    }
    @EventHandler
    public void on(PluginMessageEvent event)
    {
        if ( !event.getTag().equalsIgnoreCase( "multipvp:clusters.updates" ) )
        {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput( event.getData() );
        String subChannel = in.readUTF();
        if ( subChannel.equalsIgnoreCase( "location" ) ) {
            if (event.getReceiver() instanceof ProxiedPlayer receiver) {
                double x = in.readDouble();
                double y = in.readDouble();
                double z = in.readDouble();
                String world = in.readUTF();
                String server = receiver.getServer().getInfo().getName();
                if (positions.containsKey(receiver.getUniqueId().toString())) {
                    Loc location = positions.get(receiver.getUniqueId().toString());
                    location.setServer(server);
                    location.setX(x);
                    location.setY(y);
                    location.setZ(z);
                    location.setWorld(world);
                } else {
                    positions.put(receiver.getUniqueId().toString(),new Loc(x,y,z,world,server));
                    connections.put(receiver.getUniqueId().toString(), new HashSet<>());
                }
                playerUpdate(receiver.getUniqueId().toString());
                // do things
            }
        }
    }
    public Map<String,Loc> positions = new HashMap<>();
    private final Map<String, Set<String>> connections = new HashMap<>();

    public void playerUpdate(String player) {
        Loc mover = positions.get(player);
        ArrayList<String> oldConnections = new ArrayList<>(List.copyOf(connections.get(player)));
        ArrayList<String> oldConnections2 = new ArrayList<>(List.copyOf(connections.get(player)));
        List<String> newConnections = new ArrayList<>();
        for (String p : positions.keySet()) {
            if (p.equals(player)) continue;
            Loc pLoc = positions.get(p);
            double x = pLoc.x - mover.x;
            double y = pLoc.y - mover.y;
            double z = pLoc.z - mover.z;
            double distSqr = x * x + y * y + z * z;
            ///620.70 32.00 295.82
            // 040.40 11.00 002.96
            ///580.30 43.00 292.96

            /// 579.64 74.00 355.00
            //   51.06  0.00 101.70
            /// 630.70 73.73 253.30
            if (distSqr < 13000 && pLoc.server.equals(mover.server) && pLoc.world.equals(mover.world)) {
                newConnections.add(p);
            }
        }

        oldConnections.removeAll(newConnections);
        for (String p : oldConnections) {
            connections.get(p).remove(player);
            connections.get(player).remove(p);
            //System.out.println(String.format("%s is no longer in range of %s",p,player));
        }
        newConnections.removeAll(oldConnections2);
        for (String p : newConnections) {
            connections.get(p).add(player);
            connections.get(player).add(p);
            //System.out.println(UserCache.getDiscord(p));
            //System.out.println(UserCache.getDiscord(player));
            //System.out.println(String.format("%s is now in range of %s",p,player));
        }
        updateGroups();
    }
    public void updateGroups() {
        Map<String, Set<String>> groups = new HashMap<>(); // First string is "id"
        List<String> players = positions.keySet().stream().toList();

        // OOOP a player was detected in bounding box
        // Both players get randomly generated id
        // Search radius of everyone with id  - Add those to id, continue
        int index = 0;
        for (String player : players) {
            // players
            Set<String> players2 = new HashSet<>(connections.get(player));
            boolean hasBeenPlaced = false;
            String playerPlaced = "";
            for (String group : groups.keySet()) {
                HashSet<String> copyOfGroup = new HashSet<>(groups.get(group));
                // ["player 1": "player 2"]
                // ["player 2": "player 1","player 3"]
                // ["player 3": "player 2"]
                // group 1 ["player 2","player 3"]
                copyOfGroup.removeAll(players2);
                if (copyOfGroup.size() != groups.get(group).size()) {
                    hasBeenPlaced = true;
                    groups.get(group).add(player);
                }
//                if (groups.containsKey(player2) && !hasBeenPlaced) {
//                    groups.get(player2).add(player);
//                    playerPlaced = player2;
//                    hasBeenPlaced = true;
//                } else if (groups.containsKey(player2)) {
//                    groups.get(playerPlaced).addAll(groups.get(player2));
//                    groups.get(playerPlaced).add(player2);
//                }
            }
            if (!hasBeenPlaced) {
                index++;
                String channelId = UUID.nameUUIDFromBytes(String.format("group%d",index).getBytes()).toString();
                groups.put(channelId,players2);

                groups.get(channelId).add(player);
            }
        }
        //System.out.println(gson.toJson(groups));
        //EventListener.arrangeMembers(groups);
        Main.proxyServer.getScheduler().runAsync(this,()->{EventListener.arrangeMembers(groups);});
        //System.out.println(gson.toJson(connections));
    }
    public static class Loc {
        public double x;
        public double y;
        public double z;
        public String world;
        public String server;
        public Loc(double x,double y,double z,String world,String server) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
            this.server = server;
        }
        public void setX(double x) {
            this.x = x;
        }
        public void setY(double y) {
            this.y = y;
        }
        public void setZ(double z) {
            this.z = z;
        }
        public void setWorld(String world) {
            this.world = world;
        }
        public void setServer(String server) {
            this.server = server;
        }
    }
}
