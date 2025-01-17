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

package org.apache.sysds.test.component.compress.colgroup.scheme;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sysds.runtime.compress.CompressionSettings;
import org.apache.sysds.runtime.compress.CompressionSettingsBuilder;
import org.apache.sysds.runtime.compress.colgroup.AColGroup;
import org.apache.sysds.runtime.compress.colgroup.AColGroup.CompressionType;
import org.apache.sysds.runtime.compress.colgroup.ColGroupDDC;
import org.apache.sysds.runtime.compress.colgroup.ColGroupFactory;
import org.apache.sysds.runtime.compress.colgroup.indexes.ColIndexFactory;
import org.apache.sysds.runtime.compress.colgroup.indexes.IColIndex;
import org.apache.sysds.runtime.compress.colgroup.indexes.RangeIndex;
import org.apache.sysds.runtime.compress.colgroup.scheme.DDCScheme;
import org.apache.sysds.runtime.compress.colgroup.scheme.ICLAScheme;
import org.apache.sysds.runtime.compress.estim.ComEstExact;
import org.apache.sysds.runtime.compress.estim.CompressedSizeInfo;
import org.apache.sysds.runtime.compress.estim.CompressedSizeInfoColGroup;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.test.TestUtils;
import org.junit.Test;

public class CLADDCSchemeTest {
	protected final Log LOG = LogFactory.getLog(CLADDCSchemeTest.class.getName());

	final MatrixBlock src;
	final AColGroup g;
	final ICLAScheme sh;

	public CLADDCSchemeTest() {
		src = TestUtils.round(TestUtils.generateTestMatrixBlock(1023, 3, 0, 3, 0.9, 7));

		IColIndex colIndexes = ColIndexFactory.create(3);
		CompressionSettings cs = new CompressionSettingsBuilder().setSamplingRatio(1.0)
			.setValidCompressions(EnumSet.of(CompressionType.DDC)).create();
		final CompressedSizeInfoColGroup cgi = new ComEstExact(src, cs).getColGroupInfo(colIndexes);

		final CompressedSizeInfo csi = new CompressedSizeInfo(cgi);
		final List<AColGroup> groups = ColGroupFactory.compressColGroups(src, csi, cs, 1);
		g = groups.get(0);
		sh = g.getCompressionScheme();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidColumnApply() {
		sh.encode(null, ColIndexFactory.create(new int[] {1, 2}));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidColumnApply_2() {
		sh.encode(null, ColIndexFactory.create(new int[] {1, 2, 5, 5}));
	}

	@Test(expected = NullPointerException.class)
	public void testNull() {
		sh.encode(null, null);
	}

	@Test
	public void testEncode() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 3, 0, 3, 0.9, 7))) != null);
	}

	@Test
	public void testEncode_sparse() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 100, 0, 3, 0.01, 7))) != null);
	}

	@Test
	public void testEncodeSparseDifferentColumns() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 100, 0, 3, 0.01, 7)),
			ColIndexFactory.create(new int[] {13, 16, 30})) != null);
	}

	@Test
	public void testEncodeSparseDifferentColumns2() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 100, 0, 3, 0.01, 7)),
			ColIndexFactory.create(new int[] {15, 16, 99})) != null);
	}

	@Test
	public void testEncodeSparseDifferentColumns3() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 100, 0, 3, 0.01, 7)),
			ColIndexFactory.create(new int[] {15, 86, 99})) != null);
	}

	@Test
	public void testEncodeDenseDifferentColumns() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 100, 0, 3, 0.86, 7)),
			ColIndexFactory.create(new int[] {13, 16, 30})) != null);
	}

	@Test
	public void testEncodeDenseDifferentColumns2() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 100, 0, 3, 0.86, 7)),
			ColIndexFactory.create(new int[] {15, 16, 99})) != null);
	}

	@Test
	public void testEncodeDenseDifferentColumns3() {
		assertTrue(sh.encode(TestUtils.round(TestUtils.generateTestMatrixBlock(2, 100, 0, 3, 0.86, 7)),
			ColIndexFactory.create(new int[] {15, 86, 99})) != null);
	}

	@Test
	public void testEncodeFromColumns() {
		try {

			DDCScheme s = DDCScheme.create(new RangeIndex(2));
			MatrixBlock m = TestUtils.round(TestUtils.generateTestMatrixBlock(50, 3, 0, 2, 0.9, 7));
			s.update(m);
			AColGroup g = s.encode(m);
			assertTrue(g instanceof ColGroupDDC);
		}

		catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
