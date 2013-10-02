package xdi2.tools.commands;

import java.io.StringWriter;

import xdi2.client.http.XDIHttpClient;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("discovery")
@CommandUsage("address [endpoint]")
@CommandArgs(min=1,max=1)
public class CommandDiscovery implements Command {

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String address = commandArgs[0];
		String endpoint = commandArgs.length > 1 ? commandArgs[1] : null;

		XDIDiscoveryResult discoveryResult1 = null;
		XDIDiscoveryResult discoveryResult2 = null;

		long start = System.currentTimeMillis();

		try {

			// start discovery
			
			XDIDiscoveryClient discoveryClient = endpoint == null ? new XDIDiscoveryClient() : new XDIDiscoveryClient(new XDIHttpClient(endpoint));

			// from registry
			
			discoveryResult1 = discoveryClient.discoverFromRegistry(XDI3Segment.create(address));
			
			// from authority
			
			if (discoveryResult1 != null && discoveryResult1.getXdiEndpointUri() != null) {

				discoveryResult2 = discoveryClient.discoverFromAuthority(discoveryResult1.getXdiEndpointUri(), discoveryResult1.getCloudNumber());
			}
			
			// output result

			StringWriter writer = new StringWriter();
			StringWriter writer2 = new StringWriter();

			if (discoveryResult1 != null) {
				
				writer.write("Information from registry:\n\n");
			
				writer.write("Cloud Number: " + discoveryResult1.getCloudNumber() + "\n");
				writer.write("XDI Endpoint URI: " + discoveryResult1.getXdiEndpointUri() + "\n");
				writer.write("Public Key: " + discoveryResult1.getPublicKey() + "\n");
				writer.write("Services: " + discoveryResult1.getServices() + "\n");
			} else {
				
				writer.write("No discovery result from registry.\n");
			}

			if (discoveryResult2 != null) {
				
				writer2.write("Information from authority:\n\n");
			
				writer2.write("Cloud Number: " + discoveryResult2.getCloudNumber() + "\n");
				writer2.write("XDI Endpoint URI: " + discoveryResult2.getXdiEndpointUri() + "\n");
				writer2.write("Public Key: " + discoveryResult2.getPublicKey() + "\n");
				writer2.write("Services: " + discoveryResult2.getServices() + "\n");
			} else {
				
				writer2.write("No discovery result from authority.\n");
			}

			System.out.println(writer.getBuffer().toString());
			System.out.println(writer2.getBuffer().toString());
		} catch (Exception ex) {

			System.out.println(ex.getMessage());
		}

		long stop = System.currentTimeMillis();

		String stats = "";
		stats += Long.toString(stop - start) + " ms time. ";
		if (discoveryResult1 != null && discoveryResult1.getMessageResult() != null) stats += Long.toString(discoveryResult1.getMessageResult().getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s) from registry. ";
		if (discoveryResult2 != null && discoveryResult2.getMessageResult() != null) stats += Long.toString(discoveryResult2.getMessageResult().getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s) from authority. ";
		
		System.out.println(stats);
	}
}
