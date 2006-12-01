/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2006, University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.ba.generic;

import java.util.Arrays;
import java.util.List;

import org.apache.bcel.generic.Type;

import edu.umd.cs.findbugs.ba.generic.GenericUtilities.TypeCategory;

import junit.framework.TestCase;

/**
 * @author Nat Ayewah
 */
public class TestGenericObjectType extends TestCase {

	GenericObjectType obj;
	String javaSignature;
	String underlyingClass;
	GenericUtilities.TypeCategory typeCategory;
	
	String variable;
	Type extension;
	List<Type> parameters;
	
	
	public void initTest(
			String bytecodeSignature, 
			String javaSignature, 
			String underlyingClass, 
			GenericUtilities.TypeCategory typeCategory, 
			String variable, 
			Type extension, 
			List<Type> parameters) {
		this.obj = (GenericObjectType) GenericUtilities.getType(bytecodeSignature);
		this.javaSignature = javaSignature;
		this.underlyingClass = underlyingClass;
		this.typeCategory = typeCategory;
		this.variable = variable;
		this.extension = extension;
		this.parameters = parameters;
	}

	public void processTest() {
		assertEquals(obj.toString(true), javaSignature);
		assertEquals(obj.getClassName(), underlyingClass);
		assertEquals(obj.getTypeCategory(), typeCategory);
		
		if (typeCategory == TypeCategory.PARAMETERS) {
			assertTrue(obj.hasParameters());
			assertTrue(obj.getNumParameters() == parameters.size());
			for (int i=0; i<obj.getNumParameters(); i++)
				compareTypes(obj.getParameterAt(i), parameters.get(i));
			assertNull(obj.getVariable());
			assertNull(obj.getExtension());
		}else if (typeCategory == TypeCategory.TYPE_VARIABLE) { 
			assertFalse(obj.hasParameters());
			assertNull(obj.getExtension());
			assertNotNull(obj.getVariable());
			assertEquals(obj.getVariable(), variable);
		}else if (typeCategory == TypeCategory.WILDCARD) { 
			assertFalse(obj.hasParameters());
			assertNull(obj.getExtension());
			assertNotNull(obj.getVariable());
			assertEquals(obj.getVariable(), "*");
		} else if (typeCategory == TypeCategory.WILDCARD_EXTENDS || typeCategory == TypeCategory.WILDCARD_SUPER) { 
			assertFalse(obj.hasParameters());
			assertNotNull(obj.getExtension());
			assertNotNull(obj.getVariable());
			assertEquals(obj.getVariable(), variable);
			compareTypes(obj.getExtension(), extension);
		}
	}

	
	private void compareTypes(Type a, Type b) {
		assertEquals(a, b);
		if (a instanceof GenericObjectType || b instanceof GenericObjectType) {
			assertTrue(a instanceof GenericObjectType && b instanceof GenericObjectType);
			assertEquals( 
					((GenericObjectType)a).toString(true), 
					((GenericObjectType)b).toString(true)
					);
		}		
	}
	
	public void testParameterizedList() {
		initTest(
				"Ljava/util/List<Ljava/lang/Comparable;>;", 
				"java.util.List<java.lang.Comparable>", 
				"java.util.List", 
				GenericUtilities.TypeCategory.PARAMETERS, 
				null, 
				null,
				GenericUtilities.getTypes("Ljava/lang/Comparable;")
			);
		processTest();
	}
	
	public void testCreateTypes() {
		initTest(
				"LDummyClass<Ljava/lang/Comparable;TE;>;", 
				"DummyClass<java.lang.Comparable,E>", 
				"DummyClass", 
				GenericUtilities.TypeCategory.PARAMETERS, 
				null, 
				null,
				Arrays.asList(
						GenericUtilities.getType("Ljava/lang/Comparable;"),
						GenericUtilities.getType("TE;")
				)
			);
		processTest();
	}
	
	public void testTypeVariables() {
		initTest(
				"TE;", 
				"E", 
				"java.lang.Object", 
				GenericUtilities.TypeCategory.TYPE_VARIABLE, 
				"E", 
				null,
				null
			);
		processTest();
		
		initTest(
				"*", 
				"?", 
				"java.lang.Object", 
				GenericUtilities.TypeCategory.WILDCARD, 
				"*", 
				null,
				null
			);
		processTest();

		initTest(
				"+TE;", 
				"? extends E", 
				"java.lang.Object", 
				GenericUtilities.TypeCategory.WILDCARD_EXTENDS, 
				"+", 
				GenericUtilities.getType("TE;"),
				null
			);
		processTest();

		initTest(
				"-TE;", 
				"? super E", 
				"java.lang.Object", 
				GenericUtilities.TypeCategory.WILDCARD_SUPER, 
				"-", 
				GenericUtilities.getType("TE;"),
				null
			);
		processTest();

		initTest(
				"-[TE;", 
				"? super E[]", 
				"java.lang.Object", 
				GenericUtilities.TypeCategory.WILDCARD_SUPER, 
				"-", 
				GenericUtilities.getType("[TE;"),
				null
			);
		processTest();

	}	
}
