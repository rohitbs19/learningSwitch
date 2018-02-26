package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.util.HashMap;

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device
{	


	static private Map<MACAddress, Iface> forwardingTable = new HashMap<>();
	private Map<String, Iface> interfaces;
	static private Map<MACAddress, Integer> timings = new HashMap();


	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
	}

	// this function is used to checktime, if a certain time
	// has elapsed 15 seconds, then it will return true
	// else it will return false
	private boolean checkTime(MACAddress m) {

		if(System.currentTimeMillis() - timings[m] >= 15*1000) {
			return true;
		}
		return false;

	}

	private void setTime(MACAddress m) {

		timings.put(m, System.currentTimeMillis());
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		// if the source mac address does not exist, then place that in
		// the hashmap and update the timing
		if(!forwardingTable.containsKey(etherPacket.getSourceMACAddres())) {
			forwardingTable.put(etherPacket.getSourceMACAddres(),inIface); // update table
			setTime(etherPacket.getSourceMACAddres()); // set the timing
		} else {
			// if it does exist, then update the timing for this mac address in the timings
			setTime(etherPacket.getSourceMACAddres());

		}

		// check if the other mac addresses, have been accesses in the 
		// 15 seconds since the last time that packet arrived
		for(MACAddress m : timings) {
			if(checkTime(m)) {
				forwardingTable.remove(m);
				timings.remove(m);
			}
		}


		// check if the ethernet destination host exists within the hashmap 
		if(forwardingTable[etherPacket.getDestinationMACAddress()] == null) {

			// if it does not exist in the table, broadcast first 
				interfaces = getInterfaces();
				
				// iterate through the set of keys
				for(String name : interfaces.keySet()) {

					// if sendPacket returns true, this means
					// that the destination host has
					// accepted our packet, which means we can
					// add it to our table.
					if(sendPacket(etherPacket, interfaces[name]) {
						forwardingTable.put(etherPacket.getDestinationMACAddress(),interfaces[name]); // update table
						//setTime(etherPacket.getDestinationMACAddress()); // set timing
					}


				}


		} else {
			// in this case, we assume that the destination MAC address 
			// has an interface associated with it. 

			sendPacket(etherPacket, forwardingTable[etherPacket.getDestinationMACAddress()]);

		}
		

		/********************************************************************/
	}
}
