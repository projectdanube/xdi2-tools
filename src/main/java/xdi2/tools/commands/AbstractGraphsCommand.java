package xdi2.tools.commands;

import xdi2.core.Graph;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.transport.registry.impl.uri.UriMessagingTargetRegistry;

public abstract class AbstractGraphsCommand <T> extends AbstractMessagingTargetsCommand<T> implements Command {

	protected void commandGraphs(String applicationContextPath, T state) throws Exception {

		this.commandMessagingTargets(applicationContextPath, state);
	}

	@Override
	protected void callbackMessagingTarget(String messagingTargetPath, MessagingTarget messagingTarget, UriMessagingTargetRegistry uriMessagingTargetRegistry, T state) throws Exception {

		if (! (messagingTarget instanceof GraphMessagingTarget)) return;

		this.callbackGraph(messagingTargetPath, ((GraphMessagingTarget) messagingTarget).getGraph(), state);
	}

	protected abstract void callbackGraph(String messagingTargetPath, Graph graph, T state) throws Exception;
}
