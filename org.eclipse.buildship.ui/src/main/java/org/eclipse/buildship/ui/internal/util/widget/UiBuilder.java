/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.internal.util.widget;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

/**
 * Creates SWT widgets and aligns them through a fluent API.
 *
 * All created controls are assumed to be part of a {@link org.eclipse.swt.layout.GridLayout}.
 *
 * @param <T> the type of the control to build
 */
public final class UiBuilder<T extends Control> {

    private final T control;

    private UiBuilder(T control) {
        this.control = Preconditions.checkNotNull(control);
    }

    public T control() {
        return this.control;
    }

    /**
     * Aligns the created widget to the left.
     *
     * @return the builder
     */
    public UiBuilder<T> alignLeft() {
        this.control.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        return this;
    }

    /**
     * Aligns the created widget to the left.
     *
     * @param horizontalSpan horizontal span
     * @return the builder
     */
    public UiBuilder<T> alignLeft(int horizontalSpan) {
        this.control.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, horizontalSpan, 1));
        return this;
    }

    /**
     * Aligns the created widget to the left.
     *
     * @return the builder
     */
    public UiBuilder<T> alignRight() {
        this.control.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        return this;
    }

    /**
     * Aligns the created widget to fill the cell horizontally.
     *
     * @return the builder
     */
    public UiBuilder<T> alignFillHorizontal() {
        this.control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        return this;
    }
    
    /**
     * Aligns the created widget to fill the cell vertically aligned at top.
     *
     * @return the builder
     */
	public UiBuilder<T> alignFillVerticalTop() {
        this.control.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
		return this;
	}

    /**
     * Aligns the created widget to fill both horizontal and vertical.
     *
     * @return the builder
     */
    public UiBuilder<T> alignFillBoth(int horizontalSpan) {
        this.control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
        return this;
    }

    /**
     * Enables the created widget.
     *
     * @return the builder
     */
    public UiBuilder<T> enabled() {
        this.control.setEnabled(true);
        return this;
    }

    /**
     * Disables the created widget.
     *
     * @return the builder
     */
    public UiBuilder<T> disabled() {
        this.control.setEnabled(false);
        return this;
    }

    /**
     * Sets the argument as the current widget's font.
     *
     * @param font the font to set on the widget
     * @return the builder
     */
    public UiBuilder<T> font(Font font) {
        this.control.setFont(font);
        return this;
    }

    /**
     * Sets the argument as the current widget's text.
     *
     * @param text the text to set on the widget
     * @return the builder
     * @throws IllegalArgumentException thrown if the the control does not support the text property
     */
    public UiBuilder<T> text(String text) {
        text = Strings.nullToEmpty(text);

        T control = control();
        if (control instanceof Label) {
            ((Label) control).setText(text);
        } else if (control instanceof Text) {
            ((Text) control).setText(text);
        } else if (control instanceof Button) {
            ((Button) control).setText(text);
        } else if (control instanceof Combo) {
            ((Combo) control).setText(text);
        } else if (control instanceof Group) {
            ((Group)control).setText(text);
        } else {
            throw new IllegalStateException(String.format("Cannot set text on control of type %s.", control().getClass()));
        }

        return this;
    }

    /**
     * Factory for {@code UiBuilder} instances.
     */
    public static final class UiBuilderFactory {

        private final Optional<Font> defaultFont;

        public UiBuilderFactory(Font font) {
            this.defaultFont = Optional.of(font);
        }

        /**
         * Creates a new {@link Label} control.
         *
         * @param parent the parent control
         * @return the builder
         */
        public UiBuilder<Label> newLabel(Composite parent) {
            UiBuilder<Label> builder = new UiBuilder<>(new Label(parent, SWT.NONE));
            init(builder);
            return builder;
        }
        
        /**
         * Creates a new {@link Composite} control.
         *
         * @param parent the parent control
         * @return the builder
         */
        public UiBuilder<Composite> newComposite(Composite parent) {
			UiBuilder<Composite> builder = new UiBuilder<>(new Composite(parent, SWT.NONE));
			return builder;
		}

        /**
         * Creates a new {@link Text} control.
         *
         * @param parent the parent control
         * @return the builder
         */
        public UiBuilder<Text> newText(Composite parent) {
            UiBuilder<Text> builder = new UiBuilder<>(new Text(parent, SWT.BORDER));
            init(builder);
            return builder;
        }

        /**
         * Creates a new {@link Button} control.
         *
         * @param parent the parent control
         * @return the builder
         */
        public UiBuilder<Button> newButton(Composite parent) {
            UiBuilder<Button> builder = new UiBuilder<>(new Button(parent, SWT.PUSH));
            init(builder);
            return builder;
        }

        /**
         * Creates a new radio button which is a {@link Button} control with the {@link SWT#RADIO}
         * style bit specified.
         *
         * @param parent the parent control
         * @return the builder
         */
        public UiBuilder<Button> newRadio(Composite parent) {
            UiBuilder<Button> builder = new UiBuilder<>(new Button(parent, SWT.RADIO));
            init(builder);
            return builder;
        }

        /**
         * Creates a new checkbox which is a {@link Button} control with the {@link SWT#CHECK}
         * style bit specified.
         *
         * @param parent the parent control
         * @return the builder
         */
        public UiBuilder<Button> newCheckbox(Composite parent) {
            UiBuilder<Button> builder = new UiBuilder<>(new Button(parent, SWT.CHECK));
            init(builder);
            return builder;
        }

        /**
         * Creates a new {@link Combo} control.
         *
         * @param parent the parent control
         * @return the builder
         */
        public UiBuilder<Combo> newCombo(Composite parent) {
            UiBuilder<Combo> builder = new UiBuilder<>(new Combo(parent, SWT.NONE));
            init(builder);
            return builder;
        }

        /**
         * Creates a new {@link Tree} control.
         *
         * @param parent The parent control of the result tree.
         * @return the builder
         */
        public UiBuilder<Tree> newTree(Composite parent) {
            UiBuilder<Tree> builder = new UiBuilder<>(new Tree(parent, SWT.NONE));
            init(builder);
            return builder;
        }
        
        /**
         * Creates a new {@link CheckboxTree} control.
         *
         * @param parent The parent control of the result tree.
         * @return the builder
         */
        public UiBuilder<Tree> newCheckboxTree(Composite parent) {
        	UiBuilder<Tree> builder = new UiBuilder<>(new Tree(parent, SWT.CHECK));
            init(builder);
            return builder;
		}

        /**
         * Creates a new {@link Group} control.
         *
         * @param parent The parent control of the result group.
         * @return the builder
         */
        public UiBuilder<Group> newGroup(Composite parent) {
            UiBuilder<Group> builder = new UiBuilder<>(new Group(parent, SWT.NONE));
            init(builder);
            return builder;
        }

        /**
         * Fills the next cell in the gridlayout with nothing, i.e. with an empty button to enforce
         * a certain minimum row height.
         *
         * @param parent the control having the {@link org.eclipse.swt.layout.GridLayout} having the next column to be empty
         */
        public void span(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            GridData data = new GridData(0, 0);
            label.setLayoutData(data);
            init(label);
            label.setVisible(false);
        }

        private void init(UiBuilder<?> builder) {
            init(builder.control());
        }

        private void init(Control control) {
            if (this.defaultFont.isPresent()) {
                control.setFont(this.defaultFont.get());
            }
        }

    }

}
