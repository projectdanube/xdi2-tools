package xdi2.tools.commands;

import java.io.IOException;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.xri3.XDI3Segment;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.server.registry.HttpMessagingTargetRegistry;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("message-graphs")
@CommandUsage("operation target [mime-type] [path-to-applicationContext.xml]")
@CommandArgs(min=2,max=4)
public class CommandMessageGraphs extends AbstractGraphsCommand<CommandMessageGraphs.MyState> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String operation = commandArgs[0];
		String target = commandArgs[1];
		String mimeType = commandArgs.length > 2 ? commandArgs[2] : null;
		String applicationContextPath = commandArgs.length > 3 ? commandArgs[3] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		HttpMessagingTargetRegistry httpMessagingTargetRegistry = CommandUtil.getHttpMessagingTargetRegistry(applicationContextPath);
		if (httpMessagingTargetRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpMessagingTargetRegistry' not found in " + applicationContextPath);

		this.commandGraphs(applicationContextPath, new MyState(mimeType, operation, target));
	}

	@Override
	protected void callbackGraph(String messagingTargetPath, Graph graph, MyState state) throws Xdi2MessagingException, IOException {

		GraphMessagingTarget commandGraphMessagingTarget = new GraphMessagingTarget();
		commandGraphMessagingTarget.setGraph(graph);

		MessageEnvelope commandMessageEnvelope = MessageEnvelope.fromOperationXriAndTargetAddressOrTargetStatement(XDI3Segment.create(state.operation), state.target);
		MessageResult commandMessageResult = new MessageResult();

		commandGraphMessagingTarget.execute(commandMessageEnvelope, commandMessageResult, null);

		XDIWriter writer = state.mimeType == null ? XDIWriterRegistry.getDefault() : XDIWriterRegistry.forMimeType(new MimeType(state.mimeType));

		writer.write(commandMessageResult.getGraph(), System.out);

		System.out.println("At path " + messagingTargetPath + " executed message on graph " + graph.getClass().getSimpleName());
	}

	public class MyState {

		private String mimeType;
		private String operation;
		private String target;

		public MyState(String mimeType, String operation, String target) {

			this.mimeType = mimeType;
			this.operation = operation;
			this.target = target;
		}
	}
}
