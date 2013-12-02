package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.messaging.target.MessagingTarget;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.factory.impl.RegistryGraphMessagingTargetFactory;
import xdi2.server.registry.HttpMessagingTargetRegistry;

public abstract class AbstractMessagingTargetsCommand <T> extends AbstractMessagingTargetFactorysCommand<T> implements Command {

	protected void commandMessagingTargets(String applicationContextPath, T state) throws Exception {

		HttpMessagingTargetRegistry httpMessagingTargetRegistry = CommandUtil.getHttpMessagingTargetRegistry(applicationContextPath);
		if (httpMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpMessagingTargetRegistry' not found in " + applicationContextPath);

		for (String messagingTargetPath : httpMessagingTargetRegistry.getMessagingTargetPaths()) {

			MessagingTarget messagingTarget = httpMessagingTargetRegistry.getMessagingTarget(messagingTargetPath);
			if (messagingTarget == null) continue;

			this.callbackMessagingTarget(messagingTargetPath, messagingTarget, httpMessagingTargetRegistry, state);
		}

		this.commandMessagingTargetFactorys(applicationContextPath, state);
	}

	protected void callbackMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpMessagingTargetRegistry httpMessagingTargetRegistry, T state) throws Exception {

		if (! (messagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) return;

		Iterator<String> requestPaths = ((RegistryGraphMessagingTargetFactory) messagingTargetFactory).getRequestPaths(messagingTargetFactoryPath);

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingTargetPath = requestPath;

			MessagingTarget messagingTarget = messagingTargetFactory.mountMessagingTarget(httpMessagingTargetRegistry, messagingTargetFactoryPath, requestPath);

			this.callbackMessagingTarget(messagingTargetPath, messagingTarget, httpMessagingTargetRegistry, state);
		}
	}

	protected abstract void callbackMessagingTarget(String messagingTargetPath, MessagingTarget messagingTarget, HttpMessagingTargetRegistry httpMessagingTargetRegistry, T state) throws Exception;
}
