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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DetectorNode extends AbstractDetectorNode {

	@NotNull
	private final DetectorFactory detector;

	private boolean enabled;

	DetectorNode(
			@NotNull final DetectorFactory detector,
			final boolean enabled
	) {
		super(detector.getShortName());
		this.detector = detector;
		this.enabled = enabled;
	}

	@Override
	boolean isGroup() {
		return false;
	}

	@Override
	@Nullable
	Boolean getEnabled() {
		return enabled;
	}

	@Override
	void setEnabled(@Nullable final Boolean enabled) {
		this.enabled = null != enabled && enabled;
	}

	boolean isEnabledDefaultDifferent() {
		return enabled != detector.isDefaultEnabled();
	}

	@NotNull
	DetectorFactory getDetector() {
		return detector;
	}
}
