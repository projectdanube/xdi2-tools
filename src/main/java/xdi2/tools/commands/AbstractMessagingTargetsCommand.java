package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.messaging.target.MessagingTarget;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.factory.impl.RegistryGraphMessagingTargetFactory;
import xdi2.server.registry.HttpEndpointRegistry;

public abstract class AbstractMessagingTargetsCommand <T> extends AbstractMessagingTargetFactorysCommand<T> implements Command {

	protected void commandMessagingTargets(String applicationContextPath, T state) throws Exception {

		HttpEndpointRegistry httpEndpointRegistry = CommandUtil.getHttpEndpointRegistry(applicationContextPath);
		if (httpEndpointRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpEndpointRegistry' not found in " + applicationContextPath);

		for (String messagingTargetPath : httpEndpointRegistry.getMessagingTargetPaths()) {

			MessagingTarget messagingTarget = httpEndpointRegistry.getMessagingTarget(messagingTargetPath);
			if (messagingTarget == null) continue;

			this.callbackMessagingTarget(messagingTargetPath, messagingTarget, httpEndpointRegistry, state);
		}

		this.commandMessagingTargetFactorys(applicationContextPath, state);
	}

	protected void callbackMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpEndpointRegistry httpEndpointRegistry, T state) throws Exception {

		if (! (messagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) return;

		Iterator<String> requestPaths = ((RegistryGraphMessagingTargetFactory) messagingTargetFactory).getRequestPaths(messagingTargetFactoryPath);

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingTargetPath = requestPath;

			MessagingTarget messagingTarget = messagingTargetFactory.mountMessagingTarget(httpEndpointRegistry, messagingTargetFactoryPath, requestPath);

			this.callbackMessagingTarget(messagingTargetPath, messagingTarget, httpEndpointRegistry, state);
		}
	}

	protected abstract void callbackMessagingTarget(String messagingTargetPath, MessagingTarget messagingTarget, HttpEndpointRegistry httpEndpointRegistry, T state) throws Exception;
}
