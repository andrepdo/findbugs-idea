package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;
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
import java.util.Collection;
import java.util.List;

/**
 * $Date$
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.97
 */
final class ImportPluginsDialog extends DialogWrapper {

	private static final Logger LOGGER = Logger.getInstance(ImportPluginsDialog.class.getName());

	private final Project _project;
	private final List<String> _validPlugins;
	private final List<String> _invalidPlugins;
	private final List<PluginPathPane> _invalidPluginPanes;


	ImportPluginsDialog(final Project project, final PersistencePreferencesBean prefs, final List<String> invalidPlugins) {
		super(project, true);
		_project = project;
		_validPlugins = filterPlugins(prefs, invalidPlugins);
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_invalidPlugins = invalidPlugins;
		_invalidPluginPanes = new ArrayList<PluginPathPane>(invalidPlugins.size());
		//noinspection DialogTitleCapitalization
		setTitle("Import plugins");
		init();
	}


	private static List<String> filterPlugins(final PersistencePreferencesBean prefs, final Collection<String> excludePlugins) {
		final List<String> result = new ArrayList<String>();
		for (final String plugin : prefs.getPlugins()) {
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
		for (final String plugin : _invalidPlugins) {
			final PluginPathPane pane = new PluginPathPane(_project, plugin);
			_invalidPluginPanes.add(pane);
			main.add(pane);
		}
		return main;
	}


	Collection<String> getPlugins() {
		final List<String> plugins = new ArrayList<String>();
		plugins.addAll(_validPlugins);
		for (final PluginPathPane plugin : _invalidPluginPanes) {
			if (plugin.isImport()) {
				try {
					plugins.add(FindBugsCustomPluginUtil.getAsString(plugin.getPlugin()));
				} catch (final MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return plugins;
	}


	@Override
	protected ValidationInfo doValidate() {
		ValidationInfo info;
		for (final PluginPathPane plugin : _invalidPluginPanes) {
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
			public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
				return super.isFileVisible(file, showHiddenFiles) &&
						(file.isDirectory() || "jar".equals(file.getExtension()) || file.getFileType() == StdFileTypes.ARCHIVE);
			}

			@Override
			public boolean isFileSelectable(final VirtualFile file) {
				return file.getFileType() == StdFileTypes.ARCHIVE;
			}
		};
	}


	private static class PluginPathPane extends JPanel {

		private final JCheckBox _importCheckbox;
		private final TextFieldWithBrowseButton _pathTextField;


		PluginPathPane(final Project project, final String plugin) {
			super(new BorderLayout());
			_importCheckbox = new JCheckBox("Import");
			_importCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					_pathTextField.setEnabled(isImport());
				}
			});
			_pathTextField = new TextFieldWithBrowseButton();
			_pathTextField.setEnabled(false);
			try {
				_pathTextField.setText(FindBugsCustomPluginUtil.getAsFile(plugin).getPath());
			} catch (final MalformedURLException e) {
				LOGGER.debug("invalid plugin=" + plugin, e);
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
					if (!FindBugsCustomPluginUtil.check(archive)) {
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
