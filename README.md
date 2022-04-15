# data-generator

Ruff-cut at a data generator that produces simulated log files.  Project _could_ grow to 
produce other types of file formats.

## Server Log Generator

Generates CSV files.

### Data Format

Field Name        | Data Type | Example Data | Additional Details
----------------- | --------- | ------------ | ------------------
Event Timestamp   | Timestamp | 2021-07-04 13:01:01 | Format is yyyy-MM-dd HH:mm:ss
IP Address        | String    | 202.101.36.182 | Static set of 990 values
Application Name  | String    | Payroll | Static set of 26 values
Process ID        | Integer   | 2397 | Random number from 99 - 5999
Log Type          | String    | THREAT | Randomly select from this list with highest probability from left-right; EVENT, AUDIT, REQUEST, AVAILABILTY, THREAT
Log Level         | String    | WARN | Randomly select from this list with highest probability from left-right; INFO, DEBUG, WARN, TRACE, ERROR, FATAL
Message ID        | String    | CCE-5059 | Random value with format XXX-9999
Message Details   | String    | more bugs Tomcat ERP bug bugs buffer overflow App95 10aebd44-78dc-459a-a678-01e586bd6309 | Random assembly of 7 words and/or phrases from a static set plus a UUID at the end

### Usage Details

After building with Maven you can run the generator with the following three arguments.
1. **logDirectory** -- the file path you want the log files saved do (NOTE: include the trailing `/` character)
2. **numEvents** -- how many log records you want generated for this single day
3. **logDay** -- the specific day you want the generator to create log files for in YYYY-MM-DD format

Here is an example usage.

`java -jar data-generator-0.0.1-SNAPSHOT.jar 2021-07-10 240000 /Users/lester/dev/bogus/`


