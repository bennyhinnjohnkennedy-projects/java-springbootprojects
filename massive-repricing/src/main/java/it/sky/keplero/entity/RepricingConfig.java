package it.sky.keplero.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "repricing_config", schema = "keplero")
public class RepricingConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String configName;
    private String configValue;
    private Instant createdTime;
    private Instant updatedTime;
    private String description;
    private boolean active;

    public void setId(int id) {
        this.id = id;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public void setUpdatedTime(Instant updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getConfigName() {
        return configName;
    }

    public String getConfigValue() {
        return configValue;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public Instant getUpdatedTime() {
        return updatedTime;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}
