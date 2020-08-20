/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.test.functions.federated;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.sysds.common.Types;
import org.apache.sysds.runtime.controlprogram.federated.*;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestUtils;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@net.jcip.annotations.NotThreadSafe
public class FederatedNegativeTest {
	protected static Logger log = Logger.getLogger(FederatedNegativeTest.class);

	static {
		Logger.getLogger("org.apache.sysds").setLevel(Level.OFF);
	}

	@Test
	public void NegativeTest1() {
        int port = AutomatedTestBase.getRandomAvailablePort();
		String[] args = {"-w", Integer.toString(port)};
        Thread t = AutomatedTestBase.startLocalFedWorkerWithArgs(args);
		Map<FederatedRange, FederatedData> fedMap = new HashMap<>();
		FederatedRange r = new FederatedRange(new long[]{0,0}, new long[]{1,1});
		FederatedData d = new FederatedData(
				Types.DataType.SCALAR,
				new InetSocketAddress("localhost", port),
				"Nowhere");
		fedMap.put(r,d);
		FederationMap fedM = new FederationMap(fedMap);
		FederatedRequest fr = new FederatedRequest(FederatedRequest.RequestType.GET_VAR);
		Future<FederatedResponse>[] res = fedM.execute(0, fr);
		try {
			FederatedResponse fres = res[0].get();
			assertFalse(fres.isSuccessful());
			assertTrue(fres.getErrorMessage().contains("Variable 0 does not exist at federated worker"));

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		TestUtils.shutdownThread(t);
	}

}
