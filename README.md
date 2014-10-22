760
===

##Running
You need Test.java and all the files in the include folder then to compile run (note you might need to use \ instead of /:

javac -cp "Include/*" FileCreator.java Test.java

Then to create the files you need to run the application use:

java FileCreator -n 1000;10000 -r

If you would like to test with something other than the default configuration then use the parameters specified below to create the files and then use more command line arguments to specify the files to use in Test.

Then to run use (note you might need to use : instead of ;):

java -cp "./:./Include/*" Test

##Command line arguments
* -nodesinhidden int
* -numtestsperdataset int
* -numdatasets int
* -filesize int
* -testingsize int
* -maxiter int
* -learningrate double
* -maxerror double
* -outputfile string
* -summaryfile string
* -learnfile string
* -learnrandfile string
* -testfile string
* -testrandfile string

Also there are command line argument for the FileCreator program:
* -n comma separated list of file sizes you which to create
* -prefix
* -postfix
* -randfix
* -zerofix
* -r void create extra random column as well
* -z void create extra zero column as well
* -rep integer repeat each value in the file this number of time

