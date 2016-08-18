package me.olivervscreeper.networkutilities.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Getter;
import me.olivervscreeper.networkutilities.messages.Message;

/**
 * Created on 28/11/2014. Template CommandManager. Handles the loading and monitoring of commands
 * and executes methods with permission checks where necessary.
 *
 * @author Gonther Jungbluth, OliverVsCreeper
 */
public class CommandManager implements Listener {

    public String permissionMessage;
    public String errorMessage;
    private ConcurrentMap<Object, ConcurrentHashMap<String, ArrayList<MethodPair>>> commands;
    private ConcurrentMap<String, String> aliases;
    private ConcurrentMap<String, ArrayList<MethodPair>> commandsOrderedByPrioirity;
    private Plugin plugin;

    public CommandManager(Plugin plugin) {
        this(plugin, ChatColor.RED + "It seems that you can't do this right now!", ChatColor.RED + "BOOM! It seems that command didn't work.");
    }

    public CommandManager(Plugin plugin, String permissionMessage, String errorMessage) {
        this.plugin = plugin;
        commands = new ConcurrentHashMap<Object, ConcurrentHashMap<String, ArrayList<MethodPair>>>();
        aliases = new ConcurrentHashMap<String, String>();
        loadCommandsByPriority();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.permissionMessage = ChatColor.translateAlternateColorCodes('&', permissionMessage);
        this.errorMessage = ChatColor.translateAlternateColorCodes('&', errorMessage);
    }

    public Set<String> getRegisteredCommand() {
        return commandsOrderedByPrioirity.keySet();
    }

    public void addAlias(String alias, String command) {
        aliases.put(alias, command);
        registerCommandIntoBukkit(alias);
    }

    public void removeAlias(String alias, String command) {
        aliases.remove(alias);
    }

    private void loadCommandsByPriority() {
        commandsOrderedByPrioirity = new ConcurrentHashMap<String, ArrayList<MethodPair>>();
        Iterator<Entry<Object, ConcurrentHashMap<String, ArrayList<MethodPair>>>> ite = commands.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<Object, ConcurrentHashMap<String, ArrayList<MethodPair>>> e = ite.next();
            Iterator<Entry<String, ArrayList<MethodPair>>> ite1 = e.getValue().entrySet().iterator();
            while (ite1.hasNext()) {
                Entry<String, ArrayList<MethodPair>> e1 = ite1.next();
                String cmdName = e1.getKey();
                ArrayList<MethodPair> array = e1.getValue();
                ArrayList<MethodPair> array1 = commandsOrderedByPrioirity.get(cmdName);
                if (array1 == null) {
                    array1 = new ArrayList<CommandManager.MethodPair>();
                    commandsOrderedByPrioirity.put(cmdName, array1);
                }
                array1.addAll(array);
            }
        }
        Iterator<Entry<String, ArrayList<MethodPair>>> ite1 = commandsOrderedByPrioirity.entrySet().iterator();
        while (ite1.hasNext()) {
            Entry<String, ArrayList<MethodPair>> e = ite1.next();
            ArrayList<MethodPair> methods = e.getValue();
            Collections.sort(methods, new Comparator<MethodPair>() {
                public int compare(MethodPair arg0, MethodPair arg1) {
                    return -Integer.compare(arg0.getPriority(), arg1.getPriority());
                }
            });
        }
    }

    /**
     * Removes the object from the command handler
     *
     * @author scipio3000
     */
    public void unregisterCommands(Object object) {
        commands.remove(object);
        loadCommandsByPriority();
    }

    public void registerCommands(Object object) {
        registerCommands(object, true);
    }

    /**
     * Register a new command object
     *
     * @param object The object
     * @author scipio3000
     */
    public void registerCommands(Object object, boolean registerIntoBukkit) {
        for (Method method : object.getClass().getDeclaredMethods()) {//Declared output private methods too
            if (method.getAnnotation(Command.class) == null) continue;
            Command command = ((Command) method.getAnnotation(Command.class));
            String commandName = command.label();
            if (commandName == null) continue;
            commandName = commandName.toLowerCase();
            ConcurrentHashMap<String, ArrayList<MethodPair>> methodList = commands.get(object);
            if (methodList == null) {
                methodList = new ConcurrentHashMap<String, ArrayList<MethodPair>>();
                commands.put(object, methodList);
            }
            ArrayList<MethodPair> methods = methodList.get(commandName);
            if (methods == null) {
                methods = new ArrayList<MethodPair>();
                methodList.put(commandName, methods);
            }
            String permission = command.permission();
            int priority = command.priority();
            if (registerIntoBukkit)
                registerCommand(commandName);//It's alright if the command already exist.
            methods.add(new MethodPair(method, object, permission, priority));
        }
        loadCommandsByPriority();
    }

    /**
     * Register a command into bukkit
     *
     * @param name The name of the command
     * @author scipio3000
     */
    private void registerCommandIntoBukkit(String name) {
        PluginCommand command = getCommand(name);
        SimpleCommandMap map = (SimpleCommandMap) getCommandMap();
        map.register(plugin.getDescription().getName(), command);
    }

    /**
     * Register a command on bukkit
     *
     * @author scipio3000
     */
    public void registerCommand(String name) {
        PluginCommand command = getCommand(name);
        SimpleCommandMap map = (SimpleCommandMap) getCommandMap();
        map.register(plugin.getDescription().getName(), command);
    }

    /**
     * Gets the command map.
     *
     * @return the command map
     * @author scipio3000
     */
    private CommandMap getCommandMap() {
        CommandMap commandMap = null;
        try {
            if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commandMap;
    }

    /**
     * Gets a bukkit command object
     *
     * @param name the name
     * @return the command
     * @author scipio3000
     */
    private PluginCommand getCommand(String name) {
        PluginCommand command = null;
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            command = c.newInstance(name, plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return command;
    }

    /**
     * Parses an executed command and handles it. If the command is registered, it will be
     * triggered. Else, the event will be ignored and the command left to run.
     *
     * @param event the event that has been triggered by the command
     */
    @EventHandler
    public void onCommandPre(PlayerCommandPreprocessEvent event) {
        List<String> messageArgs = new ArrayList(Arrays.asList(event.getMessage().split(" ")));
        String command = messageArgs.iterator().next();
        messageArgs.remove(command);
        Boolean success = parseCommand(event.getPlayer(), command.replace("/", ""), messageArgs);
        if (success) event.setCancelled(true);
    }

    /**
     * Checks an executed command against all stored commands. If the command is registered, it will
     * be executed.
     *
     * @param player  player running command (for permissions)
     * @param command name of the command being run
     * @param args    arguments used by the executor
     * @return boolean If a command as executed.
     */
    public Boolean parseCommand(Player player, String command, List<String> args) {
        command = command.toLowerCase();
        ArrayList<MethodPair> methods = commandsOrderedByPrioirity.get(command);
        boolean found = false, good = false;
        if (methods == null) {
            if (aliases.containsValue(command))
                methods = commandsOrderedByPrioirity.get(aliases.get(command));
        }
        if (methods != null) {
            for (MethodPair pair : methods) {
                if (!player.hasPermission(pair.permission) && !pair.permission.equalsIgnoreCase("none"))
                    continue;
                good = true;
                try {
                    pair.method.invoke(pair.getObject(), player, args);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Message(Message.INFO).addRecipient(player).send(errorMessage);
                }
            }
            found = true;
        }
        if (found) {
            if (!good) {
                new Message(Message.INFO).addRecipient(player).send(permissionMessage);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Store a method with its permission and priority
     *
     * @author scipio3000
     */
    @Getter
    private class MethodPair {

        private Method method;
        private Object object;
        private String permission;
        private int priority;

        public MethodPair(Method method, Object object, String permission, int priority) {
            this.method = method;
            this.object = object;
            this.permission = permission;
            this.priority = priority;
        }

    }

}
