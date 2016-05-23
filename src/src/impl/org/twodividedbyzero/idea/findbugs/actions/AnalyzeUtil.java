/*
 * Copyright 2008-2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.actionSystem.DataKey;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

final class AnalyzeUtil {

	static final DataKey<AnalysisScope> KEY;

	static {
		DataKey<AnalysisScope> _key = null;

		try {
			_key = getStaticDataKey(Class.forName("com.intellij.analysis.AnalysisScopeUtil"));
		} catch (final ClassNotFoundException ignored) { // ignore e;
		} catch (final NoSuchFieldException ignored) { // ignore e;
		} catch (final IllegalAccessException e) {
			throw new Error(e); // we are expecting a public field
		}

		if (_key == null) {
			try {
				_key = getStaticDataKey(AnalysisScope.class);
			} catch (final NoSuchFieldException e) {
				throw new Error(e);
			} catch (final IllegalAccessException e) {
				throw new Error(e); // we are expecting a public field
			}
		}

		if (_key == null) {
			throw new Error("No data key");
		}

		KEY = _key;
	}

	private AnalyzeUtil() {
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static DataKey<AnalysisScope> getStaticDataKey(@Nullable final Class<?> clazz) throws NoSuchFieldException, IllegalAccessException {
		if (clazz != null) {
			final Field key = clazz.getDeclaredField("KEY");
			return (DataKey<AnalysisScope>) key.get(null);
		}
		return null;
	}
}
