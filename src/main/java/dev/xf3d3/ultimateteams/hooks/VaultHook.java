package dev.xf3d3.ultimateteams.hooks;

import dev.xf3d3.ultimateteams.UltimateTeams;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultHook {

    private final Economy economyAPI;
    private final UltimateTeams plugin;

    public VaultHook(@NotNull UltimateTeams plugin) {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            throw new IllegalStateException("Could not resolve economy provider");
        }

        this.economyAPI = economyProvider.getProvider();
        this.plugin = plugin;
        sendMessages();
    }

    public boolean hasMoney(@NotNull Player player, @NotNull Double amount) {
        return validAmount(amount) && economyAPI.has(player, amount);
    }

    public String getCurrencyNameSingular() { return economyAPI.currencyNameSingular(); }

    public String getCurrencyNamePlural() { return economyAPI.currencyNamePlural(); }

    public boolean takeMoney(@NotNull Player player, @NotNull Double amount) {
        return validAmount(amount) && economyAPI.withdrawPlayer(player, amount).transactionSuccess();
    }

    public void giveMoney(@NotNull Player player, @NotNull Double amount) {
        tryGiveMoney(player, amount);
    }

    /** Keep giveMoney's existing API while exposing failures to transaction callers. */
    public boolean tryGiveMoney(@NotNull Player player, @NotNull Double amount) {
        return validAmount(amount) && economyAPI.depositPlayer(player, amount).transactionSuccess();
    }

    private static boolean validAmount(Double amount) {
        return amount != null && Double.isFinite(amount) && amount >= 0;
    }

    private void sendMessages() {
        plugin.sendConsole("-------------------------------------------");
        plugin.sendConsole("&6UltimateTeams: &3Hooked into Vault");
        plugin.sendConsole("&6UltimateTeams: &3Economy features enabled!");
        plugin.sendConsole("-------------------------------------------");
    }
}