package it.sky.keplero.repository;

import it.sky.keplero.entity.RepricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepricingConfigRepository extends JpaRepository<RepricingConfig, Integer> {

    @Query("SELECT rc FROM RepricingConfig rc WHERE rc.active = true")
    public List<RepricingConfig> getActiveConfigs();
}
