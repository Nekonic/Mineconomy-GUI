package nekonic.utils;

import nekonic.DynamicGraphPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class Create_GUI_Item {

    public static ItemStack createGUIItem(int index, boolean isIncrease) {
        ItemStack GUI_Item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) GUI_Item.getItemMeta();
        meta.setHideTooltip(true);

        // 커스텀 모델 데이터 설정
        meta.setCustomModelData(index);

        // 색상 설정 (상승이면 빨간색, 하락이면 파란색)
        if (isIncrease) {
            meta.setColor(Color.RED);
        } else {
            meta.setColor(Color.BLUE);
        }

        GUI_Item.setItemMeta(meta);
        return GUI_Item;
    }

    public static void initGUIItem(){
        ItemStack GUI_Item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = GUI_Item.getItemMeta();

        meta.displayName(Component.text("HOME"+NamedTextColor.BLUE));

    }

    public static ItemStack createCurrencyItem(int amount) {
        ItemStack currencyItem = new ItemStack(Material.GOLD_INGOT, amount);
        ItemMeta meta = currencyItem.getItemMeta();

        // 아이템 이름 설정
        meta.displayName(Component.text("Gold Coin" + NamedTextColor.GOLD));

        // 아이템 설명 추가
        meta.lore(
                Component.text("Used as currency in Mineconomy", NamedTextColor.DARK_AQUA).children()
        );

        // 커스텀 모델 데이터 추가
        meta.setCustomModelData(1001); // 1001번 커스텀 모델 데이터 설정
        currencyItem.setItemMeta(meta);

        return currencyItem;
    }
}