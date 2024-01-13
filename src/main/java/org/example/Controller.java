package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        replaceIntellijFiles();
        createMvnProject();
        postProcess();
        //                runCommands();
        System.out.println("*************Process Completed************");
    }

    private void postProcess() throws Exception
    {
        checkGitAtEnd();
    }

    private void runCommands() throws Exception
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("commands");
        String commandText = IOUtils.toString(stream, StandardCharsets.UTF_8);
        String[] commands = commandText.split("\n");

        for( String command : commands )
            executeCommand(mvnDev, command.replaceAll("[\n\r]", ""));
    }

    private CommandResult executeCommand( File dir, String command ) throws Exception
    {
        log.info("Executing command in : {}", dir.getAbsoluteFile());
        CommandResult commandResult = new CommandResult();
        commandResult.setCommand(command);
        commandResult.setPath(dir.getAbsolutePath());
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command.split(" "));
        builder.directory(dir);
        Process process = builder.start();
        BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        do
        {
            line = inStreamReader.readLine();
            if( line != null )
            {
                commandResult.setResult(commandResult.getCommand() + "\n" + line);
                System.out.println(line);
            }
        } while( line != null );

        int exitCode = process.waitFor();
        commandResult.setExitCode(exitCode);
        if( exitCode == 0 )
            log.info("Command '{}' Executed Successfully", command);
        else
            log.error("Command '{}' Failed to Execute", command);

        return commandResult;
    }

    private void replaceIntellijFiles() throws Exception
    {
        log.info("Replacing Intellij Files");
        File backup = new File("backup");
        File intellij = new File(backup.getAbsoluteFile() + File.separator + "intellij");
        FileUtils.copyDirectory(intellij, new File(mvnDev.getAbsoluteFile() + File.separator + ".idea"));
        log.info("Successfully replaced Intellij Files");
    }

    private void backup() throws Exception
    {
        log.info("Backup pom.xml");
        File backup = new File("backup");
        File pom = new File(backup.getAbsoluteFile() + File.separator + "pom");
        File intellij = new File(backup.getAbsoluteFile() + File.separator + "intellij");
        backupPomFile(pom);
        log.info("Backup .idea");
        File idea = new File(mvnDev.getAbsoluteFile() + File.separator + ".idea");
        if( idea.exists() )
            FileUtils.copyDirectory(idea, intellij);

        log.info("Backup Complete");
    }

    private void cleanCodeBase( File moduleFolder ) throws Exception
    {
        File[] files = moduleFolder.listFiles();
        for( File file : files )
        {
            if( !file.getName().equals("src") && !file.getName().equals("database") && !file.getName().equals(
                    "resources") )
                FileUtil.deleteFolder(file);
        }
    }

    private void initProcess() throws Exception
    {
        backup();
        FileUtil.createFolder(mvnCodeBase);
        FileUtil.createFolder(mvnDev);
        checkGitAtStart();
        copyPomFiles(mvnDev);
    }

    private void checkGitAtStart() throws Exception
    {
        boolean gitExist = isGitClean();
        if( gitExist )
            checkOutToMaster();
    }

    private void checkGitAtEnd() throws Exception
    {
        boolean gitExist = isGitExist();
        if( !gitExist )
        {
            gitInit();
        }
    }

    private void checkOutToMaster() throws Exception
    {
        CommandResult commandResult = executeCommand(mvnDev, "cmd.exe /c git checkout master");

        if( commandResult.getExitCode() != 0 )
        {
            log.error("Unable To checkout to master");
            throw new Exception("Unable To checkout to master");
        }
    }

    private void gitInit() throws Exception
    {
        CommandResult commandResult = executeCommand(mvnDev, "cmd.exe /c git init && git add .");

        if( commandResult.getExitCode() != 0 )
        {
            log.error("Unable To checkout to master");
            throw new Exception("Unable To checkout to master");
        }
    }

    private boolean isGitExist() throws Exception
    {
        CommandResult commandResult = executeCommand(mvnDev, "cmd.exe /c git status");
        return commandResult.getExitCode() == 0;
    }

    private boolean isGitClean() throws Exception
    {
        CommandResult commandResult = executeCommand(mvnDev, "cmd.exe /c git status");

        if( isGitExist() )
        {
            if( !commandResult.getResult().contains("nothing to commit, working tree clean") )
            {
                log.error("Changes found in the git");
                throw new Exception("Changes found in the git");
            }
            return true;
        }
        else
            return false;

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
                FileUtil.deleteFolder(moduleFolder);
                FileUtils.copyDirectory(antCodeBaseFolder, moduleFolder);
                log.info("Cleaning : {}", moduleFolder);
                cleanCodeBase(moduleFolder);
                log.info("Creating Maven structure in : {}", moduleFolder);
                createMavenProjectStructure(moduleFolder);
            }
            log.info("Successfully Created Maven Project");
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

            oldResources = new File(moduleFolder.getAbsoluteFile() + File.separator + "resources");
            FileUtils.copyDirectory(oldResources, newResources);
            FileUtil.deleteFolder(oldResources);
            FileUtil.deleteFolder(new File(newResources.getAbsoluteFile() + File.separator + "log4j2.properties"));

            File backup = new File("backup");
            oldResources = new File(backup.getAbsoluteFile() + File.separator + "properties");
            FileUtils.copyDirectory(oldResources, newResources);

            File reports = new File(newResources.getAbsoluteFile() + File.separator + "Reports");
            FileUtils.copyDirectory(reports, new File(moduleFolder.getAbsoluteFile() + File.separator + "Reports"));
            FileUtil.deleteFolder(reports);
        }

        copyPomFiles(moduleFolder);
    }

    private void copyPomFiles( File moduleFolder ) throws Exception
    {
        File backup = new File("backup");
        String modulePath = moduleFolder.getAbsoluteFile() + File.separator;
        File destFile = new File(modulePath + "pom.xml");
        File srcFile = new File(
                backup.getAbsoluteFile() + File.separator + "pom" + File.separator + moduleFolder.getName() + ".xml");
        FileUtils.copyFile(srcFile, destFile);
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
