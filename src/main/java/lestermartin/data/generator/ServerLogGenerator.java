package lestermartin.data.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;


public class ServerLogGenerator {

    private String fieldTokenizer = ",";
    private Float[] hourlyWeights = {
            0.07f, 0.07f, 0.01f, 0.01f, 0.02f, 0.10f,
            0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f,
            0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f,
            0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f
    };
    private List<Float> shuffledHourlyWeights = null;
    private List<String> ipAddresses = null;
    private String[] appIds = {
            "Tomcat", "Apache", "WebLogic", "Payroll", "HRIS", "ERP", "CRM",
            "DB2", "MySQL", "PostgreSQL", "MongoDB", "Cassandra",
            "App01", "App02", "App03", "App04", "App05", "App06", "App07",
            "App91", "App92", "App93", "App94", "App95", "App96", "App97"
    };
    private String[] weightedLogTypes = {
            "REQUEST","REQUEST","REQUEST","REQUEST","REQUEST","REQUEST","REQUEST","REQUEST",
            "AUDIT","AUDIT","AUDIT","AUDIT","AUDIT","AUDIT","AUDIT","AUDIT","AUDIT","AUDIT",
            "AVAILABILITY","AVAILABILITY","AVAILABILITY","AVAILABILITY","AVAILABILITY","AVAILABILITY",
            "THREAT","THREAT","THREAT","THREAT",
            "EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT",
            "EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT",
            "EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT",
            "EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT",
            "EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT",
            "EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT",
            "EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT","EVENT"
    };
    private String[] weightedLogLevels = {
            "TRACE", "TRACE","TRACE","TRACE","TRACE","TRACE","TRACE","TRACE","TRACE","TRACE",
            "TRACE", "TRACE","TRACE","TRACE","TRACE","TRACE","TRACE","TRACE","TRACE","TRACE",
            "DEBUG", "DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG",
            "DEBUG", "DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG",
            "DEBUG", "DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG","DEBUG",
            "INFO", "INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO",
            "INFO", "INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO",
            "INFO", "INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO",
            "INFO", "INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO",
            "INFO", "INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO",
            "INFO", "INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO","INFO",
            "WARN", "WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN",
            "WARN", "WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN","WARN",
            "ERROR", "ERROR","ERROR","ERROR","ERROR","ERROR","ERROR","ERROR","ERROR","ERROR",
            "FATAL","FATAL","FATAL","FATAL","FATAL"
    };
    private String[] serverMessages = {
            "Tomcat ", "Apache ", "WebLogic ", "Payroll ", "HRIS ", "ERP ", "CRM ",
            "DB2 ", "MySQL ", "PostgreSQL ", "MongoDB ", "Cassandra ",
            "App01 ", "App02 ", "App03 ", "App04 ", "App05 ", "App06 ", "App07 ",
            "App91 ", "App92 ", "App93 ", "App94 ", "App95 ", "App96 ", "App97 ",
            "server down ", "invalid attempt ", "invalid number of retries ", "ghost in the machine ",
            "the running man ", "movie list incorrect ", "buffer overflow ", "garbage collection ",
            "the roof is on fire ", "we don't need no water ", "let the silly thing burn ",
            "inadequate cooling ", "network starved ", "situation untenable ", "solent green is people ",
            "buffer overflow ", "denial of service ", "catcher in the rye ", "the lost boys ",
            "the only thing wrong with santa clarita is all the darn vampires ", "press the enter key ",
            "algorithm is not good ", "algorithm is not sound ", "algorithm is faulty ",
            "analog beats digital anyday ", "bitmap images are simple ", "browser incompatability ",
            "bug ", "bugs ", "more bugs ", "heckofa lot of bugs ", "minivan money level of bugs ",
            "bug ", "bugs ", "more bugs ", "heckofa lot of bugs ", "minivan money level of bugs ",
            "bug ", "bugs ", "more bugs ", "heckofa lot of bugs ", "minivan money level of bugs ",
            "bug ", "bugs ", "more bugs ", "heckofa lot of bugs ", "minivan money level of bugs ",
            "bug ", "bugs ", "more bugs ", "heckofa lot of bugs ", "minivan money level of bugs "
    };

    public ServerLogGenerator() {
        //create bogus IP addresses
        ipAddresses = new ArrayList<String>();
        ipAddresses.add("55.55.55.55");
        ipAddresses.add("99.99.99.99");
        for(int i=100; i < 251; i++) {
            for(int j=20; j < 53; j++) {
                ipAddresses.add(i + ".101." + j + "." + i);
            }
        }
        for(int k=201; k < 216; k++) {
            ipAddresses.add("77." + k + ".177.88");
        }

        //shuffle the hourly weighting of the total number of logs to produce for the day
        shuffledHourlyWeights = Arrays.asList(hourlyWeights);
        Collections.shuffle(shuffledHourlyWeights);
        System.out.println("hourly weights; " + Arrays.toString(shuffledHourlyWeights.toArray()));
    }

    public static void main(String[] args) {
        System.out.println("*** DATA GENERATION TIME; server log style!");
        if(args.length != 3) {
            System.out.println("ERR: 3 args are required");
            System.out.println(" args list: logDay, numEvents, logDirectory");
            System.out.println("   example: 2021-07-01 240000 /Users/lester/dev/bogus/");
            System.exit(404);
        }
        //TODO: validation logic on cli args
        String logDate = args[0];
        System.out.println("       logDay = " + logDate);
        int recsThisDay = Integer.parseInt(args[1]);
        System.out.println("    numEvents = " + recsThisDay);
        String fqDirName = args[2];
        System.out.println(" logDirectory = " + fqDirName);



        ServerLogGenerator jenny = new ServerLogGenerator();

        //loop thru the 24 randomized weighted hours
        for(int hour = 0; hour < jenny.shuffledHourlyWeights.size(); hour++) {
            //pretty up the single digit hour values to have a left-padded space
            String twoCharHour = String.format("%02d", hour);
            //break the total/day into totals/hour that use the uniquely mixed hourly weights
            int recsThisHour = (int) (jenny.shuffledHourlyWeights.get(hour) * recsThisDay);
            //stand up a List to hold all records for this hour
            List<String> singleHourLogLines = new ArrayList<String>(recsThisHour);

            //need to spread out the specific hour into even # recs/min
            int recsPerMin = 0;
            if(recsThisHour < 100) {
                recsPerMin = recsThisHour;
            } else {
                recsPerMin = (recsThisHour / 45) + 1;
            };
            int counterRecsThisMin = 0;
            int currMin = 11;
            for(int i=1; i <= recsThisHour; i++) {
                if(counterRecsThisMin >= recsPerMin) {
                    counterRecsThisMin = 0;
                    currMin++;
                }
                singleHourLogLines.add(jenny.generateLogRecord(logDate +
                        " " + twoCharHour + ":" + currMin + ":43"));
                counterRecsThisMin++;
            }
            //write this hour's worth of records to a file
            jenny.writeListToFile(singleHourLogLines, fqDirName +
                    logDate + "_" + twoCharHour + ".txt");
        }
    }

    private String generateLogRecord(String recTimestamp) {
        //Random rand = new Random();
        StringBuffer newRecord = new StringBuffer();
        //timestamp
        newRecord.append(recTimestamp);
        newRecord.append(fieldTokenizer);
        //random ip address
        newRecord.append(ipAddresses.get(ThreadLocalRandom.current().nextInt(ipAddresses.size())));
        newRecord.append(fieldTokenizer);
        //random application id
        newRecord.append(appIds[ThreadLocalRandom.current().nextInt(appIds.length)]);
        newRecord.append(fieldTokenizer);
        //random process id
        newRecord.append(ThreadLocalRandom.current().nextInt(99, 5999));
        newRecord.append(fieldTokenizer);
        //random log type
        newRecord.append(weightedLogTypes[ThreadLocalRandom.current().nextInt(weightedLogTypes.length)]);
        newRecord.append(fieldTokenizer);
        //random log level
        newRecord.append(weightedLogLevels[ThreadLocalRandom.current().nextInt(weightedLogLevels.length)]);
        newRecord.append(fieldTokenizer);
        //random message id
        newRecord.append(StringUtils.upperCase(RandomStringUtils.random(3, true, false)));
        newRecord.append("-");
        newRecord.append(RandomStringUtils.random(4, false, true));
        newRecord.append(fieldTokenizer);
        //random message
        for(int i=0; i < 7; i++) {
            newRecord.append(serverMessages[ThreadLocalRandom.current().nextInt(serverMessages.length)]);
        }
        newRecord.append(UUID.randomUUID());
        newRecord.append(System.lineSeparator());
        return newRecord.toString();
    }

    private void writeListToFile(List<String> allRecords, String fqFileName){
        try {
            File file = new File(fqFileName);
            FileWriter fileWriter = new FileWriter(file);
            for(String logRecord : allRecords) {
                fileWriter.write(logRecord);
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            //reached an abend (abnormal end for the non-framers)
            e.printStackTrace();     //print out what is happening
            System.exit(500); //being silly w/http err code
        }
        System.out.println("Wrote " + allRecords.size() + " records to " + fqFileName);
    }

}
