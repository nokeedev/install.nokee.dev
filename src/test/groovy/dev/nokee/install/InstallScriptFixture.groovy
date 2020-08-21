package dev.nokee.install

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file
import static dev.gradleplugins.fixtures.file.FileSystemUtils.getDescendants

trait InstallScriptFixture {
    void assertInstallationSucceeded(File userHomeDirectory) {
        assert getDescendants(userHomeDirectory) == ['.gradle/init.d/nokee.init.gradle'] as Set
        assert file(userHomeDirectory, '.gradle/init.d/nokee.init.gradle').text == SystemProperties.installExpectedInitScript.text
    }
}
