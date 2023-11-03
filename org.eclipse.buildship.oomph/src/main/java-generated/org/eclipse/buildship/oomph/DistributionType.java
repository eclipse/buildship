/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '<em><b>Distribution
 * Type</b></em>', and utility methods for working with them. <!-- end-user-doc -->
 * 
 * @see org.eclipse.buildship.oomph.GradleImportPackage#getDistributionType()
 * @model
 * @generated
 */
public enum DistributionType implements Enumerator {
    /**
     * The '<em><b>GRADLE WRAPPER</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #GRADLE_WRAPPER_VALUE
     * @generated
     * @ordered
     */
    GRADLE_WRAPPER(0, "GRADLE_WRAPPER", "GRADLE_WRAPPER"),

    /**
     * The '<em><b>LOCAL INSTALLATION</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #LOCAL_INSTALLATION_VALUE
     * @generated
     * @ordered
     */
    LOCAL_INSTALLATION(1, "LOCAL_INSTALLATION", "LOCAL_INSTALLATION"),

    /**
     * The '<em><b>REMOTE DISTRIBUTION</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #REMOTE_DISTRIBUTION_VALUE
     * @generated
     * @ordered
     */
    REMOTE_DISTRIBUTION(2, "REMOTE_DISTRIBUTION", "REMOTE_DISTRIBUTION"),

    /**
     * The '<em><b>SPECIFIC GRADLE VERSION</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #SPECIFIC_GRADLE_VERSION_VALUE
     * @generated
     * @ordered
     */
    SPECIFIC_GRADLE_VERSION(3, "SPECIFIC_GRADLE_VERSION", "SPECIFIC_GRADLE_VERSION");

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final String copyright = "Copyright (c) 2023 the original author or authors.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n";

    /**
     * The '<em><b>GRADLE WRAPPER</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>GRADLE WRAPPER</b></em>' literal object isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #GRADLE_WRAPPER
     * @model
     * @generated
     * @ordered
     */
    public static final int GRADLE_WRAPPER_VALUE = 0;

    /**
     * The '<em><b>LOCAL INSTALLATION</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>LOCAL INSTALLATION</b></em>' literal object isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #LOCAL_INSTALLATION
     * @model
     * @generated
     * @ordered
     */
    public static final int LOCAL_INSTALLATION_VALUE = 1;

    /**
     * The '<em><b>REMOTE DISTRIBUTION</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>REMOTE DISTRIBUTION</b></em>' literal object isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #REMOTE_DISTRIBUTION
     * @model
     * @generated
     * @ordered
     */
    public static final int REMOTE_DISTRIBUTION_VALUE = 2;

    /**
     * The '<em><b>SPECIFIC GRADLE VERSION</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>SPECIFIC GRADLE VERSION</b></em>' literal object isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #SPECIFIC_GRADLE_VERSION
     * @model
     * @generated
     * @ordered
     */
    public static final int SPECIFIC_GRADLE_VERSION_VALUE = 3;

    /**
     * An array of all the '<em><b>Distribution Type</b></em>' enumerators. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    private static final DistributionType[] VALUES_ARRAY = new DistributionType[] { GRADLE_WRAPPER, LOCAL_INSTALLATION, REMOTE_DISTRIBUTION, SPECIFIC_GRADLE_VERSION, };

    /**
     * A public read-only list of all the '<em><b>Distribution Type</b></em>' enumerators. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final List<DistributionType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the '<em><b>Distribution Type</b></em>' literal with the specified literal value.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param literal the literal.
     * @return the matching enumerator or <code>null</code>.
     * @generated
     */
    public static DistributionType get(String literal) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            DistributionType result = VALUES_ARRAY[i];
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Distribution Type</b></em>' literal with the specified name. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param name the name.
     * @return the matching enumerator or <code>null</code>.
     * @generated
     */
    public static DistributionType getByName(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            DistributionType result = VALUES_ARRAY[i];
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Distribution Type</b></em>' literal with the specified integer value.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the integer value.
     * @return the matching enumerator or <code>null</code>.
     * @generated
     */
    public static DistributionType get(int value) {
        switch (value) {
            case GRADLE_WRAPPER_VALUE:
                return GRADLE_WRAPPER;
            case LOCAL_INSTALLATION_VALUE:
                return LOCAL_INSTALLATION;
            case REMOTE_DISTRIBUTION_VALUE:
                return REMOTE_DISTRIBUTION;
            case SPECIFIC_GRADLE_VERSION_VALUE:
                return SPECIFIC_GRADLE_VERSION;
        }
        return null;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private final int value;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private final String name;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private final String literal;

    /**
     * Only this class can construct instances. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private DistributionType(int value, String name, String literal) {
        this.value = value;
        this.name = name;
        this.literal = literal;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getLiteral() {
        return literal;
    }

    /**
     * Returns the literal value of the enumerator, which is its string representation. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        return literal;
    }

} // DistributionType
