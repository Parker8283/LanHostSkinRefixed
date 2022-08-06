/*
 * Copyright (c) 2022 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf.reference;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentTranslation;
import net.parker8283.lhsrf.LHSRFHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

@SideOnly(Side.CLIENT)
public class FixedGuiConnecting extends GuiConnecting
{
    // --- START OF THINGS INCLUDED TO MAKE COMPILING HAPPY ---
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private NetworkManager networkManager;
    private boolean cancel;
    private final GuiScreen previousGuiScreen;

    public FixedGuiConnecting(GuiScreen p_i1181_1_, Minecraft p_i1181_2_, ServerData p_i1181_3_)
    {
        super(p_i1181_1_, p_i1181_2_, p_i1181_3_);
        this.previousGuiScreen = p_i1181_1_;
    }
    // --- END OF THINGS INCLUDED TO MAKE COMPILING HAPPY ---

    // METHOD INSTRUCTIONS CHANGED
    // NOTE: Had to change all GuiConnecting.this instances to FixedGuiConnecting.this to get compiler to stop yelling at me
    private void connect(final String ip, final int port)
    {
        logger.info("Connecting to " + ip + ", " + port);
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet())
        {
            private static final String __OBFID = "CL_00000686";
            public void run()
            {
                InetAddress inetaddress = null;

                try
                {
                    if (FixedGuiConnecting.this.cancel)
                    {
                        return;
                    }

                    inetaddress = InetAddress.getByName(ip);
                    FixedGuiConnecting.this.networkManager = NetworkManager.provideLanClient(inetaddress, port);
                    FixedGuiConnecting.this.networkManager.setNetHandler(new NetHandlerLoginClient(FixedGuiConnecting.this.networkManager, FixedGuiConnecting.this.mc, FixedGuiConnecting.this.previousGuiScreen));
                    FixedGuiConnecting.this.networkManager.scheduleOutboundPacket(new C00Handshake(5, ip, port, EnumConnectionState.LOGIN), new GenericFutureListener[0]);
                    // --- START CHANGES ---
                    // Replaced "GuiConnecting.this.networkmanager.scheduleOutboundPacket(new C00PacketLoginStart(GuiConnecting.this.mc.getSession().getProfile()), new GenericFutureListener[0]);" with the below
                    GameProfile gameProfile = FixedGuiConnecting.this.mc.getSession().getProfile();
                    if (!LHSRFHooks.hasCachedProperties()) {
                        gameProfile = FixedGuiConnecting.this.mc.getSessionService().fillProfileProperties(gameProfile, true);
                        LHSRFHooks.setProperties(gameProfile.getProperties());
                    }
                    FixedGuiConnecting.this.networkManager.scheduleOutboundPacket(new C00PacketLoginStart(gameProfile));
                    // --- END CHANGES ---
                }
                catch (UnknownHostException unknownhostexception)
                {
                    if (FixedGuiConnecting.this.cancel)
                    {
                        return;
                    }

                    FixedGuiConnecting.logger.error("Couldn\'t connect to server", unknownhostexception);
                    FixedGuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(FixedGuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Unknown host"})));
                }
                catch (Exception exception)
                {
                    if (FixedGuiConnecting.this.cancel)
                    {
                        return;
                    }

                    FixedGuiConnecting.logger.error("Couldn\'t connect to server", exception);
                    String s = exception.toString();

                    if (inetaddress != null)
                    {
                        String s1 = inetaddress.toString() + ":" + port;
                        s = s.replaceAll(s1, "");
                    }

                    FixedGuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(FixedGuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {s})));
                }
            }
        }).start();
    }
}
