/*
 */

package net.craftminecraft.mcprotodumper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author Robin
 */
public class PacketFieldsFinder extends ClassVisitor {
    String className;
    Map<String,String> methodTypeMapping = new HashMap<String,String>() {{
        put("readByte", "byte");
        put("readBytes", "byte[]");
        put("readUnsignedByte", "ubyte");
        put("readShort", "short");
        put("readUnsignedShort", "ushort");
        put("readInt", "int");
        put("readLong", "long");
        put("readFloat", "float");
        put("readDouble", "double");
        put("readBoolean", "bool");
        put("a", "varint");
        put("c", "slot");
        put("c(I)Ljava/lang/String;", "string");
    }};
    Map<String,String> fields = new LinkedHashMap<String,String>();  // Insertion order is pretty important !
    public PacketFieldsFinder() {
        super(Opcodes.ASM4);
    }
    
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        className = name;
        fields = new LinkedHashMap<String,String>();
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, 
            String signature, String[] exceptions) {
        final Type methodType = Type.getMethodType(desc);
        if (name.equals("a") && methodType.getArgumentTypes().length == 1
              /*  && methodType.getArgumentTypes()[0].getClassName().equals("ep")*/) { // TODO : Fill in the blanks
            return new MethodVisitor(Opcodes.ASM4) {
                String currentMethod = "unknown";
                String currentMethodDesc = "";
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    if (opcode == Opcodes.PUTFIELD && owner.equals(className)) {
                        if (methodTypeMapping.containsKey(currentMethod + currentMethodDesc)) {
                            fields.put(name, methodTypeMapping.get(currentMethod + currentMethodDesc));
                        } else if (methodTypeMapping.containsKey(currentMethod)) {
                            fields.put(name, methodTypeMapping.get(currentMethod));
                        } else {
                            fields.put(name, currentMethod);
                        }
                        currentMethod = "unknown";
                    }
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
                
                public void visitMethodInsn(int opcode, String owner, String methodName, String desc) {
                    if (opcode == Opcodes.INVOKEVIRTUAL 
                            && owner.equals(methodType.getArgumentTypes()[0].getInternalName())) {
                        currentMethod = methodName;
                        currentMethodDesc = desc;
                    }
                    super.visitMethodInsn(opcode, owner, methodName, desc);
                }
                
                public void visitIntInsn(int opcode, int operand) {
                    // TODO : String max value
                }
            };
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
