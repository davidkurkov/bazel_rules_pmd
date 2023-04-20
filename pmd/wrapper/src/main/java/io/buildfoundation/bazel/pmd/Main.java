package io.buildfoundation.bazel.pmd;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.renderers.TextColorRenderer;
import net.sourceforge.pmd.renderers.TextPadRenderer;
import net.sourceforge.pmd.renderers.TextRenderer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class Main {

    public static void main(String[] args) {
        List<String> arguments = Arrays.asList(args);
        String executionResultOutputPath = getTestResultOutputPath(arguments);

        String[] pmdArguments = sanitizePmdArguments(arguments);

        PMD.StatusCode result = PMD.runPmd(pmdArguments);

        if (!result.equals(PMD.StatusCode.OK)) {
            printError(arguments);
        }

        writeExecutionResultToFile(result, executionResultOutputPath);

        System.exit(0);
    }

    private static void printError(List<String> arguments) {

        String reportFormat = argument(arguments, "--format");
        String reportFilePath = argument(arguments, "--report-file");

        if (reportFormat != null && reportFilePath != null) {
            if (Arrays.asList(TextRenderer.NAME, TextColorRenderer.NAME, TextPadRenderer.NAME).contains(reportFormat)) {
                printFile(reportFilePath);
            }
        }
    }

    private static void writeExecutionResultToFile(PMD.StatusCode statusCode, String executionResultOutputPath) {
        String os = System.getProperty("os.name").toLowerCase();
        String content;

        if (os.contains("win")) {
            content = String.format("exit /b %d", statusCode.toInt());
        } else {
            content = String.format("#!/bin/bash\n\nexit %d", statusCode.toInt());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(executionResultOutputPath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTestResultOutputPath(List<String> arguments) {
        String outputPath = argument(arguments, "--test-result");
        if (outputPath == null) {
            System.exit(1);
        }
        return outputPath;
    }

    private static String[] sanitizePmdArguments(List<String> arguments) {
        Set<String> excludeArgs = new HashSet<>(new ArrayList<>(Collections.singletonList("--execution-result")));
        return filterOutArgValuePairs(arguments, excludeArgs);
    }

    public static String[] filterOutArgValuePairs(List<String> args, Set<String> excludeArgs) {
        List<String> filteredList = new ArrayList<>();

        int index = 0;

        while (index < args.size()) {
            String value = args.get(index);
            if (!excludeArgs.contains(value)) {
                filteredList.add(value);
            } else {
                // skip the arg-value pair since matching argument was found
                index += 1;
            }
            index += 1;
        }

        return filteredList.toArray(new String[0]);
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
