package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Must have at least one argument");
            System.exit(0);
        }

        Repository.setupPersistence();

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args[0], args, 1);
                Repository.init();
                break;
            case "commit":
                validateNumArgs(args[0], args, 2);
                Repository.commit(args[1]);
                break;
            case "add":
                validateNumArgs(args[0], args, 2);
                Repository.add(args[1]);
                break;
            case "rm":
                validateNumArgs(args[0], args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args[0], args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs(args[0], args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs(args[0], args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateNumArgs(args[0], args, 1);
                Repository.status();
                break;
            case "branch":
                validateNumArgs(args[0], args, 2);
                Repository.branch(args[1]);
                break;
            case "checkout":
                // TODO: fix validation
//                validateNumArgs(args[0], args, 2);
                Repository.checkout(args);
                break;
            case "rm-branch":
                validateNumArgs(args[0], args, 2);
                Repository.rm_branch(args[1]);
                break;
            case "reset":
                validateNumArgs(args[0], args, 2);
                Repository.checkout(args);
                break;
            case "merge":
                validateNumArgs(args[0], args, 2);
                Repository.merge(args[1]);
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                String.format("Invalid number of arguments for: %s.", cmd));
        }
    }

}
