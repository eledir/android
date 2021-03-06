/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tools.profilers.cpu;

import com.android.tools.adtui.common.ColumnTreeBuilder;
import com.android.tools.adtui.model.RangedTreeModel;
import com.intellij.codeInspection.ui.OptionAccessor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

public class CpuTraceTreeSorter implements ColumnTreeBuilder.TreeSorter<DefaultMutableTreeNode> {

  @NotNull private JTree myTree;
  private DefaultMutableTreeNode myRoot;
  private TopDownTreeModel myModel;
  private Comparator<DefaultMutableTreeNode> myComparator;

  public CpuTraceTreeSorter(@NotNull JTree tree) {
    myTree = tree;
  }

  public void setModel(TopDownTreeModel model, Comparator<DefaultMutableTreeNode> sorting) {
    myModel = model;
    if (myModel != null) {
      myRoot = (DefaultMutableTreeNode)model.getRoot();
      sort(sorting, SortOrder.UNSORTED); //SortOrder Parameter is not used.
      myTree.invalidate();
    }
  }

  @Override
  public void sort(Comparator<DefaultMutableTreeNode> comparator, SortOrder order) {
    if (myModel != null && myRoot != null) {
      myComparator = comparator;
      TreePath selectionPath = myTree.getSelectionPath();
      sortTree(myRoot);
      myTree.collapseRow(0);
      myTree.setSelectionPath(selectionPath);
      myTree.scrollPathToVisible(selectionPath);
      myModel.reload();
    }
  }
  private void sortTree(@NotNull DefaultMutableTreeNode parent) {
    if (parent.isLeaf()) {
      return;
    }
    int childCount = parent.getChildCount();
    List< DefaultMutableTreeNode> children = new ArrayList<>(childCount);
    for (int i = 0; i < childCount; i++) {
      children.add((DefaultMutableTreeNode) parent.getChildAt(i));
    }

    Collections.sort(children, myComparator);
    parent.removeAllChildren();
    for (DefaultMutableTreeNode node: children) {
      sortTree(node);
      parent.add(node);
    }
  }
}
