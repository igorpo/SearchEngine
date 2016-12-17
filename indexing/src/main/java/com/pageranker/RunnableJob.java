package com.pageranker;

import java.io.IOException;

/**
 * An interface which all runnable map reduce jobs must adhere to. This is so
 * multiple different classes of jobs can all share a common backbone.
 */
public interface RunnableJob {

    /**
     * Runs the job.
     * @param inputPath The input path for the job.
     * @param outputPath The output path for the job.
     * @throws IOException
     */
    public void run(String inputPath, String outputPath)
            throws IOException;

}
