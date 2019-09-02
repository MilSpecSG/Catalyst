package modules.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import modules.PluginMessages;
import modules.PluginPermissions;
import modules.google.MSGoogle;

import javax.annotation.Nonnull;

public class GoogleCommand implements Command {

    @Override
    public void execute(@Nonnull CommandSource source, String[] args)
    {
        if(source.hasPermission(PluginPermissions.GOOGLE))
        {
            if(args.length > 0)
            {
                source.sendMessage(MSGoogle.googleLink(args));
            }
        }else{
           source.sendMessage(PluginMessages.noPermissions);
        }
    }
}
