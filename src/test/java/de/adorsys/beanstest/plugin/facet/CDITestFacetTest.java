/**
 * Copyright (C) 2012 Christian Brandenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adorsys.beanstest.plugin.facet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import javax.inject.Inject;

import org.jboss.forge.shell.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.adorsys.beanstest.ForgeTestCommons;
import de.adorsys.beanstest.plugin.BeanstestConfiguration;
import de.adorsys.beanstest.plugin.SimpleRunner;

@RunWith(SimpleRunner.class)
public class CDITestFacetTest {
    private static final String TESTPROJECTNAME = "CDITestFacetTest";
    private static final String TESTPACKAGENAME = "de.adorsys.testproject";

    @Inject
    Shell shell;

    @Inject
    ForgeTestCommons forgeTestCommons;

    @Before
    public void init() throws Exception {
        forgeTestCommons.init(TESTPROJECTNAME, TESTPACKAGENAME, false);
    }

    @After
    public void destroy() throws Exception {
        forgeTestCommons.cleanUp();
    }

    @Test
    public void testSetup() throws Exception {
        forgeTestCommons.setNewInput("\n10\n");

        shell.execute("beanstest setup");

        // TODO test pom

        // test beans.xml created
        assertTrue("test beans.xml missing", new File("target/" + TESTPROJECTNAME + "/src/test/resources/META-INF/beans.xml").exists());

        // test SimpleRunner
        String simpleRunnerPath = TESTPACKAGENAME.replaceAll("\\.", "/") + "/beanstest/SimpleRunner.java";
        assertTrue(simpleRunnerPath + " missing", new File("target/" + TESTPROJECTNAME + "/src/test/java/" + simpleRunnerPath).exists());
    }

    @Test
    public void testMockMissingScopes() throws Exception {
        forgeTestCommons.setNewInput("\n10\n");

        shell.execute("beanstest setup");
        
        shell.execute("beanstest mock-scopes");
 
        // test services/javax.enterprise.inject.spi.Extension created
        assertTrue("CDI extensions services file missing", new File("target/" 
                + TESTPROJECTNAME 
                + "/src/test/resources/META-INF/services/javax.enterprise.inject.spi.Extension").exists());

        // test SimpleRunner
        String simpleRunnerPath = TESTPACKAGENAME.replaceAll("\\.", "/") + "/beanstest/MockMissingScopesExtension.java";
        assertTrue(simpleRunnerPath + " missing", new File("target/" + TESTPROJECTNAME + "/src/test/java/" + simpleRunnerPath).exists());
    }
    
    @Test
    public void testMockMissingScopesExistingServices() throws Exception {
        forgeTestCommons.setNewInput("\n10\n");

        shell.execute("beanstest setup");
        
        new File("target/" 
                + TESTPROJECTNAME 
                + "/src/test/resources/META-INF/services").mkdirs();
        
        File f = new File("target/" 
                + TESTPROJECTNAME 
                + "/src/test/resources/META-INF/services/javax.enterprise.inject.spi.Extension");
        f.createNewFile();

        FileOutputStream fis = new FileOutputStream(f);
        fis.write("XXX\n".getBytes());
        fis.close();

        shell.execute("beanstest mock-scopes");
        
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        
        assertEquals("XXX", bufferedReader.readLine());
        assertEquals(TESTPACKAGENAME + BeanstestConfiguration.PACKAGESUFFIX + "." + "MockMissingScopesExtension", bufferedReader.readLine());
        bufferedReader.close();
    }

}
