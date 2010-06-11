package zero.detector.testdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import zero.detector.DefaultEncodingDetector;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Detector2;
import edu.umd.cs.findbugs.DetectorToDetector2Adapter;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.NoOpFindBugsProgress;
import edu.umd.cs.findbugs.ba.AnalysisCacheToAnalysisContextAdapter;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.FieldSummary;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.DescriptorFactory;
import edu.umd.cs.findbugs.classfile.Global;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IClassFactory;
import edu.umd.cs.findbugs.classfile.IClassPath;
import edu.umd.cs.findbugs.classfile.IClassPathBuilder;
import edu.umd.cs.findbugs.classfile.ICodeBaseLocator;
import edu.umd.cs.findbugs.classfile.analysis.AnnotationValue;
import edu.umd.cs.findbugs.classfile.impl.ClassFactory;
import findbugs.test.support.ExpectBug;
import findbugs.test.support.SimpleBugReporter;

public class DefaultEncoding {

	/**
	 * Does not override the parent class's problematic method. Invocations of
	 * that method on instances of this class should be flagged.
	 */
	public class MyBAOS extends ByteArrayOutputStream {
		@ExpectBug(value="DENC", occurrences=1)
		public void bar() {
			// Problem - should be flagged
			this.toString();
		}
	}
	
	/**
	 * Overrides the parent class's problematic method. Invocations of the
	 * overriding method should NOT be flagged. However, direct calls to the superclass's
	 * problematic method via super should be flagged.
	 */
	public class MyOtherBAOS extends ByteArrayOutputStream {
		
		@Override
		public String toString() {
			try {
				// not a problem
				return super.toString("UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			return "";
		}
		
		public void bar() {
			// not a problem
			this.toString();
		}
		
		@ExpectBug(value="DENC", occurrences=1)
		public void foo() {
			// Problem - should be flagged
			super.toString();
		}

	}
	
	@ExpectBug(value="DENC", occurrences=3)
	public void string() {
		new String(new byte[]{});
		new String(new byte[]{}, 0, 0);
		"".getBytes();
	}
	
	@ExpectBug(value="DENC", occurrences=6)
	public void fileReaderWriter() throws IOException {
		new FileReader("");
		new FileReader(new File(""));
		new FileReader(new FileDescriptor());
		new FileWriter("");
		new FileWriter(new File(""));
		new FileWriter(new FileDescriptor());
	}
	
	@ExpectBug(value="DENC", occurrences=8)
	public void printStreamWriter() throws IOException {
		new PrintStream(new File(""));
		new PrintStream(new FileOutputStream(""));
		new PrintStream(new FileOutputStream(""), true);
		new PrintStream("");
		new PrintWriter(new File(""));
		new PrintWriter(new FileOutputStream(""));
		new PrintWriter(new FileOutputStream(""), true);
		new PrintWriter("");
	}

	@ExpectBug(value="DENC", occurrences=7)
	public void misc() throws IOException {
		new ByteArrayOutputStream().toString();			
		new InputStreamReader(new FileInputStream(""));
		new OutputStreamWriter(new FileOutputStream(""));
		new Scanner(new FileInputStream(""));
		new Formatter("");
		new Formatter(new File(""));
		new Formatter(new FileOutputStream(""));
	}
	
	/**
	 * These are all fine and should not be flagged.
	 */
	public void notBugs() throws IOException {
		String a = "foobar";
		a.getBytes(Charset.forName("UTF-8"));
		a.getBytes("UTF-8");
		new String(new byte[]{}, "UTF-8");
		new String(new byte[]{}, 0, 0, "UTF-8");
		(new ByteArrayOutputStream()).toString("UTF-8");			
		new InputStreamReader(new FileInputStream(""), "UTF-8");
		new OutputStreamWriter(new FileOutputStream(""), "UTF-8");
		new PrintStream(new File(""), "UTF-8");
		new PrintStream(new FileOutputStream(""), true, "UTF-8");
		new PrintStream("", "UTF-8");
		new PrintWriter(new File(""), "UTF-8");
		new PrintWriter("", "UTF-8");
		new Scanner(new FileInputStream(""), "UTF-8");
		new Formatter("", "UTF-8");
		new Formatter(new File(""), "UTF-8");
		new Formatter(new FileOutputStream(""), "UTF-8");
		new StringBuilder().toString();
		new ArrayList<Object>().toString();
		List<String> failures = new ArrayList<String>(); 
		failures.toString();
	}
	

}
