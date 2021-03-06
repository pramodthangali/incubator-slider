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

package org.apache.slider.common.tools

import groovy.util.logging.Slf4j
import org.apache.hadoop.conf.Configuration
import org.apache.slider.client.SliderClient
import org.apache.slider.core.zk.ZKIntegration
import org.apache.slider.test.KeysForTests
import org.apache.slider.test.YarnZKMiniClusterTestBase
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.ZooDefs
import org.apache.zookeeper.data.Stat
import org.junit.Before
import org.junit.Test

@Slf4j

class TestZKIntegration extends YarnZKMiniClusterTestBase implements KeysForTests {

  @Before
  void createCluster() {
    Configuration conf = getConfiguration()
    createMicroZKCluster(conf)
  }

  @Test
  public void testIntegrationCreate() throws Throwable {
    assertHasZKCluster()
    ZKIntegration zki = createZKIntegrationInstance(ZKBinding, "cluster1", true, false, 5000)
    String userPath = ZKIntegration.mkSliderUserPath(USERNAME)
    Stat stat = zki.stat(userPath)
    assert stat != null
    log.info("User path $userPath has stat $stat")
  }

  @Test
  public void testListUserClustersWithoutAnyClusters() throws Throwable {
    assertHasZKCluster()

    ZKIntegration zki = createZKIntegrationInstance(ZKBinding, "", true, false, 5000)
    String userPath = ZKIntegration.mkSliderUserPath(USERNAME)
    List<String> clusters = zki.clusters
    assert clusters.empty
  }

  @Test
  public void testListUserClustersWithOneCluster() throws Throwable {
    assertHasZKCluster()

    ZKIntegration zki = createZKIntegrationInstance(ZKBinding, "", true, false, 5000)
    String userPath = ZKIntegration.mkSliderUserPath(USERNAME)
    String fullPath = zki.createPath(userPath, "/cluster-",
                                     ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                     CreateMode.EPHEMERAL_SEQUENTIAL)
    log.info("Ephemeral path $fullPath")
    List<String> clusters = zki.clusters
    assert clusters.size() == 1
    assert fullPath.endsWith(clusters[0])
  }

  @Test
  public void testListUserClustersWithTwoCluster() throws Throwable {
    ZKIntegration zki = createZKIntegrationInstance(ZKBinding, "", true, false, 5000)
    String userPath = ZKIntegration.mkSliderUserPath(USERNAME)
    String c1 = createEphemeralChild(zki, userPath)
    log.info("Ephemeral path $c1")
    String c2 = createEphemeralChild(zki, userPath)
    log.info("Ephemeral path $c2")
    List<String> clusters = zki.clusters
    assert clusters.size() == 2
    assert (c1.endsWith(clusters[0]) && c1.endsWith(clusters[1])) ||
           (c1.endsWith(clusters[1]) && c2.endsWith(clusters[0]))
  }

  @Test
  public void testCreateAndDeleteDefaultZKPath() throws Throwable {
    MockSliderClient client = new MockSliderClient()

    String path = client.createZookeeperNode("cl1", true)
    ZKIntegration zki = client.getLastZKIntegration()

    String zkPath = ZKIntegration.mkClusterPath(USERNAME, "cl1")
    assert zkPath == "/services/slider/users/" + USERNAME + "/cl1", "zkPath must be as expected"
    assert path == zkPath
    assert zki == null, "ZKIntegration should be null."
    zki = createZKIntegrationInstance(getZKBinding(), "cl1", true, false, 5000);
    assert false == zki.exists(zkPath), "zkPath should not exist"

    path = client.createZookeeperNode("cl1", false)
    zki = client.getLastZKIntegration()
    assert zkPath == "/services/slider/users/" + USERNAME + "/cl1", "zkPath must be as expected"
    assert path == zkPath
    assert true == zki.exists(zkPath), "zkPath must exist"
    zki.createPath(zkPath, "/cn", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    assert true == zki.exists(zkPath + "/cn"), "zkPath with child node must exist"
    client.deleteZookeeperNode("cl1")
    assert false == zki.exists(zkPath), "zkPath must not exist"

  }

  public String createEphemeralChild(ZKIntegration zki, String userPath) {
    return zki.createPath(userPath, "/cluster-",
                          ZooDefs.Ids.OPEN_ACL_UNSAFE,
                          CreateMode.EPHEMERAL_SEQUENTIAL)
  }

  class MockSliderClient extends SliderClient {
    private ZKIntegration zki;

    @Override
    public String getUsername() {
      return USERNAME
    }

    @Override
    protected ZKIntegration getZkClient(String clusterName, String user) {
      zki = createZKIntegrationInstance(getZKBinding(), "cl1", true, false, 5000)
      return zki;
    }

    @Override
    public synchronized Configuration getConfig() {
      Configuration conf = new Configuration();
      return conf;
    }

    public ZKIntegration getLastZKIntegration() {
      return zki
    }

  }

}
