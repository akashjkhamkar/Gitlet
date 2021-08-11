package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.current_branch;
import static gitlet.Repository.stage;
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
    public String parent;
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
        parent = Repository.current_branch.latest;

        if (parent != null){
            files = updatedTree();
        }else{
            files = new TreeMap<String, String>();
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

        System.out.println(updatedtree);
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
