/*
 */

package net.craftminecraft.mcprotodumper;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Robin
 */
class EnumFinderVisitor extends ClassVisitor {
    public String className = null;
    int requiredFieldsCount = 0;
    public Set<String> addPacketsMethods = new HashSet<String>();

    public EnumFinderVisitor() {
        super(Opcodes.ASM4);
    }
    // Keep classname somewhere accessible
    @Override
    public void visit(int version, int access, String name, String signature,
              String superName, String[] interfaces) {
      className = name;
      requiredFieldsCount = 0;
      addPacketsMethods.clear();
      super.visit(version, access, name, signature, superName, interfaces);
    }

    // Count the required fields
    @Override 
    public FieldVisitor visitField(int access, String name, String desc,
                            String signature, Object value) {
        //println(className)
        if (desc.equals("Lcom/google/common/collect/BiMap;")
                || desc.equals("Lgnu/trove/map/TIntObjectMap;")) {
          requiredFieldsCount += 1;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    // find the methods that add to the BiMaps
    @Override public MethodVisitor visitMethod(int access, 
                                              String name,
                                              String desc,
                                              String signature,
                                              String[] exceptions) {
        if (desc.startsWith("(ILjava/lang/Class;)")) addPacketsMethods.add(name);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
