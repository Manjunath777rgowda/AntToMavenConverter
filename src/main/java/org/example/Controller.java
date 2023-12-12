package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URL;
import java.util.List;

@Slf4j
@RequestMapping
@RestController
public class Controller {


    @Value("${ant.codebase}")
    private File antCodeBase;

    @Value("${maven.codebase}")
    private File mvnCodeBase;

    @Value("${maven.dev}")
    private File mvnDev;

    @Value("${modules}")
    private List<String> modules;

    @GetMapping("/maven")
    public void maven() throws Exception
    {
        initProcess();
        createMvnProject();
    }

    @GetMapping("/backup")
    public void backup() throws Exception
    {
        log.info("Backup pom.xml");
        File backup = new File("backup");
        File pom = new File(backup.getAbsoluteFile() + File.separator + "pom");
        File intellij = new File(backup.getAbsoluteFile() + File.separator + "intellij");
        backupPomFile(pom);
        log.info("Backup .idea");
        FileUtils.copyDirectory(new File(mvnDev.getAbsoluteFile() + File.separator + ".idea"), intellij);
        log.info("Backup Complete");
    }

    private void cleanCodeBase( File moduleFolder ) throws Exception
    {
        File[] files = moduleFolder.listFiles();
        for( File file : files )
        {
            if( !file.getName().equals("src") && !file.getName().equals("database") )
                FileUtil.deleteFolder(file);
        }
    }

    private void initProcess() throws Exception
    {
        FileUtil.deleteFolder(mvnCodeBase);
        FileUtil.createFolder(mvnCodeBase);
        FileUtil.createFolder(mvnDev);
        copyPomFiles(mvnDev);
    }

    private void createMvnProject()
    {
        try
        {
            log.info("Copying ant code base");
            for( String module : modules )
            {
                File antCodeBaseFolder = new File(antCodeBase + File.separator + module);
                File moduleFolder = new File(mvnDev.getAbsoluteFile() + File.separator + module);
                log.info("Copying : {}", moduleFolder);
                FileUtils.copyDirectory(antCodeBaseFolder, moduleFolder);
                log.info("Cleaning : {}", moduleFolder);
                cleanCodeBase(moduleFolder);
                log.info("Creating Maven structure in : {}", moduleFolder);
                createMavenProjectStructure(moduleFolder);
            }
            log.info("Successfully coppied ant code base");
        }
        catch( Exception e )
        {
            throw new RuntimeException(e);
        }
    }

    private void createMavenProjectStructure( File moduleFolder ) throws Exception
    {
        File src = new File(moduleFolder.getAbsoluteFile() + File.separator + "src");
        File main = new File(src.getAbsoluteFile() + File.separator + "main");
        File java = new File(main.getAbsoluteFile() + File.separator + "java");
        File temp = new File(moduleFolder.getAbsoluteFile() + File.separator + "temp");
        src.renameTo(temp);

        FileUtil.createFolder(src);
        FileUtil.createFolder(main);
        FileUtil.createFolder(java);
        FileUtils.copyDirectory(temp, java);
        FileUtil.deleteFolder(temp);

        if( moduleFolder.getName().equals("Server") )
        {
            File oldWebApp = new File(java.getAbsoluteFile() + File.separator + "webapp");
            File newWebApp = new File(main.getAbsoluteFile() + File.separator + "webapp");
            FileUtils.copyDirectory(oldWebApp, newWebApp);
            FileUtil.deleteFolder(oldWebApp);

            File newResources = new File(main.getAbsoluteFile() + File.separator + "resources");
            File oldResources = new File(antCodeBase.getAbsoluteFile() + File.separator + "Resources");
            FileUtils.copyDirectory(oldResources, newResources);
        }

        copyPomFiles(moduleFolder);
    }

    private void copyPomFiles( File moduleFolder ) throws Exception
    {
        URL url = getClass().getClassLoader().getResource(
                "backup/pom" + File.separator + moduleFolder.getName() + "_pom.xml");
        FileUtils.copyFile(new File(url.toURI()),
                new File(moduleFolder.getAbsoluteFile() + File.separator + "pom.xml"));
    }

    private void backupPomFile( File backup ) throws Exception
    {
        String pomFileName = "pom.xml";
        File mvnPomFile = new File(mvnDev + File.separator + pomFileName);
        File backupPomFileName = new File(backup.getAbsoluteFile() + File.separator + "Dev.xml");
        if( mvnPomFile.exists() )
            FileUtils.copyFile(mvnPomFile, backupPomFileName);

        for( String module : modules )
        {
            mvnPomFile = new File(mvnDev + File.separator + module + File.separator + pomFileName);
            backupPomFileName = new File(backup.getAbsoluteFile() + File.separator + module + ".xml");
            if( mvnPomFile.exists() )
                FileUtils.copyFile(mvnPomFile, backupPomFileName);
        }


    }
}
