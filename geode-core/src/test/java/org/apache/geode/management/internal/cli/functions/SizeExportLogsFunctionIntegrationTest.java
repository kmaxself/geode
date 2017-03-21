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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.apache.geode.management.internal.cli.functions.SizeExportLogsFunction.Args;
import org.apache.commons.io.FileUtils;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.test.junit.categories.IntegrationTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@Category(IntegrationTest.class)
public class SizeExportLogsFunctionIntegrationTest {

  private File dir;
  private DistributedMember member;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public TestName testName = new TestName();

  @Before
  public void before() throws Exception {
    this.dir = this.temporaryFolder.getRoot();
    this.member = mock(DistributedMember.class);
  }

  @Test
  public void functionReturnsSize() throws Exception {
    File logFile = new File(this.dir, this.testName.getMethodName() + ".log");
    fillUpFile(logFile, (int) Math.pow(1024, 2));

    File statArchive = new File(this.dir, this.testName.getMethodName() + ".gfs");
    fillUpFile(statArchive, (int) Math.pow(1024, 2));

    SizeExportLogsFunction.Args args = new Args(null, null, null, false,
    false,  false);

    SizeExportLogsFunction function = new SizeExportLogsFunction();
    assertThat(function.estimateLogFileSize(this.member, logFile, statArchive, args)).isEqualTo(0);
  }

  private void fillUpFile(File file, int sizeInBytes) throws IOException {
    PrintWriter writer = new PrintWriter(file, "UTF-8");
    while (FileUtils.sizeOf(file) < sizeInBytes) {
      writer.println("this is a line of data in the file");
    }
    writer.close();
  }

}