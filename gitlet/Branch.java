package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Repository.current_branch_name;
import static gitlet.Repository.metaFolder;
import static gitlet.Utils.plainFilenamesIn;

public class Branch implements Serializable, Dumpable{
    public String branchName;
    public String head;
    public String latest;

    Branch(String name, String commit){
        branchName = name;
        latest = commit;
        head = commit;
    }

    @Override
    public void dump(){
        System.out.println(branchName);
        System.out.println("latest : "+latest);
        System.out.println("head : "+head);
    }

    public static void status(){
        // branches and current
        List<String> allBranchNames = plainFilenamesIn(metaFolder);

        System.out.println("=== Branches ===");
        for (String branchName: allBranchNames) {
            if (branchName.equals("current")){
                continue;
            }
            if (branchName.equals(current_branch_name)){
                System.out.println("*"+branchName);
            }else{
                System.out.println(branchName);
            }
        }

        System.out.println();
    }

    public void saveBranch() {
        File branchMeta = Utils.join(metaFolder, branchName);
        try {
            if (!branchMeta.exists()){
                branchMeta.createNewFile();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        Utils.writeObject(branchMeta, this);
    }

    public static Branch getBranch(String Branchname){
        File branchMetafile = Utils.join(Repository.GITLET_DIR, "meta/"+Branchname);
        return Utils.readObject(branchMetafile, Branch.class);
    }

}
