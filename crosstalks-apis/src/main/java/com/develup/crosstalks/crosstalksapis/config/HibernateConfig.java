package com.develup.crosstalks.crosstalksapis.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

@Configuration
public class HibernateConfig {

  @Value("${db.password}")
  private String DB_PASSWORD;

  @Value("${db.url}")
  private String DB_URL;

  @Value("${db.username}")
  private String DB_USERNAME;

  @Value("${db.source_class}")
  private String DB_SOURCE_CLASS;

  @Value("${hibernate.dialect}")
  private String HIBERNATE_DIALECT;

  @Value("${hibernate.show_sql}")
  private String HIBERNATE_SHOW_SQL;

  @Value("${hibernate.hbm2ddl.auto}")
  private String HIBERNATE_HBM2DDL_AUTO;

  @Value("${entitymanager.packagesToScan}")
  private String ENTITYMANAGER_PACKAGES_TO_SCAN;

  @Value("${db.maximum_connection_pool_size}")
  private Integer DB_MAX_CONNECTION_POOL_SIZE;

  @Value("${db.fetch_from_connection_pool_timeout_in_ms}")
  private Integer DB_FETCH_FROM_POOL_TIMEOUT;

  @Value("${db.connection_lifetime_in_minutes}")
  private Integer CONNECTION_LIFETIME_IN_MINUTES;

  @Bean
  public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setDataSourceClassName(DB_SOURCE_CLASS);
    ds.addDataSourceProperty("url", DB_URL);
    ds.addDataSourceProperty("user", DB_USERNAME);
    ds.addDataSourceProperty("password", DB_PASSWORD);
    ds.setMaximumPoolSize(DB_MAX_CONNECTION_POOL_SIZE);
    ds.setConnectionTimeout(DB_FETCH_FROM_POOL_TIMEOUT);
    ds.setMaxLifetime(TimeUnit.MINUTES.toMillis(CONNECTION_LIFETIME_IN_MINUTES));
    return ds;
  }


  @Bean
  public LocalSessionFactoryBean sessionFactoryHibernate() {
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(dataSource());
    sessionFactory.setPackagesToScan(ENTITYMANAGER_PACKAGES_TO_SCAN);
    sessionFactory.setHibernateProperties(hibernateProperties());
    sessionFactory.setPhysicalNamingStrategy(new MyPhysicalNamingStrategyImpl());

    return sessionFactory;
  }

  private Properties hibernateProperties() {
    Properties hibernateProperties = new Properties();
    hibernateProperties.setProperty("hibernate.hbm2ddl.auto", HIBERNATE_HBM2DDL_AUTO);
    hibernateProperties.setProperty("hibernate.show_sql", HIBERNATE_SHOW_SQL);
    hibernateProperties.setProperty("hibernate.dialect", HIBERNATE_DIALECT);

    return hibernateProperties;
  }

}
