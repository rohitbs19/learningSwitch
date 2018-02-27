package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
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
		
		// if the packet is IPv4
		if(etherPacket.getEtherType() == 4) { 
		
			// get the IP header and cast it to IPv4
			IPv4 pkt = (IPv4) etherPacket.getPayload();
			
			// store the existing checksum			
			short ip_checksum = pkt.getChecksum();
			short calculated_checksum;
			// zero out existing checksum
			pkt.setChecksum(0);


			
			// computes new checksum
			byte[] data = new byte[pkt.getHeaderLength()*4];
			ByteBuffer bb = ByteBuffer.wrap(data);

			bb.put((byte) (((pkt.getVersion() & 0xf) << 4) | (pkt.getHeaderLength() & 0xf)));
			bb.put(pkt.getDiffServ());
			bb.putShort(pkt.getHeaderLength()*4);
			bb.putShort(pkt.getIdentification());
			bb.putShort((short) (((pkt.getFlags() & 0x7) << 13) | (pkg.getFragmentOffset() & 0x1fff)));
			bb.put(pkt.getTtl());
			bb.put(pkt.getProtocol());
			bb.putShort(pkt.getChecksum());
			bb.putInt(pkt.getSourceAddress());
			bb.putInt(pkt.getDestinationAddress());
			if (pkt.getOption() != null)
			    bb.put(this.options);

			// compute checksum if needed
			bb.rewind();
			int accumulation = 0;
			for (int i = 0; i < pkt.getHeaderLength() * 2; ++i) {
				accumulation += 0xffff & bb.getShort();
			}
			accumulation = ((accumulation >> 16) & 0xffff)
			 + (accumulation & 0xffff);
			   
			calculated_checksum = (short) (~accumulation & 0xffff);
			
			// if the checksums match, then reset the checksum to
			// new checksum	
			if(calculated_checksum == ip_checksum) {
			    
			     pkt.setChecksum((short) (~accumulation & 0xffff));
		
				// decrement the ttl	
				pkt.setTtl(pkt.getTtl()-1);
	
				if(pkt.getTtl() > 0){
					// obtain all the interfaces	
				   	 Map<String, Iface> interfaces = getInterfaces();
	

					boolean flag = false
					// iterate through the key set
					for(String name : interfaces.keySet()) { 
						
						if(pkt.getDestinationAddress() == interfaces.get(name).getIPAddress()) {
							break;
							flag = true;
						}	

					}


					if(!flag)  {
							
						RouteEntry dest; 
						if((dest = routeTable.lookup(pkt.getDestinationAddress)) != null) {
							// get the MAC Address of the next hop
							byte[] next_dest_mac = arpCache.lookup(dest.getGatewayAddress()).toBytes();					
							
							byte[] new_source_mac = etherPacket.getDestinationMACAddress();
								
							etherPacket.setDestinationMACAddress(new_dest_mac);	
							etherPacket.setSourceMACAddress(new_source_mac);

							Iface temp = null;
				   	 		Map<String, Iface> interfaces = getInterfaces();
							for(String name : interfaces.keySet()) {
							
								if(etherPacket.getDestinationMAC().equals(interfaces.get(name).getMACAddress())) {
									temp=interfaces.get(name);
									break;
								}
							}
							
							if(temp != null) { 
								sendPacket(etherPacket, temp);
							}



	
						}													
						



					}
					

				}
			}	
			
			
	
		}

		
		
		/********************************************************************/
	}
}
