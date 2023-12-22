# AntToMavenConverter
This application will help to convert the ant project to maven project

## Overview
AntToMavenConverter is a tool designed to simplify the process of converting Ant projects to Maven projects. This tool takes the path of an existing Ant project and generates the corresponding Maven project structure, 
Note:- pom.xml file should be generated manually

## Features
- Converts Ant projects to Maven projects.
- Automatically generates the necessary Maven project structure.
- Utilizes a predefined pom.xml file for consistency.

## Prerequisites

Make sure you have the following installed on your system:

- Java (JDK 8 or higher)
- Maven
- IntelliJ

## Usage
1. Clone the repository to your local machine:
```bash
git clone https://github.com/Manjunath777rgowda/AntToMavenConverter.git
```
2. Navigate to the project directory:
```bash
cd AntToMavenConverter
```
3. Change `ant.codebase` to your ant project folder
4. Change `maven.codebase` to your maven project folder
5. Change `modules` to select which are the modules to be converted
6Maven clean and install
```bash
mvn clean install
```
7. Run the application in Intellij
8. Use API `/maven`

