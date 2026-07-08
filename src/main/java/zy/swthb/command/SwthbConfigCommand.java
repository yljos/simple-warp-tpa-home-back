package zy.swthb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import zy.swthb.config.ModConfig;

import java.util.function.Supplier;
import me.lucko.fabric.api.permissions.v0.Permissions;

/**
 * /swthbconfig command — Modify mod runtime config (Requires admin permissions)
 * <p>
 * Supported modifications: /back toggle, reload config, save config
 */
public class SwthbConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("swthbconfig")
                .requires(source -> Permissions.check(source, "swthb.command.config", 4))
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

    private static int executeSetBackEnabled(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        CommandSourceStack source = ctx.getSource();

        ModConfig config = ModConfig.getInstance();
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