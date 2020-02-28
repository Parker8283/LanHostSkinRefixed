/*
 * Copyright (c) 2020 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@IFMLLoadingPlugin.SortingIndex(1001) // Run after the deobf transformer so we only have to deal with SRG names in non-dev
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"net.parker8283.lhsrf"})
public class LHSRFLoader implements IFMLLoadingPlugin {
    public static final Logger LOG = LogManager.getLogger("LanHostSkinReFixed");
    public static Boolean DEV_ENV = null;

    public LHSRFLoader() {
        if (Thread.currentThread().getName().equals("main")) {
            LOG.info("We're on the hunt for your skin...");
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"net.parker8283.lhsrf.LHSRFTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return "net.parker8283.lhsrf.LHSRFContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        DEV_ENV = (Boolean)data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
