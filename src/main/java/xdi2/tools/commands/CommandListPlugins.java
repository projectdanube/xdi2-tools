package xdi2.tools.commands;

import java.io.File;

import xdi2.core.plugins.PluginsLoader;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("list-plugins")
@CommandUsage("")
@CommandArgs(min=0,max=0)
public class CommandListPlugins implements Command {

	@Override
	public void execute(String[] commandArgs) throws Exception {

		File[] files = PluginsLoader.getFiles();

		if (files == null) {

			System.out.println("No plugins.");
			return;
		}

		for (File file : files) {

			System.out.println(file.getAbsolutePath());
		}
	}
}
