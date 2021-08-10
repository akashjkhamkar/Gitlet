package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.List;

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
    public HashMap<String, String> files;

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

        files = hashStagedFiles();
    }


    private static HashMap<String, String> hashStagedFiles(){
        HashMap<String, String> hashedFileNames = new HashMap<>();
        for (String file: Repository.current_branch.stagedFiles) {
            File orignal = new File(file);

            if (!orignal.exists()){
                continue;
            }

            System.out.println("making hash for file :- "+file);
            String hash = sha1(Utils.readContentsAsString(orignal));
            System.out.println("completed");
            File destination = Utils.join(Repository.blobs, hash);


            try{
                if (!destination.exists()){
                    destination.createNewFile();
                }else{
                    continue;
                }
            }catch (Exception e) {
                System.out.println(e);
            }

            Utils.writeContents(destination, Utils.readContentsAsString(orignal));
            hashedFileNames.put(file,hash);
        }

        Repository.current_branch.stagedFiles.clear();
        return hashedFileNames;
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
