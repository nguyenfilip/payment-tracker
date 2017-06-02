package net.bytemix;

import net.bytemix.domain.MoneyAmount;
import net.bytemix.services.MoneyService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class PaymentTracker
{
    private static Logger logger = LoggerFactory.getLogger(PaymentTracker.class);
    private static MoneyService moneyService = MoneyService.getInstance();
    private static Options options;

    public static void main( String[] args ) throws ParseException {
        options = prepareCmdOptions();

        CommandLine commandLine = null;
        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (Exception ex) {
            logger.error("Error parsing arguments ", ex);
            printHelp();
            return;
        }

        if (commandLine.hasOption("h")) {
            printHelp();
            return;
        }

        if (commandLine.hasOption("f")) {
            String filePath = commandLine.getOptionValue("f");
            Path file = FileSystems.getDefault().getPath(filePath);

            if (!Files.exists(file)) {
                System.err.println("File doesn't exist");
                logger.error("User supplied non-existing file: "+ filePath);
                System.exit(1);
            }

            moneyService.loadFromFile(file);
        }
        if (commandLine.hasOption("e")) {
            String filePath = commandLine.getOptionValue("e");
            Path file = FileSystems.getDefault().getPath(filePath);

            if (!Files.exists(file)) {
                System.err.println("File doesn't exist");
                logger.error("User supplied non-existing file: "+ filePath);
                System.exit(1);
            }

            moneyService.loadExchangeRatesFromFile(file);
        }

        Scanner scanner = new Scanner(System.in);

        long secondsBetweenPrintouts = 60;

        if (commandLine.hasOption("t")) {
            String timeoutLine = commandLine.getOptionValue("t");
            try {
                secondsBetweenPrintouts = Long.parseLong(timeoutLine);
            } catch (Exception ex) {
                logger.warn("Error parsing 'timeout' argument", ex);
            }
        }
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new PeriodicMoneyPrinter(), 0, secondsBetweenPrintouts, TimeUnit.SECONDS);

        while (true) {
            String line = scanner.nextLine();
            try {
                moneyService.registerPayment(new MoneyAmount(line));
            } catch (MoneyParseException parseException) {
                System.err.println("Wrong input format. Please enter (without quotes): \"<CURRENCY_CODE> <DECIMAL VALUE>\"");
                logger.info("Bad user input", parseException);
            }
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar payment-tracker.jar [options]", "Application that tracks payments in-memory.", options, "");
    }

    private static Options prepareCmdOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Displays help");
        options.addOption(Option.builder("f")
                .longOpt("file")
                .hasArg()
                .argName("FILE")
                .optionalArg(false)
                .desc("Loads payments from a specified file at startup")
                .build()
        );
        options.addOption(Option.builder("e")
                .longOpt("exchange")
                .hasArg()
                .argName("FILE")
                .optionalArg(false)
                .desc("File that contains the exchange rates. Each line of the file should contain three comma-separated entries: \n <CODE>, <CODE>, <RATE>")
                .build()
        );
        options.addOption(Option.builder("t")
                .longOpt("timeout")
                .hasArg()
                .argName("SECONDS")
                .optionalArg(false)
                .desc("Specifies delay in seconds between printing out the payment status. Default is 60")
                .build()
        );
        return options;
    }
}
