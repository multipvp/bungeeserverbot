package org.multipvp.serverbot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Plugin implements Listener {
    public static ProxyServer proxyServer;
    @Override
    public void onEnable() {
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
                    connections.put(receiver.getUniqueId().toString(), new ArrayList<>());
                }
                playerUpdate(receiver.getUniqueId().toString());
                // do things
            }
        }
    }
    public Map<String,Loc> positions = new HashMap<>();
    private final Map<String, List<String>> connections = new HashMap<>();

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
            if (distSqr < 100) {
                newConnections.add(p);
            }
        }

        oldConnections.removeAll(newConnections);
        for (String p : oldConnections) {
            connections.get(p).remove(player);
            System.out.println(String.format("%s is no longer in range of %s",p,player));
        }
        newConnections.removeAll(oldConnections2);
        for (String p : newConnections) {
            connections.get(p).add(player);
            System.out.println(String.format("%s is now in range of %s",p,player));
        }
    }
    public void updateGroups() {
        
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
