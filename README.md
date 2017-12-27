# drogon
One of those script names which has nothing to do with functionality.

This script reads all the files in the input file path and its sub-directories recursively and stores the hash values of the files in a metadata file (if they don't already exist). If they already exist, it will compute the hash and check it against the hash in the metadata files and report any discrepencies. 

This script can be scheduled to run as a cron-job that will check for any unintended changes in content of the files. One of the most useful applications of the script is for archived data sets which do not normally get edited over time.

Disclaimer: I am not a developer and this script may contain un-clean documentation, not-so-efficient data strucutures usage and may have to be modified a little to make it work on your environment. 

Free to download and imporvise. 
