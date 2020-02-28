/*
 * Copyright (c) 2020 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf.reference;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Session;
import net.parker8283.lhsrf.LHSRFHooks;

@SideOnly(Side.CLIENT)
public class FixedSession extends Session {

    // INCLUDED TO MAKE COMPILING HAPPY
    public FixedSession(String username, String playerID, String token, String sessionType) {
        super(username, playerID, token, sessionType);
    }

    // METHOD INSTRUCTIONS CHANGED
    @Override
    public GameProfile getProfile() {
        try
        {
            UUID uuid = UUIDTypeAdapter.fromString(this.getPlayerID());
            // --- START CHANGES ---
            // Replaced "return new GameProfile(uuid, this.getUsername());" with the below
            GameProfile ret = new GameProfile(uuid, this.getUsername());
            LHSRFHooks.tryAddCachedProperties(ret);
            return ret;
            // --- END CHANGES ---
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            return new GameProfile(EntityPlayer.getUUID(new GameProfile(null, this.getUsername())), this.getUsername());
        }
    }
}
