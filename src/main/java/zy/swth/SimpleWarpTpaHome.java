package zy.swth;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zy.swth.command.HomeCommand;
import zy.swth.command.SwthConfigCommand;
import zy.swth.command.TpaCommand;
import zy.swth.command.WarpCommand;
import zy.swth.config.ModConfig;
import zy.swth.handler.TeleportHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class SimpleWarpTpaHome implements ModInitializer {
	public static final String MOD_ID = "simple-warp-tpa-home";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 注册所有命令
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> TpaCommand.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> HomeCommand.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> SwthConfigCommand.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> WarpCommand.register(dispatcher));

		// 服务器启动时加载当前存档的数据
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ModConfig.load(server);
			LOGGER.info("存档数据已加载");
		});

		// 每 tick 处理传送倒计时
		ServerTickEvents.START_SERVER_TICK.register(server -> TeleportHandler.tick());

		// 服务器关闭时保存数据并清除旧状态（切换存档时确保互不影响）
		ServerLifecycleEvents.SERVER_STOPPING.register(_ -> {
			ModConfig.getInstance().save();
			ModConfig.reset();
			LOGGER.info("存档数据已保存");
		});

		LOGGER.info("Simple Warp TPA Home 已初始化！");
	}
}
