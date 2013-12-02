package xdi2.tools.commands;

import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.registry.HttpMessagingTargetRegistry;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("list-messaging-targets")
@CommandUsage("[path-to-applicationContext.xml]")
@CommandArgs(min=0,max=1)
public class CommandListMessagingTargets extends AbstractMessagingTargetsCommand<Object> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String applicationContextPath = commandArgs.length > 0 ? commandArgs[0] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandMessagingTargets(applicationContextPath, null);
	}

	protected void callbackMessagingTarget(String messagingTargetPath, MessagingTarget messagingTarget, HttpMessagingTargetRegistry httpMessagingTargetRegistry, Object state) {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingTargetPath + " --> " + messagingTarget.getClass().getSimpleName());

		if (messagingTarget instanceof GraphMessagingTarget) {

			buffer.append(" (" + ((GraphMessagingTarget) messagingTarget).getGraph().getClass().getSimpleName() + ")");
		}

		System.out.println(buffer.toString());
	}

	protected void callbackMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpMessagingTargetRegistry httpMessagingTargetRegistry, Object state) throws Exception {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingTargetFactoryPath + " ==> " + messagingTargetFactory.getClass().getSimpleName());

		System.out.println(buffer.toString());

		super.callbackMessagingTargetFactory(messagingTargetFactoryPath, messagingTargetFactory, httpMessagingTargetRegistry, state);
	}
}
