package me.arasple.mc.trmenu.mat;

import io.izzel.taboolib.internal.apache.lang3.math.NumberUtils;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.Variables;
import io.izzel.taboolib.util.lite.Materials;
import me.arasple.mc.trmenu.TrMenu;
import me.arasple.mc.trmenu.utils.MaterialUtils;
import me.arasple.mc.trmenu.utils.Skulls;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Arasple
 * @date 2019/10/4 16:21
 */
@SuppressWarnings({"deprecation", "AliDeprecation"})
public class Mat {

    private String mat;
    private MatType type;

    private Material material;
    private String playerHead;
    private String variableHead;
    private String textureHead;

    private int modelData;
    private byte dataValue;

    public Mat(String mat) {
        this.mat = mat.replace(' ', '_').toUpperCase();
        this.type = initType(mat);
    }

    public ItemStack createItem(Player player) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        switch (type) {
            case ORIGINAL:
                if (dataValue != 0) {
                    itemStack.setDurability(dataValue);
                }
                return itemStack;
            case MODEL_DATA:
                itemMeta.setCustomModelData(modelData);
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            case PLAYER_HEAD:
                return Skulls.getPlayerSkull(playerHead);
            case VARIABLE_HEAD:
                return Skulls.getPlayerSkull(TLocale.Translate.setPlaceholders(player, variableHead));
            case CUSTOM_HEAD:
                return Skulls.getCustomSkull(textureHead);
            default:
                return itemStack;
        }
    }

    /**
     * 判断材质类型并注入
     *
     * @param material text
     * @return 类型
     */
    public MatType initType(String material) {
        List<Variables.Variable> variable = new Variables(material).find().getVariableList().stream().filter(Variables.Variable::isVariable).collect(Collectors.toList());

        if (variable.size() >= 1) {
            String[] args = variable.get(0).getText().split(":");
            if (args.length >= 2) {
                switch (args[0]) {
                    case "MODEL-DATA":
                        if (args.length == 3) {
                            this.material = MaterialUtils.matchMaterial(args[1]);
                            this.modelData = NumberUtils.toInt(args[2], 0);
                            return MatType.MODEL_DATA;
                        }
                        break;
                    case "PLAYER-HEAD":
                        if (args.length == 2) {
                            this.material = Materials.matchMaterials("PLAYER_HEAD").parseMaterial();
                            this.playerHead = args[1];
                            return MatType.PLAYER_HEAD;
                        }
                    case "VARIABLE-HEAD":
                        if (args.length == 2) {
                            this.material = Materials.matchMaterials("PLAYER_HEAD").parseMaterial();
                            this.variableHead = args[1];
                            return MatType.VARIABLE_HEAD;
                        }
                    case "CUSTOM-HEAD":
                        if (args.length == 2) {
                            this.material = Materials.matchMaterials("PLAYER_HEAD").parseMaterial();
                            this.textureHead = args[1];
                            // 这里提前异步加载，缓存. 防止玩家首次打开卡炸
                            Bukkit.getScheduler().runTaskAsynchronously(TrMenu.getPlugin(), () -> {
                                Skulls.getCustomSkull(textureHead);
                            });
                            return MatType.CUSTOM_HEAD;
                        }
                    default:
                        break;
                }
            }
        } else {
            if (MaterialUtils.existMaterial(material)) {
                String[] args = material.replace(' ', '_').toUpperCase().split(":");
                this.material = MaterialUtils.matchMaterial(args[0]);
                this.dataValue = (byte) (args.length >= 2 ? NumberUtils.toInt(args[1], 0) : 0);
                return MatType.ORIGINAL;
            }
        }
        this.material = Material.BEDROCK;
        return MatType.UNKNOW;
    }

}