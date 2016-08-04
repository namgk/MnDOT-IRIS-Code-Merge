/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms;

import java.util.Arrays;
import junit.framework.TestCase;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;
import us.mn.state.dot.tms.utils.SString;

/**
 * MultiString test cases
 * @author Michael Darter
 * @author Douglas Lau
 */
public class MultiStringTest extends TestCase {

	public MultiStringTest(String name) {
		super(name);
	}

	public void testAsText() {
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC[fo1]DEF").asText()));
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC [fo1]DEF").asText()));
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC [sc4]DEF").asText()));
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC [sc4]DEF[/sc]").asText()));
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC[jl4]DEF").asText()));
		assertTrue("ABC  DEF".equals(new MultiString(
			"ABC[nl]DEF").asText()));
		assertTrue("ABC   DEF".equals(new MultiString(
			"ABC[nl][nl]DEF").asText()));
		assertTrue("ABC  DEF".equals(new MultiString(
			"ABC[np]DEF").asText()));
	}

	public void testGetNumPages() {
		assertTrue(new MultiString("").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC[nl][nl]").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC[nl][nl]").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC[nl][np]").
			getNumPages() == 2);
		assertTrue(new MultiString("ABC[nl][np]DEF").
			getNumPages() == 2);
		assertTrue(new MultiString("ABC[nl][np]DEF[np]").
			getNumPages() == 3);
	}

	public void testGetLinesSinglePage() {
		checkGetLines("", new String[] { "" });
		checkGetLines("ABC", new String[] { "ABC" });
		checkGetLines("ABC[nl]DEF", new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl]DEF[nl]GHI",
			new String[] { "ABC", "DEF", "GHI" });
		checkGetLines("ABC[nl]DEF[nl]GHI[nl]JKL",
			new String[] { "ABC", "DEF", "GHI", "JKL" });
		checkGetLines("ABC[nl]", new String[] { "ABC" });
		checkGetLines("ABC[nl][nl]", new String[] { "ABC" });
		checkGetLines("ABC[nl][nl][nl]", new String[] { "ABC" });
		checkGetLines("[nl]DEF", new String[] { "", "DEF" });
		checkGetLines("[nl]DEF[nl]GHI",
			new String[] { "", "DEF", "GHI" });
		checkGetLines("[nl]DEF[nl]GHI[nl]JKL",
			new String[] { "", "DEF", "GHI", "JKL" });
		// new line tags with spacing
		checkGetLines("ABC[nl3]DEF",
			new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl]DEF[nl2]GHI",
			new String[] { "ABC", "DEF", "GHI" });
		// character spacing tags
		checkGetLines("ABC[sc3]DEF", new String[] { "ABC[sc3]DEF" });
		checkGetLines("ABC[sc3]DEF[/sc]GHI", new String[] {
			"ABC[sc3]DEF[/sc]GHI" });
	}

	public void testGetTextLineTags() {
		// check invalid tags
		checkGetLines("ABC[nl]D[j1x]E[j1x]F[nl]GHI",
			new String[] {"ABC", "DEF", "GHI"});
		// line justification tags
		checkGetLines("ABC[nl][jl2]D[jl3]E[jl4]F[nl]GHI",
			new String[] {"ABC", "[jl2]D[jl3]E[jl4]F", "GHI"});
		// Test for non-line tags being stripped
		checkGetLines("[cb8]ABC", new String[] { "ABC" });
		checkGetLines("[pb0,0,0]ABC", new String[] { "ABC" });
		checkGetLines("[cr255,0,0]ABC", new String[] { "ABC" });
		checkGetLines("[fo1]ABC", new String[] { "ABC" });
		checkGetLines("[g1,0,0]ABC", new String[] { "ABC" });
		checkGetLines("[jp3]ABC", new String[] { "ABC" });
		checkGetLines("[pt50o0]ABC", new String[] { "ABC" });
		checkGetLines("[tr0,0,5,5]ABC", new String[] { "ABC" });
		checkGetLines("ABC[nl][cb8]DEF", new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl][pb0,0,0]DEF",
			new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl][cr255,0,0]DEF",
			new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl][fo1]DEF", new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl][g1,0,0]DEF",
			new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl][jp3]DEF", new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl][pt50o0]DEF",
			new String[] { "ABC", "DEF" });
		checkGetLines("ABC[nl][tr0,0,5,5]DEF",
			new String[] { "ABC", "DEF" });
		// mixed line and non-line tags
		checkGetLines("[jp3]ABC[jl4]DEF",
			new String[] { "ABC[jl4]DEF" });
		checkGetLines("[jl2]ABC[nl]DEF[g1,1,1]",
			new String[] { "[jl2]ABC", "DEF" });
		checkGetLines("[cf0,0,0]ABC[nl]DE[sc5]F",
			new String[] { "[cf0,0,0]ABC", "DE[sc5]F" });
	}

	private void checkGetLines(String multi, String[] text) {
		for (int i = 1; i <= 6; i++) {
			String[] lns = new MultiString(multi).getLines(i);
			assertTrue(lns.length == i);
			for (int j = 0; j < i; j++) {
				if (j < text.length)
					assertTrue(lns[j].equals(text[j]));
				else
					assertTrue(lns[j].equals(""));
			}
		}
	}

	public void testGetLinesMultiPage() {
		checkGetLines("ABC[np]", 1, new String[] { "ABC" });
		checkGetLines("ABC[np]", 2, new String[] { "ABC", "" });
		checkGetLines("ABC[np]", 3, new String[] { "ABC", "", "" });
		checkGetLines("ABC[np][nl]", 1, new String[] { "ABC", "" });
		checkGetLines("ABC[np][nl]", 2,
			new String[] { "ABC", "", "", "" });
		checkGetLines("ABC[np][nl]", 3,
			new String[] { "ABC", "", "", "", "", "" });
		checkGetLines("ABC[np]DEF", 2,
			new String[] { "ABC", "", "DEF", "" });
		checkGetLines("ABC[nl][np]DEF", 2,
			new String[] { "ABC", "", "DEF", "" });
		checkGetLines("ABC[nl][np]DEF[np]GHI", 2,
			new String[] { "ABC", "", "DEF", "", "GHI", "" });
		checkGetLines("ABC[nl]DEF[np]GHI", 2,
			new String[] { "ABC", "DEF", "GHI", "" });
		checkGetLines("ABC[nl][nl]DEF[np][nl]", 3,
			new String[] {"ABC", "", "DEF", "", "", "" });
		checkGetLines("ABC[nl]DEF[np]GHI[nl]JKL", 2,
			new String[] { "ABC", "DEF", "GHI", "JKL" });
		checkGetLines("ABC[nl]DEF[np]GHI[nl][nl]JKL", 2,
			new String[] { "ABC", "DEF", "GHI", "" });
		checkGetLines("ABC[nl]DEF[np]GHI[nl]JKL", 3,
			new String[] { "ABC", "DEF", "", "GHI", "JKL", "" });
		checkGetLines("ABC[nl]DEF[np]GHI[nl][nl]JKL", 3,
			new String[] { "ABC", "DEF", "", "GHI", "", "JKL" });
		checkGetLines("ABC[nl]DEF[np]GHI[nl]JKL[nl]MNO", 2,
			new String[] { "ABC", "DEF", "GHI", "JKL" });
		checkGetLines("ABC[nl][np]", 3, new String[] { "ABC", "", "" });
		checkGetLines("ABC[nl][np]DEF", 3,
			new String[] { "ABC", "", "", "DEF", "", "" });
		checkGetLines("ABC[nl][np]DEF[np]", 3,
			new String[] { "ABC", "", "", "DEF", "", "" });
		checkGetLines("ABC[nl]DEF[np]GHI", 3,
			new String[] { "ABC", "DEF", "", "GHI", "", "" });
	}

	private void checkGetLines(String multi, int n_lines, String[] text) {
		assertTrue(Arrays.equals(new MultiString(multi).getLines(
			n_lines), text));
	}

	public void testEquals() {
		MultiString t1 = new MultiString("x");
		MultiString t2 = new MultiString("x");
		MultiString t3 = new MultiString("x");
		// equals null contract
		assertFalse(t1.equals(null));
		// reflexive
		assertTrue(t1.equals(t1));
		// symmetric
		assertTrue(t1.equals(t2) && t2.equals(t1));
		// transitive
		assertTrue(t1.equals(t2) && t2.equals(t3) && t1.equals(t3));
		// simple cases
		assertTrue(new MultiString("").equals(new MultiString("")));
		assertTrue(new MultiString("").equals(""));
		assertTrue(new MultiString("XXX").equals("XXX"));
		assertTrue(new MultiString("XXX").equals(new MultiString("XXX")));
		assertFalse(new MultiString("XXX").equals("XXY"));
		assertFalse(new MultiString("XXX").equals(new MultiString("XXY")));
		// verify normalization used
		assertTrue(new MultiString("[fo1]abc").equals("[fo1]abc"));
		assertTrue(new MultiString("[fo1]abc").equals(new MultiString("[fo1]abc")));
	}

	public void testNormalize() {
		assertTrue(MultiString.normalize("01234567890").
			equals("01234567890"));
		assertTrue(MultiString.normalize("ABC").
			equals("ABC"));
		assertTrue(MultiString.normalize("abc").
			equals("abc"));
		assertTrue(MultiString.normalize("DON'T").
			equals("DON'T"));
		assertTrue(MultiString.normalize("SPACE SPACE").
			equals("SPACE SPACE"));
		assertTrue(MultiString.normalize("AB|C").
			equals("AB|C"));
		assertTrue(MultiString.normalize("AB|{}{}C{}").
			equals("AB|{}{}C{}"));
		assertTrue(MultiString.normalize("!\"#$%&\'()*+,-./").
			equals("!\"#$%&\'()*+,-./"));
		assertTrue(MultiString.normalize(":;<=>?@\\^_`{|}~").
			equals(":;<=>?@\\^_`{|}~"));
		assertTrue(MultiString.normalize("[]][\t\b\n\r\f").
			equals(""));
		assertTrue(MultiString.normalize("ABC_DEF").
			equals("ABC_DEF"));
		assertTrue(MultiString.normalize("ABC[bad]DEF").
			equals("ABCDEF"));
		assertTrue(MultiString.normalize("ABC[nl]DEF").
			equals("ABC[nl]DEF"));
		assertTrue(MultiString.normalize("ABC[nl3]DEF").
			equals("ABC[nl3]DEF"));
		assertTrue(MultiString.normalize("ABC[np]DEF").
			equals("ABC[np]DEF"));
		assertTrue(MultiString.normalize("ABC[jl4]DEF").
			equals("ABC[jl4]DEF"));
		assertTrue(MultiString.normalize("ABC[jl6]DEF").
			equals("ABCDEF"));
		assertTrue(MultiString.normalize("ABC[jp4]DEF").
			equals("ABC[jp4]DEF"));
		assertTrue(MultiString.normalize("[fo3]ABC DEF").
			equals("[fo3]ABC DEF"));
		assertTrue(MultiString.normalize("[fo3,beef]ABC DEF").
			equals("[fo3,beef]ABC DEF"));
		assertTrue(MultiString.normalize("[g1]").
			equals("[g1]"));
		assertTrue(MultiString.normalize("[g1_]").equals(""));
		assertTrue(MultiString.normalize("[g1,5,5]").
			equals("[g1,5,5]"));
		assertTrue(MultiString.normalize("[g1,5,5,beef]").
			equals("[g1,5,5,beef]"));
		assertTrue(MultiString.normalize("[cf255,255,255]").
			equals("[cf255,255,255]"));
		assertTrue(MultiString.normalize("[cf0,255,255]").
			equals("[cf0,255,255]"));
		assertTrue(MultiString.normalize("[cf0,255,0]").
			equals("[cf0,255,0]"));
		assertTrue(MultiString.normalize("[pto]").
			equals("[pto]"));
		assertTrue(MultiString.normalize("[pt10o]").
			equals("[pt10o]"));
		assertTrue(MultiString.normalize("[pt10o5]").
			equals("[pt10o5]"));
		assertTrue(MultiString.normalize("[pto5]").
			equals("[pto5]"));
		assertTrue(MultiString.normalize("ABC[sc3]DEF").
			equals("ABC[sc3]DEF"));
		assertTrue(MultiString.normalize("ABC[sc3]DEF[/sc]GHI").
			equals("ABC[sc3]DEF[/sc]GHI"));
		assertTrue(MultiString.normalize("[tr1,1,40,20]").
			equals("[tr1,1,40,20]"));
		assertTrue(MultiString.normalize("[tr1,1,0,0]").
			equals("[tr1,1,0,0]"));
		assertTrue(MultiString.normalize("[pb0,128,255]").
			equals("[pb0,128,255]"));
		assertTrue(MultiString.normalize("[ttS100]").
			equals("[ttS100]"));
		assertTrue(MultiString.normalize("[feedL1]").
			equals("[feedL1]"));
		assertTrue(MultiString.normalize("[feedL1_2]").
			equals("[feedL1_2]"));
	}

	public void testPageOnTime() {
		Interval df_pgon = PageTimeHelper.defaultPageOnInterval();
		// test page time specified once for entire message
		assertTrue(new MultiString("ABC[nl]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("ABC[nl]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("ABC[np]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("[pt13o0]ABC[nl]DEF").
			pageOnInterval().round(DECISECONDS) == 13);
		assertTrue(new MultiString("ABC[nl][pt14o]DEF").
			pageOnInterval().round(DECISECONDS) == 14);
		assertTrue(new MultiString("ABC[nl]DEF[pt14o]").
			pageOnInterval().round(DECISECONDS) == 14);
		assertTrue(new MultiString("ABC[np][pt14o]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("ABC[np]DEF[pt14o]").
			pageOnInterval().equals(df_pgon));
	}

	public void testPageOnIntervals() {
		// Single page tests
		checkPageOn("", 0, new int[] { 0 });
		checkPageOn("", 999, new int[] { 999 });
		checkPageOn("[pto]", 10, new int[] { 10 });
		checkPageOn("[pt5o]", 10, new int[] { 5 });
		checkPageOn("[pto5]", 10, new int[] { 10 });
		checkPageOn("ABC", 10, new int[] { 10 });
		checkPageOn("[pto]ABC", 10, new int[] { 10 });
		checkPageOn("[pt5o]ABC", 10, new int[] { 5 });
		checkPageOn("[pto5]ABC", 10, new int[] { 10 });
		checkPageOn("ABC[pto]", 10, new int[] { 10 });
		checkPageOn("ABC[pt5o]", 10, new int[] { 5 });
		checkPageOn("ABC[pto5]", 10, new int[] { 10 });
		checkPageOn("ABC[nl][pto]123", 10, new int[] { 10 });
		checkPageOn("ABC[nl]123[pto]", 10, new int[] { 10 });
		checkPageOn("ABC[nl][pt5o]123", 10, new int[] { 5 });
		checkPageOn("ABC[nl]123[pt5o]", 10, new int[] { 5 });
		checkPageOn("ABC[nl][pto5]123", 10, new int[] { 10 });
		checkPageOn("ABC[nl]123[pto5]", 10, new int[] { 10 });
		// Two page tests
		checkPageOn("[np]", 8, new int[] { 8, 8 });
		checkPageOn("[pto][np]", 8, new int[] { 8, 8 });
		checkPageOn("[np][pto]", 8, new int[] { 8, 8 });
		checkPageOn("[pt7o][np]", 8, new int[] { 7, 7 });
		checkPageOn("[pt4o][np]", 8, new int[] { 4, 4 });
		checkPageOn("[np][pt7o]", 8, new int[] { 8, 7 });
		checkPageOn("[pt7o][np][pto]", 8, new int[] { 7, 8 });
		checkPageOn("ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOn("[pto]ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOn("ABC[np][pto]123", 8, new int[] { 8, 8 });
		checkPageOn("[pt7o]ABC[np]123", 8, new int[] { 7, 7 });
		checkPageOn("[pt4o]ABC[np]123", 8, new int[] { 4, 4 });
		checkPageOn("ABC[np][pt7o]123", 8, new int[] { 8, 7 });
		checkPageOn("[pto]ABC[np]123[pt7o]", 8, new int[] { 8, 7 });
		checkPageOn("[pt7o]ABC[np][pto]123", 8, new int[] { 7, 8 });
		checkPageOn("[pt7o]ABC[np]123[pto]", 8, new int[] { 7, 8 });
		// Three page tests
		checkPageOn("PG1[np]PG2[np]PG3", 6, new int[] { 6, 6, 6 });
		checkPageOn("[pto]PG1[np]PG2[np]PG3", 6, new int[] { 6, 6, 6 });
		checkPageOn("[pt7o4]PG1[np][pt8o4]PG2[np]PG3", 10,
			new int[] { 7, 8, 8 });
		checkPageOn("PG1[np][pt8o4]PG2[np]PG3", 10,
			new int[] { 10, 8, 8 });
		checkPageOn("PG1[np][pt8o4]PG2[np][pto]PG3", 10,
			new int[] { 10, 8, 10 });
	}

	private void checkPageOn(String ms, int dflt_ds, int[] intvls) {
		Interval dflt = new Interval(dflt_ds, DECISECONDS);
		Interval[] t = new MultiString(ms).pageOnIntervals(dflt);
		assertTrue(t.length == intvls.length);
		for(int i = 0; i < t.length; i++) {
			Interval val = new Interval(intvls[i], DECISECONDS);
			assertTrue(t[i].equals(val));
		}
	}

	public void testPageOffIntervals() {
		// Single page tests
		checkPageOff("", 0, new int[] { 0 });
		checkPageOff("", 999, new int[] { 999 });
		checkPageOff("[pto]", 10, new int[] { 10 });
		checkPageOff("[pt5o]", 10, new int[] { 10 });
		checkPageOff("[pto5]", 10, new int[] { 5 });
		checkPageOff("ABC", 10, new int[] { 10 });
		checkPageOff("[pto]ABC", 10, new int[] { 10 });
		checkPageOff("[pt5o]ABC", 10, new int[] { 10 });
		checkPageOff("[pto5]ABC", 10, new int[] { 5 });
		checkPageOff("ABC[pto]", 10, new int[] { 10 });
		checkPageOff("ABC[pt5o]", 10, new int[] { 10 });
		checkPageOff("ABC[pto5]", 10, new int[] { 5 });
		checkPageOff("ABC[nl][pto]123", 10, new int[] { 10 });
		checkPageOff("ABC[nl]123[pto]", 10, new int[] { 10 });
		checkPageOff("ABC[nl][pt5o]123", 10, new int[] { 10 });
		checkPageOff("ABC[nl]123[pt5o]", 10, new int[] { 10 });
		checkPageOff("ABC[nl][pto5]123", 10, new int[] { 5 });
		checkPageOff("ABC[nl]123[pto5]", 10, new int[] { 5 });
		// Two page tests
		checkPageOff("[np]", 8, new int[] { 8, 8 });
		checkPageOff("[pto][np]", 8, new int[] { 8, 8 });
		checkPageOff("[np][pto]", 8, new int[] { 8, 8 });
		checkPageOff("[pto7][np]", 8, new int[] { 7, 7 });
		checkPageOff("[pto4][np]", 8, new int[] { 4, 4 });
		checkPageOff("[np][pto7]", 8, new int[] { 8, 7 });
		checkPageOff("[pto7][np][pto]", 8, new int[] { 7, 8 });
		checkPageOff("ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOff("[pto]ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOff("ABC[np][pto]123", 8, new int[] { 8, 8 });
		checkPageOff("[pto7]ABC[np]123", 8, new int[] { 7, 7 });
		checkPageOff("[pto4]ABC[np]123", 8, new int[] { 4, 4 });
		checkPageOff("ABC[np][pto7]123", 8, new int[] { 8, 7 });
		checkPageOff("[pto]ABC[np]123[pto7]", 8, new int[] { 8, 7 });
		checkPageOff("[pto7]ABC[np][pto]123", 8, new int[] { 7, 8 });
		checkPageOff("[pto7]ABC[np]123[pto]", 8, new int[] { 7, 8 });
		// Three page tests
		checkPageOff("PG1[np]PG2[np]PG3", 6, new int[] { 6, 6, 6 });
		checkPageOff("[pto]PG1[np]PG2[np]PG3", 6,
			new int[] { 6, 6, 6 });
		checkPageOff("[pt7o4]PG1[np][pt8o6]PG2[np]PG3", 10,
			new int[] { 4, 6, 6 });
		checkPageOff("PG1[np][pt8o4]PG2[np]PG3", 10,
			new int[] { 10, 4, 4 });
		checkPageOff("PG1[np][pt8o4]PG2[np][pto]PG3", 10,
			new int[] { 10, 4, 10 });
	}

	private void checkPageOff(String ms, int dflt_ds, int[] intvls) {
		Interval dflt = new Interval(dflt_ds, DECISECONDS);
		Interval[] t = new MultiString(ms).pageOffIntervals(dflt);
		assertTrue(t.length == intvls.length);
		for(int i = 0; i < t.length; i++) {
			Interval val = new Interval(intvls[i], DECISECONDS);
			assertTrue(t[i].equals(val));
		}
	}

	public void testReplacePageOnTime() {
		checkReplacePageTimes("YA1[np]YA2",
		                      "[pt4o]YA1[np]YA2",
		                      7, 4, null, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o]YA1[np]OH YA2",
		                      "[pt4o]YA1[np]OH YA2",
		                      7, 4, null, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o50]YA1[np]OH YA2",
		                      "[pt4o50]YA1[np]OH YA2",
		                      7, 4, 50, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o50]YA1[np][pt22o60]OH YA2",
		                      "[pt4o50]YA1[np][pt4o50]OH YA2",
		                      7, 4, 50, new int[] { 4, 4 });
	}

	private void checkReplacePageTimes(String ms, String cms, int dflt_ds,
		int pot, Integer pof, int[] intvls)
	{
		Interval dflt = new Interval(dflt_ds, DECISECONDS);
		MultiString ms2 = new MultiString(MultiString.replacePageTime(
			ms, pot, pof));
		Interval[] t = ms2.pageOnIntervals(dflt);
		assertTrue(t.length == intvls.length);
		for (int i = 0; i < t.length; i++) {
			Interval val = new Interval(intvls[i], DECISECONDS);
			assertTrue(t[i].equals(val));
		}
		assertTrue(cms.equals(ms2.toString()));
	}

	public void testGetFonts() {
		// bogus default font numbers
		assertTrue(new MultiString("").getFonts(-10).length == 0);
		assertTrue(new MultiString("").getFonts(0).length == 0);
		assertTrue(new MultiString("").getFonts(256).length == 0);
		assertTrue(new MultiString("").getFonts(257).length == 0);

		// default is used - 1 page
		assertTrue(new MultiString("YA1").getFonts(255).length == 1);
		assertTrue(new MultiString("YA1").getFonts(255)[0] == 255);

		// default is used - 2 page
		assertTrue(new MultiString("YA1[np]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("YA1[np]YA2").
			getFonts(255)[0] == 255);
		assertTrue(new MultiString("YA1[np]YA2").
			getFonts(255)[1] == 255);

		// mainline 1 page
		assertTrue(new MultiString("[fo2]YA1").
			getFonts(255).length == 1);
		assertTrue(new MultiString("[fo2]YA1").
			getFonts(255)[0] == 2);
		assertTrue(new MultiString("[fo10]ABC").
			getFonts(255).length == 1);
		assertTrue(new MultiString("[fo10]ABC").
			getFonts(255)[0] == 10);

		// mainline 2 page
		assertTrue(new MultiString("[fo2]YA1[np][fo3]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("[fo2]YA1[np][fo3]YA2").
			getFonts(255)[0] == 2);
		assertTrue(new MultiString("[fo2]YA1[np][fo3]YA2").
			getFonts(255)[1] == 3);

		// mainline 2 page w/ default
		assertTrue(new MultiString("YA1[np][fo3]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("YA1[np][fo3]YA2").
			getFonts(255)[0] == 255);
		assertTrue(new MultiString("YA1[np][fo3]YA2").
			getFonts(255)[1] == 3);

		// mainline 2 page w/ font carryover
		assertTrue(new MultiString("[fo3]YA1[np]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("[fo3]YA1[np]YA2").
			getFonts(255)[0] == 3);
		assertTrue(new MultiString("[fo3]YA1[np]YA2").
			getFonts(255)[1] == 3);
	}

	public void testEtc() {
		// constructor
		try {
			new MultiString(null);
			assertTrue(false);
		} catch (NullPointerException ex) {
			assertTrue(true);
		}

		// isValid
		assertTrue(new MultiString().isValid());
		assertTrue(new MultiString("").isValid());
		assertTrue(new MultiString("ABC").isValid());

		// nl tag
		assertTrue(new MultiString("ABC[nl]DEF").isValid());
		assertTrue(new MultiString("ABC[nl1]DEF").isValid());

		// fo tag
		assertTrue(new MultiString("ABC[fo]DEF").isValid());
		assertTrue(new MultiString("ABC[fo1]DEF").isValid());
		assertTrue(new MultiString("ABC[fo12]DEF").isValid());
		assertTrue(new MultiString("ABC[fo123]DEF").isValid());

		// np tag
		assertTrue(new MultiString("ABC[np]DEF").isValid());

		// jp tag
		assertTrue(new MultiString("ABC[jp1]DEF").isValid());
		assertTrue(new MultiString("ABC[jp2]DEF").isValid());
		assertTrue(new MultiString("ABC[jp3]DEF").isValid());
		assertTrue(new MultiString("ABC[jp4]DEF").isValid());

		// pt tag
		assertTrue(new MultiString("ABC[pto]").isValid());
		assertTrue(new MultiString("ABC[pt1o]").isValid());
		assertTrue(new MultiString("ABC[pt12o]").isValid());
		assertTrue(new MultiString("ABC[pt123o]").isValid());
		assertTrue(new MultiString("ABC[pto1]").isValid());
		assertTrue(new MultiString("ABC[pto12]").isValid());
		assertTrue(new MultiString("ABC[pto123]").isValid());
	}
}