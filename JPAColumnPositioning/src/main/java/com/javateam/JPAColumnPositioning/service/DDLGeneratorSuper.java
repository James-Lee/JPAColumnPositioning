package com.javateam.JPAColumnPositioning.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Version;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;

public class DDLGeneratorSuper {

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected Environment environment;

    protected SessionFactoryImpl getSessionFactory() {
        Session session = (Session) entityManager.getDelegate();
        return (SessionFactoryImpl) session.getSessionFactory();
    }
    
    public Properties getEnvProperties() {
    	
    	Properties props = new Properties();
    	props.put("spring.datasource.driver-class-name", environment.getProperty("spring.datasource.driver-class-name"));
        props.put("hibernate.dialect", environment.getProperty("spring.jpa.database-platform"));
        props.put("hibernate.hbm2ddl.auto", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        props.put("hibernate.show_sql", true);
        props.put("hibernate.connection.username", environment.getProperty("spring.datasource.username"));
        props.put("hibernate.connection.password", environment.getProperty("spring.datasource.password"));
        props.put("hibernate.connection.url", environment.getProperty("spring.datasource.url"));
        
        return props;
    }
    
	protected void getDDLExport() {
    	
    	// Hibernate 5
    	if (Version.getVersionString().startsWith("5")) {

            BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
            StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder( bsr );
            
            ssrBuilder.applySettings(getEnvProperties());
            StandardServiceRegistry standardServiceRegistry = ssrBuilder.build();

            MetadataSources metadataSources = new MetadataSources( standardServiceRegistry );
            MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();

            EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.DATABASE, TargetType.STDOUT);
            new SchemaExport().createOnly(targetTypes, metadataBuilder.build());
        }
    	
       throw new RuntimeException("지원되지 않는 Hibernate 버전입니다.");
    }

    @SuppressWarnings("deprecation")
	protected ClassMetadata getClassMetaData(String tableName) {
    	
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Map<String, ClassMetadata> classMetadataMap = sessionFactory.getAllClassMetadata();

        for (String className : classMetadataMap.keySet()) {
            ClassMetadata classMetadata = classMetadataMap.get(className);

            if (((SingleTableEntityPersister) classMetadata).getTableName().toLowerCase().equals(tableName.toLowerCase()))
                return classMetadata;
        }

        throw new IllegalArgumentException("테이블이 존재하지 않습니다.");
    } //
    
}