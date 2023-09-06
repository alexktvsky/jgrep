package org.jgrep;

public class CommandLineOption {

    private final String opt;
    private final String longOpt;
    private final boolean hasArg;

    public CommandLineOption(String opt, String longOpt, boolean hasArg) {
        this.opt = opt;
        this.longOpt = longOpt;
        this.hasArg = hasArg;
    }

    public CommandLineOption(String opt, boolean hasArg) {
        this(opt, null, hasArg);
    }

    public String getOpt() {
        return opt;
    }

    public String getLongOpt() {
        return longOpt;
    }

    public boolean hasArg() {
        return hasArg;
    }

    @Override
    public String toString() {
        return "CommandLineOption [opt=" + opt + ", longOpt=" + longOpt + ", hasArg=" + hasArg + "]";
    }
}
