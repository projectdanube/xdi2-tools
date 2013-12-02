package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.registry.HttpMessagingTargetRegistry;

public abstract class AbstractGraphCommand <T> implements Command {

	protected void commandGraph(String applicationContextPath, String requestPath, T state) throws Exception {

		HttpMessagingTargetRegistry httpMessagingTargetRegistry = CommandUtil.getHttpMessagingTargetRegistry(applicationContextPath);
		if (httpMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpMessagingTargetRegistry' not found in " + applicationContextPath);

		String messagingTargetPath = httpMessagingTargetRegistry.findMessagingTargetPath(requestPath);
		MessagingTarget messagingTarget = messagingTargetPath == null ? null : httpMessagingTargetRegistry.getMessagingTarget(messagingTargetPath);

		if (messagingTarget == null) {

			String messagingTargetFactoryPath = httpMessagingTargetRegistry.findMessagingTargetFactoryPath(requestPath);
			MessagingTargetFactory messagingTargetFactory = messagingTargetFactoryPath == null ? null : httpMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			messagingTarget = messagingTargetFactory == null ? null : messagingTargetFactory.mountMessagingTarget(httpMessagingTargetRegistry, messagingTargetFactoryPath, requestPath);
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