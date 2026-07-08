package zy.swthb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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

/**
 * Single home command: /home, /sethome
 */
public class HomeCommand {

    private static final String HOME_NAME = "default"; // Internal fixed name

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /home
        dispatcher.register(Commands.literal("home")
                .executes(HomeCommand::executeHome));

        // /sethome
        dispatcher.register(Commands.literal("sethome")
                .executes(HomeCommand::executeSetHome));
    }

    // ========== /home ==========

    private static int executeHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ModConfig config = ModConfig.getInstance();
        ModConfig.HomeEntry home = config.getHome(player.getUUID(), HOME_NAME);

        if (home == null) {
            source.sendFailure(
                    Component.translatableWithFallback("swthb.home.not_found", "你还没有设置家")
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        Identifier dimId = Identifier.parse(home.getWorld());
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimId);
        ServerLevel targetLevel = source.getServer().getLevel(dimKey);

        if (targetLevel == null) {
            source.sendFailure(Component.translatableWithFallback(
                    "swthb.home.dimension_invalid", "家的维度数据异常，无法传送"));
            return 0;
        }

        // Save location for /back command
        BackHandler.saveBackPoint(player, BackHandler.LocationType.TELEPORT);

        TeleportTransition transition = new TeleportTransition(
                targetLevel,
                new Vec3(home.getX(), home.getY(), home.getZ()),
                player.getDeltaMovement(),
                home.getYaw(),
                home.getPitch(),
                TeleportTransition.DO_NOTHING
        );
        TeleportHandler.startTeleport(
                player,
                transition,
                () -> {
                    player.sendSystemMessage(
                            Component.translatableWithFallback("swthb.home.teleported", "已传送到家")
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

    // ========== /sethome ==========

    private static int executeSetHome(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ModConfig config = ModConfig.getInstance();

        ModConfig.HomeEntry entry = new ModConfig.HomeEntry(
                HOME_NAME,
                player.level().dimension().identifier().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        );

        // Set or overwrite the single home
        config.setHome(player.getUUID(), entry);

        player.sendSystemMessage(
                Component.translatableWithFallback("swthb.home.set_success", "已设置家")
                        .withStyle(ChatFormatting.GREEN)
        );

        return 1;
    }
}