package org.npbeta.OldwangMC;

import javafx.concurrent.Task;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

public class Updater {

    private String repositoryUrl;

    Task<Number> Clone = new Task<Number>() {
        @Override
        protected Number call() {
            try {
                final int[] Progress = { 0,0 };
                CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("admin@npbeta.com",
                        "8TR$/wE6RY3vSdk");
                Git.cloneRepository().setCredentialsProvider(credentialsProvider).setURI(repositoryUrl)
                        .setBranch("release").setDirectory(new File("LwMC"))
                        .setProgressMonitor(new ProgressMonitor() {
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
                        })
                        .call();
                Git.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;

    }

    public static boolean openExistingRepo() {
        try {
            Git.open(new File("LwMC"));
            Git.shutdown();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

