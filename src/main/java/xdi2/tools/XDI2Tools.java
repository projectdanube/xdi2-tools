package xdi2.tools;

import java.util.Arrays;

import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.tools.commands.Command;
import xdi2.tools.commands.CommandGenerateDigestSecret;
import xdi2.tools.commands.CommandMigrateGraphs;

public class XDI2Tools {

	private final static Command[] commands = new Command[] {
		new CommandGenerateDigestSecret(),
		new CommandMigrateGraphs()
	};

	public static void main(String... args) throws Exception {

		if (args.length < 1) {

			commandUsage(null);
			return;
		}

		String commandName = args[0];
		Command command = findCommand(commandName);

		if (command == null) {

			printUsage(null);
			return;
		}

		String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

		if (commandArgs.length < commandMinArgs(command) || commandArgs.length > commandMaxArgs(command)) {

			printUsage(command);
		}
		
		command.execute(commandArgs);
	}

	private static Command findCommand(String commandName) {

		for (Command command : commands) {

			if (commandName.equals(commandName(command))) return command;
		}

		return null;
	}

	private static void printUsage(Command command) {

		System.out.println("Usage: java -jar xdi2-tools-XXX.one-jar.jar");

		if (command == null) {
			for (Command c : commands) {

				System.out.println("   " + c.getClass().getAnnotation(CommandName.class).value() + " " + c.getClass().getAnnotation(CommandUsage.class).value());
			}
		} else {

			System.out.println("   " + command.getClass().getAnnotation(CommandName.class).value() + " " + command.getClass().getAnnotation(CommandUsage.class).value());
		}
	}

	private static String commandName(Command command) {

		return command.getClass().getAnnotation(CommandName.class).value();
	}

	private static String commandUsage(Command command) {

		return command.getClass().getAnnotation(CommandUsage.class).value();
	}

	private static int commandMinArgs(Command command) {

		return command.getClass().getAnnotation(CommandArgs.class).min();
	}

	private static int commandMaxArgs(Command command) {

		return command.getClass().getAnnotation(CommandArgs.class).max();
	}
}
