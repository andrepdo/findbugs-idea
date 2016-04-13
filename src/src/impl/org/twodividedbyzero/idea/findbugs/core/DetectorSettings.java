/*
 * Copyright 2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.util.Iterator;
import java.util.Map;

@Tag(value = "detectors")
public final class DetectorSettings implements Comparable<DetectorSettings> {

	/**
	 * Note that the map only contains enabled state which are not equal to the default enable state
	 * {@link DetectorFactory#isDefaultEnabled()}.
	 * <p>
	 * Key = {@link DetectorFactory#getShortName()} (like {@link edu.umd.cs.findbugs.config.UserPreferences#detectorEnablementMap})
	 * Value = Enabled state
	 */
	@Property(surroundWithTag = false)
	@MapAnnotation(
			surroundWithTag = false,
			surroundValueWithTag = false,
			surroundKeyWithTag = false,
			entryTagName = "detector",
			keyAttributeName = "name",
			valueAttributeName = "enabled"
	)
	public Map<String, Boolean> enabled = New.map();

	@Override
	public int compareTo(@NotNull final DetectorSettings o) {
		final Iterator<Map.Entry<String, Boolean>> a = enabled.entrySet().iterator();
		final Iterator<Map.Entry<String, Boolean>> b = o.enabled.entrySet().iterator();
		while (true) {
			final boolean aHasNext = a.hasNext();
			final boolean bHasNext = b.hasNext();
			if (!aHasNext && !bHasNext) {
				return 0;
			} else if (aHasNext && bHasNext) {
				final Map.Entry<String, Boolean> aNext = a.next();
				final Map.Entry<String, Boolean> bNext = b.next();
				int ret = aNext.getValue().compareTo(bNext.getValue());
				if (ret != 0) {
					return ret;
				}
				ret = aNext.getValue().compareTo(bNext.getValue());
				if (ret != 0) {
					return ret;
				}
			} else if (aHasNext) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final DetectorSettings settings = (DetectorSettings) o;
		return enabled.equals(settings.enabled);

	}

	@Override
	public int hashCode() {
		return enabled.hashCode();
	}
}
