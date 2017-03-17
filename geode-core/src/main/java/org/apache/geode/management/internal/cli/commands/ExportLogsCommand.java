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

package org.apache.geode.management.internal.cli.commands;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.geode.cache.Region;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.internal.cache.GemFireCacheImpl;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.management.cli.CliMetaData;
import org.apache.geode.management.cli.ConverterHint;
import org.apache.geode.management.cli.Result;
import org.apache.geode.management.internal.cli.AbstractCliAroundInterceptor;
import org.apache.geode.management.internal.cli.CliUtil;
import org.apache.geode.management.internal.cli.GfshParseResult;
import org.apache.geode.management.internal.cli.functions.ExportLogsFunction;
import org.apache.geode.management.internal.cli.i18n.CliStrings;
import org.apache.geode.management.internal.cli.result.ResultBuilder;
import org.apache.geode.management.internal.cli.shell.Gfsh;
import org.apache.geode.management.internal.cli.util.ExportLogsCacheWriter;
import org.apache.geode.management.internal.configuration.utils.ZipUtils;
import org.apache.geode.management.internal.security.ResourceOperation;
import org.apache.geode.security.ResourcePermission;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExportLogsCommand implements CommandMarker {

  private static final Logger logger = LogService.getLogger();

  public static final String FORMAT = "yyyy/MM/dd/HH/mm/ss/SSS/z";
  public static final String ONLY_DATE_FORMAT = "yyyy/MM/dd";

  private GemFireCacheImpl cache;

  public ExportLogsCommand() {
    cache = GemFireCacheImpl.getInstance();
  }

  @CliCommand(value = CliStrings.EXPORT_LOGS, help = CliStrings.EXPORT_LOGS__HELP)
  @CliMetaData(shellOnly = false, isFileDownloadOverHttp = true,
      interceptor = "org.apache.geode.management.internal.cli.commands.ExportLogsInterceptor",
      relatedTopic = {CliStrings.TOPIC_GEODE_SERVER, CliStrings.TOPIC_GEODE_DEBUG_UTIL})
  @ResourceOperation(resource = ResourcePermission.Resource.CLUSTER,
      operation = ResourcePermission.Operation.READ)
  public Result exportLogs(
      @CliOption(key = CliStrings.EXPORT_LOGS__DIR, help = CliStrings.EXPORT_LOGS__DIR__HELP,
          mandatory = false) String dirName,
      @CliOption(key = CliStrings.EXPORT_LOGS__GROUP,
          unspecifiedDefaultValue = CliMetaData.ANNOTATION_NULL_VALUE,
          optionContext = ConverterHint.MEMBERGROUP,
          help = CliStrings.EXPORT_LOGS__GROUP__HELP) String[] groups,
      @CliOption(key = CliStrings.EXPORT_LOGS__MEMBER,
          unspecifiedDefaultValue = CliMetaData.ANNOTATION_NULL_VALUE,
          optionContext = ConverterHint.ALL_MEMBER_IDNAME,
          help = CliStrings.EXPORT_LOGS__MEMBER__HELP) String[] memberIds,
      @CliOption(key = CliStrings.EXPORT_LOGS__LOGLEVEL,
          unspecifiedDefaultValue = LogService.DEFAULT_LOG_LEVEL,
          optionContext = ConverterHint.LOG_LEVEL,
          help = CliStrings.EXPORT_LOGS__LOGLEVEL__HELP) String logLevel,
      @CliOption(key = CliStrings.EXPORT_LOGS__UPTO_LOGLEVEL, unspecifiedDefaultValue = "false",
          help = CliStrings.EXPORT_LOGS__UPTO_LOGLEVEL__HELP) boolean onlyLogLevel,
      @CliOption(key = CliStrings.EXPORT_LOGS__MERGELOG, unspecifiedDefaultValue = "false",
          help = CliStrings.EXPORT_LOGS__MERGELOG__HELP) boolean mergeLog,
      @CliOption(key = CliStrings.EXPORT_LOGS__STARTTIME,
          unspecifiedDefaultValue = CliMetaData.ANNOTATION_NULL_VALUE,
          help = CliStrings.EXPORT_LOGS__STARTTIME__HELP) String start,
      @CliOption(key = CliStrings.EXPORT_LOGS__ENDTIME,
          unspecifiedDefaultValue = CliMetaData.ANNOTATION_NULL_VALUE,
          help = CliStrings.EXPORT_LOGS__ENDTIME__HELP) String end,
      @CliOption(key = CliStrings.EXPORT_LOGS__LOGSONLY, unspecifiedDefaultValue = "false",
          specifiedDefaultValue = "true",
          help = CliStrings.EXPORT_LOGS__LOGSONLY__HELP) boolean logsOnly,
      @CliOption(key = CliStrings.EXPORT_LOGS__STATSONLY, unspecifiedDefaultValue = "false",
          specifiedDefaultValue = "true",
          help = CliStrings.EXPORT_LOGS__STATSONLY__HELP) boolean statsOnly) {
    Result result = null;
    GemFireCacheImpl cache = this.cache;
    if (cache == null || cache.isClosed()) {
      new Exception("KEN: cache is null").printStackTrace();
      cache = GemFireCacheImpl.getInstance();
    } else {
      new Exception("KEN: cache has been created").printStackTrace();
    }
    try {
      Set<DistributedMember> targetMembers =
          CliUtil.findMembersIncludingLocators(groups, memberIds);

      if (targetMembers.isEmpty()) {
        return ResultBuilder.createUserErrorResult(CliStrings.NO_MEMBERS_FOUND_MESSAGE);
      }

      Map<String, Path> zipFilesFromMembers = new HashMap<>();
      for (DistributedMember server : targetMembers) {
        Region region = ExportLogsFunction.createOrGetExistingExportLogsRegion(true, cache);

        ExportLogsCacheWriter cacheWriter =
            (ExportLogsCacheWriter) region.getAttributes().getCacheWriter();

        cacheWriter.startFile(server.getName());

        CliUtil.executeFunction(new ExportLogsFunction(),
            new ExportLogsFunction.Args(start, end, logLevel, onlyLogLevel, logsOnly, statsOnly),
            server).getResult();
        Path zipFile = cacheWriter.endFile();
        ExportLogsFunction.destroyExportLogsRegion(cache);

        // only put the zipfile in the map if it is not null
        if (zipFile != null) {
          logger.info("Received zip file from member {}: {}", server.getId(), zipFile);
          zipFilesFromMembers.put(server.getId(), zipFile);
        }
      }

      if (zipFilesFromMembers.isEmpty()) {
        return ResultBuilder.createUserErrorResult("No files to be exported.");
      }

      Path tempDir = Files.createTempDirectory("exportedLogs");
      // make sure the directory is created, so that even if there is no files unzipped to this dir,
      // we can
      // still zip it and send an empty zip file back to the client
      Path exportedLogsDir = tempDir.resolve("exportedLogs");
      FileUtils.forceMkdir(exportedLogsDir.toFile());

      for (Path zipFile : zipFilesFromMembers.values()) {
        Path unzippedMemberDir =
            exportedLogsDir.resolve(zipFile.getFileName().toString().replace(".zip", ""));
        ZipUtils.unzip(zipFile.toAbsolutePath().toString(), unzippedMemberDir.toString());
        FileUtils.deleteQuietly(zipFile.toFile());
      }

      Path workingDir = Paths.get(System.getProperty("user.dir"));
      Path exportedLogsZipFile = workingDir
          .resolve("exportedLogs_" + System.currentTimeMillis() + ".zip").toAbsolutePath();

      logger.info("Zipping into: " + exportedLogsZipFile.toString());
      ZipUtils.zipDirectory(exportedLogsDir, exportedLogsZipFile);
      FileUtils.deleteDirectory(tempDir.toFile());

      // TODO:GEODE-2420: warn user if exportedLogsZipFile size > threshold
      if (isOverDiskSpaceThreshold()) {
        // append warning to exportedLogsZipFile.toString()??
      }

      result = ResultBuilder.createInfoResult(exportedLogsZipFile.toString());
    } catch (Exception ex) {
      logger.error(ex, ex);
      result = ResultBuilder.createGemFireErrorResult(ex.getMessage());
    } finally {
      ExportLogsFunction.destroyExportLogsRegion(cache);
    }
    logger.debug("Exporting logs returning = {}", result);
    return result;
  }

  protected static Gfsh getGfsh() {
    return Gfsh.getCurrentInstance();
  }

  boolean isOverDiskSpaceThreshold() {
    return false;
  }

}
