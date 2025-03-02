package de.tmxx.trading.trade.inventory.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradingState;
import de.tmxx.trading.trade.inventory.TradeInventoryBuilder;
import de.tmxx.trading.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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
    private static final int[] BORDER_SLOTS = new int[] { 1, 2, 4, 6, 7, 13, 22, 31, 49 };

    private static final int[] PARTNER_STATUS_SLOTS = new int[] { 23, 24, 25, 26 };
    private static final Map<Integer, Integer> PARTNER_SLOT_MAP;
    private static final int[] OWN_STATUS_SLOTS = new int[] { 18, 19, 20, 21 };

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
        inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, user.translate("inventory.name", trade.getPartner(user).getName()));

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
        ItemStack ten = getValueModifierItem(10);
        ItemStack hundred = getValueModifierItem(100);
        ItemStack thousand = getValueModifierItem(1000);
        ItemStack tenThousand = getValueModifierItem(10000);

        inventory.setItem(9, ten);
        inventory.setItem(17, ten);

        inventory.setItem(10, hundred);
        inventory.setItem(16, hundred);

        inventory.setItem(11, thousand);
        inventory.setItem(15, thousand);

        inventory.setItem(12, tenThousand);
        inventory.setItem(14, tenThousand);
    }

    private ItemStack getValueModifierItem(int amount) {
        ItemStack itemStack = ItemStack.of(Material.GOLD_INGOT);
        itemStack.editMeta(meta -> {
            meta.displayName(user.translate("item.value-modifier.name", amount));
            meta.lore(user.translateLore("item.value-modifier.lore"));
        });

        return itemStack;
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
            meta.displayName(player.displayName());
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
        ItemStack itemStack = ItemStack.of(Material.SUGAR_CANE);
        itemStack.editMeta(meta -> {
            meta.displayName(user.translate("item.value.name", value));
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
        ItemStack itemStack = switch (user.getTradingState()) {
            case TRADING -> offerButton();
            case OFFERED -> offerWaitingButton();
            case ACCEPTED -> confirmTradeButton();
            case COMPLETION -> completeButton();
        };

        User partner = trade.getPartner(user);
        if (user.getTradingState().equals(TradingState.OFFERED) &&
                (partner.getTradingState().equals(TradingState.OFFERED) || partner.getTradingState().equals(TradingState.ACCEPTED))) {
            itemStack = offerWaitingButton();
        }

        inventory.setItem(40, itemStack);
    }

    private ItemStack tradingPaneItem() {
        ItemStack itemStack = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        itemStack.editMeta(meta -> meta.displayName(Component.empty()));
        return itemStack;
    }

    private ItemStack offeredPaneItem() {
        ItemStack itemStack = ItemStack.of(Material.ORANGE_STAINED_GLASS_PANE);
        itemStack.editMeta(meta -> meta.displayName(user.translate("item.status.offered")));
        return itemStack;
    }

    private ItemStack confirmPaneItem() {
        ItemStack itemStack = ItemStack.of(Material.LIME_STAINED_GLASS_PANE);
        itemStack.editMeta(meta -> meta.displayName(user.translate("item.status.confirm")));
        return itemStack;
    }

    private ItemStack completionPaneItem(int countdown) {
        ItemStack itemStack = ItemStack.of(Material.LIME_STAINED_GLASS_PANE).asQuantity(countdown);
        itemStack.editMeta(meta -> meta.displayName(user.translate("item.status.completion")));
        return itemStack;
    }

    private ItemStack offerButton() {
        ItemStack itemStack = ItemStack.of(Material.ORANGE_DYE);
        itemStack.editMeta(meta -> meta.displayName(user.translate("item.trade.offer")));
        return itemStack;
    }

    private ItemStack confirmTradeButton() {
        ItemStack itemStack = ItemStack.of(Material.LIGHT_BLUE_DYE);
        itemStack.editMeta(meta -> meta.displayName(user.translate("item.trade.confirm")));
        return itemStack;
    }

    private ItemStack completeButton() {
        ItemStack itemStack = ItemStack.of(Material.LIME_DYE);
        itemStack.editMeta(meta -> meta.displayName(user.translate("item.trade.complete")));
        return itemStack;
    }

    private ItemStack offerWaitingButton() {
        ItemStack itemStack = ItemStack.of(Material.LIME_DYE);
        itemStack.editMeta(meta -> meta.displayName(user.translate("item.trade.offer-waiting")));
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
