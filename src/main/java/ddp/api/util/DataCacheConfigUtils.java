package ddp.api.util;

import ddp.api.*;
import org.slf4j.*;

import java.util.*;

public class DataCacheConfigUtils
{
    private static final Logger _logger = LoggerFactory.getLogger(DataCacheConfigUtils.class);

    public static int getNextConfigRoll(Map<Integer, DataCacheConfiguration> dataCacheConfigurations, Integer currentConfigIndex) throws ConfigurationException
    {
        if (dataCacheConfigurations == null || dataCacheConfigurations.keySet().isEmpty())
        {
            String errorMessage = "Could not find any configurations for Data Cache.";

            _logger.error(errorMessage);

            throw new ConfigurationException(errorMessage);
        }

        Set<Integer> keySet = dataCacheConfigurations.keySet();

        if (currentConfigIndex == null)
        {
            //TODO DS this is not great or safe
            return keySet.stream().min(Comparator.<Integer>naturalOrder()).get();
        }

        if (keySet.size() == 1)
        {
            String errorMessage = "Only one Data Cache config exist, thus no more to try.";

            _logger.error(errorMessage);

            throw new ConfigurationException(errorMessage);
        }

        Optional<Integer> nextHigherIndex = keySet.stream().sorted().filter(key -> key > currentConfigIndex).findFirst();

        if (nextHigherIndex.isPresent())
        {
            return nextHigherIndex.get();
        }
        else
        {
            Optional<Integer> nextLowerIndex = keySet.stream().sorted().filter(key -> key < currentConfigIndex).min(Comparator.<Integer>naturalOrder());

            if (nextLowerIndex.isPresent())
            {
                return nextLowerIndex.get();
            }
            else
            {
                //TODO DS needs to be better...
                throw new ConfigurationException("GGGGGGGGG");
            }
        }
    }
}