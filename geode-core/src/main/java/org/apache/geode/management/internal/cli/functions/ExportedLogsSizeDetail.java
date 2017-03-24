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

import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ExportedLogsSizeDetail implements DataSerializable {
  private long logsSize;
  private long availableDisk;

  // Used for deserialization only
  public ExportedLogsSizeDetail() {
    logsSize = 0;
    availableDisk = 0;
  }

  public ExportedLogsSizeDetail(long logsSize, long availableDisk) {
    this.logsSize = logsSize;
    this.availableDisk = availableDisk;
  }

  public long getAvailableDisk() {
    return availableDisk;
  }

  public long getLogsSize() {

    return logsSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ExportedLogsSizeDetail that = (ExportedLogsSizeDetail) o;

    if (logsSize != that.logsSize) {
      return false;
    }
    return availableDisk == that.availableDisk;
  }

  @Override
  public int hashCode() {
    int result = (int) (logsSize ^ (logsSize >>> 32));
    result = 31 * result + (int) (availableDisk ^ (availableDisk >>> 32));
    return result;
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeLong(logsSize, out);
    DataSerializer.writeLong(availableDisk, out);
  }

  @Override
  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    logsSize = DataSerializer.readLong(in);
    availableDisk = DataSerializer.readLong(in);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("[");
    result.append("logsSize: " + logsSize);
    result.append(", availableDisk: " + availableDisk);
    result.append("]");
    return result.toString();
  }
}
