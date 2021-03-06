package apollo;

import apollo.build.MockInjector;
import apollo.enums.ConfigSourceType;
import apollo.internals.AbstractConfig;
import apollo.internals.DefaultInjector;
import apollo.model.ConfigFileChangeEvent;
import apollo.spi.ConfigFactory;
import apollo.util.ConfigUtil;
import framework.apollo.core.ConfigConsts;
import framework.apollo.core.enums.ConfigFileFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigServiceTest {
  private static String someAppId;

  @Before
  public void setUp() throws Exception {
    someAppId = "someAppId";

    //as ConfigService is singleton, so we must manually clear its container
    ConfigService.reset();
    MockInjector.reset();
    MockInjector.setDelegate(new DefaultInjector());
    MockInjector.setInstance(ConfigUtil.class, new MockConfigUtil());
  }

  @Test
  public void testHackConfig() {
    String someNamespace = "hack";
    String someKey = "first";
    ConfigService.setConfig(new MockConfig(someNamespace));

    Config config = ConfigService.getAppConfig();

    assertEquals(someNamespace + ":" + someKey, config.getProperty(someKey, null));
    assertEquals(null, config.getProperty("unknown", null));
  }

  @Test
  public void testHackConfigFactory() throws Exception {
    String someKey = "someKey";
    ConfigService.setConfigFactory(new MockConfigFactory());

    Config config = ConfigService.getAppConfig();

    assertEquals(ConfigConsts.NAMESPACE_APPLICATION + ":" + someKey,
        config.getProperty(someKey, null));
  }

  @Test
  public void testMockConfigFactory() throws Exception {
    String someNamespace = "mock";
    String someKey = "someKey";
    MockInjector.setInstance(ConfigFactory.class, someNamespace, new MockConfigFactory());

    Config config = ConfigService.getConfig(someNamespace);

    assertEquals(someNamespace + ":" + someKey, config.getProperty(someKey, null));
    assertEquals(null, config.getProperty("unknown", null));
  }

  @Test
  public void testMockConfigFactoryForConfigFile() throws Exception {
    String someNamespace = "mock";
    ConfigFileFormat someConfigFileFormat = ConfigFileFormat.Properties;
    String someNamespaceFileName =
        String.format("%s.%s", someNamespace, someConfigFileFormat.getValue());
    MockInjector.setInstance(ConfigFactory.class, someNamespaceFileName, new MockConfigFactory());

    ConfigFile configFile = ConfigService.getConfigFile(someNamespace, someConfigFileFormat);

    assertEquals(someNamespaceFileName, configFile.getNamespace());
    assertEquals(someNamespaceFileName + ":" + someConfigFileFormat.getValue(), configFile.getContent());
  }

  private static class MockConfig extends AbstractConfig {
    private final String m_namespace;

    public MockConfig(String namespace) {
      m_namespace = namespace;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
      if (key.equals("unknown")) {
        return null;
      }

      return m_namespace + ":" + key;
    }

    @Override
    public Set<String> getPropertyNames() {
      return null;
    }

    @Override
    public ConfigSourceType getSourceType() {
      return null;
    }

    @Override
    public void onRepositoryChange(String namespace, Properties newProperties) {

    }
  }

  private static class MockConfigFile implements ConfigFile {
    private ConfigFileFormat m_configFileFormat;
    private String m_namespace;

    public MockConfigFile(String namespace,
                          ConfigFileFormat configFileFormat) {
      m_namespace = namespace;
      m_configFileFormat = configFileFormat;
    }

    @Override
    public String getContent() {
      return m_namespace + ":" + m_configFileFormat.getValue();
    }

    @Override
    public boolean hasContent() {
      return true;
    }

    @Override
    public String getNamespace() {
      return m_namespace;
    }

    @Override
    public ConfigFileFormat getConfigFileFormat() {
      return m_configFileFormat;
    }

    @Override
    public void fireConfigChange(ConfigFileChangeEvent changeEvent) {

    }

    @Override
    public void addChangeListener(ConfigFileChangeListener listener) {

    }

    @Override
    public boolean removeChangeListener(ConfigChangeListener listener) {
      return false;
    }

    @Override
    public ConfigSourceType getSourceType() {
      return null;
    }
  }

  public static class MockConfigFactory implements ConfigFactory {
    @Override
    public Config create(String namespace) {
      return new MockConfig(namespace);
    }

    @Override
    public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
      return new MockConfigFile(namespace, configFileFormat);
    }
  }

  public static class MockConfigUtil extends ConfigUtil {
    @Override
    public String getAppId() {
      return someAppId;
    }
  }

}
