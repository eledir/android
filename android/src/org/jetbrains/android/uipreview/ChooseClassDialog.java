/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.android.uipreview;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Alexander Lobas
 */
public class ChooseClassDialog extends DialogWrapper implements ListSelectionListener {
  private final JList myList = new JBList();
  private final JScrollPane myComponent = ScrollPaneFactory.createScrollPane(myList);
  private final Condition<PsiClass> myFilter;
  private String myResultClassName;

  public ChooseClassDialog(Module module, String title, boolean includeAll, @Nullable Condition<PsiClass> filter, String... classes) {
    super(module.getProject());
    myFilter = filter;

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(MouseEvent e) {
        if (myList.getSelectedValue() != null) {
          close(OK_EXIT_CODE);
          return true;
        }
        return false;
      }
    }.installOn(myList);

    DefaultListModel model = new DefaultListModel();
    findClasses(module, includeAll, model, classes);
    myList.setModel(model);
    myList.setCellRenderer(new DefaultPsiElementCellRenderer());

    ListSelectionModel selectionModel = myList.getSelectionModel();
    selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    selectionModel.addListSelectionListener(this);

    new ListSpeedSearch(myList) {
      @Override
      protected boolean isMatchingElement(Object element, String pattern) {
        PsiClass psiClass = (PsiClass)element;
        return compare(psiClass.getName(), pattern) || compare(psiClass.getQualifiedName(), pattern);
      }
    };

    setTitle(title);
    setOKActionEnabled(false);

    init();

    Dimension size = myComponent.getPreferredSize();
    size.height = myList.getPreferredSize().height + 20;
    myComponent.setPreferredSize(size);
  }

  protected void findClasses(Module module, boolean includeAll, DefaultListModel model, String[] classes) {
    for (String className : classes) {
      for (PsiClass psiClass : findInheritors(module, className, includeAll)) {
        if (myFilter != null && !myFilter.value(psiClass)) {
          continue;
        }
        model.addElement(psiClass);
      }
    }
  }

  public static Collection<PsiClass> findInheritors(Module module, String name, boolean includeAll) {
    PsiClass base = findClass(module, name);
    if (base != null) {
      GlobalSearchScope scope = includeAll ?
                                GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false) :
                                GlobalSearchScope.moduleScope(module);
      Collection<PsiClass> classes;
      try {
        classes = ClassInheritorsSearch.search(base, scope, true).findAll();
      }
      catch (IndexNotReadyException e) {
        classes = Collections.emptyList();
      }
      return classes;
    }
    return Collections.emptyList();
  }

  @Nullable
  public static PsiClass findClass(Module module, @Nullable String name) {
    if (name == null) {
      return null;
    }
    Project project = module.getProject();
    PsiClass aClass;
    try {
      aClass = JavaPsiFacade.getInstance(project).findClass(name, GlobalSearchScope.allScope(project));
    }
    catch (IndexNotReadyException e) {
      aClass = null;
    }
    return aClass;
  }

  /**
   * Open a dialog if indices are available, otherwise show an error message.
   *
   * @return class name if user has selected one, null otherwise
   */
  @Nullable
  public static String openDialog(Module module, String title,
                                  boolean includeAll,
                                  @Nullable Condition<PsiClass> filter,
                                  @NotNull String... classes) {
    final Project project = module.getProject();
    final DumbService dumbService = DumbService.getInstance(project);
    if (dumbService.isDumb()) {
      // Variable "title" contains a string like "Classes", "Activities", "Fragments".
      dumbService.showDumbModeNotification(String.format("%1$s are not available while indices are updating.", title));
      return null;
    }

    final ChooseClassDialog dialog = new ChooseClassDialog(module, title, includeAll, filter, classes);
    return dialog.showAndGet() ? dialog.getClassName() : null;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myList;
  }

  @Override
  protected JComponent createCenterPanel() {
    return myComponent;
  }

  public String getClassName() {
    return myResultClassName;
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    PsiClass psiClass = (PsiClass)myList.getSelectedValue();
    setOKActionEnabled(psiClass != null);
    myResultClassName = psiClass == null ? null : psiClass.getQualifiedName();
  }
}
