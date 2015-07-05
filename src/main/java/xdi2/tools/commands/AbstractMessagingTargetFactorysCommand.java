package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.messaging.target.factory.impl.uri.UriMessagingTargetFactory;
import xdi2.transport.registry.impl.uri.UriMessagingTargetRegistry;

public abstract class AbstractMessagingTargetFactorysCommand <T> implements Command {

	protected void commandMessagingTargetFactorys(String applicationContextPath, T state) throws Exception {

		UriMessagingTargetRegistry uriMessagingTargetRegistry = CommandUtil.getUriMessagingTargetRegistry(applicationContextPath);
		if (uriMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingTargetRegistry' not found in " + applicationContextPath);

		for (String messagingTargetFactoryPath : uriMessagingTargetRegistry.getMessagingTargetFactoryPaths()) {

			UriMessagingTargetFactory messagingTargetFactory = uriMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			if (messagingTargetFactory == null) continue;

			this.callbackMessagingTargetFactory(messagingTargetFactoryPath, messagingTargetFactory, uriMessagingTargetRegistry, state);
		}
	}

	protected abstract void callbackMessagingTargetFactory(String messagingTargetFactoryPath, UriMessagingTargetFactory messagingTargetFactory, UriMessagingTargetRegistry uriMessagingTargetRegistry, T state) throws Exception;
}
