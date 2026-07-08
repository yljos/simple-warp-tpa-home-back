package zy.swthb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import zy.swthb.handler.BackHandler;
import zy.swthb.handler.TeleportHandler;

/**
 * Direct teleport functionality.
 * Players can directly teleport to another player using /tpa.
 */
public class TpaCommand {

    /**
     * Register the /tpa command.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /tpa <target> — Teleport directly to the target player
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(TpaCommand::executeTpa)));
    }

    /**
     * Execute /tpa <target>
     */
    private static int executeTpa(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ServerPlayer target;
        try {
            target = EntityArgument.getPlayer(ctx, "target");
        } catch (Exception e) {
            source.sendFailure(Component.translatableWithFallback("swthb.tpa.player_not_found", "玩家未找到"));
            return 0;
        }

        if (player.getUUID().equals(target.getUUID())) {
            source.sendFailure(Component.translatableWithFallback("swthb.tpa.self", "不能传送到自己"));
            return 0;
        }

        // Save location for /back command
        BackHandler.saveBackPoint(player, BackHandler.LocationType.TELEPORT);

        // Teleport transition to target
        TeleportTransition transition = new TeleportTransition(
                target.level(),
                target.position(),
                player.getDeltaMovement(),
                target.getYRot(),
                target.getXRot(),
                TeleportTransition.DO_NOTHING
        );
        
        Component targetName = target.getDisplayName().copy().withStyle(ChatFormatting.AQUA);
        
        TeleportHandler.startTeleport(
                player,
                transition,
                () -> player.sendSystemMessage(
                        Component.translatableWithFallback("swthb.tpa.teleporting",
                                "正在传送到 %s ...", targetName)
                                .withStyle(ChatFormatting.GREEN)
                ),
                () -> {}
        );

        return 1;
    }
}