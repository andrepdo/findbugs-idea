package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * $Date$
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.97
 */
final class ImportPluginsDialog extends DialogWrapper {

	private final Project _project;
	private final List<String> _validPlugins;
	private final List<String> _invalidPlugins;
	private final List<PluginPathPane> _invalidPluginPanes;


	ImportPluginsDialog(Project project, PersistencePreferencesBean prefs, List<String> invalidPlugins) {
		super(project, true);
		_project = project;
		_validPlugins = filterPlugins(prefs, invalidPlugins);
		_invalidPlugins = invalidPlugins;
		_invalidPluginPanes = new ArrayList<PluginPathPane>(invalidPlugins.size());
		setTitle("Import plugins");
		init();
	}


	private static List<String> filterPlugins(PersistencePreferencesBean prefs, List<String> excludePlugins) {
		final List<String> result = new ArrayList<String>();
		for (String plugin : prefs.getPlugins()) {
			if (!excludePlugins.contains(plugin)) {
				result.add(plugin);
			}
		}
		return result;
	}


	@Override
	protected JComponent createCenterPanel() {
		final JPanel main = new JPanel();
		main.setPreferredSize(new Dimension(400, 0));
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		for (String plugin : _invalidPlugins) {
			final PluginPathPane pane = new PluginPathPane(_project, plugin);
			_invalidPluginPanes.add(pane);
			main.add(pane);
		}
		return main;
	}


	List<String> getPlugins() {
		final List<String> plugins = new ArrayList<String>();
		plugins.addAll(_validPlugins);
		for (PluginPathPane plugin : _invalidPluginPanes) {
			if (plugin.isImport()) {
				try {
					plugins.add(FindBugsPreferences.getPluginAsString(plugin.getPlugin()));
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return plugins;
	}


	@Override
	protected ValidationInfo doValidate() {
		ValidationInfo info;
		for (PluginPathPane plugin : _invalidPluginPanes) {
			info = plugin.doValidate();
			if (info != null) {
				return info;
			}
		}
		return null;
	}


	private static FileChooserDescriptor createJarChooserDescriptor() {
		return new FileChooserDescriptor(true, false, true, false, true, false) {
			@Override
			public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
				return super.isFileVisible(file, showHiddenFiles) &&
						(file.isDirectory() || "jar".equals(file.getExtension()) || file.getFileType() == StdFileTypes.ARCHIVE);
			}

			@Override
			public boolean isFileSelectable(VirtualFile file) {
				return file.getFileType() == StdFileTypes.ARCHIVE;
			}
		};
	}


	private static class PluginPathPane extends JPanel {

		private final JCheckBox _importCheckbox;
		private final TextFieldWithBrowseButton _pathTextField;


		PluginPathPane(Project project, String plugin) {
			super(new BorderLayout());
			_importCheckbox = new JCheckBox("Import");
			_importCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					_pathTextField.setEnabled(isImport());
				}
			});
			_pathTextField = new TextFieldWithBrowseButton();
			_pathTextField.setEnabled(false);
			try {
				_pathTextField.setText(FindBugsPreferences.getPluginAsFile(plugin).getPath());
			} catch (MalformedURLException e) {
				_pathTextField.setText(plugin);
			}
			_pathTextField.addBrowseFolderListener("Choose plugin", "Please select the plugin archive.", project, createJarChooserDescriptor());
			add(_importCheckbox, BorderLayout.NORTH);
			add(_pathTextField);
			add(new Box.Filler(new Dimension(16, 0), new Dimension(16, 0), new Dimension(16, 0)), BorderLayout.WEST);
		}


		boolean isImport() {
			return _importCheckbox.isSelected();
		}


		private File getPlugin() {
			return new File(getPath());
		}


		private String getPath() {
			return _pathTextField.getText();
		}


		ValidationInfo doValidate() {
			if (isImport()) {
				final String path = getPath();
				if (null != path) {
					final File archive = new File(path);
					if (!FindBugsPreferences.checkPlugin(archive)) {
						return new ValidationInfo("Path is invalid", _pathTextField);
					}
				} else {
					return new ValidationInfo("No path specified", _pathTextField);
				}
			}
			return null;
		}


	}
}
