package nerdhub.cardinal.components;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CardinalComponents implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("cardinal-components");

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            try {
                Class<?> testModClass = Class.forName("nerdhub.cardinal.components.testmod.TestMod");
                Method testEntryPoint = testModClass.getMethod("onInitialize");
                testEntryPoint.invoke(null);
            } catch (ClassNotFoundException e) {
                // do nothing, the mod is being used as a library
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("Failed to load cardinal components' test mod");
            }
        }
    }
}
