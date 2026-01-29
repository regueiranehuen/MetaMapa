package com.tpagrupo17.Metamapa.servicioEstadistica.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.tpagrupo17.Metamapa.servicioEstadistica.repositories",
        entityManagerFactoryRef = "estadisticaEntityManagerFactory",
        transactionManagerRef = "estadisticaTransactionManager"
)
public class DbEstadisticaConfig {

    @Bean(name = "estadisticaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db5")
    public DataSource estadisticaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "estadisticaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean estadisticaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("estadisticaDataSource") DataSource ds) {

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.hbm2ddl.auto", "update"); // usar "create" solo en la primera corrida si necesitás forzar creación
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProps.put("hibernate.show_sql", true);
        jpaProps.put("hibernate.format_sql", true);

        return builder
                .dataSource(ds)
                .packages("com.tpagrupo17.Metamapa.servicioEstadistica.entities")
                .persistenceUnit("db5")
                .properties(jpaProps) // <- clave para que se aplique a este EMF
                .build();
    }

    @Bean(name = "estadisticaTransactionManager")
    public PlatformTransactionManager estadisticaTransactionManager(
            @Qualifier("estadisticaEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
