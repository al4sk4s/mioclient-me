package me.mioclient.asm;

import me.mioclient.Mio;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.Name("Mio")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class MixinLoader implements IFMLLoadingPlugin {

    private static boolean isObfuscatedEnvironment;

    public MixinLoader() {
        Mio.LOGGER.info("Loading mio mixins...\n");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.mio.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        Mio.LOGGER.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
        isObfuscatedEnvironment = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

    public String getAccessTransformerClass() {
        return null;
    }
}

