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

package org.gradle.internal.execution.steps;

import com.google.common.collect.ImmutableCollection;

import java.util.Optional;

public interface ValidationContext extends AfterPreviousExecutionContext {
    /**
     * Returns validation warnings or {@link Optional#empty()} if there were no validation problems.
     */
    Optional<ValidationResult> getValidationProblems();

    interface ValidationResult {
        ImmutableCollection<String> getWarnings();
    }
}
