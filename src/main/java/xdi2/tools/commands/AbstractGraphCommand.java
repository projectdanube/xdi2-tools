package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.registry.HttpEndpointRegistry;

public abstract class AbstractGraphCommand <T> implements Command {

	protected void commandGraph(String applicationContextPath, String requestPath, T state) throws Exception {

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

		this.callbackGraph(messagingTargetPath, graph, state);
	}

	protected abstract void callbackGraph(String messagingTargetPath, Graph graph, T state) throws Exception;
}
