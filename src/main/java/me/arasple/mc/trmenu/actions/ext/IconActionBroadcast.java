package me.arasple.mc.trmenu.actions.ext;

import me.arasple.mc.trmenu.actions.BaseAction;
import me.arasple.mc.trmenu.actions.option.ActionOption;
import me.arasple.mc.trmenu.utils.Vars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;

import java.util.HashMap;

/**
 * @author Arasple
 * @date 2019/10/4 18:24
 */
public class IconActionBroadcast extends BaseAction {

    public IconActionBroadcast(String command, HashMap<ActionOption, String> options) {
        super(command, options);
    }

    @Override
    public void onExecute(Player player, InventoryEvent e) {
        Bukkit.getOnlinePlayers().stream().filter(p -> {
            String permission = getOptions().getOrDefault(ActionOption.PERMISSION, null);
            return permission == null || p.hasPermission(permission);
        }).forEach(p -> p.sendMessage(Vars.replace(player, getCommand())));
    }

}
