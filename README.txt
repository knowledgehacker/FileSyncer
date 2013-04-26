FileSyncer is a small tool implemented in Java to synchronize the create/delete and change operations on a directory 
from one machine to the other machine via SSH and HTTP protocols. Have fun:-)

1. SSH version
FileSyncer ssh implementation resides in "ssh" branch.
Constraints:
a. Currently only support linux-to-linux.
b. For synchronizing via SSH, you need to generate private/public keys with name id_rsa/id_rda.pub in ~/.ssh/,
and ensure the remote server has sshd service running.

Usage:
a. Build from source
make
b. Run FileSyncer
java -jar FileSyncer.jar directory_you_want_to_synchronize user@server

2. HTTP version
FileSyncer http implementation contains both server and client. You can use setup the server and sync to it.
a. Build from source
mvn package	# in directory "http"
