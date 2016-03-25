package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.gui.preferences.PluginConfiguration;
import org.twodividedbyzero.idea.findbugs.gui.preferences.importer.SonarProfileImporter;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SonarImporterUtil {

    private static final Logger LOGGER = Logger.getInstance(SonarImporterUtil.class.getName());

    public static final String PERSISTENCE_ROOT_NAME = "findbugs";

    public static void importRules(FindBugsPlugin plugin, String filePath) {
        if (StringUtils.isNotBlank(filePath)) {
            try {
                importRules(plugin, new FileInputStream(filePath));
            } catch (FileNotFoundException e) {
                String message = "File not found (continuing without importing rules): " + filePath;
                LOGGER.warn(message);
                showToolWindowNotifier(plugin.getProject(), message, MessageType.WARNING);
            }
        }
    }

    public static void importRules(FindBugsPlugin plugin, InputStream stream) {
        if (stream == null) {
            showToolWindowNotifier(plugin.getProject(), "Unable to read specified file.", MessageType.WARNING);
            return;
        }
        PersistencePreferencesBean prefs;
        try {
            final Document document = JDOMUtil.loadDocument(stream);
            if (SonarProfileImporter.isValid(document)) {
                prefs = SonarProfileImporter.doImport(plugin.getProject(), document);
                if (prefs == null) {
                    return;
                }
            } else {
                if (!PERSISTENCE_ROOT_NAME.equals(document.getRootElement().getName())) {
                    showToolWindowNotifier(plugin.getProject(), "The file format is invalid.", MessageType.WARNING);
                    return;
                }
                prefs = XmlSerializer.deserialize(document, PersistencePreferencesBean.class);
            }
            if (!validatePreferences(plugin.getProject(), prefs)) {
                return;
            }
            plugin.loadState(prefs);
        } catch (final Exception ex) {
            LOGGER.warn(ex);
            final String msg = ex.getLocalizedMessage();
            FindBugsPluginImpl.showToolWindowNotifier(plugin.getProject(),
                    "Import failed! " + (msg != null && !msg.isEmpty() ? msg : ex.toString()), MessageType.WARNING);
        }
    }

    private static boolean validatePreferences(Project project, @Nullable final PersistencePreferencesBean prefs) {
        if (prefs == null) {
            showToolWindowNotifier(project, "File not well configured", MessageType.WARNING);
            return false;
        } else if (prefs.isEmpty()) {
            showToolWindowNotifier(project, "Configuration is empty, skipping it.", MessageType.WARNING);
            return false;
        } else if (!FindBugsPreferences.collectInvalidPlugins(prefs.getPlugins()).isEmpty()) {
            return false;
        }
        return true;
    }

    private static void showToolWindowNotifier(final Project project, final String message, final MessageType type) {
        EventDispatchThreadHelper.invokeLater(new Runnable() {
            public void run() {
                FindBugsPluginImpl.showToolWindowNotifier(project, message, type);
            }
        });
    }
}
