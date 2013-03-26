FileSyncer is a small tool implemented in Java to synchronize the create/delete and change operations on a directory to a specified remote server via ssh.
You can use it to transfer files between two machines, or backup your files to a remote machine. Have fun:-)

Constraints:
1. Currently only support linux-to-linux.
2. You need to generate private/public keys with name id_rsa/id_rda.pub in ~/.ssh/. And ensure the remote server has sshd service running.

Usage:
1. Extract FileSyncer.tar.gz.
tar xf FileSyncer.tar.gz

2. Run FileSyncer
java -jar FileSyncer.jar directory_you_want_to_synchronize user@server
