/*
 * Copyright 2008-2013 Andre Pfeiler
 *
 * This file is part of FindBugs-IDEA.
 *
 * FindBugs-IDEA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FindBugs-IDEA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FindBugs-IDEA.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.ui.JBColor;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * A label, that can be rotated by 90 degrees either left or right. Thus the
 * reading direction is either horizontal (the default), up or down.
 *
 * @author Johannes RÃ¶ssel
 */
public class RotatedJLabel extends JLabel {

  /** Serialisation ID. */
  private static final long serialVersionUID = -5589305224172247331L;

  /** Represents the direction of a rotated label. */
  public enum Direction {
    /** Normal horizontal direction. */
    HORIZONTAL,
    /** Vertical, upwards (for left-to-right languages). */
    VERTICAL_UP,
    /** Vertical, downwards (for left-to-right languages). */
    VERTICAL_DOWN
  }

  /** The text direction. */
  private Direction _direction;

  {
    // it's better to set this here as default for all constructors since they
    // only call the super constructors.
    setDirection(Direction.HORIZONTAL);
  }

  /**
   * A flag indicating whether {link #getSize()} and such methods need to return
   * a rotated dimension.
   */
  private boolean _needsRotate;

  /**
   * Initialises a new instance of the {@link RotatedJLabel} class using default
   * values.
   */
  public RotatedJLabel() {
  }

  /**
   * Initialises a new instance of the {@link RotatedJLabel} class using the
   * specified icon and horizontal alignment.
   *
   * @param image
   *          The icon to use for this label.
   * @param horizontalAlignment
   *          The horizontal alignment of the text.
   */
  public RotatedJLabel(final Icon image, final int horizontalAlignment) {
    super(image, horizontalAlignment);
  }

  /**
   * Initialises a new instance of the {@link RotatedJLabel} class using the
   * specified icon.
   *
   * @param image
   *          The icon to use for this label.
   */
  public RotatedJLabel(final Icon image) {
    super(image);
  }

  /**
   * Initialises a new instance of the {@link RotatedJLabel} class using the
   * specified text, icon and horizontal alignment.
   *
   * @param text
   *          The text to display.
   * @param icon
   *          The icon to use for this label.
   * @param horizontalAlignment
   *          The horizontal alignment of the text.
   */
  public RotatedJLabel(final String text, final Icon icon, final int horizontalAlignment) {
    super(text, icon, horizontalAlignment);
  }

  /**
   * Initialises a new instance of the {@link RotatedJLabel} class using the
   * specified text and horizontal alignment.
   *
   * @param text
   *          The text to display.
   * @param horizontalAlignment
   *          The horizontal alignment of the text.
   */
  public RotatedJLabel(final String text, final int horizontalAlignment) {
    super(text, horizontalAlignment);
  }

  /**
   * Initialises a new instance of the {@link RotatedJLabel} class using the
   * specified text.
   *
   * @param text
   *          The text to display.
   */
  public RotatedJLabel(final String text) {
    super(text);
  }

  /**
   * Gets the text direction of this {@link RotatedJLabel}.
   *
   * @return The current text direction.
   */
  public Direction getDirection() {
    return _direction;
  }

  /**
   * Sets the text direction of this {@link RotatedJLabel}.
   *
   * @param direction
   *          The new direction.
   */
  public void setDirection(final Direction direction) {
	  _direction = direction;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    final Dimension preferredSize = super.getPreferredSize();

    // swap size for vertical alignments
    switch (getDirection()) {
    case VERTICAL_UP: // NOTE: fall-through
    case VERTICAL_DOWN:
      return new Dimension(preferredSize.height, preferredSize.width);
    default:
      return preferredSize;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.Component#getSize()
   */
  @Override
  public Dimension getSize() {
    if (!_needsRotate) {
      return super.getSize();
    }

    final Dimension size = super.getSize();

    switch (getDirection()) {
    case VERTICAL_DOWN:
    case VERTICAL_UP:
      return new Dimension(size.height, size.width);
    default:
      return super.getSize();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getHeight()
   */
  @Override
  public int getHeight() {
    return getSize().height;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getWidth()
   */
  @Override
  public int getWidth() {
    return getSize().width;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(final Graphics g) {
    final Graphics2D gr = (Graphics2D) g.create();

    switch (getDirection()) {
    case VERTICAL_UP:
      gr.translate(0, getSize().getHeight());
      //gr.transform(AffineTransform.getQuadrantRotateInstance(-1));
      gr.transform(AffineTransform.getRotateInstance(-1 * Math.PI / 2.0));
      break;
    case VERTICAL_DOWN:
      //gr.transform(AffineTransform.getQuadrantRotateInstance(1));
      gr.transform(AffineTransform.getRotateInstance(1 * Math.PI / 2.0));
      gr.translate(0, -getSize().getWidth());
      break;
    default:
    }

    _needsRotate = true;
    super.paintComponent(gr);
    _needsRotate = false;
  }

  /**
   * Test method.
   *
   * @param args
   *          command line arguments.
   */
  public static void main(final String[] args) {
	  SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
			  final JFrame f = new JFrame("Test");
			  f.setLayout(new FlowLayout());
			  final RotatedJLabel rl = new RotatedJLabel("BLAHBLAH");
			  rl.setBackground(JBColor.ORANGE);
			  rl.setOpaque(true);
			  rl.setDirection(Direction.VERTICAL_DOWN);
			  f.add(rl);
			  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			  f.pack();
			  f.setVisible(true);
		  }
	  });
  }


	@Override
	public String toString() {
		return "RotatedJLabel{" + "_direction=" + _direction + ", _needsRotate=" + _needsRotate + '}';
	}
}
