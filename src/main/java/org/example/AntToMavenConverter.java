package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.List;

@Slf4j
@SpringBootApplication
public class AntToMavenConverter implements CommandLineRunner {


    @Value("${ant.codebase}")
    private String antCodeBase;

    @Value("${maven.codebase}")
    private String mvnCodeBase;

    @Value("${modules}")
    private List<String> modules;

    public static void main( String[] args )
    {
        SpringApplication.run(AntToMavenConverter.class, args);
    }

    @Override
    public void run( String... args ) throws Exception
    {
    }
}