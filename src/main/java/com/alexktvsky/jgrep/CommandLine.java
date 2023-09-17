package com.alexktvsky.jgrep;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CommandLine {

    private final List<CommandLineOption> options;
    private final Map<String, String> parsedOptions;
    private final List<String> parsedArguments;

    public CommandLine() {
        options = new ArrayList<>();
        parsedOptions = new HashMap<>();
        parsedArguments = new ArrayList<>();
    }

    public String getOptionValue(String opt) {
        return parsedOptions.get(opt);
    }

    public boolean hasOption(String opt) {
        return parsedOptions.get(opt) != null;
    }

    public String[] getArgs() {
        return parsedArguments.toArray(new String[0]);
    }

    public void addOption(CommandLineOption option) {
        options.add(option);
    }

    /**
     * Parses a command line that may contain one or more flags
     * before an optional command string.
     * @param args command line arguments
     * @return true if parsing succeeded, false otherwise.
     */
    public void parse(String[] args) throws JGrepException {
        List<String> argList = Arrays.asList(args);
        Iterator<String> it = argList.iterator();

        while (it.hasNext()) {
            String opt = it.next();
            boolean found = false;

            if (!opt.startsWith("-")) {
                parsedArguments.add(opt);
                continue;
            }

            try {
                for (CommandLineOption option : options) {
                    if (opt.equals("-" + option.getOpt()) || opt.equals("--" + option.getLongOpt())) {
                        if (option.hasArg()) {
                            parsedOptions.put(option.getOpt(), it.next());
                        }
                        else {
                            parsedOptions.put(option.getOpt(), "true");
                        }

                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new JGrepException("Unknown argument " + opt);
                }
            }
            catch (NoSuchElementException e) {
                throw new JGrepException("No argument found for option " + opt);
            }
        }
    }
}
