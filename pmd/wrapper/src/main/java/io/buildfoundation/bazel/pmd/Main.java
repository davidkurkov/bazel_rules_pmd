package io.buildfoundation.bazel.pmd;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.renderers.TextColorRenderer;
import net.sourceforge.pmd.renderers.TextPadRenderer;
import net.sourceforge.pmd.renderers.TextRenderer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Main {

    public static void main(String[] args) {
        ArrayList<String> newArguments = new ArrayList<>();
        boolean shouldRunAsTestTarget = false;
        String outputPath = "";
        boolean foundExitFile = false;

        for (String arg : args) {
            if ("--run_as_test_target".equals(arg)) {
                shouldRunAsTestTarget = true;
            } else if ("--test_target_report".startsWith(arg)) {
                foundExitFile = true;
            } else {
                if (foundExitFile) {
                    outputPath = arg;
                    foundExitFile = false;
                } else {
                    newArguments.add(arg);
                }
            }
        }

        System.out.printf(Arrays.toString(newArguments.toArray(new String[0])));

        PMD.StatusCode result = PMD.runPmd(newArguments.toArray(new String[0]));

        if (!result.equals(PMD.StatusCode.OK)) {
            printError(args);
        }

        String content = String.format("#!/bin/bash\n\nexit %d\n", result.toInt());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (shouldRunAsTestTarget) {
            System.exit(0);
        } else {
            System.exit(result.toInt());
        }
    }

    private static void printError(String[] args) {
        List<String> arguments = Arrays.asList(args);

        String reportFormat = argument(arguments, "-format");
        String reportFilePath = argument(arguments, "-reportfile");

        if (reportFormat != null && reportFilePath != null) {
            if (Arrays.asList(TextRenderer.NAME, TextColorRenderer.NAME, TextPadRenderer.NAME).contains(reportFormat)) {
                printFile(reportFilePath);
            }
        }
    }

    private static String argument(List<String> arguments, String name) {
        try {
            return arguments.get(arguments.indexOf(name) + 1);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    private static void printFile(String filePath) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filePath))) {
            fileReader.lines().forEach(System.err::println);
        } catch (IOException ignored) {
        }
    }
}
