package dev.nokee.install

import org.apache.commons.lang3.SystemUtils
import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.jruby.ast.impl.ListImpl
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

import static org.asciidoctor.OptionsBuilder.options
import static org.junit.Assume.assumeFalse
import static org.junit.Assume.assumeTrue

class ReadmeDocumentationTest extends Specification implements InstallScriptFixture {
    @Shared Asciidoctor asciidoctor = Asciidoctor.Factory.create()
    @Shared Document readme = asciidoctor.loadFile(readmeFile, options().asMap())

    private static File getReadmeFile() {
        return SystemProperties.installReadMe
    }

    // TODO: Extract HtmlLinkTester to docs-gradle-plugins
//    def "checks for broken links"() {
//        given:
//        def rootDirectory = Files.createTempDirectory('nokee')
//        def renderedReadMeFile = rootDirectory.resolve('readme.html').toFile()
//        renderedReadMeFile.text = asciidoctor.convertFile(readmeFile, options().toFile(false))
//
//        expect:
//        def report = new HtmlLinkTester(validEmails("hello@nokee.dev"), new HtmlLinkTester.BlackList() {
//            @Override
//            boolean isBlackListed(URI uri) {
//                if (uri.scheme == 'file') {
//                    def path = rootDirectory.toUri().relativize(uri).toString()
//                    if (new File(readmeFile.parentFile, path).exists()) {
//                        return true
//                    }
//                }
//                return false
//            }
//        }).reportBrokenLinks(rootDirectory.toFile())
//        report.assertNoFailures()
//    }

    @Unroll
    def "check '#sample.title' code snippet"(sample) {
        given:
        assumeFalse('wget requires a mocked http server', sample.command.contains('wget'))
        assumeTrue((sample.requiredShell == Shell.BASH && !SystemUtils.IS_OS_WINDOWS) || (sample.requiredShell == Shell.POWERSHELL && SystemUtils.IS_OS_WINDOWS))
        def userHomeDirectory = Files.createTempDirectory('install.nokee.dev')

        when:
        def process = sample.commandUnderTest.execute(["HOME=${userHomeDirectory}"], null)

        then:
        process.waitFor() == 0
        assertInstallationSucceeded(userHomeDirectory.toFile())

        where:
        sample << sampleBlocks
    }

    static class Sample {
        final String title
        final String command
        final Shell requiredShell

        Sample(String title, String command, Shell requiredShell) {
            this.title = title
            this.command = command
            this.requiredShell = requiredShell
        }

        List<String> getCommandUnderTest() {
            return requiredShell.launcherCommand + [command]
        }
    }

    static enum Shell {
        BASH(['bash', '-c']), POWERSHELL([])

        final launcherCommand
        private Shell(List<String> launcherCommand) {
            this.launcherCommand = launcherCommand
        }
    }

    List<Sample> getSampleBlocks() {
        return findSnippetBlocks().collect {
            // Unexpand the less-than character
            def command = it.content.toString().replace('&lt;', '<')

            // TODO: Create smoke test to ensure everything is proper.
            //    My current thought is to "release" smoke-test jar that can then be run at various publishing gate to ensure everything is working as expected.
            // Use local install script instead
            command = command.replace('https://nokee.dev/install.sh', SystemProperties.installBashScript.toURI().toString()).replace('https://nokee.dev/install.ps1', SystemProperties.installPowershellScript.toURI().toString())
            def requiredShell = command.contains('bash') ? Shell.BASH : Shell.POWERSHELL
            return new Sample(it.title, command, requiredShell)
        }
    }

    List<StructuralNode> findSnippetBlocks() {
        def snippets = new ArrayList<StructuralNode>();
        def queue = new ArrayDeque<StructuralNode>();
        queue.add(readme);
        while (!queue.isEmpty()) {
            StructuralNode node = queue.poll();
            if (node instanceof ListImpl) {
                queue.addAll(((ListImpl) node).getItems());
            } else {
                for (StructuralNode child : node.getBlocks()) {
                    if (child.isBlock() && child.getContext().equals("listing") && child.getStyle().equals("source")) {
                        snippets.add(child)
                    } else {
                        queue.offer(child);
                    }
                }
            }
        }

        return snippets
    }
}
