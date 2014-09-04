/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.slider.agent.freezethaw

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.fs.FileSystem as HadoopFS
import org.apache.hadoop.fs.Path
import org.apache.hadoop.yarn.api.records.ApplicationReport
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.slider.agent.AgentMiniClusterTestBase
import org.apache.slider.client.SliderClient
import org.apache.slider.common.tools.SliderUtils
import org.apache.slider.core.main.ServiceLauncher
import org.junit.Test

/**
 * stop and start an AM
 */
@CompileStatic
@Slf4j

class TestFreezeThawMasterlessAM extends AgentMiniClusterTestBase {

  File getConfDirFile() {
    return new File("target/TestFreezeThawMasterlessAM/conf")
  }

  @Override
  String getConfDir() {
    return confDirFile.toURI().toString()
  }

  @Test
  public void testFreezeThawMasterlessAM() throws Throwable {
    YarnConfiguration conf = configuration
    String clustername = createMiniCluster("", conf, 1, 1, 1, true, false)
    
    describe "create a masterless AM, stop it, start it"
    //copy the confdir somewhere
    Path resConfPath = new Path(resourceConfDirURI)
    Path tempConfPath = new Path(confDir)
    SliderUtils.copyDirectory(conf, resConfPath, tempConfPath, null)


    ServiceLauncher<SliderClient> launcher = createStandaloneAM(
        clustername,
        true,
        true)
    SliderClient sliderClient = launcher.service
    addToTeardown(sliderClient);

    assert 0 == clusterActionFreeze(sliderClient, clustername)
    

    // here we do something devious: delete our copy of the configuration
    // this makes sure the remote config gets picked up
    HadoopFS localFS = HadoopFS.get(tempConfPath.toUri(), conf)
    localFS.delete(tempConfPath,true)
    
    //now start the cluster
    ServiceLauncher launcher2 = thawCluster(clustername, [], true);
    SliderClient newCluster = launcher2.service
    addToTeardown(newCluster);

//    ApplicationReport report = waitForClusterLive(newCluster)
    newCluster.getClusterDescription(clustername);
    //stop
    assert 0 == clusterActionFreeze(sliderClient, clustername)

    //stop again
    assert 0 == clusterActionFreeze(sliderClient, clustername)

  }

}
