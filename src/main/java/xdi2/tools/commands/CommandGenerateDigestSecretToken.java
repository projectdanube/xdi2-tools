package xdi2.tools.commands;

import xdi2.core.features.secrettokens.SecretTokens;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("generate-digest-secret-token")
@CommandUsage("secretToken globalSalt [localSalt]")
@CommandArgs(min=2,max=3)
public class CommandGenerateDigestSecretToken implements Command {

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String secretToken = commandArgs[0];
		String globalSalt = commandArgs[1];
		String localSalt = commandArgs.length > 2 ? commandArgs[2] : null;

		String localSaltAndDigestSecretToken;

		if (localSalt == null)
			localSaltAndDigestSecretToken = SecretTokens.localSaltAndDigestSecretToken(secretToken, globalSalt);
		else
			localSaltAndDigestSecretToken = SecretTokens.localSaltAndDigestSecretToken(secretToken, globalSalt, localSalt);

		System.out.println(localSaltAndDigestSecretToken);
	}
}
