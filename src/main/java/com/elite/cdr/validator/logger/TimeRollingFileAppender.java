package com.elite.cdr.validator.logger;

import org.apache.log4j.RollingFileAppender;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeRollingFileAppender extends RollingFileAppender {

  @Override
  public void setFile(String file) {
    super.setFile(prependDate(file));
  }

  private static String prependDate(String filename) {
    return filename.replace(
        "{}",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")) + "");
  }
}
