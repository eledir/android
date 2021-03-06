/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.tests.gui.framework.fixture;

import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.ui.ConflictsDialog;
import com.intellij.ui.EditorTextField;
import org.fest.swing.core.Robot;
import org.fest.swing.core.matcher.JTextComponentMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.timing.Wait;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.JTextComponent;
import java.awt.event.KeyEvent;
import java.io.File;

import static com.android.tools.idea.tests.gui.framework.GuiTests.findAndClickButton;

public class RenameRefactoringDialogFixture extends IdeaDialogFixture<RenameDialog> {
  private RenameRefactoringDialogFixture(@NotNull Robot robot, @NotNull DialogAndWrapper<RenameDialog> dialogAndWrapper) {
    super(robot, dialogAndWrapper);
  }

  @NotNull
  public RenameRefactoringDialogFixture setNewName(@NotNull final String newName) {
    final EditorTextField field = robot().finder().findByType(target(), EditorTextField.class);
    GuiActionRunner.execute(new GuiTask() {
      @Override
      protected void executeInEDT() throws Throwable {
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> {
          IdeFocusManager.getGlobalInstance().requestFocus(field, true);
        });
      }
    });
    robot().pressAndReleaseKey(KeyEvent.VK_BACK_SPACE); // to make sure we don't append to existing item on Linux
    robot().enterText(newName);
    Wait.seconds(1).expecting("EditorTextField to show new name").until(() -> newName.equals(field.getText()));
    return this;
  }

  @NotNull
  public RenameRefactoringDialogFixture clickRefactor() {
    findAndClickButton(this, "Refactor");
    return this;
  }

  @NotNull
  public static RenameRefactoringDialogFixture find(@NotNull Robot robot) {
    return new RenameRefactoringDialogFixture(robot, find(robot, RenameDialog.class));
  }

  public static class ConflictsDialogFixture extends IdeaDialogFixture<ConflictsDialog> {
    protected ConflictsDialogFixture(@NotNull Robot robot, @NotNull DialogAndWrapper<ConflictsDialog> dialogAndWrapper) {
      super(robot, dialogAndWrapper);
    }

    @NotNull
    public ConflictsDialogFixture clickContinue() {
      findAndClickButton(this, "Continue");
      return this;
    }

    @NotNull
    public String getHtml() {
      final JTextComponent component = robot().finder().find(target(), JTextComponentMatcher.any());
      return GuiQuery.getNonNull(component::getText);
    }

    @NotNull
    public String getText() {
      String html = getHtml();
      String text = TextFormat.HTML.convertTo(html, TextFormat.TEXT).trim();
      return text.replace(File.separatorChar, '/');
    }

    @NotNull
    public static ConflictsDialogFixture find(@NotNull Robot robot) {
      return new ConflictsDialogFixture(robot, find(robot, ConflictsDialog.class));
    }
  }
}
