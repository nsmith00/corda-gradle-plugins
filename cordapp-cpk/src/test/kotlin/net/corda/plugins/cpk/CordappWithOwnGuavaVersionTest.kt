package net.corda.plugins.cpk

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter
import org.junit.jupiter.api.io.TempDir
import org.osgi.framework.Constants.BUNDLE_LICENSE
import org.osgi.framework.Constants.BUNDLE_NAME
import org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME
import org.osgi.framework.Constants.BUNDLE_VENDOR
import org.osgi.framework.Constants.BUNDLE_VERSION
import org.osgi.framework.Constants.EXPORT_PACKAGE
import org.osgi.framework.Constants.IMPORT_PACKAGE
import org.osgi.framework.Constants.REQUIRE_CAPABILITY
import java.nio.file.Path
import java.util.jar.JarFile

class CordappWithOwnGuavaVersionTest {
    companion object {
        const val cordaGuavaVersion = "20.0"
        const val guavaVersion = "29.0-jre"
        private lateinit var testProject: GradleProject

        @Suppress("unused")
        @BeforeAll
        @JvmStatic
        fun setup(@TempDir testProjectDir: Path, reporter: TestReporter) {
            testProject = GradleProject(testProjectDir, reporter)
                .withTestName("cordapp-with-guava")
                .withSubResource("src/main/java/com/example/contract/GuavaContract.java")
                .build(
                    "-Pcordapp_contract_version=$expectedCordappContractVersion",
                    "-Pcorda_release_version=$cordaReleaseVersion",
                    "-Pcorda_guava_version=$cordaGuavaVersion",
                    "-Pguava_version=$guavaVersion"
                )
        }
    }

    @Test
    fun conflictingGuavaVersionsTest() {
        assertThat(testProject.dependencyConstraints)
            .noneMatch { it.startsWith("guava-$cordaGuavaVersion.jar") }
            .anyMatch { it.startsWith("guava-$guavaVersion.jar") }
            .hasSizeGreaterThanOrEqualTo(1)

        val artifacts = testProject.artifacts
        assertThat(artifacts).hasSize(2)

        val cpk = artifacts.single { it.toString().endsWith(".cpk") }
        assertThat(cpk).isRegularFile()

        val cordapp = artifacts.single { it.toString().endsWith(".jar") }
        assertThat(cordapp).isRegularFile()

        val jarManifest = JarFile(cordapp.toFile()).use(JarFile::getManifest)
        println(jarManifest.mainAttributes.entries)

        with(jarManifest.mainAttributes) {
            assertEquals("CorDapp With Guava", getValue(BUNDLE_NAME))
            assertEquals("com.example.cordapp-with-guava", getValue(BUNDLE_SYMBOLICNAME))
            assertEquals("1.0.1.SNAPSHOT", getValue(BUNDLE_VERSION))
            assertEquals("com.google.common.collect;version=\"[29.0,30)\",net.corda.core.contracts,net.corda.core.transactions", getValue(IMPORT_PACKAGE))
            assertEquals("com.example.contract;uses:=\"net.corda.core.contracts,net.corda.core.transactions\";version=\"1.0.1\"", getValue(EXPORT_PACKAGE))
            assertEquals("osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.8))\"", getValue(REQUIRE_CAPABILITY))
            assertEquals("Test-Licence", getValue(BUNDLE_LICENSE))
            assertEquals("R3", getValue(BUNDLE_VENDOR))
            assertEquals("true", getValue("Sealed"))
            assertNull(getValue("Private-Package"))
        }
    }
}