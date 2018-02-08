# Few Scala demo cases

[![Build Status](https://travis-ci.org/SergeyAxenov/qx-scala.svg?branch=master)](https://travis-ci.org/SergeyAxenov/qx-scala) 

Reads "transactions" from CSV file, generates 3 reports.

Regarding rolling stat report:
* Five days rolling window is used for the rolling stat report.
* The rolling stat report can be generated in two modes: with and without partial first 5 days (use `-p` argument to run with partial)

## Building From Source
This demo is built with [SBT](http://www.scala-sbt.org/0.13/docs/Command-Line-Reference.html)

To build a JAR file simply run `sbt package` from the project root.

To build a "Fat JAR" run `sbt clean assembly` from the project root.

## Unit tests
To run the unit tests, you should run `sbt test`

## Running using SBT
The output reports are stored in the project root:
* dailyAmount.csv
* accountAverage.csv
* rollingStats.csv


### Running with default parameters
Will try to find `transactions.txt` in the project root.  

```
sbt run
```  

### Running providing path to the transaction file
```
sbt "run C:/Users/User1/Desktop/transactions.txt"
```  

### Running providing path to the transaction file and enable partial first 5 days of the rolling stat report
```
sbt "run C:/Users/User1/Desktop/transactions.txt -p"
```  

## Rolling stat report without partial 5 days statistics (default)
The rolling stat report excludes initial 5 days of rolling stat by default.
The reports start with day 6.


```
Day,Account ID,Maximum,Average,AA Total Value,CC Total Value,FF Total Value
6,A1,977.98,458.93666666666667,0.0,171.19,977.98
6,A10,747.62,465.39750000000004,0.0,556.4,0.0
6,A11,914.89,597.085,1906.46,0.0,0.0
6,A12,928.86,594.972,0.0,0.0,438.88
...
```


## Rolling stat report with partial 5 days statistics (`-p` argument)
The rolling stat report includes initial 5 days of rolling stat by default.
The reports start with day 2.

The partial stat for day 2 is calculated using only day 1 of transactions.
The partial stat for day 3 is calculated using days 1 and 2 of transactions.
The partial stat for day 4 is calculated using days 1, 2 and 3 of transactions.
The partial stat for day 5 is calculated using days 1, 2, 3 and 4 of transactions.
The stat for day 6 is the first full stat and calculated using days 1, 2, 3, 4 and 5 of transactions.


```
Day,Account ID,Maximum,Average,AA Total Value,CC Total Value,FF Total Value
2,A1,227.64,227.64,0.0,0.0,0.0
2,A10,557.57,490.755,0.0,423.94,0.0
2,A13,996.67,829.4,0.0,0.0,0.0
...
5,A7,650.23,406.8566666666666,0.0,0.0,203.34
5,A8,624.81,371.8616666666667,598.28,253.09,323.22
5,A9,812.73,661.99,0.0,0.0,0.0
6,A1,977.98,458.93666666666667,0.0,171.19,977.98
6,A10,747.62,465.39750000000004,0.0,556.4,0.0
6,A11,914.89,597.085,1906.46,0.0,0.0
...
```  