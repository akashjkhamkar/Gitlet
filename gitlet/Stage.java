package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public void status(){
        System.out.println("=== Staged Files ===");
        TreeMap<String, String> stagedFiles = stage.getStagedFiles();
        for (Map.Entry<String, String> entry: stagedFiles.entrySet()) {
            String filename = entry.getKey();
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        TreeMap<String, String> removedFiles = stage.getRemovedFiles();
        for (Map.Entry<String, String> entry: removedFiles.entrySet()) {
            String filename = entry.getKey();
            System.out.println(filename);
        }
        System.out.println();


        System.out.println("=== Modifications Not Staged For Commit ===");
        // making a treemap for the dir files

        TreeMap<String, Boolean> modified = new TreeMap<String, Boolean>();
        List<String> dirFiles = plainFilenamesIn(CWD);
        TreeMap<String, String> dirTree = new TreeMap<String, String>();
        for (String fileName: dirFiles) {
            File file = join(CWD, fileName);
            String hash = sha1(readContentsAsString(file));
            dirTree.put(fileName, hash);
        }

        // 1. logging the modified or deleted staged files
        for (Map.Entry<String, String> entry: staged.entrySet()) {
            String fileName = entry.getKey();
            String filehash = entry.getValue();

            if (!dirTree.containsKey(fileName)){
                modified.put(fileName, false);
                continue;
            }

            if (!dirTree.containsValue(filehash)){
                modified.put(fileName, true);
                continue;
            }

        }

        // 2. logging deleted tracked files or modified but not added
        Commit headCommit = Commit.getCommit(current_branch.head);
        for (Map.Entry<String, String> entry: headCommit.files.entrySet()) {
            String fileName = entry.getKey();
            String filehash = entry.getValue();

            if (!dirTree.containsKey(fileName)){
                if (!removed.containsKey(fileName)){
                    modified.put(fileName, false);
                }
                continue;
            }else if (removed.containsKey(fileName)){
                continue;
            }

            if (!dirTree.containsValue(filehash) && !staged.containsKey(fileName)){
                modified.put(fileName, true);
            }

            // only keeping the newly added files to log untracked files
            dirTree.remove(fileName);
        }

        for (Map.Entry<String, Boolean> entry: modified.entrySet()) {
            String state = entry.getValue() ? "(modified)":"(deleted)";
            System.out.println(entry.getKey() + " " + state);
        }
        System.out.println();

        // 3. logging the untracked files
        System.out.println("=== Untracked Files ===");
        for (Map.Entry<String, String> entry: dirTree.entrySet()) {
            String fileName = entry.getKey();

            if (!staged.containsKey(fileName)){
                System.out.println(fileName);
            }
        }

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
        File file = join(CWD, filename);
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
        File file = join(CWD, filename);

        // check if exists
        if (!file.exists()){
            System.out.println("File does not exist.");
            return;
        }

        if (removed.containsKey(filename)){
            removed.remove(filename);
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
