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

package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.EffectType;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andre.pfeiler@gmail.com>
 * @version $Revision$
 * @since 0.9.97
 */
public enum AnnotationType {

	// todo: name should be rank based scary scariest ...
	HighPriority(HighlightSeverity.ERROR, Color.RED, Color.WHITE, Color.RED, EffectType.WAVE_UNDERSCORE, Font.BOLD),
	NormalPriority(HighlightSeverity.WARNING, Color.BLACK, Color.WHITE, Color.YELLOW.darker(), EffectType.WAVE_UNDERSCORE, Font.ITALIC),
	ExpPriority(HighlightSeverity.INFORMATION, Color.BLACK, Color.WHITE, Color.GRAY, EffectType.WAVE_UNDERSCORE, Font.PLAIN),
	LowPriority(HighlightSeverity.INFORMATION, Color.BLACK, Color.WHITE, Color.GREEN, EffectType.BOXED, Font.PLAIN),
	IgnorePriority(HighlightSeverity.INFORMATION, Color.BLACK, Color.WHITE, Color.MAGENTA.darker().darker(), EffectType.WAVE_UNDERSCORE, Font.PLAIN);


	public static final String FOREGROUND = "foreground";
	public static final String BACKGROUND = "background";
	public static final String EFFECT_COLOR = "effectColor";
	public static final String EFFECT_TYPE = "effectType";
	public static final String FONT = "font";

	@SuppressWarnings("PublicStaticArrayField")
	public static String[] PROP_ORDER = new String[5];


	static {
		PROP_ORDER[0] = FOREGROUND;
		PROP_ORDER[1] = BACKGROUND;
		PROP_ORDER[2] = EFFECT_COLOR;
		PROP_ORDER[3] = EFFECT_TYPE;
		PROP_ORDER[4] = FONT;
	}


	private final transient HighlightSeverity _severity;
	private Color _foregroundColor;
	private Color _backgroundColor;
	private Color _effectColor;
	private EffectType _effectType;
	private int _font;


	AnnotationType(final HighlightSeverity severity, final Color foregroundColor, final Color backgroundColor, final Color effectColor, final EffectType effectType, final int font) {
		_severity = severity;
		_foregroundColor = foregroundColor;
		_backgroundColor = backgroundColor;
		_effectColor = effectColor;
		_effectType = effectType;
		_font = font;
	}


	public HighlightSeverity getSeverity() {
		return _severity;
	}


	public Color getForegroundColor() {
		return _foregroundColor;
	}


	public Color getBackgroundColor() {
		return _backgroundColor;
	}


	public Color getEffectColor() {
		return _effectColor;
	}


	public EffectType getEffectType() {
		return _effectType;
	}


	public int getFont() {
		return _font;
	}


	@SuppressWarnings("UnusedDeclaration")
	public static List<EffectType> getEffectTypes() {
		final List<EffectType> result = new ArrayList<EffectType>();
		for (final AnnotationType annotationType : values()) {
			result.add(annotationType.getEffectType());
		}
		return result;
	}


	public void setForegroundColor(final Color foregroundColor) {
		_foregroundColor = foregroundColor;
	}


	public void setBackgroundColor(final Color backgroundColor) {
		_backgroundColor = backgroundColor;
	}


	public void setEffectColor(final Color effectColor) {
		_effectColor = effectColor;
	}


	public void setEffectType(final EffectType effectType) {
		_effectType = effectType;
	}


	public void setFont(final int font) {
		_font = font;
	}


	public static Map<String, String> flatten(final Map<String, Map<String, String>> map) {
		final Map<String, String> result = new HashMap<String, String>();
		final Set<Entry<String, Map<String, String>>> entries = map.entrySet();
		for (final Entry<String, Map<String, String>> entry : entries) {
			final Map<String, String> value = entry.getValue();
			final StringBuilder properties = new StringBuilder();
			for (final String key : PROP_ORDER) {
				final String s = value.get(key);
				properties.append(s).append(';');
			}
			result.put(entry.getKey(), properties.toString());

		}
		return result;
	}


	public static Map<String, Map<String, String>> complex(final Map<String, String> annotationTypeSettings) {
		final Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		final Set<Entry<String, String>> entries = annotationTypeSettings.entrySet();
		for (final Entry<String, String> entry : entries) {
			final Map<String, String> properties = new HashMap<String, String>();
			final String[] split = entry.getValue().split(";");
			for (int i = 0, splitLength = split.length; i < splitLength; i++) {
				final String s = split[i];
				//noinspection SizeReplaceableByIsEmpty
				if (s.length() > 0) {
					final String trim = s.trim();
					properties.put(AnnotationType.PROP_ORDER[i], trim);
				}
				result.put(entry.getKey(), properties);
			}
		}
		return result;
	}


	public static void configureFrom(final Map<String, Map<String, String>> annotationTypeSettings) {
		final Set<Entry<String, Map<String, String>>> entries = annotationTypeSettings.entrySet();
		for (final Entry<String, Map<String, String>> entry : entries) {
			final Map<String, String> value = entry.getValue();
			final AnnotationType annotationType = AnnotationType.valueOf(entry.getKey());
			for (final String s : PROP_ORDER) {
				if (FOREGROUND.equals(s)) {
					annotationType.setForegroundColor(Color.decode(value.get(s)));
				} else if (BACKGROUND.equals(s)) {
					annotationType.setBackgroundColor(Color.decode(value.get(s)));
				} else if (EFFECT_COLOR.equals(s)) {
					annotationType.setEffectColor(Color.decode(value.get(s)));
				} else if (EFFECT_TYPE.equals(s)) {
					annotationType.setEffectType(EffectType.valueOf(value.get(s)));
				} else if (FONT.equals(s)) {
					annotationType.setFont(Integer.parseInt(value.get(s)));
				}
			}
		}
	}
}
