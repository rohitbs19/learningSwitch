package edu.wisc.cs.sdn.vnet.sw;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import net.floodlightcontroller.packet.MACAddress;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.Set;
import java.util.*;
/**
 * @author Aaron Gember-Jacobson
 */
 class Wrapper{
	long currTime;
	Iface inface;
	public Wrapper(long currTime, Iface inface) {
		this.currTime = currTime;
		this.inface = inface;
	}
	public long getCurrTime(){
		return this.currTime;
	}

	public Iface GetIface() {
		return this.inface;
	}

	public void setCurrTime(long currTime) {
		this.currTime = currTime;
	}
}
public class Switch extends Device
{
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */

	public static ConcurrentHashMap<MACAddress, Wrapper> hmap = new ConcurrentHashMap<MACAddress, Wrapper>();

	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		 synchronized(this.hmap){
		System.out.println("*** -> Received packet: " +
				etherPacket.toString().replace("\n", "\n\t"));

		/********************************************************************/
		/* TODO: Handle packets                                             */
		/*basic structure of the learning switch
		 *
		 * 1: first extract the source and the destination mac addresses and process the args.
		 * 2: then handle the cases if the dest mac address or the source is null
		 * 3: then try searching for the Destination Mac Address in the table we maintain for
		 * 3-2: Mac addresses mapped to different interfaces.
		 * 4: if lookup fails, that means the switch doesn't know where the destination mac address
		 * 4-2: is mapped to (interface).
		 * 5: then in that case we flood to all the interfaces connected to the switch
		 * 6: we flood by sending the packet to all the other interfaces.
		 * */
		// extracts the destination mac address
		MACAddress destAddr = etherPacket.getDestinationMAC();
		// extracts the source mac address
		MACAddress srcAddr = etherPacket.getSourceMAC();
		//map containing all the mac addressed mapped to key that is its name
		Map<String,Iface> interfaces = getInterfaces();


		for (String name : interfaces.keySet()) {
			System.out.println("names: " + interfaces.get(name).getName());
		}

		if (destAddr == null) {
			System.out.println("Error: invalid destination mac address & address is null");
            System.exit(1);
		}

		if(srcAddr==null){
			System.out.println("Error: invalid src mac address & address is null");
            System.exit(1);
		}
		/*code for removing entries based on timeouts*/
		//print the hashmap
		
			for (MACAddress name : hmap.keySet()) {
				System.out.println(hmap.get(name).GetIface().getName());
			}
			for (MACAddress name : hmap.keySet()) {
				if (hmap.get(name)!=null && System.currentTimeMillis() - hmap.get(name).getCurrTime()> (15000)) {
					hmap.remove(name);
				}
			}
			
			//checks if the destination mac address is inside the has map
			// temp to hold the looked up dest addr
			//temp for holding looked up src in hash map
			if (hmap.get(srcAddr) == null) {
				Wrapper new_src = new Wrapper(System.currentTimeMillis(), inIface);
				hmap.put(srcAddr, new_src);
			}else{
				hmap.get(srcAddr).setCurrTime(System.currentTimeMillis());
			}
	        System.out.println("////////////////////////////////////////////////////////////////////////////");	
		for (MACAddress name : hmap.keySet()) {
			System.out.println(hmap.get(name).GetIface().getName());
		}
		System.out.println("////////////////////////////////////////////////////////////////////////////");
		// if destination addr is inside the hash map send packet through the corresponding interface
		
		
		if (hmap.get(destAddr)!=null && hmap.get(destAddr).GetIface()!=inIface) {
			
			sendPacket(etherPacket, hmap.get(destAddr).GetIface());
		}else if(hmap.get(destAddr)==null ){
			//broadcast/flood
			for (String name : interfaces.keySet()) {
				if(!name.equals(inIface.getName())){
					sendPacket(etherPacket, interfaces.get(name));
				}
				System.out.println("searching for reply from interface.. ==> " + interfaces.get(name).getName());
			}
		}
		else{
			System.out.println("exit");
			System.exit(1);
		}
		///////////////////////////// END ////////////////////////////////////////////////////////////////



		/********************************************************************/
	}
}
}

