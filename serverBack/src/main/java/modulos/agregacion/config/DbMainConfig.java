package modulos.agregacion.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        basePackages = "modulos.agregacion.repositories.DbMain",
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
public class DbMainConfig {

    @Primary
    @Bean(name = "mainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db4")
    public DataSource mainDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mainDataSource") DataSource ds) {

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.hbm2ddl.auto", "update");          // <- CLAVE
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProps.put("hibernate.format_sql", false);
        jpaProps.put("hibernate.show_sql", false);

        return builder
                .dataSource(ds)
                .packages("modulos.agregacion.entities.DbMain", "modulos.agregacion.entities.atributosHecho")
                .persistenceUnit("db4")
                .properties(jpaProps)                                   // <- aplica al EMF
                .build();
    }

    @Primary
    @Bean(name = "mainTransactionManager")
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}



