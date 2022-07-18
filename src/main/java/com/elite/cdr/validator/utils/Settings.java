package com.elite.cdr.validator.utils;

import com.beust.jcommander.Parameter;

public class Settings {

  @Parameter(
      names = {"--env", "-en"},
      description = "Running Environment",
      arity = 1,
      required = true)
  public String runningEnv;

  @Parameter(
          names = {"--file", "-f"},
          description = "file input path",
          arity = 1,
          required = true)
  public String filePath;

  @Parameter(
          names = {"--prop", "-prop"},
          description = "file .propreties path",
          arity = 1,
          required = true)
  public String propretiesPath;

  public static final String LOCAL = "local";

  public Settings() {}

  public Settings(String runningEnv) {
    this.runningEnv = runningEnv;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getRunningEnv() {
    return runningEnv;
  }

  public String getPropretiesPath() {
    return propretiesPath;
  }
}
