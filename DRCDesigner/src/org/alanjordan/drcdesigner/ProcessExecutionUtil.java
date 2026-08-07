package org.alanjordan.drcdesigner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Logger;

public final class ProcessExecutionUtil {
    private ProcessExecutionUtil() {
    }

    public static int runCommandAndCaptureOutput(ProcessBuilder processBuilder, File outputFile, String operationName)
            throws IOException, InterruptedException {
        return runCommandAndCaptureOutput(processBuilder, outputFile, operationName, false);
    }

    public static int runCommandAndCaptureOutput(ProcessBuilder processBuilder, File outputFile, String operationName, boolean append)
            throws IOException, InterruptedException {
        Logger logger = AppLogger.getLogger();

        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        processBuilder.redirectErrorStream(true);
        logger.info(operationName + " command: " + processBuilder.command());

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
               PrintWriter outputWriter = new PrintWriter(new FileWriter(outputFile, append))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputWriter.println(line);
                logger.info(operationName + ": " + line);
            }
        }

        int exitCode = process.waitFor();
        logger.info(operationName + " exit code: " + exitCode);
        return exitCode;
    }
}
