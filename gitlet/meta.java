package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class meta implements Serializable, Dumpable{
    public String branchName;
    public String head;
    public String latest;
    public List<String> stagedFiles = new ArrayList<>();

    meta(String name, String commit){
        branchName = name;
        latest = commit;
        head = commit;
    }

    public void addtoStage(String file){
        System.out.println(stagedFiles);
        if (stagedFiles.contains(file)){
            System.out.println("file already ready added !");
            System.exit(0);
        }
        stagedFiles.add(file);
        saveMeta();
        System.out.println("added : "+file);
    }

    @Override
    public void dump(){
        System.out.println(branchName);
        System.out.println("latest : "+latest);
        System.out.println("head : "+head);
        System.out.println("staged : "+stagedFiles);
    }

    public void saveMeta() {
        File branchMeta = Utils.join(Repository.metaFolder, branchName);
        metafile updatedRepoMeta = new metafile(branchName);

        try {
            if (!branchMeta.exists()){
                branchMeta.createNewFile();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        Utils.writeObject(Repository.repometa, updatedRepoMeta);
        Utils.writeObject(branchMeta, this);
    }

    public static meta getBranchMeta(String Branchname){
        File branchMetafile = Utils.join(Repository.GITLET_DIR, "meta/"+Branchname);
        return Utils.readObject(branchMetafile, meta.class);
    }

}
