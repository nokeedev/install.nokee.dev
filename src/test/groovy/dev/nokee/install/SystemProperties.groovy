package dev.nokee.install

class SystemProperties {
    static File getInstallPowershellScript() {
        return new File(get('dev.nokee.install.ps1'))
    }

    static File getInstallBashScript() {
        return new File(get('dev.nokee.install.sh'))
    }

    static File getInstallReadMe() {
        return new File(get('dev.nokee.install.readme'))
    }

    static File getInstallExpectedInitScript() {
        return new File(get('dev.nokee.install.init'))
    }

    private static String get(String key) {
        assert System.getProperties().containsKey(key)
        return System.getProperty(key)
    }
}
