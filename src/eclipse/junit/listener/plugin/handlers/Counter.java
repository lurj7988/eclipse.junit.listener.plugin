/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eclipse.junit.listener.plugin.handlers;

public class Counter
{

    private String type;

    private int missed;

    private int covered;

    private Counter() {
        // NOOP - Here to allow binding
    }

    public Counter(final String type, final int covered, final int missed) {
        this.type = type;
        this.covered = covered;
        this.missed = missed;
    }

    public String getType() {
        return SafeReturn.safe(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMissed(int missed) {
        this.missed = missed;
    }

    public int getMissed() {
        return missed;
    }

    public int getCovered() {
        return covered;
    }

    public void setCovered(int covered) {
        this.covered = covered;
    }

    public int getTotal() {
        return covered + missed;
    }

    public double getPercentage() {
        return (covered / (float) getTotal()) * 100;
    }

    @Override
    public String toString() {
        return "Counter{" + "type='" + type + '\'' + ", missed=" + missed + ", covered=" + covered + '}';
    }
}
