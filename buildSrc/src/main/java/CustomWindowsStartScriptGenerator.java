import org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator;
import org.gradle.api.internal.plugins.StartScriptTemplateBindingFactory;
import org.gradle.util.TextUtil;

public class CustomWindowsStartScriptGenerator extends DefaultTemplateBasedStartScriptGenerator {
    public CustomWindowsStartScriptGenerator() {
        super(
                TextUtil.getWindowsLineSeparator(),
                StartScriptTemplateBindingFactory.windows(),
                utf8ClassPathResource(CustomWindowsStartScriptGenerator.class, "customWindowsStartScript.txt")
        );
    }
}
