package com.javateam.JPAColumnPositioning;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.javateam.JPAColumnPositioning.entity.Store;
import com.javateam.JPAColumnPositioning.service.DDLGenerator;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SchemaGeneratorTest {

    @Autowired
    private DDLGenerator ddlGenerator;
    
    @Test
    public void createSchema() throws IOException, ClassNotFoundException, URISyntaxException {

		// 테이블 자동 생성 단위 테스트	
    	// log.info(ddlGenerator.getEnvProperties().toString());
    	
     	 // (주의사항-1) VO 에 반드시 @Table(name="VO 이름 +_tbl(접미어)") 식으로 어노테이션 주입할 것    	 
     	 // ex) @Table(name="store_table")
    	 // (주의사항-2) 각 필드의 경우 반드시 @Column을 붙일 것.
    	 // @Column(name="store_name")
    	
    	 ddlGenerator.createDDL(new Store().getClass(), 
    			 				"d:/sql/ddl_table.sql", 
    			 				ddlGenerator.getEnvProperties(),
    			 				false);
    } //
    
} //