/*
 */

package net.craftminecraft.mcprotodumper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class SuperFinderVisitor extends ClassVisitor {
    String superClazz;
    String className;
    boolean mustVisit = false;
    public Map<Integer, Type> packetTypesIn;
    public Map<Integer, Type> packetTypesOut;
    String packetIn, packetOut; 
    
    public SuperFinderVisitor(String superClazz, String packetIn, String packetOut) {
        super(Opcodes.ASM4);
        this.superClazz = superClazz;
        this.packetIn = packetIn;
        this.packetOut = packetOut;
    }
    
    @Override 
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {        
        if (superName.equals(superClazz)) {
            mustVisit = true;
            this.className = name;
            packetTypesIn = new HashMap<Integer, Type>();
            packetTypesOut = new HashMap<Integer, Type>();
        } else {
            mustVisit = false;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override 
    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {
        if (mustVisit && name.equals("<init>")) {
            return new PacketFinderMethodVisitor(this);
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
