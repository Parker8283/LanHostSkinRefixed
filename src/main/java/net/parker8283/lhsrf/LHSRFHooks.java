/*
 * Copyright (c) 2020 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

public class LHSRFHooks {
    private static PropertyMap properties;

    public static void setProperties(PropertyMap properties) {
        if (LHSRFHooks.properties == null) {
            LHSRFHooks.properties = properties;
        }
    }

    // Checks if any properties have been cached yet
    public static boolean hasCachedProperties() {
        return properties != null;
    }

    public static void tryAddCachedProperties(GameProfile profile) {
        if (properties != null) {
            profile.getProperties().putAll(properties);
        }
    }

}
