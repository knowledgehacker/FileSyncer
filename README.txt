FileSyncer is a small tool implemented in Java to synchronize the create/delete and change operations on a directory from one machine to the other
machine via SSH and HTTP protocols. Have fun:-)

Constraints:
1. Currently only support linux-to-linux.
2. For synchronizing via SSH, you need to generate private/public keys with name id_rsa/id_rda.pub in ~/.ssh/, and ensure the remote server has 
sshd service running.

Usage:
1. SSH version
You can build it from source in ssh directory or just use the FileSyncer.tar.gz by the following steps:
1). Extract FileSyncer.tar.gz.
tar xf FileSyncer.tar.gz

2). Run FileSyncer
java -jar FileSyncer.jar directory_you_want_to_synchronize user@server

2. HTTP version
You can build the server and client from source in http directory by running make in the corresponding directories.

