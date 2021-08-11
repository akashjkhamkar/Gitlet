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
    public static final File current_branch_file = join(metaFolder, "current");
    public static final File stageFile = join(GITLET_DIR, "stage");

    public static String current_branch_name;
    public static Branch current_branch;
    public static Stage stage;

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

        if (!stageFile.exists()){
            stage = new Stage();
            stage.saveStage();
        }else{
            stage = Stage.getStage();
        }

        if (!current_branch_file.exists()){
            try {
                current_branch_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        current_branch_name = Utils.readContentsAsString(current_branch_file);

        if (!current_branch_name.equals("")){
            current_branch = Branch.getBranch(current_branch_name);
        }
    }

    public static void log(){
        Commit.log();
    }

    public static void rm(String fileName){
        stage.removeFromStage(fileName);
        stage.saveStage();
    }

    public static void add(String fileName){
        stage.addToStage(fileName);
        stage.saveStage();
    }

    public static void commit(String m){
        if (!stage.areFilesStaged()){
            System.out.println("No changes added to the commit.");
            return;
        }
        new Commit(m).saveCommit();
    }

    public static void init() {
        if (!current_branch_name.equals("")) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        } else {
            new Branch("master", null).saveBranch();
            Utils.writeContents(current_branch_file, "master");
            current_branch_name = "master";
            current_branch = Branch.getBranch(current_branch_name);

            new Commit("initial commit").saveCommit();
        }
    }
}
