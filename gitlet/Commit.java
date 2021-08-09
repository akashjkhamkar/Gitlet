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
    private static metafile repometa;
    private static meta branchmeta;

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
        repometa = metafile.getMeta();
        branchmeta = meta.getBranchMeta(repometa.currentBranch);

        message = m;
        date = new Date();
        branch = repometa.currentBranch;
        parent = branchmeta.latest;

        files = hashStagedFiles();
    }


    private static HashMap<String, String> hashStagedFiles(){
        HashMap<String, String> hashedFileNames = new HashMap<>();
        for (String file: branchmeta.stagedFiles) {
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

        branchmeta.stagedFiles.clear();
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
                System.out.println("unique hash !");
                commit.createNewFile();
            }else{
                System.out.println("already exists !");
                getCommit(hash).dump();
                dump();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        new meta(branch, hash).saveMeta();
        Utils.writeObject(commit, this);
    }
}
