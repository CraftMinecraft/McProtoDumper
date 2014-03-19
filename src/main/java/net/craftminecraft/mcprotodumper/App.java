package net.craftminecraft.mcprotodumper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
        if (args.length < 1) {
            System.out.println("You need to provide a path to the jar");
            return;
        }

        ZipFile zipFile = new ZipFile(args[0]);
        System.out.println(args[0]);
        EnumFinderVisitor enumFinder = new EnumFinderVisitor();
        ZipEntry currentEntry = null;
        
        // Step 1 : find the enum
        for (ZipEntry zipEntry : IterableEnumeration.make(zipFile.entries())) {
            currentEntry = zipEntry;
            if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class")) {
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                ClassReader reader = new ClassReader(inputStream);
                reader.accept(enumFinder, 0);
                inputStream.close();
                if (enumFinder.requiredFieldsCount == 3) break;
            }
        }
        
        if (enumFinder.requiredFieldsCount != 3) {
            System.out.println("I couldn't find the protocol enum. Please open an issue, along with the minecraft version used");
            return;
        }
        
        // Step 2 : find the subclasses inside the enum, and map them to their state's name
        SubClassesFinder finder = new SubClassesFinder();
        InputStream stream = zipFile.getInputStream(currentEntry);
        ClassReader reader = new ClassReader(stream);
        reader.accept(finder, 0);
        stream.close();

        // Step 3 : Get the packet classes from those subclasses
        Iterator<String> methods = enumFinder.addPacketsMethods.iterator();
        SuperFinderVisitor packetFinder = new SuperFinderVisitor(enumFinder.className, 
                methods.next(), methods.next());
        
        Map<String,Map<Integer,Type>> packetsToServer = new HashMap<String,Map<Integer,Type>>();
        Map<String,Map<Integer,Type>> packetsToClient = new HashMap<String,Map<Integer,Type>>();
        
        for (Map.Entry<String,String> entry : finder.maps.entrySet()) {
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(entry.getValue() + ".class"));
            reader = new ClassReader(inputStream);
            reader.accept(packetFinder, 0);
            packetsToServer.put(entry.getKey(), packetFinder.packetTypesIn);
            packetsToClient.put(entry.getKey(), packetFinder.packetTypesOut);
            inputStream.close();
        }
        
        // Hacky step : make sure packetIn and packetOut are in the correct order
        if (packetsToServer.get("HANDSHAKE") == null
                || packetsToServer.get("HANDSHAKE").size() == 0) {
            Map<String,Map<Integer,Type>> tmp = packetsToServer;
            packetsToServer = packetsToClient;
            packetsToClient = tmp;
        }
        
        // Step 4 : Enter those types and see what they're made of !
        PacketFieldsFinder fieldFinder = new PacketFieldsFinder();
        Map<String,List<Map<String,String>>[]> fields = new HashMap<String,List<Map<String,String>>[]>();
        // State.Direction.ID.FieldName.Type
        for (Map.Entry<String,String> entry : finder.maps.entrySet()) {
            fields.put(entry.getKey(), new List[] {
                new LinkedList<Map<String,String>>(),
                new LinkedList<Map<String,String>>(),
            });
        }
        final int toServer = 0, toClient = 1; 
        for (Map.Entry<String,Map<Integer,Type>> entry : packetsToServer.entrySet()) {
            List<Map<String,String>> state = fields.get(entry.getKey())[toServer];
            for (Map.Entry<Integer,Type> entryIn : entry.getValue().entrySet()) {
                InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(entryIn.getValue().getClassName() + ".class"));
                reader = new ClassReader(inputStream);
                reader.accept(fieldFinder, 0);
                state.add(fieldFinder.fields);
                inputStream.close();
            }
        }
        for (Map.Entry<String,Map<Integer,Type>> entry : packetsToClient.entrySet()) {
            List<Map<String,String>> state = fields.get(entry.getKey())[toClient];
            for (Map.Entry<Integer,Type> entryIn : entry.getValue().entrySet()) {
                InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(entryIn.getValue().getClassName() + ".class"));
                reader = new ClassReader(inputStream);
                reader.accept(fieldFinder, 0);
                state.add(fieldFinder.fields);
                inputStream.close();
            }
        }
        
        // Step N : print everything
        System.out.println("Protocol State Enum : " + enumFinder.className);
        System.out.println("Protocol States : ");
        for (Map.Entry<String,String> entry : finder.maps.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println();
        System.out.println("Protocol packets To Server : ");
        for (Map.Entry<String,Map<Integer,Type>> entry : packetsToServer.entrySet()) {
            for (Map.Entry<Integer,Type> entryIn : entry.getValue().entrySet()) {
                System.out.println(entry.getKey() + "." + entryIn.getKey() + " : " + entryIn.getValue().getClassName());
            }
        }
        System.out.println();
        System.out.println("Protocol packets To Client : ");
        for (Map.Entry<String,Map<Integer,Type>> entry : packetsToClient.entrySet()) {
            for (Map.Entry<Integer,Type> entryIn : entry.getValue().entrySet()) {
                System.out.println(entry.getKey() + "." + entryIn.getKey() + " : " + entryIn.getValue().getClassName());
            }
        }
        System.out.println();
        System.out.println("Packet fields to server");
        for (Map.Entry<String,List<Map<String,String>>[]> entry : fields.entrySet()) {
            int i = 0;
            for (Map<String,String> entryIn1 : entry.getValue()[toServer]) {
                for (Map.Entry<String,String> entryIn2 : entryIn1.entrySet()) {
                    System.out.println(entry.getKey() + "." + i + "." + entryIn2.getKey() + " : " + entryIn2.getValue());
                }
                i++;
            }
        }
        System.out.println("Packet fields to client");
        for (Map.Entry<String,List<Map<String,String>>[]> entry : fields.entrySet()) {
            int i = 0;
            for (Map<String,String> entryIn1 : entry.getValue()[toClient]) {
                for (Map.Entry<String,String> entryIn2 : entryIn1.entrySet()) {
                    System.out.println(entry.getKey() + "." + i + "." + entryIn2.getKey() + " : " + entryIn2.getValue());
                }
                i++;
            }
        }

    }
}
