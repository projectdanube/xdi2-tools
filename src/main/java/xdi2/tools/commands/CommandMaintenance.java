package xdi2.tools.commands;

import java.io.IOException;
import java.net.URLEncoder;

import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.features.nodetypes.XdiLocalRoot;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("maintenance")
@CommandUsage("[path-to-applicationContext.xml]")
@CommandArgs(min=1,max=3)
public class CommandMaintenance extends AbstractGraphsCommand<Object> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String applicationContextPath = commandArgs.length > 0 ? commandArgs[0] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandGraphs(applicationContextPath, null);
	}

	@Override
	protected void callbackGraph(String messagingTargetPath, Graph graph, Object state) throws Xdi2MessagingException, IOException {

		XDI3Segment x = XdiLocalRoot.findLocalRoot(graph).getSelfPeerRoot().getXriOfPeerRoot();

		String uri = "http://mycloud.neustar.biz:8080/myapp/personalclouds/" + URLEncoder.encode(x.toString(), "UTF-8") + "/connect/request";

		graph.setStatement(XDI3Statement.create("$public$do/$get/$https$connect$xdi<$uri>"));
		graph.setStatement(XDI3Statement.create("$public$do/$get/$msg$encrypt$keypair$public<$key>"));
		graph.setStatement(XDI3Statement.create("$public$do/$get/$msg$sig$keypair$public<$key>"));
		Literal literal = graph.getRootContextNode().setDeepLiteral(XDI3Segment.create("$https$connect$xdi<$uri>&"), uri);

		System.out.println(literal.getStatement());
	}
}
