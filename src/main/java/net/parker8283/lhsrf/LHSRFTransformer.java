/*
 * Copyright (c) 2020 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf;

import net.minecraft.launchwrapper.IClassTransformer;
import net.parker8283.lhsrf.asm.ASMHelper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static net.parker8283.lhsrf.LHSRFLoader.LOG;
import static org.objectweb.asm.Opcodes.*;

/**
 * This incorporates the following patches made to Forge:
 * PR#1832 by Parker8283 : https://github.com/MinecraftForge/MinecraftForge/pull/1832/files
 *   NOTE: The new field and methods in Session were put in LHSRFHooks.
 * PR#2069 by Simon816 : https://github.com/MinecraftForge/MinecraftForge/pull/2069/files
 *
 * This also includes a new patch to GuiConnecting as that is the other place a C00PacketLoginStart is sent.
 * I would put the code just in that packet class, but I need the session manager, and since there are only two
 * places that I personally care about that send the packet, it's fine to do this (to me at least).
 *
 * Exact changes can be viewed in the "reference" package.
 */
public class LHSRFTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        boolean isDev = !LHSRFLoader.DEV_ENV;
        if (transformedName.equals("net.minecraft.client.Minecraft")) {
            LOG.debug("Found Minecraft class");
            ClassNode minecraft = ASMHelper.readClassFromBytes(basicClass);

            // Patch launchIntegratedServer
            MethodNode launchIntegratedServer = ASMHelper.findMethodNodeOfClass(minecraft, isDev ? "launchIntegratedServer" : "func_71371_a", "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;)V");
            if (launchIntegratedServer != null) {
                AbstractInsnNode target = ASMHelper.findInstruction(launchIntegratedServer.instructions.getLast(), new TypeInsnNode(NEW, "net/minecraft/network/login/client/C00PacketLoginStart"), true);
                if (target != null) {
                    target = target.getPrevious(); // ALOAD 7
                    InsnList toInject = new InsnList();
                    LabelNode gameProfileDeclared = new LabelNode();
                    LabelNode ifEnd = new LabelNode();
                    LabelNode methodEnd = ASMHelper.findEndLabel(launchIntegratedServer);
                    toInject.add(new VarInsnNode(ALOAD, 0));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/Minecraft", isDev ? "getSession" : "func_110432_I", "()Lnet/minecraft/util/Session;", false));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/Session", isDev ? "getProfile" : "func_148256_e", "()Lcom/mojang/authlib/GameProfile;", false));
                    toInject.add(new VarInsnNode(ASTORE, 8));
                    toInject.add(gameProfileDeclared);
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "net/parker8283/lhsrf/LHSRFHooks", "hasCachedProperties", "()Z", false));
                    toInject.add(new JumpInsnNode(IFNE, ifEnd));
                    toInject.add(new VarInsnNode(ALOAD, 0));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/Minecraft", isDev ? "getSessionService" : "func_152347_ac", "()Lcom/mojang/authlib/minecraft/MinecraftSessionService;", false));
                    toInject.add(new VarInsnNode(ALOAD, 8));
                    toInject.add(new InsnNode(ICONST_1));
                    toInject.add(new MethodInsnNode(INVOKEINTERFACE, "com/mojang/authlib/minecraft/MinecraftSessionService", "fillProfileProperties", "(Lcom/mojang/authlib/GameProfile;Z)Lcom/mojang/authlib/GameProfile;", true));
                    toInject.add(new VarInsnNode(ASTORE, 8));
                    toInject.add(new VarInsnNode(ALOAD, 8));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "com/mojang/authlib/GameProfile", "getProperties", "()Lcom/mojang/authlib/properties/PropertyMap;", false));
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "net/parker8283/lhsrf/LHSRFHooks", "setProperties", "(Lcom/mojang/authlib/properties/PropertyMap;)V", false));
                    toInject.add(ifEnd);
                    launchIntegratedServer.instructions.insertBefore(target, toInject);
                    target = ASMHelper.findInstruction(target, new MethodInsnNode(INVOKESPECIAL, "net/minecraft/network/login/client/C00PacketLoginStart", "<init>", "(Lcom/mojang/authlib/GameProfile;)V", false), false);
                    if (target != null) {
                        launchIntegratedServer.instructions.remove(target.getPrevious()); // INVOKEVIRTUAL net/minecraft/util/Session.getProfile ()Lcom/mojang/authlib/GameProfile;
                        launchIntegratedServer.instructions.remove(target.getPrevious()); // INVOKEVIRTUAL net/minecraft/client/Minecraft.getSession ()Lnet/minecraft/util/Session;
                        ((VarInsnNode)target.getPrevious()).var = 8;
                        launchIntegratedServer.localVariables.add(new LocalVariableNode("gameProfile", "Lcom/mojang/authlib/GameProfile;", null, gameProfileDeclared, methodEnd, 8));
                        LOG.debug("Patched loadIntegratedServer method");
                    } else {
                        throw new RuntimeException("Could not find constructor for C00PacketLoginStart in launchIntegratedServer method in Minecraft class");
                    }
                } else {
                    throw new RuntimeException("Could not find injection point in launchIntegratedServer method in Minecraft class");
                }
            } else {
                throw new RuntimeException("Could not find launchIntegratedServer method in Minecraft class");
            }

            LOG.info("Successfully patched Minecraft class");
            return ASMHelper.writeClassToBytes(minecraft);
        } else if (transformedName.equals("net.minecraft.util.Session")) {
            LOG.debug("Found Session class");
            ClassNode session = ASMHelper.readClassFromBytes(basicClass);

            // Patch getProfile()
            MethodNode getProfile = ASMHelper.findMethodNodeOfClass(session, isDev ? "getProfile" : "func_148256_e", "()Lcom/mojang/authlib/GameProfile;");
            if (getProfile != null) {
                AbstractInsnNode target = ASMHelper.findInstruction(getProfile.instructions.getFirst(), new MethodInsnNode(INVOKESPECIAL, "com/mojang/authlib/GameProfile", "<init>", "(Ljava/util/UUID;Ljava/lang/String;)V", false), false);
                if (target != null) {
                    InsnList toInject = new InsnList();
                    LabelNode retDeclared = new LabelNode();
                    LabelNode afterTryReturn = getProfile.tryCatchBlocks.get(0).end;
                    toInject.add(new VarInsnNode(ASTORE, 2));
                    toInject.add(retDeclared);
                    toInject.add(new VarInsnNode(ALOAD, 2));
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "net/parker8283/lhsrf/LHSRFHooks", "tryAddCachedProperties", "(Lcom/mojang/authlib/GameProfile;)V", false));
                    toInject.add(new VarInsnNode(ALOAD, 2));
                    getProfile.instructions.insert(target, toInject);
                    getProfile.localVariables.add(new LocalVariableNode("ret", "Lcom/mojang/authlib/GameProfile;", null, retDeclared, afterTryReturn, 2));
                    LOG.debug("Patched getProfile method");
                } else {
                    throw new RuntimeException("Could not find injection point in getProfile method in Session class");
                }
            } else {
                throw new RuntimeException("Could not find getProfile method in Session class");
            }

            LOG.info("Successfully patched Session class");
            return ASMHelper.writeClassToBytes(session);
        } else if (transformedName.equals("net.minecraft.client.multiplayer.GuiConnecting$1")) {
            LOG.debug("Found GuiConnecting$1 class");
            ClassNode guiConnecting = ASMHelper.readClassFromBytes(basicClass);

            // Patch run() of connect()
            MethodNode func = ASMHelper.findMethodNodeOfClass(guiConnecting, "run", "()V");
            if (func != null) {
                AbstractInsnNode target = ASMHelper.findInstruction(func.instructions.getFirst(), new TypeInsnNode(NEW, "net/minecraft/network/login/client/C00PacketLoginStart"), false);
                if (target != null) {
                    // Remove moved instructions (I'll just re-create them later)
                    target = target.getPrevious().getPrevious(); // GETFIELD net/minecraft/client/multiplayer/GuiConnecting$1.this$0 : Lnet/minecraft/client/multiplayer/GuiConnecting;
                    func.instructions.remove(target.getNext()); // INVOKESTATIC net/minecraft/client/multiplayer/GuiConnecting.access$100 (Lnet/minecraft/client/multiplayer/GuiConnecting;)Lnet/minecraft/network/NetworkManager;
                    func.instructions.remove(target.getNext()); // NEW net/minecraft/network/login/client/C00PacketLoginStart
                    func.instructions.remove(target.getNext()); // DUP
                    func.instructions.remove(target.getNext()); // ALOAD 0
                    func.instructions.remove(target.getNext()); // GETFIELD net/minecraft/client/multiplayer/GuiConnecting$1.this$0 : Lnet/minecraft/client/multiplayer/GuiConnecting;
                    // Insert new instructions
                    target = target.getNext().getNext().getNext().getNext(); // INVOKESPECIAL net/minecraft/network/login/client/C00PacketLoginStart.<init> (Lcom/mojang/authlib/GameProfile;)V
                    InsnList toInject = new InsnList();
                    LabelNode gameProfileDeclared = new LabelNode();
                    LabelNode ifEnd = new LabelNode();
                    LabelNode gameProfileOOS = ASMHelper.findNextLabel(target);
                    toInject.add(new VarInsnNode(ASTORE, 2));
                    toInject.add(gameProfileDeclared);
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "net/parker8283/lhsrf/LHSRFHooks", "hasCachedProperties", "()Z", false));
                    toInject.add(new JumpInsnNode(IFNE, ifEnd));
                    toInject.add(new VarInsnNode(ALOAD, 0));
                    toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/GuiConnecting$1", isDev ? "this$0" : "field_148230_c", "Lnet/minecraft/client/multiplayer/GuiConnecting;"));
                    toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/GuiConnecting", isDev ? "mc" : "field_146297_k", "Lnet/minecraft/client/Minecraft;"));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/Minecraft", isDev ? "getSessionService" : "func_152347_ac", "()Lcom/mojang/authlib/minecraft/MinecraftSessionService;", false));
                    toInject.add(new VarInsnNode(ALOAD, 2));
                    toInject.add(new InsnNode(ICONST_1));
                    toInject.add(new MethodInsnNode(INVOKEINTERFACE, "com/mojang/authlib/minecraft/MinecraftSessionService", "fillProfileProperties", "(Lcom/mojang/authlib/GameProfile;Z)Lcom/mojang/authlib/GameProfile;", true));
                    toInject.add(new VarInsnNode(ASTORE, 2));
                    toInject.add(new VarInsnNode(ALOAD, 2));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "com/mojang/authlib/GameProfile", "getProperties", "()Lcom/mojang/authlib/properties/PropertyMap;", false));
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "net/parker8283/lhsrf/LHSRFHooks", "setProperties", "(Lcom/mojang/authlib/properties/PropertyMap;)V", false));
                    toInject.add(ifEnd);
                    toInject.add(new VarInsnNode(ALOAD, 0));
                    toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/GuiConnecting$1", isDev ? "this$0" : "field_148230_c", "Lnet/minecraft/client/multiplayer/GuiConnecting;"));
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "net/minecraft/client/multiplayer/GuiConnecting", "access$100", "(Lnet/minecraft/client/multiplayer/GuiConnecting;)Lnet/minecraft/network/NetworkManager;", false));
                    toInject.add(new TypeInsnNode(NEW, "net/minecraft/network/login/client/C00PacketLoginStart"));
                    toInject.add(new InsnNode(DUP));
                    toInject.add(new VarInsnNode(ALOAD, 2));
                    func.instructions.insertBefore(target, toInject);
                    func.localVariables.add(new LocalVariableNode("gameProfile", "Lcom/mojang/authlib/GameProfile;", null, gameProfileDeclared, gameProfileOOS, 2));
                    LOG.debug("Patched run method");
                } else {
                    throw new RuntimeException("Could not find injection point in run method in connect method in GuiConnecting class");
                }
            } else {
                throw new RuntimeException("Could not find run method made in connect method in GuiConnecting class");
            }

            LOG.info("Successfully patched GuiConnecting$1 class");
            return ASMHelper.writeClassToBytes(guiConnecting);
        } else if (transformedName.equals("net.minecraft.client.resources.SkinManager$3")) {
            LOG.debug("Found SkinManager$3 class");
            ClassNode skinManager = ASMHelper.readClassFromBytes(basicClass);

            // Patch run() of func_152790_a()
            MethodNode func = ASMHelper.findMethodNodeOfClass(skinManager, "run", "()V");
            if (func != null) {
                AbstractInsnNode target = ASMHelper.findInstruction(func.instructions.getLast(), new MethodInsnNode(INVOKEINTERFACE, "com/mojang/authlib/minecraft/MinecraftSessionService", "getTextures", "(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;", true), true);
                if (target != null) {
                    target = target.getPrevious(); // ICONST_0
                    // Remove the old SkinManager.this.sessionService.fillProfileProperties(p_152790_1_, false) call
                    for (int i = 0; i < 7; i++) {
                        func.instructions.remove(target.getPrevious());
                    }
                    InsnList toInject = new InsnList();
                    toInject.add(new MethodInsnNode(INVOKESTATIC, "net/minecraft/client/Minecraft", isDev ? "getMinecraft" : "func_71410_x ", "()Lnet/minecraft/client/Minecraft;", false));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/Minecraft", isDev ? "getSession" : "func_110432_I", "()Lnet/minecraft/util/Session;", false));
                    toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/Session", isDev ? "getProfile" : "func_148256_e", "()Lcom/mojang/authlib/GameProfile;", false));
                    func.instructions.insertBefore(target, toInject);
                    LOG.debug("Patched run method");
                } else {
                    throw new RuntimeException("Could not find alteration point in run method made in func_152790_a method in SkinManager class");
                }
            } else {
                throw new RuntimeException("Could not find run method made in func_152790_a method in SkinManager class");
            }

            LOG.info("Successfully patched SkinManager$3 class");
            return ASMHelper.writeClassToBytes(skinManager);
        }
        return basicClass;
    }
}
