package zy.swthb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import zy.swthb.config.ModConfig;

import java.util.function.Supplier;

/**
 * /swthbconfig command — Modify mod runtime config (Requires admin permissions)
 * <p>
 * Supported modifications: max warps, /back toggle, reload config, save config
 */
public class SwthbConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("swthbconfig")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.literal("maxWarps")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(SwthbConfigCommand::executeSetMaxWarps)))
                .then(Commands.literal("backEnabled")
                        .then(Commands.literal("true")
                                .executes(ctx -> executeSetBackEnabled(ctx, true)))
                        .then(Commands.literal("false")
                                .executes(ctx -> executeSetBackEnabled(ctx, false))))
                .then(Commands.literal("reload")
                        .executes(SwthbConfigCommand::executeReload))
                .then(Commands.literal("save")
                        .executes(SwthbConfigCommand::executeSave))
        );
    }

    private static int executeSetMaxWarps(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        ModConfig config = ModConfig.getInstance();
        int oldValue = config.getMaxWarps();
        config.setMaxWarps(amount);

        source.sendSuccess(() ->
                Component.translatableWithFallback("swthb.config.max_warps_set",
                                "已将 Warp 最大数量从 %s 修改为 %s",
                                String.valueOf(oldValue), String.valueOf(amount))
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        return 1;
    }

    private static int executeSetBackEnabled(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        CommandSourceStack source = ctx.getSource();

        ModConfig config = ModConfig.getInstance();
        boolean oldValue = config.isBackEnabled();
        config.setBackEnabled(enabled);

        Supplier<Component> msg;
        if (enabled) {
            msg = () -> Component.translatableWithFallback("swthb.config.back_enabled",
                            "已开启 /back 返回功能").withStyle(ChatFormatting.GREEN);
        } else {
            msg = () -> Component.translatableWithFallback("swthb.config.back_disabled",
                            "已关闭 /back 返回功能").withStyle(ChatFormatting.RED);
        }
        source.sendSuccess(msg, true);

        return 1;
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        ModConfig.load(source.getServer());

        source.sendSuccess(() ->
                Component.translatableWithFallback("swthb.config.reloaded",
                        "配置文件已从磁盘重新加载").withStyle(ChatFormatting.GREEN),
                true
        );

        return 1;
    }

    private static int executeSave(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        ModConfig.getInstance().save();

        source.sendSuccess(() ->
                Component.translatableWithFallback("swthb.config.saved",
                        "配置文件已保存到磁盘").withStyle(ChatFormatting.GREEN),
                true
        );

        return 1;
    }
}