package xdi2.tools.commands;

import xdi2.messaging.target.factory.impl.uri.UriMessagingTargetFactory;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.transport.registry.impl.uri.UriMessagingTargetRegistry;

@CommandName("list-messaging-target-factorys")
@CommandUsage("[path-to-applicationContext.xml]")
@CommandArgs(min=0,max=1)
public class CommandListMessagingTargetFactorys extends AbstractMessagingTargetFactorysCommand<Object> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String applicationContextPath = commandArgs.length > 0 ? commandArgs[0] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandMessagingTargetFactorys(applicationContextPath, null);
	}

	@Override
	protected void callbackMessagingTargetFactory(String messagingTargetFactoryPath, UriMessagingTargetFactory messagingTargetFactory, UriMessagingTargetRegistry uriMessagingTargetRegistry, Object state) {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingTargetFactoryPath + " ==> " + messagingTargetFactory.getClass().getSimpleName());

		System.out.println(buffer.toString());
	}
}
