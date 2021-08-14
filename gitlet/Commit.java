package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */

public class Commit implements Serializable ,Dumpable{
    /** The message of this Commit. */
    public Date date;
    public String message;
    public List<String> parents;
    public String branch;
    public TreeMap<String, String> files;

    public void dump(){
        System.out.println(date);
        System.out.println(message);
        System.out.println(branch);
        System.out.println(files);
    }

    Commit(String m){
        message = m;
        date = new Date();
        branch = Repository.current_branch_name;
        parents = new ArrayList<String>();
        parents.add(Repository.current_branch.latest);

        if (parents.get(0) != null){
            files = updatedTree();
        }else{
            files = new TreeMap<String, String>();
        }
    }

    public static void details(Commit node, String hash){
        System.out.println("===");
        System.out.println("commit " + hash);
        if (node.parents.size() == 2){
            String hash1 = node.parents.get(0).substring(0,7);
            String hash2 = node.parents.get(1).substring(0,7);
            System.out.println("Merge: " + hash1 + " " + hash2);
        }
        System.out.println("Date: " + node.date);
        System.out.println(node.message);
        System.out.println();
    }

    public static void find(String m) {
        List<String> allCommits = plainFilenamesIn(commits);

        boolean exists = false;
        for (String hash : allCommits) {
            Commit node = Commit.getCommit(hash);
            if (node.message.toLowerCase().contains(m.toLowerCase())) {
                exists = true;
                System.out.println(hash);
            }
        }

        if (!exists){
            System.out.println("Found no commit with that message.");
        }
    }

    public static List<String> log(String commitId, Boolean verbose){
        String hash = commitId;
        List<String> history = new ArrayList<String>();
        while (hash!=null){
            history.add(hash);
            Commit node = Commit.getCommit(hash);

            if (verbose){
                details(node, hash);
            }

            hash = node.parents.get(0);
        }
        return history;
    }

    public static void globalLog(){
        List<String> allCommits = plainFilenamesIn(commits);
        for (String hash: allCommits) {
            Commit node = Commit.getCommit(hash);
            details(node, hash);
        }
    }

    // take the staging area

    // iterate over last commmit files
    // add updated files
    // add new files
    // dont add removed files
    // clear staging area

    private static TreeMap<String, String> updatedTree(){
        TreeMap<String, String> updatedtree = Commit.getCommit(current_branch.head).files;
        TreeMap<String, String> stagedFiles = stage.getStagedFiles();
        TreeMap<String, String> removedFiles = stage.getRemovedFiles();

        // updating old files, adding newly added files
        for (Map.Entry<String, String> entry: stagedFiles.entrySet()){
            String filename = entry.getKey();
            String filehash = entry.getValue();

            updatedtree.put(filename, filehash);
        }

        // removing the removed files
        for (Map.Entry<String, String> entry: removedFiles.entrySet()){
            String filename = entry.getKey();

            updatedtree.remove(filename);
        }

        // clear stage
        stage.clearStage();

        return updatedtree;
    }

    public static Commit getCommit(String name){
        return readObject(join(Repository.commits, name), Commit.class);
    }

    public void saveCommit(){
        String hash = Utils.sha1(Utils.serialize(this));
        File commit = Utils.join(Repository.commits, hash);

        try {
            if (!commit.exists()){
                commit.createNewFile();
            }else{
                getCommit(hash).dump();
                dump();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        // logging the latest commmit in branch
        new Branch(branch, hash).saveBranch();
        Utils.writeObject(commit, this);
    }
}
