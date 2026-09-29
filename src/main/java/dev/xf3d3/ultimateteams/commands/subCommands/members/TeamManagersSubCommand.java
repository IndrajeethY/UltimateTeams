package dev.xf3d3.ultimateteams.commands.subCommands.members;

import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.models.Team;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamManagersSubCommand {
    private static final String TEAM_PLACEHOLDER = "%TEAM%";
    private static final String PLAYER_TO_KICK = "%KICKEDPLAYER%";

    private final UltimateTeams plugin;

    public TeamManagersSubCommand(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
    }

    public void teamPromoteSubCommand(CommandSender sender, OfflinePlayer offlinePlayer) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(MineDown.parse(plugin.getMessages().getGeneral().getPlayerOnlyCommand()));
            return;
        }

        if (!offlinePlayer.hasPlayedBefore() || offlinePlayer.getName() == null) {
            player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getKick().getPlayerNotFound().replace(PLAYER_TO_KICK, offlinePlayer.toString())));
            return;
        }

        plugin.getTeamStorageUtil().findTeamByMember(player.getUniqueId()).ifPresentOrElse(
                team -> {
                    // Check permission
                    if (!(team.getOwner().equals(player.getUniqueId()) || (team.getMembers().getOrDefault(player.getUniqueId(), 0) == 2 && team.hasPermission(Team.Permission.PROMOTE)))) {
                        sender.sendMessage(MineDown.parse(plugin.getMessages().getGeneral().getNoPermission()));
                        return;
                    }


                    if (player.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                        player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getManager().getPromoteSelfError()));

                        return;
                    }

                    if (!team.getMembers().containsKey(offlinePlayer.getUniqueId())) {
                        player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getKick().getTargetNotInTeam().replace(PLAYER_TO_KICK, offlinePlayer.getName())));
                        return;
                    }

                    // Promotion must never demote the owner or create membership.
                    if (!team.getMembers().replace(offlinePlayer.getUniqueId(), 1, 2)) {
                        sender.sendMessage(MineDown.parse(plugin.getMessages().getGeneral().getNoPermission()));
                        return;
                    }
                    plugin.runAsync(task -> plugin.getTeamStorageUtil().updateTeamData(player, team));

                    player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getManager().getPromoteSuccessful().replace("%PLAYER%", offlinePlayer.getName())));
                },
                () -> player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getInfo().getNotInTeam()))
        );
    }

    public void teamDemoteSubCommand(CommandSender sender, OfflinePlayer offlinePlayer) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(MineDown.parse(plugin.getMessages().getGeneral().getPlayerOnlyCommand()));
            return;
        }

        if (!offlinePlayer.hasPlayedBefore() || offlinePlayer.getName() == null) {
            player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getKick().getPlayerNotFound().replace(PLAYER_TO_KICK, offlinePlayer.toString())));
            return;
        }

        if (!plugin.getTeamStorageUtil().isTeamOwner(player)) {
            sender.sendMessage(MineDown.parse(plugin.getMessages().getGeneral().getMustBeOwner()));
            return;
        }

        plugin.getTeamStorageUtil().findTeamByOwner(player.getUniqueId()).ifPresentOrElse(
                team -> {

                    if (player.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                        player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getManager().getDemoteSelfError()));

                        return;
                    }

                    if (!team.getMembers().containsKey(offlinePlayer.getUniqueId())) {

                        player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getKick().getTargetNotInTeam().replace(PLAYER_TO_KICK, offlinePlayer.getName())));
                        return;
                    }

                    if (!team.getMembers().replace(offlinePlayer.getUniqueId(), 2, 1)) {
                        sender.sendMessage(MineDown.parse(plugin.getMessages().getGeneral().getNoPermission()));
                        return;
                    }
                    plugin.runAsync(task -> plugin.getTeamStorageUtil().updateTeamData(player, team));

                    player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getManager().getDemoteSuccessful().replace("%PLAYER%", offlinePlayer.getName())));
                },
                () -> player.sendMessage(MineDown.parse(plugin.getMessages().getTeam().getInfo().getNotInTeam()))
        );
    }
}
