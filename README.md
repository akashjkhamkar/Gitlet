# Gitlet
Gitlet is a lightweight java implementation of git, made for learning purposes

# Commands
add, rm, commit, branch, log, global-log, status, checkout, find, merge

# Overview
just like the real git, I made the structure as below

**Blobs**: Real git stores the chunks of the file in the blobs. Although here we store the entire file with its sha1 hash as its name in blobs.
Multiple commits can refer to the same blob.

**Commits**: Commit folder containes the serialised Commit objects. Each object contains the log message, other meta data, snapshot of the directory at time and reference to the last commit.

## How it takes the snapshot of the directory ?

We ONLY COPY THE FILES THAT HAVE CHANGED into the blobs folder and rename them with their sha1 hash. we note down the file name and hash in the commit. So if we were to restore files from that commit, we can refer to the hash in front of that file name.
