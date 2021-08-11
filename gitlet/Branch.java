package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    public void saveBranch() {
        File branchMeta = Utils.join(Repository.metaFolder, branchName);
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
