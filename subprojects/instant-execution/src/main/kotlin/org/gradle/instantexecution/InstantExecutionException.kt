/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.instantexecution

import org.gradle.instantexecution.serialization.PropertyProblem
import org.gradle.internal.exceptions.Contextual
import org.gradle.internal.exceptions.DefaultMultiCauseException


// TODO sealed class hierarchy?


@Contextual
abstract class InstantExecutionException(
    message: String,
    causes: Iterable<Throwable> = emptyList()
) : DefaultMultiCauseException(message, causes)


// TODO make this NOT an InstantExecutionException and let it pop out of the failure barrier
//  then needs to be @Contextual in order to be reported in * What went wrong:
class InstantExecutionErrorsException(errors: List<PropertyProblem.Error>) : InstantExecutionException(
    "Instant execution state could not be cached.",
    errors.map(PropertyProblem.Error::exception) // TODO unique by description!
)


class TooManyInstantExecutionProblemsException(summary: String, problems: List<PropertyProblem>) : InstantExecutionException(
    "${MESSAGE}\n$summary", problems.mapNotNull(PropertyProblem::exception) // TODO unique by description!
) {
    companion object {
        const val MESSAGE = "Maximum number of instant execution problems has been reached.\nThis behavior can be adjusted via -D${SystemProperties.maxProblems}=<integer>."
    }
}


class InstantExecutionProblemsException(summary: String, problems: List<PropertyProblem>) : InstantExecutionException(
    "$MESSAGE\n$summary", problems.mapNotNull(PropertyProblem::exception) // TODO unique by description!
) {
    companion object {
        const val MESSAGE = "Problems found while caching instant execution state.\nFailing because -D${SystemProperties.failOnProblems} is 'true'."
    }
}


@Contextual
class InstantExecutionProblemException(message: String, cause: Throwable? = null) : Exception(message, cause)
