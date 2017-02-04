package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.exceptions.Xdi2MessagingException;
import xdi2.messaging.container.factory.impl.uri.RegistryUriMessagingContainerFactory;
import xdi2.messaging.container.factory.impl.uri.UriMessagingContainerFactory;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.transport.exceptions.Xdi2TransportException;
import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

@CommandName("migrate-graphs")
@CommandUsage("path-to-input-applicationContext.xml path-to-output-applicationContext.xml")
@CommandArgs(min=2,max=2)
public class CommandMigrateGraphs implements Command {

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String inputApplicationContextPath;
		String outputApplicationContextPath;

		if (commandArgs.length == 2) {

			inputApplicationContextPath = commandArgs[0];
			outputApplicationContextPath = commandArgs[1];
		} else {

			return;
		}

		UriMessagingContainerRegistry inputUriMessagingContainerRegistry = CommandUtil.getUriMessagingContainerRegistry(inputApplicationContextPath);
		UriMessagingContainerRegistry outputUriMessagingContainerRegistry = CommandUtil.getUriMessagingContainerRegistry(outputApplicationContextPath);
		if (inputUriMessagingContainerRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingContainerRegistry' not found in " + inputApplicationContextPath);
		if (outputUriMessagingContainerRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingContainerRegistry' not found in " + outputApplicationContextPath);

		migrateMessagingContainers(inputUriMessagingContainerRegistry, outputUriMessagingContainerRegistry);
		migrateMessagingContainerFactorys(inputUriMessagingContainerRegistry, outputUriMessagingContainerRegistry);
	}

	private static void migrateMessagingContainers(UriMessagingContainerRegistry inputUriMessagingContainerRegistry, UriMessagingContainerRegistry outputUriMessagingContainerRegistry) {

		for (String messagingContainerPath : inputUriMessagingContainerRegistry.getMessagingContainerPaths()) {

			MessagingContainer inputMessagingContainer = inputUriMessagingContainerRegistry.getMessagingContainer(messagingContainerPath);
			MessagingContainer outputMessagingContainer = outputUriMessagingContainerRegistry.getMessagingContainer(messagingContainerPath);

			migrateMessagingContainer(messagingContainerPath, inputMessagingContainer, outputMessagingContainer);
		}
	}

	private static void migrateMessagingContainerFactorys(UriMessagingContainerRegistry inputUriMessagingContainerRegistry, UriMessagingContainerRegistry outputUriMessagingContainerRegistry) throws Xdi2TransportException, Xdi2MessagingException {

		for (String messagingContainerFactoryPath : inputUriMessagingContainerRegistry.getMessagingContainerFactoryPaths()) {

			UriMessagingContainerFactory inputMessagingContainerFactory = inputUriMessagingContainerRegistry.getMessagingContainerFactory(messagingContainerFactoryPath);
			UriMessagingContainerFactory outputMessagingContainerFactory = outputUriMessagingContainerRegistry.getMessagingContainerFactory(messagingContainerFactoryPath);

			migrateMessagingContainerFactory(messagingContainerFactoryPath, inputMessagingContainerFactory, outputMessagingContainerFactory, inputUriMessagingContainerRegistry, outputUriMessagingContainerRegistry);
		}
	}

	private static void migrateMessagingContainer(String messagingContainerPath, MessagingContainer inputMessagingContainer, MessagingContainer outputMessagingContainer) {

		if (inputMessagingContainer == null || ! (inputMessagingContainer instanceof GraphMessagingContainer)) return;
		if (outputMessagingContainer == null || ! (outputMessagingContainer instanceof GraphMessagingContainer)) return;

		Graph inputGraph = ((GraphMessagingContainer) inputMessagingContainer).getGraph();
		Graph outputGraph = ((GraphMessagingContainer) outputMessagingContainer).getGraph();
		Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();

		CopyUtil.copyGraph(inputGraph, tempGraph, null);

		outputGraph.clear();
		CopyUtil.copyGraph(tempGraph, outputGraph, null);

		System.out.println("At path " + messagingContainerPath + " copied " + inputGraph.getRootContextNode().getAllStatementCount() + " statements from " + inputGraph.getClass().getSimpleName() + " to " + outputGraph.getRootContextNode().getAllStatementCount() + " statements in " + outputGraph.getClass().getSimpleName());

		tempGraph.close();
	}

	private static void migrateMessagingContainerFactory(final String messagingContainerFactoryPath, final UriMessagingContainerFactory inputMessagingContainerFactory, UriMessagingContainerFactory outputMessagingContainerFactory, UriMessagingContainerRegistry inputMessagingContainerRegistry, UriMessagingContainerRegistry outputMessagingContainerRegistry) throws Xdi2TransportException, Xdi2MessagingException {

		if (inputMessagingContainerFactory == null || ! (inputMessagingContainerFactory instanceof RegistryUriMessagingContainerFactory)) return;
		if (outputMessagingContainerFactory == null || ! (outputMessagingContainerFactory instanceof RegistryUriMessagingContainerFactory)) return;

		if (! (((RegistryUriMessagingContainerFactory) inputMessagingContainerFactory).getPrototypeMessagingContainer() instanceof GraphMessagingContainer)) return;
		if (! (((RegistryUriMessagingContainerFactory) outputMessagingContainerFactory).getPrototypeMessagingContainer() instanceof GraphMessagingContainer)) return;

		Iterator<XDIArc> ownerPeerRootXDIArcs = inputMessagingContainerFactory.getOwnerPeerRootXDIArcs();

		Iterator<String> requestPaths = new MappingIterator<XDIArc, String> (ownerPeerRootXDIArcs) {

			@Override
			public String map(XDIArc ownerPeerRootXDIArc) {

				return inputMessagingContainerFactory.getRequestPath(messagingContainerFactoryPath, ownerPeerRootXDIArc);
			}
		};

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingContainerPath = requestPath;

			MessagingContainer inputMessagingContainer = inputMessagingContainerFactory.mountMessagingContainer(inputMessagingContainerRegistry, messagingContainerFactoryPath, requestPath);
			MessagingContainer outputMessagingContainer = outputMessagingContainerFactory.mountMessagingContainer(outputMessagingContainerRegistry, messagingContainerFactoryPath, requestPath);

			migrateMessagingContainer(messagingContainerPath, inputMessagingContainer, outputMessagingContainer);

			inputMessagingContainerRegistry.unmountMessagingContainer(inputMessagingContainer);
			outputMessagingContainerRegistry.unmountMessagingContainer(outputMessagingContainer);
		}

		System.out.println("At path " + messagingContainerFactoryPath + " migrated from " + inputMessagingContainerFactory.getClass().getSimpleName() + " to " + outputMessagingContainerFactory.getClass().getSimpleName());
	}
}
