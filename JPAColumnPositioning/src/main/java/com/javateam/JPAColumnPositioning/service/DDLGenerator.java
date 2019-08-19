package com.javateam.JPAColumnPositioning.service;

import org.apache.commons.io.IOUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.javateam.JPAColumnPositioning.annotation.ColumnPosition;
import com.javateam.JPAColumnPositioning.entity.ColumnDefinition;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Transient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class DDLGenerator extends DDLGeneratorSuper {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

    private List<ColumnDefinition> columnDefinitions;
	
    public void createDDL(Class<?> claszz, String sqlPathFile, Properties props) throws IOException, ClassNotFoundException, URISyntaxException {
        
    	BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
        StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder(bsr);
        ssrBuilder.applySettings(props);
        
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
        													 .applySettings(props);
        MetadataSources metadataSources = new MetadataSources(registryBuilder.build());

        // Entity(VO)
        String className = claszz.getSimpleName();
        metadataSources.addAnnotatedClass(claszz);

        Metadata metadata = metadataSources.buildMetadata();
    	JdbcEnvironment jdbcEnv = metadata.getDatabase().getJdbcEnvironment();
        String scriptOutputPath = sqlPathFile.toString();
        
        log.info("file path : "+scriptOutputPath);
        
        // 기존 파일 내용 초기화
        File defaultFile = new File(scriptOutputPath);
        
        if (!defaultFile.exists()) {
        	 log.info("신규 파일 생성");
        	 defaultFile.createNewFile();
        } else {
	    	 log.info("기존 파일 존재함");
	         clearDDLFile(scriptOutputPath);
        }
        
        // Hibernate 식의 기존 DDL 생성(position 적용 안됨)
        new SchemaExport()
        		.setDelimiter(";")
                .setFormat(true)
                .setOutputFile(scriptOutputPath)
                .create(EnumSet.of(TargetType.STDOUT, TargetType.SCRIPT), metadata);
        
        ///////////////////////////////////////////////////////////////////////
        
        log.info("position 적용된 DDL 생성 단계");
        
        List<String> DDLs = IOUtils.readLines(new FileInputStream(scriptOutputPath), "UTF-8");
        List<String> convertedDDLs = new ArrayList<>();
       
        for (String DDL : DDLs) {
            if (DDL.toLowerCase().startsWith("create table")) {
                convertedDDLs.add(convert(DDL, jdbcEnv, className)); 
            } else {
                convertedDDLs.add(DDL);
            } 
        } //
      
        log.info("###### 변환전 DDLs");
        for (String DDL : DDLs) {
        	log.info(DDL);
        	// log.info("-----------");
        }
        
        // 교정
        // convertedDDLs.clear();
        // 기존 DDL 구문 정리
        // create table 이전 구문은 기존 그대로 이용하고 변환된 create table 구문만 변환 추가
        convertedDDLs.clear();
        
        for (String DDL : DDLs) {
        	 if (DDL.trim().toLowerCase().startsWith("drop") || DDL.trim().toLowerCase().startsWith("create sequence")) {
        		 convertedDDLs.add(DDL.trim());
        	 }
        }
        
        // 테이블 작성(create table) 이후 구문만 추출
        // log.info("########## 변환한 DDL : "+convert(DDLs.toString(), jdbcEnv, className)); // 교정
        
        convertedDDLs.add(convert(DDLs.toString(), jdbcEnv, className));
        
    	log.info("########## 실행할 DDL ############");
    	for (String temp_sql : convertedDDLs) {
    		log.info("convertedDDLs : "+temp_sql);	
    	}
    	
    	log.info("########## DDL 실행 시작");
    	for (String convertedDDL : convertedDDLs) {
    		
	        log.info("convertedDDL : " + convertedDDL);
	        // 버그 패치 : 문장 끝(;) SQL 인식 에러 => ";" 제거후 실행
	        try {
	        		jdbcTemplate.execute(convertedDDL.replaceAll("\\;", "").trim()); // DB 저장
	        } catch(Exception e) {
	        	log.error("SQL 에러 발생 : "+ e);
	        }
	        
        } // for

    	log.info("DDL 실행 끌");
    	
    	
      	log.info("########## DDL 파일 저장 시작");
        // 기존 DDL 파일 조정/변경
    	for (String convertedDDL : convertedDDLs) {
    	
    		if (convertedDDL.trim().toLowerCase().startsWith("create table")) {
    			modifyDDLFile(defaultFile, convertedDDL);
    		}		
    	} //
    	log.info("########## DDL 파일 저장 끌");
    } //

    private String convert(String ddl, JdbcEnvironment jdbcEnv, String className) throws ClassNotFoundException {
    	
    	log.info("########### convert : DDL 변환 #########");
    	
        StringBuilder convertedDDL = new StringBuilder();
        int startColumnBody = ddl.indexOf("("); // 정정
        int endColumnBody = ddl.lastIndexOf(")");
        
        String tableName = "";
        
        // 검색 시작 위치 지정
    	int indexStart = ddl.indexOf("create table ");
        
        try {
        		tableName = ddl.substring(indexStart+"create table ".length(), startColumnBody).trim(); //
        		log.info("테이블명 : "+tableName);
        } catch (Exception e) {
        	log.error("구문 에러");
        } //
        
        String columnBody = ddl.substring(startColumnBody + 1, endColumnBody);
        String primaryKeyDefinition = "";
        
        columnBody = columnBody.replaceAll(",,         ", ",").trim().replaceAll(" char\\)", "\\)"); // 교정
        int primaryKey = columnBody.indexOf("primary key");
        primaryKeyDefinition = columnBody.substring(primaryKey);
        columnBody = columnBody.substring(1, primaryKey - 1).trim(); 
        
        log.info("####### columnBody : " + columnBody);

        columnDefinitions = Arrays.stream(columnBody.split(",")) // 교정
				        		  .map(ColumnDefinition::new)
				        		  .collect(toList()); // 교정
        
        columnDefinitions.add(new ColumnDefinition(primaryKeyDefinition));

        String tableEntity = "com.javateam.JPAColumnPositioning.entity." + className;
        Class<?> clazz = Class.forName(tableEntity);
        Field[] fields = clazz.getDeclaredFields();
        
        // log.info("######## 위치 변환 전 ");
        // log.info("######## columnDefinitions : "+columnDefinitions.toString());

        for (Field field : fields) {
        	setPosition(field, jdbcEnv); // 교정
        }
        
        // log.info("######## 위치 변환 후 ");
        // log.info("columnDefinitions : " + columnDefinitions.toString());
        
        
        for (Field field : fields) { // 변환된 순서대로 출력됨
        	log.info("필드 : " + field.getName());
        }
        
        // log.info("######## 컬럼 포지션 설정");

        convertedDDL
                .append("create table")
                .append(" ")
                .append(tableName)
                .append(" ")
                .append("(\n\t");
        
        StringJoiner columns = new StringJoiner(", ");
        
        // log.info("columnDefinitions : "+columnDefinitions);
        
        // log.info("######### 변환 반영 시작 #########");
        
        columnDefinitions
                .stream()
                .sorted(Comparator.comparingInt(ColumnDefinition::getPosition)
                        .thenComparing(ColumnDefinition::getColumnName))
                .forEach(entityField -> {
                	log.info(entityField.getColumnDefinition());
                    columns.add(entityField.getColumnDefinition());
                });
        
        // log.info("######### 변환 반영 끝 #########");
        
        // 마지막 부분 "," 제거
        convertedDDL.append(columns.toString().substring(0, columns.toString().length()-2));
        convertedDDL.append("));"); // DDL 마무리
        
        // log.info("######## 컬럼 포지션 설정 끝");

        return convertedDDL.toString();
    }

    public void setPosition(Field field, JdbcEnvironment jdbcEnv) {
    	
    	// log.info("########## setPosition 호출  ########");
        
    	String name = field.getName();
        String columnName = null;
        int position = Integer.MAX_VALUE - 10;

        if (field.getAnnotation(Transient.class) == null) {
            Column column = field.getAnnotation(Column.class);
            ColumnPosition columnPosition = field.getAnnotation(ColumnPosition.class);

            if (column != null && !"".equals(column.name())) {
                columnName = column.name();
                //log.info("-- 컬럼 이름 : "+columnName);
            } else {
            	columnName = new SpringPhysicalNamingStrategy()
            				.toPhysicalColumnName(Identifier.toIdentifier(name), jdbcEnv)
            				.getText();
            	
            	//log.info("-- 컬럼 이름 : "+columnName);
            }

            if (columnPosition != null && columnPosition.value() > 0) {
                position = columnPosition.value();
            }

            if (columnName != null) {
                for (ColumnDefinition columnDefinition : columnDefinitions) {
                    if (columnDefinition.getColumnName() != null) {
                        if (columnName.toLowerCase().trim().
                        		equals(columnDefinition.getColumnName().toLowerCase().trim())) { 
                            columnDefinition.setPosition(position);
                        } // if
                    }
                } // for
                
            } // if
        }
    } //
    
    // 기존 DDL 제거
    public static void clearDDLFile(String fileName) throws IOException {
    	
        FileWriter fwOb = new FileWriter(fileName, false); 
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }
    
    // DDL 파일 변경
    public static void modifyDDLFile(File file, String newDDL) throws IOException {
    	
    	log.info("###### DDL(SQL) 파일 읽기");
    	
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
    	
    	String fileContent = "";
    	String temp = "";
    	
    	while ((temp = br.readLine()) != null) {
    		fileContent += temp;
    	} // while
    	
       	// log.info("fileContent : " + fileContent);
    	
    	FileWriter outFile = new FileWriter(file);
    	
    	// log.info("create table index : " + fileContent.indexOf("create table"));
    	fileContent = fileContent.substring(0, fileContent.indexOf("create table"));
    	fileContent += newDDL;
    	fileContent = fileContent.replaceAll(",", ",\n\t\t\t").replaceAll(";", ";\n");
    	
    	// log.info("fileContent : " + fileContent);
    	
    	outFile.write(fileContent);

    	outFile.close();
    	fr.close();
    } //
    
    // 경로명과 확장자 분리
    
} //