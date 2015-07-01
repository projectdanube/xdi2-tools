package xdi2.tools.commands;

import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import xdi2.client.impl.http.XDIHttpClient;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("discovery")
@CommandUsage("address [endpoint]")
@CommandArgs(min=1,max=2)
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

			discoveryResultRegistry = discoveryClient.discoverFromRegistry(XDIAddress.create(address), null);

			// from authority

			if (discoveryResultRegistry != null && discoveryResultRegistry.getXdiEndpointUrl() != null) {

				discoveryResultAuthority = discoveryClient.discoverFromAuthority(discoveryResultRegistry.getXdiEndpointUrl(), discoveryResultRegistry.getCloudNumber(), null);
			}

			// output result

			StringWriter writer = new StringWriter();
			StringWriter writer2 = new StringWriter();

			if (discoveryResultRegistry != null) {

				writer.write("Information from registry:\n\n");

				writer.write("Cloud Number: " + discoveryResultRegistry.getCloudNumber() + "\n");
				writer.write("XDI Endpoint URI: " + discoveryResultRegistry.getXdiEndpointUrl() + "\n");
				writer.write("Signature Public Key: " + discoveryResultRegistry.getSignaturePublicKey() + "\n");
				writer.write("Encryption Public Key: " + discoveryResultRegistry.getEncryptionPublicKey() + "\n");

				if (discoveryResultRegistry.getEndpointUris().isEmpty()) {

					writer.write("Services: (none)\n");
				} else {

					for (Map.Entry<XDIAddress, URI> endpointUri : discoveryResultRegistry.getEndpointUris().entrySet()) {

						writer.write("Service " + endpointUri.getKey() + ": " + endpointUri.getValue() + "\n");
					}
				}

				writer.write("\n");
			} else {

				writer.write("No discovery result from registry.\n");
			}

			if (discoveryResultAuthority != null) {

				writer2.write("Information from authority:\n\n");

				writer2.write("Cloud Number: " + discoveryResultAuthority.getCloudNumber() + "\n");
				writer2.write("XDI Endpoint URI: " + discoveryResultAuthority.getXdiEndpointUrl() + "\n");
				writer2.write("Signature Public Key: " + discoveryResultAuthority.getSignaturePublicKey() + "\n");
				writer2.write("Encryption Public Key: " + discoveryResultAuthority.getEncryptionPublicKey() + "\n");

				if (discoveryResultAuthority.getEndpointUris().isEmpty()) {

					writer2.write("Services: (none)\n");
				} else {

					for (Map.Entry<XDIAddress, URI> endpointUri : discoveryResultAuthority.getEndpointUris().entrySet()) {

						writer2.write("Service " + endpointUri.getKey() + ": " + endpointUri.getValue() + "\n");
					}
				}

				writer2.write("\n");
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
		if (discoveryResultRegistry != null && discoveryResultRegistry.getMessagingResponse() != null) stats += Long.toString(discoveryResultRegistry.getMessagingResponse().getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s) from registry. ";
		if (discoveryResultAuthority != null && discoveryResultAuthority.getMessagingResponse() != null) stats += Long.toString(discoveryResultAuthority.getMessagingResponse().getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s) from authority. ";

		System.out.println(stats);
	}
}
