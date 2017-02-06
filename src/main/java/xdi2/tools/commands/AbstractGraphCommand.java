package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.factory.impl.uri.UriMessagingContainerFactory;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

public abstract class AbstractGraphCommand <T> implements Command {

	protected void commandGraph(String applicationContextPath, String requestPath, T state) throws Exception {

		UriMessagingContainerRegistry uriMessagingContainerRegistry = CommandUtil.getUriMessagingContainerRegistry(applicationContextPath);
		if (uriMessagingContainerRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingContainerRegistry' not found in " + applicationContextPath);

		String messagingContainerPath = uriMessagingContainerRegistry.findMessagingContainerPath(requestPath);
		MessagingContainer messagingContainer = messagingContainerPath == null ? null : uriMessagingContainerRegistry.getMessagingContainer(messagingContainerPath);

		if (messagingContainer == null) {

			String messagingContainerFactoryPath = uriMessagingContainerRegistry.findMessagingContainerFactoryPath(requestPath);
			UriMessagingContainerFactory messagingContainerFactory = messagingContainerFactoryPath == null ? null : uriMessagingContainerRegistry.getMessagingContainerFactory(messagingContainerFactoryPath);
			messagingContainer = messagingContainerFactory == null ? null : messagingContainerFactory.mountMessagingContainer(uriMessagingContainerRegistry, messagingContainerFactoryPath, requestPath);
		}

		if (messagingContainer == null) {

			System.out.println("No messaging container found at request path " + requestPath);
			return;
		}

		Graph graph = messagingContainer instanceof GraphMessagingContainer ? ((GraphMessagingContainer) messagingContainer).getGraph() : null;

		if (graph == null) {

			System.out.println("Messaging target at request path " + requestPath + " has no graph.");
			return;
		}

		this.callbackGraph(messagingContainerPath, graph, state);
	}

	protected abstract void callbackGraph(String messagingContainerPath, Graph graph, T state) throws Exception;
}
