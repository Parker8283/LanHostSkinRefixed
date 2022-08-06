/*
 * Copyright (c) 2020 Parker Young ("Parker8283").
 * This file is part of the Lan Host Skin Refixed project.
 * It is distributed under the MIT License.
 * A copy should have been included with the source distribution.
 * If not, you can obtain a copy at https://opensource.org/licenses/MIT.
 */
package net.parker8283.lhsrf.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A Helper for ASM-related stuff. Code stolen from squeek502.
 * Source: https://github.com/squeek502/ASMHelper/blob/1.7.10/raw/squeek/asmhelper/ASMHelper.java
 */
public class ASMHelper {
    public static InsnComparator insnComparator = new InsnComparator();

    public static ClassNode readClassFromBytes(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        return classNode;
    }

    public static byte[] writeClassToBytes(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static MethodNode findMethodNodeOfClass(ClassNode classNode, String methodName, String methodDesc) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(methodName) && (methodDesc == null || method.desc.equals(methodDesc))) {
                return method;
            }
        }
        return null;
    }

    public static AbstractInsnNode findInstruction(AbstractInsnNode firstInsnToCheck, AbstractInsnNode toFind, boolean reverseDirection) {
        for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext()) {
            if (insnComparator.areInsnsEqual(instruction, toFind)) {
                return instruction;
            }
        }
        return null;
    }

    public static LabelNode findEndLabel(MethodNode method) {
        for (AbstractInsnNode instruction = method.instructions.getLast(); instruction != null; instruction = instruction.getPrevious()) {
            if (instruction instanceof LabelNode) {
                return (LabelNode)instruction;
            }
        }
        return null;
    }

    public static LabelNode findNextLabel(AbstractInsnNode instruction) {
        for (AbstractInsnNode insn = instruction.getNext(); insn != null; insn = insn.getNext()) {
            if (insn.getType() == AbstractInsnNode.LABEL) {
                return (LabelNode)insn;
            }
        }
        return null;
    }
}
