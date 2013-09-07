package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

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

@CommandName("list-graphs")
@CommandUsage("[path-to-applicationContext.xml]")
@CommandArgs(min=0,max=1)
public class CommandListGraphs implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String applicationContextPath;

		if (commandArgs.length == 1) {

			applicationContextPath = commandArgs[0];
		} else if (commandArgs.length == 0) {

			applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;
		} else {

			return;
		}

		HttpEndpointRegistry httpEndpointRegistry = CommandUtil.getHttpEndpointRegistry(applicationContextPath);
		if (httpEndpointRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpEndpointRegistry' not found in " + applicationContextPath);

		printMessagingTargets(httpEndpointRegistry);
		printMessagingTargetFactorys(httpEndpointRegistry);
	}

	private static void printMessagingTargets(HttpEndpointRegistry httpEndpointRegistry) {

		for (String messagingTargetPath : httpEndpointRegistry.getMessagingTargetPaths()) {

			MessagingTarget messagingTarget = httpEndpointRegistry.getMessagingTarget(messagingTargetPath);

			printMessagingTarget(messagingTargetPath, messagingTarget, "");
		}
	}

	private static void printMessagingTargetFactorys(HttpEndpointRegistry httpEndpointRegistry) throws Xdi2ServerException, Xdi2MessagingException {

		for (String messagingTargetFactoryPath : httpEndpointRegistry.getMessagingTargetFactoryPaths()) {

			MessagingTargetFactory messagingTargetFactory = httpEndpointRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);

			printMessagingTargetFactory(messagingTargetFactoryPath, messagingTargetFactory, httpEndpointRegistry);
		}
	}

	private static void printMessagingTarget(String messagingTargetPath, MessagingTarget messagingTarget, String indent) {

		StringBuilder buffer = new StringBuilder();

		buffer.append(indent);
		buffer.append(messagingTargetPath + " --> " + messagingTarget.getClass().getSimpleName());

		if (messagingTarget instanceof GraphMessagingTarget) {

			buffer.append(" (" + ((GraphMessagingTarget) messagingTarget).getGraph().getClass().getSimpleName() + ")");
		}

		System.out.println(buffer.toString());
	}

	private static void printMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpEndpointRegistry httpEndpointRegistry) throws Xdi2ServerException, Xdi2MessagingException {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingTargetFactoryPath + " --> " + messagingTargetFactory.getClass().getSimpleName());

		System.out.println(buffer.toString());

		if (messagingTargetFactory instanceof RegistryGraphMessagingTargetFactory) {

			Iterator<String> requestPaths = ((RegistryGraphMessagingTargetFactory) messagingTargetFactory).getRequestPaths(messagingTargetFactoryPath);

			while (requestPaths.hasNext()) {

				String requestPath = requestPaths.next();
				String messagingTargetPath = requestPath;

				MessagingTarget messagingTarget = messagingTargetFactory.mountMessagingTarget(httpEndpointRegistry, messagingTargetFactoryPath, requestPath);

				printMessagingTarget(messagingTargetPath, messagingTarget, "  ");
			}
		}
	}
}
