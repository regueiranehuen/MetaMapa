// DbHechosProxyConfig.java
package modulos.agregacion.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
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
        basePackages = "modulos.agregacion.repositories.DbProxy",
        entityManagerFactoryRef = "proxyEntityManagerFactory",
        transactionManagerRef = "proxyTransactionManager"
)
public class DbHechosProxyConfig {

    @Bean(name = "proxyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db3")
    public DataSource proxyDataSource() {
        return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
    }

    @Bean(name = "proxyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean proxyEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("proxyDataSource") DataSource ds) {

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.hbm2ddl.auto", "update"); // "create" la 1ª vez si necesitás
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProps.put("hibernate.show_sql", false);
        jpaProps.put("hibernate.format_sql", false);

        return builder
                .dataSource(ds)
                .packages("modulos.agregacion.entities.DbProxy",
                        "modulos.agregacion.entities.atributosHecho")
                .persistenceUnit("db3")
                .properties(jpaProps)
                .build();
    }

    @Bean(name = "proxyTransactionManager")
    public PlatformTransactionManager proxyTransactionManager(
            @Qualifier("proxyEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

