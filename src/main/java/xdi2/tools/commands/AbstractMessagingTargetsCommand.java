package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.syntax.XDIArc;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.factory.impl.uri.RegistryUriMessagingTargetFactory;
import xdi2.messaging.target.factory.impl.uri.UriMessagingTargetFactory;
import xdi2.transport.registry.impl.uri.UriMessagingTargetRegistry;

public abstract class AbstractMessagingTargetsCommand <T> extends AbstractMessagingTargetFactorysCommand<T> implements Command {

	protected void commandMessagingTargets(String applicationContextPath, T state) throws Exception {

		UriMessagingTargetRegistry uriMessagingTargetRegistry = CommandUtil.getUriMessagingTargetRegistry(applicationContextPath);
		if (uriMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingTargetRegistry' not found in " + applicationContextPath);

		for (String messagingTargetPath : uriMessagingTargetRegistry.getMessagingTargetPaths()) {

			MessagingTarget messagingTarget = uriMessagingTargetRegistry.getMessagingTarget(messagingTargetPath);
			if (messagingTarget == null) continue;

			this.callbackMessagingTarget(messagingTargetPath, messagingTarget, uriMessagingTargetRegistry, state);
		}

		this.commandMessagingTargetFactorys(applicationContextPath, state);
	}

	@Override
	protected void callbackMessagingTargetFactory(final String messagingTargetFactoryPath, final UriMessagingTargetFactory messagingTargetFactory, UriMessagingTargetRegistry uriMessagingTargetRegistry, T state) throws Exception {

		if (! (messagingTargetFactory instanceof RegistryUriMessagingTargetFactory)) return;

		Iterator<XDIArc> ownerPeerRootXDIArcs = messagingTargetFactory.getOwnerPeerRootXDIArcs();

		Iterator<String> requestPaths = new MappingIterator<XDIArc, String> (ownerPeerRootXDIArcs) {

			@Override
			public String map(XDIArc ownerPeerRootXDIArc) {

				return messagingTargetFactory.getRequestPath(messagingTargetFactoryPath, ownerPeerRootXDIArc);
			}
		};

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingTargetPath = requestPath;

			MessagingTarget messagingTarget = messagingTargetFactory.mountMessagingTarget(uriMessagingTargetRegistry, messagingTargetFactoryPath, requestPath);

			this.callbackMessagingTarget(messagingTargetPath, messagingTarget, uriMessagingTargetRegistry, state);
		}
	}

	protected abstract void callbackMessagingTarget(String messagingTargetPath, MessagingTarget messagingTarget, UriMessagingTargetRegistry uriMessagingTargetRegistry, T state) throws Exception;
}
