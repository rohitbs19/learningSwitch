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
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		

		if(!forwardingTable.containsKey(etherPacket.getSourceMACAddres())) {
			forwardingTable.put(etherPacket.getSourceMACAddres(),inIface);
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
						forwardingTable.put(etherPacket.getDestinationMACAddress(),interfaces[name]);
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
