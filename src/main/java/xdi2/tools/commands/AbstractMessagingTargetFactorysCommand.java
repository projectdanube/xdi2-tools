package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.messaging.target.factory.impl.uri.UriMessagingTargetFactory;
import xdi2.transport.registry.impl.uri.UriMessagingTargetRegistry;

public abstract class AbstractMessagingTargetFactorysCommand <T> implements Command {

	protected void commandMessagingTargetFactorys(String applicationContextPath, T state) throws Exception {

		UriMessagingTargetRegistry httpMessagingTargetRegistry = CommandUtil.getUriMessagingTargetRegistry(applicationContextPath);
		if (httpMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingTargetRegistry' not found in " + applicationContextPath);

		for (String messagingTargetFactoryPath : httpMessagingTargetRegistry.getMessagingTargetFactoryPaths()) {

			UriMessagingTargetFactory messagingTargetFactory = httpMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			if (messagingTargetFactory == null) continue;

			this.callbackMessagingTargetFactory(messagingTargetFactoryPath, messagingTargetFactory, httpMessagingTargetRegistry, state);
		}
	}

	protected abstract void callbackMessagingTargetFactory(String messagingTargetFactoryPath, UriMessagingTargetFactory messagingTargetFactory, UriMessagingTargetRegistry httpMessagingTargetRegistry, T state) throws Exception;
}
