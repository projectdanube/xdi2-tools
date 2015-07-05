package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.factory.impl.uri.RegistryUriMessagingTargetFactory;
import xdi2.messaging.target.factory.impl.uri.UriMessagingTargetFactory;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.transport.exceptions.Xdi2TransportException;
import xdi2.transport.registry.impl.uri.UriMessagingTargetRegistry;

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

		UriMessagingTargetRegistry inputUriMessagingTargetRegistry = CommandUtil.getUriMessagingTargetRegistry(inputApplicationContextPath);
		UriMessagingTargetRegistry outputUriMessagingTargetRegistry = CommandUtil.getUriMessagingTargetRegistry(outputApplicationContextPath);
		if (inputUriMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingTargetRegistry' not found in " + inputApplicationContextPath);
		if (outputUriMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'UriMessagingTargetRegistry' not found in " + outputApplicationContextPath);

		migrateMessagingTargets(inputUriMessagingTargetRegistry, outputUriMessagingTargetRegistry);
		migrateMessagingTargetFactorys(inputUriMessagingTargetRegistry, outputUriMessagingTargetRegistry);
	}

	private static void migrateMessagingTargets(UriMessagingTargetRegistry inputUriMessagingTargetRegistry, UriMessagingTargetRegistry outputUriMessagingTargetRegistry) {

		for (String messagingTargetPath : inputUriMessagingTargetRegistry.getMessagingTargetPaths()) {

			MessagingTarget inputMessagingTarget = inputUriMessagingTargetRegistry.getMessagingTarget(messagingTargetPath);
			MessagingTarget outputMessagingTarget = outputUriMessagingTargetRegistry.getMessagingTarget(messagingTargetPath);

			migrateMessagingTarget(messagingTargetPath, inputMessagingTarget, outputMessagingTarget);
		}
	}

	private static void migrateMessagingTargetFactorys(UriMessagingTargetRegistry inputUriMessagingTargetRegistry, UriMessagingTargetRegistry outputUriMessagingTargetRegistry) throws Xdi2TransportException, Xdi2MessagingException {

		for (String messagingTargetFactoryPath : inputUriMessagingTargetRegistry.getMessagingTargetFactoryPaths()) {

			UriMessagingTargetFactory inputMessagingTargetFactory = inputUriMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			UriMessagingTargetFactory outputMessagingTargetFactory = outputUriMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);

			migrateMessagingTargetFactory(messagingTargetFactoryPath, inputMessagingTargetFactory, outputMessagingTargetFactory, inputUriMessagingTargetRegistry, outputUriMessagingTargetRegistry);
		}
	}

	private static void migrateMessagingTarget(String messagingTargetPath, MessagingTarget inputMessagingTarget, MessagingTarget outputMessagingTarget) {

		if (inputMessagingTarget == null || ! (inputMessagingTarget instanceof GraphMessagingTarget)) return;
		if (outputMessagingTarget == null || ! (outputMessagingTarget instanceof GraphMessagingTarget)) return;

		Graph inputGraph = ((GraphMessagingTarget) inputMessagingTarget).getGraph();
		Graph outputGraph = ((GraphMessagingTarget) outputMessagingTarget).getGraph();
		Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();

		CopyUtil.copyGraph(inputGraph, tempGraph, null);

		outputGraph.clear();
		CopyUtil.copyGraph(tempGraph, outputGraph, null);

		System.out.println("At path " + messagingTargetPath + " copied " + inputGraph.getRootContextNode().getAllStatementCount() + " statements from " + inputGraph.getClass().getSimpleName() + " to " + outputGraph.getRootContextNode().getAllStatementCount() + " statements in " + outputGraph.getClass().getSimpleName());

		tempGraph.close();
	}

	private static void migrateMessagingTargetFactory(final String messagingTargetFactoryPath, final UriMessagingTargetFactory inputMessagingTargetFactory, UriMessagingTargetFactory outputMessagingTargetFactory, UriMessagingTargetRegistry inputMessagingTargetRegistry, UriMessagingTargetRegistry outputMessagingTargetRegistry) throws Xdi2TransportException, Xdi2MessagingException {

		if (inputMessagingTargetFactory == null || ! (inputMessagingTargetFactory instanceof RegistryUriMessagingTargetFactory)) return;
		if (outputMessagingTargetFactory == null || ! (outputMessagingTargetFactory instanceof RegistryUriMessagingTargetFactory)) return;

		if (! (((RegistryUriMessagingTargetFactory) inputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) return;
		if (! (((RegistryUriMessagingTargetFactory) outputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) return;

		Iterator<XDIArc> ownerPeerRootXDIArcs = inputMessagingTargetFactory.getOwnerPeerRootXDIArcs();

		Iterator<String> requestPaths = new MappingIterator<XDIArc, String> (ownerPeerRootXDIArcs) {

			@Override
			public String map(XDIArc ownerPeerRootXDIArc) {

				return inputMessagingTargetFactory.getRequestPath(messagingTargetFactoryPath, ownerPeerRootXDIArc);
			}
		};

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingTargetPath = requestPath;

			MessagingTarget inputMessagingTarget = inputMessagingTargetFactory.mountMessagingTarget(inputMessagingTargetRegistry, messagingTargetFactoryPath, requestPath);
			MessagingTarget outputMessagingTarget = outputMessagingTargetFactory.mountMessagingTarget(outputMessagingTargetRegistry, messagingTargetFactoryPath, requestPath);

			migrateMessagingTarget(messagingTargetPath, inputMessagingTarget, outputMessagingTarget);

			inputMessagingTargetRegistry.unmountMessagingTarget(inputMessagingTarget);
			outputMessagingTargetRegistry.unmountMessagingTarget(outputMessagingTarget);
		}

		System.out.println("At path " + messagingTargetFactoryPath + " migrated from " + inputMessagingTargetFactory.getClass().getSimpleName() + " to " + outputMessagingTargetFactory.getClass().getSimpleName());
	}
}
