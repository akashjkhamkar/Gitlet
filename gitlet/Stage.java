package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Struct;
import java.util.TreeMap;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

public class Stage implements Serializable, Dumpable{
    private TreeMap<String, String> staged;
    private TreeMap<String, String> removed;

    Stage(){
        staged = new TreeMap<String, String>();
        removed = new TreeMap<String, String>();
    }

    @Override
    public void dump(){
        System.out.println("stage :" + staged);
        System.out.println("removed :" + removed);
    }

    public boolean areFilesStaged(){
        return !(staged.isEmpty() && removed.isEmpty());
    }

    public void clearStage(){
        staged.clear();
        removed.clear();
        saveStage();
    }

    public TreeMap<String, String> getStagedFiles(){
        return staged;
    }

    public TreeMap<String, String> getRemovedFiles(){
        return removed;
    }


    // 1. if added , remove
    // 2. if present in the last commit , delete and stage for removal
    // 3. if not added or present in last commmit , print y t hell ar you deleting

    public void removeFromStage(String filename){
        File file = new File(filename);
        Commit headCommit = Commit.getCommit(current_branch.head);

        // check if exists
        if (!file.exists()){
            System.out.println("File does not exist.");
            return;
        }

        if (!staged.containsKey(filename) && !headCommit.files.containsKey(filename)){
            System.out.println("No reason to remove the file.");
            return;
        }

        String fileHash = sha1(readContentsAsString(file));

        // check if its staged, remove if its
        if (staged.containsKey(filename)){
            staged.remove(filename);
        }

        // check in that last commit, stage for removal if it is and
        // delete
        if (headCommit.files.containsKey(filename)){
            removed.put(filename, fileHash);
            restrictedDelete(file);
        }
    }

    public void addToStage(String filename){
        File file = new File(filename);

        // check if exists
        if (!file.exists()){
            System.out.println("File does not exist.");
            return;
        }

        String fileHash = sha1(readContentsAsString(file));

        // compare with last commit
        Commit headCommit = Commit.getCommit(current_branch.head);
        if (headCommit.files.containsValue(fileHash)){
            System.out.println("file is not changed!");

            if (staged.containsKey(filename)){
                staged.remove(filename);
            }
            return;
        }

        // check if already added
        if (staged.containsValue(fileHash)){
            System.out.println("already staged !");
            return;
        }

        File destination = join(Repository.blobs, fileHash);
        writeContents(destination, readContentsAsString(file));
        staged.put(filename, fileHash);
    }

    public void saveStage(){
        Utils.writeObject(stageFile, this);
    }

    public static Stage getStage(){
        return readObject(stageFile, Stage.class);
    }
}
