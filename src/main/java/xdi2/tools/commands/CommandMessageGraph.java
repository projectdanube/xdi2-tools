package xdi2.tools.commands;

import java.io.IOException;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.impl.local.XDILocalClient;
import xdi2.core.Graph;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.syntax.XDIAddress;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("message-graph")
@CommandUsage("request-path operation target [mime-type] [path-to-applicationContext.xml]")
@CommandArgs(min=3,max=5)
public class CommandMessageGraph extends AbstractGraphCommand<CommandMessageGraph.MyState> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String requestPath = commandArgs[0];
		String operation = commandArgs[1];
		String target = commandArgs[2];
		String mimeType = commandArgs.length > 3 ? commandArgs[3] : null;
		String applicationContextPath = commandArgs.length > 4 ? commandArgs[4] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandGraph(applicationContextPath, requestPath, new MyState(mimeType, operation, target));
	}

	@Override
	protected void callbackGraph(String messagingContainerPath, Graph graph, MyState state) throws Xdi2ClientException, IOException {

		GraphMessagingContainer commandGraphMessagingContainer = new GraphMessagingContainer();
		commandGraphMessagingContainer.setGraph(graph);

		MessageEnvelope commandMessageEnvelope = MessageEnvelope.fromOperationXDIAddressAndTargetXDIAddressOrTargetXDIStatement(XDIAddress.create(state.operation), state.target);
		MessagingResponse commandMessagingResponse;

		commandMessagingResponse = new XDILocalClient(commandGraphMessagingContainer).send(commandMessageEnvelope);

		XDIWriter writer = state.mimeType == null ? XDIWriterRegistry.getDefault() : XDIWriterRegistry.forMimeType(new MimeType(state.mimeType));
		writer.write(commandMessagingResponse.getResultGraph(), System.out);

		System.out.println("At path " + messagingContainerPath + " executed message on graph " + graph.getClass().getSimpleName());
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
