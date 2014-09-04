/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.slider.providers.agent.application.metadata;

import org.apache.slider.providers.agent.AgentProviderService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.mockito.Mockito.doReturn;

/**
 *
 */
public class TestMetainfoParser {
  protected static final Logger log =
      LoggerFactory.getLogger(TestMetainfoParser.class);
  public static final String METAINFO_XML =
      "/org/apache/slider/providers/agent/application/metadata/metainfo.xml";

  @Test
  public void testParse() throws IOException {

    InputStream resStream = this.getClass().getResourceAsStream(
        METAINFO_XML);
    MetainfoParser parser = new MetainfoParser();
    Metainfo metainfo = parser.parse(resStream);
    Assert.assertNotNull(metainfo);
    Assert.assertNotNull(metainfo.getApplication());
    Application application = metainfo.getApplication();
    assert "STORM".equals(application.getName());
    assert 5 == application.getComponents().size();
    OSPackage pkg = application.getOSSpecifics().get(0).getPackages().get(0);
    assert "tarball".equals(pkg.getType());
    assert "files/apache-storm-0.9.1.2.1.1.0-237.tar.gz".equals(pkg.getName());
    boolean found = false;
    for (Component comp : application.getComponents()) {
      if (comp != null && comp.getName().equals("NIMBUS")) {
        found = true;
        Assert.assertEquals(0, comp.getComponentExports().size());
      }
      if (comp != null && comp.getName().equals("SUPERVISOR")) {
        Assert.assertEquals(1, comp.getComponentExports().size());
      }
    }
    assert found;
    Assert.assertEquals(0, application.getConfigFiles().size());
  }
}
