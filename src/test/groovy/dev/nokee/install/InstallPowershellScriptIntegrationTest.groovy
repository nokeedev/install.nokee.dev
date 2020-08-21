package dev.nokee.install

import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

@Requires({ SystemUtils.IS_OS_WINDOWS })
class InstallPowershellScriptIntegrationTest extends AbstractInstallScriptIntegrationTest {
    @Override
    protected File getInstallScriptUnderTest() {
        return SystemProperties.installPowershellScript
    }

    @Override
    protected Set<URL> getExpectedUrlsContainedInScript() {
        return [new URL('https://nokee.dev/getting-started'), new URL('https://nokee.dev/install.sh'), new URL('https://raw.githubusercontent.com/nokeedev/init.nokee.dev/main/nokee.init.gradle')] as Set
    }

    @Override
    protected String getNokeeConsoleLogo() {
        return """
                 _                               
     _ __   ___ | | _____  ___                   
    | '_ \\ / _ \\| |/ / _ \\/ _ \\                  
    | | | | (_) |   <  __/  __/                  
    |_| |_|\\___/|_|\\_\\___|\\___| installer        
         Painless native development with Gradle 
                                                 
   >  Manual: https://nokee.dev/getting-started 
""".trim() // so we have a nice display above ;)
    }
}
