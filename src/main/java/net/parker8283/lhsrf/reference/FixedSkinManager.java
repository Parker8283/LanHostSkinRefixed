/*
 * Copyright (c) 2020 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf.reference;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;

@SideOnly(Side.CLIENT)
public class FixedSkinManager extends SkinManager {

    // --- START OF THINGS INCLUDED TO MAKE COMPILING HAPPY ---
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    private final MinecraftSessionService sessionService;

    public FixedSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
        super(textureManagerInstance, skinCacheDirectory, sessionService);
        this.sessionService = sessionService;
    }
    // --- END OF THINGS INCLUDED TO MAKE COMPILING HAPPY ---

    // CHANGED CONTAINED IN THIS CODE BLOCK
    public void func_152790_a(final GameProfile p_152790_1_, final SkinManager.SkinAvailableCallback p_152790_2_, final boolean p_152790_3_)
    {
        THREAD_POOL.submit(new Runnable() // SkinManager$3
        {
            private static final String __OBFID = "CL_00001827";
            // METHOD INSTRUCTIONS CHANGED
            public void run()
            {
                final HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture> hashmap = Maps.newHashMap();

                try
                {
                    hashmap.putAll(FixedSkinManager.this.sessionService.getTextures(p_152790_1_, p_152790_3_));
                }
                catch (InsecureTextureException ignored) {}

                if (hashmap.isEmpty() && p_152790_1_.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId()))
                {
                    // CHANGED LINE
                    // Replaced "SkinManager.this.sessionService.fillProfileProperties(p_152790_1_, false)" with "Minecraft.getMinecraft().getSession().getProfile()" below.
                    hashmap.putAll(FixedSkinManager.this.sessionService.getTextures(Minecraft.getMinecraft().getSession().getProfile(), false));
                }

                Minecraft.getMinecraft().addScheduledTask(new Runnable()
                {
                    private static final String __OBFID = "CL_00001826";
                    public void run()
                    {
                        if (hashmap.containsKey(MinecraftProfileTexture.Type.SKIN))
                        {
                            FixedSkinManager.this.loadSkin(hashmap.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN, p_152790_2_);
                        }

                        if (hashmap.containsKey(MinecraftProfileTexture.Type.CAPE))
                        {
                            FixedSkinManager.this.loadSkin(hashmap.get(MinecraftProfileTexture.Type.CAPE), MinecraftProfileTexture.Type.CAPE, p_152790_2_);
                        }
                    }
                });
            }
        });
    }
}
