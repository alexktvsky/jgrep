package com.alexktvsky.jgrep;

import java.lang.RuntimeException;
import java.util.Arrays;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.BiConsumer;

public class JGrep {

    private static final String VERSION = "0.1";

    private final String pattern;
    private final String[] filenames;
    private final boolean recursive;
    private final boolean ignoreCase;
    private final boolean showLineNumbers;

    public JGrep(String pattern, String[] filenames, boolean recursive, boolean ignoreCase, boolean showLineNumbers) {
        this.pattern = pattern;
        this.filenames = filenames;
        this.recursive = recursive;
        this.ignoreCase = ignoreCase;
        this.showLineNumbers = showLineNumbers;
    }

    public JGrep(String pattern, boolean ignoreCase, boolean showLineNumbers) {
        this(pattern, null, false, ignoreCase, showLineNumbers);
    }

    private void printLine(String line, int lineNumber) {
        if (showLineNumbers)
            System.out.println("" + ConsoleColors.GREEN + lineNumber + ConsoleColors.RESET + ":" + line);
        else
            System.out.println(line);
    }

    private void printLineWithFilename(String line, int lineNumber, String filename) {
        String coloredFilename = ConsoleColors.MAGENTA + filename + ConsoleColors.RESET;
        if (showLineNumbers)
            System.out.println(coloredFilename + ":" + ConsoleColors.GREEN + lineNumber + ConsoleColors.RESET + ":" + line);
        else
            System.out.println(coloredFilename + ":" + line);
    }

    private Pattern getCompiledPattern(String pattern, boolean caseInsensitive) {
        return caseInsensitive ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : Pattern.compile(pattern);
    }

    private void processLine(String line, int lineNumber, Pattern compiledPattern, BiConsumer<String, Integer> consumer) {
        Matcher matcher = compiledPattern.matcher(line);
        String output = line;
        while (matcher.find()) {
            String foundSubstring = matcher.group();
            String replacement = ConsoleColors.RED_BOLD + foundSubstring + ConsoleColors.RESET;
            output = output.replaceAll(foundSubstring, replacement);
            consumer.accept(output, lineNumber);
        }
    }

    private void processInputUsingReader(LineNumberReader reader, BiConsumer<String, Integer> consumer) throws IOException {
        Pattern compiledPattern = getCompiledPattern(pattern, ignoreCase);
        String input;
        while ((input = reader.readLine()) != null) {
            processLine(input, reader.getLineNumber(), compiledPattern, consumer);
        }
    }

    private void processInputFromStdin() {
        try (LineNumberReader reader =
                new LineNumberReader(new InputStreamReader(System.in))
        ) {
            BiConsumer<String, Integer> consumer = this::printLine;
            processInputUsingReader(reader, consumer);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to process stdio", e);
        }
    }

    private void processFile(String filename) {
        try (LineNumberReader reader =
                new LineNumberReader(new FileReader(filename))
        ) {
            BiConsumer<String, Integer> consumer = (line, lineNumber) -> printLineWithFilename(line, lineNumber, filename);
            processInputUsingReader(reader, consumer);
        }
        catch (FileNotFoundException e) {
            System.err.println(String.format("jgrep: %s: No such file or directory", filename));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read data from file " + filename, e);
        }
    }

    private void processFiles() {
        for (String filename : filenames) {
            processFile(filename);
        }
    }

    private void processDirectory(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file.getAbsolutePath());
            }
            else {
                processFile(file.getAbsolutePath());
            }
        }
    }

    private void processDirectories() {
        for (String filename : filenames) {
            processDirectory(filename);
        }
    }

    public void run() {
        if (filenames == null) {
            processInputFromStdin();
            exit(0);
        }

        if (recursive) {
            processDirectories();
        }
        else {
            processFiles();
        }
    }

    private static void exit(int exitStatus) {
        try {
            System.exit(exitStatus);
        }
        catch (java.lang.SecurityException e) {
            if (exitStatus != 0) {
                throw new RuntimeException("jgrep failed to exit with status " + exitStatus);
            }
        }
    }

    private static void printHelp() {
        System.out.println("Usage: jgrep [OPTION]... PATTERNS [FILE]...");
        System.out.println("Search for PATTERNS in each FILE.");
        System.out.println("Example: jgrep -i 'hello world' text.txt");
    }

    private static void printVersion() {
        System.out.println("jgrep " + VERSION);
    }

    public static void main(String[] args) {

        CommandLine cl = new CommandLine();

        cl.addOption(new CommandLineOption("h", "help", false));
        cl.addOption(new CommandLineOption("v", "version", false));
        cl.addOption(new CommandLineOption("n", "line-number", false));
        cl.addOption(new CommandLineOption("i", "ignore-case", false));
        cl.addOption(new CommandLineOption("r", "recursive", false));
        cl.addOption(new CommandLineOption("g", "regexp", true));

        try {
            cl.parse(args);
        }
        catch (JGrepException e) {
            System.err.println(e);
            printHelp();
            exit(1);
        }

        boolean showHelpCommand = cl.hasOption("h");
        boolean showVersionCommand = cl.hasOption("v");
        boolean showLineNumbers = cl.hasOption("n");
        boolean ignoreCase = cl.hasOption("i");
        boolean recursive = cl.hasOption("r");

        if (showHelpCommand) {
            printHelp();
            exit(0);
        }
        if (showVersionCommand) {
            printVersion();
            exit(0);
        }

        String[] cmdArgs = cl.getArgs();
        if (cmdArgs.length < 1) {
            printHelp();
            exit(1);
        }

        String pattern = cmdArgs[0];
        String[] filenames = Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length);

        JGrep main;
        if (filenames.length == 0)
            main = new JGrep(pattern, ignoreCase, showLineNumbers);
        else
            main = new JGrep(pattern, filenames, recursive, ignoreCase, showLineNumbers);

        main.run();
    }
}
