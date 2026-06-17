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

public class SimpleWarpTpaHome implements ModInitializer {
	public static final String MOD_ID = "simple-warp-tpa-home";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 注册所有命令
		CommandRegistrationCallback.EVENT.register(TpaCommand::register);
		CommandRegistrationCallback.EVENT.register(HomeCommand::register);
		CommandRegistrationCallback.EVENT.register(SwthConfigCommand::register);
		CommandRegistrationCallback.EVENT.register(WarpCommand::register);

		// 服务器启动时加载配置文件
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ModConfig.load();
			LOGGER.info("配置文件已加载");
		});

		// 服务器关闭时保存配置文件
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ModConfig.getInstance().save();
			LOGGER.info("配置文件已保存");
		});

		LOGGER.info("Simple Warp TPA Home 已初始化！");
	}
}
