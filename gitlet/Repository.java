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
    public static final File blobs = join(GITLET_DIR, "blobs");
    public static final File metaFolder = join(GITLET_DIR, "meta");
    public static final File repometa = join(metaFolder, "metafile");

    /** Checks if existing gitlet exists , if doesn't
     * makes a new one and adds branch master to it
     */
    public static void setupPersistence() {

        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }

        if (!commits.exists()) {
            commits.mkdir();
        }

        if (!blobs.exists()) {
            blobs.mkdir();
        }

        if (!metaFolder.exists()) {
            metaFolder.mkdir();
        }

        if (!repometa.exists()) {
            try {
                repometa.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        if (!Utils.readContentsAsString(repometa).equals("")) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        } else {
            new meta("master", null).saveMeta();
            new Commit("initial commit").saveCommit();
            Utils.writeContents(repometa, "master");
        }
    }
}
