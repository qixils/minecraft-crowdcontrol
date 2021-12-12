package dev.qixils.crowdcontrol.plugin.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleUtil {
    public static void spawnPlayerParticles(Player player, Particle particle, int count) {
        Location location = player.getLocation();
        location.setY(Math.ceil(location.getY()));
        particle.builder().location(location).offset(.5d, 1d, .5d).source(player).receivers(75).count(count).spawn();
    }
}
