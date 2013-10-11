package xdi2.tools.commands;

import java.io.IOException;
import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.xri3.XDI3Segment;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.server.exceptions.Xdi2ServerException;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.factory.impl.RegistryGraphMessagingTargetFactory;
import xdi2.server.registry.HttpEndpointRegistry;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("message-graphs")
@CommandUsage("operation target [mime-type] [path-to-applicationContext.xml]")
@CommandArgs(min=2,max=4)
public class CommandMessageGraphs implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String operation = commandArgs[0];
		String target = commandArgs[1];
		String mimeType = commandArgs.length > 2 ? commandArgs[2] : null;
		String applicationContextPath = commandArgs.length > 3 ? commandArgs[3] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		HttpEndpointRegistry httpEndpointRegistry = CommandUtil.getHttpEndpointRegistry(applicationContextPath);
		if (httpEndpointRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpEndpointRegistry' not found in " + applicationContextPath);

		executeMessagingTargets(httpEndpointRegistry, operation, target, mimeType);
		executeMessagingTargetFactorys(httpEndpointRegistry, operation, target, mimeType);
	}

	private static void executeMessagingTargets(HttpEndpointRegistry httpEndpointRegistry, String operation, String target, String mimeType) throws Xdi2MessagingException, IOException {

		for (String messagingTargetPath : httpEndpointRegistry.getMessagingTargetPaths()) {

			MessagingTarget messagingTarget = httpEndpointRegistry.getMessagingTarget(messagingTargetPath);

			executeMessagingTarget(messagingTargetPath, messagingTarget, operation, target, mimeType);
		}
	}

	private static void executeMessagingTargetFactorys(HttpEndpointRegistry httpEndpointRegistry, String operation, String target, String mimeType) throws Xdi2ServerException, Xdi2MessagingException, IOException {

		for (String messagingTargetFactoryPath : httpEndpointRegistry.getMessagingTargetFactoryPaths()) {

			MessagingTargetFactory messagingTargetFactory = httpEndpointRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);

			executeMessagingTargetFactory(messagingTargetFactoryPath, messagingTargetFactory, httpEndpointRegistry, operation, target, mimeType);
		}
	}

	private static void executeMessagingTarget(String messagingTargetPath, MessagingTarget messagingTarget, String operation, String target, String mimeType) throws Xdi2MessagingException, IOException {

		if (messagingTarget == null || ! (messagingTarget instanceof GraphMessagingTarget)) return;

		Graph graph = ((GraphMessagingTarget) messagingTarget).getGraph();

		GraphMessagingTarget commandGraphMessagingTarget = new GraphMessagingTarget();
		commandGraphMessagingTarget.setGraph(graph);

		MessageEnvelope commandMessageEnvelope = MessageEnvelope.fromOperationXriAndTargetAddressOrTargetStatement(XDI3Segment.create(operation), target);
		MessageResult commandMessageResult = new MessageResult();

		commandGraphMessagingTarget.execute(commandMessageEnvelope, commandMessageResult, null);

		XDIWriter writer = mimeType == null ? XDIWriterRegistry.getDefault() : XDIWriterRegistry.forMimeType(new MimeType(mimeType));

		System.out.println("At path " + messagingTargetPath + " executed message on graph " + graph.getClass().getSimpleName());

		writer.write(commandMessageResult.getGraph(), System.out);
	}

	private static void executeMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpEndpointRegistry httpEndpointRegistry, String operation, String target, String mimeType) throws Xdi2ServerException, Xdi2MessagingException, IOException {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingTargetFactoryPath + " --> " + messagingTargetFactory.getClass().getSimpleName());

		System.out.println(buffer.toString());

		if (messagingTargetFactory instanceof RegistryGraphMessagingTargetFactory) {

			Iterator<String> requestPaths = ((RegistryGraphMessagingTargetFactory) messagingTargetFactory).getRequestPaths(messagingTargetFactoryPath);

			while (requestPaths.hasNext()) {

				String requestPath = requestPaths.next();
				String messagingTargetPath = requestPath;

				MessagingTarget messagingTarget = messagingTargetFactory.mountMessagingTarget(httpEndpointRegistry, messagingTargetFactoryPath, requestPath);

				executeMessagingTarget(messagingTargetPath, messagingTarget, operation, target, mimeType);
			}
		}
	}
}
