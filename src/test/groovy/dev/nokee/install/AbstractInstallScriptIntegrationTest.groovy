package dev.nokee.install

import dev.gradleplugins.fixtures.file.FileSystemUtils
import org.apache.commons.lang3.SystemUtils
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.regex.Pattern

import static org.hamcrest.MatcherAssert.assertThat

abstract class AbstractInstallScriptIntegrationTest extends Specification implements InstallScriptFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "can install init script in the user home directory"() {
        def outStream = new ByteArrayOutputStream()
        def process = [installScriptUnderTest].execute(["HOME=${temporaryFolder.root}"], null)
        def outStreamThread = process.consumeProcessOutputStream(outStream)
        process.out << "\n"
        expect:
        process.waitFor() == 0
        assertInstallationSucceeded(temporaryFolder.root)
        String expectedOutput = """==> This script will install:
${temporaryFolder.root}/.gradle/init.d/nokee.init.gradle
==> The following new directories will be created:
${temporaryFolder.root}/.gradle/init.d
==> Downloading and installing Nokee...
==> Installation successful!"""
        assertThat(normalize(outStream.toString()), CoreMatchers.endsWith(expectedOutput))

        cleanup:
        outStreamThread?.join()
    }

    def "shows the logo"() {
        def outStream = new ByteArrayOutputStream()
        def process = [installScriptUnderTest].execute(["HOME=${temporaryFolder.root}", 'NOKEE_TESTING='], null)
        def outStreamThread = process.consumeProcessOutputStream(outStream)
        process.out << "\n"
        expect:
        process.waitFor() == 0
        normalize(outStream.toString()).startsWith(nokeeConsoleLogo)

        cleanup:
        outStreamThread?.join()
    }

    // Test it shows what will be done and wait for user input

    // Test it shows what is done (with the various header)

    def "cancels the installation for any inputs other than ENTER"() {
        def outStream = new ByteArrayOutputStream()
        def process = [installScriptUnderTest].execute(["HOME=${temporaryFolder.root}", 'NOKEE_TESTING='], null)
        def outStreamThread = process.consumeProcessOutputStream(outStream)
        process.out << "a"
        expect:
        process.waitFor() == 1
        FileSystemUtils.getDescendants(temporaryFolder.root) == [] as Set
        normalize(outStream.toString()).contains """==> This script will install:
${temporaryFolder.root}/.gradle/init.d/nokee.init.gradle
"""
        !outStream.toString().contains('==> Installation successful!')

        cleanup:
        outStreamThread?.join()
    }
    // Test proper error message when no curl or wget
    // Test proper error message when no HOME variable

    // Test URL from README exists in proper location
    // Test command from README

    def "contains valid urls"() {
        def urls = extractAllUrlsFromScript(installScriptUnderTest)

        expect:
        urls == expectedUrlsContainedInScript
        urls.collect { AbstractInstallScriptIntegrationTest.head(it) }.every()
    }

    private static String normalize(String s) {
        return s.replace('\r\n', '\n').trim()
    }

    // Crappy way to issue an head request
    private static boolean head(URL url) {
        HttpURLConnection httpConnection = null
        System.setProperty("http.keepAlive", "false")
        try {
            httpConnection = (HttpURLConnection) url.openConnection()
            httpConnection.setRequestMethod("HEAD")
            httpConnection.getInputStream().close()
            return httpConnection.responseCode == 200
        } finally {
            httpConnection?.disconnect()
        }
    }

    protected abstract File getInstallScriptUnderTest()

    protected abstract Set<URL> getExpectedUrlsContainedInScript()

    private static Set<URL> extractAllUrlsFromScript(File script) {
        def regex = Pattern.compile('https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9]{1,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)')
        def matcher = regex.matcher(script.text)
        def result = [] as Set
        while (matcher.find()) {
            result.add(new URL(matcher.group(0)))
        }
        return result
    }

    private static String getExpectedInitScriptContent() {
        return SystemProperties.getInstallExpectedInitScript().text
    }

    protected abstract getNokeeConsoleLogo()
}

// Publish *.text script as jbake-assets
// Publish install.sh, install.ps1, and README.adoc in GitHub pages
// Publish README or whatever content as jbake-content