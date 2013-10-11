package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.registry.HttpEndpointRegistry;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("dump-graph")
@CommandUsage("request-path [mime-type] [path-to-applicationContext.xml]")
@CommandArgs(min=1,max=3)
public class CommandDumpGraph implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String requestPath = commandArgs[0];
		String mimeType = commandArgs.length > 1 ? commandArgs[1] : null;
		String applicationContextPath = commandArgs.length > 2 ? commandArgs[2] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		HttpEndpointRegistry httpEndpointRegistry = CommandUtil.getHttpEndpointRegistry(applicationContextPath);
		if (httpEndpointRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpEndpointRegistry' not found in " + applicationContextPath);

		String messagingTargetPath = httpEndpointRegistry.findMessagingTargetPath(requestPath);
		MessagingTarget messagingTarget = messagingTargetPath == null ? null : httpEndpointRegistry.getMessagingTarget(messagingTargetPath);
		
		if (messagingTarget == null) {
			
			String messagingTargetFactoryPath = httpEndpointRegistry.findMessagingTargetFactoryPath(requestPath);
			MessagingTargetFactory messagingTargetFactory = messagingTargetFactoryPath == null ? null : httpEndpointRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			messagingTarget = messagingTargetFactory == null ? null : messagingTargetFactory.mountMessagingTarget(httpEndpointRegistry, messagingTargetFactoryPath, requestPath);
		}
		
		if (messagingTarget == null) {
			
			System.out.println("No messaging target found at request path " + requestPath);
			return;
		}
		
		Graph graph = messagingTarget instanceof GraphMessagingTarget ? ((GraphMessagingTarget) messagingTarget).getGraph() : null;
		
		if (graph == null) {
			
			System.out.println("Messaging target at request path " + requestPath + " has no graph.");
			return;
		}

		XDIWriter writer = mimeType == null ? XDIWriterRegistry.getDefault() : XDIWriterRegistry.forMimeType(new MimeType(mimeType));

		writer.write(graph, System.out);
	}
}
