package de.tmxx.trading.trade.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.trading.trade.Trade;
import de.tmxx.trading.trade.TradeInventory;
import de.tmxx.trading.trade.TradingState;
import de.tmxx.trading.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Project: trading
 * 28.02.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TradeInventoryImpl implements TradeInventory, InventoryHolder, Listener {
    private final JavaPlugin plugin;
    private final Trade trade;
    private final User user;

    private Inventory inventory = null;

    @Inject
    TradeInventoryImpl(JavaPlugin plugin, @Assisted Trade trade, @Assisted User user) {
        this.plugin = plugin;
        this.trade = trade;
        this.user = user;
    }

    @Override
    public @NotNull Inventory getInventory() {
        buildInventoryIfNotExists();
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getInventory().getHolder(false) != this) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.getUniqueId().equals(user.getUniqueId())) return;

        if (!ALLOWED_ACTIONS.contains(event.getAction())) {
            event.setResult(Event.Result.DENY);
            return;
        }

        // allow all clicks in the players own inventory
        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) return;

        if (!ALLOWED_SLOTS_TO_MOVE.contains(event.getRawSlot())) {
            event.setResult(Event.Result.DENY);
            // TODO: perform action
            return;
        }

        registerEdit();
        Bukkit.getScheduler().runTask(plugin, () -> {
            updateOwnItems();
            update();
            updatePartnerInventory();
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) != this) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.getUniqueId().equals(user.getUniqueId())) return;

        for (Integer slot : event.getNewItems().keySet()) {
            if (ALLOWED_SLOTS_TO_MOVE.contains(slot)) continue;

            event.setResult(Event.Result.DENY);
            return;
        }

        registerEdit();
        Bukkit.getScheduler().runTask(plugin, () -> {
            updateOwnItems();
            update();
            updatePartnerInventory();
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().getUniqueId().equals(user.getUniqueId())) return;

        trade.cancel(user);
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().getUniqueId().equals(user.getUniqueId())) return;

        trade.cancel(user);
        HandlerList.unregisterAll(this);
    }

    @Override
    public void open() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        user.getPlayer().openInventory(getInventory());
    }

    @Override
    public void update() {
        setPartnerItems();
        setValueItems();
        setStatusItems();
        setContinuationButton();
    }

    private void buildInventoryIfNotExists() {
        if (inventory != null) return;

        buildInventory();
        update();
    }

    private void buildInventory() {
        inventory = Bukkit.createInventory(this, INVENTORY_SIZE, user.translate("inventory.name", trade.getPartner(user).getName()));
        setBorder();
        setValueModifierItems();
        setPlayerHeads();
    }

    private void updatePartnerInventory() {
        trade.getPartner(user).getInventory().update();
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

    private void setPlayerHeads() {
        inventory.setItem(0, getOwnHead());
        inventory.setItem(8, getPartnerHead());
    }

    private ItemStack getValueModifierItem(int amount) {
        ItemStack itemStack = ItemStack.of(Material.GOLD_INGOT);
        itemStack.editMeta(meta -> {
            meta.displayName(user.translate("item.value-modifier.name", amount));
            meta.lore(user.translateLore("item.value-modifier.lore"));
        });

        return itemStack;
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

    private void updateOwnItems() {
        ItemStack[] contents = new ItemStack[OWN_SLOT_TO_ITEM_MAP.size()];
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (!OWN_SLOT_TO_ITEM_MAP.containsKey(slot)) continue;
            contents[OWN_SLOT_TO_ITEM_MAP.get(slot)] = inventory.getItem(slot);
        }

        user.setTradeContents(contents);
    }

    private void registerEdit() {
        if (user.getTradingState().equals(TradingState.TRADING)) return;

        user.setTradingState(TradingState.TRADING);
        trade.getPartner(user).setTradingState(TradingState.TRADING);
        trade.resetCountdown();

        update();
        updatePartnerInventory();
    }

    private static final int INVENTORY_SIZE = 54;
    private static final ItemStack BORDER_ITEM;

    private static final int[] BORDER_SLOTS = new int[] { 1, 2, 4, 6, 7, 13, 22, 31, 49 };
    private static final int[] OWN_STATUS_SLOTS = new int[] { 18, 19, 20, 21 };
    private static final Map<Integer, Integer> OWN_SLOT_TO_ITEM_MAP = new HashMap<>();
    private static final Map<Integer, Integer> OWN_ITEM_TO_SLOT_MAP = new HashMap<>();

    private static final int[] PARTNER_STATUS_SLOTS = new int[] { 23, 24, 25, 26 };
    private static final Map<Integer, Integer> PARTNER_SLOT_MAP = new HashMap<>();

    private static final Set<InventoryAction> ALLOWED_ACTIONS = Set.of(
            InventoryAction.NOTHING,
            InventoryAction.PICKUP_ALL,
            InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.PLACE_ALL,
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.SWAP_WITH_CURSOR
    );
    private static final Set<Integer> ALLOWED_SLOTS_TO_MOVE = Set.of(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48);

    static {
        BORDER_ITEM = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        BORDER_ITEM.editMeta(meta -> meta.displayName(Component.empty()));

        OWN_ITEM_TO_SLOT_MAP.put(0, 27);
        OWN_ITEM_TO_SLOT_MAP.put(1, 28);
        OWN_ITEM_TO_SLOT_MAP.put(2, 29);
        OWN_ITEM_TO_SLOT_MAP.put(3, 30);
        OWN_ITEM_TO_SLOT_MAP.put(4, 36);
        OWN_ITEM_TO_SLOT_MAP.put(5, 37);
        OWN_ITEM_TO_SLOT_MAP.put(6, 38);
        OWN_ITEM_TO_SLOT_MAP.put(7, 39);
        OWN_ITEM_TO_SLOT_MAP.put(8, 45);
        OWN_ITEM_TO_SLOT_MAP.put(9, 46);
        OWN_ITEM_TO_SLOT_MAP.put(10, 47);
        OWN_ITEM_TO_SLOT_MAP.put(11, 48);

        OWN_SLOT_TO_ITEM_MAP.putAll(OWN_ITEM_TO_SLOT_MAP.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));

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
