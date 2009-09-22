/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import edu.umd.cs.findbugs.Project;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseClassCollector;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseFileCollector;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class FindBugsProject extends Project {

	private static final Logger LOGGER = Logger.getInstance(FindBugsProject.class.getName());
	private VirtualFile[] _outputFiles;
	//private RecurseCollectorTask _collectorTask;


	public FindBugsProject() {
		super();
	}


	public void configureSourceDirectories(final VirtualFile file) {
		final VirtualFile[] files = new VirtualFile[] {file};
		configureSourceDirectories(files);
	}


	public void configureSourceDirectories(final VirtualFile[] selectedSourceFiles) {
		for (final VirtualFile file : selectedSourceFiles) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				final VirtualFile parent = file.getParent();
				if (parent != null && parent.isDirectory()) {
					addSourceDir(parent.getPresentableUrl());
					LOGGER.debug("adding source dir: " + parent.getPresentableUrl());// NON-NLS

					/*ApplicationManager.getApplication().invokeLater(new Runnable() {
						public void run() {*/
					//_collectorTask.setIndicatorText("adding source dir: " + parent.getPresentableUrl());
					/*}
					});*/
				}
			} else if (file.isDirectory()) { // package dir
				addSourceDir(file.getPresentableUrl());
				LOGGER.debug("adding source dir: " + file.getPresentableUrl());// NON-NLS
			}
		}
	}


	public void configureAuxClasspathEntries(final VirtualFile[] classpathFiles) {
		for (final VirtualFile file : classpathFiles) {
			addAuxClasspathEntry(file.getPresentableUrl());
			//_findBugsTask.getProgressIndicator().setText("Collecting auxiliary classpath entires...");
			LOGGER.debug("adding aux classpath entry: " + file.getPresentableUrl());// NON-NLS

			/*ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {*/
			//_collectorTask.setIndicatorText("adding aux classpath entry: " + file.getPresentableUrl());
			/*}
			});*/
		}
	}


	public void configureOutputFiles(final com.intellij.openapi.project.Project project, final VirtualFile[] selectedSourceFiles) {
		//final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(dataContext);
		_outputFiles = selectedSourceFiles.clone();

		RecurseClassCollector classCollector = null;
		for (final VirtualFile file : selectedSourceFiles) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				classCollector = new RecurseClassCollector(this, project);
				//classCollector.setVirtualFile(file);
				classCollector.addContainingClasses(file);
			}
		}

		// clear for gc
		if (classCollector != null) {
			classCollector.getResult().clear();
		}
	}


	public void configureOutputFile(final com.intellij.openapi.project.Project project, final PsiClass selectedPsiClass) {
		//final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(dataContext);
		final VirtualFile vFile = IdeaUtilImpl.getVirtualFile(selectedPsiClass);
		if (vFile != null) {
			_outputFiles = new VirtualFile[] {vFile};

			final RecurseClassCollector classCollector = new RecurseClassCollector(this, project);
			//classCollector.setVirtualFile(file);
			classCollector.addContainingClasses(selectedPsiClass);

			// clear for gc
			classCollector.getResult().clear();
		}
	}


	public void configureOutputFiles(final VirtualFile selectedPackage, final com.intellij.openapi.project.Project project) {
		final VirtualFile path = IdeaUtilImpl.getCompilerOutputPath(selectedPackage, project);
		//System.err.println(selectedPackage.getPresentableUrl());
		//System.err.println(selectedPackage.getPresentableName());
		assert path != null;
		//System.err.println("####### path " + path.getPresentableUrl());
		//System.err.println(IdeaUtilImpl.findFileByIoFile(new File(selectedPackage.getPath())));
		RecurseFileCollector.addFiles(this, new File(selectedPackage.getPath()));
	}


	public void configureOutputFiles(final String path) {
		RecurseFileCollector.addFiles(this, new File(path));
	}


	public VirtualFile[] getConfiguredOutputFiles() {
		return _outputFiles.clone();
	}


	/*public void setCollectorTask(final RecurseCollectorTask collectorTask) {
		_collectorTask = collectorTask;
	}


	public RecurseCollectorTask getCollectorTask() {
		return _collectorTask;
	}*/
}
