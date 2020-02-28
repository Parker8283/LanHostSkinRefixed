/*
 * Copyright (c) 2020 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf.reference;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.net.Proxy;
import java.net.SocketAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Session;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.parker8283.lhsrf.LHSRFHooks;

@SideOnly(Side.CLIENT)
public class FixedMinecraft extends Minecraft
{
    // --- START OF THINGS INCLUDED TO MAKE COMPILING HAPPY ---
    private IntegratedServer theIntegratedServer;
    private boolean integratedServerIsRunning;
    private NetworkManager myNetworkManager;

    public FixedMinecraft(Session sessionIn, int displayWidth, int displayHeight, boolean fullscreen, boolean isDemo, File dataDir, File assetsDir, File resourcePackDir, Proxy proxy, String version, Multimap twitchDetails, String assetsJsonVersion) {
        super(sessionIn, displayWidth, displayHeight, fullscreen, isDemo, dataDir, assetsDir, resourcePackDir, proxy, version, twitchDetails, assetsJsonVersion);
    }
    // --- END OF THINGS INCLUDED TO MAKE COMPILING HAPPY ---

    // METHOD INSTRUCTIONS CHANGED
    public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn)
    {
        FMLClientHandler.instance().startIntegratedServer(folderName, worldName, worldSettingsIn);
        this.loadWorld(null);
        System.gc();
        ISaveHandler isavehandler = this.getSaveLoader().getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null && worldSettingsIn != null)
        {
            worldinfo = new WorldInfo(worldSettingsIn, folderName);
            isavehandler.saveWorldInfo(worldinfo);
        }

        if (worldSettingsIn == null)
        {
            worldSettingsIn = new WorldSettings(worldinfo);
        }

        try
        {
            this.theIntegratedServer = new IntegratedServer(this, folderName, worldName, worldSettingsIn);
            this.theIntegratedServer.startServerThread();
            this.integratedServerIsRunning = true;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
            crashreportcategory.addCrashSection("Level ID", folderName);
            crashreportcategory.addCrashSection("Level Name", worldName);
            throw new ReportedException(crashreport);
        }

        this.loadingScreen.displaySavingString(I18n.format("menu.loadingLevel"));

        while (!this.theIntegratedServer.serverIsInRunLoop())
        {
            if (!StartupQuery.check())
            {
                loadWorld(null);
                displayGuiScreen(null);
                return;
            }
            String s2 = this.theIntegratedServer.getUserMessage();

            if (s2 != null)
            {
                this.loadingScreen.displayLoadingString(I18n.format(s2));
            }
            else
            {
                this.loadingScreen.displayLoadingString("");
            }

            try
            {
                Thread.sleep(200L);
            }
            catch (InterruptedException ignored)
            {
                ;
            }
        }

        this.displayGuiScreen(null);
        SocketAddress socketaddress = this.theIntegratedServer.getNetworkSystem().addLocalEndpoint();
        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, null));
        networkmanager.scheduleOutboundPacket(new C00Handshake(5, socketaddress.toString(), 0, EnumConnectionState.LOGIN));
        // --- START CHANGES ---
        // Replaced "networkmanager.scheduleOutboundPacket(new C00PacketLoginStart(this.getSession().getProfile()), new GenericFutureListener[0]);" with the below
        GameProfile gameProfile = this.getSession().getProfile();
        if (!LHSRFHooks.hasCachedProperties()) {
            gameProfile = getSessionService().fillProfileProperties(gameProfile, true);
            LHSRFHooks.setProperties(gameProfile.getProperties());
        }
        networkmanager.scheduleOutboundPacket(new C00PacketLoginStart(gameProfile));
        // --- END CHANGES ---
        this.myNetworkManager = networkmanager;
    }
}