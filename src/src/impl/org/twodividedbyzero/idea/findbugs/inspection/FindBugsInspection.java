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
package org.twodividedbyzero.idea.findbugs.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.VersionManager;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterInspectionEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterInspectionEvent;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.gui.inspection.FindBugsInspectionPanel;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class FindBugsInspection extends LocalInspectionTool implements EventListener<BugReporterInspectionEvent> {

	private static final Logger LOGGER = Logger.getInstance(FindBugsInspection.class.getName());

	private boolean _isListener;
	private final List<ProblemDescriptor> _problems;
	private PsiFile _psiFile;
	private static final ProblemDescriptor[] EMPTY_PROBLEM_DESCRIPTOR = {};


	public FindBugsInspection() {
		_problems = new ArrayList<ProblemDescriptor>();
		//EventManagerImpl.getInstance().addEventListener(new BugReporterInspectionEventFilter(IdeaUtilImpl.getProject().getName()), this);
	}


	@Override
	@Nullable
	public JComponent createOptionsPanel() {
		return new FindBugsInspectionPanel();
	}


	@Override
	@Nls
	@NotNull
	public String getGroupDisplayName() {
		return ResourcesLoader.getString("findbugs.inspection.group");
	}


	@Override
	@Nls
	@NotNull
	public String getDisplayName() {
		return ResourcesLoader.getString("findbugs.inspection.displayName");
	}


	@Nls
	@Override
	public String getStaticDescription() {
		return "<html><body><p>" + ResourcesLoader.getString("findbugs.inspection.description") +
				"</p><br/><p><font size='8px' color='gray'>Powered by " + VersionManager.getFullVersion() +
				" with Findbugs&trade; version " + FindBugsUtil.getFindBugsFullVersion() + "</font></p></body></html>";
	}


	@Override
	public String loadDescription() {
		return getStaticDescription();
	}


	@Nls
	@Override
	@NotNull
	public String getShortName() {
		return "FindBugsIDEA";
	}


	public static InspectionManager getManager(final PsiElement psiElement) {
		return getManager(psiElement.getProject());
	}


	private static InspectionManager getManager(final Project project) {
		return InspectionManager.getInstance(project);
	}


	private static void isPluginAccessible(final Project project) {
		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			Messages.showWarningDialog("Couldn't get findbugs plugin", "FindBugs");
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}
	}


	@Override
	public boolean runForWholeFile() {
		return true;
	}


	// do not remove to be idea < 10.5.1 compatible
	@SuppressWarnings( {"MethodMayBeStatic", "UnusedDeclaration", "UnusedDeclaration"})
	public void inspectionStarted(final LocalInspectionToolSession session) {
		LOGGER.debug("Inspection started...");
	}


	@Override
	public void inspectionFinished(@NotNull final LocalInspectionToolSession session) {
		LOGGER.debug("Inspection finished...");
	}


	@Override
	public void inspectionFinished(@NotNull final LocalInspectionToolSession session, @NotNull final ProblemsHolder problemsHolder) {
		LOGGER.debug("Inspection finished...");
	}


	@Override
	public ProblemDescriptor[] checkFile(@NotNull final PsiFile psiFile, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
		LOGGER.debug("Running FindBugs-IDEA inspection on psiFile: " + psiFile.getName());
		LOGGER.debug("Inspection has been invoked.");

		if (!psiFile.isValid() || !psiFile.isPhysical() || !IdeaUtilImpl.isValidFileType(psiFile.getFileType())) {
			LOGGER.debug("Skipping file as invalid: " + psiFile.getName());
			return EMPTY_PROBLEM_DESCRIPTOR;
		}

		_psiFile = psiFile;
		isPluginAccessible(manager.getProject());

		try {
			initWorker(psiFile);
		} catch (final ProcessCanceledException e) {
			LOGGER.debug(getGroupDisplayName() + " - " + getDisplayName(), e);
		}

		return _problems.toArray(new ProblemDescriptor[_problems.size()]);
	}


	/**
	 * See {@link InspectionManager#createProblemDescriptor(PsiElement, String, LocalQuickFix, ProblemHighlightType) } for method descriptions.
	 *
	 * @param psiFile
	 * @param bugInstance
	 * @return the created ProblemDescriptor
	 */
	@SuppressWarnings({"HardcodedLineSeparator"})
	private static ProblemDescriptor createProblemDescriptor(final PsiFile psiFile, final BugInstance bugInstance) {
		final int[] lines = BugInstanceUtil.getSourceLines(bugInstance);
		final MethodAnnotation methodAnnotation = BugInstanceUtil.getPrimaryMethod(bugInstance);
		final FieldAnnotation fieldAnnotation = BugInstanceUtil.getPrimaryField(bugInstance);
		final StringBuilder description = new StringBuilder();

		if (lines[0] == -1) {
			description.append(BugInstanceUtil.getAbridgedMessage(bugInstance));
			description.append("\r\n");
		}

		description.append(BugInstanceUtil.getDetailText(bugInstance));

		if (fieldAnnotation != null) {
			description.append("Field: ");
			description.append(BugInstanceUtil.getFieldName(bugInstance)).append(" ");
		}

		if (methodAnnotation != null) {
			description.append("\r\nMethod: ");
			if ("<init>".equals(BugInstanceUtil.getMethodName(bugInstance))) {
				description.append(BugInstanceUtil.getJavaSourceMethodName(bugInstance)).append("<init>").append(BugInstanceUtil.getFullMethod(bugInstance)).append(" ");
			} else {
				description.append(methodAnnotation.getMethodName()).append(BugInstanceUtil.getFullMethod(bugInstance)).append(" ");
			}
		}
		if (lines[0] > -1) {
			description.append("\r\nLine: ").append(lines[0]).append(" - ").append(lines[1]);
		}

		final ProblemDescriptor[] problemDescriptor = new ProblemDescriptor[1];
		ApplicationManager.getApplication().runReadAction(new Runnable() {
			public void run() {

				try {
					PsiElement element = IdeaUtilImpl.getElementAtLine(psiFile, lines[0] - 1);
					if (element == null) {
						element = psiFile;
					}
					final SuppressWarningFix fix = new SuppressWarningFix("org.twodividedbyzero.idea.findbugs.common.annotations.SuppressWarnings", BugInstanceUtil.getBugType(bugInstance)); // LATER: use SurroundWithTagFix ?
					problemDescriptor[0] = getManager(element).createProblemDescriptor(element, description.toString(), true, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, fix);
				} catch (final ProcessCanceledException ignore) {
				}
			}
		});

		return problemDescriptor[0];
	}


	/*@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
		return super.buildVisitor(holder, isOnTheFly);	// TODO: implement
	}*/


	/**
	 * See {@link InspectionManager#createProblemDescriptor(PsiElement, String, LocalQuickFix, ProblemHighlightType) } for method descriptions.
	 *
	 * @param bugInstance
	 * @return the created ProblemDescriptor
	 */
	ProblemDescriptor createProblemDescriptor(final BugInstance bugInstance) {
		return createProblemDescriptor(_psiFile, bugInstance);
	}


	private void initWorker(@SuppressWarnings("TypeMayBeWeakened") final PsiFile psiFile) {
		final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(psiFile);
		final FindBugsInspector worker = new FindBugsInspector(project, this);

		// set aux classpath
		final Module module = ModuleUtil.findModuleForFile(psiFile.getVirtualFile(), project);
		final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(module);
		worker.configureAuxClasspathEntries(files);

		// set source dirs
		final VirtualFile selectedSourceFiles = IdeaUtilImpl.getVirtualFile(psiFile);
		if (selectedSourceFiles == null) {
			return;
		}
		worker.configureSourceDirectories(selectedSourceFiles);

		// set class files
		worker.configureOutputFiles(new VirtualFile[] {selectedSourceFiles});

		if (selectedSourceFiles.getTimeStamp() > System.currentTimeMillis() - 30000) {
			worker.compile(new VirtualFile[] {selectedSourceFiles}, project);
		}

		worker.work("Running FindBugs inspection...");
	}


	public void onEvent(@NotNull final BugReporterInspectionEvent event) {
		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				_problems.clear();
				break;
			case ANALYSIS_ABORTED:
			case ANALYSIS_FINISHED:
				unregisterEventListner();
				break;
			case NEW_BUG_INSTANCE:
				final BugInstance bugInstance = event.getBugInstance();
				final ProblemDescriptor problemDescriptor = createProblemDescriptor(bugInstance);
				_problems.add(problemDescriptor);
				break;
			default:
		}
	}


	public void registerEventListener(final Project project) {
		if (!_isListener) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterInspectionEventFilter(project.getName()), this);
			_isListener = true;

		}
	}


	public void unregisterEventListner() {
		EventManagerImpl.getInstance().removeEventListener(this);
		_isListener = false;
	}


	@NotNull
	@Override
	public HighlightDisplayLevel getDefaultLevel() {
		return new HighlightDisplayLevel(HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING, GuiResources.FINDBUGS_ICON);
	}


	public PsiFile getPsiFile() {
		return _psiFile;
	}
}
