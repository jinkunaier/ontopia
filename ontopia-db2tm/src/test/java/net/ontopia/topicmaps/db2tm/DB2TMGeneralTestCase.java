/*
 * #!
 * Ontopia DB2TM
 * #-
 * Copyright (C) 2001 - 2013 The Ontopia Project
 * #-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * !#
 */
package net.ontopia.topicmaps.db2tm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.utils.ImportExportUtils;
import net.ontopia.topicmaps.utils.ltm.LTMTopicMapWriter;
import net.ontopia.topicmaps.xml.CanonicalXTMWriter;
import net.ontopia.utils.TestFileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DB2TMGeneralTestCase {

  private static final boolean DEBUG_LTM = false; // keep off in CVS
  protected boolean recanonicalizeSource = true;

  private final static String testdataDirectory = "db2tm";

  private String base;
  private String filename;

  @Parameters
  public static List<String[]> generateTests() throws IOException {
    TestFileUtils.transferTestInputDirectory(testdataDirectory + "/in");
    return TestFileUtils.getTestInputFiles(testdataDirectory, "in", ".xml");
  }

  public DB2TMGeneralTestCase(String root, String filename) {
    this.filename = filename;
    this.base = TestFileUtils.getTestdataOutputDirectory() + testdataDirectory;
  }

  @Test
  public void testFile() throws IOException {
    TestFileUtils.verifyDirectory(base, "out");

    String name = filename.substring(0, filename.length() - 4);

    // Path to the config file.
    String cfg = TestFileUtils.getTransferredTestInputFile(testdataDirectory, "in", filename).getPath();

    // Path to the topic map seed.
    String in = TestFileUtils.getTestInputFile(testdataDirectory, "in", name + ".ltm");

    // Path to the cxtm version of the output topic map.
    File cxtm = TestFileUtils.getTestOutputFile(testdataDirectory, "out", name + ".cxtm");

    // Path to the baseline.
    String baseline = TestFileUtils.getTestInputFile(testdataDirectory, "baseline", name + ".cxtm");

    // Import the topic map seed.
    TopicMapIF topicmap = ImportExportUtils.getReader(in).read();

    // Extend the topic map seed with the the config file.
    DB2TM.add(cfg, topicmap);

    // Export the result topic map to ltm, for manual inspection purposes.
    if (DEBUG_LTM) {
      File ltm = TestFileUtils.getTestOutputFile(testdataDirectory, "out", name + ".ltm");
      new LTMTopicMapWriter(ltm).write(topicmap);
    }

    // Export the result topic map to cxtm
    new CanonicalXTMWriter(cxtm).write(topicmap);

    // Check that the cxtm output matches the baseline.
    Assert.assertTrue("The canonicalized conversion from " + filename
            + " does not match the baseline: " + cxtm + " " + baseline,
            TestFileUtils.compareFileToResource(cxtm, baseline));
  }
}
