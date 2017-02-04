package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.messaging.container.factory.impl.uri.UriMessagingContainerFactory;
import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

public abstract class AbstractMessagingContainerFactorysCommand <T> implements Command {

	protected void commandMessagingContainerFactorys(String applicationContextPath, T state) throws Exception {

		UriMessagingContainerRegistry uriMessagingContainerRegistry = CommandUtil.getUriMessagingContainerRegistry(applicationContextPath);
		if (uriMessagingContainerRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingContainerRegistry' not found in " + applicationContextPath);

		for (String messagingContainerFactoryPath : uriMessagingContainerRegistry.getMessagingContainerFactoryPaths()) {

			UriMessagingContainerFactory messagingContainerFactory = uriMessagingContainerRegistry.getMessagingContainerFactory(messagingContainerFactoryPath);
			if (messagingContainerFactory == null) continue;

			this.callbackMessagingContainerFactory(messagingContainerFactoryPath, messagingContainerFactory, uriMessagingContainerRegistry, state);
		}
	}

	protected abstract void callbackMessagingContainerFactory(String messagingContainerFactoryPath, UriMessagingContainerFactory messagingContainerFactory, UriMessagingContainerRegistry uriMessagingContainerRegistry, T state) throws Exception;
}
