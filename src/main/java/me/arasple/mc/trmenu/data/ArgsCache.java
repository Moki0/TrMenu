package me.arasple.mc.trmenu.data;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Arasple
 * @date 2019/10/4 20:04
 */
public class ArgsCache {

    private static HashMap<UUID, String[]> args = new HashMap<>();

    public static HashMap<UUID, String[]> getArgs() {
        return args;
    }

    public static String[] getPlayerArgs(Player player) {
        return args.getOrDefault(player.getUniqueId(), new String[0]);
    }

}
