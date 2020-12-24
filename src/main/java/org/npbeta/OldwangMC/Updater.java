package org.npbeta.OldwangMC;

import javafx.concurrent.Task;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

public class Updater {

    private final static String username = "xxxxxxxx";
    private final static String password = "xxxxxxxx";
    private final static File pathname = new File("LwMC");
    private final static CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username,
            password);

    Task<Number> CloneRepo = new Task<Number>() {
        @Override
        protected Number call() {
            try {
                final int[] Progress = { 0,0 };
                String repositoryUrl = "https://gitee.com/npbeta/OldwangMC.git";
                String branch = "release";
                Git.cloneRepository().setCredentialsProvider(credentialsProvider).setURI(repositoryUrl)
                        .setBranch(branch).setDirectory(pathname)
                        .setProgressMonitor(new ProgressMonitor() {
                            @Override
                            public void start(int totalTasks) {
                                System.out.println("Starting work on " + totalTasks + " tasks");
                            }

                            @Override
                            public void beginTask(String title, int totalWork) {
                                System.out.println("Start " + title + ": " + totalWork);
                                Progress[0] = totalWork;
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
                            }

                            @Override
                            public boolean isCancelled() {
                                return false;
                            }
                        })
                        .call();
                Git.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    };

    Task<Object> CheckUpdate = new Task<Object>() {
        @Override
        protected Object call() {
            Object checkResult = null;
            try {
                RevCommit revCommit = Git.open(pathname).log().setMaxCount(1).call().iterator().next();
                String LatestVersion = Git.open(pathname).fetch().setCredentialsProvider(credentialsProvider)
                        .setDryRun(true).call().getAdvertisedRef("refs/heads/release").getObjectId().getName();
                Git.shutdown();
                checkResult = LatestVersion.equals(revCommit.getName());
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
                e.printStackTrace();
                return false;
            }
        }
    };
}

