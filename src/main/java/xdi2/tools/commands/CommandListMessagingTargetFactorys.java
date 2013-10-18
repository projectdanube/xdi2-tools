package xdi2.tools.commands;

import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.server.exceptions.Xdi2ServerException;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.registry.HttpEndpointRegistry;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

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

	protected void callbackMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory messagingTargetFactory, HttpEndpointRegistry httpEndpointRegistry, Object state) throws Xdi2ServerException, Xdi2MessagingException {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingTargetFactoryPath + " --> " + messagingTargetFactory.getClass().getSimpleName());

		System.out.println(buffer.toString());
	}
}
