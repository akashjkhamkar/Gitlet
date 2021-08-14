package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD_real = new File(System.getProperty("user.dir"));
    public static final File CWD = join(CWD_real, "CWD_safe");
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

    private static String splitPoint(String commit1, String commit2){
        // find a path till initial commit of repo,
        // iterate to find the common split point
        List<String> path1 = Commit.log(commit1, false);
        List<String> path2 = Commit.log(commit2, false);

        for (String node: path1) {
            if (path2.contains(node)){
                return node ;
            }
        }

        System.out.println("something went wrong :(");
        return null;
    }

    private static void conflict(String fileName, String hash1, String hash2){
        String resultantFile = "<<<<<<< HEAD\n";

        if (hash1 != null){
            resultantFile += readContentsAsString(join(blobs, hash1));
        }
        resultantFile += "=======\n";

        if (hash2 != null){
            resultantFile += readContentsAsString(join(blobs, hash2));
        }
        resultantFile += ">>>>>>>";

        // update the file in cwd
        writeContents(join(CWD, fileName), resultantFile);
    }

    public static void merge(String branchName){
        if (!join(metaFolder, branchName).exists()){
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (branchName.equals(current_branch_name)){
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        if (stage.areFilesStaged()){
            System.out.println("You have uncommitted changes.");
            return;
        }

        if (stage.getUntrackedFiles().size() != 0){
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }

        // find the split point
        String currentId = current_branch.latest;
        String givenId = Branch.getBranch(branchName).latest;
        String splitPointId = splitPoint(currentId, givenId);

        Commit currentCommit = Commit.getCommit(currentId);
        Commit givenCommit = Commit.getCommit(givenId);
        Commit splitPoint = Commit.getCommit(splitPointId);

        // fast forward merge
        if (splitPointId.equals(givenId)){
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        if (splitPointId.equals(currentId)){

            // adding the references to the both commits
            // first one is the current braches last commit, second is where this commit came from
            // updating the current branches pointer to new commit (done in savecommit)
            givenCommit.parents.add(0, currentId);
            givenCommit.branch = current_branch_name;
            givenCommit.saveCommit();


            // deleting the branch file
            rm_branch(branchName);

            // checkout the merged commit
            String[] args = {"reset", sha1(serialize(givenCommit))};
            checkout(args);

            System.out.println("Current branch fast-forwarded.");
            return;
        }

        // 3 way merge
        TreeMap<String, String> currentFiles = currentCommit.files;
        TreeMap<String, String> givenFiles = givenCommit.files;
        TreeMap<String, String> splitFiles = splitPoint.files;

        boolean conflictsFound = false;

        for (Map.Entry<String, String> entry: givenFiles.entrySet()){
            String fileName = entry.getKey();
            String givenHash = entry.getValue();

            // if both have the file
            if (splitFiles.containsKey(fileName) && currentFiles.containsKey(fileName)){
                String currentHash = currentFiles.get(fileName);
                String splitHash = splitFiles.get(fileName);

                // 1. modified in the given but not in the current
                // 2. vice versa
                if (givenHash.equals(splitHash) && !currentHash.equals(splitHash)){
                    continue;
                }else if (!givenHash.equals(splitHash) && currentHash.equals(splitHash)){
                    writeContents(join(CWD, fileName), readContentsAsString(join(blobs, givenHash)));
                    add(fileName);
                }else if (givenHash.equals(currentHash)){
                    continue;
                }else {
                    conflict(fileName, currentHash, givenHash);
                    conflictsFound = true;
                    add(fileName);
                }
                continue;
            }

            if (splitFiles.containsKey(fileName)){
                String splitHash = splitFiles.get(fileName);
                if (!splitHash.equals(givenHash)){
                    conflict(fileName, null, givenHash);
                    conflictsFound = true;
                    add(fileName);
                }
                continue;
            }

            if (currentFiles.containsKey(fileName)){
                String currentHash = currentFiles.get(fileName);
                if (!currentHash.equals(givenHash)){
                    conflict(fileName, currentHash, givenHash);
                    conflictsFound = true;
                    add(fileName);
                }
                continue;
            }

            writeContents(join(CWD, fileName), readContentsAsString(join(blobs, givenHash)));
            add(fileName);
        }

        for (Map.Entry<String, String> entry: currentFiles.entrySet()){
            String fileName = entry.getKey();
            String currentHash = entry.getValue();

            if (splitFiles.containsKey(fileName) && !givenFiles.containsKey(fileName)){
                String splitHash = splitFiles.get(fileName);
                if (currentHash.equals(splitHash)){
                    rm(fileName);
                }else{
                    conflict(fileName, currentHash, null);
                    conflictsFound = true;
                    add(fileName);
                }
            }
        }

        Commit newCommit = commit("Merged "+ branchName + " into " + current_branch_name);
        if (newCommit != null){
            newCommit.parents.add(givenId);
            newCommit.saveCommit();
            join(metaFolder, branchName).delete();
        }
        if (conflictsFound){
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void rm_branch(String branchName){
        if (current_branch_name.equals(branchName)){
            System.out.println("Cannot remove the current branch.");
        }else if (!join(metaFolder, branchName).exists()){
            System.out.println("A branch with that name does not exist.");
        }else{
            join(metaFolder, branchName).delete();
        }
    }

    public static void checkout(String[] args){
        System.out.println(Arrays.toString(args));

        if (args.length > 4 || args.length < 2) {
            System.out.println("wrong args");
            return;
        }

        if ((args.length == 3 && args[1].equals("--")) || (args.length == 4 && (args[2].equals("--")))) {
            String fileName;
            Commit commit;

            if (args[1].equals("--")) {
                fileName = args[2];
                commit = Commit.getCommit(current_branch.head);

            }else {
                fileName = args[3];
                String commitId = args[1];
                if (!join(commits, commitId).exists()){
                    System.out.println("No commit with that id exists.");
                    return;
                }
                commit = Commit.getCommit(commitId);
            }
            // restore from the last commit
            File file = join(CWD, fileName);

            TreeMap<String, String> trackedFiles = commit.files;

            if (!trackedFiles.containsKey(fileName)){
                System.out.println("File does not exist in that commit.");
                return;
            }
            writeContents(file, readContentsAsString(join(blobs, trackedFiles.get(fileName))));
        }else if (args.length == 2){
            // change branch

            // 1. we need to delete whole directory
            // 2. paste all the files from the checked out files
            // if any untracked files are present , give warning
            String checkedOut;

            if (args[0].equals("checkout")){
                checkedOut = args[1];
                if (current_branch_name.equals(checkedOut)){
                    System.out.println("No need to checkout the current branch.");
                    return;
                }else if (!join(metaFolder, checkedOut).exists()){
                    System.out.println("No such branch exists.");
                    return;
                }
            }else {
                // validation for reset command
                // check if the commit exist
                checkedOut = args[1];
                if (!join(commits, checkedOut).exists()){
                    System.out.println("No commit with that id exists.");
                    return;
                }
            }

            if (stage.getUntrackedFiles().size() != 0){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }

            // clearing stage
            stage.clearStage();

            // deleting whole directory
            for (String filetoBeDeleted: plainFilenamesIn(CWD)){
                restrictedDelete(join(CWD, filetoBeDeleted));
            }

            // copying contents of the checked out branche's/commits files
            TreeMap<String, String> newfiles;
            if (args[0].equals("checkout")){
                String checkedCommit = Branch.getBranch(checkedOut).head;
                newfiles = Commit.getCommit(checkedCommit).files;
                Utils.writeContents(current_branch_file, checkedOut);
            }else{
                Commit checkedCommit = Commit.getCommit(checkedOut);
                newfiles = checkedCommit.files;

                // updating head of the branch
                Utils.writeContents(current_branch_file, checkedCommit.branch);
                Branch checkedBranch = Branch.getBranch(checkedCommit.branch);
                checkedBranch.head = checkedOut;
                checkedBranch.saveBranch();
            }

            for (Map.Entry<String, String> entry: newfiles.entrySet()) {
                String fileName = entry.getKey();
                String hash = entry.getValue();

                writeContents(join(CWD, fileName), readContentsAsString(join(blobs, hash)));
            }


        }else{
            System.out.println("wrong arg");
        }

    }

    // 1. head commmit will have 2 owners
    public static void branch(String branchName){
        File newBranch = join(metaFolder, branchName);
        if (newBranch.exists()){
            System.out.println("A branch with that name already exists.");
        }else{
            new Branch(branchName, current_branch.head).saveBranch();
        }
    }

    public static void status(){
        Branch.status();
        stage.status();
    }

    public static void find(String m){
        Commit.find(m);
    }

    public static void log(){
        Commit.log(current_branch.head, true);
    }

    public static void globalLog(){
        Commit.globalLog();
    }

    public static void rm(String fileName){
        stage.removeFromStage(fileName);
        stage.saveStage();
    }

    public static void add(String fileName){
        stage.addToStage(fileName);
        stage.saveStage();
    }

    public static Commit commit(String m){
        if (!stage.areFilesStaged()){
            System.out.println("No changes added to the commit.");
            return null;
        }
        Commit newCommit = new Commit(m);
        newCommit.saveCommit();
        return newCommit;
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
