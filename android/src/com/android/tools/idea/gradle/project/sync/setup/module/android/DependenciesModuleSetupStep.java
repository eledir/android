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
package com.android.tools.idea.gradle.project.sync.setup.module.android;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.SyncIssue;
import com.android.tools.idea.gradle.LibraryFilePaths;
import com.android.tools.idea.gradle.project.model.AndroidModuleModel;
import com.android.tools.idea.gradle.project.sync.SyncAction;
import com.android.tools.idea.gradle.project.sync.issues.SyncIssuesReporter;
import com.android.tools.idea.gradle.project.sync.issues.UnresolvedDependenciesReporter;
import com.android.tools.idea.gradle.project.sync.setup.module.AndroidModuleSetupStep;
import com.android.tools.idea.gradle.project.sync.setup.module.common.DependencySetupErrors;
import com.android.tools.idea.gradle.project.sync.setup.module.dependency.DependenciesExtractor;
import com.android.tools.idea.gradle.project.sync.setup.module.dependency.DependencySet;
import com.android.tools.idea.gradle.project.sync.setup.module.dependency.LibraryDependency;
import com.android.tools.idea.gradle.project.sync.setup.module.dependency.ModuleDependency;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.sdk.AndroidSdkAdditionalData;
import org.jetbrains.android.sdk.AndroidSdkData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import static com.android.SdkConstants.FD_JARS;
import static com.android.tools.idea.gradle.project.sync.setup.module.dependency.LibraryDependency.PathType.BINARY;
import static com.android.tools.idea.gradle.project.sync.setup.module.dependency.LibraryDependency.PathType.DOCUMENTATION;
import static com.android.tools.idea.gradle.util.FilePaths.findParentContentEntry;
import static com.android.tools.idea.gradle.util.FilePaths.pathToIdeaUrl;
import static com.android.tools.idea.gradle.util.Projects.setModuleCompiledArtifact;
import static com.intellij.openapi.roots.DependencyScope.COMPILE;
import static com.intellij.openapi.roots.OrderRootType.CLASSES;
import static com.intellij.openapi.util.io.FileUtil.*;
import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static com.intellij.openapi.vfs.VfsUtilCore.virtualToIoFile;

public class DependenciesModuleSetupStep extends AndroidModuleSetupStep {
  @NotNull private final DependenciesExtractor myDependenciesExtractor;
  @NotNull private final AndroidModuleDependenciesSetup myDependenciesSetup;

  @NotNull
  public static DependenciesModuleSetupStep getInstance() {
    for (AndroidModuleSetupStep step : AndroidModuleSetupStep.getExtensions()) {
      if (step instanceof DependenciesModuleSetupStep) {
        return (DependenciesModuleSetupStep)step;
      }
    }
    throw new AssertionError("Unable to find an instance of " + DependenciesModuleSetupStep.class.getSimpleName());
  }

  @SuppressWarnings("unused") // Instantiated by IDEA
  public DependenciesModuleSetupStep(@NotNull DependenciesExtractor dependenciesExtractor, @NotNull LibraryFilePaths libraryFilePaths) {
    this(dependenciesExtractor, new AndroidModuleDependenciesSetup(libraryFilePaths));
  }

  @VisibleForTesting
  DependenciesModuleSetupStep(@NotNull DependenciesExtractor dependenciesExtractor, @NotNull AndroidModuleDependenciesSetup dependenciesSetup) {
    myDependenciesExtractor = dependenciesExtractor;
    myDependenciesSetup = dependenciesSetup;
  }

  @Override
  protected void doSetUpModule(@NotNull Module module,
                               @NotNull IdeModifiableModelsProvider ideModelsProvider,
                               @NotNull AndroidModuleModel androidModel,
                               @Nullable SyncAction.ModuleModels gradleModels,
                               @Nullable ProgressIndicator indicator) {
    AndroidProject androidProject = androidModel.getAndroidProject();

    DependencySet dependencies = myDependenciesExtractor.extractFrom(androidModel);
    for (LibraryDependency dependency : dependencies.onLibraries()) {
      updateLibraryDependency(module, ideModelsProvider, dependency, androidProject);
    }
    for (ModuleDependency dependency : dependencies.onModules()) {
      updateModuleDependency(module, ideModelsProvider, dependency, androidProject);
    }

    addExtraSdkLibrariesAsDependencies(module, ideModelsProvider, androidProject);

    Collection<SyncIssue> syncIssues = androidModel.getSyncIssues();
    if (syncIssues != null) {
      SyncIssuesReporter.getInstance().report(syncIssues, module);
    }
    else {
      //noinspection deprecation
      Collection<String> unresolvedDependencies = androidProject.getUnresolvedDependencies();
      UnresolvedDependenciesReporter.getInstance().report(unresolvedDependencies, module);
    }
  }

  private void updateModuleDependency(@NotNull Module module,
                                      @NotNull IdeModifiableModelsProvider modelsProvider,
                                      @NotNull ModuleDependency dependency,
                                      @NotNull AndroidProject androidProject) {
    Module moduleDependency = dependency.getModule(modelsProvider);
    LibraryDependency compiledArtifact = dependency.getBackupDependency();

    if (moduleDependency != null) {
      ModuleOrderEntry orderEntry = modelsProvider.getModifiableRootModel(module).addModuleOrderEntry(moduleDependency);
      orderEntry.setScope(dependency.getScope());
      orderEntry.setExported(true);

      if (compiledArtifact != null) {
        setModuleCompiledArtifact(moduleDependency, compiledArtifact);
      }
      return;
    }

    DependencySetupErrors dependencySetupErrors = DependencySetupErrors.getInstance(module.getProject());
    String backupName = compiledArtifact != null ? compiledArtifact.getName() : null;
    dependencySetupErrors.addMissingModule(dependency.getGradlePath(), module.getName(), backupName);

    // fall back to library dependency, if available.
    if (compiledArtifact != null) {
      updateLibraryDependency(module, modelsProvider, compiledArtifact, androidProject);
    }
  }

  public void updateLibraryDependency(@NotNull Module module,
                                      @NotNull IdeModifiableModelsProvider modelsProvider,
                                      @NotNull LibraryDependency dependency,
                                      @NotNull AndroidProject androidProject) {
    String name = dependency.getName();
    DependencyScope scope = dependency.getScope();
    myDependenciesSetup.setUpLibraryDependency(module, modelsProvider, name, scope, dependency.getArtifactPath(),
                                               dependency.getPaths(BINARY), dependency.getPaths(DOCUMENTATION));

    File buildFolder = androidProject.getBuildFolder();

    // Exclude jar files that are in "jars" folder in "build" folder.
    // see https://code.google.com/p/android/issues/detail?id=123788
    ContentEntry[] contentEntries = modelsProvider.getModifiableRootModel(module).getContentEntries();
    if (contentEntries.length > 0) {
      for (File binaryPath : dependency.getPaths(BINARY)) {
        File parent = binaryPath.getParentFile();
        if (parent != null && FD_JARS.equals(parent.getName()) && isAncestor(buildFolder, parent, true)) {
          ContentEntry parentContentEntry = findParentContentEntry(parent, contentEntries);
          if (parentContentEntry != null) {
            parentContentEntry.addExcludeFolder(pathToIdeaUrl(parent));
          }
        }
      }
    }
  }

  /**
   * Sets the 'useLibrary' libraries or SDK add-ons as library dependencies.
   * <p>
   * These libraries are set at the project level, which makes it impossible to add them to a IDE SDK definition because the IDE SDK is
   * global to the whole IDE. To work around this limitation, we set these libraries as module dependencies instead.
   * </p>
   */
  private void addExtraSdkLibrariesAsDependencies(@NotNull Module module,
                                                  @NotNull IdeModifiableModelsProvider modelsProvider,
                                                  @NotNull AndroidProject androidProject) {
    ModifiableRootModel moduleModel = modelsProvider.getModifiableRootModel(module);
    Sdk sdk = moduleModel.getSdk();
    assert sdk != null; // If we got here, SDK will *NOT* be null.

    String suffix = null;
    AndroidSdkData sdkData = AndroidSdkData.getSdkData(sdk);
    if (sdkData != null) {
      SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof AndroidSdkAdditionalData) {
        AndroidSdkAdditionalData androidSdkData = (AndroidSdkAdditionalData)data;
        suffix = androidSdkData.getBuildTargetHashString();
      }
    }

    if (suffix == null) {
      // In practice, we won't get here. A proper Android SDK has been already configured by now, and the suffix won't be null.
      suffix = androidProject.getCompileTarget();
    }

    Set<String> currentIdeSdkFilePaths = Sets.newHashSetWithExpectedSize(5);
    for (VirtualFile sdkFile : sdk.getRootProvider().getFiles(CLASSES)) {
      // We need to convert the VirtualFile to java.io.File, because the path of the VirtualPath is using 'jar' protocol and it won't match
      // the path returned by AndroidProject#getBootClasspath().
      File sdkFilePath = virtualToIoFile(sdkFile);
      currentIdeSdkFilePaths.add(sdkFilePath.getPath());
    }
    Collection<String> bootClasspath = androidProject.getBootClasspath();
    for (String library : bootClasspath) {
      if (isNotEmpty(library) && !currentIdeSdkFilePaths.contains(library)) {
        // Library is not in the SDK IDE definition. Add it as library and make the module depend on it.
        File binaryPath = new File(library);
        String name = binaryPath.isFile() ? getNameWithoutExtension(binaryPath) : sanitizeFileName(library);
        // Include compile target as part of the name, to ensure the library name is unique to this Android platform.

        name = name + "-" + suffix; // e.g. maps-android-23, effects-android-23 (it follows the library naming convention: library-version
        myDependenciesSetup.setUpLibraryDependency(module, modelsProvider, name, COMPILE, binaryPath);
      }
    }
  }

  @Override
  @NotNull
  public String getDescription() {
    return "Android dependencies setup";
  }

  @Override
  public boolean invokeOnBuildVariantChange() {
    return true;
  }
}
