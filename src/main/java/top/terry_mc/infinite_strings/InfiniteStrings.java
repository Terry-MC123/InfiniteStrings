package top.terry_mc.infinite_strings;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(InfiniteStrings.MODID)
public class InfiniteStrings {
    public static final String MODID = "infinite_strings";
    private static final Logger LOGGER = LogManager.getLogger("Infinite Strings");
    public InfiniteStrings() {
        LOGGER.info("Infinite Strings loaded!");
    }
}
