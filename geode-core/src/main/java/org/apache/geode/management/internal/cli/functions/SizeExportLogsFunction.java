/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.management.internal.cli.functions;

import org.apache.commons.lang.StringUtils;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.internal.InternalEntity;
import org.apache.geode.internal.cache.GemFireCacheImpl;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.management.internal.cli.util.LogFilter;
import org.apache.geode.management.internal.cli.util.LogSizer;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

public class SizeExportLogsFunction extends ExportLogsFunction implements Function, InternalEntity {
  private static final Logger LOGGER = LogService.getLogger();
  private static final long serialVersionUID = 1L;

  @Override
  public void execute(final FunctionContext context) {
    try {
      GemFireCacheImpl cache = GemFireCacheImpl.getInstance();
      DistributionConfig config = cache.getDistributedSystem().getConfig();
      Args args = (Args) context.getArguments();

      long estimatedSize = estimateLogFileSize(cache.getMyId(), config.getLogFile(), config.getStatisticArchiveFile(), args);

      if (estimatedSize == -1) {
        context.getResultSender().lastResult(null);
      } else {
        context.getResultSender().lastResult(Arrays.asList(new long[]{estimatedSize}));
      }

    } catch (Exception e) {
      LOGGER.error(e);
      context.getResultSender().sendException(e);
    }
  }

  long estimateLogFileSize(final DistributedMember member, final File logFile, final File statArchive, final Args args)
      throws ParseException, IOException {
    LOGGER.info("ExportLogsFunction started for member {}", member);

    File baseLogFile = null;
    File baseStatsFile = null;

    if (args.isIncludeLogs() && !logFile.toString().isEmpty()) {
      baseLogFile = logFile.getAbsoluteFile();
    }
    if (args.isIncludeStats() && !statArchive.toString().isEmpty()) {
      baseStatsFile = statArchive.getAbsoluteFile();
    }

    LogFilter logFilter = new LogFilter(args.getLogLevel(), args.isThisLogLevelOnly(),
        args.getStartTime(), args.getEndTime());

    long estimatedSize = new LogSizer(logFilter, baseLogFile, baseStatsFile).getFilteredSize();

    LOGGER.info("Estimated log file size: " + estimatedSize);

    return estimatedSize;
  }

//  @Override
//  public boolean isHA() {
//    return false;
//  }
//
//  public static class Args implements Serializable {
//    private LocalDateTime startTime;
//    private LocalDateTime endTime;
//    private Level logLevel;
//    private boolean thisLogLevelOnly;
//    private boolean includeLogs;
//    private boolean includeStats;
//
//    public Args(String startTime, String endTime, String logLevel, boolean logLevelOnly,
//                boolean logsOnly, boolean statsOnly) {
//      this.startTime = parseTime(startTime);
//      this.endTime = parseTime(endTime);
//
//      if (StringUtils.isBlank(logLevel)) {
//        this.logLevel = Level.INFO;
//      } else {
//        this.logLevel = Level.getLevel(logLevel.toUpperCase());
//      }
//      this.thisLogLevelOnly = logLevelOnly;
//
//      this.includeLogs = !statsOnly;
//      this.includeStats = !logsOnly;
//    }
//
//    public LocalDateTime getStartTime() {
//      return startTime;
//    }
//
//    public LocalDateTime getEndTime() {
//      return endTime;
//    }
//
//    public Level getLogLevel() {
//      return logLevel;
//    }
//
//    public boolean isThisLogLevelOnly() {
//      return thisLogLevelOnly;
//    }
//
//    public boolean isIncludeLogs() {
//      return includeLogs;
//    }
//
//    public boolean isIncludeStats() {
//      return includeStats;
//    }
//  }
//
//  public static LocalDateTime parseTime(String dateString) {
//    if (dateString == null) {
//      return null;
//    }
//
//    try {
//      SimpleDateFormat df = new SimpleDateFormat(ExportLogsCommand.FORMAT);
//      return df.parse(dateString).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//    } catch (ParseException e) {
//      try {
//        SimpleDateFormat df = new SimpleDateFormat(ExportLogsCommand.ONLY_DATE_FORMAT);
//        return df.parse(dateString).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//      } catch (ParseException e1) {
//        return null;
//      }
//    }
//  }
}
