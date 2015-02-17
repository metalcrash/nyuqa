/*
 * Copyright 2008-2011 Grant Ingersoll, Thomas Morton and Drew Farris
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -------------------
 * To purchase or learn more about Taming Text, by Grant Ingersoll, Thomas Morton and Drew Farris, visit
 * http://www.manning.com/ingersoll
 */

package nlp.qa.QUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

public class MemoryStatus {

  static String[] units = {
      "Bytes", "KBytes", "MBytes", "GBytes"
  };
  
  List<MemoryPoolMXBean> memoryBeans;
  
  public MemoryStatus() {
    memoryBeans = ManagementFactory.getMemoryPoolMXBeans();
  }
  
  public void dumpMemory(String title) {
    System.err.println("----------" + title + "----------");
    double total = 0;
    for (MemoryPoolMXBean m: memoryBeans) {
      MemoryUsage u = m.getUsage();
      double used = u.getUsed();
      total += used;
      System.err.println(m.getName() + " " + toMemoryString(used));
    }
    System.err.println("Total " + toMemoryString(total));
    System.err.println("---------------------------------");
  }
  
  private String toMemoryString(double bytes) {
    int pos = 0;
    while (bytes > 1024) {
      pos++;
      bytes = bytes / 1024;
    }
    return String.format("%2.2f", bytes) + " " + units[pos];
  }
  
  public static void main(String[] args) {
    MemoryStatus stat = new MemoryStatus();
    stat.dumpMemory("before");
    Object[] o = new Object[100000];
    for (int i=0; i < o.length; i++) {
      o[i] = new String("booga");
    }
    stat.dumpMemory("after");
  }
}
