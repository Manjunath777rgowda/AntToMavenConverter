package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;

@Slf4j
public class FileUtil {

    public static void createFolder( File file) throws Exception
    {
        boolean mkdir = file.mkdir();
        if( !mkdir )
        {
            log.error("Failed creating folder : {}", file);
            throw new Exception("Failed creating folder : {}" + file.getAbsoluteFile());
        }
    }

    public static void deleteFolder( File file ) throws Exception
    {
        if( !file.exists() )
            return;

        boolean delete = FileUtils.deleteQuietly(file);
        if( !delete )
        {
            log.error("Failed Delete folder : {}", file);
            throw new Exception("Failed Delete folder : {}" + file.getAbsoluteFile());
        }
    }
}
