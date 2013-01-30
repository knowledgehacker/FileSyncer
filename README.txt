FileSyncer is a small tool to synchronize the create/delete and change operations on the current directory to a specified remote server.
This is an experimental release, it only monitor the operations for one minute. Have fun:-)

Constraints:
1. Currently only support linux-to-linux.
2. You need to generate private/public keys with name id_rsa/id_rda.pub in ~/.ssh/. And ensure the remote server has sshd service running.

Usage:
1. Extract FileSyncer.tar.gz to the directory you want to synchronize.
tar jx FileSyncer.tar.gz -C directory_you_want_to_synchronize

2. Run FileSyncer
java -jar FileSyncer.jar user@server
