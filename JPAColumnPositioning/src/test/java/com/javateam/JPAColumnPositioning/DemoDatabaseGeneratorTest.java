package com.javateam.JPAColumnPositioning;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.javateam.JPAColumnPositioning.entity.Store;
import com.javateam.JPAColumnPositioning.entity.User;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DemoDatabaseGeneratorTest {
	
	@Test
	public void test() {
		
		Map<String, String> settings = new HashMap<>();
		
        // settings.put("connection.driver_class", "oracle.jdbc.OracleDriver");
		settings.put("spring.datasource.driverClassName", "oracle.jdbc.OracleDriver");
        settings.put("dialect", "org.hibernate.dialect.Oracle12cDialect");
        settings.put("hibernate.connection.url", "jdbc:oracle:thin:@localhost:1521:xe");
        settings.put("hibernate.connection.username", "spring");
        settings.put("hibernate.connection.password", "spring");
        settings.put("hibernate.hbm2ddl.auto", "create");
        settings.put("show_sql", "true");
 
        MetadataSources metadata = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySettings(settings)
                        .build());
        
        log.info("------------ 2");

        // entity
        metadata.addAnnotatedClass(Store.class);
        metadata.addAnnotatedClass(User.class);
        
        SchemaExport schemaExport = new SchemaExport();
        
        schemaExport.setHaltOnError(true)
			        .setFormat(true)
			        .setDelimiter(";")
			        .setOutputFile("d:\\sql\\db-schema.sql")
			        .create(EnumSet.of(TargetType.STDOUT, TargetType.DATABASE), metadata.buildMetadata());
	}

}
