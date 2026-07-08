package zy.swthb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
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

/**
 * /back command — Return to the last death or teleport location
 * <p>
 * Can be toggled in config via backEnabled.
 */
public class BackCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("back")
                .executes(BackCommand::executeBack));
    }

    private static int executeBack(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        // Check if feature is enabled
        if (!ModConfig.getInstance().isBackEnabled()) {
            source.sendFailure(Component.translatableWithFallback(
                    "swthb.back.disabled", "返回功能已关闭"));
            return 0;
        }

        // Check if there is a back point
        BackHandler.BackPoint bp = BackHandler.getBackPoint(player);
        if (bp == null) {
            source.sendFailure(Component.translatableWithFallback(
                    "swthb.back.none", "没有可返回的位置"));
            return 0;
        }

        // Parse target dimension
        Identifier dimId = Identifier.parse(bp.world());
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimId);
        ServerLevel targetLevel = source.getServer().getLevel(dimKey);

        if (targetLevel == null) {
            source.sendFailure(Component.translatableWithFallback(
                    "swthb.back.dimension_invalid", "目标维度数据异常，无法返回"));
            return 0;
        }

        // Execute cross-dimension teleport
        TeleportTransition transition = new TeleportTransition(
                targetLevel,
                new Vec3(bp.x(), bp.y(), bp.z()),
                player.getDeltaMovement(),
                bp.yaw(),
                bp.pitch(),
                TeleportTransition.DO_NOTHING
        );
        player.teleport(transition);

        // Play Enderman teleport sound at destination
        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        // Display return source type (Death location / Pre-teleport location)
        String typeKey = bp.type() == BackHandler.LocationType.DEATH
                ? "swthb.back.type_death" : "swthb.back.type_teleport";
        String typeFallback = bp.type() == BackHandler.LocationType.DEATH
                ? "死亡地点" : "传送前位置";

        player.sendSystemMessage(
                Component.translatableWithFallback("swthb.back.success",
                        "已返回至 %s", Component.translatableWithFallback(typeKey, typeFallback))
                        .withStyle(ChatFormatting.GREEN)
        );

        return 1;
    }
}