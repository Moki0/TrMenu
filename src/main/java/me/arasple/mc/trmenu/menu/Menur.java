package me.arasple.mc.trmenu.menu;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.Strings;
import me.arasple.mc.trmenu.TrMenu;
import me.arasple.mc.trmenu.actions.ActionRunner;
import me.arasple.mc.trmenu.actions.BaseAction;
import me.arasple.mc.trmenu.api.events.MenuOpenEvent;
import me.arasple.mc.trmenu.bstats.Metrics;
import me.arasple.mc.trmenu.data.ArgsCache;
import me.arasple.mc.trmenu.display.Button;
import me.arasple.mc.trmenu.display.Icon;
import me.arasple.mc.trmenu.display.Item;
import me.arasple.mc.trmenu.utils.JavaScript;
import me.arasple.mc.trmenu.utils.Vars;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Arasple
 * @date 2019/10/4 14:09
 */
public class Menur {

    private String name;
    private InventoryType invType;
    private String title;
    private int rows;
    private HashMap<Button, List<Integer>> buttons;
    private List<String> openCommands;
    private List<BaseAction> openActions, closeActions;

    private String openRequirement, closeRequirement;
    private List<BaseAction> openDenyActions, closeDenyActions;

    private boolean lockPlayerInv;
    private boolean transferArgs;
    private int forceTransferArgsAmount;
    private List<String> bindItemLore;
    private List<String> dependExpansions;

    public Menur(String name, String title, InventoryType invType, int rows, HashMap<Button, List<Integer>> buttons, String openRequirement, List<BaseAction> openDenyActions, String closeRequirement, List<BaseAction> closeDenyActions, List<String> openCommands, List<BaseAction> openActions, List<BaseAction> closeActions, boolean lockPlayerInv, boolean transferArgs, int forceTransferArgsAmount, List<String> bindItemLore, List<String> dependExpansions) {
        this.name = name;
        this.title = title;
        this.invType = invType;
        this.rows = rows;
        this.buttons = buttons;
        this.openRequirement = openRequirement;
        this.openDenyActions = openDenyActions;
        this.closeRequirement = closeRequirement;
        this.closeDenyActions = closeDenyActions;
        this.openCommands = openCommands;
        this.openActions = openActions;
        this.closeActions = closeActions;
        this.lockPlayerInv = lockPlayerInv;
        this.transferArgs = transferArgs;
        this.forceTransferArgsAmount = forceTransferArgsAmount;
        this.bindItemLore = bindItemLore;
        this.dependExpansions = dependExpansions;
        checkDepends();
    }

    public void open(Player player, String... args) {
        Metrics.increase(0);

        MenuOpenEvent event = new MenuOpenEvent(player, this);
        if (event.isCancelled()) {
            return;
        }
        if (event.getMenu() != this) {
            event.getMenu().open(player, args);
            return;
        }
        if (!Strings.isBlank(openRequirement) && !(boolean) JavaScript.run(player, openRequirement)) {
            event.setCancelled(true);
            ActionRunner.runActions(getOpenDenyActions(), player, null);
            return;
        }
        List<String> unInstalledDepends = checkDepends();
        if (unInstalledDepends.size() > 0) {
            TLocale.sendTo(player, "MENU.DEPEND-EXPANSIONS-REQUIRED", Arrays.toString(unInstalledDepends.toArray()));
            event.setCancelled(true);
            return;
        }
        ArgsCache.getArgs().put(player.getUniqueId(), args);
        Inventory menu = invType == null ? Bukkit.createInventory(new MenurHolder(this), 9 * rows, Vars.replace(player, title)) : Bukkit.createInventory(new MenurHolder(this), invType, Vars.replace(player, title));

        // 初始化设置
        buttons.forEach((button, slots) -> Bukkit.getScheduler().runTaskAsynchronously(TrMenu.getPlugin(), () -> {
            button.refreshConditionalIcon(player, null);
            Item item = button.getCurrentIcon().getItem();
            ItemStack itemStack = item.createItemStack(player, args);
            for (int i : item.getSlots().size() > 0 ? item.getNextSlots(menu) : slots) {
                if (menu.getSize() > i) {
                    menu.setItem(i, itemStack);
                }
            }
        }));
        // 开始刷新
        buttons.forEach((button, slots) -> {
            // 判断刷新周期是否合法
            if (slots != null && button.getUpdate() >= 1) {
                int update = Math.max(button.getUpdate(), 3);
                // 创建 Runnable
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        long start = System.currentTimeMillis();
                        // 如果该菜单已被玩家关闭, 则取消刷新进程
                        if (!player.getOpenInventory().getTopInventory().equals(menu)) {
                            cancel();
                            return;
                        }

                        Item item = button.getCurrentIcon().getItem();
                        ItemStack itemStack = item.createItemStack(player, args);

                        if (player.hasMetadata("TrMenu-Debug")) {
                            if (item == null || itemStack == null) {
                                return;
                            }
                            ItemMeta meta = itemStack.getItemMeta();
                            if (meta != null) {
                                List<String> lore = meta.hasLore() ? meta.getLore() : Lists.newArrayList();
                                lore.add("");
                                lore.add("§8[TrMenu-Debug] §7Last Refresh: §8" + System.currentTimeMillis() + "; §7Took: §8" + (System.currentTimeMillis() - start) + "ms.");
                                lore.add("§8[TrMenu-Debug] §7Mats: " + button.getCurrentIcon().getItem().getMaterials().size() + "; Index: " + Arrays.toString(button.getCurrentIcon().getItem().getIndexs()));
                                meta.setLore(lore);
                                itemStack.setItemMeta(meta);
                            }
                        }
                        for (int i : item.getSlots().size() > 0 ? item.getNextSlots(menu) : slots) {
                            if (menu.getSize() > i) {
                                menu.setItem(i, itemStack);
                            }
                        }
                        // 清理残留
                        clearEmptySlots(menu, item.getSlots());
                    }
                }.runTaskTimerAsynchronously(TrMenu.getPlugin(), update, update);
            }
            // 判断重新计算优先级
            if (slots != null && button.getRefreshConditions() >= 1 && button.getConditionalIcons().size() > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        button.refreshConditionalIcon(player, null);
                    }
                }.runTaskTimer(TrMenu.getPlugin(), button.getRefreshConditions(), button.getRefreshConditions());
            }
        });
        // 为玩家打开此菜单
        Bukkit.getScheduler().runTaskLater(TrMenu.getPlugin(), () -> {
            player.openInventory(menu);
            if (getOpenActions() != null) {
                getOpenActions().forEach(action -> action.onExecute(player, null));
            }
        }, 2);
    }

    /**
     * 检测菜单需要的 PAPI 依赖并自动下载
     *
     * @return 未安装的
     */
    private List<String> checkDepends() {
        List<String> unInstalled = Lists.newArrayList();
        if (getDependExpansions() != null && getDependExpansions().size() > 0) {
            if (PlaceholderAPIPlugin.getInstance().getExpansionCloud().getCloudExpansions().isEmpty()) {
                PlaceholderAPIPlugin.getInstance().getExpansionCloud().fetch(false);
            }
            unInstalled = getDependExpansions().stream().filter(d -> PlaceholderAPI.getExpansions().stream().noneMatch(e -> e.getName().equalsIgnoreCase(d)) && PlaceholderAPIPlugin.getInstance().getExpansionCloud().getCloudExpansion(d) != null && !PlaceholderAPIPlugin.getInstance().getExpansionCloud().isDownloading(d)).collect(Collectors.toList());
            if (unInstalled.size() > 0) {
                unInstalled.forEach(ex -> {
                    CloudExpansion cloudExpansion = PlaceholderAPIPlugin.getInstance().getExpansionCloud().getCloudExpansion(ex);
                    PlaceholderAPIPlugin.getInstance().getExpansionCloud().downloadExpansion(null, cloudExpansion);
                });
                Bukkit.getScheduler().runTaskLater(TrMenu.getPlugin(), () -> PlaceholderAPIPlugin.getInstance().getExpansionManager().registerAllExpansions(), 20);
            }
        }
        return unInstalled;
    }

    public Button getButton(int slot) {
        if (slot < 0) {
            return null;
        }
        for (Map.Entry<Button, List<Integer>> entry : buttons.entrySet()) {
            Icon icon = entry.getKey().getCurrentIcon();
            if (icon.getItem().getCurrentSlots() != null && icon.getItem().getCurrentSlots().contains(slot)) {
                return entry.getKey();
            } else if (entry.getValue().contains(slot)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void clearEmptySlots(Inventory menu, List<List<Integer>> slots) {
        slots.forEach(s -> s.forEach(i -> {
            if (menu.getItem(i) != null && getButton(i) == null) {
                if (menu.getSize() > i) {
                    menu.setItem(i, null);
                }
            }
        }));
    }

    /*
    GETTERS & SETTERS
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InventoryType getInvType() {
        return invType;
    }

    public void setInvType(InventoryType invType) {
        this.invType = invType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public HashMap<Button, List<Integer>> getButtons() {
        return buttons;
    }

    public void setButtons(HashMap<Button, List<Integer>> buttons) {
        this.buttons = buttons;
    }

    public List<String> getOpenCommands() {
        return openCommands;
    }

    public void setOpenCommands(List<String> openCommands) {
        this.openCommands = openCommands;
    }

    public List<BaseAction> getOpenActions() {
        return openActions;
    }

    public void setOpenActions(List<BaseAction> openActions) {
        this.openActions = openActions;
    }

    public List<BaseAction> getCloseActions() {
        return closeActions;
    }

    public void setCloseActions(List<BaseAction> closeActions) {
        this.closeActions = closeActions;
    }

    public String getOpenRequirement() {
        return openRequirement;
    }

    public void setOpenRequirement(String openRequirement) {
        this.openRequirement = openRequirement;
    }

    public List<BaseAction> getOpenDenyActions() {
        return openDenyActions;
    }

    public void setOpenDenyActions(List<BaseAction> openDenyActions) {
        this.openDenyActions = openDenyActions;
    }

    public String getCloseRequirement() {
        return closeRequirement;
    }

    public void setCloseRequirement(String closeRequirement) {
        this.closeRequirement = closeRequirement;
    }

    public List<BaseAction> getCloseDenyActions() {
        return closeDenyActions;
    }

    public void setCloseDenyActions(List<BaseAction> closeDenyActions) {
        this.closeDenyActions = closeDenyActions;
    }

    public boolean isLockPlayerInv() {
        return lockPlayerInv;
    }

    public void setLockPlayerInv(boolean lockPlayerInv) {
        this.lockPlayerInv = lockPlayerInv;
    }

    public boolean isTransferArgs() {
        return transferArgs;
    }

    public void setTransferArgs(boolean transferArgs) {
        this.transferArgs = transferArgs;
    }

    public int getForceTransferArgsAmount() {
        return forceTransferArgsAmount;
    }

    public void setForceTransferArgsAmount(int forceTransferArgsAmount) {
        this.forceTransferArgsAmount = forceTransferArgsAmount;
    }

    public List<String> getBindItemLore() {
        return bindItemLore;
    }

    public void setBindItemLore(List<String> bindItemLore) {
        this.bindItemLore = bindItemLore;
    }

    public List<String> getDependExpansions() {
        return dependExpansions;
    }

    public void setDependExpansions(List<String> dependExpansions) {
        this.dependExpansions = dependExpansions;
    }

}
