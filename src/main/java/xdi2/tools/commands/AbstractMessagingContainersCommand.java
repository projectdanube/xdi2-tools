package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.syntax.XDIArc;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.factory.impl.uri.RegistryUriMessagingContainerFactory;
import xdi2.messaging.container.factory.impl.uri.UriMessagingContainerFactory;
import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

public abstract class AbstractMessagingContainersCommand <T> extends AbstractMessagingContainerFactorysCommand<T> implements Command {

	protected void commandMessagingContainers(String applicationContextPath, T state) throws Exception {

		UriMessagingContainerRegistry uriMessagingContainerRegistry = CommandUtil.getUriMessagingContainerRegistry(applicationContextPath);
		if (uriMessagingContainerRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingContainerRegistry' not found in " + applicationContextPath);

		for (String messagingContainerPath : uriMessagingContainerRegistry.getMessagingContainerPaths()) {

			MessagingContainer messagingContainer = uriMessagingContainerRegistry.getMessagingContainer(messagingContainerPath);
			if (messagingContainer == null) continue;

			this.callbackMessagingContainer(messagingContainerPath, messagingContainer, uriMessagingContainerRegistry, state);
		}

		this.commandMessagingContainerFactorys(applicationContextPath, state);
	}

	@Override
	protected void callbackMessagingContainerFactory(final String messagingContainerFactoryPath, final UriMessagingContainerFactory messagingContainerFactory, UriMessagingContainerRegistry uriMessagingContainerRegistry, T state) throws Exception {

		if (! (messagingContainerFactory instanceof RegistryUriMessagingContainerFactory)) return;

		Iterator<XDIArc> ownerPeerRootXDIArcs = messagingContainerFactory.getOwnerPeerRootXDIArcs();

		Iterator<String> requestPaths = new MappingIterator<XDIArc, String> (ownerPeerRootXDIArcs) {

			@Override
			public String map(XDIArc ownerPeerRootXDIArc) {

				return messagingContainerFactory.getRequestPath(messagingContainerFactoryPath, ownerPeerRootXDIArc);
			}
		};

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingContainerPath = requestPath;

			MessagingContainer messagingContainer = messagingContainerFactory.mountMessagingContainer(uriMessagingContainerRegistry, messagingContainerFactoryPath, requestPath);

			this.callbackMessagingContainer(messagingContainerPath, messagingContainer, uriMessagingContainerRegistry, state);
		}
	}

	protected abstract void callbackMessagingContainer(String messagingContainerPath, MessagingContainer messagingContainer, UriMessagingContainerRegistry uriMessagingContainerRegistry, T state) throws Exception;
}
