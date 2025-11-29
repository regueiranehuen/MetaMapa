// DbHechosDinamicaConfig.java
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
        basePackages = "modulos.agregacion.repositories.DbDinamica",
        entityManagerFactoryRef = "dinamicaEntityManagerFactory",
        transactionManagerRef = "dinamicaTransactionManager"
)
public class DbHechosDinamicaConfig {

    @Bean(name = "dinamicaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource dinamicaDataSource() {
        return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
    }

    @Bean(name = "dinamicaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dinamicaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dinamicaDataSource") DataSource ds) {

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.hbm2ddl.auto", "update"); // "create" si querés forzar creación
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProps.put("hibernate.show_sql", false);
        jpaProps.put("hibernate.format_sql", false);

        return builder
                .dataSource(ds)
                .packages("modulos.agregacion.entities.DbDinamica",
                        "modulos.agregacion.entities.atributosHecho")
                .persistenceUnit("db2")
                .properties(jpaProps)
                .build();
    }

    @Bean(name = "dinamicaTransactionManager")
    public PlatformTransactionManager dinamicaTransactionManager(
            @Qualifier("dinamicaEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
