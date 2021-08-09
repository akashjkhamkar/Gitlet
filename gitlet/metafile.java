package gitlet;

import java.io.Serializable;

public class metafile implements Serializable, Dumpable {
    String currentBranch;

    metafile(String branch){
        currentBranch = branch;
    }

    @Override
    public void dump(){
        System.out.println(currentBranch);
    }


    public static metafile getMeta(){
        return Utils.readObject(Repository.repometa, metafile.class);
    }


}
