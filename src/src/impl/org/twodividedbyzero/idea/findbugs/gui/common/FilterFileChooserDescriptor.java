package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Custom FileChooser Descriptor that allows the specification of a file filter.
 */
public class FilterFileChooserDescriptor extends FileChooserDescriptor {
  private final FileFilter _fileFilter;

  /**
   * Construct a file chooser descriptor for the given file filter.
   *
   * @param title       the dialog title.
   * @param description the dialog description.
   * @param filter      the file filter.
   */
  public FilterFileChooserDescriptor(final String title, final String description, final FileFilter filter) {
    // select a single file, not jar contents
    super(true, false, true, true, false, false);
    setTitle(title);
    setDescription(description);
    _fileFilter = filter;
  }

  /**
   * Construct a file chooser descriptor for directories only.
   *
   * @param title the dialog title.
   * @param description the dialog description.
   */
  public FilterFileChooserDescriptor(final String title, final String description) {
    // select a single file, not jar contents
    super(false, true, false, true, false, false);
    setTitle(title);
    setDescription(description);
    _fileFilter = new DirectoryFileFilter();
  }

  @Override
  public boolean isFileSelectable(final VirtualFile file) {
    return _fileFilter.accept(VfsUtilCore.virtualToIoFile(file));
  }

  @Override
  public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
    return file.isDirectory() || _fileFilter.accept(VfsUtilCore.virtualToIoFile(file));
  }

  /**
   * File filter that only accepts directories.
   */
  private static class DirectoryFileFilter extends FileFilter {
    public DirectoryFileFilter() {
    }

    @Override
    public boolean accept(final File file) {
      return file.isDirectory();
    }

    @Override
    public String getDescription() {
      return "directories-only";
    }
  }
}
