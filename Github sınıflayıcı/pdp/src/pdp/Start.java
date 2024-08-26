package pdp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.*;
import java.util.stream.Stream;

public class Start {
    public static void main(String[] args) {

        // get the github repository link and make sure it's a valid github link
        String githubLinkPattern = "https?://github\\.com/[\\w\\-]+/[\\w\\-]+";
        String githubLink;
        Scanner scanner = new Scanner(System.in);
        
        
        while (true) {
            System.out.print("Enter the github link: ");
            githubLink = scanner.nextLine();
            if(Pattern.matches(githubLinkPattern, githubLink)){ break; }
            System.out.print("Seems like you entered an invalid github repository link. Please try again.\n");
        }
        
        scanner.close();

        
        System.out.print("Pulling repository: " + githubLink + "\n\n");
        
        String command = "git clone " + githubLink;
        try {  // clone the repository
            ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s+"));

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if( exitCode == 0 ){
                System.out.println("Successfuly cloned the repository.\n");
            } else {
                System.out.println("Process exited with code " + exitCode);
                System.out.println("Fix the errors and try again.\n");
                return;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
         

        // String directoryPath = "Odev1Ornek";
        String[] tmp = githubLink.split("/");
        String directoryPath = tmp[tmp.length - 1].replace(".git", "");
        Path absolutePath = Paths.get(directoryPath).toAbsolutePath();
        
        ArrayList<File> javaFilesList = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(absolutePath)) {
            paths.filter(path -> !path.endsWith(".git") && !path.toFile().isHidden()) // Filter out .git folder and hidden files
                 .filter(Files::isRegularFile) // Filter out regular files
                 .filter(path -> path.toString().endsWith(".java")) // Filter Java files
                 .map(Path::toFile) // Convert Path to File
                 .forEach(javaFilesList::add); // Add each File to the list
        } catch (IOException e) {
            e.printStackTrace();
        }

        File[] javaFiles = javaFilesList.toArray(new File[0]);

        System.out.println("\nJava files:");
        for (File javaFile : javaFiles) {
            System.out.println(javaFile.getName());
        }
        System.out.println("\n\n");

        String javadocsPattern = "/\\*\\*(?s)(.*?)\\*/";
        String classPattern = "class .* \\{(?s)(.+)\\}";  // best regex for class regex
        String commentPatternMultiline = "/\\*[^*](?s)(.*?)\\*/";
        String commentPatternSingleLine = "^(\\s*//.*)";
        String commentPattern = "//";
        String codeLinePattern = ".+";
        String codeSingleLinePattern = "^\\s*(//|/\\*|\\*)";
        String functionPattern = "(\\b(?!else\\b)\\w+\\b)\\s[a-zA-Z0-9]+\\s?\\(.*\\)(?s).*?\\{";
        
        for (File javaFile : javaFiles) {
            String fileName = javaFile.getName();
            int javaDocCount = 0;
            int commentCount = 0;
            int codeLineCount = 0;
            int lineOfCodeCount = 0;  // including comments and spaces
            int functionCount = 0;

           
            // read each java file
            try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
                Path filePath = Paths.get(javaFile.getPath());
                String fileContent = Files.readString(filePath);

                lineOfCodeCount = fileContent.trim().split("\n").length;

                Pattern compiledClassPattern = Pattern.compile(classPattern);
                Matcher classMatcher = compiledClassPattern.matcher(fileContent);

                
                ArrayList<String> classes = new ArrayList<>();
                
                String classMatch;

                if( !classMatcher.find() ){
                    System.out.println(fileName + " is not a class file. Moving on.");
                    continue;
                } else {
                    classMatch = classMatcher.group(0);
                    classes.add(classMatch);
                }
                
                while (classMatcher.find()) {
                    classMatch = classMatcher.group(0);
                    classes.add(classMatch);
                }

                for(String _class : classes){

                    Pattern compiledJavadocPattern = Pattern.compile(javadocsPattern);
                    Matcher javadocMatcher = compiledJavadocPattern.matcher(fileContent);
                    
                    Pattern compiledCommentPatternMultiline = Pattern.compile(commentPatternMultiline);
                    Matcher commentMatcherMultiline = compiledCommentPatternMultiline.matcher(_class);

                    Pattern compiledCommentPatternSingleLine = Pattern.compile(commentPatternSingleLine);
                    Matcher commentMatcherSingleLine = compiledCommentPatternSingleLine.matcher(_class);
                    // System.out.println(commentMatcherSingleLine.find());


                    Pattern compiledCommentPattern = Pattern.compile(commentPattern);
                    Matcher commentMatcher = compiledCommentPattern.matcher(_class);
                    
                    Pattern compiledCodeLinePattern = Pattern.compile(codeLinePattern);
                    Matcher codeLineMatcher = compiledCodeLinePattern.matcher(fileContent);
                    
                    Pattern compiledFunctionPattern = Pattern.compile(functionPattern);
                    Matcher functionMatcher = compiledFunctionPattern.matcher(_class);
    
                    int leftoverJavadocCount = 0;
                    while (javadocMatcher.find()) {
                        String match = javadocMatcher.group();
                        // System.out.println("Found match: " + match.trim());
                        String[] javadocLines = match.split("\n");
                        javaDocCount += javadocLines.length -2;
                        leftoverJavadocCount += 2;
                    }

                    int leftoverCommentCount = 0;
                    int multilineComment = 0;
                    while (commentMatcherMultiline.find()) {
                        String match = commentMatcherMultiline.group(); // Get the matched string
                        // System.out.println("Comment match: " + match.trim());
                        String[] commentLines = match.trim().split("\n");
                        commentCount += commentLines.length - 2;
                        multilineComment += commentLines.length;
                        leftoverCommentCount += 2;
                    }
                    int singleLineComments = 0;
                    /*
                     
                    while (commentMatcherSingleLine.find()) {
                        String match = commentMatcherSingleLine.group(); // Get the matched string
                        
                        System.out.println("Comment match: " + match);
                        singleLineComments ++;
                    }
                    */
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.matches(commentPatternSingleLine)) {
                            singleLineComments ++;
                        } 
                    }
                    while (commentMatcher.find()) {
                        String match = commentMatcher.group(); // Get the matched string
                        // System.out.println("Comment match: " + match.trim());
                        String[] commentLines = match.trim().split("\n");
                        commentCount += commentLines.length;
                    }

                    
                    while (codeLineMatcher.find()) {
                        String match = codeLineMatcher.group();
                        if(match.trim().length() > 0){
                            // System.out.println("Found match: " + match.trim());
                            String[] codeLines = match.trim().split("\n");
                            codeLines = Arrays.stream(codeLines)
                            .filter(l -> !l.isEmpty())
                            .toArray(String[]::new);
                            // for (String line : codeLines) { System.out.println(line); }
                            codeLineCount += codeLines.length;
                        }
                    }
                    
                    
                    while (functionMatcher.find()) {functionCount ++;}
                    
                    // code lines are non empty, non comment non javadoc lines
                    codeLineCount -= (multilineComment + singleLineComments + javaDocCount + leftoverJavadocCount);
                    
                    /*
                        YG=[(Javadoc_Satır_Sayısı + Diğer_yorumlar_satır_sayısı)*0.8]/Fonksiyon_Sayisi
                        YH= (Kod_satir_sayisi/Fonksiyon_Sayisi)*0.3
                        Yorum Sapma Yüzdesinin Hesabı: [(100*YG)/YH]-100
                     */
                    double YG = ((javaDocCount + commentCount) * 0.8) / functionCount;
                    double YH = (codeLineCount / (double) functionCount) * 0.3;
                    double yorumSapmaYuzdesi = ((100 * YG) / YH) - 100;
                    
                    System.out.println("================================");
                    System.out.println("Sınıf: " + fileName);
                    System.out.println("Javadoc Satır Sayısı: " + javaDocCount);
                    System.out.println("Yorum Satır Sayısı: " + commentCount);
                    System.out.println("Kod Satır Sayısı: " + codeLineCount);
                    System.out.println("LOC: " + lineOfCodeCount);
                    System.out.println("Fonksiyon Sayısı: " + functionCount);
                    System.out.println("Yorum Sapma Yüzdesi: %" + Math.round(yorumSapmaYuzdesi * 100.0) / 100.0);
                    System.out.println("================================");
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}