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

		XDIDiscoveryResult discoveryResultRegistry = null;
		XDIDiscoveryResult discoveryResultAuthority = null;

		long start = System.currentTimeMillis();

		try {

			// start discovery

			XDIDiscoveryClient discoveryClient = endpoint == null ? new XDIDiscoveryClient() : new XDIDiscoveryClient(new XDIHttpClient(endpoint));

			// from registry

			discoveryResultRegistry = discoveryClient.discoverFromRegistry(XDI3Segment.create(address));

			// from authority

			if (discoveryResultRegistry != null && discoveryResultRegistry.getXdiEndpointUri() != null) {

				discoveryResultAuthority = discoveryClient.discoverFromAuthority(discoveryResultRegistry.getXdiEndpointUri(), discoveryResultRegistry.getCloudNumber());
			}

			// output result

			StringWriter writer = new StringWriter();
			StringWriter writer2 = new StringWriter();

			if (discoveryResultRegistry != null) {

				writer.write("Information from registry:\n\n");

				writer.write("Cloud Number: " + discoveryResultRegistry.getCloudNumber() + "\n");
				writer.write("XDI Endpoint URI: " + discoveryResultRegistry.getXdiEndpointUri() + "\n");
				writer.write("Signature Public Key: " + discoveryResultRegistry.getSignaturePublicKey() + "\n");
				writer.write("Encryption Public Key: " + discoveryResultRegistry.getEncryptionPublicKey() + "\n");
				writer.write("Services: " + discoveryResultRegistry.getServices() + "\n");
			} else {

				writer.write("No discovery result from registry.\n");
			}

			if (discoveryResultAuthority != null) {

				writer2.write("Information from authority:\n\n");

				writer2.write("Cloud Number: " + discoveryResultAuthority.getCloudNumber() + "\n");
				writer2.write("XDI Endpoint URI: " + discoveryResultAuthority.getXdiEndpointUri() + "\n");
				writer2.write("Signature Public Key: " + discoveryResultAuthority.getSignaturePublicKey() + "\n");
				writer2.write("Encryption Public Key: " + discoveryResultAuthority.getEncryptionPublicKey() + "\n");
				writer2.write("Services: " + discoveryResultAuthority.getServices() + "\n");
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
		if (discoveryResultRegistry != null && discoveryResultRegistry.getMessageResult() != null) stats += Long.toString(discoveryResultRegistry.getMessageResult().getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s) from registry. ";
		if (discoveryResultAuthority != null && discoveryResultAuthority.getMessageResult() != null) stats += Long.toString(discoveryResultAuthority.getMessageResult().getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s) from authority. ";

		System.out.println(stats);
	}
}
