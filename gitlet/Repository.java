package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File metaFolder = join(GITLET_DIR, "meta");
    public static final File repometa = join(metaFolder, "metafile");

    /** Checks if existing gitlet exists , if doesn't
     * makes a new one and adds branch master to it
     */
    public static void test(){
        File test = new File("dummy");
        File dummy = new File("dummy.txt");

        Utils.writeContents(test, Utils.readContentsAsString(dummy));
        try {
            test.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(String fileName){
        File file = new File(fileName);

        // 1. check if it exists !
        if (!file.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // 2. check if the file is mutated
        metafile repometafile = metafile.getMeta();
        meta branch = meta.getBranchMeta(repometafile.currentBranch);
        Commit headCommit = Commit.getCommit(branch.head);

        if (headCommit.files.containsValue(sha1(readContentsAsString(file)))){
            System.out.println("file is not changed!");
            System.exit(0);
        }

        branch.addtoStage(fileName);
    }

    public static void commit(String m){
        if (meta.getBranchMeta(metafile.getMeta().currentBranch).stagedFiles.isEmpty()){
            System.out.println("no files staged for commit !");
            System.exit(0);
        }
        new Commit(m).saveCommit();
    }

    public static void init() {
        try {
            if (!GITLET_DIR.exists()) {
                GITLET_DIR.mkdir();
                commits.mkdir();
                metaFolder.mkdir();
                repometa.createNewFile();

                new meta("master", null).saveMeta();
                new Commit("initial commit").saveCommit();
            }else{
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }


}
