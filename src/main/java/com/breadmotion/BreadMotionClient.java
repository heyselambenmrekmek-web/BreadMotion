package com.breadmotion;

import com.breadmotion.anim.BreadAnimator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BreadMotionClient implements ClientModInitializer {
    public static final String MOD_ID = "breadmotion";
    public static final Logger LOGGER = LoggerFactory.getLogger("BreadMotion");

    public static volatile BreadConfig config = new BreadConfig();

    private static int ticks = 0;

    @Override
    public void onInitializeClient() {
        config = BreadConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            BreadAnimator.clientTick(client);
            if (++ticks % 40 == 0) {
                config = BreadConfig.reloadIfChanged(config);
            }
        });

        LOGGER.info("BreadMotion hazır - ekmekler kabarıyor!");
    }
}
