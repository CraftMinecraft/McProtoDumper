/*
 */

package net.craftminecraft.mcprotodumper;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author Robin
 */
class SubClassesFinder extends ClassVisitor {
    Map<String, String> maps = new HashMap<String, String>();
    public SubClassesFinder() {
        super(Opcodes.ASM4);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, 
                                     String signature, String[] exceptions) {
        if (name.equals("<clinit>")) {
            return new MethodVisitor(Opcodes.ASM4) {
                String currentType;
                boolean parsing = true;
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    if (opcode == Opcodes.ANEWARRAY)
                        parsing = false;
                }
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                    if (parsing) maps.put(currentType, owner);
                }
                
                @Override
                public void visitLdcInsn(Object cst) {
                    if (parsing && cst instanceof String) {
                        currentType = (String) cst;
                    }
                }
            };
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
