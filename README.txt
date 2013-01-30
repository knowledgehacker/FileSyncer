FileSyncer is a small tool to synchronize the create/delete and change operations on the current directory to a specified remote server.
This is an experimental release, it only monitor the operations for one minute. Have fun:-)

Usage:
1. Extract FileSyncer.tar.gz to the directory you want to synchronize.
tar jx FileSyncer.tar.gz -C directory_you_want_to_synchronize

2. Run FileSyncer
java -jar FileSyncer.jar user@server
