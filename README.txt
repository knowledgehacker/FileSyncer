FileSyncer is a small tool based on ssh to synchronize the create/delete and change operations on a directory to a specified remote server.
This is an experimental release. Have fun:-)

Constraints:
1. Currently only support linux-to-linux.
2. You need to generate private/public keys with name id_rsa/id_rda.pub in ~/.ssh/. And ensure the remote server has sshd service running.

Usage:
1. Extract FileSyncer.tar.gz.
tar jx FileSyncer.tar.gz

2. Run FileSyncer
java -jar FileSyncer.jar directory_you_want_to_synchronize user@server
