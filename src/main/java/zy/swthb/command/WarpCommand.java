// WarpCommand.java
package zy.swthb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import zy.swthb.config.ModConfig;
import zy.swthb.handler.BackHandler;
import zy.swthb.handler.TeleportHandler;
import zy.swthb.util.DimensionDisplay;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Warp public teleport commands: warp, warps, setwarp, delwarp
 */
public class WarpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /warp <name>
        dispatcher.register(Commands.literal("warp")
                .requires(source -> Permissions.check(source, "swthb.command.warp", 4))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .suggests(WarpCommand::suggestWarps)
                        .executes(WarpCommand::executeWarp)));

        // /warps
        dispatcher.register(Commands.literal("warps")
                .requires(source -> Permissions.check(source, "swthb.command.warps", 4))
                .executes(WarpCommand::executeWarps));

        // /setwarp <name>
        dispatcher.register(Commands.literal("setwarp")
                .requires(source -> Permissions.check(source, "swthb.command.setwarp", 4))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .suggests(WarpCommand::suggestWarps)
                        .executes(WarpCommand::executeSetWarp)));

        // /delwarp <name>
        dispatcher.register(Commands.literal("delwarp")
                .requires(source -> Permissions.check(source, "swthb.command.delwarp", 4))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .suggests(WarpCommand::suggestWarps)
                        .executes(WarpCommand::executeDelWarp)));
    }

    // ========== /warp <name> ==========

    private static int executeWarp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(ctx, "name");

        ModConfig config = ModConfig.getInstance();
        ModConfig.WarpEntry warp = config.getWarp(name);

        if (warp == null) {
            source.sendFailure(
                    Component.translatableWithFallback("swthb.warp.not_found",
                            "找不到 \"%s\"", name)
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        Identifier dimId = Identifier.parse(warp.getWorld());
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimId);
        ServerLevel targetLevel = source.getServer().getLevel(dimKey);

        if (targetLevel == null) {
            source.sendFailure(Component.translatableWithFallback(
                    "swthb.warp.dimension_invalid", "维度数据异常，无法传送"));
            return 0;
        }

        // Save location for /back command
        BackHandler.saveBackPoint(player, BackHandler.LocationType.TELEPORT);

        TeleportTransition transition = new TeleportTransition(
                targetLevel,
                new Vec3(warp.getX(), warp.getY(), warp.getZ()),
                player.getDeltaMovement(),
                warp.getYaw(),
                warp.getPitch(),
                TeleportTransition.DO_NOTHING
        );
        TeleportHandler.startTeleport(
                player,
                transition,
                () -> {
                    player.sendSystemMessage(
                            Component.translatableWithFallback("swthb.warp.teleported",
                                    "已到达 \"%s\"", name)
                                    .withStyle(ChatFormatting.GREEN)
                    );
                    
                    // Play Enderman teleport sound
                    player.level().playSound(
                            null, 
                            player.blockPosition(), 
                            SoundEvents.ENDERMAN_TELEPORT, 
                            SoundSource.PLAYERS, 
                            1.0F, 
                            1.0F
                    );
                },
                () -> {}
        );

        return 1;
    }

    // ========== /warps ==========

    private static int executeWarps(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ModConfig config = ModConfig.getInstance();
        Map<String, ModConfig.WarpEntry> warps = config.getWarps();

        Component title = Component.translatableWithFallback("swthb.warp.list_title",
                "=== 信号阵列 (%s) ===", String.valueOf(warps.size()))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        player.sendSystemMessage(title);

        if (warps.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatableWithFallback("swthb.warp.empty",
                            "暂无，请添加")
                            .withStyle(ChatFormatting.GRAY)
            );
            return 1;
        }

        int index = 1;
        for (Map.Entry<String, ModConfig.WarpEntry> entry : warps.entrySet()) {
            String warpName = entry.getKey();
            ModConfig.WarpEntry w = entry.getValue();
            Component dimDisplay = DimensionDisplay.of(w.getWorld());

            Component nameComp = Component.literal(index + ". " + warpName)
                    .withStyle(style -> style
                            .withColor(ChatFormatting.LIGHT_PURPLE)
                            .withClickEvent(new ClickEvent.RunCommand("/warp " + warpName))
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.translatableWithFallback("swthb.warp.click_teleport",
                                            "飞向 \"%s\"", warpName))));

            Component infoComp = Component.literal("  [")
                    .withStyle(ChatFormatting.GRAY)
                    .append(dimDisplay)
                    .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("%.0f, %.0f, %.0f", w.getX(), w.getY(), w.getZ()))
                            .withStyle(ChatFormatting.DARK_GRAY));

            player.sendSystemMessage(Component.literal("").append(nameComp).append(infoComp));
            index++;
        }

        return 1;
    }

    // ========== /setwarp <name> ==========

    private static int executeSetWarp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(ctx, "name");

        ModConfig config = ModConfig.getInstance();

        boolean isUpdate = config.getWarp(name) != null;

        ModConfig.WarpEntry entry = new ModConfig.WarpEntry(
                player.level().dimension().identifier().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        );

        config.setWarp(name, entry);

        if (isUpdate) {
            source.sendSuccess(() ->
                    Component.translatableWithFallback("swthb.warp.updated",
                            "已更新 \"%s\"", name)
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
        } else {
            source.sendSuccess(() ->
                    Component.translatableWithFallback("swthb.warp.set_success",
                            "已设置 \"%s\"", name)
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
        }

        return 1;
    }

    // ========== /delwarp <name> ==========

    private static int executeDelWarp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(ctx, "name");

        ModConfig config = ModConfig.getInstance();
        if (config.removeWarp(name)) {
            source.sendSuccess(() ->
                    Component.translatableWithFallback("swthb.warp.deleted",
                            "已删除 \"%s\"", name)
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
            return 1;
        } else {
            source.sendFailure(
                    Component.translatableWithFallback("swthb.warp.not_found",
                            "找不到 \"%s\"", name)
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }
    }

    // ========== TAB Completion ==========

    private static CompletableFuture<Suggestions> suggestWarps(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ModConfig config = ModConfig.getInstance();
        for (String name : config.getWarps().keySet()) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    }

}