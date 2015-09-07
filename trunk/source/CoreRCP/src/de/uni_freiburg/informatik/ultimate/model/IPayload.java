/*
 * Copyright (C) 2008-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.model;

import java.io.Serializable;
import java.util.HashMap;

import de.uni_freiburg.informatik.ultimate.model.annotation.IAnnotations;
import de.uni_freiburg.informatik.ultimate.model.location.ILocation;

/**
 * 
 * This interface describes all information contained in an INode. We use it to
 * hide the data structure from the information and to save resources
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public interface IPayload extends Serializable {

	/**
	 * Get a two-dimensional HashMap containing Annotations. Initializes
	 * annotations automatically if not already initialized.
	 * 
	 * @return the HashMap containing the annotations
	 */
	HashMap<String, IAnnotations> getAnnotations();

	/**
	 * Sets a two-dimensional HashMap containing Annotations. Do not use to
	 * initialize annotations (use getAnnotation() instead).
	 * 
	 * @param annotations
	 *            the annotations
	 * @deprecated
	 */
	void setAnnotations(HashMap<String, IAnnotations> annotations);

	/**
	 * gets the name of this node, usually the token it represents
	 * 
	 * @deprecated
	 * @return the name
	 */
	String getName();

	/***
	 * 
	 * @deprecated
	 * @param name
	 */
	void setName(String name);

	/**
	 * Tries to give you a satisfying answer where this token was found. May be
	 * null.
	 * 
	 * @return the location itself
	 */
	ILocation getLocation();

	/***
	 * 
	 * @deprecated
	 * @param loc
	 */
	void setLocation(ILocation loc);

	/**
	 * Returns the unique identifier for this node. The identifier should be
	 * unique for all objects of the same type.
	 * 
	 * @return A composite data structure serving as unique identifier. Consists
	 *         of a short, long and int.
	 */
	UltimateUID getID();

	/**
	 * Returns true if the annotation hash map is already initialized and
	 * contains elements. Returns false if the annotations are not initialized
	 * or contain no elements. Should be used instead of a direct null test of
	 * the getAnnotations() method to prevent unnecessary initialization.
	 * 
	 * @return true if the annotation hash map is already initialized and
	 *         contains elements. Returns false if the annotations are not
	 *         initialized or contain no elements.
	 */
	boolean hasAnnotation();

	/**
	 * Returns true if the Location Object is already initialized and false if
	 * not. Should be used instead of a direct null test of the getLocation()
	 * method to prevent unnecessary initialization.
	 * 
	 * @return True if the Location has been initialized, false if not
	 */
	boolean hasLocation();

	/**
	 * Returns true if the UltimateUID Object is already initialized and false
	 * if not. Should be used instead of a direct null test of the getID()
	 * method to prevent unnecessary initialization.
	 * 
	 * @return True if the UltimateUID has been initialized, false if not
	 */
	boolean hasID();

	/**
	 * Returns true if the name of the payload is already set and false if not.
	 * Should be used instead of a direct null test of the getName() method to
	 * prevent unnecessary initialization.
	 * 
	 * @deprecated
	 * @return True if the name has been initialized, false if not
	 */
	boolean hasName();

	/**
	 * Returns true if the name of the payload represents a value and false if
	 * not. This method is used to distinguish tokens from real values in case
	 * one our our token keywords equals a value in the original source.
	 * 
	 * @deprecated
	 * @return Returns true if the name of the payload represents a value and
	 *         false if not
	 */
	boolean isValue();
}
