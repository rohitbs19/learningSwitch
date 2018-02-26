package edu.wisc.cs.sdn.vnet.sw;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import net.floodlightcontroller.packet.MACAddress;
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

	public static HashMap<MACAddress, Wrapper> hmap = new HashMap<MACAddress, Wrapper>();

	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
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
		 * 7: clarity: how do we know if the right interface took the packet ?
		 * 7-2: clarity: does the interface send a ack back to the switch
		 * 8: assumption: that the switch sends an ack and we know the right interface.
		 * 9: we then update the mac mapping to include the new mapping of the destination address
		 * 9-2: to the Mac address.
		 * 10: Then we use the send packet method to send the packet to the destination mac address
		 * 11: and specify the interface we found.
		 * */
		// extracts the destination mac address
		////////////////////////////////////////// INIT VARS ////////////////////////////////////////////////////
		MACAddress destAddr = etherPacket.getDestinationMAC();
		// extracts the source mac address
		MACAddress srcAddr = etherPacket.getSourceMAC();
		//map containing all the mac addressed mapped to key that is its name
		Map<String,Iface> interfaces = getInterfaces();
		////////////////////////////////////////// END //////////////////////////////////////////////////////////

		///////////////////////////////////////// TRIVIAL CHECKING CONDS ///////////////////////////////////////
		if (destAddr == null) {
			System.out.println("Error: invalid destination mac address & address is null");
		}

		if(srcAddr==null){
			System.out.println("Error: invalid src mac address & address is null");
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*code for removing entries based on timeouts*/
		////////////////////////////////////////// TIMEOUTS DELETION ENTRY PART ////////////////////////////////
		//gets the set of keys of the hash map
		Set<MACAddress> macKeySet= hmap.keySet();
		//itr for the setkeys to iterate through all the keys
		Iterator<MACAddress> itrMac_key = macKeySet.iterator();

		// checks if the next elem is present in the set
		while (itrMac_key.hasNext()) {
			// checking condition for time elapsed
			if((System.currentTimeMillis() - hmap.get(itrMac_key).getCurrTime()) > 15*1000){
				//removes if the difference of time is more than 15000 ms
				hmap.remove(itrMac_key);
			}
			itrMac_key.next();
		}
		/*end*/
		/////////////////////////////////////// END ///////////////////////////////////////////////////////////

		/////////////////////////////////// LOOKUP FOR SRC AND DEST ///////////////////////////////////////////
		//checks if the destination mac address is inside the has map
		boolean exists = false;
		// temp to hold the looked up dest addr
		Wrapper look_dest = hmap.get(destAddr);
		if (look_dest == null) {
			exists = false;
		}else{
			exists = true;
		}

		//temp for holding looked up src in hash map
		Wrapper look_src = hmap.get(srcAddr);

		if (look_src == null) {
			Wrapper new_src = new Wrapper(System.currentTimeMillis(), inIface);
			hmap.put(srcAddr, new_src);
		}else{
			look_src.setCurrTime(System.currentTimeMillis());
		}
		/////////////////////////////////// END //////////////////////////////////////////////////////////////

		// if destination addr is inside the hash map send packet through the corresponding interface
		///////////////////////////////// SEND PACKET & BROADCAST IF !exists ////////////////////////////////
		if (exists) {
			sendPacket(etherPacket, look_dest.GetIface());
		}else{
			//broadcast/flood
			boolean ack = false;
			Iface foundDestInterface= null;
			Set<String> temp = interfaces.keySet();
			//itr for all the keys inside the interfaces map
			Iterator<String> itr_key_name = temp.iterator();
			while (itr_key_name.hasNext()) {
				ack = sendPacket(etherPacket, interfaces.get(itr_key_name));
				//if ack is true found destination interface.
				if (ack) {
					foundDestInterface = interfaces.get(itr_key_name);
				}
				itr_key_name.next();
			}
			//found the destination interface update the table
			//if interface is found through the flood process
			if (foundDestInterface != null) {
				//creates an entry into hash map via the wrapper class
				// with the current time and dest interface
				Wrapper found_des = new Wrapper(System.currentTimeMillis(), foundDestInterface);
				hmap.put(destAddr, found_des);

			}else{
				//if no interface exists
				System.out.println("Error: no interface took that");
			}

		}
		///////////////////////////// END ////////////////////////////////////////////////////////////////



		/********************************************************************/
	}
}
