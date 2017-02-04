package xdi2.tools.commands;

import xdi2.core.Graph;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

public abstract class AbstractGraphsCommand <T> extends AbstractMessagingContainersCommand<T> implements Command {

	protected void commandGraphs(String applicationContextPath, T state) throws Exception {

		this.commandMessagingContainers(applicationContextPath, state);
	}

	@Override
	protected void callbackMessagingContainer(String messagingContainerPath, MessagingContainer messagingContainer, UriMessagingContainerRegistry uriMessagingContainerRegistry, T state) throws Exception {

		if (! (messagingContainer instanceof GraphMessagingContainer)) return;

		this.callbackGraph(messagingContainerPath, ((GraphMessagingContainer) messagingContainer).getGraph(), state);
	}

	protected abstract void callbackGraph(String messagingContainerPath, Graph graph, T state) throws Exception;
}
