package de.tmxx.trading.trade.inventory.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradingState;
import de.tmxx.trading.trade.inventory.TradeInventoryBuilder;
import de.tmxx.trading.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Project: trading
 * 02.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeInventoryBuilderImpl implements TradeInventoryBuilder {
    private static final int INVENTORY_SIZE = 54;

    private static final ItemStack BORDER_ITEM;
    private static final int[] BORDER_SLOTS = new int[] { 4, 13, 22, 31, 49 };

    private static final int[] PARTNER_STATUS_SLOTS = new int[] { 6, 7, 14, 15, 16, 17, 23, 24, 25, 26 };
    private static final Map<Integer, Integer> PARTNER_SLOT_MAP;
    private static final int[] OWN_STATUS_SLOTS = new int[] { 1, 2, 18, 19, 20, 21 };

    private final Trade trade;
    private final User user;

    private Inventory inventory = null;

    @Inject
    TradeInventoryBuilderImpl(@Assisted Trade trade, @Assisted User user) {
        this.trade = trade;
        this.user = user;
    }

    @Override
    public Inventory newInventory(InventoryHolder holder) {
        inventory = Bukkit.createInventory(
                holder,
                INVENTORY_SIZE,
                user.translate("inventory.name", trade.getPartner(user).getDisplayName())
        );

        setStaticItems();
        updateContent();

        return inventory;
    }

    @Override
    public void updateContent() {
        setPartnerItems();
        setValueItems();
        setStatusItems();
        setContinuationButton();
    }

    private void setStaticItems() {
        setBorder();
        setValueModifierItems();
        setPlayerHeads();
    }

    private void setBorder() {
        for (int slot : BORDER_SLOTS) {
            inventory.setItem(slot, BORDER_ITEM);
        }
    }

    private void setValueModifierItems() {
        ItemStack[] valueModifierItems = new ItemStack[] {
                getValueModifierItem(10),
                getValueModifierItem(100),
                getValueModifierItem(1000),
                getValueModifierItem(10000)
        };

        for (int i = 0; i < valueModifierItems.length; i++) {
            inventory.setItem(9 + i, valueModifierItems[i]);
        }
    }

    private ItemStack getValueModifierItem(int amount) {
        ItemStack itemStack = ItemStack.of(valueModifierMaterial(amount));
        itemStack.editMeta(meta -> {
            meta.displayName(user.translate("item.value-modifier.name", amount).decoration(TextDecoration.ITALIC, false));
            meta.lore(user.translateLore("item.value-modifier.lore"));
        });

        return itemStack;
    }

    private Material valueModifierMaterial(int amount) {
        if (amount < 100) {
            return Material.GOLD_NUGGET;
        } else if (amount < 10000) {
            return Material.GOLD_INGOT;
        } else {
            return Material.GOLD_BLOCK;
        }
    }

    private void setPlayerHeads() {
        inventory.setItem(0, getOwnHead());
        inventory.setItem(8, getPartnerHead());
    }

    private ItemStack getOwnHead() {
        return buildHead(user.getPlayer());
    }

    private ItemStack getPartnerHead() {
        return buildHead(trade.getPartner(user).getPlayer());
    }

    private ItemStack buildHead(Player player) {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.displayName(player.displayName().decoration(TextDecoration.ITALIC, false));
            meta.setPlayerProfile(player.getPlayerProfile());
        });
        return head;
    }

    private void setPartnerItems() {
        User partner = trade.getPartner(user);
        ItemStack[] contents = partner.getTradeContents();

        for (int i = 0; i < contents.length; i++) {
            inventory.setItem(PARTNER_SLOT_MAP.get(i), contents[i]);
        }
    }

    private void setValueItems() {
        inventory.setItem(3, getValueItem(user.getCurrentBid()));
        inventory.setItem(5, getValueItem(trade.getPartner(user).getCurrentBid()));
    }

    private ItemStack getValueItem(int value) {
        ItemStack itemStack = createItem(Material.EMERALD, null, value != 0);
        itemStack.editMeta(meta -> {
            meta.displayName(user.translate("item.value.name", value).decoration(TextDecoration.ITALIC, false));
        });
        return itemStack;
    }

    private void setStatusItems() {
        setStatusItems(OWN_STATUS_SLOTS, user.getTradingState());
        setStatusItems(PARTNER_STATUS_SLOTS, trade.getPartner(user).getTradingState());
    }

    private void setStatusItems(int[] slots, TradingState state) {
        ItemStack itemStack = getStatusItem(state);

        for (int slot : slots) {
            inventory.setItem(slot, itemStack);
        }
    }

    private ItemStack getStatusItem(TradingState state) {
        return switch (state) {
            case TRADING -> tradingPaneItem();
            case OFFERED -> offeredPaneItem();
            case ACCEPTED -> confirmPaneItem();
            case COMPLETION -> completionPaneItem(trade.getCountdown());
        };
    }

    private void setContinuationButton() {
        TradingState partnerState = trade.getPartner(user).getTradingState();

        ItemStack itemStack = switch (user.getTradingState()) {
            case TRADING -> user.isOfferValid() ? offerButton() : noValidOfferButton();
            case OFFERED -> partnerState.equals(TradingState.TRADING) ? waitingButton() : acceptButton();
            case ACCEPTED -> waitingButton();
            case COMPLETION -> completeButton();
        };

        inventory.setItem(40, itemStack);
    }

    private ItemStack tradingPaneItem() {
        return createItem(Material.GRAY_STAINED_GLASS_PANE, null);
    }

    private ItemStack offeredPaneItem() {
        return createItem(Material.ORANGE_STAINED_GLASS_PANE, "item.status.offered");
    }

    private ItemStack confirmPaneItem() {
        return createItem(Material.LIME_STAINED_GLASS_PANE, "item.status.confirm");
    }

    private ItemStack completionPaneItem(int countdown) {
        return createItem(Material.LIME_STAINED_GLASS_PANE, "item.status.completion").asQuantity(countdown);
    }

    private ItemStack noValidOfferButton() {
        return createItem(Material.BARRIER, "item.trade.no-valid-offer", false);
    }

    private ItemStack offerButton() {
        return createItem(Material.ORANGE_DYE, "item.trade.offer", true);
    }

    private ItemStack acceptButton() {
        return createItem(Material.LIGHT_BLUE_DYE, "item.trade.accept", true);
    }

    private ItemStack completeButton() {
        return createItem(Material.LIME_DYE, "item.trade.complete");
    }

    private ItemStack waitingButton() {
        return createItem(Material.GRAY_DYE, "item.trade.waiting");
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, false);
    }

    private ItemStack createItem(Material material, String name, boolean enchanted) {
        ItemStack itemStack = ItemStack.of(material);

        if (enchanted) itemStack.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);

        itemStack.editMeta(meta -> {
            meta.displayName(user.translate(name).decoration(TextDecoration.ITALIC, false));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        });
        return itemStack;
    }

    static {
        BORDER_ITEM = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        BORDER_ITEM.editMeta(meta -> meta.displayName(Component.empty()));

        PARTNER_SLOT_MAP = new HashMap<>();
        PARTNER_SLOT_MAP.put(0, 32);
        PARTNER_SLOT_MAP.put(1, 33);
        PARTNER_SLOT_MAP.put(2, 34);
        PARTNER_SLOT_MAP.put(3, 35);
        PARTNER_SLOT_MAP.put(4, 41);
        PARTNER_SLOT_MAP.put(5, 42);
        PARTNER_SLOT_MAP.put(6, 43);
        PARTNER_SLOT_MAP.put(7, 44);
        PARTNER_SLOT_MAP.put(8, 50);
        PARTNER_SLOT_MAP.put(9, 51);
        PARTNER_SLOT_MAP.put(10, 52);
        PARTNER_SLOT_MAP.put(11, 53);
    }
}
