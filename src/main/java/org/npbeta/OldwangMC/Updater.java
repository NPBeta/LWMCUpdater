package org.npbeta.OldwangMC;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.ThreadSafeProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;


//        updater.setRepositoryUrl("https://gitee.com/npbeta/OldwangMC.git");
//        updater.setRepositoryUrl("https://gitee.com/willbeahero/IOTGate.git");

public class Updater {

    private final static String username = "admin@npbeta.com";
    private final static String password = "8TR$/wE6RY3vSdk";
    private final static File pathname = new File("LwMC");
    private final static CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username,
            password);

    Task<Void> CloneRepo = new Task<Void>() {
        @Override
        protected Void call() {
            try {
                final int[] Progress = { 0,0 };
                String repositoryUrl = "https://gitee.com/npbeta/OldwangMC.git";
                String branch = "release";
                Git.cloneRepository().setCredentialsProvider(credentialsProvider).setURI(repositoryUrl)
                        .setBranch(branch).setDirectory(pathname)
                        .setProgressMonitor(new ThreadSafeProgressMonitor(new ProgressMonitor() {
                            @Override
                            public void start(int totalTasks) {
                                System.out.println("Starting work on " + totalTasks + " tasks");
                                //TODO: Add Total Number for Tasks Count Indicator
                            }

                            @Override
                            public void beginTask(String title, int totalWork) {
                                System.out.println("Start " + title + ": " + totalWork);
                                Progress[0] = totalWork;
                                //TODO: Add TaskName Indicator
                            }

                            @Override
                            public void update(int completed) {
                                System.out.print(completed + "-");
                                Progress[1] += 1;
                                updateProgress(Progress[1],Progress[0]);
                            }

                            @Override
                            public void endTask() {
                                System.out.println("Done");
                                Progress[1] = 0;
                                //TODO: Add Done Number for Tasks Count Indicator
                            }

                            @Override
                            public boolean isCancelled() {
                                return false;
                            }
                        }))
                        .call();
                Git.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    Task<String[]> CheckUpdate = new Task<String[]>() {
        @Override
        protected String[] call() {
            String[] checkResult = new String[3];
            try {
                RevCommit revCommit = Git.open(pathname).log().setMaxCount(1).call().iterator().next();
                String LatestVersion = Git.open(pathname).fetch().setCredentialsProvider(credentialsProvider)
                        .setDryRun(true).call().getAdvertisedRef("HEAD").getObjectId().getName();
                checkResult[1] = String.valueOf(revCommit.getCommitTime());
                checkResult[2] = revCommit.getFullMessage();
                Git.shutdown();
                if (LatestVersion.equals(revCommit.getName())) {
                    checkResult[0] = "true";
                } else {
                    checkResult[0] = "false";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return checkResult;
        }
    };

    Task<Boolean> PullRepo = new Task<Boolean>() {
        @Override
        protected Boolean call() {
            PullResult pullResult;
            try {
                pullResult = Git.open(pathname).pull().setCredentialsProvider(credentialsProvider).call();
                Git.shutdown();
                return pullResult.isSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    Task<Boolean> OpenExistingRepo = new Task<Boolean>() {
        @Override
        protected Boolean call() {
            try {
                Git.open(new File("LwMC"));
                Git.shutdown();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    };

    public static boolean openExistingRepo() {
        return false;
    }

    public static boolean checkUpdateRepo() {
        try {
            Updater updater = new Updater();
            Thread thread = new Thread(updater.CheckUpdate);
            thread.start();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

