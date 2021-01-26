/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.configurationcache.inputs.undeclared

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.test.fixtures.file.TestFile

trait KotlinPluginImplementation {
    void kotlinPlugin(TestFile sourceFile, SystemPropertyRead read) {
        sourceFile << """
            import ${Project.name}
            import ${Plugin.name}

            class SneakyPlugin: Plugin<Project> {
                override fun apply(project: Project) {
                    var value = ${read.kotlinExpression}
                    println("apply = " + value)

                    // Call from a function body
                    val f = { p: String ->
                        println("\$p FUNCTION = " + System.getProperty("FUNCTION"))
                    }
                    f("apply")

                    project.tasks.register("thing") {
                        doLast {
                            var value = ${read.kotlinExpression}
                            println("task = " + value)

                            f("task")
                        }
                    }
                }
            }
        """
    }

    void kotlinDsl(TestFile sourceFile, SystemPropertyRead read) {
        sourceFile << """
            println("apply = " + ${read.kotlinExpression})

            tasks.register("thing") {
                doLast {
                    println("task = " + ${read.kotlinExpression})
                }
            }
        """
    }
}
