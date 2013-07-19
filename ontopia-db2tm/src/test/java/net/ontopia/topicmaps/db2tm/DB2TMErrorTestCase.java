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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.ontopia.utils.FileUtils;
import net.ontopia.utils.TestFileUtils;
import net.ontopia.utils.OntopiaRuntimeException;
import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.db2tm.*;
import net.ontopia.topicmaps.utils.ImportExportUtils;
import net.ontopia.topicmaps.xml.CanonicalXTMWriter;
import net.ontopia.topicmaps.utils.ltm.LTMTopicMapWriter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DB2TMErrorTestCase {

  private static final boolean DEBUG_LTM = false; // keep off in CVS
  protected boolean recanonicalizeSource = true;

  private final static String testdataDirectory = "db2tm";

  /**
   * @return The test cases generated by this.
   */
  @Parameters
  public static List generateTests() throws IOException {
    TestFileUtils.transferTestInputDirectory(testdataDirectory + "/error");
    return TestFileUtils.getTestInputFiles(testdataDirectory, "error", ".xml");
  }

  // --- Test case class

    private String base;
    private String filename;

    public DB2TMErrorTestCase(String root, String filename) {
      this.filename = filename;
      this.base = TestFileUtils.getTestdataOutputDirectory() + testdataDirectory;
    }

    @Test
    public void testFile() throws IOException {
      String name = filename.substring(0, filename.length() - ".xml".length());

      // Path to the config file.
      String cfg = TestFileUtils.getTransferredTestInputFile(testdataDirectory, "error", filename).getPath();

      // Path to the topic map seed.
      String in = TestFileUtils.getTestInputFile(testdataDirectory, "error", name + ".ltm");
      String default_in = TestFileUtils.getTestInputFile(testdataDirectory, "error", "default.ltm");
      
      // Import the topic map seed.
      TopicMapIF topicmap;
      URL infile = getClass().getClassLoader().getResource(in.substring("classpath:".length()));
      if (infile != null)
        topicmap = ImportExportUtils.getReader(in).read();
      else
        topicmap = ImportExportUtils.getReader(default_in).read();
               
      try {
        // Extend the topic map seed with the the config file.
        DB2TM.add(cfg, topicmap);
        
        Assert.fail("The conversion from " + cfg
          + " executed without error. It should have caused an error.");
      } catch (DB2TMException e) {
      } catch (OntopiaRuntimeException e) {
      }
    }

}
