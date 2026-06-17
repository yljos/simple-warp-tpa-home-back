package zy.swth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import zy.swth.config.ModConfig;

/**
 * /swthconfig 命令 — 修改模组配置项（需要管理员权限）
 */
public class SwthConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registry,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("swthconfig")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.literal("maxHomes")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(SwthConfigCommand::executeSetMaxHomes)))
                .then(Commands.literal("maxWarps")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(SwthConfigCommand::executeSetMaxWarps)))
                .then(Commands.literal("reload")
                        .executes(SwthConfigCommand::executeReload))
                .then(Commands.literal("save")
                        .executes(SwthConfigCommand::executeSave))
        );
    }

    private static int executeSetMaxHomes(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        ModConfig config = ModConfig.getInstance();
        int oldValue = config.getMaxHomes();
        config.setMaxHomes(amount);

        source.sendSuccess(() ->
                Component.translatableWithFallback("swth.config.max_homes_set",
                                "已将每名玩家最大家的数量从 %s 修改为 %s",
                                String.valueOf(oldValue), String.valueOf(amount))
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        return 1;
    }

    private static int executeSetMaxWarps(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        ModConfig config = ModConfig.getInstance();
        int oldValue = config.getMaxWarps();
        config.setMaxWarps(amount);

        source.sendSuccess(() ->
                Component.translatableWithFallback("swth.config.max_warps_set",
                                "已将 Warp 最大数量从 %s 修改为 %s",
                                String.valueOf(oldValue), String.valueOf(amount))
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        return 1;
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        ModConfig.load();

        source.sendSuccess(() ->
                Component.translatableWithFallback("swth.config.reloaded",
                        "配置文件已从磁盘重新加载").withStyle(ChatFormatting.GREEN),
                true
        );

        return 1;
    }

    private static int executeSave(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        ModConfig.getInstance().save();

        source.sendSuccess(() ->
                Component.translatableWithFallback("swth.config.saved",
                        "配置文件已保存到磁盘").withStyle(ChatFormatting.GREEN),
                true
        );

        return 1;
    }
}
