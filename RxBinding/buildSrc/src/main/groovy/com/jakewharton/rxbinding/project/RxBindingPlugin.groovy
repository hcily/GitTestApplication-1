package com.jakewharton.rxbinding.project

import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Top-level plugin for managing tasks for project modules
 **/
class RxBindingPlugin implements Plugin<Project> {

  private static final String INCLUDE_PATTERN = "**/Rx*.java"
  private static final String EXCLUDE_PATTERN = "**/internal/*"

  @Override
  void apply(Project project) {
    project.afterEvaluate {

      // Grab the release variant
      // Convenience approach so that we can grab the source sets off of it
      Collection<LibraryVariant> variants = project.android.libraryVariants
      LibraryVariant variant = variants.find { v -> v.name == "release" }
      List<Collection<File>> variantJavaSources = variant.getSourceSets().collect { it.getJavaDirectories() }

      // Create a "generateKotlinFor" task for generating kotlin bindings
      KotlinGenTask genTask = project.task(type: KotlinGenTask, "generateKotlin") {
        source = variantJavaSources
        include INCLUDE_PATTERN
        exclude EXCLUDE_PATTERN
      } as KotlinGenTask

      genTask.outputs.upToDateWhen { false }
      project.tasks.add(genTask)

      // Task for validating Rx*.java factory method structures
      ValidateBindingsTask validateBindingsTask = project.task(type: ValidateBindingsTask, "validateBindings") {
        source = variantJavaSources
        include INCLUDE_PATTERN
        exclude EXCLUDE_PATTERN
      } as ValidateBindingsTask

      project.tasks.check.dependsOn(validateBindingsTask)
    }
  }

}
