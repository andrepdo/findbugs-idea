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

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
/*public class UntestedMethodHighlighter implements FileEditorManagerListener {

	private static final Logger LOG = Logger.getInstance("org.intellij.plugins.junit.actions.UntestedMethodHighlighter");

	private boolean highlightGetters = false;
	private boolean highlightSetters = false;
	private CommandUtil commandUtil;
	private PsiTreeChangeListener psiTreeChangeListener;
	private Editor selectedEditor;
	private Timer timer;


	public UntestedMethodHighlighter(Project project, ClassLocator classLocator, MethodLocator methodLocator) {
		this(project, classLocator, methodLocator, new CommandUtil());
	}


	public UntestedMethodHighlighter(Project project, ClassLocator classLocator, MethodLocator methodLocator, CommandUtil commandUtil) {
		super(project, classLocator, methodLocator);
		this.commandUtil = commandUtil;
	}


	public void updateTreeFromPsiTreeChange(PsiTreeChangeEvent event) {
		updateEditorElementAddedOrRemoved(event);
	}


	private void updateEditorElementAddedOrRemoved(final PsiTreeChangeEvent event) {
		PsiFile file = event.getFile();
		if (selectedEditor != null && PsiJavaFile.class.isAssignableFrom(file.getClass())) {
			PsiElement elementAtCaret = file.findElementAt(selectedEditor.getCaretModel().getOffset());
			if (elementAtCaret != null) {
				startTimer();
			}
		}
	}


	public void setOptions(boolean isSetIncluded, boolean isGetIncluded) {
		highlightSetters = isSetIncluded;
		highlightGetters = isGetIncluded;
	}


	*//*@if Aurora@*//*
	public void selectionChanged(FileEditorManagerEvent event) {
		selectedEditor = event.getManager().getSelectedTextEditor();
*//*@else@
   public void selectedFileChanged(FileEditorManagerEvent event) {
      selectedEditor = event.getManager().getSelectedEditor();
  @end@*//*
		if (selectedEditor != null && started) {
			markUntestedMethodsInEditor(selectedEditor);
		} else {
			removeUntestedMethodHighlighters(selectedEditor);
		}
	}


	public void fileOpened(FileEditorManager source, VirtualFile file) {
	}


	public void fileClosed(FileEditorManager source, VirtualFile file) {
	}


	public void markUntestedMethodsInEditor(Editor editor) {
		if (editor == null) {
			return;
		}
		PsiFile file = OpenApiFacade.getPsiDocumentManager(project).getPsiFile(editor.getDocument());
		if (file == null || !PsiJavaFile.class.isAssignableFrom(file.getClass())) {
			return;
		}
		PsiJavaFile psiJavaFile = (PsiJavaFile) file;
		if (!psiJavaFile.canContainJavaCode()) {
			return;
		}
		PsiClass[] psiClasses = psiJavaFile.getClasses();
		for (int j = 0; j < psiClasses.length; j++) {
			PsiClass psiClass = psiClasses[j];
			if (isClassTestable(psiClass)) {
				markUntestedMethods(psiClass, editor);
			}
		}
	}


	public void markUntestedMethods(final PsiClass testedClass, final Editor editor) {
		try {
			commandUtil.runOnMainThreadAsynchonously(new Runnable() {
				public void run() {
					doMarkUntestedMethods(testedClass, editor);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void addGutterIcon(RangeHighlighter rangeHighlighter, final PsiMethod method) {
		rangeHighlighter.setGutterIconRenderer(new GutterIconRenderer() {
			public Icon getIcon() {
				return Icons.METHOD_NOT_TESTED;
			}


			public String getTooltipText() {
				return "Create JUnit Test for method: " + method.getName();
			}


			public boolean isNavigateAction() {
				return true;
			}


			public AnAction getClickAction() {
				return new ToggleTestCaseTestedClassAction(method);
			}
		});
	}


	public boolean isHighlightSetters() {
		return highlightSetters;
	}


	public boolean isHighlightGetters() {
		return highlightGetters;
	}


	protected void register() {
		OpenApiFacade.getFileEditorManager(project).addFileEditorManagerListener(this);
		initPsiTreeListener();
	}


	public void mark() {
		Editor[] allEditors = getEditors();
		for (int i = 0; i < allEditors.length; i++) {
			Editor editor = allEditors[i];
			markUntestedMethodsInEditor(editor);
		}
	}


	protected void unregister() {
		OpenApiFacade.getFileEditorManager(project).removeFileEditorManagerListener(this);
		OpenApiFacade.getPsiManager(project).removePsiTreeChangeListener(psiTreeChangeListener);
	}


	public void unmark() {
		Editor[] allEditors = getEditors();
		for (int i = 0; i < allEditors.length; i++) {
			removeUntestedMethodHighlighters(allEditors[i]);
		}
	}


	private Editor[] getEditors() {
		Editor[] allEditors = OpenApiFacade.getAllEditors();
		return allEditors;
	}


	protected boolean isTestClass(PsiClass psiClass) {
		return ToggleTestCaseTestedClassAction.isUnitTest(psiClass);
	}


	protected void markMethod(PsiMethod method, Document document) {
		int line = getMethodStartLineNumber(method, document);
		MarkupModel markupModel = document.getMarkupModel(project);
		RangeHighlighter rangeHighlighter = markupModel.addLineHighlighter(line, HighlighterLayer.FIRST, null);
		addGutterIcon(rangeHighlighter, method);
	}


	private void initPsiTreeListener() {
		LOG.debug("PsiTreeChangeAdapter creation");
		psiTreeChangeListener = new PsiTreeChangeAdapter() {

			public void childrenChanged(PsiTreeChangeEvent event) {
				updateTreeFromPsiTreeChange(event);
			}


			public void childReplaced(PsiTreeChangeEvent event) {
				updateTreeFromPsiTreeChange(event);
			}
		};
		PsiManager manager = OpenApiFacade.getPsiManager(project);
		manager.addPsiTreeChangeListener(psiTreeChangeListener);
	}


	private void doMarkUntestedMethods(PsiClass testedClass, Editor editor) {
		Set methods = getUntestedMethods(testedClass);
		removeUntestedMethodHighlighters(editor);
		for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
			PsiMethod method = (PsiMethod) iterator.next();
			markMethod(method, editor.getDocument());
		}
	}


	private void removeUntestedMethodHighlighters(Editor editor) {
		if (editor == null) {
			return;
		}
		MarkupModel markupModel = editor.getDocument().getMarkupModel(project);
		RangeHighlighter[] rangeHighlighters = markupModel.getAllHighlighters();
		for (int i = 0; i < rangeHighlighters.length; i++) {
			RangeHighlighter rangeHighlighter = rangeHighlighters[i];
			if (isMethodUntestedHighlighter(rangeHighlighter)) {
				markupModel.removeHighlighter(rangeHighlighter);
			}
		}
	}


	private boolean isMethodUntestedHighlighter(RangeHighlighter rangeHighlighter) {
		return rangeHighlighter.getGutterIconRenderer() != null && rangeHighlighter.getGutterIconRenderer().getIcon() != null && rangeHighlighter.getGutterIconRenderer().getIcon().equals(Icons.METHOD_NOT_TESTED);
	}


	public Set getUntestedMethods(PsiClass psiClass) {
		Set untestedMethods = new HashSet();
		PsiClass testClass = classLocator.findTestClass(psiClass);
		PsiMethod[] methods = psiClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			PsiMethod method = methods[i];
			if ((isMethodTestable(method)) && (!isMethodTested(method, testClass))) {
				untestedMethods.add(method);
			}
		}
		return untestedMethods;
	}


	private boolean isMethodTestable(PsiMethod method) {
		return !isPrivate(method) && !isAbstract(method) && !matchesExcludedPattern(method);
	}


	private boolean isPrivate(PsiMethod method) {
		return method.getModifierList().hasModifierProperty(PsiModifier.PRIVATE);
	}


	private boolean isAbstract(PsiMethod method) {
		return method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT);
	}


	private boolean isMethodTested(PsiMethod method, PsiClass testClass) {
		if (testClass == null) {
			return false;
		}
		PsiMethod[] testMethods = methodLocator.findTestMethods(method, testClass);
		return testMethods != null && testMethods.length > 0;
	}


	private boolean matchesExcludedPattern(PsiMethod method) {
		return isExcludedGetter(method) || isExcludedSetter(method);
	}


	private boolean isExcludedSetter(PsiMethod method) {
		return !highlightSetters && method.getName().startsWith("set");
	}


	private boolean isExcludedGetter(PsiMethod method) {
		return !highlightGetters && (method.getName().startsWith("get") || method.getName().startsWith("is"));
	}


	private boolean isClassTestable(PsiClass psiClass) {
		return !psiClass.isInterface() && !isTestClass(psiClass);
	}


	private int getMethodStartLineNumber(PsiMethod method, Document document) {
		int offset = method.getNameIdentifier().getTextOffset();
		return document.getLineNumber(offset);
	}


	private void startTimer() {
		stopTimer();
		timer = new Timer(1500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				debug("Started marking untested methods");
				stopTimer();
				PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
				if (documentManager.hasUncommitedDocuments()) {
					documentManager.commitAllDocuments();
				}
				markUntestedMethodsInEditor(selectedEditor);
				debug("Finished marking untested methods");
			}
		});
		timer.setRepeats(false);
		timer.setCoalesce(true);
		timer.start();
	}


	private void stopTimer() {
		if (timer != null) {
			timer.stop();
			timer = null;
		}
	}


	private void debug(String message) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(message);
		}
	}
}*/

