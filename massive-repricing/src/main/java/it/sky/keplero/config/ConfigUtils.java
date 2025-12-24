package it.sky.keplero.config;

import it.sky.keplero.entity.RepricingConfig;
import it.sky.keplero.repository.RepricingConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfigUtils {
    private final RepricingConfigRepository repricingConfigRepository;
    private final Map<String, String> configMap = new ConcurrentHashMap<>();

    public ConfigUtils(RepricingConfigRepository repricingConfigRepository) {
        this.repricingConfigRepository = repricingConfigRepository;
    }

    public String getConfigValue(String configName) {
        if(!this.configMap.isEmpty()) {
            getActiveConfigsList();
        }

        if(!this.configMap.containsKey(configName)){
            throw new IllegalArgumentException("No config available in the database: " + configName);
        }

        return this.configMap.get(configName);
    }

    @PostConstruct
    public void getActiveConfigsList() {
        List<RepricingConfig> configList = this.repricingConfigRepository.getActiveConfigs();
        for(RepricingConfig config: configList) {
            configMap.put(config.getConfigName(), config.getConfigValue());
        }
    }
}
