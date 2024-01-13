package org.example;

import lombok.Data;

@Data
public class CommandResult {
    private String result;
    private String command;
    private String path;
    private Integer exitCode;
}
