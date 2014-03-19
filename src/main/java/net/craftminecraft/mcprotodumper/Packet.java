/*
 */

package net.craftminecraft.mcprotodumper;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Robin
 */
public class Packet {
    public enum Direction { toClient, toServer };
    public Direction direction = null;
    public String state = null;
    public String className = null;
    public int packetType;
    public Map<String,String> packetFields = new HashMap<String,String>();
}
