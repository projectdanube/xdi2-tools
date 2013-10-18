package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.registry.HttpEndpointRegistry;

public abstract class AbstractMessagingTargetFactorysCommand <T> implements Command {

	protected void commandMessagingTargetFactorys(String applicationContextPath, T state) throws Exception {

		HttpEndpointRegistry httpEndpointRegistry = CommandUtil.getHttpEndpointRegistry(applicationContextPath);
		if (httpEndpointRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpEndpointRegistry' not found in " + applicationContextPath);

		for (String messagingTargetFactoryPath : httpEndpointRegistry.getMessagingTargetFactoryPaths()) {

			MessagingTargetFactory messagingTargetFactory = httpEndpointRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			if (messagingTargetFactory == null) continue;

			this.callbackMessagingTargetFactory(messagingTargetFactoryPath, messagingTargetFactory, httpEndpointRegistry, state);
		}
	}

	protected abstract void callbackMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpEndpointRegistry httpEndpointRegistry, T state) throws Exception;
}
