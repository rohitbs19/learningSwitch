package edu.wisc.cs.sdn.vnet.rt;
import net.floodlightcontroller.packet.BasePacket;
import net.floodlightcontroller.packet.IPacket;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.nio.ByteBuffer;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.Ethernet;
import java.util.Map;
import java.util.*;
/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */

public class Router extends Device
{
	/** Routing table for the router */
	private RouteTable routeTable;
	//IPacket payload; /** ARP cache for the router */
	private ArpCache arpCache;

	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}

	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }

	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}

		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}

	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}

		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
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
		/*CHECKING PACKETS*/
		/*
		 * 1: check the version of the Ip address using getEtherType()
		 * 2: if proper version that is IpV4 then proceed to 3 or else drop packet
		 * 3: if 2 then verify checksum and TTL of IP packet. getPayload of Ethernet class to get the IPV4 header;
		 * 4:  take care of the casting of 3 then verify the checksum using serialize in Ipv4 class
		 * 5: decrement the TTL by 1
		 * 6:  if TTl==0 then drop the packet
		 * 7: check from the map of interfaces if the ip matches with any interface ip if yes drop
		 * 8: then use the lookup function that we use
		 * 9: */
		//System.out.println("hello");

		//System.out.println("*** -> Received packet: " +
		//		etherPacket.toString().replace("\n", "\n\t"));
		//System.out.println("hello1");
		/********************************************************************/
		/* TODO: Handle packets                                             */

		/*

			1. Check the version number uing getEherType(); check if IPv4
			2. Extract IP header using getPayload()
			3. Verify checksum and TTL using the header
			4. Compute checksum using code from serialize();
			5. decrememnt TTL
			6. Check validity of TTL, if 0 drop packet
			7. Check destination IP address against the ip addresses
			of each interface, if equal ?  drop.

		*/
		/*
		System.out.println("Ethernet packet type = " + etherPacket.getEtherType());
		System.out.println("Ethernet type for TYPE_IPv4 = " + Ethernet.TYPE_IPv4);
		System.out.println("Ethernet type for TYPE_ARP" + Ethernet.TYPE_ARP);

		System.out.println("Ethernet type for TYPE_RARP" + Ethernet.TYPE_RARP);

		System.out.println("Ethernet type for TYPE_LLDP" + Ethernet.TYPE_LLDP);

		System.out.println("Ethernet type for TYPE_BSN" + Ethernet.TYPE_BSN);

		System.out.println("Ethernet type for TYPE_VLAN_UNTAGGED" + Ethernet.VLAN_UNTAGGED);
		//i if the packet is IPv4
		*/
		if(etherPacket.getEtherType() == Ethernet.TYPE_IPv4) {
			//	System.out.println("hello123");

			// get the IP header and cast it to IPv4
			IPv4 pkt = (IPv4) etherPacket.getPayload();

			// store the existing checksum
			short ip_checksum = pkt.getChecksum();
			System.out.println("original checksum ==> " + ip_checksum);
			short calculated_checksum;
			// zero out existing checksum

			pkt.setChecksum((short)0);

			// computes new checksum
			System.out.println("head Len ==> " + pkt.getHeaderLength() * 4);
			int optionsLength = 0;
			byte headerLength = pkt.getHeaderLength();
			short totalLength = 0;

			if (pkt.getOptions() != null) {
				optionsLength = pkt.getOptions().length / 4;
				headerLength = (byte) (headerLength + optionsLength);

			}
			byte[] data = new byte[(pkt.getHeaderLength()*4)];

			ByteBuffer bb = ByteBuffer.wrap(data);

			totalLength = (short) pkt.getTotalLength();

			bb.put((byte) (((pkt.getVersion() & 0xf) << 4) | (headerLength & 0xf)));

			bb.put(pkt.getDiffServ());

			System.out.println("Diff serv ==> " + pkt.getDiffServ());
			bb.putShort(totalLength);
			System.out.println("total length ==> " + pkt.getTotalLength());

			bb.putShort(pkt.getIdentification());
			System.out.println("ident==> " + pkt.getIdentification());

			bb.putShort((short) (((pkt.getFlags() & 0x7) << 13) | (pkt.getFragmentOffset() & 0x1fff)));

			bb.put(pkt.getTtl());
			System.out.println("ttl ==> "+pkt.getTtl());

			bb.put(pkt.getProtocol());
			System.out.println("protocol ==> "+pkt.getProtocol());

			bb.putShort(pkt.getChecksum());
			System.out.println("checksum ==> " + pkt.getChecksum());

			bb.putInt(pkt.getSourceAddress());
			System.out.println("src addrs ==> " + pkt.getSourceAddress());

			bb.putInt(pkt.getDestinationAddress());
			System.out.println("Destination addrs ==> " + pkt.getDestinationAddress());

			if (pkt.getOptions() != null){
				System.out.println("options ==> " + pkt.getOptions());

				bb.put(pkt.getOptions());
			}

			// compute checksum if needed
			bb.rewind();
			int accumulation = 0;
			for (int i = 0; i < headerLength * 2; ++i) {
				accumulation += 0xffff & bb.getShort();
			}
			accumulation = ((accumulation >> 16) & 0xffff)
					+ (accumulation & 0xffff);

			calculated_checksum = (short) (~accumulation & 0xffff);

			// if the checksums match, then reset the checksum to
			// new checksum
			System.out.println("calculated_checksum ==> " + calculated_checksum);

			if((calculated_checksum + 0)== (ip_checksum )) {
				System.out.println("check to see if the checksum is being calculated properly");
				pkt.setChecksum((short) (~accumulation & 0xffff));

				// decrement the ttl
				//conflict bw int and byte type as pkt.getTtl is of byte type
				byte temp = (byte)(pkt.getTtl() -1);
				pkt.setTtl(temp);

				if(pkt.getTtl() > 0){
					// obtain all the interfaces
					Map<String, Iface> interfaces = getInterfaces();
					boolean flag = false;
					// iterate through the key set
					for(String name : interfaces.keySet()) {
						if(pkt.getDestinationAddress() == interfaces.get(name).getIpAddress()) {
							flag = true;
							break;
						}
					}
					if(!flag)  {
						System.out.println("flag gets checked \n");

						RouteEntry dest;
						System.out.println("destination address from Router.java = " + pkt.getDestinationAddress());
						dest = routeTable.lookup(pkt.getDestinationAddress(), pkt.getSourceAddress());
						
						if(dest != null) {
							System.out.println("dest ========>     " + dest);
							System.out.println("Destination address =======> * " + dest.getDestinationAddress());

							// get the MAC Address of the next hop
							System.out.println(pkt.toString());						
				

							byte[] next_dest_mac = null				;
							// if the gateway address of the destination is not zero, then
							// send it to the gateway
							System.out.println(arpCache.toString());
							ArpEntry entryII = null;
							if (dest.getGatewayAddress() != 0) {
								ArpEntry entryI = arpCache.lookup(dest.getGatewayAddress());
								if (entryI != null) {
									next_dest_mac = entryI.getMac().toBytes();
								}
							} else {
								// if the gateway is zero, that means we have reached the destination								       // node, so we can send it directly to the host that is attached to 
								// this node.
								entryII = arpCache.lookup(pkt.getDestinationAddress());
								System.out.println(" entryII value = " + entryII);
								if (entryII != null) {
									next_dest_mac = entryII.getMac().toBytes();
								}
							}
							
							//next_dest_mac = arpCache.lookup(dest.getGatewayAddress()).getMac().toBytes();
							System.out.println("next destination address ====> " + next_dest_mac);
							if (next_dest_mac != null) {
								

								
								byte[] new_source_mac = dest.getInterface().getMacAddress().toBytes(); 
								System.out.println("Outgoing interface name : " + dest.getInterface().getName());	
								System.out.println(" Ethernet destination address to be new source  = " + etherPacket.getDestinationMAC().toString());
								// set destination mac address (ultimate)
									etherPacket.setDestinationMACAddress(entryII.getMac().toString());	
								// set source mac address 

								etherPacket.setSourceMACAddress(arpCache.lookup(dest.getInterface().getIpAddress()).getMac().toString());
								System.out.println("next destination address ====> " + entryII.getMac().toString());
								System.out.println("new source address ====> " + dest.getInterface().getMacAddress().toString());
								//Map<String, Iface> interfaces2 = getInterfaces();
								pkt.resetChecksum();
								if (dest.getInterface() != null) {
									System.out.println("enters? *************************");
									sendPacket(etherPacket,dest.getInterface());
								}
							}
						}
					}
				}
			}
		}
	}
}


