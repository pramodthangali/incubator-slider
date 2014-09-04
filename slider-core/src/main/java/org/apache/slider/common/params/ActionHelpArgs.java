/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.slider.common.params;

import com.beust.jcommander.Parameters;

/**
 * The Help command
 */
@Parameters(commandNames = {SliderActions.ACTION_HELP, SliderActions.ACTION_USAGE},
            commandDescription = SliderActions.DESCRIBE_ACTION_HELP)
public class ActionHelpArgs extends AbstractActionArgs {
  @Override
  public String getActionName() {
    return SliderActions.ACTION_HELP;
  }

  /**
   * Get the min #of params expected
   * @return the min number of params in the {@link #parameters} field
   */
  @Override
  public int getMinParams() {
    return 0;
  }

  /**
   * This action does not need hadoop services
   * @return false
   */
  @Override
  public boolean getHadoopServicesRequired() {
    return false;
  }
}
