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

package org.gradle.integtests.resolve.artifacts

import org.gradle.integtests.fixtures.GradleMetadataResolveRunner
import org.gradle.integtests.fixtures.RequiredFeature
import org.gradle.integtests.fixtures.RequiredFeatures
import org.gradle.integtests.resolve.AbstractModuleDependencyResolveTest
import spock.lang.Unroll

/**
 * There is more test coverage for 'dependency artifacts' in ArtifactDependenciesIntegrationTest (old test setup).
 */
@RequiredFeatures(
    // This test bypasses all metadata using 'artifact()' metadata sources. It is sufficient to test with one metadata setup.
    @RequiredFeature(feature = GradleMetadataResolveRunner.GRADLE_METADATA, value = "true")
)
class DependencyArtifactsResolveIntegrationTest extends AbstractModuleDependencyResolveTest {

    def setup() {
        resolve.expectDefaultConfiguration(useMaven() ? 'runtime' : 'default')
        buildFile << """
            repositories.all {
                metadataSources {
                    artifact() //sss
                }
            }
        """
    }

    def "can combine artifact notation and constraints"() {
        given:
        repository {
            'org:foo:1.0' {
                withModule {
                    undeclaredArtifact(type: 'distribution-tgz')
                }
            }
        }

        buildFile << """
            dependencies {
                conf('org:foo@distribution-tgz')
              
                constraints {
                   conf('org:foo:1.0')
                }
            }
        """

        when:
        repositoryInteractions {
            'org:foo:1.0' {
                expectHeadArtifact(type: 'distribution-tgz') // test for the artifact when we would usually download metadata
                expectHeadArtifact(type: 'distribution-tgz') // test for the artifact before actually retrieving it
                expectGetArtifact(type: 'distribution-tgz')
            }
        }
        succeeds 'checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge('org:foo', 'org:foo:1.0') {
                    artifact(type: 'distribution-tgz')
                }
                constraint('org:foo:1.0')
            }
        }
    }

    @Unroll
    def "The first artifact is used as replacement for metadata if multiple artifacts are declared using #declaration"() {
        given:
        repository {
            'org:foo:1.0' {
                withModule {
                    undeclaredArtifact(name: artifactName, type: 'distribution-tgz')
                    undeclaredArtifact(name: artifactName, type: 'zip')
                }
            }
        }

        buildFile << """
            dependencies {
                $declaration
                constraints {
                   conf('org:foo:1.0')
                }
            }
        """

        when:
        repositoryInteractions {
            'org:foo:1.0' {
                // HEAD requests happen in parallel when Gradle downloads metadata.
                // In this cases "downloading metadata" means testing for the artifact.
                // Each declaration is treated separately with it's own "consumer provided" metadata
                // Depending on the timing, this can lead to multiple parallel requests (one for each declaration)
                maybeHeadOrGetArtifact(name: artifactName, type: 'distribution-tgz')
                expectGetArtifact(name: artifactName, type: 'zip')
            }
        }
        succeeds 'checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge('org:foo', 'org:foo:1.0') {
                    artifact(name: artifactName, type: 'distribution-tgz')
                    artifact(name: artifactName, type: 'zip')
                }
                constraint('org:foo:1.0')
            }
        }

        where:
        notation                                               | artifactName | declaration
        'multiple dependency declarations (AT notation)'       | 'foo'        | "conf('org:foo@distribution-tgz'); conf('org:foo@zip')"
        'multiple dependency declarations (artifact notation)' | 'bar'        | "conf('org:foo') { artifact { name = 'bar'; type = 'distribution-tgz' } }; conf('org:foo') { artifact { name = 'bar'; type = 'zip' } }"
        'multiple artifact declaration'                        | 'bar'        | "conf('org:foo') { artifact { name = 'bar'; type = 'distribution-tgz' }; artifact { name = 'bar'; type = 'zip' } }"
    }
}
