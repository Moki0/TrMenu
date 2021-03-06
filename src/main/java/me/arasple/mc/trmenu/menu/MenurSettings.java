package me.arasple.mc.trmenu.menu;

/**
 * @author Arasple
 * @date 2019/10/4 17:21
 */
public enum MenurSettings {

    /**
     * 菜单的设置
     */
    MENU_TITLE("title"),
    MENU_SHAPE("shape"),
    MENU_TYPE("type"),

    MENU_OPEN_REQUIREMENT("open-requirement(s)?"),
    MENU_OPEN_DENY_ACTIONS("open-deny-action(s)?"),
    MENU_CLOSE_REQUIREMENT("close-requirement(s)?"),
    MENU_CLOSE_DENY_ACTIONS("close-deny-action(s)?"),

    MENU_OPEN_COMAMNDS("open-commands"),
    MENU_OPEN_ACTIONS("open-actions"),
    MENU_CLOSE_ACTIONS("close-actions"),

    MENU_OPTIONS("option(s)?"),
    MENU_OPTIONS_DEPEND_EXPANSIONS("depend-expansion(s)?"),
    MENU_OPTIONS_LOCKHAND("lock-player-inv"),
    MENU_OPTIONS_ARGS("transfer-args"),
    MENU_OPTIONS_FORCEARGS("force-transfer-args"),
    MENU_OPTIONS_BINDLORES("bind-item-lore"),

    MENU_BUTTONS("button(s)?"),

    /**
     * 按钮各种设置
     */
    BUTTON_UPDATE_PERIOD("update(s)?"),
    BUTTON_REFRESH_CONDITIONS("refresh-condition(s)?"),
    BUTTON_ICONS("icons"),
    BUTTON_ICONS_CONDITION("condition"),

    ICON_DISPLAY_NAME("name(s)?|displayname(s)?"),
    ICON_DISPLAY_MATERIALS("material(s)?|id(s)?|mat(s)?"),
    ICON_DISPLAY_LORES("lore(s)?"),
    ICON_DISPLAY_SLOTS("slot(s)?"),
    ICON_DISPLAY_FLAGS("flag(s)?"),
    ICON_DISPLAY_SHINY("shiny|glow"),
    ICON_DISPLAY_AMOUNT("amount(s)?");

    private String name;

    MenurSettings(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
