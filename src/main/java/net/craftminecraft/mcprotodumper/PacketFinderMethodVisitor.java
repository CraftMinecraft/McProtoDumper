/*
 */

package net.craftminecraft.mcprotodumper;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author Robin
 */
class PacketFinderMethodVisitor extends MethodVisitor {
    SuperFinderVisitor parentVisitor;
    public PacketFinderMethodVisitor(SuperFinderVisitor parentVisitor) {
        super(Opcodes.ASM4);
        this.parentVisitor = parentVisitor;
        inMethod = parentVisitor.packetIn;
        outMethod = parentVisitor.packetOut;
    }
    
    String inMethod, outMethod;
    int currentPacket = -2;
    Type currentPacketType = null;
    boolean parsing = false;
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.INVOKESPECIAL) {
            parsing = true;
        } else if (opcode == Opcodes.INVOKEVIRTUAL) {
            if (name.equals(inMethod)) {
                parentVisitor.packetTypesIn.put(currentPacket, currentPacketType);
            } else if (name.equals(outMethod)) {
                parentVisitor.packetTypesOut.put(currentPacket, currentPacketType);
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }
    
    @Override
    public void visitInsn(int opcode) {
        String currentInsn = "unknown";
        switch (opcode) {
            case Opcodes.INVOKEVIRTUAL:
                currentInsn = "INVOKEVIRTUAL";
                break;
            case Opcodes.POP: 
                currentInsn = "POP";
                break;
            case Opcodes.LDC:
                currentInsn = "LDC";
                break;
            case Opcodes.ICONST_M1:
                currentPacket = -1;
                currentInsn = "ICONST_M1";
                break;
            case Opcodes.ICONST_0:
                currentPacket = 0;
                currentInsn = "ICONST_0";
                break;
            case Opcodes.ICONST_1:
                currentPacket = 1;
                currentInsn = "ICONST_1";
                break;
            case Opcodes.ICONST_2:
                currentPacket = 2;
                currentInsn = "ICONST_2";
                break;
            case Opcodes.ICONST_3:
                currentPacket = 3;
                currentInsn = "ICONST_3";
                break;
            case Opcodes.ICONST_4:
                currentPacket = 4; 
                currentInsn = "ICONST_4";
                break;
            case Opcodes.ICONST_5:
                currentPacket = 5;
                currentInsn = "ICONST_5";
                break;
            case Opcodes.BIPUSH:
                currentInsn = "BIPUSH";
                break;
            case Opcodes.ALOAD:
                currentInsn = "ALOAD";
                break;
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (parsing && opcode == Opcodes.BIPUSH) {
          currentPacket = operand;
        }
        super.visitVarInsn(opcode, operand);
    }
  
    @Override
    public void visitLdcInsn(Object cst) {
        if (parsing && cst instanceof Type) {
          currentPacketType = (Type) cst;
        }
        super.visitLdcInsn(cst);
    }
}

