/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twodividedbyzero.idea.findbugs.gui.editor;


import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9
 */
public class IconLineMarkerProvider implements LineMarkerProvider {

	@NonNls
	private static final
	String JAVAX_SWING_ICON = "javax.swing.Icon";
	private static final int ICON_MAX_WEIGHT = 16;
	private static final int ICON_MAX_HEIGHT = 16;
	private static final int ICON_MAX_SIZE = 2 * 1024 * 1024; //2Kb
	private static final List<String> ICON_EXTS = Arrays.asList("png", "ico", "bmp", "gif", "jpg");  // NON-NLS

	//TODO: remove old unused icons from the cache
	private final HashMap<String, Pair<Long, Icon>> _iconsCache = new HashMap<String, Pair<Long, Icon>>();


	public LineMarkerInfo<?> getLineMarkerInfo(final PsiElement element) {
		//if (! DaemonCodeAnalyzerSetting.getInstance().SHOW_SMALL_ICONS_IN_GUTTER) return null;

		if (element instanceof PsiAssignmentExpression) {
			final PsiExpression lExpression = ((PsiAssignmentExpression) element).getLExpression();
			final PsiExpression expr = ((PsiAssignmentExpression) element).getRExpression();
			if (lExpression instanceof PsiReferenceExpression) {
				final PsiElement var = ((PsiReferenceExpression) lExpression).resolve();
				if (var instanceof PsiVariable) {
					return resolveIconInfo(((PsiVariable) var).getType(), expr);
				}
			}
		} else if (element instanceof PsiReturnStatement) {
			final PsiReturnStatement psiReturnStatement = (PsiReturnStatement) element;
			final PsiExpression value = psiReturnStatement.getReturnValue();
			final PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
			if (method != null) {
				final PsiType returnType = method.getReturnType();
				final LineMarkerInfo<PsiElement> result = resolveIconInfo(returnType, value);

				if (result != null || !isIconClassType(returnType) || value == null) {
					return result;
				}

				if (methodContainsReturnStatementOnly(method)) {
					for (final PsiReference ref : value.getReferences()) {
						final PsiElement field = ref.resolve();
						if (field instanceof PsiField) {
							return resolveIconInfo(returnType, ((PsiField) field).getInitializer(), psiReturnStatement);
						}
					}
				}
			}
		} else if (element instanceof PsiVariable) {
			final PsiVariable var = (PsiVariable) element;
			return resolveIconInfo(var.getType(), var.getInitializer());
		}
		return null;
	}


	private static boolean methodContainsReturnStatementOnly(@NotNull final PsiMethod method) {
		final PsiCodeBlock body = method.getBody();
		if (body == null || body.getStatements().length != 1) {
			return false;
		}

		return body.getStatements()[0] instanceof PsiReturnStatement;
	}


	@Nullable
	private LineMarkerInfo<PsiElement> resolveIconInfo(final PsiType type, final PsiExpression initializer) {
		return resolveIconInfo(type, initializer, initializer);
	}


	@Nullable
	private LineMarkerInfo<PsiElement> resolveIconInfo(final PsiType type, final PsiExpression initializer, final PsiElement bindingElement) {
		/*if (initializer != null && isIconClassType(type)) {

			final List<FileReference> refs = new ArrayList<FileReference>();
			initializer.accept(new JavaRecursiveElementWalkingVisitor() {
				@Override
				public void visitElement(PsiElement element) {
					if (element instanceof PsiLiteralExpression) {
						for (PsiReference ref : element.getReferences()) {
							if (ref instanceof FileReference) {
								refs.add((FileReference) ref);
							}
						}
					}
					super.visitElement(element);
				}
			});

			for (FileReference ref : refs) {
				final PsiFileSystemItem psiFileSystemItem = ref.resolve();
				VirtualFile file = null;
				if (psiFileSystemItem == null) {
					final ResolveResult[] results = ref.multiResolve(false);
					for (ResolveResult result : results) {
						final PsiElement element = result.getElement();
						if (element instanceof PsiBinaryFile) {
							file = ((PsiFile) element).getVirtualFile();
							break;
						}
					}
				} else {
					file = psiFileSystemItem.getVirtualFile();
				}

				if (file == null || file.isDirectory() || !isIconFileExtension(file.getExtension()) || file.getLength() > ICON_MAX_SIZE) {
					continue;
				}

				final Icon icon = getIcon(file);

				if (icon != null) {
					final GutterIconNavigationHandler<PsiElement> navHandler = new GutterIconNavigationHandler<PsiElement>() {
						public void navigate(MouseEvent e, PsiElement elt) {
							psiFileSystemItem.navigate(true);
						}
					};
					return new LineMarkerInfo<PsiElement>(bindingElement, bindingElement.getTextRange(), icon, Pass.UPDATE_ALL, null, navHandler, GutterIconRenderer.Alignment.LEFT);
				}
			}
		}*/
		
		return null;
	}


	private static boolean isIconFileExtension(final String extension) {
		return extension != null && ICON_EXTS.contains(extension.toLowerCase(Locale.ENGLISH));
	}


	public void collectSlowLineMarkers(final List<PsiElement> elements, final Collection<LineMarkerInfo> result) {
	}


	private static boolean hasProperSize(final Icon icon) {
		return icon.getIconHeight() <= ICON_MAX_HEIGHT && icon.getIconWidth() <= ICON_MAX_WEIGHT;
	}


	@Nullable
	private Icon getIcon(final VirtualFile file) {
		final String path = file.getPath();
		final long stamp = file.getModificationStamp();
		Pair<Long, Icon> iconInfo = _iconsCache.get(path);
		if (iconInfo == null || iconInfo.getFirst() < stamp) {
			try {
				final Icon icon = new ImageIcon(file.contentsToByteArray());
				iconInfo = new Pair<Long, Icon>(stamp, hasProperSize(icon) ? icon : null);
				_iconsCache.put(file.getPath(), iconInfo);
			} catch (final Exception ignore) {//
				//noinspection AssignmentToNull
				iconInfo = null;
				_iconsCache.remove(path);
			}
		}
		return iconInfo == null ? null : iconInfo.getSecond();
	}


	private static boolean isIconClassType(final PsiType type) {
		return InheritanceUtil.isInheritor(type, JAVAX_SWING_ICON);
	}
}
