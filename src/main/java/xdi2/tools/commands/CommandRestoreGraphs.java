package xdi2.tools.commands;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import xdi2.core.Graph;
import xdi2.core.io.XDIReader;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("restore-graphs")
@CommandUsage("filename [path-to-applicationContext.xml]")
@CommandArgs(min=1,max=2)
public class CommandRestoreGraphs extends AbstractGraphsCommand<CommandRestoreGraphs.MyState> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String filename = commandArgs[0];
		String applicationContextPath = commandArgs.length > 1 ? commandArgs[1] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		ZipFile zipFile = new ZipFile(filename);

		this.commandGraphs(applicationContextPath, new MyState(zipFile));

		zipFile.close();
	}

	protected void callbackGraph(String messagingTargetPath, Graph graph, MyState state) throws Exception {

		String zipEntryName = messagingTargetPath + ".xdi";
		if (zipEntryName.startsWith("/")) zipEntryName = zipEntryName.substring(1);

		ZipEntry zipEntry = state.zipFile.getEntry(zipEntryName);

		if (zipEntry == null) {

			System.out.println("Not restoring graph " + messagingTargetPath + ": Not found in ZIP file.");
			return;
		} else {

			System.out.println("Restoring graph " + messagingTargetPath + ".");
		}

		InputStream zipInputStream = state.zipFile.getInputStream(zipEntry);

		XDIReader reader = XDIReaderRegistry.getAuto();

		graph.clear();
		reader.read(graph, zipInputStream);

		zipInputStream.close();
	}

	public class MyState {

		private ZipFile zipFile;

		public MyState(ZipFile zipFile) {

			this.zipFile = zipFile;
		}
	}
}
