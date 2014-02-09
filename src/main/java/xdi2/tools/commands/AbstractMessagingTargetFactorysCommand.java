package xdi2.tools.commands;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.transport.impl.http.factory.MessagingTargetFactory;
import xdi2.transport.impl.http.registry.HttpMessagingTargetRegistry;

public abstract class AbstractMessagingTargetFactorysCommand <T> implements Command {

	protected void commandMessagingTargetFactorys(String applicationContextPath, T state) throws Exception {

		HttpMessagingTargetRegistry httpMessagingTargetRegistry = CommandUtil.getHttpMessagingTargetRegistry(applicationContextPath);
		if (httpMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpMessagingTargetRegistry' not found in " + applicationContextPath);

		for (String messagingTargetFactoryPath : httpMessagingTargetRegistry.getMessagingTargetFactoryPaths()) {

			MessagingTargetFactory messagingTargetFactory = httpMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			if (messagingTargetFactory == null) continue;

			this.callbackMessagingTargetFactory(messagingTargetFactoryPath, messagingTargetFactory, httpMessagingTargetRegistry, state);
		}
	}

	protected abstract void callbackMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpMessagingTargetRegistry httpMessagingTargetRegistry, T state) throws Exception;
}
