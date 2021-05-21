package videoplayer.vlcj;

import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class InternalDiscovery implements NativeDiscoveryStrategy {

    @Override
    public boolean supported() {
        return RuntimeUtil.isWindows();
    }

    @Override
    public String discover() {
        String path = System.getProperty("vlcj.library.path");
        if (path != null)
            return path;

        // Standard path: lib/vlc in the same folder as jar
        File f = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
        return URLDecoder.decode(f.getPath(), StandardCharsets.UTF_8)
                + File.separatorChar + "lib"
                + File.separatorChar + "vlc";
    }

    @Override
    public boolean onFound(String path) {
        return true;
    }

    @Override
    public boolean onSetPluginPath(String path) {
        String pluginPath = String.format("%s\\plugins", path);
        return LibC.INSTANCE._putenv(String.format("%s=%s", "VLC_PLUGIN_PATH", pluginPath)) == 0;
    }
}
