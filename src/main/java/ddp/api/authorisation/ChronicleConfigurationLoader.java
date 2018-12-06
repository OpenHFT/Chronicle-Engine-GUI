package ddp.api.authorisation;


import ddp.api.ConfigurationException;
import net.openhft.chronicle.core.pool.*;
import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.cfg.*;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.wire.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utilities to load Chronicle configuration.
 */
public class ChronicleConfigurationLoader {

    private static final Logger _logger = LoggerFactory.getLogger(ChronicleConfigurationLoader.class);

    /**
     * Loads the Chronicle Engine configuration file and installs it on the given asset tree.
     *
     * @param configUri                 Uri in AssetTree to install configuration.
     * @param chronicleEngineConfigFile
     */
    public static void loadAndInstallChronicleEngineConfiguration(AssetTree assetTree, String configUri, String chronicleEngineConfigFile) throws ConfigurationException {
        try {
            loadChronicleClassAliases();
            addClassToPool(EngineCfg.class);
            addClassToPool(ClustersCfg.class);

            TextWire yaml = TextWire.fromFile(chronicleEngineConfigFile);
            Installable installable = (Installable) yaml.readObject();

            assetTree.registerSubscriber(configUri, TopologicalEvent.class, e -> _logger.info("Config change " + e));

            installable.install(configUri, assetTree);
            _logger.info("Config URI '{}' configured", configUri);

        } catch (Exception e) {
            _logger.error("Error starting a component, stopping", e);
            assetTree.close();

            throw new ConfigurationException(e);
        }
    }

    /**
     * Loads Chronicle Class Aliases which are populated in a static block of code in RequestContext
     */
    private static void loadChronicleClassAliases() {
        ClassLookup classAliases = RequestContext.CLASS_ALIASES;
    }

    /**
     * Adds the given Installable to the class pool used by Chronicle for configuration loading.
     *
     * @param iClasses Classes to add to class pool.
     */
    private static <I extends Installable> void addClassToPool(Class<I>... iClasses)
    {
        ClassAliasPool.CLASS_ALIASES.addAlias(iClasses);
    }

}
