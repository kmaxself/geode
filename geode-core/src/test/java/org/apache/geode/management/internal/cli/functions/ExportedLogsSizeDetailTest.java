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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public class ExportedLogsSizeDetailTest {
  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public final void testExportedLogsSizeDetailConstructor() {
    ExportedLogsSizeDetail sizeDetail =
        new ExportedLogsSizeDetail(1L, 2L);
    assertThat(sizeDetail).isNotNull();
    assertThat(sizeDetail.getLogsSize()).isEqualTo(1L);
    assertThat(sizeDetail.getAvailableDisk()).isEqualTo(2L);
  }

  @Test
  public final void testExportedLogsSizeDetail0ArgConstructor() {
    ExportedLogsSizeDetail sizeDetail = new ExportedLogsSizeDetail();
    assertThat(sizeDetail).isNotNull();
    assertThat(sizeDetail.getLogsSize()).isEqualTo(0L);
    assertThat(sizeDetail.getAvailableDisk()).isEqualTo(0L);
  }

  @Test
  public void equals_returnsTrueForTwoInstancesWithTheSameFieldValues() throws Exception {
    ExportedLogsSizeDetail sizeDetail1 =
        new ExportedLogsSizeDetail(3L, 4L);
    ExportedLogsSizeDetail sizeDetail2 =
        new ExportedLogsSizeDetail(3L, 4L);
    assertThat(sizeDetail1.equals(sizeDetail1)).isTrue();
    assertThat(sizeDetail1.equals(sizeDetail2)).isTrue();
    assertThat(sizeDetail2.equals(sizeDetail1)).isTrue();
  }

  @Test
  public void equals_returnsFalseWhenLogsSizeDiffers() throws Exception {
    ExportedLogsSizeDetail sizeDetail1 =
        new ExportedLogsSizeDetail(3L, 4L);
    ExportedLogsSizeDetail sizeDetail2 =
        new ExportedLogsSizeDetail(4L, 4L);
    assertThat(sizeDetail1.equals(sizeDetail2)).isFalse();
    assertThat(sizeDetail2.equals(sizeDetail1)).isFalse();
  }

  @Test
  public void equals_returnsFalseWhenAvailableDiskDiffers() throws Exception {
    ExportedLogsSizeDetail sizeDetail1 =
        new ExportedLogsSizeDetail(3L, 4L);
    ExportedLogsSizeDetail sizeDetail2 =
        new ExportedLogsSizeDetail(3L, 3L);
    assertThat(sizeDetail1.equals(sizeDetail2)).isFalse();
    assertThat(sizeDetail2.equals(sizeDetail1)).isFalse();
  }

  @Test
  public void equals_returnsFalseForComparisonWithNullObject() throws Exception {
    ExportedLogsSizeDetail sizeDetail1 =
        new ExportedLogsSizeDetail(3L, 4L);
    ExportedLogsSizeDetail sizeDetail2 = null;
//        new ExportedLogsSizeDetail(3L, 4L);
    assertThat(sizeDetail1.equals(sizeDetail2)).isFalse();
  }

  @Test
  public final void testClassInequality() {
    ExportedLogsSizeDetail sizeDeatai1 =
        new ExportedLogsSizeDetail(5L, 6L);
    String sizeDetail2 = sizeDeatai1.toString();
    assertThat(sizeDeatai1.equals(sizeDetail2)).isFalse();
    assertThat(sizeDetail2.equals(sizeDeatai1)).isFalse();
  }

  @Test
  public void testHashCode() throws Exception {
    ExportedLogsSizeDetail sizeDetail1 = new ExportedLogsSizeDetail();
    ExportedLogsSizeDetail sizeDetail2 =
        new ExportedLogsSizeDetail(11L, 12L);
    ExportedLogsSizeDetail sizeDetail3 =
        new ExportedLogsSizeDetail(12L, 11L);

    assertThat(sizeDetail1.hashCode()).isNotEqualTo(sizeDetail2.hashCode());
    assertThat(sizeDetail1.hashCode()).isNotEqualTo(sizeDetail3.hashCode());
    assertThat(sizeDetail2.hashCode()).isNotEqualTo(sizeDetail3.hashCode());

    assertThat(sizeDetail1.hashCode()).isEqualTo(0);
    assertThat(sizeDetail2.hashCode()).isEqualTo(353);
    assertThat(sizeDetail3.hashCode()).isEqualTo(383);

  }

  @Test
  public void deserialization_setsFieldsToOriginalUnserializedValues() throws Exception {
    ExportedLogsSizeDetail sizeDetail =
        new ExportedLogsSizeDetail(3L, 4L);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutput out = new DataOutputStream(baos);
    sizeDetail.toData(out);
    ExportedLogsSizeDetail sizeDetailIn = new ExportedLogsSizeDetail();
    sizeDetailIn.fromData(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

    assertThat(sizeDetailIn).isEqualTo(sizeDetail);
  }

  @Test
  public void testToString() throws Exception {
    ExportedLogsSizeDetail sizeDetail =
        new ExportedLogsSizeDetail(7L, 8L);
    assertThat(sizeDetail.toString()).isEqualTo("[logsSize: 7, availableDisk: 8]");

  }

}