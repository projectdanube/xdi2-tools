package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.xri3.XDI3SubSegment;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.transport.exceptions.Xdi2TransportException;
import xdi2.transport.impl.http.factory.MessagingTargetFactory;
import xdi2.transport.impl.http.factory.impl.RegistryGraphMessagingTargetFactory;
import xdi2.transport.impl.http.registry.HttpMessagingTargetRegistry;

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

		HttpMessagingTargetRegistry inputHttpMessagingTargetRegistry = CommandUtil.getHttpMessagingTargetRegistry(inputApplicationContextPath);
		HttpMessagingTargetRegistry outputHttpMessagingTargetRegistry = CommandUtil.getHttpMessagingTargetRegistry(outputApplicationContextPath);
		if (inputHttpMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpMessagingTargetRegistry' not found in " + inputApplicationContextPath);
		if (outputHttpMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpMessagingTargetRegistry' not found in " + outputApplicationContextPath);

		migrateMessagingTargets(inputHttpMessagingTargetRegistry, outputHttpMessagingTargetRegistry);
		migrateMessagingTargetFactorys(inputHttpMessagingTargetRegistry, outputHttpMessagingTargetRegistry);
	}

	private static void migrateMessagingTargets(HttpMessagingTargetRegistry inputHttpMessagingTargetRegistry, HttpMessagingTargetRegistry outputHttpMessagingTargetRegistry) {

		for (String messagingTargetPath : inputHttpMessagingTargetRegistry.getMessagingTargetPaths()) {

			MessagingTarget inputMessagingTarget = inputHttpMessagingTargetRegistry.getMessagingTarget(messagingTargetPath);
			MessagingTarget outputMessagingTarget = outputHttpMessagingTargetRegistry.getMessagingTarget(messagingTargetPath);

			migrateMessagingTarget(messagingTargetPath, inputMessagingTarget, outputMessagingTarget);
		}
	}

	private static void migrateMessagingTargetFactorys(HttpMessagingTargetRegistry inputHttpMessagingTargetRegistry, HttpMessagingTargetRegistry outputHttpMessagingTargetRegistry) throws Xdi2TransportException, Xdi2MessagingException {

		for (String messagingTargetFactoryPath : inputHttpMessagingTargetRegistry.getMessagingTargetFactoryPaths()) {

			MessagingTargetFactory inputMessagingTargetFactory = inputHttpMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			MessagingTargetFactory outputMessagingTargetFactory = outputHttpMessagingTargetRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);

			migrateMessagingTargetFactory(messagingTargetFactoryPath, inputMessagingTargetFactory, outputMessagingTargetFactory, inputHttpMessagingTargetRegistry, outputHttpMessagingTargetRegistry);
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

	private static void migrateMessagingTargetFactory(final String messagingTargetFactoryPath, final MessagingTargetFactory inputMessagingTargetFactory, MessagingTargetFactory outputMessagingTargetFactory, HttpMessagingTargetRegistry inputHttpMessagingTargetRegistry, HttpMessagingTargetRegistry outputHttpMessagingTargetRegistry) throws Xdi2TransportException, Xdi2MessagingException {

		if (inputMessagingTargetFactory == null || ! (inputMessagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) return;
		if (outputMessagingTargetFactory == null || ! (outputMessagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) return;

		if (! (((RegistryGraphMessagingTargetFactory) inputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) return;
		if (! (((RegistryGraphMessagingTargetFactory) outputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) return;

		Iterator<XDI3SubSegment> ownerPeerRootXris = inputMessagingTargetFactory.getOwnerPeerRootXris();

		Iterator<String> requestPaths = new MappingIterator<XDI3SubSegment, String> (ownerPeerRootXris) {

			@Override
			public String map(XDI3SubSegment ownerPeerRootXri) {

				return inputMessagingTargetFactory.getRequestPath(messagingTargetFactoryPath, ownerPeerRootXri);
			}
		};

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingTargetPath = requestPath;

			MessagingTarget inputMessagingTarget = inputMessagingTargetFactory.mountMessagingTarget(inputHttpMessagingTargetRegistry, messagingTargetFactoryPath, requestPath);
			MessagingTarget outputMessagingTarget = outputMessagingTargetFactory.mountMessagingTarget(outputHttpMessagingTargetRegistry, messagingTargetFactoryPath, requestPath);

			migrateMessagingTarget(messagingTargetPath, inputMessagingTarget, outputMessagingTarget);

			inputHttpMessagingTargetRegistry.unmountMessagingTarget(inputMessagingTarget);
			outputHttpMessagingTargetRegistry.unmountMessagingTarget(outputMessagingTarget);
		}

		System.out.println("At path " + messagingTargetFactoryPath + " migrated from " + inputMessagingTargetFactory.getClass().getSimpleName() + " to " + outputMessagingTargetFactory.getClass().getSimpleName());
	}
}
